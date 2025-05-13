package com.auditStudyHub.service.impl;

import com.auditStudyHub.dto.CaptchaResponse;
import com.auditStudyHub.service.CaptchaService;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.ChineseCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    // 验证码有效期（2分钟）
    private static final long CAPTCHA_EXPIRE_SECONDS = 2 * 60;
    
    // Redis前缀
    private static final String CAPTCHA_PREFIX = "captcha:";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public CaptchaResponse generateCaptcha() throws IOException {
        // 生成验证码ID
        String captchaId = UUID.randomUUID().toString();
        
        // 随机选择验证码类型
        Captcha captcha = null;
        try {
            captcha = generateRandomCaptcha();
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        }

        // 获取验证码文本
        String captchaText = captcha.text();
        
        // 将验证码存入Redis，有效期2分钟
        String key = CAPTCHA_PREFIX + captchaId;
        redisTemplate.opsForValue().set(key, captchaText, CAPTCHA_EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        log.info("生成验证码: ID={}, 内容={}, 有效期={}秒", captchaId, captchaText, CAPTCHA_EXPIRE_SECONDS);
        
        // 构建响应
        return CaptchaResponse.builder()
                .captchaId(captchaId)
                .imageBase64(captcha.toBase64())
                .build();
    }

    @Override
    public boolean verifyCaptcha(String captchaId, String captchaCode) {
        if (captchaId == null || captchaCode == null) {
            log.warn("验证码参数为空");
            return false;
        }
        
        // 构建Redis键
        String key = CAPTCHA_PREFIX + captchaId;
        
        // 从Redis获取验证码
        Object savedCode = redisTemplate.opsForValue().get(key);
        
        if (savedCode == null) {
            log.warn("验证码不存在或已过期: {}", captchaId);
            return false;
        }
        
        // 验证码比较（忽略大小写）
        boolean isValid = captchaCode.equalsIgnoreCase(savedCode.toString());
        
        // 无论验证成功与否，都删除验证码，防止重复使用
        redisTemplate.delete(key);
        
        if (isValid) {
            log.info("验证码验证成功: {}", captchaId);
        } else {
            log.warn("验证码验证失败: {}, 输入: {}, 正确: {}", captchaId, captchaCode, savedCode);
        }
        
        return isValid;
    }
    
    /**
     * 随机生成不同类型的验证码
     *
     * @return 验证码对象
     */
    private Captcha generateRandomCaptcha() throws  FontFormatException {
        // 不再随机选择类型，固定使用普通字符验证码
        // 使用SpecCaptcha（普通字符验证码）
        Captcha captcha = new SpecCaptcha(120, 45);
        captcha.setCharType(Captcha.TYPE_DEFAULT);
        
        // 设置验证码字体
        try {
            captcha.setFont(Captcha.FONT_1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 设置验证码长度
        captcha.setLen(4);
        
        return captcha;
    }
} 