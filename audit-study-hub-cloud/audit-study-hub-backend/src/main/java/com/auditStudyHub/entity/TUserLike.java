package com.auditStudyHub.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户点赞表(TUserLike)表实体类
 *
 * @author Auroral
 * @since 2025-05-06 11:29:04
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_user_like")
public class TUserLike {
    //点赞ID@TableId
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //用户ID
    private Long userId;
    //资源ID
    private Long resourceId;
    //创建时间
    private Date createTime;
    //是否删除，0-未删除，1-已删除
    private Integer isDeleted;


}

