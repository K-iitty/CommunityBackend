package com.community.admin.common.util;

import com.community.admin.service.RedisMessageService;
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
            redisMessageService.publishAdminChange("CREATE", entityType, entityId, data);
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
            redisMessageService.publishAdminChange("UPDATE", entityType, entityId, data);
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
            redisMessageService.publishAdminChange("DELETE", entityType, entityId, null);
            log.info("Published DELETE message for {}: {}", entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to publish DELETE message for {}: {}", entityType, entityId, e);
        }
    }

    /**
     * 发布系统公告通知
     * @param action 操作类型
     * @param noticeTitle 公告标题
     * @param adminName 管理员姓名
     */
    public void publishSystemNoticeNotification(String action, String noticeTitle, String adminName) {
        try {
            String title = "";
            String content = "";
            
            switch (action) {
                case "CREATE":
                    title = "系统公告发布";
                    content = "管理员 " + adminName + " 发布了新公告：" + noticeTitle;
                    break;
                case "UPDATE":
                    title = "系统公告更新";
                    content = "管理员 " + adminName + " 更新了公告：" + noticeTitle;
                    break;
                case "DELETE":
                    title = "系统公告删除";
                    content = "管理员 " + adminName + " 删除了公告：" + noticeTitle;
                    break;
            }
            
            // 通知所有端
            redisMessageService.publishNotification("owner", "SYSTEM_NOTICE_" + action, title, content, null);
            redisMessageService.publishNotification("property", "SYSTEM_NOTICE_" + action, title, content, null);
            
            log.info("Published system notice notification: {} - {}", title, content);
        } catch (Exception e) {
            log.error("Failed to publish system notice notification", e);
        }
    }

    /**
     * 发布社区信息更新通知
     * @param action 操作类型
     * @param communityName 社区名称
     * @param adminName 管理员姓名
     */
    public void publishCommunityInfoNotification(String action, String communityName, String adminName) {
        try {
            String title = "";
            String content = "";
            
            switch (action) {
                case "UPDATE":
                    title = "社区信息更新";
                    content = "管理员 " + adminName + " 更新了社区信息：" + communityName;
                    break;
                case "CONFIG":
                    title = "社区配置更新";
                    content = "管理员 " + adminName + " 更新了社区配置：" + communityName;
                    break;
            }
            
            // 通知所有端
            redisMessageService.publishNotification("owner", "COMMUNITY_" + action, title, content, null);
            redisMessageService.publishNotification("property", "COMMUNITY_" + action, title, content, null);
            
            log.info("Published community info notification: {} - {}", title, content);
        } catch (Exception e) {
            log.error("Failed to publish community info notification", e);
        }
    }

    /**
     * 发布用户管理通知
     * @param action 操作类型
     * @param userType 用户类型
     * @param userName 用户名称
     * @param adminName 管理员姓名
     */
    public void publishUserManagementNotification(String action, String userType, String userName, String adminName) {
        try {
            String title = "";
            String content = "";
            
            switch (action) {
                case "CREATE":
                    title = "新用户创建";
                    content = "管理员 " + adminName + " 创建了新" + userType + "：" + userName;
                    break;
                case "UPDATE":
                    title = "用户信息更新";
                    content = "管理员 " + adminName + " 更新了" + userType + "信息：" + userName;
                    break;
                case "DISABLE":
                    title = "用户已禁用";
                    content = "管理员 " + adminName + " 禁用了" + userType + "：" + userName;
                    break;
                case "ENABLE":
                    title = "用户已启用";
                    content = "管理员 " + adminName + " 启用了" + userType + "：" + userName;
                    break;
            }
            
            // 根据用户类型通知相应端
            if ("业主".equals(userType)) {
                redisMessageService.publishNotification("owner", "USER_" + action, title, content, null);
            } else if ("物业人员".equals(userType)) {
                redisMessageService.publishNotification("property", "USER_" + action, title, content, null);
            }
            
            log.info("Published user management notification: {} - {}", title, content);
        } catch (Exception e) {
            log.error("Failed to publish user management notification", e);
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
