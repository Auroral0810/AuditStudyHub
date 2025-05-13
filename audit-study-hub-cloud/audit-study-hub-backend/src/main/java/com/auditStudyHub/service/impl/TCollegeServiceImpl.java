package com.auditStudyHub.service.impl;

import com.auditStudyHub.dto.college.CollegeBaseDTO;
import com.auditStudyHub.dto.college.CollegeDTO;
import com.auditStudyHub.entity.TCollege;
import com.auditStudyHub.mapper.TCollegeMapper;
import com.auditStudyHub.service.TCollegeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学院表(TCollege)表服务实现类
 *
 * @author Claude
 */
@Service("tCollegeService")
public class TCollegeServiceImpl extends ServiceImpl<TCollegeMapper, TCollege> implements TCollegeService {

    @Override
    public List<CollegeBaseDTO> listAllColleges() {
        // 查询未删除的学院
        LambdaQueryWrapper<TCollege> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TCollege::getIsDeleted, 0);
        
        List<TCollege> collegeList = this.list(queryWrapper);
        
        // 转换为DTO
        return collegeList.stream()
                .map(this::convertToBaseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CollegeBaseDTO getCollegeById(Long id) {
        // 查询未删除的指定学院
        LambdaQueryWrapper<TCollege> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TCollege::getId, id)
                   .eq(TCollege::getIsDeleted, 0);
        
        TCollege college = this.getOne(queryWrapper);
        
        // 转换为DTO
        return college != null ? convertToBaseDTO(college) : null;
    }

    @Override
    public List<CollegeDTO> listAllCollegesForAdmin() {
        // 管理界面可以看到所有学院（包括已删除的）
        List<TCollege> collegeList = this.list();
        
        // 转换为DTO
        return collegeList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CollegeDTO getCollegeByIdForAdmin(Long id) {
        // 管理界面可以看到指定学院（包括已删除的）
        TCollege college = this.getById(id);
        
        // 转换为DTO
        return college != null ? convertToDTO(college) : null;
    }
    
    /**
     * 将学院实体转换为基础DTO
     */
    private CollegeBaseDTO convertToBaseDTO(TCollege college) {
        CollegeBaseDTO dto = new CollegeBaseDTO();
        BeanUtils.copyProperties(college, dto);
        return dto;
    }
    
    /**
     * 将学院实体转换为完整DTO
     */
    private CollegeDTO convertToDTO(TCollege college) {
        CollegeDTO dto = new CollegeDTO();
        BeanUtils.copyProperties(college, dto);
        return dto;
    }
}

