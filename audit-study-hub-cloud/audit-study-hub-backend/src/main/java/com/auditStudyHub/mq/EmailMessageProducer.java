package com.auditStudyHub.mq;

import com.auditStudyHub.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮件消息生产者
 */
@Component
@Slf4j
public class EmailMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送验证码邮件消息到RabbitMQ
     *
     * @param to 收件人邮箱
     * @param code 验证码
     */
    public void sendVerificationCodeEmail(String to, String code) {
        Map<String, String> message = new HashMap<>();
        message.put("to", to);
        message.put("code", code);
        
        log.info("发送邮件验证码消息到MQ: {}", to);
        
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    message
            );
            log.info("验证码邮件消息已发送到MQ: {}", to);
        } catch (Exception e) {
            log.error("发送邮件验证码消息到MQ失败:", e);
            throw new RuntimeException("发送邮件验证码失败", e);
        }
    }
} 