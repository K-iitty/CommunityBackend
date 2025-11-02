package com.community.owner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.owner.entity.Owner;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OwnerMapper extends BaseMapper<Owner> {
    // 可以添加自定义的查询方法
}
