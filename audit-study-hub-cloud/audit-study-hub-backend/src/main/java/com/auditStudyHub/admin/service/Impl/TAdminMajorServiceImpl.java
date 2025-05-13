package com.auditStudyHub.admin.service.Impl;

import com.auditStudyHub.admin.service.TAdminMajorService;
import com.auditStudyHub.dto.major.MajorDTO;
import com.auditStudyHub.entity.TCollege;
import com.auditStudyHub.entity.TMajor;
import com.auditStudyHub.mapper.TMajorMapper;
import com.auditStudyHub.service.TCollegeService;
import com.auditStudyHub.utils.RedisCacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.auditStudyHub.exception.EntityRelationException;

/**
 * 专业表(TMajor)表服务实现类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:15
 */
@Service("tAdminMajorService")
public class TAdminMajorServiceImpl extends ServiceImpl<TMajorMapper, TMajor> implements TAdminMajorService {

    private static final Logger logger = LoggerFactory.getLogger(TAdminMajorServiceImpl.class);
    
    // Redis缓存key前缀
    private static final String MAJOR_LIST_KEY = "major:list:all";
    private static final String MAJOR_DETAIL_KEY_PREFIX = "major:detail:";
    private static final String MAJOR_COLLEGE_KEY_PREFIX = "major:college:";
    
    @Autowired
    private TCollegeService collegeService;
    
    @Autowired
    private TMajorMapper majorMapper;
    
    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public List<MajorDTO> listAllMajorsForAdmin() {
        // 先尝试从Redis缓存获取
        List<MajorDTO> majorList = redisCacheUtil.getList(MAJOR_LIST_KEY);
        
        if (majorList != null && !majorList.isEmpty()) {
            logger.info("从Redis缓存获取专业列表");
            return majorList;
        }
        
        // 缓存未命中，从数据库查询
        logger.info("Redis缓存未命中，从数据库查询专业列表");
        List<TMajor> dbMajorList = majorMapper.selectAllWithDeleted();
        
        // 获取所有相关学院
        Map<Long, String> collegeMap = getCollegeMap(dbMajorList);
        
        // 转换为DTO
        majorList = dbMajorList.stream()
                .map(major -> convertToDTO(major, collegeMap.get(major.getCollegeId())))
                .collect(Collectors.toList());
        
        // 存入Redis缓存
        redisCacheUtil.set(MAJOR_LIST_KEY, majorList);
        
        return majorList;
    }

    @Override
    public MajorDTO getMajorByIdForAdmin(Long id) {
        if (id == null) {
            return null;
        }
        
        // 构建Redis缓存key
        String cacheKey = MAJOR_DETAIL_KEY_PREFIX + id;
        
        // 先尝试从Redis缓存获取
        MajorDTO majorDTO = redisCacheUtil.get(cacheKey, MajorDTO.class);
        
        if (majorDTO != null) {
            logger.info("从Redis缓存获取专业, id: {}", id);
            return majorDTO;
        }
        
        // 缓存未命中，从数据库查询
        logger.info("Redis缓存未命中，从数据库查询专业, id: {}", id);
        TMajor major = majorMapper.selectByIdAll(id);
        
        if (major == null) {
            return null;
        }
        
        // 获取学院名称
        TCollege college = collegeService.getById(major.getCollegeId());
        String collegeName = college != null ? college.getName() : "";
        
        // 转换为DTO
        majorDTO = convertToDTO(major, collegeName);
        
        // 存入Redis缓存
        redisCacheUtil.set(cacheKey, majorDTO);
        
        return majorDTO;
    }

    @Override
    public List<MajorDTO> listMajorsByCollegeIdForAdmin(Long collegeId) {
        if (collegeId == null) {
            return List.of();
        }
        
        // 构建Redis缓存key
        String cacheKey = MAJOR_COLLEGE_KEY_PREFIX + collegeId;
        
        // 先尝试从Redis缓存获取
        List<MajorDTO> majorList = redisCacheUtil.getList(cacheKey);
        
        if (majorList != null && !majorList.isEmpty()) {
            logger.info("从Redis缓存获取学院专业列表, collegeId: {}", collegeId);
            return majorList;
        }
        
        // 缓存未命中，从数据库查询
        logger.info("Redis缓存未命中，从数据库查询学院专业列表, collegeId: {}", collegeId);
        List<TMajor> dbMajorList = majorMapper.selectByCollegeIdWithDeleted(collegeId);
        
        // 获取学院名称
        TCollege college = collegeService.getById(collegeId);
        String collegeName = college != null ? college.getName() : "";
        
        // 转换为DTO
        majorList = dbMajorList.stream()
                .map(major -> convertToDTO(major, collegeName))
                .collect(Collectors.toList());
        
        // 存入Redis缓存
        redisCacheUtil.set(cacheKey, majorList);
        
        return majorList;
    }

