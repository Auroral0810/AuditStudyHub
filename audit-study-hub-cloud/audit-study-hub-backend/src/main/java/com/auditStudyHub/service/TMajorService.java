package com.auditStudyHub.service;

import com.auditStudyHub.dto.major.MajorBaseDTO;
import com.auditStudyHub.dto.major.MajorDTO;
import com.auditStudyHub.entity.TMajor;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 专业表(TMajor)表服务接口
 *
 * @author Auroral
 * @since 2025-05-06 11:28:15
 */
public interface TMajorService extends IService<TMajor> {
    
    /**
     * 获取所有专业列表(用户界面使用)
     * 
     * @return 专业基础信息列表
     */
    List<MajorBaseDTO> listAllMajors();
    
    /**
     * 根据ID获取专业详情(用户界面使用)
     * 
     * @param id 专业ID
     * @return 专业基础信息
     */
    MajorBaseDTO getMajorById(Long id);
    
    /**
     * 根据学院ID获取专业列表(用户界面使用)
     * 
     * @param collegeId 学院ID
     * @return 专业基础信息列表
     */
    List<MajorBaseDTO> listMajorsByCollegeId(Long collegeId);
    
    /**
     * 获取所有专业列表(管理界面使用)
     * 
     * @return 专业完整信息列表
     */
    List<MajorDTO> listAllMajorsForAdmin();
    
    /**
     * 根据ID获取专业详情(管理界面使用)
     * 
     * @param id 专业ID
     * @return 专业完整信息
     */
    MajorDTO getMajorByIdForAdmin(Long id);
    
    /**
     * 根据学院ID获取专业列表(管理界面使用)
     * 
     * @param collegeId 学院ID
     * @return 专业完整信息列表
     */
    List<MajorDTO> listMajorsByCollegeIdForAdmin(Long collegeId);
}

