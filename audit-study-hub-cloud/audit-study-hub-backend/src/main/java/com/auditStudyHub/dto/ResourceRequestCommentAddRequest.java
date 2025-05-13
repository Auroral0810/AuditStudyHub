package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 添加资源请求评论的请求DTO
 * 
 * @author auroral
 */
@Data
@Schema(description = "添加资源请求评论")
public class ResourceRequestCommentAddRequest {
    
    @Schema(description = "资源请求ID", required = true)
    private Long requestId;
    
    @Schema(description = "评论内容", required = true)
    private String content;
    
    @Schema(description = "用户ID", required = true)
    private Long userId;
    
    @Schema(description = "父评论ID（可选，如果是回复评论则提供）")
    private Long parentId;
} 