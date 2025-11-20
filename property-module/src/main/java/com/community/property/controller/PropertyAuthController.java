package com.community.property.controller;

import com.community.property.domain.dto.request.AuthRequest;
import com.community.property.domain.dto.response.AuthResponse;
import com.community.property.service.RedisMessageService;
import com.community.property.utils.JwtUtil;
import com.community.property.utils.PasswordUtil;
import com.community.property.domain.entity.Staff;
import com.community.property.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 物业员工认证控制器
 */
@RestController
@RequestMapping("/api/auth/property")
@Tag(name = "物业认证接口", description = "物业员工登录、退出等认证相关接口")
public class PropertyAuthController {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordUtil passwordUtil;
    
    @Autowired
    private StaffService staffService;

    @Autowired
    private RedisMessageService redisMessageService;
    
    /**
     * 物业员工登录
     */
    @PostMapping("/login")
    @Operation(summary = "物业员工登录", description = "物业员工使用用户名和密码进行登录")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "登录成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "用户名或密码错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public AuthResponse login(
        @Parameter(description = "认证请求信息", required = true) 
        @RequestBody AuthRequest authRequest) {
        try {
            // 查询员工信息
            Staff staff = staffService.findByUsername(authRequest.getUsername());
            
            if (staff == null) {
                return new AuthResponse(null, null, "用户不存在");
            }
            
            // 检查账号状态
            if (!"正常".equals(staff.getAccountStatus())) {
                return new AuthResponse(null, null, "账号已被禁用或锁定，请联系管理员");
            }
            
            // 检查工作状态
            if (!"在职".equals(staff.getWorkStatus())) {
                return new AuthResponse(null, null, "账号已离职，无法登录");
            }
            
            // 验证密码
            if (passwordUtil.matches(authRequest.getPassword(), staff.getPassword())) {
                // 生成JWT Token
                String token = jwtUtil.generateToken(staff.getUsername(), "staff", staff.getId());
                
                // 更新登录信息
                staffService.updateLoginInfo(staff.getId(), authRequest.getLoginIp());
                
                return new AuthResponse(token, "staff", "登录成功");
            } else {
                return new AuthResponse(null, null, "用户名或密码错误");
            }
        } catch (Exception e) {
            return new AuthResponse(null, null, "登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "物业员工修改自己的登录密码")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "修改成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权或原密码错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public AuthResponse changePassword(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "旧密码", required = true)
            @RequestParam("oldPassword") String oldPassword,
            @Parameter(description = "新密码", required = true)
            @RequestParam("newPassword") String newPassword) {
        
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Staff staff = staffService.findByUsername(username);
            
            if (staff == null) {
                return new AuthResponse(null, null, "用户不存在");
            }
            
            // 验证原密码
            if (!passwordUtil.matches(oldPassword, staff.getPassword())) {
                return new AuthResponse(null, null, "原密码错误");
            }
            
            // 更新密码
            String encodedPassword = passwordUtil.encodePassword(newPassword);
            boolean success = staffService.updatePassword(staff.getId(), encodedPassword);
            
            if (success) {
                return new AuthResponse(null, null, "密码修改成功，请重新登录");
            } else {
                return new AuthResponse(null, null, "密码修改失败");
            }
        } catch (Exception e) {
            return new AuthResponse(null, null, "修改失败: " + e.getMessage());
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "物业员工退出登录")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "退出成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public AuthResponse logout(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            return new AuthResponse(null, null, "退出登录成功");
        } catch (Exception e) {
            return new AuthResponse(null, null, "退出登录失败: " + e.getMessage());
        }
    }
}

