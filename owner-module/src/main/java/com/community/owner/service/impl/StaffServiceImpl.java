package com.community.owner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.owner.entity.Staff;
import com.community.owner.service.StaffService;
import com.community.owner.mapper.StaffMapper;
import org.springframework.stereotype.Service;

@Service
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements StaffService {
    
    @Override
    public Staff findByUsername(String username) {
        QueryWrapper<Staff> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }
    
    @Override
    public Staff findByPhone(String phone) {
        QueryWrapper<Staff> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return getOne(queryWrapper);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        QueryWrapper<Staff> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return count(queryWrapper) > 0;
    }
}