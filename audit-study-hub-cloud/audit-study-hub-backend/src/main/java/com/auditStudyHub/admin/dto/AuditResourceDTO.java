package com.auditStudyHub.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "审核资源DTO")
public class AuditResourceDTO {
    
    @Schema(description = "资源ID")
    private Long id;
    
    @Schema(description = "资源标题")
    private String title;
    
    @Schema(description = "资源分类名称")
    private String categoryName;
    
    @Schema(description = "文件类型")
    private String fileType;
    
    @Schema(description = "所属学院")
    private String collegeName;
    
    @Schema(description = "状态，0-待审核，1-已通过，2-已拒绝")
    private Integer status;
    
    @Schema(description = "上传时间")
    private Date createTime;
}
