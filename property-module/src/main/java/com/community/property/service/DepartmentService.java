package com.community.property.service;

import com.community.property.entity.Department;

import java.util.Map;

/**
 * 部门服务接口
 */
public interface DepartmentService {
    
    /**
     * 根据ID查询部门
     */
    Department findById(Long id);
    
    /**
     * 获取部门任务统计
     */
    Map<String, Object> getDepartmentTaskStatistics(Long departmentId);
}

