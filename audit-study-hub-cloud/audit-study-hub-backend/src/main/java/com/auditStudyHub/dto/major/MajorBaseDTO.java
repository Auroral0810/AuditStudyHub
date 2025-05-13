package com.auditStudyHub.dto.major;

import lombok.Data;

/**
 * 学院基础数据传输对象(用于用户界面展示)
 * 
 * @author Claude
 */
@Data
public class MajorBaseDTO {
    //专业ID@TableId
    private Long id;
    //专业名称
    private String name;
    //所属学院ID
    private Long collegeId;

    //所属学院名称
    private String collegeName;
    //学位类型
    private String degree;
    //学历等级
    private Integer xl;
    //学制
    private String xz;

} 