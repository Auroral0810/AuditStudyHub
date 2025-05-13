package com.auditStudyHub.admin.service;

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
public interface TAdminMajorService extends IService<TMajor> {

    /**
     * 获取所有专业列表（包括已删除的）
     *
     * @return 专业列表
     */
    List<MajorDTO> listAllMajorsForAdmin();

    /**
     * 获取指定专业详情（包括已删除的）
     *
     * @param id 专业ID
     * @return 专业详情
     */
    MajorDTO getMajorByIdForAdmin(Long id);

    /**
     * 获取指定学院下的专业列表（包括已删除的）
     *
     * @param collegeId 学院ID
     * @return 专业列表
     */
    List<MajorDTO> listMajorsByCollegeIdForAdmin(Long collegeId);

    /**
     * 添加专业
     *
     * @param major 专业信息
     * @return 添加结果
     */
    MajorDTO addMajor(TMajor major);

    /**
     * 更新专业
     *
     * @param major 专业信息
     * @return 更新结果
     */
    MajorDTO updateMajor(TMajor major);

    /**
     * 删除专业
     *
     * @param id 专业ID
     * @return 删除结果
     */
    boolean deleteMajor(Long id);
}

