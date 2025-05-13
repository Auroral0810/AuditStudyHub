package com.auditStudyHub.registry;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 注册中心与Spring Boot Admin服务监控主应用
 */
@EnableAdminServer
@EnableDiscoveryClient
@SpringBootApplication
public class RegistryApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RegistryApplication.class, args);
    }
} 