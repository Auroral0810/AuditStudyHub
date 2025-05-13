package com.auditStudyHub.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "管理员评论信息")
public class AdminCommentDTO {
    
    @Schema(description = "评论ID")
    private Long id;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "资源ID")
    private Long resourceId;
    
    @Schema(description = "资源名称")
    private String resourceTitle;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名称")
    private String userName;
    
    @Schema(description = "父评论ID")
    private Long parentId;
    
    @Schema(description = "父评论内容")
    private String parentContent;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;
    
    @Schema(description = "是否删除，0-未删除，1-已删除")
    private Integer isDeleted;
} 