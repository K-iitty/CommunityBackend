package com.community.owner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.owner.mapper.IssueFollowUpMapper;
import com.community.owner.mapper.OwnerIssueMapper;
import com.community.owner.dto.IssueDetailVO;
import com.community.owner.dto.IssueEvaluationRequest;
import com.community.owner.dto.IssueFollowUpRequest;
import com.community.owner.dto.IssueSubmitRequest;
import com.community.owner.entity.*;
import com.community.owner.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 业主问题服务实现类
 */
@Service
public class OwnerIssueServiceImpl extends ServiceImpl<OwnerIssueMapper, OwnerIssue> implements OwnerIssueService {
    
    @Autowired
    private IssueFollowUpMapper issueFollowUpMapper;

    @Autowired
    private HouseService houseService;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private CommunityInfoService communityInfoService;

    @Autowired
    private StaffService staffService;
    
    @Override
    @Transactional
    public OwnerIssue submitIssue(Long ownerId, IssueSubmitRequest request) {
        OwnerIssue issue = new OwnerIssue();
        BeanUtils.copyProperties(request, issue);
        
        // 设置业主信息
        issue.setOwnerId(ownerId);
        issue.setCommunityId(1L); // 默认社区ID，实际应该从业主信息中获取
        
        // 设置状态信息
        issue.setIssueStatus("待处理");
        issue.setWorkStatus("未分配");
        issue.setReportedTime(LocalDateTime.now());
        issue.setIsEvaluated(0);
        issue.setHasCost(0);
        
        // 处理附加图片字段 - 从additionalImages复制到相应字段
        // additionalImages已经是阿里云OSS的URL格式（由前端上传时获取），直接赋值
        if (request.getAdditionalImages() != null && !request.getAdditionalImages().isEmpty()) {
            issue.setAdditionalImages(request.getAdditionalImages());
        }
        
        // 保存问题
        save(issue);
        
        // 创建初始跟进记录
        IssueFollowUp followUp = new IssueFollowUp();
        followUp.setIssueId(issue.getId());
        followUp.setFollowUpType("问题提交");
        followUp.setFollowUpContent("业主提交了问题：" + request.getIssueTitle());
        followUp.setOperatorType("owner");
        followUp.setOperatorId(ownerId);
        followUp.setOperatorName(request.getContactName());
        issueFollowUpMapper.insert(followUp);
        
        return issue;
    }
    
    @Override
    public Map<String, Object> listOwnerIssues(Long ownerId, Integer page, Integer size) {
        return listOwnerIssues(ownerId, page, size, null);
    }
    
    @Override
    public Map<String, Object> listOwnerIssues(Long ownerId, Integer page, Integer size, String status) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        
        // 日志输出
        System.out.println("=== 反馈查询 ===");
        System.out.println("ownerId: " + ownerId);
        System.out.println("page: " + page);
        System.out.println("size: " + size);
        System.out.println("status: [" + status + "]");
        System.out.println("status is null: " + (status == null));
        System.out.println("status is empty: " + (status != null && status.trim().isEmpty()));
        
        // 构建查询条件
        QueryWrapper<OwnerIssue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("owner_id", ownerId);
        
        // 如果提供了状态参数，添加状态过滤
        if (status != null && !status.trim().isEmpty()) {
            System.out.println("添加状态过滤: " + status);
            queryWrapper.eq("issue_status", status);
        }
        
        queryWrapper.orderByDesc("reported_time");
        
        // 计算分页
        int offset = (page - 1) * size;
        queryWrapper.last("LIMIT " + size + " OFFSET " + offset);
        
        // 查询数据
        List<OwnerIssue> issues = list(queryWrapper);
        System.out.println("查询结果数量: " + issues.size());
        
