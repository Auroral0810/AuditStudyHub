package com.auditStudyHub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * 审学汇平台 - 启动类
 *
 * @author AuditStudyHub
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@MapperScan("com.auditStudyHub.mapper")
@EnableConfigurationProperties
@RefreshScope
public class AuditStudyHubApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuditStudyHubApplication.class, args);
    }
} 