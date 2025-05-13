package com.auditStudyHub.dto.category;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 *
 * @author Auroral
 * @since 2025-05-06 11:25:58
 */
@SuppressWarnings("serial")
@Data
public class CategoryBaseDTO {
    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图标url
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

}

