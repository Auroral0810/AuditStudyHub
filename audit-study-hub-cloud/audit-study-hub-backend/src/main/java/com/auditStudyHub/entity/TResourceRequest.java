package com.auditStudyHub.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 资源求助表(TResourceRequest)表实体类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:34
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_resource_request")
public class TResourceRequest {
    //求助ID@TableId
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //求助标题
    private String title;
    //求助内容
    private String content;
    //用户ID
    private Long userId;
    //学院ID
    private Long collegeId;
    //学院名称
    private String collegeName;
    //专业ID
    private Long majorId;
    //专业名称
    private String majorName;
    //课程ID
    private Long courseId;
    //课程名称
    private String courseName;
    //分类ID
    private Long categoryId;
    //分类名称
    private String categoryName;
    //状态，0-未解决，1-已解决
    private Integer status;
    //回复数量
    private Integer replyCount;
    //浏览数量
    private Integer viewCount;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //是否删除，0-未删除，1-已删除
    private Integer isDeleted;


}

