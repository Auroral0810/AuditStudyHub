package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

/**
 * 用户上传记录DTO
 */
@Data
@Schema(description = "用户上传记录")
public class UserUploadDTO {
    
    @Schema(description = "资源ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "资源标题")
    private String title;
    
    @Schema(description = "资源描述（摘要）")
    private String description;
    
    @Schema(description = "资源分类名称")
    private String categoryName;
    
    @Schema(description = "文件类型")
    private String fileType;
    
    @Schema(description = "文件大小(字节)")
    private Long fileSize;
    
    @Schema(description = "下载次数")
    private Integer downloadCount;
    
    @Schema(description = "浏览次数")
    private Integer viewCount;
    
    @Schema(description = "点赞次数")
    private Integer likeCount;
    
    @Schema(description = "状态：0-待审核，1-已通过，2-已拒绝")
    private Integer status;
    
    @Schema(description = "上传时间")
    private Date createTime;
} 