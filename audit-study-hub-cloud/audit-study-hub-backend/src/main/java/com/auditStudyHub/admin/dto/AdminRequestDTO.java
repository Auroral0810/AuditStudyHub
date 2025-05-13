package com.auditStudyHub.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "管理员资源请求DTO")
public class AdminRequestDTO {
    
    @Schema(description = "请求ID")
    private Long id;
    
    @Schema(description = "请求标题")
    private String title;
    
    @Schema(description = "请求内容")
    private String content;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名称")
    private String userName;
    
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
    
    @Schema(description = "关联资源ID")
    private Long resourceId;
    
    @Schema(description = "关联资源名称")
    private String resourceTitle;
    
    @Schema(description = "状态，0-未解决，1-已解决，2-关闭")
    private Integer status;
    
    @Schema(description = "回复数量")
    private Integer replyCount;
    
    @Schema(description = "浏览数量")
    private Integer viewCount;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;
    
    @Schema(description = "是否删除，0-未删除，1-已删除")
    private Integer isDeleted;
    
    @Schema(description = "父评论内容")
    private String parentContent;
} 