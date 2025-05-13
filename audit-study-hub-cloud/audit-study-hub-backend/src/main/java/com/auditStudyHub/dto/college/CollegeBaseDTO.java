package com.auditStudyHub.dto.college;

import lombok.Data;

/**
 * 学院基础数据传输对象(用于用户界面展示)
 * 
 * @author Claude
 */
@Data
public class CollegeBaseDTO {
    /**
     * 学院ID
     */
    private Long id;
    
    /**
     * 学院名称
     */
    private String name;
    
    /**
     * 学院封面图片URL
     */
    private String coverUrl;
    
    /**
     * 学院logo图片URL
     */
    private String logoUrl;
} 