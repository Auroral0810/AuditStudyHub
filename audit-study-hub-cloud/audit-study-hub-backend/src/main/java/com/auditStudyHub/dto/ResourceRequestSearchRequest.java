package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "资源请求搜索条件")
public class ResourceRequestSearchRequest {
    @Schema(description = "页码，从1开始")
    private Integer page = 1;
    
    @Schema(description = "每页数量")
    private Integer size = 10;
    
    @Schema(description = "搜索关键词")
    private String keyword;
    
    @Schema(description = "学院ID")
    private Long collegeId;
    
    @Schema(description = "专业ID")
    private Long majorId;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "状态：0-未解决，1-已解决")
    private Integer status;
    
    @Schema(description = "开始日期 格式：yyyy-MM-dd")
    private String startDate;
    
    @Schema(description = "结束日期 格式：yyyy-MM-dd")
    private String endDate;
    
    @Schema(description = "排序方式：newest, viewCount")
    private String sort = "newest";
    
    @Schema(description = "用户ID，用于获取当前用户发布的资源请求")
    private Long userId;
}
