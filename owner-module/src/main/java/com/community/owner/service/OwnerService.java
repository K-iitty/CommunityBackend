package com.community.owner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.owner.entity.Owner;

public interface OwnerService extends IService<Owner> {
    /**
     * 根据用户名查找业主
     * @param username 用户名
     * @return 业主实体
     */
    Owner findByUsername(String username);
    
    /**
     * 根据手机号查找业主
     * @param phone 手机号
     * @return 业主实体
     */
    Owner findByPhone(String phone);
    
    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
}