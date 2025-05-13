package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

/**
 * 用户下载记录DTO
 */
@Data
@Schema(description = "用户下载记录")
public class UserDownloadDTO {
    
    @Schema(description = "下载记录ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "资源ID")
    private Long resourceId;
    
    @Schema(description = "资源标题")
    private String resourceTitle;
    
    @Schema(description = "资源分类名称")
    private String categoryName;
    
    @Schema(description = "资源封面URL")
    private String coverUrl;
    
    @Schema(description = "上传者名称")
    private String uploaderName;
    
    @Schema(description = "资源文件类型")
    private String fileType;
    
    @Schema(description = "文件大小(字节)")
    private Long fileSize;
    
    @Schema(description = "下载时间")
    private Date downloadedAt;
} 