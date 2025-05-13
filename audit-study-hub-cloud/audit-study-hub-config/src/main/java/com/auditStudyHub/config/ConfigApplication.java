package com.auditStudyHub.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.alibaba.cloud.nacos.NacosConfigProperties;

/**
 * 配置中心主应用
 */
@EnableConfigServer
@EnableDiscoveryClient
@SpringBootApplication
public class ConfigApplication {
    
    @Bean
    @ConfigurationProperties(prefix = "spring.cloud.nacos.config")
    public NacosConfigProperties nacosConfigProperties() {
        NacosConfigProperties properties = new NacosConfigProperties();
        // 手动禁用配置导入检查
        System.setProperty("spring.cloud.nacos.config.import-check.enabled", "false");
        return properties;
    }
    
    public static void main(String[] args) {
        // 添加系统属性禁用import-check
        System.setProperty("spring.cloud.nacos.config.import-check.enabled", "false");
        SpringApplication.run(ConfigApplication.class, args);
    }
} 