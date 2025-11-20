package com.community.owner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.owner.domain.dto.vo.IssueDetailVO;
import com.community.owner.domain.dto.request.IssueEvaluationRequest;
import com.community.owner.domain.dto.request.IssueFollowUpRequest;
import com.community.owner.domain.dto.request.IssueSubmitRequest;
import com.community.owner.domain.entity.OwnerIssue;

import java.util.List;
import java.util.Map;

/**
 * 业主问题服务接口
 */
public interface OwnerIssueService extends IService<OwnerIssue> {
    
    /**
     * 提交问题
     * @param ownerId 业主ID
     * @param request 问题提交请求
     * @return 提交结果
     */
    OwnerIssue submitIssue(Long ownerId, IssueSubmitRequest request);
    
    /**
     * 查询业主的问题列表
     */
    Map<String, Object> listOwnerIssues(Long ownerId, Integer page, Integer size);
    
    /**
     * 按状态查询业主的问题列表
     */
    Map<String, Object> listOwnerIssues(Long ownerId, Integer page, Integer size, String status);
    
    /**
     * 查看问题详情
     * @param issueId 问题ID
     * @param ownerId 业主ID
     * @return 问题详情
     */
    IssueDetailVO getIssueDetail(Long issueId, Long ownerId);
    
    /**
     * 业主追加问题描述
     * @param ownerId 业主ID
     * @param request 追加请求
     * @return 追加结果
     */
    boolean addFollowUp(Long ownerId, IssueFollowUpRequest request);
    
    /**
     * 业主评价问题处理结果
     * @param ownerId 业主ID
     * @param request 评价请求
     * @return 评价结果
     */
    boolean evaluateIssue(Long ownerId, IssueEvaluationRequest request);
    
    /**
     * 获取问题的跟进记录
     * @param issueId 问题ID
     * @param ownerId 业主ID
     * @return 跟进记录列表
     */
    List<Map<String, Object>> getFollowUpRecords(Long issueId, Long ownerId);
    
    /**
     * 分页获取问题的跟进记录
     * @param issueId 问题ID
     * @param ownerId 业主ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页跟进记录
     */
    Map<String, Object> getFollowUpRecords(Long issueId, Long ownerId, Integer page, Integer size);
}

