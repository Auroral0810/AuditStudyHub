package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 批量收藏请求
 */
@Data
@Schema(description = "批量收藏请求")
public class BatchCollectRequest {
    @Schema(description = "用户ID", required = true)
    private Long userId;

    @Schema(description = "资源ID列表", required = true)
    private List<Long> ids;
}