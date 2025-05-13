package com.auditStudyHub.controller;

import com.auditStudyHub.common.Result;
import com.auditStudyHub.dto.course.CourseBaseDTO;
import com.auditStudyHub.dto.course.CourseDTO;
import com.auditStudyHub.entity.TCourse;
import com.auditStudyHub.service.TCourseService;
import com.auditStudyHub.service.impl.TCourseServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author auroral
 * @date 2025/5/7 12:32
 */
@RestController
@RequestMapping("/course")
public class CourseController {
    
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    
    @Autowired
    private TCourseService courseService;
    
    @Autowired
    private TCourseServiceImpl courseServiceImpl;
    
    /**
     * 获取所有课程列表
     *
     * @return 课程列表
     */
    @GetMapping("/getCourses")
    public Result<List<CourseBaseDTO>> getCourses() {
        logger.info("获取课程列表");
        List<CourseBaseDTO> courses = courseService.listAllCourses();
        return Result.success(courses);
    }
   
    /**
     * 根据ID获取课程详情
     *
     * @param id 课程ID
     * @return 课程详情
     */
    @GetMapping("/getCourse")
    public Result<CourseBaseDTO> getCourse(@RequestParam("id") Long id) {
        logger.info("获取课程详情，ID: {}", id);
        
        if (id == null) {
            return Result.error("课程ID不能为空");
        }
        
        CourseBaseDTO course = courseService.getCourseById(id);
        if (course == null) {
            return Result.error("课程不存在");
        }
        
        return Result.success(course);
    }
    
    /**
     * 添加课程（管理员）
     */
    @PostMapping("/addCourse")
    public Result<Void> addCourse(@RequestBody CourseBaseDTO courseDTO) {
        logger.info("添加课程: {}", courseDTO.getName());
        
        TCourse course = new TCourse();
        BeanUtils.copyProperties(courseDTO, course);
        
        // 设置创建时间和更新时间
        Date now = new Date();
        course.setCreateTime(now);
        course.setUpdateTime(now);
        course.setIsDeleted(0);
        
        boolean success = courseService.save(course);
        
        if (success) {
            // 清除课程列表缓存
            courseServiceImpl.clearCourseCache();
            return Result.success();
        } else {
            return Result.error("添加课程失败");
        }
    }
    
    /**
     * 更新课程信息（管理员）
     */
    @PutMapping("/updateCourse")
    public Result<Void> updateCourse(@RequestBody CourseBaseDTO courseDTO) {
        logger.info("更新课程, ID: {}", courseDTO.getId());
        
        if (courseDTO.getId() == null) {
            return Result.error("课程ID不能为空");
        }
        
        // 查询课程是否存在
        TCourse existingCourse = courseService.getById(courseDTO.getId());
        if (existingCourse == null) {
            return Result.error("课程不存在");
        }
        
        // 更新课程信息
        BeanUtils.copyProperties(courseDTO, existingCourse);
        existingCourse.setUpdateTime(new Date());
        
        boolean success = courseService.updateById(existingCourse);
        
        if (success) {
            // 清除该课程的缓存
            courseServiceImpl.clearCourseCacheById(courseDTO.getId());
            return Result.success();
        } else {
            return Result.error("更新课程失败");
        }
    }
    
    /**
     * 删除课程（管理员）
     */
    @DeleteMapping("/deleteCourse")
    public Result<Void> deleteCourse(@RequestParam("id") Long id) {
        logger.info("删除课程, ID: {}", id);
        
        if (id == null) {
            return Result.error("课程ID不能为空");
        }
        
        // 查询课程是否存在
        TCourse course = courseService.getById(id);
        if (course == null) {
            return Result.error("课程不存在");
        }
        
        // 逻辑删除课程
        course.setIsDeleted(1);
        course.setUpdateTime(new Date());
        
        boolean success = courseService.updateById(course);
        
        if (success) {
            // 清除该课程的缓存
            courseServiceImpl.clearCourseCacheById(id);
            return Result.success();
        } else {
            return Result.error("删除课程失败");
        }
    }
    
    /**
     * 搜索课程（支持课程名称和描述）
     */
    @GetMapping("/searchCourses")
    public Result<List<CourseBaseDTO>> searchCourses(@RequestParam("keyword") String keyword) {
        logger.info("搜索课程，关键字: {}", keyword);
        
        // 从缓存获取所有课程
        List<CourseBaseDTO> allCourses = courseService.listAllCourses();
        
        // 在内存中过滤符合条件的课程
        List<CourseBaseDTO> filteredCourses = allCourses.stream()
                .filter(course -> 
                    (course.getName() != null && course.getName().contains(keyword)) || 
                    (course.getDescription() != null && course.getDescription().contains(keyword)))
                .collect(Collectors.toList());
        
        return Result.success(filteredCourses);
    }
}