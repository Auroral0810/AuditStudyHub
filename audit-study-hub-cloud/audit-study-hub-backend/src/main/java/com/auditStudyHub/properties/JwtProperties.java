package com.auditStudyHub.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * JWT属性配置类，支持动态刷新
 *
 * @author AuditStudyHub
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "audit-study-hub.security.jwt")
public class JwtProperties {

    /**
     * 密钥
     */
    private String secretKey;

    /**
     * 过期时间
     */
    private long expiration;

    /**
     * 记住我过期时间
     */
    private long rememberMeExpiration;

    /**
     * token前缀
     */
    private String tokenPrefix;

    /**
     * 请求头
     */
    private String header;

    /**
     * token有效期（秒）
     */
    private int tokenValidityInSeconds;

    /**
     * 记住我token有效期（秒）
     */
    private int tokenValidityInSecondsForRememberMe;
} 