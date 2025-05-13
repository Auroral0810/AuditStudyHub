package com.auditStudyHub.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 管理员资源数据传输对象
 */
@Data
@Schema(description = "管理员资源信息")
public class AdminResourceDTO {
    
    @Schema(description = "资源ID")
    private Long id;
    
    @Schema(description = "资源标题")
    private String title;
    
    @Schema(description = "资源描述")
    private String description;
    
    @Schema(description = "文件URL")
    private String fileUrl;
    
    @Schema(description = "文件类型")
    private String fileType;
    
    @Schema(description = "文件大小（字节）")
    private Long fileSize;
    
    @Schema(description = "下载次数")
    private Integer downloadCount;
    
    @Schema(description = "浏览次数")
    private Integer viewCount;
    
    @Schema(description = "点赞次数")
    private Integer likeCount;
    
    @Schema(description = "上传者ID")
    private Long uploaderId;
    
    @Schema(description = "上传者用户名")
    private String uploaderName;
    
    @Schema(description = "学院ID")
    private Long collegeId;
    
    @Schema(description = "学院名称")
    private String collegeName;
    
    @Schema(description = "专业ID")
    private Long majorId;
    
    @Schema(description = "专业名称")
    private String majorName;
    
    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "课程名称")
    private String courseName;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "分类名称")
    private String categoryName;
    
    @Schema(description = "标签，多个标签用逗号分隔")
    private String tags;
    
    @Schema(description = "状态，0-待审核，1-已通过，2-已拒绝")
    private Integer status;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;
    
    @Schema(description = "是否删除，0-未删除，1-已删除")
    private Integer isDeleted;
} 