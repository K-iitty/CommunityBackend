package com.community.owner.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role")
public class Role extends BaseEntity {
    
    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;
    
    /**
     * 角色编码
     */
    @TableField("role_code")
    private String roleCode;
    
    /**
     * 角色类型
     */
    @TableField("role_type")
    private String roleType;
    
    /**
     * 描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 成员数量
     */
    @TableField("member_count")
    private Integer memberCount;
    
    /**
     * 状态:启用/禁用
     */
    @TableField("status")
    private String status;
    
    /**
     * 权限配置(JSON)
     */
    @TableField("permissions")
    private String permissions;
}