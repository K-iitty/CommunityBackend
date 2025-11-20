package com.community.owner.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("smart_qa_knowledge")
public class SmartQaKnowledge {
    
    @TableId
    private Long id;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 文档标题
     */
    private String title;
    
    /**
     * 文档描述/摘要
     */
    private String description;
    
    /**
     * 文档存储路径
     */
    private String filePath;
    
    /**
     * 文档文件名
     */
    private String fileName;
    
    /**
     * 文档类型 (pdf/doc/txt等)
     */
    private String fileType;
    
    /**
     * 文档大小(字节)
     */
    private Long fileSize;
    
    /**
     * 标签，逗号分隔
     */
    private String tags;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 状态:启用/禁用
     */
    private String status;
    
    /**
     * 查看次数
     */
    private Integer viewCount;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 上传人ID
     */
    private Long createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
