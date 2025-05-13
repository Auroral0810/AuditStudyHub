package com.auditStudyHub.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 资源请求回复表(TResourceReply)表实体类
 *
 * @author Auroral
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_resource_reply")
public class TResourceReply {
    
    // 回复ID
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    // 请求内容
    private String content;
    
    // 用户ID
    private Long userId;
    
    // 回复内容
    private String replyContent;
    
    // 关联资源ID
    private Long resourceId;
    
    // 父回复ID
    private Long parentId;
    
    // 创建时间
    private Date createTime;
    
    // 回复时间
    private Date replyTime;
    
    // 是否删除，0-未删除，1-已删除
    private Integer isDeleted;
} 