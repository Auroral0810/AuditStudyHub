package com.auditStudyHub.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "管理员资源请求回复DTO")
public class AdminResourceRequestReplyDTO {

    @Schema(description = "回复ID")
    private Long id;
    
    @Schema(description = "请求ID")
    private Long requestId;
    
    @Schema(description = "请求内容")
    private String content;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名称")
    private String userName;
    
    @Schema(description = "回复内容")
    private String replyContent;
    
    @Schema(description = "关联资源ID")
    private Long resourceId;
    
    @Schema(description = "关联资源名称")
    private String resourceName;
    
    @Schema(description = "父回复ID")
    private Long parentId;
    
    @Schema(description = "父回复内容")
    private String parentContent;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "回复时间")
    private Date replyTime;
    
    @Schema(description = "状态：0-正常，1-已删除")
    private Integer isDeleted;
} 