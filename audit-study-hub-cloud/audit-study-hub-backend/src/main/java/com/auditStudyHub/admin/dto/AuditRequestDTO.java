package com.auditStudyHub.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "资源审核请求")
public class AuditRequestDTO {
    
    @Schema(description = "资源ID列表")
    private List<Long> resourceIds;
    
    @Schema(description = "审核结果: 1-通过, 2-拒绝")
    private Integer auditResult;
} 