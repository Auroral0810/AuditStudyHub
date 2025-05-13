package com.auditStudyHub.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 获取请求路径
        String requestPath = request.getServletPath();
        
        // 直接放行错误路径和管理员路径
        if (requestPath.equals("/admin") || requestPath.startsWith("/admin/")) {
            log.debug("直接放行路径: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 打印当前请求路径
            log.debug("处理请求: {}", requestPath);
            
            // 从请求中提取JWT令牌
            String jwt = resolveToken(request);
            
            // 如果令牌有效，设置认证信息
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("已设置认证信息: {}", authentication.getName());
            }
        } catch (Exception e) {
            log.debug("JWT处理中发生错误: {}", e.getMessage());
            // 不设置认证信息，让Spring Security继续处理
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/v3/api-docs") || 
            path.startsWith("/swagger-ui") || 
            path.equals("/doc.html") ||
            path.startsWith("/knife4j") ||
            path.startsWith("/webjars") ||
            path.contains("swagger");
    }
}
