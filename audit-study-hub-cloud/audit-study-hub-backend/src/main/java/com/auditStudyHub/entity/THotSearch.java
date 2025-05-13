package com.auditStudyHub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 搜索记录表(THotSearch)表实体类
 *
 * @author makejava
 * @since 2025-05-07 21:29:02
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_hot_search")
public class THotSearch {
    //搜索记录ID
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //搜索关键词
    private String keyword;
    //搜索次数
    private Integer searchCount;
    //最近搜索时间
    private Date lastSearchTime;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //是否删除，0-未删除，1-已删除
    private Integer isDeleted;


}

