package com.community.property.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.community.property.entity.IssueFollowUp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题详情展示对象
 */
@Data
@Schema(description = "问题详情")
public class IssueDetailVO {
    
    @Schema(description = "问题ID")
    private Long id;
    
    @Schema(description = "问题标题")
    private String issueTitle;
    
    @Schema(description = "问题详情描述")
    private String issueContent;
    
    @Schema(description = "问题类型")
    private String issueType;
    
    @Schema(description = "问题子类型")
    private String subType;
    
    @Schema(description = "具体位置")
    private String specificLocation;
    
    @Schema(description = "联系人姓名")
    private String contactName;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "紧急程度")
    private String urgencyLevel;
    
    @Schema(description = "问题状态")
    private String issueStatus;
    
    @Schema(description = "工单状态")
    private String workStatus;
    
    @Schema(description = "问题图片")
    private String issueImages;
    
    @Schema(description = "处理方案")
    private String processPlan;
    
    @Schema(description = "处理结果")
    private String processResult;
    
    @Schema(description = "处理结果图片")
    private String resultImages;
    
    @Schema(description = "指派处理人员ID")
    private Long processorStaffId;
    
    @Schema(description = "处理人员姓名")
    private String processorName;
    
    @Schema(description = "处理人员电话")
    private String processorPhone;
    
    @Schema(description = "业主姓名")
    private String ownerName;
    
    @Schema(description = "业主电话")
    private String ownerPhone;
    
    @Schema(description = "是否产生费用")
    private Integer hasCost;
    
    @Schema(description = "总费用")
    private BigDecimal totalCost;
    
    @Schema(description = "费用支付状态")
    private String costPaymentStatus;
    
    @Schema(description = "满意度评分")
    private Integer satisfactionLevel;
    
    @Schema(description = "满意度反馈")
    private String satisfactionFeedback;
    
    @Schema(description = "上报时间")
    private LocalDateTime reportedTime;
    
    @Schema(description = "预计完成时间")
    private LocalDateTime estimatedCompleteTime;
    
    @Schema(description = "实际完成时间")
    private LocalDateTime actualCompleteTime;
    
    @Schema(description = "是否已评价")
    private Integer isEvaluated;
    
    @Schema(description = "跟进记录列表")
    private List<IssueFollowUp> followUps;
}

