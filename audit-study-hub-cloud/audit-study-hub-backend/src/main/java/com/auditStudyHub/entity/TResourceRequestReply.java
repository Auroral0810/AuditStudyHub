package com.auditStudyHub.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 资源求助回复表(TResourceRequestReply)表实体类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:39
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_resource_request_reply")
public class TResourceRequestReply {
    //回复ID@TableId
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //求助ID
    private Long requestId;
    //用户ID
    private Long userId;
    //回复内容
    private String content;
    //回复父ID
    private Long parentId;
    //关联资源ID
    private Long resourceId;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //是否删除，0-未删除，1-已删除
    private Integer isDeleted;


}

