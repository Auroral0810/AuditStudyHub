package com.auditStudyHub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 验证码验证请求DTO
 */
@Data
public class VerifyCaptchaRequest {
    
    /**
     * 验证码唯一标识
     */
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;
    
    /**
     * 用户输入的验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
} 