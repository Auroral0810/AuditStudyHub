package com.auditStudyHub.exception;

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {
    
    private Integer code;
    
    public BusinessException(String message) {
        super(message);
        this.code = 50000; // 默认业务异常错误码
    }
    
    public BusinessException(String message, Integer code) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 50000;
    }
    
    public BusinessException(String message, Integer code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public Integer getCode() {
        return code;
    }
} 