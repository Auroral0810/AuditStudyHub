package com.auditStudyHub.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "审核资源搜索请求")
public class AuditResourceSearchRequest {
    
    @Schema(description = "页码，默认1")
    private Integer page = 1;
    
    @Schema(description = "每页数量，默认10")
    private Integer size = 10;
    
    @Schema(description = "资源标题关键词")
    private String title;
    
    @Schema(description = "学院ID")
    private Long collegeId;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "文件类型")
    private String fileType;
    
    @Schema(description = "资源状态：0-待审核，1-已通过，2-已拒绝")
    private Integer status;
} 