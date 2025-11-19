package com.community.property.controller;

import com.community.property.service.RedisMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 实时同步控制器
 * 提供SSE (Server-Sent Events) 支持，用于实时推送数据变更
 */
@RestController
@RequestMapping("/api/realtime")
@Slf4j
public class RealtimeSyncController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisMessageService redisMessageService;

    // 存储活跃的SSE连接
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> sseConnections = new ConcurrentHashMap<>();

    /**
     * 建立SSE连接，用于实时推送数据变更
     * @param userId 用户ID
     * @param clientType 客户端类型 (miniprogram/web)
     * @return SseEmitter
     */
    @GetMapping("/subscribe/{userId}")
    public SseEmitter subscribe(@PathVariable String userId, 
                               @RequestParam(defaultValue = "miniprogram") String clientType) {
        
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30分钟超时
        String connectionKey = clientType + ":" + userId;
        
        // 添加到连接池
        sseConnections.computeIfAbsent(connectionKey, k -> new CopyOnWriteArrayList<>()).add(emitter);
        
        log.info("New SSE connection established: userId={}, clientType={}", userId, clientType);
        
        // 设置连接完成和超时的回调
        emitter.onCompletion(() -> {
            removeConnection(connectionKey, emitter);
            log.info("SSE connection completed: userId={}, clientType={}", userId, clientType);
        });
        
        emitter.onTimeout(() -> {
            removeConnection(connectionKey, emitter);
            log.info("SSE connection timeout: userId={}, clientType={}", userId, clientType);
        });
        
        emitter.onError((ex) -> {
            removeConnection(connectionKey, emitter);
            log.error("SSE connection error: userId={}, clientType={}", userId, clientType, ex);
        });
        
        try {
            // 发送连接成功消息
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connection established successfully"));
        } catch (IOException e) {
            log.error("Error sending initial SSE message", e);
            removeConnection(connectionKey, emitter);
        }
        
        return emitter;
    }

    /**
     * 获取最新的数据变更时间戳
     * 用于微信小程序轮询检查是否有数据更新
     */
    @GetMapping("/check-updates")
    public Map<String, Object> checkUpdates(@RequestParam String lastUpdateTime,
                                          @RequestParam(required = false) String entityTypes) {
        
        long lastUpdate = Long.parseLong(lastUpdateTime);
        long currentTime = System.currentTimeMillis();
        
        // 检查Redis中是否有新的数据变更
        String[] types = entityTypes != null ? entityTypes.split(",") : new String[]{"all"};
        
        boolean hasUpdates = false;
        for (String type : types) {
            String key = "community:last_update:" + type;
            String lastUpdateStr = (String) redisTemplate.opsForValue().get(key);
            if (lastUpdateStr != null && Long.parseLong(lastUpdateStr) > lastUpdate) {
                hasUpdates = true;
                break;
            }
        }
        
        return Map.of(
            "hasUpdates", hasUpdates,
            "currentTime", currentTime,
            "message", hasUpdates ? "有新的数据更新" : "暂无数据更新"
        );
    }

    /**
     * 手动触发数据同步通知
     * 主要用于测试
     */
    @PostMapping("/notify")
    public Map<String, Object> notifyDataChange(@RequestBody Map<String, Object> request) {
        
        String module = (String) request.get("module");
        String action = (String) request.get("action");
        String entityType = (String) request.get("entityType");
        Object entityId = request.get("entityId");
        Object data = request.get("data");
        
        // 发布Redis消息
        redisMessageService.publishDataChange(module, action, entityType, entityId, data);
        
        // 更新最后更新时间
        String key = "community:last_update:" + entityType;
        redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()));
        
        // 通过SSE推送给所有连接的客户端
        broadcastToAllConnections("dataChange", request);
        
        return Map.of("success", true, "message", "数据变更通知已发送");
    }

    /**
     * 移除SSE连接
     */
    private void removeConnection(String connectionKey, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> connections = sseConnections.get(connectionKey);
        if (connections != null) {
            connections.remove(emitter);
            if (connections.isEmpty()) {
                sseConnections.remove(connectionKey);
            }
        }
    }

    /**
     * 向所有连接广播消息
     */
    private void broadcastToAllConnections(String eventName, Object data) {
        sseConnections.forEach((connectionKey, emitters) -> {
            emitters.removeIf(emitter -> {
                try {
                    emitter.send(SseEmitter.event().name(eventName).data(data));
                    return false;
                } catch (IOException e) {
                    log.warn("Failed to send SSE message to connection: {}", connectionKey);
                    return true; // 移除失败的连接
                }
            });
        });
    }

    /**
     * 获取当前活跃连接数
     */
    @GetMapping("/connections/count")
    public Map<String, Object> getConnectionCount() {
        int totalConnections = sseConnections.values().stream()
                .mapToInt(CopyOnWriteArrayList::size)
                .sum();
        
        return Map.of(
            "totalConnections", totalConnections,
            "connectionsByType", sseConnections.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().size()
                ))
        );
    }
}
