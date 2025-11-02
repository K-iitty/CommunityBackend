package com.community.owner.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String role;
    private String message;
    
    public AuthResponse(String token, String role, String message) {
        this.token = token;
        this.role = role;
        this.message = message;
    }
}