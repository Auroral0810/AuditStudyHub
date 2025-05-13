package com.auditStudyHub.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员资源请求搜索请求")
public class AdminRequestSearchRequest {
    
    @Schema(description = "当前页码")
    private Integer page = 1;
    
    @Schema(description = "每页数量")
    private Integer size = 10;
    
    @Schema(description = "请求标题")
    private String title;
    
    @Schema(description = "请求内容")
    private String content;
    
    @Schema(description = "用户名称")
    private String userName;
    
    @Schema(description = "学院ID")
    private Long collegeId;
    
    @Schema(description = "专业ID")
    private Long majorId;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "请求状态")
    private Integer status;
    
    @Schema(description = "是否删除")
    private Integer isDeleted;
    
    @Schema(description = "开始日期")
    private String startDate;
    
    @Schema(description = "结束日期")
    private String endDate;
} 