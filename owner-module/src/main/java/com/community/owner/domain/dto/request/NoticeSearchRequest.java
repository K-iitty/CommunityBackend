package com.community.owner.domain.dto.request;

import lombok.Data;

/**
 * 公告搜索请求对象
 */
@Data
public class NoticeSearchRequest {
    
    /**
     * 搜索关键词（搜索标题、内容、公告类型）
     */
    private String keyword;
    
    /**
     * 页码(从1开始)
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer size = 10;
}

