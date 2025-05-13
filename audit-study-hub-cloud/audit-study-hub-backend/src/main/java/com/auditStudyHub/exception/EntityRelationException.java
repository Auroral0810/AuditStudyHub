package com.auditStudyHub.exception;

/**
 * 实体关系异常，用于处理实体之间的关联关系约束异常
 */
public class EntityRelationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public EntityRelationException(String message) {
        super(message);
    }

    public EntityRelationException(String message, Throwable cause) {
        super(message, cause);
    }
} 