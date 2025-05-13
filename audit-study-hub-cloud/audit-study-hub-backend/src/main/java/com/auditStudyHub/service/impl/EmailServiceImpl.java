package com.auditStudyHub.service.impl;

import com.auditStudyHub.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * 邮件服务实现类
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${audit-study-hub.email.verification-subject:审学汇账户验证}")
    private String verificationSubject;

    @Override
    public void sendVerificationCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(verificationSubject);
            
            // 使用简单的HTML
            String content = "<div style='padding:20px;font-family:Arial,sans-serif;'>"
                    + "<h2 style='color:#4481eb;'>审学汇账户验证</h2>"
                    + "<p>您好，您正在进行密码重置操作，您的验证码为：</p>"
                    + "<h3 style='color:#333;letter-spacing:2px;font-size:24px;font-weight:bold;'>" + code + "</h3>"
                    + "<p>该验证码将在5分钟后失效，请勿将验证码告知他人。</p>"
                    + "<p>如非本人操作，请忽略此邮件。</p>"
                    + "<hr/>"
                    + "<p style='font-size:12px;color:#999;'>本邮件由系统自动发送，请勿回复</p>"
                    + "</div>";
            helper.setText(content, true);
            
            mailSender.send(message);
            log.info("验证码邮件已发送到: {}", to);
        } catch (MessagingException e) {
            log.error("发送邮件失败:", e);
            throw new RuntimeException("发送邮件失败", e);
        }
    }
} 