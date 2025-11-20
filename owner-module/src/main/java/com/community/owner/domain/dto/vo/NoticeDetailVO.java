package com.community.owner.domain.dto.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 公告详情展示对象（用于详情页面）
 * 注意：只包含业主需要看到的字段，隐藏内部管理字段
 */
@Data
public class NoticeDetailVO {
    
    /**
     * 公告ID（用于跳转，但不在页面显示）
     */
    private Long id;
    
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 公告类型:社区公告/活动公告/紧急通知/温馨提示
     */
    private String noticeType;
    
    /**
     * 公告内容
     */
    private String content;
    
    /**
     * 公告图片（JSON数组格式，可能有多张）
     */
    private String noticeImages;
    
    /**
     * 是否紧急（0-否，1-是）
     */
    private Integer isUrgent;
    
    /**
     * 是否置顶（0-否，1-是）
     */
    private Integer isTop;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 阅读次数
     */
    private Integer readCount;
    
    // ===== 活动相关字段（活动公告时显示）=====
    
    /**
     * 活动日期（活动公告时显示）
     */
    private LocalDate activityDate;
    
    /**
     * 活动时间（活动公告时显示）
     */
    private String activityTime;
    
    /**
     * 活动地点（活动公告时显示）
     */
    private String activityLocation;
    
    /**
     * 活动组织者（活动公告时显示）
     */
    private String activityOrganizer;
    
    /**
     * 活动联系人（活动公告时显示）
     */
    private String activityContact;
    
    /**
     * 活动联系电话（活动公告时显示）
     */
    private String activityContactPhone;
    
    // ===== 其他字段 =====
    
    /**
     * 附件信息（JSON格式，可能为null）
     */
    private String attachments;
}

