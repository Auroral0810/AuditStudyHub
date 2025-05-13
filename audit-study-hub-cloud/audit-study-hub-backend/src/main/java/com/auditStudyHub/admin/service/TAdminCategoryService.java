package com.auditStudyHub.admin.service;

import com.auditStudyHub.dto.category.CategoryDTO;
import com.auditStudyHub.entity.TCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 分类表(TCategory)表服务接口
 *
 * @author Auroral
 */
public interface TAdminCategoryService extends IService<TCategory> {

    /**
     * 获取所有分类(包括已删除的)
     *
     * @return 分类列表
     */
    List<CategoryDTO> listAdminCategories();
    
    /**
     * 根据ID获取分类详情(包括已删除的)
     *
     * @param id 分类ID
     * @return 分类详情
     */
    CategoryDTO getCategoryById(Long id);

    /**
     * 添加分类
     *
     * @param category 分类信息
     * @return 添加结果
     */
    CategoryDTO addCategory(TCategory category);

    /**
     * 更新分类
     *
     * @param category 分类信息
     * @return 更新结果
     */
    CategoryDTO updateCategory(TCategory category);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 删除结果
     */
    boolean deleteCategory(Long id);
    
    /**
     * 搜索分类
     *
     * @param keyword 关键词
     * @return 搜索结果
     */
    List<CategoryDTO> searchCategories(String keyword);
}

