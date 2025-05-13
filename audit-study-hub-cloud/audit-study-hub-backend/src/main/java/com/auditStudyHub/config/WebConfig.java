package com.auditStudyHub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web相关配置
 *
 * @author AuditStudyHub
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${audit-study-hub.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${audit-study-hub.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${audit-study-hub.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${audit-study-hub.cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${audit-study-hub.cors.max-age}")
    private long maxAge;

    /**
     * 配置跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders)
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置knife4j和swagger相关资源路径
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
} 