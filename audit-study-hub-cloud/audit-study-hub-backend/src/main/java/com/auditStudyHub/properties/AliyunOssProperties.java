package com.auditStudyHub.properties;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS配置属性
 */
@Data
@ToString
@Component
@ConfigurationProperties(prefix = "audit-study-hub.aliyun.oss")
@EnableConfigurationProperties
public class AliyunOssProperties {
    
    /**
     * OSS访问域名
     */
    private String endpoint;
    
    /**
     * 访问密钥ID
     */
    private String accessKeyId;
    
    /**
     * 访问密钥Secret
     */
    private String accessKeySecret;
    
    /**
     * 存储桶名称
     */
    private String bucketName;
    
    /**
     * URL前缀
     */
    private String urlPrefix;
    
    /**
     * 下载配置
     */
    private Download download = new Download();
    
    @Data
    public static class Download {
        /**
         * 下载链接过期时间（毫秒）
         */
        private Long expireTime = 3600000L; // 默认1小时
    }
} 