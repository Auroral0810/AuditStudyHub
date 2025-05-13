package com.auditStudyHub.service.impl;

import com.auditStudyHub.dto.category.CategoryBaseDTO;
import com.auditStudyHub.dto.college.CollegeBaseDTO;
import com.auditStudyHub.entity.TCategory;
import com.auditStudyHub.entity.TCollege;
import com.auditStudyHub.mapper.TCategoryMapper;
import com.auditStudyHub.service.TCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源分类表(TCategory)表服务实现类
 *
 * @author Auroral
 * @since 2025-05-06 11:26:02
 */
@Service("tCategoryService")
public class TCategoryServiceImpl extends ServiceImpl<TCategoryMapper, TCategory> implements TCategoryService {

    @Override
    public List<CategoryBaseDTO> listAllCategories() {
        // 查询未删除的分类
        LambdaQueryWrapper<TCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TCategory::getIsDeleted, 0);

        List<TCategory> categoryList = this.list(queryWrapper);

        // 转换为DTO
        return categoryList.stream()
                .map(this::convertToBaseDTO)
                .collect(Collectors.toList());
    }
    /**
     * 将分类实体转换为基础DTO
     */
    private CategoryBaseDTO convertToBaseDTO(TCategory category) {
        CategoryBaseDTO dto = new CategoryBaseDTO();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }

}

