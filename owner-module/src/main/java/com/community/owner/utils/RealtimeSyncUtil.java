package com.community.owner.utils;

import com.community.owner.service.RedisMessageService;
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
            redisMessageService.publishOwnerChange("CREATE", entityType, entityId, data);
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
            redisMessageService.publishOwnerChange("UPDATE", entityType, entityId, data);
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
            redisMessageService.publishOwnerChange("DELETE", entityType, entityId, null);
            log.info("Published DELETE message for {}: {}", entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to publish DELETE message for {}: {}", entityType, entityId, e);
        }
    }

    /**
     * 发布业主问题相关通知
     * @param action 操作类型
     * @param ownerName 业主姓名
     * @param issueTitle 问题标题
     */
    public void publishIssueNotification(String action, String ownerName, String issueTitle) {
        try {
            String title = "";
            String content = "";
            
            switch (action) {
                case "CREATE":
                    title = "新问题反馈";
                    content = "业主 " + ownerName + " 提交了新的问题反馈：" + issueTitle;
                    break;
                case "UPDATE":
                    title = "问题更新";
                    content = "业主 " + ownerName + " 更新了问题：" + issueTitle;
                    break;
                case "RESOLVE":
                    title = "问题已解决";
                    content = "问题已解决：" + issueTitle;
                    break;
            }
            
            // 通知物业端和管理端
            redisMessageService.publishNotification("property", "ISSUE_" + action, title, content, null);
            redisMessageService.publishNotification("admin", "ISSUE_" + action, title, content, null);
            
            log.info("Published issue notification: {} - {}", title, content);
        } catch (Exception e) {
            log.error("Failed to publish issue notification", e);
        }
    }

    /**
     * 发布公告相关通知
     * @param action 操作类型
     * @param noticeTitle 公告标题
     */
    public void publishNoticeNotification(String action, String noticeTitle) {
        try {
            String title = "";
            String content = "";
            
            switch (action) {
                case "CREATE":
                    title = "新公告发布";
                    content = "发布了新公告：" + noticeTitle;
                    break;
                case "UPDATE":
                    title = "公告更新";
                    content = "公告已更新：" + noticeTitle;
                    break;
                case "DELETE":
                    title = "公告删除";
                    content = "公告已删除：" + noticeTitle;
                    break;
            }
            
            // 通知所有端
            redisMessageService.publishNotification("owner", "NOTICE_" + action, title, content, null);
            redisMessageService.publishNotification("property", "NOTICE_" + action, title, content, null);
            redisMessageService.publishNotification("admin", "NOTICE_" + action, title, content, null);
            
            log.info("Published notice notification: {} - {}", title, content);
        } catch (Exception e) {
            log.error("Failed to publish notice notification", e);
        }
    }

    /**
     * 发布房屋信息相关通知
     * @param action 操作类型
     * @param houseAddress 房屋地址
     */
    public void publishHouseNotification(String action, String houseAddress) {
        try {
            String title = "";
            String content = "";
            
            switch (action) {
                case "CREATE":
                    title = "新房屋信息";
                    content = "新增房屋：" + houseAddress;
                    break;
                case "UPDATE":
                    title = "房屋信息更新";
                    content = "房屋信息已更新：" + houseAddress;
                    break;
                case "DELETE":
                    title = "房屋信息删除";
                    content = "房屋信息已删除：" + houseAddress;
                    break;
            }
            
            // 通知相关端
            redisMessageService.publishNotification("property", "HOUSE_" + action, title, content, null);
            redisMessageService.publishNotification("admin", "HOUSE_" + action, title, content, null);
            
            log.info("Published house notification: {} - {}", title, content);
        } catch (Exception e) {
            log.error("Failed to publish house notification", e);
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
