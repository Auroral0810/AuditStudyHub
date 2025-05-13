package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新资源请求的请求DTO
 * 
 * @author auroral
 */
@Data
@Schema(description = "更新资源请求")
public class ResourceRequestUpdateRequest {
    
    @Schema(description = "请求标题", required = true)
    private String title;
    
    @Schema(description = "请求内容", required = true)
    private String content;
    
    @Schema(description = "课程ID（可选）")
    private Long courseId;
    
    @Schema(description = "课程名称（可选）")
    private String courseName;
    
    @Schema(description = "分类ID", required = true)
    private Long categoryId;
    
    @Schema(description = "学院ID（可选）")
    private Long collegeId;
    
    @Schema(description = "专业ID（可选）")
    private Long majorId;
}
