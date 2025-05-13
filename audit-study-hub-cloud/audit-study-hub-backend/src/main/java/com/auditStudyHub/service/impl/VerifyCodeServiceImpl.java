package com.auditStudyHub.service.impl;

import com.auditStudyHub.service.VerifyCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Service
@Slf4j
public class VerifyCodeServiceImpl implements VerifyCodeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 验证码长度
    private static final int CODE_LENGTH = 6;
    
    // 验证码有效期（5分钟）
    private static final long CODE_EXPIRE_SECONDS = 5 * 60;
    
    // Redis前缀
    private static final String VERIFY_CODE_PREFIX = "verify_code:";

    @Override
    public String generateCode() {
        // 生成6位数字验证码
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    @Override
    public void saveCode(String email, String code) {
        // 将验证码保存到Redis，设置过期时间为5分钟
        String key = VERIFY_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("验证码已保存: {} -> {}, 有效期: {}秒", email, code, CODE_EXPIRE_SECONDS);
    }

    @Override
    public boolean verifyCode(String email, String code) {
        // 从Redis获取验证码进行验证
        String key = VERIFY_CODE_PREFIX + email;
        Object savedCode = redisTemplate.opsForValue().get(key);
        
        // 检查savedCode是否为已验证状态的验证码
        if (savedCode != null && savedCode.toString().startsWith("VERIFIED_")) {
            // 如果是已验证状态，提取原始验证码进行比较
            String originalCode = savedCode.toString().substring("VERIFIED_".length());
            if (code.equals(originalCode)) {
                log.info("已验证状态的验证码验证成功: {}", email);
                return true;
            }
        }
        
        if (savedCode == null) {
            log.warn("验证码不存在或已过期: {}", email);
            return false;
        }
        
        boolean isValid = code.equals(savedCode.toString());
        
        if (isValid) {
            // 验证成功后不删除验证码，而是标记为已验证状态
            // 并延长过期时间为10分钟，确保有足够时间完成重置密码流程
            redisTemplate.opsForValue().set(key, "VERIFIED_" + code, 10, TimeUnit.MINUTES);
            log.info("验证码验证成功并标记为已验证状态: {}", email);
        } else {
            log.warn("验证码验证失败: {}, 输入: {}, 保存: {}", email, code, savedCode);
        }
        
        return isValid;
    }
} 