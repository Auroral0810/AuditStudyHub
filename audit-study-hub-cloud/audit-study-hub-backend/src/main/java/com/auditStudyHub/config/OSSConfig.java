package com.auditStudyHub.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.auditStudyHub.properties.AliyunOssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 阿里云OSS配置类
 *
 * @author AuditStudyHub
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OSSConfig {

    private final AliyunOssProperties ossProperties;
    private final Environment environment;

    /**
     * 创建OSS客户端Bean
     *
     * @return OSS客户端
     */
    @Bean
    public OSS ossClient() {
        // 首先尝试从配置类获取参数
        String endpoint = ossProperties.getEndpoint();
        String accessKeyId = ossProperties.getAccessKeyId();
        String accessKeySecret = ossProperties.getAccessKeySecret();
        
        // 如果配置类中没有，尝试从环境变量获取
        if (isEmpty(endpoint)) {
            endpoint = environment.getProperty("OSS_ENDPOINT");
            log.info("从环境变量获取OSS_ENDPOINT: {}", endpoint);
        }
        
        if (isEmpty(accessKeyId)) {
            accessKeyId = environment.getProperty("OSS_ACCESS_KEY_ID");
            log.info("从环境变量获取OSS_ACCESS_KEY_ID");
        }
        
        if (isEmpty(accessKeySecret)) {
            accessKeySecret = environment.getProperty("OSS_ACCESS_KEY_SECRET");
            log.info("从环境变量获取OSS_ACCESS_KEY_SECRET");
        }
        
        // 检查并记录配置
        log.info("OSS配置 - endpoint: {}, accessKeyId存在: {}, accessKeySecret存在: {}", 
                endpoint, !isEmpty(accessKeyId), !isEmpty(accessKeySecret));
        
        // 创建OSS客户端
        if (isEmpty(endpoint) || isEmpty(accessKeyId) || isEmpty(accessKeySecret)) {
            throw new IllegalArgumentException("OSS配置不完整，请检查配置文件或环境变量");
        }
        
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
    
    /**
     * 获取存储桶名称
     *
     * @return 存储桶名称
     */
    public String getBucketName() {
        String bucketName = ossProperties.getBucketName();
        if (isEmpty(bucketName)) {
            bucketName = environment.getProperty("OSS_BUCKET_NAME");
        }
        log.debug("OSS存储桶名称: {}", bucketName);
        return bucketName;
    }
    
    /**
     * 获取URL前缀
     *
     * @return URL前缀
     */
    public String getUrlPrefix() {
        String urlPrefix = ossProperties.getUrlPrefix();
        if (isEmpty(urlPrefix)) {
            urlPrefix = environment.getProperty("OSS_URL_PREFIX");
        }
        return urlPrefix;
    }
    
    /**
     * 判断字符串是否为空
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
} 