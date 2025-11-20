package com.community.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.admin.domain.entity.ParkingSpace;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ParkingSpaceDao extends BaseMapper<ParkingSpace> {
}