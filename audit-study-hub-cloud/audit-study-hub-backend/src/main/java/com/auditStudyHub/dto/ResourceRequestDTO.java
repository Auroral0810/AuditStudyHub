package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "资源请求DTO")
public class ResourceRequestDTO {
    @Schema(description = "请求ID")
    private Long id;
    
    @Schema(description = "请求标题")
    private String title;
    
    @Schema(description = "请求内容")
    private String content;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "用户头像")
    private String userAvatar;
    
    @Schema(description = "学院ID")
    private Long collegeId;
    
    @Schema(description = "学院名称")
    private String collegeName;
    
    @Schema(description = "专业ID")
    private Long majorId;
    
    @Schema(description = "专业名称")
    private String majorName;
    
    @Schema(description = "发布者学院ID")
    private Long publisherCollegeId;
    
    @Schema(description = "发布者学院名称")
    private String publisherCollegeName;
    
    @Schema(description = "发布者专业ID")
    private Long publisherMajorId;
    
    @Schema(description = "发布者专业名称")
    private String publisherMajorName;
    
    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "课程名称")
    private String courseName;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "分类名称")
    private String categoryName;
    
    @Schema(description = "状态，0-未解决，1-已解决")
    private Integer status;
    
    @Schema(description = "回复数量")
    private Integer replyCount;
    
    @Schema(description = "浏览数量")
    private Integer viewCount;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;
}
