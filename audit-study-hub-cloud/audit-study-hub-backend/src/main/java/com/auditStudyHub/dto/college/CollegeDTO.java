package com.auditStudyHub.dto.college;

import lombok.Data;
import java.util.Date;

/**
 * 学院数据传输对象
 * 
 * @author Claude
 */
@Data
public class CollegeDTO extends CollegeBaseDTO {
    
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