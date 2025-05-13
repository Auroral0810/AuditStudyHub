package com.auditStudyHub.service;

/**
 * 验证码服务接口
 */
public interface VerifyCodeService {

    /**
     * 生成验证码
     *
     * @return 随机生成的6位数字验证码
     */
    String generateCode();
    
    /**
     * 保存验证码
     *
     * @param email 邮箱
     * @param code 验证码
     */
    void saveCode(String email, String code);
    
    /**
     * 验证验证码
     *
     * @param email 邮箱
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean verifyCode(String email, String code);
} 