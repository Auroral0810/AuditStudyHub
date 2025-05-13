package com.auditStudyHub.service.impl;

import com.auditStudyHub.dto.major.MajorBaseDTO;
import com.auditStudyHub.dto.major.MajorDTO;
import com.auditStudyHub.entity.TCollege;
import com.auditStudyHub.entity.TMajor;
import com.auditStudyHub.mapper.TCollegeMapper;
import com.auditStudyHub.mapper.TMajorMapper;
import com.auditStudyHub.service.TCollegeService;
import com.auditStudyHub.service.TMajorService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专业表(TMajor)表服务实现类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:15
 */
@Service("tMajorService")
public class TMajorServiceImpl extends ServiceImpl<TMajorMapper, TMajor> implements TMajorService {

    @Autowired
    private TCollegeService collegeService;

    @Override
    public List<MajorBaseDTO> listAllMajors() {
        // 查询未删除的专业
        LambdaQueryWrapper<TMajor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TMajor::getIsDeleted, 0);
        
        List<TMajor> majorList = this.list(queryWrapper);
        
        // 获取所有相关学院
        Map<Long, String> collegeMap = getCollegeMap(majorList);
        
        // 转换为DTO
        return majorList.stream()
                .map(major -> convertToBaseDTO(major, collegeMap.get(major.getCollegeId())))
                .collect(Collectors.toList());
    }

    @Override
    public MajorBaseDTO getMajorById(Long id) {
        // 查询未删除的指定专业
        LambdaQueryWrapper<TMajor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TMajor::getId, id)
                   .eq(TMajor::getIsDeleted, 0);
        
        TMajor major = this.getOne(queryWrapper);
        if (major == null) {
            return null;
        }
        
        // 获取学院名称
        TCollege college = collegeService.getById(major.getCollegeId());
        String collegeName = college != null ? college.getName() : "";
        
        // 转换为DTO
        return convertToBaseDTO(major, collegeName);
    }

    @Override
    public List<MajorBaseDTO> listMajorsByCollegeId(Long collegeId) {
        // 查询未删除的指定学院的专业
        LambdaQueryWrapper<TMajor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TMajor::getCollegeId, collegeId)
                   .eq(TMajor::getIsDeleted, 0);
        
        List<TMajor> majorList = this.list(queryWrapper);
        
        // 获取学院名称
        TCollege college = collegeService.getById(collegeId);
        String collegeName = college != null ? college.getName() : "";
        
        // 转换为DTO
        return majorList.stream()
                .map(major -> convertToBaseDTO(major, collegeName))
                .collect(Collectors.toList());
    }

    @Override
    public List<MajorDTO> listAllMajorsForAdmin() {
        // 管理界面可以看到所有专业（包括已删除的）
        List<TMajor> majorList = this.list();
        
        // 获取所有相关学院
        Map<Long, String> collegeMap = getCollegeMap(majorList);
        
        // 转换为DTO
        return majorList.stream()
                .map(major -> convertToDTO(major, collegeMap.get(major.getCollegeId())))
                .collect(Collectors.toList());
    }

    @Override
    public MajorDTO getMajorByIdForAdmin(Long id) {
        // 管理界面可以看到指定专业（包括已删除的）
        TMajor major = this.getById(id);
        if (major == null) {
            return null;
        }
        
        // 获取学院名称
        TCollege college = collegeService.getById(major.getCollegeId());
        String collegeName = college != null ? college.getName() : "";
        
        // 转换为DTO
        return convertToDTO(major, collegeName);
    }

    @Override
    public List<MajorDTO> listMajorsByCollegeIdForAdmin(Long collegeId) {
        // 管理界面可以看到指定学院的专业（包括已删除的）
        LambdaQueryWrapper<TMajor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TMajor::getCollegeId, collegeId);
        
        List<TMajor> majorList = this.list(queryWrapper);
        
        // 获取学院名称
        TCollege college = collegeService.getById(collegeId);
        String collegeName = college != null ? college.getName() : "";
        
        // 转换为DTO
        return majorList.stream()
                .map(major -> convertToDTO(major, collegeName))
                .collect(Collectors.toList());
    }
    
    /**
     * 将专业实体转换为基础DTO
     */
    private MajorBaseDTO convertToBaseDTO(TMajor major, String collegeName) {
        MajorBaseDTO dto = new MajorBaseDTO();
        BeanUtils.copyProperties(major, dto);
        dto.setCollegeName(collegeName);
        return dto;
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
                .collect(Collectors.toMap(TCollege::getId, TCollege::getName));
    }
}

