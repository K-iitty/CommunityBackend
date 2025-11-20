package com.community.owner.domain.dto.request;

import lombok.Data;

/**
 * 问题追加请求对象
 */
@Data
public class IssueFollowUpRequest {
    
    /**
     * 问题ID
     */
    private Long issueId;
    
    /**
     * 追加内容
     */
    private String followUpContent;
    
    /**
     * 附件信息(JSON格式)
     */
    private String attachments;
}

