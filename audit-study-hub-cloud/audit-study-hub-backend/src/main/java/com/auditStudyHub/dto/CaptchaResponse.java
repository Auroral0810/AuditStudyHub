package com.auditStudyHub.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 验证码响应DTO
 */
@Data
@Builder
public class CaptchaResponse {
    
    /**
     * 验证码唯一标识
     */
    private String captchaId;
    
    /**
     * Base64编码的验证码图片
     */
    private String imageBase64;
} 