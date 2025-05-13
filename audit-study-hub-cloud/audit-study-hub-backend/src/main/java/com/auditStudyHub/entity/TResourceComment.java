package com.auditStudyHub.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 资源评论表(TResourceComment)表实体类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:26
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_resource_comment")
public class TResourceComment {
    //评论ID@TableId
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //资源ID
    private Long resourceId;
    //用户ID
    private Long userId;
    //评论内容
    private String content;
    //父评论ID
    private Long parentId;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //是否删除，0-未删除，1-已删除
    private Integer isDeleted;


}

