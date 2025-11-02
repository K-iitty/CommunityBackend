package com.community.owner.dto;

import lombok.Data;

/**
 * 问题评价请求对象
 */
@Data
public class IssueEvaluationRequest {
    
    /**
     * 问题ID
     */
    private Long issueId;
    
    /**
     * 满意度评分(1-5分)
     */
    private Integer satisfactionLevel;
    
    /**
     * 评价内容（前端发送使用的字段名）
     */
    private String satisfactionFeedback;
    
    /**
     * 是否满意（true-满意，问题结束；false-不满意，继续追加）
     */
    private Boolean isSatisfied;
    
    /**
     * 不满意时的追加描述
     */
    private String additionalContent;
}

