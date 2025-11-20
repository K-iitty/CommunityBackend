package com.community.property.controller;

import com.community.property.service.RedisMessageService;
import com.community.property.utils.JwtUtil;
import com.community.property.domain.entity.Staff;
import com.community.property.domain.entity.Department;
import com.community.property.service.StaffService;
import com.community.property.service.DepartmentService;
import com.community.property.service.PropertyQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 物业部门信息控制器
 */
@RestController
@RequestMapping("/api/property/department")
@Tag(name = "物业部门信息管理", description = "物业员工查看部门信息、部门任务等相关接口")
public class PropertyDepartmentController {
    
    @Autowired
    private StaffService staffService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private PropertyQueryService propertyQueryService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisMessageService redisMessageService;
    
    /**
     * 获取我的部门信息
     */
    @GetMapping("/my-department")
    @Operation(summary = "获取我的部门信息", description = "获取当前员工所在部门的详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "部门不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> getMyDepartment(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Staff staff = staffService.findByUsername(username);
            
            if (staff == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            if (staff.getDepartmentId() == null) {
                response.put("success", false);
                response.put("message", "未分配部门");
                return response;
            }
            
            // 获取部门信息
            Department department = departmentService.findById(staff.getDepartmentId());
            if (department == null) {
                response.put("success", false);
                response.put("message", "部门不存在");
                return response;
            }
            
            // 构建部门信息
            Map<String, Object> deptInfo = new LinkedHashMap<>();
            deptInfo.put("id", department.getId());
            deptInfo.put("departmentName", department.getDepartmentName());
            deptInfo.put("departmentCode", department.getDepartmentCode());
            deptInfo.put("departmentLevel", department.getDepartmentLevel());
            deptInfo.put("description", department.getDescription());
            deptInfo.put("status", department.getStatus());
            
            // 获取部门人员数量
            int memberCount = staffService.countByDepartmentId(department.getId());
            deptInfo.put("memberCount", memberCount);
            
            response.put("success", true);
            response.put("data", deptInfo);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 获取部门成员列表
     */
    @GetMapping("/members")
    @Operation(summary = "获取部门成员列表", description = "获取当前员工所在部门的所有成员列表，包含详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> getDepartmentMembers(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Staff staff = staffService.findByUsername(username);
            
            if (staff == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            if (staff.getDepartmentId() == null) {
                response.put("success", false);
                response.put("message", "未分配部门");
                return response;
            }
            
            // 使用PropertyQueryService进行多表JOIN查询，获取部门成员及其部门、角色信息
            List<Map<String, Object>> members = propertyQueryService.listDepartmentMembersWithDetails(staff.getDepartmentId());
            
            response.put("success", true);
            response.put("data", members);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 获取部门任务统计（系统不支持任务管理，返回空数据）
     */
    @GetMapping("/task-statistics")
    @Operation(summary = "获取部门任务统计", description = "获取部门的任务统计信息（注：当前系统不支持任务管理功能）")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> getDepartmentTaskStatistics(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Staff staff = staffService.findByUsername(username);
            
            if (staff == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 由于系统不支持任务管理，返回空的任务统计数据
            Map<String, Object> statistics = new LinkedHashMap<>();
            statistics.put("totalTasks", 0);
            statistics.put("completedTasks", 0);
            statistics.put("pendingTasks", 0);
            statistics.put("overdueTasks", 0);
            statistics.put("completionRate", 0);
            statistics.put("message", "系统不支持任务管理功能");
            
            response.put("success", true);
            response.put("data", statistics);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }
}

