package com.community.owner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.owner.domain.entity.Owner;
import com.community.owner.service.OwnerService;
import com.community.owner.mapper.OwnerMapper;
import org.springframework.stereotype.Service;

@Service
public class OwnerServiceImpl extends ServiceImpl<OwnerMapper, Owner> implements OwnerService {
    
    @Override
    public Owner findByUsername(String username) {
        QueryWrapper<Owner> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }
    
    @Override
    public Owner findByPhone(String phone) {
        QueryWrapper<Owner> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return getOne(queryWrapper);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        QueryWrapper<Owner> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return count(queryWrapper) > 0;
    }
}