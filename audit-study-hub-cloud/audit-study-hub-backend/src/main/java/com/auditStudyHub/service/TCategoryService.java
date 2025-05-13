package com.auditStudyHub.service;

import com.auditStudyHub.dto.category.CategoryBaseDTO;
import com.auditStudyHub.entity.TCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * 资源分类表(TCategory)表服务接口
 *
 * @author Auroral
 * @since 2025-05-06 11:26:01
 */
public interface TCategoryService extends IService<TCategory> {

    List<CategoryBaseDTO> listAllCategories();
}

