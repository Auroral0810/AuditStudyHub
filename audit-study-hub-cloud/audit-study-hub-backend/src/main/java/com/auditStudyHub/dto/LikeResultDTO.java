// 点赞结果DTO
package com.auditStudyHub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeResultDTO {
    /**
     * 是否已点赞
     */
    private boolean liked;
    
    /**
     * 资源总点赞数
     */
    private int likeCount;
}