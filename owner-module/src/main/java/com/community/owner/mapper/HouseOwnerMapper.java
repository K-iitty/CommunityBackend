package com.community.owner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.owner.domain.entity.HouseOwner;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HouseOwnerMapper extends BaseMapper<HouseOwner> {
    // 可添加自定义查询方法
}


