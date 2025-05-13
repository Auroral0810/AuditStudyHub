package com.auditStudyHub.admin.service.Impl;

import com.auditStudyHub.admin.service.TAdminCourseService;
import com.auditStudyHub.dto.course.CourseDTO;
import com.auditStudyHub.entity.TCourse;
import com.auditStudyHub.mapper.TCourseMapper;
import com.auditStudyHub.utils.RedisCacheUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程表(TCourse)表服务实现类
 *
 * @author auroral
 * @since 2025-05-06 11:28:07
 */
@Service("tAdminCourseService")
public class TAdminCourseServiceImpl extends ServiceImpl<TCourseMapper, TCourse> implements TAdminCourseService {

    private static final Logger logger = LoggerFactory.getLogger(TAdminCourseServiceImpl.class);
    
    // Redis缓存key前缀
    private static final String COURSE_LIST_KEY = "course:list:all";
    private static final String COURSE_ADMIN_LIST_KEY = "course:admin:list:all";
    private static final String COURSE_DETAIL_KEY_PREFIX = "course:detail:";
    private static final String COURSE_ADMIN_DETAIL_KEY_PREFIX = "course:admin:detail:";
    private static final String COURSE_SEARCH_KEY_PREFIX = "course:search:";
    
    @Autowired
    private RedisCacheUtil redisCacheUtil;
    
    @Autowired
    private TCourseMapper courseMapper;

    @Override
    public List<CourseDTO> listAllCoursesForAdmin() {
        // 先尝试从Redis缓存获取
        List<CourseDTO> courseList = redisCacheUtil.getList(COURSE_ADMIN_LIST_KEY);
        
        if (courseList != null && !courseList.isEmpty()) {
            logger.info("从Redis缓存获取管理员课程列表");
            return courseList;
        }
        
        // 缓存未命中，从数据库获取所有课程（包括已删除的）
        logger.info("Redis缓存未命中，从数据库查询管理员课程列表");
        List<TCourse> dbCourseList = courseMapper.selectAllWithDeleted();
        
        // 转换为DTO
        courseList = dbCourseList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 存入Redis缓存
        redisCacheUtil.set(COURSE_ADMIN_LIST_KEY, courseList);
        
        return courseList;
    }

    @Override
    public CourseDTO getCourseByIdForAdmin(Long id) {
        if (id == null) {
            return null;
        }
        
        // 构建Redis缓存key
        String cacheKey = COURSE_ADMIN_DETAIL_KEY_PREFIX + id;
        
        // 先尝试从Redis缓存获取
        CourseDTO courseDTO = redisCacheUtil.get(cacheKey, CourseDTO.class);
        
        if (courseDTO != null) {
            logger.info("从Redis缓存获取管理员课程详情, id: {}", id);
            return courseDTO;
        }
        
        // 缓存未命中，从数据库查询
        logger.info("Redis缓存未命中，从数据库查询管理员课程详情, id: {}", id);
        TCourse course = courseMapper.selectByIdWithDeleted(id);
        
        if (course != null) {
            courseDTO = convertToDTO(course);
            // 存入Redis缓存
            redisCacheUtil.set(cacheKey, courseDTO);
            return courseDTO;
        }
        
        return null;
    }
    
    @Override
    public CourseDTO addCourse(TCourse course) {
        // 设置创建和更新时间
        Date now = new Date();
        course.setCreateTime(now);
        course.setUpdateTime(now);
        course.setIsDeleted(0); // 默认未删除
        
        // 保存到数据库
        this.save(course);
        
        // 清除Redis缓存，保持数据一致性
        clearCourseCache();
        clearAdminCourseCache();
        
        // 返回DTO
        return convertToDTO(course);
    }
    
    @Override
    public CourseDTO updateCourse(TCourse course) {
        // 设置更新时间
        course.setUpdateTime(new Date());
        
        // 执行更新，绕过逻辑删除
        courseMapper.updateWithDeleted(course);
        
        // 获取最新数据
        TCourse updated = courseMapper.selectByIdWithDeleted(course.getId());
        
        // 清除Redis缓存，保持数据一致性
        clearCourseCacheById(course.getId());
        clearAdminCourseCacheById(course.getId());
        clearCourseCache();
        clearAdminCourseCache();
        
        // 返回DTO
        return convertToDTO(updated);
    }
    
    @Override
    public boolean deleteCourse(Long id) {
        // 软删除，更新is_deleted字段
        TCourse course = courseMapper.selectByIdWithDeleted(id);
        if (course != null) {
            course.setIsDeleted(1);
            course.setUpdateTime(new Date());
            boolean result = courseMapper.updateWithDeleted(course);
            
            // 清除Redis缓存，保持数据一致性
            clearCourseCacheById(id);
            clearAdminCourseCacheById(id);
            clearCourseCache();
            clearAdminCourseCache();
            
            return result;
        }
        return false;
    }
    
    @Override
    public List<CourseDTO> searchCourses(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listAllCoursesForAdmin();
        }
        
        // 构建Redis缓存key
        String cacheKey = COURSE_SEARCH_KEY_PREFIX + keyword;
        
        // 先尝试从Redis缓存获取
        List<CourseDTO> courseList = redisCacheUtil.getList(cacheKey);
        
        if (courseList != null && !courseList.isEmpty()) {
            logger.info("从Redis缓存获取课程搜索结果, keyword: {}", keyword);
            return courseList;
        }
        
        // 缓存未命中，从数据库搜索课程
        logger.info("Redis缓存未命中，从数据库搜索课程, keyword: {}", keyword);
        List<TCourse> dbCourseList = courseMapper.searchWithDeleted(keyword);
        
        // 转换为DTO
        courseList = dbCourseList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 存入Redis缓存（搜索结果缓存时间短一些）
        redisCacheUtil.set(cacheKey, courseList, 3600); // 缓存1小时
        
        return courseList;
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
     * 清除用户端课程相关的所有缓存
     */
    public void clearCourseCache() {
        logger.info("清除用户端课程缓存");
        redisCacheUtil.delete(COURSE_LIST_KEY);
    }
    
    /**
     * 清除管理员端课程相关的所有缓存
     */
    public void clearAdminCourseCache() {
        logger.info("清除管理员端课程缓存");
        redisCacheUtil.delete(COURSE_ADMIN_LIST_KEY);
    }
    
    /**
     * 清除指定用户端课程的缓存
     */
    public void clearCourseCacheById(Long id) {
        if (id != null) {
            logger.info("清除用户端课程缓存, id: {}", id);
            String cacheKey = COURSE_DETAIL_KEY_PREFIX + id;
            redisCacheUtil.delete(cacheKey);
        }
    }
    
    /**
     * 清除指定管理员端课程的缓存
     */
    public void clearAdminCourseCacheById(Long id) {
        if (id != null) {
            logger.info("清除管理员端课程缓存, id: {}", id);
            String cacheKey = COURSE_ADMIN_DETAIL_KEY_PREFIX + id;
            redisCacheUtil.delete(cacheKey);
        }
    }
}

