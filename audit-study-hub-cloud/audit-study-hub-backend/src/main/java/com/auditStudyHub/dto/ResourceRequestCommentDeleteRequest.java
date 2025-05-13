package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 删除资源请求评论的请求DTO
 * 
 * @author auroral
 */
@Data
@Schema(description = "删除资源请求评论")
public class ResourceRequestCommentDeleteRequest {
    
    @Schema(description = "评论ID", required = true)
    private Long id;
    
    @Schema(description = "用户ID", required = true)
    private Long userId;
} 