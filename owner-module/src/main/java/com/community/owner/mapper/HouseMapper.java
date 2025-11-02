package com.community.owner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.owner.entity.House;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HouseMapper extends BaseMapper<House> {
    // 可添加自定义查询方法
}


