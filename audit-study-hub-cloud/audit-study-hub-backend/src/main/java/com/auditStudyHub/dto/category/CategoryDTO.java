package com.auditStudyHub.dto.category;

import lombok.Data;
import java.util.Date;

/**
 *
 * @author Claude
 */
@Data
public class CategoryDTO extends CategoryBaseDTO{
    
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