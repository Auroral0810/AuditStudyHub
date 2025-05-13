package com.auditStudyHub.admin.controller;

import com.auditStudyHub.admin.service.TAdminCourseService;
import com.auditStudyHub.common.Result;
import com.auditStudyHub.dto.course.CourseDTO;
import com.auditStudyHub.entity.TCourse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 课程管理控制器
 *
 * @author auroral
 * @date 2025/5/7 12:32
 */
@RestController
@RequestMapping("/admin/course")
public class AdminCourseController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminCourseController.class);
    
    @Autowired
    private TAdminCourseService adminCourseService;
    
    /**
     * 获取所有课程列表（包括已删除的）
     *
     * @return 课程列表
     */
    @GetMapping("/getCourses")
    public Result<List<CourseDTO>> getCourses() {
        logger.info("获取课程列表");
        List<CourseDTO> courses = adminCourseService.listAllCoursesForAdmin();
        return Result.success(courses);
    }
   
    /**
     * 根据ID获取课程详情（包括已删除的）
     *
     * @param id 课程ID
     * @return 课程详情
     */
    @GetMapping("/getCourse")
    public Result<CourseDTO> getCourse(@RequestParam("id") Long id) {
        logger.info("获取课程详情，ID: {}", id);
        
        if (id == null) {
            return Result.error("课程ID不能为空");
        }

        CourseDTO course = adminCourseService.getCourseByIdForAdmin(id);
        if (course == null) {
            return Result.error("课程不存在");
        }
        
        return Result.success(course);
    }
    
    /**
     * 添加课程
     */
    @PostMapping("/addCourse")
    public Result<CourseDTO> addCourse(@RequestBody CourseDTO courseDTO) {
        logger.info("添加课程: {}", courseDTO.getName());
        
        TCourse course = new TCourse();
        BeanUtils.copyProperties(courseDTO, course);
        
        // 调用服务添加课程
        CourseDTO result = adminCourseService.addCourse(course);
        
        return Result.success(result);
    }
    
    /**
     * 更新课程信息
     */
    @PutMapping("/updateCourse")
    public Result<CourseDTO> updateCourse(@RequestBody CourseDTO courseDTO) {
        logger.info("更新课程, ID: {}", courseDTO.getId());
        
        if (courseDTO.getId() == null) {
            return Result.error("课程ID不能为空");
        }
        
        // 转换DTO为实体
        TCourse course = new TCourse();
        BeanUtils.copyProperties(courseDTO, course);
        
        // 调用服务更新课程
        CourseDTO result = adminCourseService.updateCourse(course);
        
        return Result.success(result);
    }
    
    /**
     * 删除课程（逻辑删除）
     */
    @DeleteMapping("/deleteCourse")
    public Result<Boolean> deleteCourse(@RequestParam("id") Long id) {
        logger.info("删除课程, ID: {}", id);
        
        if (id == null) {
            return Result.error("课程ID不能为空");
        }
        
        // 调用服务删除课程
        boolean success = adminCourseService.deleteCourse(id);
        
        return Result.success(success);
    }
    
    /**
     * 搜索课程（支持课程名称和描述）
     */
    @GetMapping("/searchCourses")
    public Result<List<CourseDTO>> searchCourses(@RequestParam("keyword") String keyword) {
        logger.info("搜索课程，关键字: {}", keyword);
        
        List<CourseDTO> courses = adminCourseService.searchCourses(keyword);
        
        return Result.success(courses);
    }
}