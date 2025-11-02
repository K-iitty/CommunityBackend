package com.community.property.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公告搜索请求对象
 */
@Data
@Schema(description = "公告搜索请求")
public class NoticeSearchRequest {
    
    @Schema(description = "搜索关键词")
    private String keyword;
    
    @Schema(description = "公告类型")
    private String noticeType;
    
    @Schema(description = "页码", required = true)
    private Integer page;
    
    @Schema(description = "每页数量", required = true)
    private Integer size;
}

