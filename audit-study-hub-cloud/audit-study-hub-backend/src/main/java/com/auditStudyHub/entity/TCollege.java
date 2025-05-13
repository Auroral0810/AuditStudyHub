package com.auditStudyHub.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 学院表(TCollege)表实体类
 *
 * @author Auroral
 * @since 2025-05-06 11:27:56
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_college")
public class TCollege {
    //学院ID@TableId
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //学院名称
    private String name;
    //学院封面
    private String coverUrl;
    //学院logo
    private String logoUrl;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //是否删除，0-未删除，1-已删除
    private Integer isDeleted;


}

