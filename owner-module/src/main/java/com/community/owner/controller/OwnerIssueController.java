package com.community.owner.controller;

import com.community.owner.dto.*;
import com.community.owner.dto.IssueDetailVO;
import com.community.owner.dto.IssueEvaluationRequest;
import com.community.owner.dto.IssueFollowUpRequest;
import com.community.owner.dto.IssueSubmitRequest;
import com.community.owner.utils.JwtUtil;
import com.community.owner.entity.Owner;
import com.community.owner.entity.OwnerIssue;
import com.community.owner.service.OwnerIssueService;
import com.community.owner.service.OwnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业主问题反馈控制器
 */
@RestController
@RequestMapping("/api/owner/issues")
@Tag(name = "问题反馈管理", description = "业主问题反馈、查询、追加和评价相关接口")
public class OwnerIssueController {
    
    @Autowired
    private OwnerIssueService ownerIssueService;
    
    @Autowired
    private OwnerService ownerService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 提交问题反馈
     */
    @PostMapping("/submit")
    @Operation(summary = "提交问题反馈", description = "业主提交问题反馈，可选择问题类型、描述详情、上传图片")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "提交成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> submitIssue(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "问题提交请求", required = true)
            @RequestBody IssueSubmitRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Owner owner = ownerService.findByUsername(username);
            
            if (owner == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 提交问题
            OwnerIssue issue = ownerIssueService.submitIssue(owner.getId(), request);
            
            response.put("success", true);
            response.put("data", issue);
            response.put("message", "问题提交成功，请等待处理");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "提交失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 查询我的问题列表
     */
    @GetMapping("/my-list")
    @Operation(summary = "查询我的问题列表", description = "分页查询当前业主提交的所有问题")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> listMyIssues(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "页码(从1开始)") 
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") 
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @Parameter(description = "问题状态（可选：待处理、处理中、已完成、已关闭）") 
            @RequestParam(value = "status", required = false) String status) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Owner owner = ownerService.findByUsername(username);
            
            if (owner == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 查询问题列表 - 支持按状态过滤
            Map<String, Object> pageData = ownerIssueService.listOwnerIssues(owner.getId(), page, size, status);
            
            response.put("success", true);
            response.put("data", pageData);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 查看问题详情
     */
    @GetMapping("/detail/{issueId}")
    @Operation(summary = "查看问题详情", description = "查看问题的详细信息和处理进度")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = IssueDetailVO.class))),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "问题不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> getIssueDetail(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "问题ID", required = true)
            @PathVariable("issueId") Long issueId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Owner owner = ownerService.findByUsername(username);
            
            if (owner == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 查询问题详情
            IssueDetailVO detailVO = ownerIssueService.getIssueDetail(issueId, owner.getId());
            
            if (detailVO == null) {
                response.put("success", false);
                response.put("message", "问题不存在或无权访问");
                return response;
            }
            
            response.put("success", true);
            response.put("data", detailVO);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 追加问题描述
     */
    @PostMapping("/follow-up")
    @Operation(summary = "追加问题描述", description = "业主对问题进行补充说明或追加描述")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "追加成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> addFollowUp(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "追加请求", required = true)
            @RequestBody IssueFollowUpRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Owner owner = ownerService.findByUsername(username);
            
            if (owner == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 追加问题描述
            boolean success = ownerIssueService.addFollowUp(owner.getId(), request);
            
            if (success) {
                response.put("success", true);
                response.put("message", "追加成功");
            } else {
                response.put("success", false);
                response.put("message", "追加失败，问题不存在或无权操作");
            }
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "追加失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 评价问题处理结果
     */
    @PostMapping("/evaluate")
    @Operation(summary = "评价问题处理结果", description = "业主对问题处理结果进行满意度评价，满意则结束，不满意可继续追加")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "评价成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> evaluateIssue(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "评价请求", required = true)
            @RequestBody IssueEvaluationRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Owner owner = ownerService.findByUsername(username);
            
            if (owner == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 评价问题
            boolean success = ownerIssueService.evaluateIssue(owner.getId(), request);
            
            if (success) {
                response.put("success", true);
                response.put("message", "评价成功");
            } else {
                response.put("success", false);
                response.put("message", "评价失败，问题不存在、无权操作或问题未完成");
            }
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "评价失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 获取问题的跟进记录
     */
    @GetMapping("/{issueId}/follow-ups")
    @Operation(summary = "获取问题的跟进记录", description = "分页获取指定问题的所有跟进记录")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> getIssueFollowUps(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "问题ID", required = true)
            @PathVariable("issueId") Long issueId,
            @Parameter(description = "页码(从1开始)") 
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") 
            @RequestParam(value = "size", required = false, defaultValue = "100") Integer size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Owner owner = ownerService.findByUsername(username);
            
            if (owner == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 查询跟进记录
            Map<String, Object> pageData = ownerIssueService.getFollowUpRecords(issueId, owner.getId(), page, size);
            
            response.put("success", true);
            response.put("data", pageData);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }
}

