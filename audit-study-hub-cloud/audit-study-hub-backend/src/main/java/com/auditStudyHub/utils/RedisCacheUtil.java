package com.auditStudyHub.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存工具类
 * 
 * @author auroral
 */
@Component
public class RedisCacheUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 默认缓存时间（24小时）
    private static final long DEFAULT_EXPIRE = 24 * 60 * 60;
    
    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, DEFAULT_EXPIRE, TimeUnit.SECONDS);
    }
    
    /**
     * 设置缓存并指定过期时间
     */
    public void set(String key, Object value, long expireTime) {
        redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
    }
    
    /**
     * 获取缓存
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return clazz.cast(value);
    }
    
    /**
     * 获取列表缓存
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return (List<T>) value;
    }
    
    /**
     * 删除缓存
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
    
    /**
     * 检查缓存是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
