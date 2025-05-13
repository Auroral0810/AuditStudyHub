package com.auditStudyHub.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员资源请求回复搜索请求")
public class AdminResourceRequestReplySearchRequest {

    @Schema(description = "页码，默认1")
    private Integer page = 1;
    
    @Schema(description = "每页数量，默认10")
    private Integer size = 10;
    
    @Schema(description = "请求内容关键词")
    private String content;
    
    @Schema(description = "回复内容关键词")
    private String replyContent;
    
    @Schema(description = "用户名称")
    private String userName;
    
    @Schema(description = "资源名称")
    private String resourceName;
    
    @Schema(description = "父回复ID")
    private Long parentId;
    
    @Schema(description = "是否删除：0-未删除，1-已删除")
    private Integer isDeleted;
    
    @Schema(description = "起始日期，格式yyyy-MM-dd")
    private String startDate;
    
    @Schema(description = "截止日期，格式yyyy-MM-dd")
    private String endDate;
} 