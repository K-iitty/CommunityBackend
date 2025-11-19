package com.community.owner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis消息发布服务
 * 用于实现跨模块的实时数据同步
 */
@Service
@Slf4j
public class RedisMessageService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 定义消息主题常量
    public static final String TOPIC_DATA_CHANGE = "community:data:change";
    public static final String TOPIC_OWNER_CHANGE = "community:owner:change";
    public static final String TOPIC_PROPERTY_CHANGE = "community:property:change";
    public static final String TOPIC_ADMIN_CHANGE = "community:admin:change";

    /**
     * 发布数据变更消息
     * @param module 模块名称 (owner/property/admin)
     * @param action 操作类型 (CREATE/UPDATE/DELETE)
     * @param entityType 实体类型 (如: Owner, House, Notice等)
     * @param entityId 实体ID
     * @param data 变更的数据
     */
    public void publishDataChange(String module, String action, String entityType, Object entityId, Object data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("module", module);
            message.put("action", action);
            message.put("entityType", entityType);
            message.put("entityId", entityId);
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());

            // 发布到通用主题
            redisTemplate.convertAndSend(TOPIC_DATA_CHANGE, message);
            
            // 发布到模块特定主题
            String moduleSpecificTopic = "community:" + module.toLowerCase() + ":change";
            redisTemplate.convertAndSend(moduleSpecificTopic, message);

            log.info("Published data change message: module={}, action={}, entityType={}, entityId={}", 
                    module, action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to publish data change message", e);
        }
    }

    /**
     * 发布业主相关数据变更
     */
    public void publishOwnerChange(String action, String entityType, Object entityId, Object data) {
        publishDataChange("owner", action, entityType, entityId, data);
    }

    /**
     * 发布物业相关数据变更
     */
    public void publishPropertyChange(String action, String entityType, Object entityId, Object data) {
        publishDataChange("property", action, entityType, entityId, data);
    }

    /**
     * 发布管理端相关数据变更
     */
    public void publishAdminChange(String action, String entityType, Object entityId, Object data) {
        publishDataChange("admin", action, entityType, entityId, data);
    }

    /**
     * 发布实时通知消息
     * @param targetModule 目标模块
     * @param notificationType 通知类型
     * @param title 标题
     * @param content 内容
     * @param targetUserId 目标用户ID（可选）
     */
    public void publishNotification(String targetModule, String notificationType, String title, String content, Object targetUserId) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "notification");
            notification.put("targetModule", targetModule);
            notification.put("notificationType", notificationType);
            notification.put("title", title);
            notification.put("content", content);
            notification.put("targetUserId", targetUserId);
            notification.put("timestamp", System.currentTimeMillis());

            String topic = "community:" + targetModule.toLowerCase() + ":notification";
            redisTemplate.convertAndSend(topic, notification);

            log.info("Published notification: targetModule={}, type={}, title={}", 
                    targetModule, notificationType, title);
        } catch (Exception e) {
            log.error("Failed to publish notification", e);
        }
    }
}
