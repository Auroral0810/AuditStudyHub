package com.auditStudyHub.dto.major;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

/**
 * 专业完整数据传输对象(用于管理界面展示)
 * 继承自MajorBaseDTO，扩展了管理界面需要的字段
 * 
 * @author Claude
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MajorDTO extends MajorBaseDTO {
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