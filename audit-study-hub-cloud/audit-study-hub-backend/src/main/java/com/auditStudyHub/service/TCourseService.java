package com.auditStudyHub.service;

import com.auditStudyHub.dto.course.CourseBaseDTO;
import com.auditStudyHub.dto.course.CourseDTO;
import com.auditStudyHub.entity.TCourse;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 课程表(TCourse)表服务接口
 *
 * @author auroral
 */
public interface TCourseService extends IService<TCourse> {
    
    /**
     * 获取所有课程列表
     * 
     * @return 课程列表
     */
    List<CourseBaseDTO> listAllCourses();
    
    /**
     * 根据ID获取课程
     * 
     * @param id 课程ID
     * @return 课程信息
     */
    CourseBaseDTO getCourseById(Long id);
    
    /**
     * 获取管理员用的所有课程列表
     * 
     * @return 课程列表
     */
    List<CourseDTO> listAllCoursesForAdmin();
    
    /**
     * 管理员根据ID获取课程
     * 
     * @param id 课程ID
     * @return 课程完整信息
     */
    CourseDTO getCourseByIdForAdmin(Long id);
}

