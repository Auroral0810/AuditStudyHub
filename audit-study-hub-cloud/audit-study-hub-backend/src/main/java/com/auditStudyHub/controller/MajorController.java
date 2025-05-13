package com.auditStudyHub.controller;

import com.auditStudyHub.common.Result;
import com.auditStudyHub.dto.major.MajorBaseDTO;
import com.auditStudyHub.service.TMajorService;
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
@RequestMapping("/major")
public class MajorController {
    
    private static final Logger logger = LoggerFactory.getLogger(MajorController.class);
    
    @Autowired
    private TMajorService majorService;
    
    /**
     * 获取所有专业列表
     *
     * @return 专业列表
     */
    @GetMapping("/getMajors")
    public Result<List<MajorBaseDTO>> getMajors() {
        logger.info("获取专业列表");
        List<MajorBaseDTO> majors = majorService.listAllMajors();
        return Result.success(majors);
    }
    
    /**
     * 根据ID获取专业详情
     *
     * @param id 专业ID
     * @return 专业详情
     */
    @GetMapping("/getMajor")
    public Result<MajorBaseDTO> getMajor(@RequestParam("id") Long id) {
        logger.info("获取专业详情，ID: {}", id);
        
        if (id == null) {
            return Result.error("专业ID不能为空");
        }
        
        MajorBaseDTO major = majorService.getMajorById(id);
        if (major == null) {
            return Result.error("专业不存在");
        }
        
        return Result.success(major);
    }
    
    /**
     * 根据学院ID获取专业列表
     *
     * @param collegeId 学院ID
     * @return 专业列表
     */
    @GetMapping("/getMajorsByCollege")
    public Result<List<MajorBaseDTO>> getMajorsByCollege(@RequestParam("collegeId") Long collegeId) {
        logger.info("根据学院ID获取专业列表，学院ID: {}", collegeId);
        
        if (collegeId == null) {
            return Result.error("学院ID不能为空");
        }
        
        List<MajorBaseDTO> majors = majorService.listMajorsByCollegeId(collegeId);
        return Result.success(majors);
    }
}