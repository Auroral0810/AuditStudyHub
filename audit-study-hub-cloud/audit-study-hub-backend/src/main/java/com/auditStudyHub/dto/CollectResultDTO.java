// 收藏结果DTO
package com.auditStudyHub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectResultDTO {
    /**
     * 是否已收藏
     */
    private boolean collected;
}