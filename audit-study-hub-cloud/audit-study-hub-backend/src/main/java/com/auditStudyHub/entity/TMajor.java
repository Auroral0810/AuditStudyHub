package com.auditStudyHub.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 专业表(TMajor)表实体类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:15
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_major")
public class TMajor {
    //专业ID@TableId
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //专业名称
    private String name;
    //所属学院ID
    private Long collegeId;

    //学位类型
    private String degree;
    //学历等级
    private Integer xl;
    //学制
    private String xz;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //是否删除，0-未删除，1-已删除
    private Integer isDeleted;


}

