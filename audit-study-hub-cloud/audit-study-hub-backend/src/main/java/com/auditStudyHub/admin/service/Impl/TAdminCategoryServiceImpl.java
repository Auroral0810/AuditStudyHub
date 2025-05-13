package com.auditStudyHub.admin.service.Impl;

import com.auditStudyHub.dto.category.CategoryDTO;
import com.auditStudyHub.dto.category.CategoryBaseDTO;
import com.auditStudyHub.entity.TCategory;
import com.auditStudyHub.entity.TCollege;
import com.auditStudyHub.mapper.TCategoryMapper;
import com.auditStudyHub.admin.service.TAdminCategoryService;
import com.auditStudyHub.utils.RedisCacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
 * 资源分类表(TCategory)表服务实现类
 *
 * @author Auroral
 * @since 2025-05-06 11:26:02
 */
@Service("tAdminCategoryService")
public class TAdminCategoryServiceImpl extends ServiceImpl<TCategoryMapper, TCategory> implements TAdminCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(TAdminCategoryServiceImpl.class);
    
    // Redis缓存key前缀
    private static final String CATEGORY_LIST_KEY = "category:list:all";
    private static final String CATEGORY_DETAIL_KEY_PREFIX = "category:detail:";
    private static final String CATEGORY_SEARCH_KEY_PREFIX = "category:search:";

    @Autowired
    private TCategoryMapper categoryMapper;
    
    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public List<CategoryDTO> listAdminCategories() {
        // 先尝试从Redis缓存获取
        List<CategoryDTO> categoryList = redisCacheUtil.getList(CATEGORY_LIST_KEY);
        
        if (categoryList != null && !categoryList.isEmpty()) {
            logger.info("从Redis缓存获取分类列表");
            return categoryList;
        }
        
        // 缓存未命中，从数据库查询
        logger.info("Redis缓存未命中，从数据库查询分类列表");
        // 查询所有分类（包括已删除的）
        List<TCategory> dbCategoryList = categoryMapper.selectAllWithDeleted();

        // 转换为管理员DTO
        categoryList = dbCategoryList.stream()
                .map(this::convertToAdminDTO)
                .collect(Collectors.toList());
        
        // 存入Redis缓存
        redisCacheUtil.set(CATEGORY_LIST_KEY, categoryList);
        
        return categoryList;
    }
    
    @Override
    public CategoryDTO getCategoryById(Long id) {
        if (id == null) {
            return null;
        }
        
        // 构建Redis缓存key
        String cacheKey = CATEGORY_DETAIL_KEY_PREFIX + id;
        
        // 先尝试从Redis缓存获取
        CategoryDTO categoryDTO = redisCacheUtil.get(cacheKey, CategoryDTO.class);
        
        if (categoryDTO != null) {
            logger.info("从Redis缓存获取分类, id: {}", id);
            return categoryDTO;
        }
        
        // 缓存未命中，从数据库查询
        logger.info("Redis缓存未命中，从数据库查询分类, id: {}", id);
        TCategory category = categoryMapper.selectByIdAll(id);
        
        if (category != null) {
            categoryDTO = convertToAdminDTO(category);
            // 存入Redis缓存
            redisCacheUtil.set(cacheKey, categoryDTO);
            return categoryDTO;
        }
        
        return null;
    }
    
    @Override
    public CategoryDTO addCategory(TCategory category) {
        // 设置创建和更新时间
        Date now = new Date();
        category.setCreateTime(now);
        category.setUpdateTime(now);
        category.setIsDeleted(0); // 默认未删除
        
        // 保存到数据库
        this.save(category);
        
        // 清除缓存
        clearCategoryCache();
        
        // 返回DTO
        return convertToAdminDTO(category);
    }

    @Override
    public CategoryDTO updateCategory(TCategory category) {
        // 设置更新时间
        category.setUpdateTime(new Date());
        // 执行更新
        categoryMapper.updateWithDeleted(category);

        // 获取最新数据
        TCategory updated = categoryMapper.selectByIdAll(category.getId());

        // 清除缓存
        clearCategoryCacheById(category.getId());
        clearCategoryCache();
        
        // 返回 DTO
        return convertToAdminDTO(updated);
    }
    
    @Override
    public boolean deleteCategory(Long id) {
        // 软删除，更新is_deleted字段
        TCategory category = categoryMapper.selectByIdAll(id);
        if (category != null) {
            category.setIsDeleted(1);
            category.setUpdateTime(new Date());
            boolean result = categoryMapper.updateWithDeleted(category);
            
            // 清除缓存
            clearCategoryCacheById(id);
            clearCategoryCache();
            
            return result;
        }
        return false;
    }
    
    @Override
    public List<CategoryDTO> searchCategories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listAdminCategories();
        }
        
        // 构建Redis缓存key
        String cacheKey = CATEGORY_SEARCH_KEY_PREFIX + keyword;
        
        // 先尝试从Redis缓存获取
        List<CategoryDTO> categoryList = redisCacheUtil.getList(cacheKey);
        
        if (categoryList != null && !categoryList.isEmpty()) {
            logger.info("从Redis缓存获取分类搜索结果, keyword: {}", keyword);
            return categoryList;
        }
        
        // 缓存未命中，从数据库搜索
        logger.info("Redis缓存未命中，从数据库搜索分类, keyword: {}", keyword);
        List<TCategory> dbCategoryList = categoryMapper.searchWithDeleted(keyword);
        
        // 转换为DTO
        categoryList = dbCategoryList.stream()
                .map(this::convertToAdminDTO)
                .collect(Collectors.toList());
        
        // 存入Redis缓存（搜索结果缓存时间短一些）
        redisCacheUtil.set(cacheKey, categoryList, 3600); // 缓存1小时
        
        return categoryList;
    }
    
    /**
     * 将分类实体转换为管理员DTO
     */
    private CategoryDTO convertToAdminDTO(TCategory category) {
        CategoryDTO dto = new CategoryDTO();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }
    
    /**
     * 清除分类列表缓存
     */
    private void clearCategoryCache() {
        logger.info("清除分类列表缓存");
        redisCacheUtil.delete(CATEGORY_LIST_KEY);
    }
    
    /**
     * 清除指定分类的缓存
     */
    private void clearCategoryCacheById(Long id) {
        if (id != null) {
            logger.info("清除分类缓存, id: {}", id);
            String cacheKey = CATEGORY_DETAIL_KEY_PREFIX + id;
            redisCacheUtil.delete(cacheKey);
        }
    }
}

