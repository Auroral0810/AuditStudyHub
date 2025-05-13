package com.auditStudyHub.dto.course;

import lombok.Data;
import java.util.Date;

/**
 * 课程完整数据传输对象
 * 
 * @author auroral
 */
@Data
public class CourseDTO extends CourseBaseDTO {
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDeleted;
}
