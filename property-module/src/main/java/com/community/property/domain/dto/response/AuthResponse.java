package com.community.property.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "认证响应")
public class AuthResponse {
    
    @Schema(description = "JWT Token")
    private String token;
    
    @Schema(description = "角色类型:owner/staff")
    private String role;
    
    @Schema(description = "响应消息")
    private String message;
}

