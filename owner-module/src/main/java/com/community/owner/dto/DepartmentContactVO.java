package com.community.owner.dto;

import lombok.Data;

/**
 * 部门联系人信息展示对象
 */
@Data
public class DepartmentContactVO {
    
    /**
     * 部门ID
     */
    private Long departmentId;
    
    /**
     * 部门名称
     */
    private String departmentName;
    
    /**
     * 负责人ID
     */
    private Long staffId;
    
    /**
     * 负责人姓名
     */
    private String staffName;
    
    /**
     * 负责人职位
     */
    private String position;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 部门描述
     */
    private String description;
}

