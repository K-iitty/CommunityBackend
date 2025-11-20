package com.community.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.admin.domain.entity.Vehicle;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VehicleDao extends BaseMapper<Vehicle> {
}