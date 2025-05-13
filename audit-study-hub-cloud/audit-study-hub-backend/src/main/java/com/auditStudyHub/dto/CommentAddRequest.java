package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 添加评论请求DTO
 * 
 * @author auroral
 */
@Data
@Schema(description = "添加评论请求")
public class CommentAddRequest {
    
    @Schema(description = "资源ID", required = true)
    private Long resourceId;
    
    @Schema(description = "评论内容", required = true)
    private String content;
    
    @Schema(description = "用户ID", required = true)
    private Long userId;
    
    @Schema(description = "回复的评论ID（可选，如果是回复评论则提供）")
    private Long replyTo;
} 