package com.auditStudyHub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

/**
 * Elasticsearch配置类
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.auditStudyHub.repository.es")
public class ElasticsearchConfig extends ElasticsearchConfiguration {
    
    @Value("${spring.elasticsearch.uris}")
    private String uris;
    
    @Value("${spring.elasticsearch.username:}")
    private String username;
    
    @Value("${spring.elasticsearch.password:}")
    private String password;
    
    @Override
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = (ClientConfiguration.MaybeSecureClientConfigurationBuilder) ClientConfiguration.builder()
                .connectedTo(uris.replace("http://", ""))
                .withConnectTimeout(Duration.ofSeconds(15))  // 增加连接超时时间
                .withSocketTimeout(Duration.ofSeconds(15));  // 增加socket超时时间
        
        // 如果提供了用户名和密码，则配置认证
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            builder.withBasicAuth(username, password);
        }
        
        return builder.build();
    }
}
