package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 记录用户下载历史
 */
@Data
@Schema(description = "用户下载记录请求")
public class UserDownloadRequest {
    @Schema(description = "资源ID", required = true)
    private Long resourceId;

    @Schema(description = "用户ID", required = true)
    private Long userId;
}