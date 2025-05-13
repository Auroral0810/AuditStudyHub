package com.auditStudyHub.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 用户表(TUser)表实体类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:44
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_user")
public class TUser {
    //用户ID@TableId
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //用户名
    private String username;
    //真实姓名
    private String realName;
    //学号
    private String studentId;
    //密码
    private String password;
    //手机号
    private String phone;
    //邮箱
    private String email;
    //头像URL
    private String avatar;
    //学院ID
    private Long collegeId;
    //学院名称
    private String collegeName;
    //专业ID
    private Long majorId;
    //专业名称
    private String majorName;
    //班级ID
    //角色，0-普通用户，1-管理员
    private Integer role;
    //状态，0-禁用，1-启用
    private Integer status;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //是否删除，0-未删除，1-已删除
    private Integer isDeleted;


}

