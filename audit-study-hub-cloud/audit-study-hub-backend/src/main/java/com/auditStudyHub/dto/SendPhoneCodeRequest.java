package com.auditStudyHub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送手机验证码请求DTO
 */
@Data
public class SendPhoneCodeRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
} 