package com.auditStudyHub.controller;

import com.auditStudyHub.common.Result;
import com.auditStudyHub.dto.category.CategoryBaseDTO;
import com.auditStudyHub.dto.college.CollegeBaseDTO;
import com.auditStudyHub.service.TCategoryService;
import com.auditStudyHub.service.TCollegeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author auroral
 * @date 2025/5/7 12:32
 */
@RestController
@RequestMapping("/category")
public class CategoryController {
    private static final Logger logger = LoggerFactory.getLogger(CollegeController.class);

    @Autowired
    private TCategoryService categoryService;

    /**
     * 获取所有分类列表
     *
     * @return 分类列表
     */
    @GetMapping("/getCategories")
    public Result<List<CategoryBaseDTO>> getCategoies() {
        List<CategoryBaseDTO> categories = categoryService.listAllCategories();
        return Result.success(categories);
    }
}