package com.community.owner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.owner.domain.entity.CommunityNotice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社区公告DAO接口
 */
@Mapper
public interface CommunityNoticeMapper extends BaseMapper<CommunityNotice> {
    // 可以添加自定义的查询方法
}

