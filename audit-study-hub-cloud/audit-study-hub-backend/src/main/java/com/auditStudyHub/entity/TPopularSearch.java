package com.auditStudyHub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 热门搜索词表(TPopularSearch)表实体类
 *
 * @author makejava
 * @since 2025-05-07 21:27:45
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_popular_search")
public class TPopularSearch {
    //热门搜索ID@TableId
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //热门关键词
    private String keyword;
    //排序权重
    @TableField(value = "`rank`")
    private Integer rank;
    //统计权重
    private Integer countWeight;
    //是否手动添加，0-自动统计，1-手动添加
    private Integer isManual;
    //生效开始时间
    private Date startTime;
    //生效结束时间
    private Date endTime;
    //状态，0-下线，1-上线
    private Integer status;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //是否删除，0-未删除，1-已删除
    private Integer isDeleted;


}

