package com.community.property.service.impl;

import com.community.property.mapper.DepartmentMapper;
import com.community.property.mapper.OwnerIssueMapper;
import com.community.property.domain.entity.Department;
import com.community.property.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 部门服务实现类
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {
    
    @Autowired
    private DepartmentMapper departmentMapper;
    
    @Autowired
    private OwnerIssueMapper ownerIssueMapper;
    
    @Override
    public Department findById(Long id) {
        return departmentMapper.findById(id);
    }
    
    @Override
    public Map<String, Object> getDepartmentTaskStatistics(Long departmentId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 统计部门任务总数
        int totalTasks = ownerIssueMapper.countByDepartmentId(departmentId);
        statistics.put("totalTasks", totalTasks);
        
        // TODO: 可以添加更多统计信息
        statistics.put("pendingTasks", 0);
        statistics.put("processingTasks", 0);
        statistics.put("completedTasks", 0);
        statistics.put("urgentTasks", 0);
        
        return statistics;
    }
}

