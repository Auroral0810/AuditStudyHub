package com.auditStudyHub.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Redis属性配置类，支持动态刷新
 *
 * @author AuditStudyHub
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    /**
     * 密码
     */
    private String password;

    /**
     * 数据库索引
     */
    private int database;

    /**
     * 连接超时时间(毫秒)
     */
    private int timeout;

    /**
     * Lettuce连接池配置
     */
    private Lettuce lettuce;

    @Data
    public static class Lettuce {
        /**
         * 连接池配置
         */
        private Pool pool;

        @Data
        public static class Pool {
            /**
             * 最大活跃连接数，默认8
             */
            private int maxActive;

            /**
             * 最大等待时间(毫秒)，默认-1表示无限等待
             */
            private int maxWait;

            /**
             * 最大空闲连接数，默认8
             */
            private int maxIdle;

            /**
             * 最小空闲连接数，默认0
             */
            private int minIdle;
        }
    }
} 