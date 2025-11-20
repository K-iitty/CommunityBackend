package com.community.property.mapper;

import com.community.property.domain.entity.IssueFollowUp;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 问题跟进记录Mapper
 */
@Mapper
public interface IssueFollowUpMapper extends BaseMapper<IssueFollowUp> {
    
    /**
     * 根据问题ID查询跟进记录
     */
    @Select("SELECT * FROM issue_follow_up WHERE issue_id = #{issueId} ORDER BY created_at ASC")
    List<IssueFollowUp> findByIssueId(Long issueId);
}

