package com.community.owner.dto;

import lombok.Data;

/**
 * 公告筛选请求对象
 */
@Data
public class NoticeFilterRequest {
    
    /**
     * 公告类型（按 notice_type 分类）
     */
    private String noticeType;
    
    /**
     * 目标受众
     */
    private String targetAudience;
    
    /**
     * 目标楼栋
     */
    private String targetBuilding;
    
    /**
     * 目标业主类型
     */
    private String targetOwnerType;
    
    /**
     * 页码(从1开始)
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer size = 10;
}

