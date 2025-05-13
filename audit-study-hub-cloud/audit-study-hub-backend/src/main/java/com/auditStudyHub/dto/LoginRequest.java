package com.auditStudyHub.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    
    @NotBlank(message = "账号不能为空")
    private String account; // 通用账号字段（可以是用户名、邮箱或学号）
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private boolean rememberMe = false;
}