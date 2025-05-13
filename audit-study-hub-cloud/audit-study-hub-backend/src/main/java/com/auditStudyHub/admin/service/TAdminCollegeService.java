package com.auditStudyHub.admin.service;

import com.auditStudyHub.dto.college.CollegeDTO;
import com.auditStudyHub.entity.TCollege;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 学院表(TCollege)表服务接口
 *
 * @author Auroral
 * @since 2025-05-06 11:27:56
 */
public interface TAdminCollegeService extends IService<TCollege> {
    
    /**
     * 获取所有学院列表(管理界面使用)
     * 
     * @return 学院完整信息列表
     */
    List<CollegeDTO> listAllCollegesForAdmin();

    CollegeDTO updateCollege(TCollege college);

    boolean deleteCollege(Long id);

    CollegeDTO addCollege(TCollege college);
}

