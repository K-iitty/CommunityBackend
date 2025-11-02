package com.community.owner.controller;

import com.community.owner.dto.DepartmentContactVO;
import com.community.owner.utils.JwtUtil;
import com.community.owner.entity.Owner;
import com.community.owner.service.OwnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 电话咨询控制器
 * 显示各部门负责人联系方式
 */
@RestController
@RequestMapping("/api/owner/contacts")
@Tag(name = "业主电话咨询", description = "查询各部门负责人电话信息")
public class DepartmentContactController {
    
    @Autowired
    private OwnerService ownerService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 查询所有部门负责人联系方式
     */
    @GetMapping("/department-leaders")
    @Operation(summary = "查询部门负责人联系方式", description = "显示各个物业部门的负责人姓名、职位和电话")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = DepartmentContactVO.class))),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> getDepartmentLeaders(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {
        
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
            
            // 查询各部门负责人信息
            String sql = "SELECT " +
                    "    d.id AS departmentId, " +
                    "    d.department_name AS departmentName, " +
                    "    d.description AS description, " +
                    "    s.id AS staffId, " +
                    "    s.name AS staffName, " +
                    "    s.position AS position, " +
                    "    s.phone AS phone " +
                    "FROM department d " +
                    "LEFT JOIN staff s ON d.id = s.department_id AND s.is_manager = 1 AND s.work_status = '在职' " +
                    "WHERE d.status = '启用' " +
                    "ORDER BY d.sort_order, d.id";
            
            RowMapper<DepartmentContactVO> mapper = (rs, rowNum) -> {
                DepartmentContactVO vo = new DepartmentContactVO();
                vo.setDepartmentId(rs.getLong("departmentId"));
                vo.setDepartmentName(rs.getString("departmentName"));
                vo.setDescription(rs.getString("description"));
                vo.setStaffId((Long) rs.getObject("staffId"));
                vo.setStaffName(rs.getString("staffName"));
                vo.setPosition(rs.getString("position"));
                vo.setPhone(rs.getString("phone"));
                return vo;
            };
            
            List<DepartmentContactVO> contacts = jdbcTemplate.query(sql, mapper);
            
            response.put("success", true);
            response.put("data", contacts);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 查询指定部门负责人联系方式
     */
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "查询指定部门负责人", description = "根据部门ID查询该部门负责人的联系方式")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "部门不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> getDepartmentLeader(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "部门ID", required = true)
            @PathVariable("departmentId") Long departmentId) {
        
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
            
            // 查询指定部门负责人信息
            String sql = "SELECT " +
                    "    d.id AS departmentId, " +
                    "    d.department_name AS departmentName, " +
                    "    d.description AS description, " +
                    "    s.id AS staffId, " +
                    "    s.name AS staffName, " +
                    "    s.position AS position, " +
                    "    s.phone AS phone, " +
                    "    s.email AS email " +
                    "FROM department d " +
                    "LEFT JOIN staff s ON d.id = s.department_id AND s.is_manager = 1 AND s.work_status = '在职' " +
                    "WHERE d.id = ? AND d.status = '启用'";
            
            RowMapper<DepartmentContactVO> mapper = (rs, rowNum) -> {
                DepartmentContactVO vo = new DepartmentContactVO();
                vo.setDepartmentId(rs.getLong("departmentId"));
                vo.setDepartmentName(rs.getString("departmentName"));
                vo.setDescription(rs.getString("description"));
                vo.setStaffId((Long) rs.getObject("staffId"));
                vo.setStaffName(rs.getString("staffName"));
                vo.setPosition(rs.getString("position"));
                vo.setPhone(rs.getString("phone"));
                return vo;
            };
            
            List<DepartmentContactVO> contacts = jdbcTemplate.query(sql, mapper, departmentId);
            
            if (contacts.isEmpty()) {
                response.put("success", false);
                response.put("message", "部门不存在");
                return response;
            }
            
            response.put("success", true);
            response.put("data", contacts.get(0));
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }
}

