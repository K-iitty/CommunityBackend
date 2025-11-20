package com.community.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.property.domain.entity.IssueFollowUp;
import com.community.property.domain.entity.Owner;
import com.community.property.domain.entity.OwnerIssue;
import com.community.property.domain.entity.Staff;
import com.community.property.mapper.OwnerIssueMapper;
import com.community.property.mapper.IssueFollowUpMapper;
import com.community.property.mapper.OwnerMapper;
import com.community.property.mapper.StaffMapper;
import com.community.property.domain.dto.vo.IssueDetailVO;
import com.community.property.domain.dto.request.IssueFollowUpRequest;
import com.community.property.service.PropertyIssueService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 物业问题处理服务实现
 */
@Service
public class PropertyIssueServiceImpl extends ServiceImpl<OwnerIssueMapper, OwnerIssue> implements PropertyIssueService {

    @Autowired
    private IssueFollowUpMapper issueFollowUpMapper;

    @Autowired
    private OwnerMapper ownerMapper;

    @Autowired
    private StaffMapper staffMapper;

    @Override
    public Map<String, Object> listAllIssues(Integer page, Integer size, String status) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        QueryWrapper<OwnerIssue> queryWrapper = new QueryWrapper<>();
        
        // 只查询已分配的问题
        queryWrapper.isNotNull("assigned_staff_id");
        
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq("issue_status", status);
        }

        queryWrapper.orderByDesc("reported_time");

        int offset = (page - 1) * size;
        queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

        List<OwnerIssue> issues = list(queryWrapper);
        long total = count(new QueryWrapper<OwnerIssue>()
                .isNotNull("assigned_staff_id")
                .eq(status != null && !status.trim().isEmpty(), "issue_status", status));

        List<Map<String, Object>> issueList = issues.stream().map(this::convertToIssueVO).collect(Collectors.toList());

        Map<String, Object> pageData = new LinkedHashMap<>();
        pageData.put("page", page);
        pageData.put("size", size);
        pageData.put("total", total);
        pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
        pageData.put("items", issueList);

        return pageData;
    }

    @Override
    public Map<String, Object> listDepartmentIssues(Long departmentId, Integer page, Integer size, String status) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        QueryWrapper<OwnerIssue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("assigned_department_id", departmentId);
        
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq("issue_status", status);
        }

        queryWrapper.orderByDesc("reported_time");

        int offset = (page - 1) * size;
        queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

        List<OwnerIssue> issues = list(queryWrapper);
        long total = count(new QueryWrapper<OwnerIssue>()
                .eq("assigned_department_id", departmentId)
                .eq(status != null && !status.trim().isEmpty(), "issue_status", status));

        List<Map<String, Object>> issueList = issues.stream().map(this::convertToIssueVO).collect(Collectors.toList());

        Map<String, Object> pageData = new LinkedHashMap<>();
        pageData.put("page", page);
        pageData.put("size", size);
        pageData.put("total", total);
        pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
        pageData.put("items", issueList);

        return pageData;
    }

    @Override
    public IssueDetailVO getIssueDetail(Long issueId) {
        OwnerIssue issue = getById(issueId);
        if (issue == null) {
            return null;
        }

        IssueDetailVO vo = new IssueDetailVO();
        BeanUtils.copyProperties(issue, vo);

        // 获取业主信息
        Owner owner = ownerMapper.selectById(issue.getOwnerId());
        if (owner != null) {
            vo.setOwnerName(owner.getName());
            vo.setOwnerPhone(owner.getPhone());
        }

        // 获取处理员工信息
        if (issue.getProcessorStaffId() != null) {
            Staff processor = staffMapper.selectById(issue.getProcessorStaffId());
            if (processor != null) {
                vo.setProcessorName(processor.getName());
                vo.setProcessorPhone(processor.getPhone());
            }
        }

        // 获取跟进记录
        QueryWrapper<IssueFollowUp> followUpQuery = new QueryWrapper<>();
        followUpQuery.eq("issue_id", issueId);
        followUpQuery.orderByAsc("created_at");
        List<IssueFollowUp> followUps = issueFollowUpMapper.selectList(followUpQuery);
        vo.setFollowUps(followUps);

        return vo;
    }

    @Override
    @Transactional
    public Map<String, Object> assignIssue(Long issueId, Long staffId, String remark) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            OwnerIssue issue = getById(issueId);
            if (issue == null) {
                response.put("success", false);
                response.put("message", "问题不存在");
                return response;
            }

            Staff staff = staffMapper.selectById(staffId);
            if (staff == null) {
                response.put("success", false);
                response.put("message", "员工不存在");
                return response;
            }

            // 更新问题分配信息
            issue.setAssignedStaffId(staffId);
            issue.setAssignedTime(LocalDateTime.now());
            issue.setAssignedRemark(remark);
            issue.setWorkStatus("已分配");
            updateById(issue);

            // 创建跟进记录
            IssueFollowUp followUp = new IssueFollowUp();
            followUp.setIssueId(issueId);
            followUp.setFollowUpType("状态变更");
            followUp.setFollowUpContent("问题已分配给员工：" + staff.getName());
            followUp.setOperatorType("staff");
            followUp.setOperatorId(staffId);
            followUp.setOperatorName(staff.getName());
            followUp.setCreatedAt(LocalDateTime.now());
            issueFollowUpMapper.insert(followUp);

            response.put("success", true);
            response.put("message", "分配成功");
            response.put("data", issue);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "分配失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> startProcessing(Long issueId, Long staffId, String planDescription) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            OwnerIssue issue = getById(issueId);
            if (issue == null) {
                response.put("success", false);
                response.put("message", "问题不存在");
                return response;
            }

            Staff staff = staffMapper.selectById(staffId);
            if (staff == null) {
                response.put("success", false);
                response.put("message", "员工不存在");
                return response;
            }

            // 更新问题状态为处理中
            issue.setProcessorStaffId(staffId);
            issue.setIssueStatus("处理中");
            issue.setWorkStatus("处理中");
            issue.setProcessStartTime(LocalDateTime.now());
            issue.setProcessPlan(planDescription);
            issue.setResponseTime(LocalDateTime.now());
            updateById(issue);

            // 创建跟进记录
            IssueFollowUp followUp = new IssueFollowUp();
            followUp.setIssueId(issueId);
            followUp.setFollowUpType("处理进展");
            followUp.setFollowUpContent("处理方案：" + planDescription);
            followUp.setOperatorType("staff");
            followUp.setOperatorId(staffId);
            followUp.setOperatorName(staff.getName());
            followUp.setCreatedAt(LocalDateTime.now());
            issueFollowUpMapper.insert(followUp);

            response.put("success", true);
            response.put("message", "开始处理成功");
            response.put("data", issue);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "开始处理失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> submitProcessResult(Long issueId, Long staffId, String resultDescription, String images) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            OwnerIssue issue = getById(issueId);
            if (issue == null) {
                response.put("success", false);
                response.put("message", "问题不存在");
                return response;
            }

            Staff staff = staffMapper.selectById(staffId);
            if (staff == null) {
                response.put("success", false);
                response.put("message", "员工不存在");
                return response;
            }

            // 更新处理结果
            issue.setProcessResult(resultDescription);
            issue.setProcessEndTime(LocalDateTime.now());
            issue.setProcessImages(images);
            updateById(issue);

            // 创建跟进记录
            IssueFollowUp followUp = new IssueFollowUp();
            followUp.setIssueId(issueId);
            followUp.setFollowUpType("处理进展");
            followUp.setFollowUpContent("处理结果：" + resultDescription);
            followUp.setOperatorType("staff");
            followUp.setOperatorId(staffId);
            followUp.setOperatorName(staff.getName());
            followUp.setAttachments(images);
            followUp.setCreatedAt(LocalDateTime.now());
            issueFollowUpMapper.insert(followUp);

            response.put("success", true);
            response.put("message", "提交处理结果成功");
            response.put("data", issue);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "提交处理结果失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> submitProcessResultWithImages(Long issueId, Long staffId, String resultDescription,
            String processImages, String resultImages, String planDescription) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            OwnerIssue issue = getById(issueId);
            if (issue == null) {
                response.put("success", false);
                response.put("message", "问题不存在");
                return response;
            }

            Staff staff = staffMapper.selectById(staffId);
            if (staff == null) {
                response.put("success", false);
                response.put("message", "员工不存在");
                return response;
            }

            // 更新处理方案和处理结果
            if (planDescription != null && !planDescription.isEmpty()) {
                issue.setProcessPlan(planDescription);
            }
            issue.setProcessResult(resultDescription);
            issue.setProcessEndTime(LocalDateTime.now());
            if (processImages != null) {
                issue.setProcessImages(processImages);
            }
            if (resultImages != null) {
                issue.setResultImages(resultImages);
            }
            updateById(issue);

            // 创建跟进记录
            IssueFollowUp followUp = new IssueFollowUp();
            followUp.setIssueId(issueId);
            followUp.setFollowUpType("处理进展");
            followUp.setFollowUpContent("处理结果：" + resultDescription);
            followUp.setOperatorType("staff");
            followUp.setOperatorId(staffId);
            followUp.setOperatorName(staff.getName());
            followUp.setAttachments(processImages);
            followUp.setCreatedAt(LocalDateTime.now());
            issueFollowUpMapper.insert(followUp);

            response.put("success", true);
            response.put("message", "提交处理结果成功");
            response.put("data", issue);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "提交处理结果失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> addFollowUp(Long issueId, IssueFollowUpRequest request, Long operatorId, String operatorName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            OwnerIssue issue = getById(issueId);
            if (issue == null) {
                response.put("success", false);
                response.put("message", "问题不存在");
                return response;
            }

            // 创建跟进记录
            IssueFollowUp followUp = new IssueFollowUp();
            followUp.setIssueId(issueId);
            followUp.setFollowUpType(request.getFollowUpType());
            followUp.setFollowUpContent(request.getFollowUpContent());
            followUp.setOperatorType("staff");
            followUp.setOperatorId(operatorId);
            followUp.setOperatorName(operatorName);
            followUp.setAttachments(request.getAttachments());
            followUp.setInternalNote(request.getInternalNote());
            followUp.setCreatedAt(LocalDateTime.now());
            issueFollowUpMapper.insert(followUp);

            response.put("success", true);
            response.put("message", "添加跟进记录成功");
            response.put("data", followUp);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加跟进记录失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> getIssueFollowUps(Long issueId, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        QueryWrapper<IssueFollowUp> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("issue_id", issueId);
        queryWrapper.orderByAsc("created_at");

        int offset = (page - 1) * size;
        queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

        List<IssueFollowUp> followUps = issueFollowUpMapper.selectList(queryWrapper);
        long total = issueFollowUpMapper.selectCount(new QueryWrapper<IssueFollowUp>().eq("issue_id", issueId));

        List<Map<String, Object>> followUpList = followUps.stream().map(fu -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", fu.getId());
            map.put("followUpType", fu.getFollowUpType());
            map.put("followUpContent", fu.getFollowUpContent());
            map.put("operatorType", fu.getOperatorType());
            map.put("operatorName", fu.getOperatorName());
            map.put("createdAt", fu.getCreatedAt());
            map.put("attachments", fu.getAttachments());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> pageData = new LinkedHashMap<>();
        pageData.put("page", page);
        pageData.put("size", size);
        pageData.put("total", total);
        pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
        pageData.put("items", followUpList);

        return pageData;
    }

    @Override
    public Map<String, Object> markAsResolved(Long issueId, Long staffId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            OwnerIssue issue = getById(issueId);
            if (issue == null) {
                response.put("success", false);
                response.put("message", "问题不存在");
                return response;
            }

            Staff staff = staffMapper.selectById(staffId);
            if (staff == null) {
                response.put("success", false);
                response.put("message", "员工不存在");
                return response;
            }

            // 标记为已解决，等待管理员确认
            issue.setWorkStatus("已完成");
            updateById(issue);

            response.put("success", true);
            response.put("message", "问题已标记为已解决");
            response.put("data", issue);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "标记失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> reassignIssue(Long issueId, Long newStaffId, String remark) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            OwnerIssue issue = getById(issueId);
            if (issue == null) {
                response.put("success", false);
                response.put("message", "问题不存在");
                return response;
            }

            Staff newStaff = staffMapper.selectById(newStaffId);
            if (newStaff == null) {
                response.put("success", false);
                response.put("message", "新员工不存在");
                return response;
            }

            // 更新分配
            issue.setAssignedStaffId(newStaffId);
            issue.setAssignedTime(LocalDateTime.now());
            issue.setAssignedRemark(remark);
            updateById(issue);

            // 创建跟进记录
            IssueFollowUp followUp = new IssueFollowUp();
            followUp.setIssueId(issueId);
            followUp.setFollowUpType("状态变更");
            followUp.setFollowUpContent("问题重新分配给员工：" + newStaff.getName() + "，原因：" + remark);
            followUp.setOperatorType("staff");
            followUp.setOperatorId(newStaffId);
            followUp.setOperatorName(newStaff.getName());
            followUp.setCreatedAt(LocalDateTime.now());
            issueFollowUpMapper.insert(followUp);

            response.put("success", true);
            response.put("message", "重新分配成功");
            response.put("data", issue);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "重新分配失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> getIssueStatistics() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 查询所有已分配的问题总数
            long totalIssues = count(new QueryWrapper<OwnerIssue>()
                    .isNotNull("assigned_staff_id"));
            
            // 查询待处理的问题数
            long pendingIssues = count(new QueryWrapper<OwnerIssue>()
                    .isNotNull("assigned_staff_id")
                    .eq("issue_status", "待处理"));
            
            // 查询处理中的问题数
            long processingIssues = count(new QueryWrapper<OwnerIssue>()
                    .isNotNull("assigned_staff_id")
                    .eq("issue_status", "处理中"));
            
            // 查询已完成的问题数
            long completedIssues = count(new QueryWrapper<OwnerIssue>()
                    .isNotNull("assigned_staff_id")
                    .eq("issue_status", "已完成"));

            Map<String, Object> statistics = new LinkedHashMap<>();
            statistics.put("totalIssues", totalIssues);
            statistics.put("pendingIssues", pendingIssues);
            statistics.put("processingIssues", processingIssues);
            statistics.put("completedIssues", completedIssues);

            response.put("success", true);
            response.put("data", statistics);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "统计失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 将OwnerIssue转换为简化的VO对象
     */
    private Map<String, Object> convertToIssueVO(OwnerIssue issue) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", issue.getId());
        map.put("issueTitle", issue.getIssueTitle());
        map.put("issueType", issue.getIssueType());
        map.put("urgencyLevel", issue.getUrgencyLevel());
        map.put("issueStatus", issue.getIssueStatus());
        map.put("workStatus", issue.getWorkStatus());
        map.put("reportedTime", issue.getReportedTime());
        map.put("contactName", issue.getContactName());
        map.put("contactPhone", issue.getContactPhone());
        return map;
    }

    @Override
    public Map<String, Object> listIssuesForStaff(String staffId, Integer page, Integer size, String status) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        QueryWrapper<OwnerIssue> queryWrapper = new QueryWrapper<>();
        
        // 只查询分配给指定物业人员的问题
        queryWrapper.eq("assigned_staff_id", staffId);
        
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq("issue_status", status);
        }

        queryWrapper.orderByDesc("reported_time");

        int offset = (page - 1) * size;
        queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

        List<OwnerIssue> issues = list(queryWrapper);
        long total = count(new QueryWrapper<OwnerIssue>()
                .eq("assigned_staff_id", staffId)
                .eq(status != null && !status.trim().isEmpty(), "issue_status", status));

        // 转换为简化的VO对象
        List<Map<String, Object>> issueVOs = new ArrayList<>();
        for (OwnerIssue issue : issues) {
            Map<String, Object> vo = new HashMap<>();
            vo.put("id", issue.getId());
            vo.put("issueTitle", issue.getIssueTitle());
            vo.put("issueType", issue.getIssueType());
            vo.put("urgencyLevel", issue.getUrgencyLevel());
            vo.put("issueStatus", issue.getIssueStatus());
            vo.put("workStatus", issue.getWorkStatus());
            vo.put("reportedTime", issue.getReportedTime());
            vo.put("contactName", issue.getContactName());
            vo.put("contactPhone", issue.getContactPhone());
            vo.put("assignedStaffId", issue.getAssignedStaffId());
            vo.put("processorStaffId", issue.getProcessorStaffId());
            issueVOs.add(vo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", issueVOs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);

        return result;
    }

    @Override
    public Map<String, Object> getIssueStatisticsForStaff(String staffId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查询分配给指定物业人员的问题总数
            long totalIssues = count(new QueryWrapper<OwnerIssue>()
                    .eq("assigned_staff_id", staffId));

            // 查询待处理的问题数
            long pendingIssues = count(new QueryWrapper<OwnerIssue>()
                    .eq("assigned_staff_id", staffId)
                    .eq("issue_status", "待处理"));

            // 查询处理中的问题数
            long processingIssues = count(new QueryWrapper<OwnerIssue>()
                    .eq("assigned_staff_id", staffId)
                    .eq("issue_status", "处理中"));

            // 查询已完成的问题数
            long completedIssues = count(new QueryWrapper<OwnerIssue>()
                    .eq("assigned_staff_id", staffId)
                    .eq("issue_status", "已完成"));

            result.put("success", true);
            result.put("data", Map.of(
                "totalIssues", totalIssues,
                "pendingIssues", pendingIssues,
                "processingIssues", processingIssues,
                "completedIssues", completedIssues
            ));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "统计失败: " + e.getMessage());
        }
        
        return result;
    }
}
