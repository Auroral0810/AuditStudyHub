package com.auditStudyHub.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     *
     * @param to 收件人邮箱地址
     * @param code 验证码
     */
    void sendVerificationCode(String to, String code);
} 