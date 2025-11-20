package com.community.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.admin.domain.entity.CommunityNotice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommunityNoticeDao extends BaseMapper<CommunityNotice> {
}