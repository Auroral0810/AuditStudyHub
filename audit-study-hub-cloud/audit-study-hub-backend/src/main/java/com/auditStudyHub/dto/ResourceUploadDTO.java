package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "资源上传请求DTO")
public class ResourceUploadDTO {
    
    @Schema(description = "资源标题", required = true)
    private String title;
    
    @Schema(description = "资源描述", required = true)
    private String description;
    
    @Schema(description = "分类ID", required = true)
    private Long categoryId;
    
    @Schema(description = "分类名称")
    private String categoryName;
    
    @Schema(description = "学院ID")
    private Long collegeId;
    
    @Schema(description = "学院名称")
    private String collegeName;
    
    @Schema(description = "专业ID")
    private Long majorId;
    
    @Schema(description = "专业名称")
    private String majorName;
    
    @Schema(description = "课程名称")
    private String courseName;
    
    @Schema(description = "标签（JSON字符串）")
    private String tags;
    
    @Schema(description = "文件URL", required = true)
    private String fileUrl;
    
    @Schema(description = "文件大小（字节）")
    private Long fileSize;
    
    @Schema(description = "文件类型")
    private String fileType;
    
    @Schema(description = "上传者ID")
    private Long uploaderId;
}
