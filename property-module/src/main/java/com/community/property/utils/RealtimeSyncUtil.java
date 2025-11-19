package com.community.property.utils;

import com.community.property.service.RedisMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * 实时同步工具类
 * 提供便捷的方法来发布数据变更和通知消息
 */
@Component
@Slf4j
public class RealtimeSyncUtil {

    @Autowired
    private RedisMessageService redisMessageService;

    /**
     * 发布数据创建消息
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param data 数据
     */
    public void publishCreate(String entityType, Object entityId, Object data) {
        try {
            redisMessageService.publishPropertyChange("CREATE", entityType, entityId, data);
            log.info("Published CREATE message for {}: {}", entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to publish CREATE message for {}: {}", entityType, entityId, e);
        }
    }

    /**
     * 发布数据更新消息
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param data 数据
     */
    public void publishUpdate(String entityType, Object entityId, Object data) {
        try {
            redisMessageService.publishPropertyChange("UPDATE", entityType, entityId, data);
            log.info("Published UPDATE message for {}: {}", entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to publish UPDATE message for {}: {}", entityType, entityId, e);
        }
    }

    /**
     * 发布数据删除消息
     * @param entityType 实体类型
     * @param entityId 实体ID
     */
    public void publishDelete(String entityType, Object entityId) {
        try {
            redisMessageService.publishPropertyChange("DELETE", entityType, entityId, null);
            log.info("Published DELETE message for {}: {}", entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to publish DELETE message for {}: {}", entityType, entityId, e);
        }
    }

    /**
     * 发布问题处理通知
     * @param action 操作类型
     * @param issueTitle 问题标题
     * @param staffName 处理人员
     */
    public void publishIssueProcessNotification(String action, String issueTitle, String staffName) {
        try {
            String title = "";
            String content = "";
            
            switch (action) {
                case "ASSIGN":
                    title = "问题已分配";
                    content = "问题已分配给 " + staffName + "：" + issueTitle;
                    break;
                case "PROCESS":
                    title = "问题处理中";
                    content = staffName + " 正在处理问题：" + issueTitle;
                    break;
                case "RESOLVE":
                    title = "问题已解决";
                    content = staffName + " 已解决问题：" + issueTitle;
                    break;
            }
            
            // 通知业主端和管理端
            redisMessageService.publishNotification("owner", "ISSUE_" + action, title, content, null);
            redisMessageService.publishNotification("admin", "ISSUE_" + action, title, content, null);
            
            log.info("Published issue process notification: {} - {}", title, content);
        } catch (Exception e) {
            log.error("Failed to publish issue process notification", e);
        }
    }

    /**
     * 发布维修记录通知
     * @param action 操作类型
     * @param location 维修位置
     * @param description 维修描述
     */
    public void publishMaintenanceNotification(String action, String location, String description) {
        try {
            String title = "";
            String content = "";
            
            switch (action) {
                case "CREATE":
                    title = "新维修记录";
                    content = "在 " + location + " 创建了新的维修记录：" + description;
                    break;
                case "UPDATE":
                    title = "维修记录更新";
                    content = location + " 的维修记录已更新：" + description;
                    break;
                case "COMPLETE":
                    title = "维修完成";
                    content = location + " 的维修已完成：" + description;
                    break;
            }
            
            // 通知管理端
            redisMessageService.publishNotification("admin", "MAINTENANCE_" + action, title, content, null);
            
            log.info("Published maintenance notification: {} - {}", title, content);
        } catch (Exception e) {
            log.error("Failed to publish maintenance notification", e);
        }
    }

    /**
     * 发布通用通知
     * @param targetModule 目标模块
     * @param type 通知类型
     * @param title 标题
     * @param content 内容
     */
    public void publishNotification(String targetModule, String type, String title, String content) {
        try {
            redisMessageService.publishNotification(targetModule, type, title, content, null);
            log.info("Published notification to {}: {} - {}", targetModule, title, content);
        } catch (Exception e) {
            log.error("Failed to publish notification to {}", targetModule, e);
        }
    }
}
