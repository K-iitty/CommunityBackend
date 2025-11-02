package com.community.property.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公告详情展示对象
 */
@Data
@Schema(description = "公告详情")
public class NoticeDetailVO {
    
    @Schema(description = "公告ID")
    private Long id;
    
    @Schema(description = "公告标题")
    private String title;
    
    @Schema(description = "公告内容")
    private String content;
    
    @Schema(description = "公告类型")
    private String noticeType;
    
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;
    
    @Schema(description = "生效时间")
    private LocalDateTime startTime;
    
    @Schema(description = "失效时间")
    private LocalDateTime endTime;
    
    @Schema(description = "是否紧急")
    private Integer isUrgent;
    
    @Schema(description = "是否置顶")
    private Integer isTop;
    
    @Schema(description = "阅读次数")
    private Integer readCount;
    
    @Schema(description = "附件信息")
    private String attachments;
    
    @Schema(description = "公告图片")
    private String noticeImages;
    
    @Schema(description = "活动日期")
    private String activityDate;
    
    @Schema(description = "活动时间")
    private String activityTime;
    
    @Schema(description = "活动地点")
    private String activityLocation;
    
    @Schema(description = "活动组织者")
    private String activityOrganizer;
    
    @Schema(description = "活动联系人")
    private String activityContact;
    
    @Schema(description = "活动联系电话")
    private String activityContactPhone;
}

