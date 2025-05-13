package com.auditStudyHub.admin.service;

import com.auditStudyHub.dto.course.CourseDTO;
import com.auditStudyHub.entity.TCourse;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 课程表(TCourse)表服务接口
 *
 * @author auroral
 */
public interface TAdminCourseService extends IService<TCourse> {

    /**
     * 获取管理员用的所有课程列表（包括已删除的）
     * 
     * @return 课程列表
     */
    List<CourseDTO> listAllCoursesForAdmin();
    
    /**
     * 管理员根据ID获取课程（包括已删除的）
     * 
     * @param id 课程ID
     * @return 课程完整信息
     */
    CourseDTO getCourseByIdForAdmin(Long id);
    
    /**
     * 添加课程
     * 
     * @param course 课程信息
     * @return 添加后的课程
     */
    CourseDTO addCourse(TCourse course);
    
    /**
     * 更新课程
     * 
     * @param course 课程信息
     * @return 更新后的课程
     */
    CourseDTO updateCourse(TCourse course);
    
    /**
     * 删除课程（逻辑删除）
     * 
     * @param id 课程ID
     * @return 是否删除成功
     */
    boolean deleteCourse(Long id);
    
    /**
     * 关键词搜索课程
     * 
     * @param keyword 关键词
     * @return 搜索结果
     */
    List<CourseDTO> searchCourses(String keyword);
}

