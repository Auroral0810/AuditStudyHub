package com.auditStudyHub.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员资源搜索请求
 */
@Data
@Schema(description = "管理员资源搜索请求")
public class AdminResourceSearchRequest {
    
    @Schema(description = "页码，默认1")
    private Integer page = 1;
    
    @Schema(description = "每页数量，默认10")
    private Integer size = 10;
    
    @Schema(description = "资源标题关键词")
    private String title;
    
    @Schema(description = "上传者用户名")
    private String uploaderName;
    
    @Schema(description = "学院ID")
    private Long collegeId;
    
    @Schema(description = "专业ID")
    private Long majorId;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "文件类型")
    private String fileType;
    
    @Schema(description = "资源状态：0-待审核，1-已通过，2-已拒绝")
    private Integer status;
    
    @Schema(description = "是否删除：0-未删除，1-已删除")
    private Integer isDeleted;
    
    @Schema(description = "起始日期，格式yyyy-MM-dd")
    private String startDate;
    
    @Schema(description = "截止日期，格式yyyy-MM-dd")
    private String endDate;
    
    @Schema(description = "最小文件大小(MB)")
    private Integer minSize;
    
    @Schema(description = "最大文件大小(MB)")
    private Integer maxSize;
    
    @Schema(description = "排序方式：newest(默认), downloads, views, likes")
    private String sort = "newest";
} 