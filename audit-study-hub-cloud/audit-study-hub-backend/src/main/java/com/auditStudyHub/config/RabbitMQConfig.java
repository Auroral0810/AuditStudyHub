package com.auditStudyHub.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;

/**
 * RabbitMQ配置类
 *
 * @author AuditStudyHub
 */
@Configuration
@EnableRabbit
public class RabbitMQConfig {
    // 消息通知队列
    public static final String NOTIFY_QUEUE = "notify_queue";
    // 文件处理队列
    public static final String FILE_PROCESS_QUEUE = "file_process_queue";
    // ES同步队列
    public static final String ES_SYNC_QUEUE = "es_sync_queue";
    // 交换机
    public static final String AUDIT_STUDY_EXCHANGE = "audit_study_exchange";
    // 路由键
    public static final String NOTIFY_ROUTING_KEY = "notify";
    public static final String FILE_PROCESS_ROUTING_KEY = "file_process";
    public static final String ES_SYNC_ROUTING_KEY = "es_sync";
    
    // 定义交换机名称
    public static final String EMAIL_EXCHANGE = "email.exchange";
    
    // 定义队列名称
    public static final String EMAIL_QUEUE = "email.queue";
    
    // 定义路由键
    public static final String EMAIL_ROUTING_KEY = "email.routing.key";
    
    @Bean
    public Queue notifyQueue() {
        return new Queue(NOTIFY_QUEUE, true);
    }
    
    @Bean
    public Queue fileProcessQueue() {
        return new Queue(FILE_PROCESS_QUEUE, true);
    }
    
    @Bean
    public Queue esSyncQueue() {
        return new Queue(ES_SYNC_QUEUE, true);
    }
    
    @Bean
    public DirectExchange auditStudyExchange() {
        return new DirectExchange(AUDIT_STUDY_EXCHANGE);
    }
    
    @Bean
    public Binding notifyBinding(Queue notifyQueue, DirectExchange auditStudyExchange) {
        return BindingBuilder.bind(notifyQueue).to(auditStudyExchange).with(NOTIFY_ROUTING_KEY);
    }
    
    @Bean
    public Binding fileProcessBinding(Queue fileProcessQueue, DirectExchange auditStudyExchange) {
        return BindingBuilder.bind(fileProcessQueue).to(auditStudyExchange).with(FILE_PROCESS_ROUTING_KEY);
    }
    
    @Bean
    public Binding esSyncBinding(Queue esSyncQueue, DirectExchange auditStudyExchange) {
        return BindingBuilder.bind(esSyncQueue).to(auditStudyExchange).with(ES_SYNC_ROUTING_KEY);
    }
    
    /**
     * 声明直连交换机
     */
    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE, true, false);
    }

    /**
     * 声明邮件队列
     */
    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding bindingEmailQueue() {
        return BindingBuilder.bind(emailQueue())
                .to(emailExchange())
                .with(EMAIL_ROUTING_KEY);
    }
    
    /**
     * 提供增强的ObjectMapper Bean，用于JSON序列化和反序列化
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册JavaTimeModule以处理Java 8日期时间类型
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
    
    /**
     * 配置消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
    
    /**
     * 配置RabbitTemplate，使用JSON序列化消息
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
    
    /**
     * 配置消息监听器容器工厂，确保使用相同的消息转换器处理接收消息
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
} 