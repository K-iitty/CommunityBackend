//package com.community.owner.controller;
//
//import com.community.owner.dto.AuthRequest;
//import com.community.owner.dto.AuthResponse;
//import com.community.owner.utils.JwtUtil;
//import com.community.owner.utils.PasswordUtil;
//import com.community.owner.entity.Staff;
//import com.community.owner.service.StaffService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/auth/staff")
//@Tag(name = "员工认证接口", description = "员工登录、注册、找回密码相关接口")
//public class StaffAuthController {
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @Autowired
//    private PasswordUtil passwordUtil;
//
//    @Autowired
//    private StaffService staffService;
//
//    /**
//     * 员工登录
//     * @param authRequest 登录请求
//     * @return 认证响应
//     */
//    @PostMapping("/login")
//    @Operation(summary = "员工登录", description = "员工使用用户名和密码进行登录")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "登录成功",
//                content = @Content(mediaType = "application/json",
//                        schema = @Schema(implementation = AuthResponse.class))),
//        @ApiResponse(responseCode = "400", description = "请求参数错误"),
//        @ApiResponse(responseCode = "401", description = "用户名或密码错误"),
//        @ApiResponse(responseCode = "500", description = "服务器内部错误")
//    })
//    public AuthResponse login(
//        @Parameter(description = "认证请求信息", required = true)
//        @RequestBody AuthRequest authRequest) {
//        try {
//            // 查询员工信息
//            Staff staff = staffService.findByUsername(authRequest.getUsername());
//
//            if (staff == null) {
//                return new AuthResponse(null, null, "用户不存在");
//            }
//
//            // 验证密码
//            if (passwordUtil.matches(authRequest.getPassword(), staff.getPassword())) {
//                // 生成JWT Token
//                String token = jwtUtil.generateToken(staff.getUsername(), "staff");
//                return new AuthResponse(token, "staff", "登录成功");
//            } else {
//                return new AuthResponse(null, null, "用户名或密码错误");
//            }
//        } catch (Exception e) {
//            return new AuthResponse(null, null, "登录失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 员工注册
//     * @param staff 员工信息
//     * @return 注册结果
//     */
//    @PostMapping("/register")
//    @Operation(summary = "员工注册", description = "新员工注册账户")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "注册成功",
//                content = @Content(mediaType = "application/json",
//                        schema = @Schema(implementation = AuthResponse.class))),
//        @ApiResponse(responseCode = "400", description = "请求参数错误"),
//        @ApiResponse(responseCode = "409", description = "用户名已存在"),
//        @ApiResponse(responseCode = "500", description = "服务器内部错误")
//    })
//    public AuthResponse register(
//        @Parameter(description = "员工信息", required = true)
//        @RequestBody Staff staff) {
//        try {
//            // 检查用户名是否已存在
//            if (staffService.existsByUsername(staff.getUsername())) {
//                return new AuthResponse(null, null, "用户名已存在");
//            }
//
//            // 加密密码
//            String encodedPassword = passwordUtil.encodePassword(staff.getPassword());
//            staff.setPassword(encodedPassword);
//
//            // 保存员工信息
//            boolean saved = staffService.save(staff);
//
//            if (saved) {
//                // 生成JWT Token
//                String token = jwtUtil.generateToken(staff.getUsername(), "staff");
//                return new AuthResponse(token, "staff", "注册成功");
//            } else {
//                return new AuthResponse(null, null, "注册失败");
//            }
//        } catch (Exception e) {
//            return new AuthResponse(null, null, "注册失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 员工找回密码
//     * @param authRequest 找回密码请求
//     * @return 找回密码结果
//     */
//    @PostMapping("/forgot-password")
//    @Operation(summary = "员工找回密码", description = "员工通过用户名找回密码")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "找回密码成功",
//                content = @Content(mediaType = "application/json",
//                        schema = @Schema(implementation = AuthResponse.class))),
//        @ApiResponse(responseCode = "400", description = "请求参数错误"),
//        @ApiResponse(responseCode = "404", description = "用户不存在"),
//        @ApiResponse(responseCode = "500", description = "服务器内部错误")
//    })
//    public AuthResponse forgotPassword(
//        @Parameter(description = "找回密码请求信息", required = true)
//        @RequestBody AuthRequest authRequest) {
//        try {
//            // 查询员工信息
//            Staff staff = staffService.findByUsername(authRequest.getUsername());
//            if (staff == null) {
//                return new AuthResponse(null, null, "用户不存在");
//            }
//
//            // 这里应该发送重置密码邮件或短信
//            // 为了简化示例，直接重置密码为123456
//            String newPassword = "123456";
//            String encodedPassword = passwordUtil.encodePassword(newPassword);
//
//            staff.setPassword(encodedPassword);
//            staffService.updateById(staff);
//
//            return new AuthResponse(null, null, "密码已重置为: " + newPassword);
//        } catch (Exception e) {
//            return new AuthResponse(null, null, "找回密码失败: " + e.getMessage());
//        }
//    }
//}