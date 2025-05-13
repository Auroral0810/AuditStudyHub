package com.auditStudyHub.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * CORS跨域属性配置类，支持动态刷新
 *
 * @author AuditStudyHub
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "audit-study-hub.cors")
public class CorsProperties {

    /**
     * 允许的来源
     */
    private String allowedOrigins;

    /**
     * 允许的方法
     */
    private String allowedMethods;

    /**
     * 允许的标头
     */
    private String allowedHeaders;

    /**
     * 是否允许凭证
     */
    private boolean allowCredentials;

    /**
     * 最大缓存时间
     */
    private long maxAge;
} 