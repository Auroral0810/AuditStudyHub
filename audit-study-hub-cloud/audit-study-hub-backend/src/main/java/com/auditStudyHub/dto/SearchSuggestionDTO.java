package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索建议DTO
 * @author auroral
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "搜索建议数据传输对象")
public class SearchSuggestionDTO {
    
    @Schema(description = "搜索建议关键词")
    private String keyword;
    
    @Schema(description = "搜索建议对应的资源ID（可选）")
    private Long resourceId;
    
    @Schema(description = "搜索建议对应的资源标题（可选）")
    private String title;
    
    @Schema(description = "搜索建议对应的分类ID（可选）")
    private Long categoryId;
    
    @Schema(description = "搜索建议对应的分类名称（可选）")
    private String categoryName;
    
    @Schema(description = "匹配度或热度分值")
    private Float score;
} 