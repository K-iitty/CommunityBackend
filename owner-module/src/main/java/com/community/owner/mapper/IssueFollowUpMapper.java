package com.community.owner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.owner.domain.entity.IssueFollowUp;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问题跟进记录DAO接口
 */
@Mapper
public interface IssueFollowUpMapper extends BaseMapper<IssueFollowUp> {
}

