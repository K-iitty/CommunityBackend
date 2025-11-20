package com.community.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.admin.domain.entity.House;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HouseDao extends BaseMapper<House> {
}