package com.community.owner.controller;

import com.community.owner.domain.dto.request.AuthRequest;
import com.community.owner.domain.dto.response.AuthResponse;
import com.community.owner.service.RedisMessageService;
import com.community.owner.utils.JwtUtil;
import com.community.owner.utils.PasswordUtil;
import com.community.owner.domain.entity.Owner;
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

@RestController
@RequestMapping("/api/auth/owner")
@Tag(name = "业主认证接口", description = "业主登录、注册、找回密码相关接口")
public class OwnerAuthController {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordUtil passwordUtil;
    
    @Autowired
    private OwnerService ownerService;

    @Autowired
    private RedisMessageService redisMessageService;
    
    /**
     * 业主登录
     * @param authRequest 登录请求
     * @return 认证响应
     */
    @PostMapping("/login")
    @Operation(summary = "业主登录", description = "业主使用用户名和密码进行登录")
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
            // 查询业主信息
            Owner owner = ownerService.findByUsername(authRequest.getUsername());
            
            if (owner == null) {
                return new AuthResponse(null, null, "用户不存在");
            }
            
            // 验证密码
            if (passwordUtil.matches(authRequest.getPassword(), owner.getPassword())) {
                // 生成JWT Token
                String token = jwtUtil.generateToken(owner.getUsername(), "owner");
                return new AuthResponse(token, "owner", "登录成功");
            } else {
                return new AuthResponse(null, null, "用户名或密码错误");
            }
        } catch (Exception e) {
            return new AuthResponse(null, null, "登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 业主注册
     * @param owner 业主信息
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "业主注册", description = "新业主注册账户")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "注册成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "409", description = "用户名已存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public AuthResponse register(
        @Parameter(description = "业主信息", required = true) 
        @RequestBody Owner owner) {
        try {
            // 检查用户名是否已存在
            if (ownerService.existsByUsername(owner.getUsername())) {
                return new AuthResponse(null, null, "用户名已存在");
            }
            
            // 加密密码
            String encodedPassword = passwordUtil.encodePassword(owner.getPassword());
            owner.setPassword(encodedPassword);
            
            // 设置默认认证状态为"未认证"，防止业主自行修改
            owner.setVerifyStatus("未认证");
            
            // 设置默认状态为"正常"
            owner.setStatus("正常");
            
            // 设置默认业主类型为"业主"
            if (owner.getOwnerType() == null || owner.getOwnerType().isEmpty()) {
                owner.setOwnerType("业主");
            }
            
            // 设置默认性别为"未知"
            if (owner.getGender() == null || owner.getGender().isEmpty()) {
                owner.setGender("未知");
            }
            
            // 设置默认民族为"汉族"
            if (owner.getNationality() == null || owner.getNationality().isEmpty()) {
                owner.setNationality("汉族");
            }
            
            // ============ 必填字段初始化（数据库约束：NOT NULL + UNIQUE）============
            // 由于 phone 和 id_card 有 UNIQUE 约束，不能使用固定值
            // 解决方案：使用 username 作为默认值（保证每个用户都有唯一值）
            
            // 设置默认姓名为用户名（如果未提供）
            if (owner.getName() == null || owner.getName().isEmpty()) {
                owner.setName(owner.getUsername());
            }
            
            // 设置默认手机号为用户名（如果未提供）
            // 注：phone 有 UNIQUE 约束，使用 username 保证唯一性
            if (owner.getPhone() == null || owner.getPhone().isEmpty()) {
                owner.setPhone(owner.getUsername());
            }
            
            // 设置默认身份证号为用户名（如果未提供）
            // 注：id_card 有 UNIQUE 约束，使用 username 保证唯一性
            if (owner.getIdCard() == null || owner.getIdCard().isEmpty()) {
                owner.setIdCard(owner.getUsername());
            }
            // ====================================================================
            
            // 保存业主信息
            boolean saved = ownerService.save(owner);
            
            if (saved) {
                // 生成JWT Token
                String token = jwtUtil.generateToken(owner.getUsername(), "owner");
                return new AuthResponse(token, "owner", "注册成功");
            } else {
                return new AuthResponse(null, null, "注册失败");
            }
        } catch (Exception e) {
            return new AuthResponse(null, null, "注册失败: " + e.getMessage());
        }
    }
    
    /**
     * 业主找回密码
     * @param authRequest 找回密码请求
     * @return 找回密码结果
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "业主找回密码", description = "业主通过用户名找回密码")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "找回密码成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public AuthResponse forgotPassword(
        @Parameter(description = "找回密码请求信息", required = true) 
        @RequestBody AuthRequest authRequest) {
        try {
            // 查询业主信息
            Owner owner = ownerService.findByUsername(authRequest.getUsername());
            if (owner == null) {
                return new AuthResponse(null, null, "用户不存在");
            }
            
            // 这里应该发送重置密码邮件或短信
            // 为了简化示例，直接重置密码为123456
            String newPassword = "123456";
            String encodedPassword = passwordUtil.encodePassword(newPassword);
            
            owner.setPassword(encodedPassword);
            ownerService.updateById(owner);
            
            return new AuthResponse(null, null, "密码已重置为: " + newPassword);
        } catch (Exception e) {
            return new AuthResponse(null, null, "找回密码失败: " + e.getMessage());
        }
    }
}