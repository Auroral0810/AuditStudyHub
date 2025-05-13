package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户交互请求")
public class UserInteractionRequest {
    @Schema(description = "用户ID", required = true)
    private Long userId;

    @Schema(description = "资源ID", required = true)
    private Long resourceId;
}