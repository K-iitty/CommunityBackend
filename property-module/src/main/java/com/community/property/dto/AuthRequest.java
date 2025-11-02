package com.community.property.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 认证请求对象
 */
@Data
@Schema(description = "认证请求")
public class AuthRequest {
    
    @Schema(description = "用户名", required = true)
    private String username;
    
    @Schema(description = "密码", required = true)
    private String password;
    
    @Schema(description = "登录IP")
    private String loginIp;
}

