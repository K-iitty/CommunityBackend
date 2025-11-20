package com.community.owner.domain.dto.request;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}