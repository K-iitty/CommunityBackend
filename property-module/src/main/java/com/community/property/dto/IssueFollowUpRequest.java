package com.community.property.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 问题追加请求对象
 */
@Data
@Schema(description = "问题追加请求")
public class IssueFollowUpRequest {
    
    @Schema(description = "问题ID", required = true)
    private Long issueId;
    
    @Schema(description = "跟进类型", required = true)
    private String followUpType;
    
    @Schema(description = "追加内容", required = true)
    private String followUpContent;
    
    @Schema(description = "附件图片(JSON数组)")
    private String attachments;
    
    @Schema(description = "内部备注")
    private String internalNote;
}

