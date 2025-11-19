package com.community.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.admin.domain.entity.SystemAdmin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemAdminDao extends BaseMapper<SystemAdmin> {
}