package com.community.property.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("department")
public class Department extends BaseEntity {
    
    /**
     * 部门名称
     */
    @TableField("department_name")
    private String departmentName;
    
    /**
     * 父部门ID
     */
    @TableField("parent_id")
    private Long parentId;
    
    /**
     * 部门编码
     */
    @TableField("department_code")
    private String departmentCode;
    
    /**
     * 部门层级
     */
    @TableField("department_level")
    private Integer departmentLevel;
    
    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;
    
    /**
     * 状态:启用/禁用
     */
    @TableField("status")
    private String status;
    
    /**
     * 描述
     */
    @TableField("description")
    private String description;
}