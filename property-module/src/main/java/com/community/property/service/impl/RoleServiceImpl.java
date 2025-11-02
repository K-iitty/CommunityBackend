package com.community.property.service.impl;

import com.community.property.mapper.RoleMapper;
import com.community.property.entity.Role;
import com.community.property.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 角色服务实现类
 */
@Service
public class RoleServiceImpl implements RoleService {
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Override
    public Role findById(Long id) {
        return roleMapper.findById(id);
    }
}