        // 转换为简化的VO对象
        List<Map<String, Object>> issueList = issues.stream().map(issue -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", issue.getId());
            map.put("issueTitle", issue.getIssueTitle());
            map.put("issueType", issue.getIssueType());
            map.put("urgencyLevel", issue.getUrgencyLevel());
            map.put("issueStatus", issue.getIssueStatus());
            map.put("workStatus", issue.getWorkStatus());
            map.put("reportedTime", issue.getReportedTime());
            map.put("isEvaluated", issue.getIsEvaluated());
            return map;
        }).collect(Collectors.toList());
        
        // 查询总数
        QueryWrapper<OwnerIssue> countWrapper = new QueryWrapper<>();
        countWrapper.eq("owner_id", ownerId);
        
        // 如果提供了状态参数，总数也要按状态过滤
        if (status != null && !status.trim().isEmpty()) {
            countWrapper.eq("issue_status", status);
        }
        
        long total = count(countWrapper);
        System.out.println("总数: " + total);
        System.out.println("=== 查询结束 ===");
        
        // 构建分页结果
        Map<String, Object> pageData = new LinkedHashMap<>();
        pageData.put("page", page);
        pageData.put("size", size);
        pageData.put("total", total);
        pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
        pageData.put("items", issueList);
        
        return pageData;
    }
    
    @Override
    public IssueDetailVO getIssueDetail(Long issueId, Long ownerId) {
        // 查询问题
        OwnerIssue issue = getById(issueId);
        if (issue == null || !issue.getOwnerId().equals(ownerId)) {
            return null;
        }
        
        // 转换为详情VO
        IssueDetailVO detailVO = new IssueDetailVO();
        BeanUtils.copyProperties(issue, detailVO);
        
        // 添加完整的关联信息
        Map<String, Object> relations = new LinkedHashMap<>();
        
        // 关联房屋信息
        if (issue.getHouseId() != null) {
            House house = houseService.getById(issue.getHouseId());
            if (house != null) {
                Map<String, Object> houseInfo = new LinkedHashMap<>();
                houseInfo.put("id", house.getId());
                houseInfo.put("roomNo", house.getRoomNo());
                houseInfo.put("fullRoomNo", house.getFullRoomNo());
                houseInfo.put("houseCode", house.getHouseCode());
                houseInfo.put("houseType", house.getHouseType());
                houseInfo.put("floorLevel", house.getFloorLevel());
                relations.put("house", houseInfo);
                
                // 关联楼栋信息
                if (house.getBuildingId() != null) {
                    Building building = buildingService.getById(house.getBuildingId());
                    if (building != null) {
                        Map<String, Object> buildingInfo = new LinkedHashMap<>();
                        buildingInfo.put("id", building.getId());
                        buildingInfo.put("buildingNo", building.getBuildingNo());
                        buildingInfo.put("buildingName", building.getBuildingName());
                        buildingInfo.put("buildingAlias", building.getBuildingAlias());
                        buildingInfo.put("unitNo", building.getUnitNo());
                        relations.put("building", buildingInfo);
                    }
                }
                
                // 关联社区信息
                if (house.getCommunityId() != null) {
                    CommunityInfo community = communityInfoService.getById(house.getCommunityId());
                    if (community != null) {
                        Map<String, Object> communityData = new LinkedHashMap<>();
                        communityData.put("id", community.getId());
                        communityData.put("communityName", community.getCommunityName());
                        communityData.put("communityCode", community.getCommunityCode());
                        communityData.put("detailAddress", community.getDetailAddress());
                        communityData.put("contactPhone", community.getContactPhone());
                        relations.put("community", communityData);
                    }
                }
            }
        }
        
        // 关联处理人员信息
        if (issue.getAssignedStaffId() != null) {
            Staff assignedStaff = staffService.getById(issue.getAssignedStaffId());
            if (assignedStaff != null) {
                Map<String, Object> staffInfo = new LinkedHashMap<>();
                staffInfo.put("id", assignedStaff.getId());
                staffInfo.put("name", assignedStaff.getName());
                staffInfo.put("phone", assignedStaff.getPhone());
                staffInfo.put("position", assignedStaff.getPosition());
                relations.put("assignedStaff", staffInfo);
            }
        }
        
        if (!relations.isEmpty()) {
            detailVO.setRelations(relations);
        }
        
        return detailVO;
    }
    
    @Override
    @Transactional
    public boolean addFollowUp(Long ownerId, IssueFollowUpRequest request) {
        // 验证问题归属
        OwnerIssue issue = getById(request.getIssueId());
        if (issue == null || !issue.getOwnerId().equals(ownerId)) {
            return false;
        }
        
        // 创建跟进记录
        IssueFollowUp followUp = new IssueFollowUp();
        followUp.setIssueId(request.getIssueId());
        followUp.setFollowUpType("业主补充");
        followUp.setFollowUpContent(request.getFollowUpContent());
        followUp.setOperatorType("owner");
        followUp.setOperatorId(ownerId);
        followUp.setOperatorName(issue.getContactName());
        followUp.setAttachments(request.getAttachments());
        
        int result = issueFollowUpMapper.insert(followUp);
        
        // 更新问题状态为待处理（如果已完成则重新打开，等待管理员重新分配）
        if ("已解决".equals(issue.getIssueStatus()) || "已关闭".equals(issue.getIssueStatus())) {
            issue.setIssueStatus("待处理");
            issue.setWorkStatus("待重新分配"); // 标记为待重新分配，让管理员重新处理
            issue.setIsEvaluated(0); // 重置评价状态
            updateById(issue);
        } else if ("处理中".equals(issue.getIssueStatus()) || "待确认".equals(issue.getIssueStatus())) {
            // 如果正在处理中，也改为待重新分配，让管理员重新评估
            issue.setIssueStatus("待处理");
            issue.setWorkStatus("待重新分配");
            updateById(issue);
        }
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean evaluateIssue(Long ownerId, IssueEvaluationRequest request) {
        // 1. 验证问题归属
        OwnerIssue issue = getById(request.getIssueId());
        if (issue == null || !issue.getOwnerId().equals(ownerId)) {
            return false;
        }
        
        // 2. 检查问题状态 - 只有"已完成"状态才允许评价
        if (!"已完成".equals(issue.getIssueStatus())) {
            return false;
        }
        
        // 3. 仅插入满意度等级和满意度反馈，不修改任何其他字段
        issue.setSatisfactionLevel(request.getSatisfactionLevel());
        issue.setSatisfactionFeedback(request.getSatisfactionFeedback());
        
        // 4. 保存更改
        updateById(issue);
        return true;
    }
    
    @Override
    public List<Map<String, Object>> getFollowUpRecords(Long issueId, Long ownerId) {
        // 验证问题归属
        OwnerIssue issue = getById(issueId);
        if (issue == null || !issue.getOwnerId().equals(ownerId)) {
            return new ArrayList<>();
        }
        
        // 查询跟进记录
        QueryWrapper<IssueFollowUp> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("issue_id", issueId)
                .orderByAsc("created_at");
        
        List<IssueFollowUp> followUps = issueFollowUpMapper.selectList(queryWrapper);
        
        // 转换为Map列表
        return followUps.stream().map(followUp -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", followUp.getId());
            map.put("followUpType", followUp.getFollowUpType());
            map.put("followUpContent", followUp.getFollowUpContent());
            map.put("operatorType", followUp.getOperatorType());
            map.put("operatorName", followUp.getOperatorName());
            map.put("createdAt", followUp.getCreatedAt());
            map.put("attachments", followUp.getAttachments());
            return map;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getFollowUpRecords(Long issueId, Long ownerId, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 100;
        
        // 验证问题归属
        OwnerIssue issue = getById(issueId);
        if (issue == null || !issue.getOwnerId().equals(ownerId)) {
            Map<String, Object> emptyResult = new LinkedHashMap<>();
            emptyResult.put("page", page);
            emptyResult.put("size", size);
            emptyResult.put("total", 0);
            emptyResult.put("pages", 0);
            emptyResult.put("items", new ArrayList<>());
            return emptyResult;
        }
        
        // 查询跟进记录
        QueryWrapper<IssueFollowUp> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("issue_id", issueId)
                .orderByAsc("created_at");
        
        // 计算分页
        int offset = (page - 1) * size;
        queryWrapper.last("LIMIT " + size + " OFFSET " + offset);
        
        List<IssueFollowUp> followUps = issueFollowUpMapper.selectList(queryWrapper);
        
        // 转换为Map列表
        List<Map<String, Object>> items = followUps.stream().map(followUp -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", followUp.getId());
            map.put("followUpType", followUp.getFollowUpType());
            map.put("followUpContent", followUp.getFollowUpContent());
            map.put("operatorType", followUp.getOperatorType());
            map.put("operatorName", followUp.getOperatorName());
            map.put("createdAt", followUp.getCreatedAt());
            map.put("attachments", followUp.getAttachments());
            return map;
        }).collect(Collectors.toList());
        
        // 查询总数
        QueryWrapper<IssueFollowUp> countWrapper = new QueryWrapper<>();
        countWrapper.eq("issue_id", issueId);
        long total = issueFollowUpMapper.selectCount(countWrapper);
        
        // 构建分页结果
        Map<String, Object> pageData = new LinkedHashMap<>();
        pageData.put("page", page);
        pageData.put("size", size);
        pageData.put("total", total);
        pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
        pageData.put("items", items);
        
        return pageData;
    }
}

