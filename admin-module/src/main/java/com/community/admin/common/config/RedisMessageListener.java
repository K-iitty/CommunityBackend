package com.community.admin.common.config;

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
            
            String module = (String) messageData.get("module");
            String action = (String) messageData.get("action");
            String entityType = (String) messageData.get("entityType");
            Object entityId = messageData.get("entityId");
            
            // 如果是来自其他模块的消息，处理数据同步
            if (!"admin".equals(module)) {
                handleDataSync(channel, module, action, entityType, entityId, messageData);
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
            case "Building":
                handleBuildingSync(action, entityId, messageData);
                break;
            default:
                log.debug("No specific sync handler for entity type: {}", entityType);
        }
    }

    private void handleNoticeSync(String action, Object entityId, Map<String, Object> messageData) {
        // 处理公告同步逻辑
        log.info("Syncing notice data: action={}, entityId={}", action, entityId);
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
    }

    private void handleBuildingSync(String action, Object entityId, Map<String, Object> messageData) {
        // 处理建筑信息同步逻辑
        log.info("Syncing building data: action={}, entityId={}", action, entityId);
    }
}
