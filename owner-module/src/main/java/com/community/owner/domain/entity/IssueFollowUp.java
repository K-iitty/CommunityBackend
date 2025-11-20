package com.community.owner.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 问题跟进记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("issue_follow_up")
public class IssueFollowUp extends BaseEntity {
    
    /**
     * 问题ID
     */
    @TableField("issue_id")
    private Long issueId;
    
    /**
     * 跟进类型:业主补充/处理进展/状态变更/费用确认/满意度评价
     */
    @TableField("follow_up_type")
    private String followUpType;
    
    /**
     * 跟进内容
     */
    @TableField("follow_up_content")
    private String followUpContent;
    
    /**
     * 操作人类型:owner/staff/system
     */
    @TableField("operator_type")
    private String operatorType;
    
    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private Long operatorId;
    
    /**
     * 操作人姓名
     */
    @TableField("operator_name")
    private String operatorName;
    
    /**
     * 附件信息(JSON格式)
     */
    @TableField("attachments")
    private String attachments;
    
    /**
     * 内部备注
     */
    @TableField("internal_note")
    private String internalNote;
    
    /**
     * 覆盖 BaseEntity 中的 updatedAt 字段
     * issue_follow_up 表中没有 updated_at 字段，所以设置 exist=false
     */
    @TableField(value = "updated_at", exist = false)
    public LocalDateTime updatedAt;
}


