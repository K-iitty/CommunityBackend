package com.community.property.service;

import com.community.property.domain.entity.Role;

/**
 * 角色服务接口
 */
public interface RoleService {
    
    /**
     * 根据ID查询角色
     */
    Role findById(Long id);
}

