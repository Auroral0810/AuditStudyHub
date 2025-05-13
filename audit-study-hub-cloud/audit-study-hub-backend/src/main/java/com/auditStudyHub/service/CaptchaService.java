package com.auditStudyHub.service;

import com.auditStudyHub.dto.CaptchaResponse;

import java.io.IOException;

/**
 * 验证码服务接口
 */
public interface CaptchaService {
    
    /**
     * 生成验证码
     *
     * @return 验证码响应，包含Base64编码的图片和验证码ID
     */
    CaptchaResponse generateCaptcha() throws IOException;
    
    /**
     * 验证验证码
     *
     * @param captchaId 验证码ID
     * @param captchaCode 用户输入的验证码
     * @return 是否验证成功
     */
    boolean verifyCaptcha(String captchaId, String captchaCode);
} 