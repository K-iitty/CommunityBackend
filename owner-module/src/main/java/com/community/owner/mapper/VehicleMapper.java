package com.community.owner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.owner.entity.Vehicle;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {
    // 可添加自定义查询方法
}