    /**
     * 获取专业列表相关的学院映射（ID -> 名称）
     */
    private Map<Long, String> getCollegeMap(List<TMajor> majorList) {
        // 获取所有涉及的学院ID
        List<Long> collegeIds = majorList.stream()
                .map(TMajor::getCollegeId)
                .distinct()
                .collect(Collectors.toList());
        
        if (collegeIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // 批量查询学院
        List<TCollege> colleges = collegeService.listByIds(collegeIds);
        
        // 构建映射
        return colleges.stream()
                .collect(Collectors.toMap(TCollege::getId, TCollege::getName, (v1, v2) -> v1));
    }

    @Override
    public MajorDTO addMajor(TMajor major) {
        // 验证所属学院是否有效
        TCollege college = collegeService.getById(major.getCollegeId());
        if (college == null) {
            throw new EntityRelationException("所选学院不存在");
        }
        
        // 如果学院已被删除但尝试添加正常状态的专业，则抛出异常
        if (college.getIsDeleted() == 1 && major.getIsDeleted() == 0) {
            throw new EntityRelationException("所选学院已被删除，无法添加正常状态的专业");
        }

        // 设置创建和更新时间
        Date now = new Date();
        major.setCreateTime(now);
        major.setUpdateTime(now);
        
        // 如果学院已删除，则强制将专业也设为删除状态
        if (college.getIsDeleted() == 1) {
            major.setIsDeleted(1);
        }

        // 保存到数据库
        this.save(major);

        // 获取学院名称
        String collegeName = college.getName();

        // 清除缓存
        clearMajorCache();
        clearMajorCacheByCollegeId(major.getCollegeId());
        
        // 返回DTO
        return convertToDTO(major, collegeName);
    }

    @Override
    public MajorDTO updateMajor(TMajor major) {
        // 如果尝试将专业状态设为正常，需要检查学院状态
        if (major.getIsDeleted() != null && major.getIsDeleted() == 0) {
            // 获取学院信息
            TCollege college = collegeService.getById(major.getCollegeId());
            
            // 验证学院状态
            if (college == null || college.getIsDeleted() == 1) {
                throw new EntityRelationException("该专业对应的学院已被删除，无法将专业设为正常状态");
            }
        }
        
        // 获取原始数据（用于获取原来的学院ID，以便清除相关缓存）
        TMajor original = majorMapper.selectByIdAll(major.getId());
        Long originalCollegeId = original != null ? original.getCollegeId() : null;
        
        // 设置更新时间
        major.setUpdateTime(new Date());
        
        // 执行更新，绕过逻辑删除
        majorMapper.updateWithDeleted(major);
        
        // 获取最新数据
        TMajor updated = majorMapper.selectByIdAll(major.getId());
        
        // 获取学院名称
        TCollege college = collegeService.getById(updated.getCollegeId());
        String collegeName = college != null ? college.getName() : "";
        
        // 清除缓存
        clearMajorCacheById(major.getId());
        clearMajorCache();
        
        // 如果学院ID发生变化，清除原学院的专业列表缓存
        if (originalCollegeId != null && !originalCollegeId.equals(updated.getCollegeId())) {
            clearMajorCacheByCollegeId(originalCollegeId);
        }
        
        // 清除更新后学院的专业列表缓存
        clearMajorCacheByCollegeId(updated.getCollegeId());
        
        // 返回DTO
        return convertToDTO(updated, collegeName);
    }

    @Override
    public boolean deleteMajor(Long id) {
        // 获取专业信息
        TMajor major = majorMapper.selectByIdAll(id);
        if (major != null) {
            // 记录学院ID，用于清除相关缓存
            Long collegeId = major.getCollegeId();
            
            // 软删除，更新is_deleted字段
            major.setIsDeleted(1);
            major.setUpdateTime(new Date());
            boolean result = majorMapper.updateWithDeleted(major);
            
            // 清除缓存
            clearMajorCacheById(id);
            clearMajorCache();
            clearMajorCacheByCollegeId(collegeId);
            
            return result;
        }
        return false;
    }

    /**
     * 将专业实体转换为完整DTO
     */
    private MajorDTO convertToDTO(TMajor major, String collegeName) {
        MajorDTO dto = new MajorDTO();
        BeanUtils.copyProperties(major, dto);
        dto.setCollegeName(collegeName);
        return dto;
    }
    
    /**
     * 清除所有专业缓存
     */
    private void clearMajorCache() {
        logger.info("清除专业列表缓存");
        redisCacheUtil.delete(MAJOR_LIST_KEY);
    }
    
    /**
     * 清除指定专业的缓存
     */
    private void clearMajorCacheById(Long id) {
        if (id != null) {
            logger.info("清除专业缓存, id: {}", id);
            String cacheKey = MAJOR_DETAIL_KEY_PREFIX + id;
            redisCacheUtil.delete(cacheKey);
        }
    }
    
    /**
     * 清除指定学院下的专业列表缓存
     */
    private void clearMajorCacheByCollegeId(Long collegeId) {
        if (collegeId != null) {
            logger.info("清除学院专业列表缓存, collegeId: {}", collegeId);
            String cacheKey = MAJOR_COLLEGE_KEY_PREFIX + collegeId;
            redisCacheUtil.delete(cacheKey);
        }
    }
}

