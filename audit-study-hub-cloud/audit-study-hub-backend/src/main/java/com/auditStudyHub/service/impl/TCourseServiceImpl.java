package com.auditStudyHub.service.impl;

import com.auditStudyHub.dto.course.CourseBaseDTO;
import com.auditStudyHub.dto.course.CourseDTO;
import com.auditStudyHub.entity.TCourse;
import com.auditStudyHub.mapper.TCourseMapper;
import com.auditStudyHub.service.TCourseService;
import com.auditStudyHub.utils.RedisCacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程表(TCourse)表服务实现类
 *
 * @author auroral
 * @since 2025-05-06 11:28:07
 */
@Service("tCourseService")
public class TCourseServiceImpl extends ServiceImpl<TCourseMapper, TCourse> implements TCourseService {

    private static final Logger logger = LoggerFactory.getLogger(TCourseServiceImpl.class);
    
    // Redis缓存key前缀
    private static final String COURSE_LIST_KEY = "course:list:all";
    private static final String COURSE_DETAIL_KEY_PREFIX = "course:detail:";
    
    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public List<CourseBaseDTO> listAllCourses() {
        // 优先从Redis获取课程列表
        List<CourseBaseDTO> courseList = redisCacheUtil.getList(COURSE_LIST_KEY);
        
        if (courseList != null && !courseList.isEmpty()) {
            logger.info("从Redis缓存获取课程列表");
            return courseList;
        }
        
        // 缓存未命中，从数据库查询
        logger.info("Redis缓存未命中，从数据库查询课程列表");
        
        // 查询未删除的课程
        LambdaQueryWrapper<TCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TCourse::getIsDeleted, 0);
        
        List<TCourse> dbCourseList = this.list(queryWrapper);
        
        // 转换为DTO
        courseList = dbCourseList.stream()
                .map(this::convertToBaseDTO)
                .collect(Collectors.toList());
        
        // 更新Redis缓存
        redisCacheUtil.set(COURSE_LIST_KEY, courseList);
        
        return courseList;
    }

    @Override
    public CourseBaseDTO getCourseById(Long id) {
        if (id == null) {
            return null;
        }
        
        // 构建Redis缓存key
        String cacheKey = COURSE_DETAIL_KEY_PREFIX + id;
        
        // 优先从Redis获取
        CourseBaseDTO courseDTO = redisCacheUtil.get(cacheKey, CourseBaseDTO.class);
        
        if (courseDTO != null) {
            logger.info("从Redis缓存获取课程, id: {}", id);
            return courseDTO;
        }
        
        // 缓存未命中，从数据库查询
        logger.info("Redis缓存未命中，从数据库查询课程, id: {}", id);
        
        // 查询未删除的指定课程
        LambdaQueryWrapper<TCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TCourse::getId, id)
                   .eq(TCourse::getIsDeleted, 0);
        
        TCourse course = this.getOne(queryWrapper);
        
        // 转换为DTO
        if (course != null) {
            courseDTO = convertToBaseDTO(course);
            // 更新Redis缓存
            redisCacheUtil.set(cacheKey, courseDTO);
            return courseDTO;
        }
        
        return null;
    }

    @Override
    public List<CourseDTO> listAllCoursesForAdmin() {
        // 管理界面的数据不缓存，直接从数据库获取最新数据
        List<TCourse> courseList = this.list();
        
        // 转换为DTO
        return courseList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CourseDTO getCourseByIdForAdmin(Long id) {
        // 管理界面的数据不缓存，直接从数据库获取最新数据
        TCourse course = this.getById(id);
        
        // 转换为DTO
        return course != null ? convertToDTO(course) : null;
    }
    
    /**
     * 将课程实体转换为基础DTO
     */
    private CourseBaseDTO convertToBaseDTO(TCourse course) {
        CourseBaseDTO dto = new CourseBaseDTO();
        BeanUtils.copyProperties(course, dto);
        return dto;
    }
    
    /**
     * 将课程实体转换为完整DTO
     */
    private CourseDTO convertToDTO(TCourse course) {
        CourseDTO dto = new CourseDTO();
        BeanUtils.copyProperties(course, dto);
        return dto;
    }
    
    /**
     * 清除课程相关的所有缓存
     * 当有课程更新、删除、新增操作时调用此方法
     */
    public void clearCourseCache() {
        logger.info("清除课程缓存");
        redisCacheUtil.delete(COURSE_LIST_KEY);
    }
    
    /**
     * 清除指定课程的缓存
     */
    public void clearCourseCacheById(Long id) {
        if (id != null) {
            logger.info("清除课程缓存, id: {}", id);
            String cacheKey = COURSE_DETAIL_KEY_PREFIX + id;
            redisCacheUtil.delete(cacheKey);
            
            // 同时清除课程列表缓存，保持一致性
            redisCacheUtil.delete(COURSE_LIST_KEY);
        }
    }
}

