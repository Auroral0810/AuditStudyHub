package com.auditStudyHub.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
import com.auditStudyHub.listener.ResourceEntityListener;

import jakarta.persistence.EntityListeners;

/**
 * 资源表(TResource)表实体类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:20
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_resource")
@EntityListeners(ResourceEntityListener.class)
public class TResource {
    //资源ID@TableId
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //资源标题
    private String title;
    //资源描述
    private String description;
    //文件URL
    private String fileUrl;
    //文件类型
    private String fileType;
    //文件大小（字节）
    private Long fileSize;
    //下载次数
    private Integer downloadCount;
    //浏览次数
    private Integer viewCount;
    //点赞次数
    private Integer likeCount;
    //上传者ID
    private Long uploaderId;
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
    //标签，多个标签用逗号分隔
    private String tags;
    //状态，0-待审核，1-已通过，2-已拒绝
    private Integer status;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //是否删除，0-未删除，1-已删除
    private Integer isDeleted;


}

