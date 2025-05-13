package com.auditStudyHub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 资源分类表(TCategory)表实体类
 *
 * @author Auroral
 * @since 2025-05-06 11:25:58
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_category")
public class TCategory {
    //分类ID@TableId
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //分类名称
    private String name;
    //分类图标
    private String icon;
    //排序
    private Integer sort;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //是否删除，0-未删除，1-已删除
    private Integer isDeleted;


}

