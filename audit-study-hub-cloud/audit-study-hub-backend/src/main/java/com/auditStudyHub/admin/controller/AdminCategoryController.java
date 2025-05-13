package com.auditStudyHub.admin.controller;

import com.auditStudyHub.admin.service.TAdminCategoryService;
import com.auditStudyHub.common.Result;
import com.auditStudyHub.controller.CategoryController;
import com.auditStudyHub.dto.category.CategoryDTO;
import com.auditStudyHub.dto.category.CategoryBaseDTO;
import com.auditStudyHub.entity.TCategory;
import com.auditStudyHub.admin.service.TAdminCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author auroral
 * @date 2025/5/7 12:32
 */
@RestController
@RequestMapping("/admin/category")
public class AdminCategoryController {
    private static final Logger logger = LoggerFactory.getLogger(AdminCategoryController.class);

    @Autowired
    private TAdminCategoryService categoryService;

    
    /**
     * 管理员获取分类列表（包含所有字段）
     *
     * @return 完整分类列表
     */
    @GetMapping("/list")
    public Result<List<CategoryDTO>> getAdminCategoryList() {
        List<CategoryDTO> categories = categoryService.listAdminCategories();
        return Result.success(categories);
    }
    
    /**
     * 添加分类
     *
     * @param category 分类信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result<CategoryDTO> addCategory(@RequestBody TCategory category) {
        CategoryDTO result = categoryService.addCategory(category);
        return Result.success(result);
    }
    
    /**
     * 更新分类
     *
     * @param category 分类信息
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<CategoryDTO> updateCategory(@RequestBody TCategory category) {
        CategoryDTO result = categoryService.updateCategory(category);
        return Result.success(result);
    }
    
    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteCategory(@PathVariable Long id) {
        boolean result = categoryService.deleteCategory(id);
        return Result.success(result);
    }
}