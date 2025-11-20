package com.community.owner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.owner.domain.entity.ParkingLot;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ParkingLotMapper extends BaseMapper<ParkingLot> {
    // 可添加自定义查询方法
}


