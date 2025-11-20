package com.community.property.service;

import com.community.property.domain.dto.vo.IssueDetailVO;
import com.community.property.domain.dto.request.IssueFollowUpRequest;
import java.util.Map;

public interface PropertyIssueService {
    /**
     * 查询所有问题（物业端）
     */
    Map<String, Object> listAllIssues(Integer page, Integer size, String status);

    /**
     * 查询分配给本部门的问题
     */
    Map<String, Object> listDepartmentIssues(Long departmentId, Integer page, Integer size, String status);

    /**
     * 获取问题详情
     */
    IssueDetailVO getIssueDetail(Long issueId);

    /**
     * 分配问题给物业员工
     */
    Map<String, Object> assignIssue(Long issueId, Long staffId, String remark);

    /**
     * 物业员工开始处理问题
     */
    Map<String, Object> startProcessing(Long issueId, Long staffId, String planDescription);

    /**
     * 物业员工提交处理结果
     */
    Map<String, Object> submitProcessResult(Long issueId, Long staffId, String resultDescription, String images);

    /**
     * 物业员工提交处理结果（包含分离的process_images和result_images）
     */
    Map<String, Object> submitProcessResultWithImages(Long issueId, Long staffId, String resultDescription, 
        String processImages, String resultImages, String planDescription);

    /**
     * 物业员工添加处理进展
     */
    Map<String, Object> addFollowUp(Long issueId, IssueFollowUpRequest request, Long operatorId, String operatorName);

    /**
     * 查询问题的追加记录
     */
    Map<String, Object> getIssueFollowUps(Long issueId, Integer page, Integer size);

    /**
     * 标记问题为已解决（物业处理完成后，等待管理员状态变更）
     */
    Map<String, Object> markAsResolved(Long issueId, Long staffId);

    /**
     * 重新分配问题
     */
    Map<String, Object> reassignIssue(Long issueId, Long newStaffId, String remark);

    /**
     * 获取问题统计数据（各状态的数量）
     */
    Map<String, Object> getIssueStatistics();

    /**
     * 查询分配给指定物业人员的问题
     */
    Map<String, Object> listIssuesForStaff(String staffId, Integer page, Integer size, String status);

    /**
     * 获取指定物业人员的问题统计数据
     */
    Map<String, Object> getIssueStatisticsForStaff(String staffId);
}
