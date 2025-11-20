package com.community.owner.domain.dto.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公告列表展示对象（用于分页列表）
 */
@Data
public class NoticeListVO {
    
    /**
     * 公告ID
     */
    private Long id;
    
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 公告内容（简短描述）
     */
    private String content;
    
    /**
     * 公告图片（JSON数组格式）
     */
    private String noticeImages;
    
    /**
     * 公告类型
     */
    private String noticeType;
    
    /**
     * 是否紧急
     */
    private Integer isUrgent;
    
    /**
     * 是否置顶
     */
    private Integer isTop;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 阅读次数
     */
    private Integer readCount;
}

