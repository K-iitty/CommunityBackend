package com.community.owner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工扩展信息实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("staff_extension")
public class StaffExtension extends BaseEntity {
    
    /**
     * 员工ID
     */
    @TableField("staff_id")
    private Long staffId;
    
    /**
     * 扩展字段键
     */
    @TableField("extension_key")
    private String extensionKey;
    
    /**
     * 扩展字段值
     */
    @TableField("extension_value")
    private String extensionValue;
    
    /**
     * 值类型
     */
    @TableField("value_type")
    private String valueType;
    
    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;
}