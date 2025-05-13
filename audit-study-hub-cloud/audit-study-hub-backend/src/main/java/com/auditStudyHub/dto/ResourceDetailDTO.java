package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 资源详情数据传输对象
 */
@Data
@Schema(description = "资源详情信息")
public class ResourceDetailDTO {
    
    @Schema(description = "资源信息")
    private ResourceDTO resource;
    
    @Schema(description = "评论列表")
    private List<CommentDTO> comments;
    
    @Schema(description = "评论总数")
    private Long commentCount;
} 