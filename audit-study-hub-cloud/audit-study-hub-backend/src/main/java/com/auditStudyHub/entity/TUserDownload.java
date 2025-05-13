package com.auditStudyHub.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户下载表(TUserDownload)表实体类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:56
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_user_download")
public class TUserDownload {
    //下载ID@TableId
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //用户ID
    private Long userId;
    //资源ID
    private Long resourceId;
    //创建时间
    private Date createTime;


}

