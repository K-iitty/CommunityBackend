package com.community.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.admin.domain.entity.AccessControlRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccessControlRecordDao extends BaseMapper<AccessControlRecord> {
}