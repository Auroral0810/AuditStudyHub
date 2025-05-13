package com.auditStudyHub.admin.service.Impl;

import com.auditStudyHub.admin.service.TAdminCollegeService;
import com.auditStudyHub.dto.college.CollegeDTO;
import com.auditStudyHub.entity.TCollege;
import com.auditStudyHub.entity.TMajor;
import com.auditStudyHub.mapper.TCollegeMapper;
import com.auditStudyHub.mapper.TMajorMapper;
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
 * 学院表(TCollege)表服务实现类
 *
 * @author Claude
 */
@Service("tAdminCollegeService")
public class TAdminCollegeServiceImpl extends ServiceImpl<TCollegeMapper, TCollege> implements TAdminCollegeService {
    
    private static final Logger logger = LoggerFactory.getLogger(TAdminCollegeServiceImpl.class);
    
    // Redis缓存key前缀
    private static final String COLLEGE_LIST_KEY = "college:list:all";
    private static final String COLLEGE_DETAIL_KEY_PREFIX = "college:detail:";
    
    @Autowired
    private TCollegeMapper collegeMapper;
    
    @Autowired
    private TMajorMapper majorMapper;
    
    @Autowired
    private RedisCacheUtil redisCacheUtil;
    
    @Override
    public List<CollegeDTO> listAllCollegesForAdmin() {
        // 先尝试从Redis缓存获取
        List<CollegeDTO> collegeList = redisCacheUtil.getList(COLLEGE_LIST_KEY);
        
        if (collegeList != null && !collegeList.isEmpty()) {
            logger.info("从Redis缓存获取学院列表");
            return collegeList;
        }
        
        // 缓存未命中，从数据库查询（包括已删除的）
        logger.info("Redis缓存未命中，从数据库查询学院列表");
        List<TCollege> dbCollegeList = collegeMapper.selectAllWithDeleted();
        
        // 转换为DTO
        collegeList = dbCollegeList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 存入Redis缓存
        redisCacheUtil.set(COLLEGE_LIST_KEY, collegeList);
        
        return collegeList;
    }

    @Override
    public CollegeDTO updateCollege(TCollege college) {
        // 设置更新时间
        college.setUpdateTime(new Date());
        // 执行更新
        collegeMapper.updateWithDeleted(college);
        // 获取最新数据
        TCollege updated = collegeMapper.selectByIdAll(college.getId());

        // 清除Redis缓存
        clearCollegeCacheById(college.getId());
        
        // 返回DTO
        return convertToDTO(updated);
    }

    @Override
    public boolean deleteCollege(Long id) {
        // 软删除，更新is_deleted字段
        TCollege college = collegeMapper.selectByIdAll(id);
        if (college != null) {
            // 1. 更新学院的删除状态
            college.setIsDeleted(1);
            college.setUpdateTime(new Date());
            boolean result = collegeMapper.updateWithDeleted(college);
            
            if (result) {
                // 2. 级联删除该学院下的所有专业
                cascadeDeleteMajors(id);
            }
            
            // 3. 清除Redis缓存
            clearCollegeCacheById(id);
            
            return result;
        }
        return false;
    }

    @Override
    public CollegeDTO addCollege(TCollege college) {
        // 设置创建和更新时间
        Date now = new Date();
        college.setCreateTime(now);
        college.setUpdateTime(now);
        college.setIsDeleted(0); // 默认未删除

        // 保存到数据库
        this.save(college);

        // 清除列表缓存
        clearCollegeCache();
        
        // 返回DTO
        return convertToDTO(college);
    }

    /**
     * 将学院实体转换为完整DTO
     */
    private CollegeDTO convertToDTO(TCollege college) {
        CollegeDTO dto = new CollegeDTO();
        BeanUtils.copyProperties(college, dto);
        return dto;
    }
    
    /**
     * 清除学院相关的所有缓存
     */
    private void clearCollegeCache() {
        logger.info("清除学院列表缓存");
        redisCacheUtil.delete(COLLEGE_LIST_KEY);
    }
    
    /**
     * 清除指定学院的缓存
     */
    private void clearCollegeCacheById(Long id) {
        if (id != null) {
            logger.info("清除学院缓存, id: {}", id);
            // 清除详情缓存
            String cacheKey = COLLEGE_DETAIL_KEY_PREFIX + id;
            redisCacheUtil.delete(cacheKey);
            
            // 同时清除列表缓存，保持一致性
            clearCollegeCache();
        }
    }
    
    /**
     * 级联删除学院下的所有专业
     */
    private void cascadeDeleteMajors(Long collegeId) {
        logger.info("级联删除学院(ID:{})下的所有专业", collegeId);
        
        // 1. 查询该学院下的所有专业（包括已删除的）
        List<TMajor> majors = majorMapper.selectByCollegeIdWithDeleted(collegeId);
        
        if (!majors.isEmpty()) {
            Date now = new Date();
            
            // 2. 遍历并更新所有专业的删除状态
            for (TMajor major : majors) {
                major.setIsDeleted(1);  // 设置删除标记
                major.setUpdateTime(now);
                majorMapper.updateWithDeleted(major);
                
                // 清除专业相关缓存 (在每次更新后清除缓存是为了防止缓存过期导致的数据不一致)
                clearMajorCacheById(major.getId());
            }
            
            // 3. 清除相关缓存
            clearMajorCache();
            clearMajorCacheByCollegeId(collegeId);
        }
    }
    
    /**
     * 清除专业相关缓存的辅助方法
     */
    private void clearMajorCache() {
        logger.info("清除专业列表缓存");
        redisCacheUtil.delete("major:list:all");
    }
    
    private void clearMajorCacheById(Long id) {
        if (id != null) {
            logger.info("清除专业缓存, id: {}", id);
            String cacheKey = "major:detail:" + id;
            redisCacheUtil.delete(cacheKey);
        }
    }
    
    private void clearMajorCacheByCollegeId(Long collegeId) {
        if (collegeId != null) {
            logger.info("清除学院专业列表缓存, collegeId: {}", collegeId);
            String cacheKey = "major:college:" + collegeId;
            redisCacheUtil.delete(cacheKey);
        }
    }
}

