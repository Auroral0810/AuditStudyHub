package com.auditStudyHub.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 课程表(TCourse)表实体类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:07
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_course")
public class TCourse {
    //课程ID@TableId
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //课程名称
    private String name;
    //学分
    private Double credit;
    //课程描述
    private String description;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //是否删除，0-未删除，1-已删除
    private Integer isDeleted;


}

