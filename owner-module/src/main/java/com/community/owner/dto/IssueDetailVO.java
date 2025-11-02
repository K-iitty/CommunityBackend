package com.community.owner.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 问题详情展示对象
 */
@Data
public class IssueDetailVO {
    
    /**
     * 问题ID
     */
    private Long id;
    
    /**
     * 问题标题
     */
    private String issueTitle;
    
    /**
     * 问题详情描述
     */
    private String issueContent;
    
    /**
     * 问题类型
     */
    private String issueType;
    
    /**
     * 问题子类型
     */
    private String subType;
    
    /**
     * 具体位置
     */
    private String specificLocation;
    
    /**
     * 联系人姓名
     */
    private String contactName;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 紧急程度
     */
    private String urgencyLevel;
    
    /**
     * 问题状态
     */
    private String issueStatus;
    
    /**
     * 工单状态
     */
    private String workStatus;
    
    /**
     * 问题图片
     */
    private String issueImages;
    
    /**
     * 附加图片(阿里云OSS URL)
     */
    private String additionalImages;
    
    /**
     * 处理方案
     */
    private String processPlan;
    
    /**
     * 处理结果
     */
    private String processResult;
    
    /**
     * 处理结果图片
     */
    private String resultImages;
    
    /**
     * 指派处理人员姓名
     */
    private String assignedStaffName;
    
    /**
     * 是否产生费用
     */
    private Integer hasCost;
    
    /**
     * 总费用
     */
    private BigDecimal totalCost;
    
    /**
     * 费用支付状态
     */
    private String costPaymentStatus;
    
    /**
     * 满意度评分
     */
    private Integer satisfactionLevel;
    
    /**
     * 满意度反馈
     */
    private String satisfactionFeedback;
    
    /**
     * 上报时间
     */
    private LocalDateTime reportedTime;
    
    /**
     * 预计完成时间
     */
    private LocalDateTime estimatedCompleteTime;
    
    /**
     * 实际完成时间
     */
    private LocalDateTime actualCompleteTime;
    
    /**
     * 是否已评价
     */
    private Integer isEvaluated;
    
    /**
     * 关联信息（房屋、楼栋、社区、处理人员等）
     */
    private Map<String, Object> relations;
}

