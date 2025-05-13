package com.auditStudyHub.config;

import com.auditStudyHub.properties.AliyunOssProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置
 *
 * @author AuditStudyHub
 */
@Configuration
@RequiredArgsConstructor
public class AliyunOssConfig {

    private final AliyunOssProperties ossProperties;

    /**
     * 获取地域节点
     */
    public String getEndpoint() {
        return ossProperties.getEndpoint();
    }

    /**
     * 获取访问密钥ID
     */
    public String getAccessKeyId() {
        return ossProperties.getAccessKeyId();
    }

    /**
     * 获取访问密钥密码
     */
    public String getAccessKeySecret() {
        return ossProperties.getAccessKeySecret();
    }

    /**
     * 获取存储桶名称
     */
    public String getBucketName() {
        return ossProperties.getBucketName();
    }

    /**
     * 获取URL前缀
     */
    public String getUrlPrefix() {
        return ossProperties.getUrlPrefix();
    }
} 