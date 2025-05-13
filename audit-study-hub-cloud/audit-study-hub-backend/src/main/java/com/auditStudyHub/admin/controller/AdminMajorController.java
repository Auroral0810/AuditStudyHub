package com.auditStudyHub.admin.controller;

import com.auditStudyHub.admin.service.TAdminMajorService;
import com.auditStudyHub.common.Result;
import com.auditStudyHub.dto.college.CollegeDTO;
import com.auditStudyHub.dto.major.MajorDTO;
import com.auditStudyHub.entity.TCollege;
import com.auditStudyHub.entity.TMajor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author auroral
 * @date 2025/5/7 12:31
 */
@RestController
@RequestMapping("/admin/major")
public class AdminMajorController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminMajorController.class);
    
    @Autowired
    private TAdminMajorService adminMajorService;
    
    /**
     * 获取所有专业列表（包括已删除的）
     *
     * @return 专业列表
     */
    @GetMapping("/list")
    public Result<List<MajorDTO>> getMajors() {
        logger.info("获取专业列表");
        List<MajorDTO> majors = adminMajorService.listAllMajorsForAdmin();
        return Result.success(majors);
    }
    
    /**
     * 获取指定ID的专业详情
     *
     * @param id 专业ID
     * @return 专业详情
     */
    @GetMapping("/getMajor")
    public Result<MajorDTO> getMajorById(@RequestParam Long id) {
        logger.info("获取专业详情，ID: {}", id);
        MajorDTO major = adminMajorService.getMajorByIdForAdmin(id);
        return Result.success(major);
    }
    
    /**
     * 根据学院ID获取专业列表
     *
     * @param collegeId 学院ID
     * @return 专业列表
     */
    @GetMapping("/getMajorsByCollege")
    public Result<List<MajorDTO>> getMajorsByCollegeId(@RequestParam Long collegeId) {
        logger.info("获取学院下的专业列表，学院ID: {}", collegeId);
        List<MajorDTO> majors = adminMajorService.listMajorsByCollegeIdForAdmin(collegeId);
        return Result.success(majors);
    }
    
    /**
     * 添加专业
     *
     * @param major 专业信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result<MajorDTO> addMajor(@RequestBody TMajor major) {
        logger.info("添加专业: {}", major.getName());
        MajorDTO result = adminMajorService.addMajor(major);
        return Result.success(result);
    }
    
    /**
     * 更新专业
     *
     * @param major 专业信息
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<MajorDTO> updateMajor(@RequestBody TMajor major) {
        logger.info("更新专业，ID: {}", major.getId());
        MajorDTO result = adminMajorService.updateMajor(major);
        return Result.success(result);
    }
    
    /**
     * 删除专业
     *
     * @param id 专业ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteMajor(@PathVariable Long id) {
        logger.info("删除专业，ID: {}", id);
        boolean result = adminMajorService.deleteMajor(id);
        return Result.success(result);
    }
}