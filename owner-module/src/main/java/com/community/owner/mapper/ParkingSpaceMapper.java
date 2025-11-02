package com.community.owner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.owner.entity.ParkingSpace;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ParkingSpaceMapper extends BaseMapper<ParkingSpace> {
    // 可添加自定义查询方法
}


