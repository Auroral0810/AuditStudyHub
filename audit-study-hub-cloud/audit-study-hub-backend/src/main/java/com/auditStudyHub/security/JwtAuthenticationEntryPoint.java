package com.auditStudyHub.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT认证入口点，当用户尝试访问需要认证的资源而未提供有效凭证时处理
 *
 * @author AuditStudyHub
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        
        log.error("未认证的请求: {} {}, 错误: {}", 
                  request.getMethod(), 
                  request.getRequestURI(), 
                  authException.getMessage());
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        String errorJson = "{\"code\": 401, \"message\": \"未授权：" + authException.getMessage() + "\"}";
        response.getWriter().write(errorJson);
    }
} 