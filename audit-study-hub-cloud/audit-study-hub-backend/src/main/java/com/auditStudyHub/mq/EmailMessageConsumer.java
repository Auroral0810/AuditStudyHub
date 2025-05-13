package com.auditStudyHub.mq;

import com.auditStudyHub.config.RabbitMQConfig;
import com.auditStudyHub.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 邮件消息消费者
 */
@Component
@Slf4j
public class EmailMessageConsumer {

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 监听邮件队列，处理验证码邮件发送
     *
     * @param payload 消息内容（JSON字符串）
     * @param channel 消息通道
     * @param deliveryTag 消息标签
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleVerificationCodeEmail(
            Message message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            // 获取消息体
            String payload = new String(message.getBody());
            log.info("接收到邮件验证码消息原始内容: {}", payload);
            
            // 解析JSON
            Map<String, String> emailData = objectMapper.readValue(payload, Map.class);
            log.info("解析后的邮件验证码消息: {}", emailData);
            
            String to = emailData.get("to");
            String code = emailData.get("code");
            
            if (to == null || code == null) {
                log.error("邮件消息格式错误: {}", emailData);
                channel.basicReject(deliveryTag, false);
                return;
            }
            
            try {
                // 发送验证码邮件
                emailService.sendVerificationCode(to, code);
                // 消息处理成功，确认消息
                channel.basicAck(deliveryTag, false);
                log.info("邮件验证码发送成功: {}", to);
            } catch (Exception e) {
                log.error("发送邮件验证码失败:", e);
                // 消息处理失败，拒绝消息并重新入队
                channel.basicReject(deliveryTag, true);
            }
        } catch (IOException e) {
            log.error("处理邮件消息异常:", e);
            try {
                // 处理异常，拒绝消息但不重新入队
                channel.basicReject(deliveryTag, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败:", ex);
            }
        }
    }
} 