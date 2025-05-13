package com.auditStudyHub.config;

import com.auditStudyHub.security.JwtAuthenticationEntryPoint;
import com.auditStudyHub.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 允许匿名访问的接口路径
    private static final String[] AUTH_WHITELIST = {
            // 认证相关
            "/auth/login",
            "/auth/register",
            "/auth/captcha",
            "/auth/verify-captcha",
            "/auth/generate-password",
            "/auth/validate-user",
            "/auth/send-email-code",
            "/auth/send-phone-code",
            "/auth/reset-password",
            "/auth/verify-email-code",
            "/auth/verify-phone-code",
            "/auth/logout",

            // 文件上传和访问
            "/upload/**",

            // 学院和专业相关（用户界面可匿名访问）
            "/college/getColleges",
            "/college/getCollege",
            "/major/getMajors",
            "/major/getMajor",
            "/major/getMajorsByCollege",

            "/category/getCategories",

            "/course/getCourses",
            "/course/getCourse",
            "/course/searchCourses",
            
            // 资源相关（首页展示无需登录）
            "/resource/latest",
            "/resource/popular",
            "/resource/hot-search",
            "/resource/record-search",
            "/resource/list",
            "/resource/proxy-pdf",
            "/resource/proxy-file",
            "/resource/suggest",
            "/resource/record-download/**",
            "/resource/detail/**",
            "/resource/detail-with-comments/**",


            // 管理员相关
            "/admin/**",
            // Swagger UI
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            // 监控
            "/actuator/**",

            // 配置刷新
            "/config/refresh",
            "/config/refresh/bus",

            // 静态资源
            "/favicon.ico",

            // 添加 Knife4j 路径
            "/doc.html",
            "/webjars/**",
            "/knife4j/**",
            "/swagger-resources/**",
            "/v3/api-docs/swagger-config",
            "/v3/api-docs/默认",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions().disable())
                .authorizeHttpRequests(auth -> auth
                        // 首先明确放行所有错误处理请求
                        .requestMatchers("/error", "/error/**").permitAll()
                        // 然后放行所有DELETE方法的/error请求
                        .requestMatchers(HttpMethod.DELETE, "/error").permitAll()
                        // 白名单路径
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        // 特别加入管理员路径，确保优先级
                        .requestMatchers("/admin/**").permitAll()
                        // 最后是需要认证的请求
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/error/**", "/error")
                .requestMatchers(HttpMethod.DELETE, "/error");
    }
} 