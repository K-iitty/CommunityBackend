package com.community.property.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 问题评价请求对象
 */
@Data
@Schema(description = "问题评价请求")
public class IssueEvaluationRequest {
    
    @Schema(description = "问题ID", required = true)
    private Long issueId;
    
    @Schema(description = "是否满意", required = true)
    private Boolean isSatisfied;
    
    @Schema(description = "满意度评分(1-5分)", required = true)
    private Integer satisfactionLevel;
    
    @Schema(description = "评价内容")
    private String evaluationContent;
}

