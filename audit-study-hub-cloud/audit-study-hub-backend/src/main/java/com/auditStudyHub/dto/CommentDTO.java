package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 评论数据传输对象
 */
@Data
@Schema(description = "评论信息")
public class CommentDTO {
    
    @Schema(description = "评论ID")
    private Long id;
    
    @Schema(description = "资源ID")
    private Long resourceId;
    
    @Schema(description = "评论用户ID")
    private Long userId;
    
    @Schema(description = "评论用户名")
    private String username;
    
    @Schema(description = "评论用户头像")
    private String userAvatar;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "父评论ID")
    private Long parentId;
    
    @Schema(description = "回复目标用户名")
    private String replyToUsername;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;
    
    @Schema(description = "是否删除")
    private Integer isDeleted;
    
    @Schema(description = "子评论列表")
    private List<CommentDTO> children;
} 