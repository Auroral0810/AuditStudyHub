package com.auditStudyHub.dto.category;

import lombok.Data;
import java.util.Date;

/**
 * 管理员端分类DTO，包含所有字段
 */
@Data
public class CategoryAdminDTO {
    // 分类ID
    private Long id;
    // 分类名称
    private String name;
    // 分类图标
    private String icon;
    // 排序
    private Integer sort;
    // 创建时间
    private Date createTime;
    // 更新时间
    private Date updateTime;
    // 是否删除，0-未删除，1-已删除
    private Integer isDeleted;
} 