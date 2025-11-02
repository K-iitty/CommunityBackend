package com.community.owner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("smart_qa_search_history")
public class SmartQaSearchHistory {
    
    @TableId
    private Long id;
    
    /**
     * 业主ID
     */
    private Long ownerId;
    
    /**
     * 查询内容
     */
    private String query;
    
    /**
     * 查询类型: doc(文档)/web(网络)/manual(人工)
     */
    private String queryType;
    
    /**
     * 匹配的文档ID
     */
    private Long docId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}


