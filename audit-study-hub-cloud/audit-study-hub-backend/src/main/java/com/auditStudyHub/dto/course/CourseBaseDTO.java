package com.auditStudyHub.dto.course;

import lombok.Data;

/**
 * 课程基础数据传输对象(用于用户界面展示)
 * 
 * @author auroral
 */
@Data
public class CourseBaseDTO {
    /**
     * 课程ID
     */
    private Long id;
    
    /**
     * 课程名称
     */
    private String name;
    
    /**
     * 课程学分
     */
    private Double credit;
    
    /**
     * 课程描述
     */
    private String description;
}
