package com.auditStudyHub.service;

import com.auditStudyHub.dto.college.CollegeBaseDTO;
import com.auditStudyHub.dto.college.CollegeDTO;
import com.auditStudyHub.entity.TCollege;
import com.auditStudyHub.entity.THotSearch;
import com.auditStudyHub.entity.TPopularSearch;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 学院表(TCollege)表服务接口
 *
 * @author Auroral
 * @since 2025-05-06 11:27:56
 */
public interface TCollegeService extends IService<TCollege> {
    
    /**
     * 获取所有学院列表(用户界面使用)
     * 
     * @return 学院基础信息列表
     */
    List<CollegeBaseDTO> listAllColleges();
    
    /**
     * 根据ID获取学院详情(用户界面使用)
     * 
     * @param id 学院ID
     * @return 学院基础信息
     */
    CollegeBaseDTO getCollegeById(Long id);
    
    /**
     * 获取所有学院列表(管理界面使用)
     * 
     * @return 学院完整信息列表
     */
    List<CollegeDTO> listAllCollegesForAdmin();
    
    /**
     * 根据ID获取学院详情(管理界面使用)
     * 
     * @param id 学院ID
     * @return 学院完整信息
     */
    CollegeDTO getCollegeByIdForAdmin(Long id);


}

