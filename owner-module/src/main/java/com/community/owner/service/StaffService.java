package com.community.owner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.owner.domain.entity.Staff;

public interface StaffService extends IService<Staff> {
    /**
     * 根据用户名查找员工
     * @param username 用户名
     * @return 员工实体
     */
    Staff findByUsername(String username);
    
    /**
     * 根据手机号查找员工
     * @param phone 手机号
     * @return 员工实体
     */
    Staff findByPhone(String phone);
    
    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
}