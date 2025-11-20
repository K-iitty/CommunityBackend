package com.community.owner.config;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Redis消息监听器
 * 监听其他模块的数据变更消息，实现实时数据同步
 */
@Component
@Slf4j
public class RedisMessageListener implements MessageListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String messageBody = new String(message.getBody());
            
            log.info("Received Redis message from channel: {}", channel);
            
            // 解析消息内容
            Map<String, Object> messageData = objectMapper.readValue(messageBody, Map.class);
            
            // 检查消息类型
            String messageType = (String) messageData.get("type");
            
            if ("notification".equals(messageType)) {
                // 处理通知消息
                handleNotificationMessage(messageData);
            } else {
                // 处理数据同步消息
                String module = (String) messageData.get("module");
                String action = (String) messageData.get("action");
                String entityType = (String) messageData.get("entityType");
                Object entityId = messageData.get("entityId");
                
                // 如果是来自其他模块的消息，处理数据同步
                if (!"owner".equals(module)) {
                    handleDataSync(channel, module, action, entityType, entityId, messageData);
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing Redis message", e);
        }
    }

    /**
     * 处理数据同步逻辑
     */
    private void handleDataSync(String channel, String module, String action, String entityType, Object entityId, Map<String, Object> messageData) {
        log.info("Processing data sync: module={}, action={}, entityType={}, entityId={}", 
                module, action, entityType, entityId);
        
        // 检查必要参数是否为空
        if (entityType == null) {
            log.warn("EntityType is null, skipping message processing");
            return;
        }
        
        // 根据不同的实体类型和操作类型，执行相应的同步逻辑
        switch (entityType) {
            case "CommunityNotice":
                handleNoticeSync(action, entityId, messageData);
                break;
            case "CommunityInfo":
                handleCommunityInfoSync(action, entityId, messageData);
                break;
            case "House":
                handleHouseSync(action, entityId, messageData);
                break;
            case "Owner":
                handleOwnerSync(action, entityId, messageData);
                break;
            case "OwnerIssue":
                handleOwnerIssueSync(action, entityId, messageData);
                break;
            default:
                log.debug("No specific sync handler for entity type: {}", entityType);
        }
    }

    private void handleNoticeSync(String action, Object entityId, Map<String, Object> messageData) {
        // 处理公告同步逻辑
        log.info("Syncing notice data: action={}, entityId={}", action, entityId);
        // 这里可以添加具体的缓存更新或其他同步逻辑
    }

    private void handleCommunityInfoSync(String action, Object entityId, Map<String, Object> messageData) {
        // 处理社区信息同步逻辑
        log.info("Syncing community info data: action={}, entityId={}", action, entityId);
    }

    private void handleHouseSync(String action, Object entityId, Map<String, Object> messageData) {
        // 处理房屋信息同步逻辑
        log.info("Syncing house data: action={}, entityId={}", action, entityId);
    }

    private void handleOwnerSync(String action, Object entityId, Map<String, Object> messageData) {
        // 处理业主信息同步逻辑
        log.info("Syncing owner data: action={}, entityId={}", action, entityId);
    }

    private void handleOwnerIssueSync(String action, Object entityId, Map<String, Object> messageData) {
        // 处理业主问题同步逻辑
        log.info("Syncing owner issue data: action={}, entityId={}", action, entityId);
        // 这里可以添加具体的缓存更新或其他同步逻辑
    }

    /**
     * 处理通知消息
     */
    private void handleNotificationMessage(Map<String, Object> messageData) {
        String targetModule = (String) messageData.get("targetModule");
        String notificationType = (String) messageData.get("notificationType");
        String title = (String) messageData.get("title");
        String content = (String) messageData.get("content");
        
        log.info("Received notification: targetModule={}, type={}, title={}", 
                targetModule, notificationType, title);
        
        // 这里可以添加具体的通知处理逻辑，比如推送给前端
        // 对于公告删除通知，可以触发前端刷新
        if ("NOTICE_DELETE".equals(notificationType)) {
            log.info("Notice deleted, triggering frontend refresh");
            // 这里可以通过WebSocket或SSE通知前端刷新
        } else if ("ISSUE_DELETE".equals(notificationType)) {
            log.info("Owner issue deleted, triggering frontend refresh");
            // 这里可以通过WebSocket或SSE通知前端刷新
        }
    }
}
