package com.auditStudyHub.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置
 *
 * @author AuditStudyHub
 */
@Data
@Configuration
public class JwtConfig {

    /**
     * 密钥
     */
    @Value("${audit-study-hub.security.jwt.secret-key}")
    private String secretKey;

    /**
     * 过期时间
     */
    @Value("${audit-study-hub.security.jwt.expiration}")
    private long expiration;

    /**
     * 记住我过期时间
     */
    @Value("${audit-study-hub.security.jwt.remember-me-expiration}")
    private long rememberMeExpiration;

    /**
     * token前缀
     */
    @Value("${audit-study-hub.security.jwt.token-prefix}")
    private String tokenPrefix;

    /**
     * 请求头
     */
    @Value("${audit-study-hub.security.jwt.header}")
    private String header;
} 