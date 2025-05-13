package com.auditStudyHub.config;

import com.auditStudyHub.properties.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS跨域配置
 *
 * @author AuditStudyHub
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {
    
    private final CorsProperties corsProperties;
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 支持动态配置允许的域名
        String allowedOrigins = corsProperties.getAllowedOrigins();
        if ("*".equals(allowedOrigins)) {
            config.addAllowedOriginPattern("*");
        } else {
            String[] origins = allowedOrigins.split(",");
            for (String origin : origins) {
                config.addAllowedOrigin(origin.trim());
            }
        }
        
        // 允许跨域发送cookie
        config.setAllowCredentials(corsProperties.isAllowCredentials());
        
        // 配置允许的请求头
        String allowedHeaders = corsProperties.getAllowedHeaders();
        if ("*".equals(allowedHeaders)) {
            config.addAllowedHeader("*");
        } else {
            String[] headers = allowedHeaders.split(",");
            for (String header : headers) {
                config.addAllowedHeader(header.trim());
            }
        }
        
        // 配置允许的请求方法
        String allowedMethods = corsProperties.getAllowedMethods();
        if ("*".equals(allowedMethods)) {
            config.addAllowedMethod("*");
        } else {
            String[] methods = allowedMethods.split(",");
            for (String method : methods) {
                config.addAllowedMethod(method.trim());
            }
        }
        
        // 暴露响应头
        config.addExposedHeader("*");
        
        // 预检请求的有效期，单位为秒
        config.setMaxAge(corsProperties.getMaxAge());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
} 