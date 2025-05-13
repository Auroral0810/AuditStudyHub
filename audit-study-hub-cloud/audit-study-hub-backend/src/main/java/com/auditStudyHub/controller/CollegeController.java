package com.auditStudyHub.controller;

import com.auditStudyHub.common.Result;
import com.auditStudyHub.dto.college.CollegeBaseDTO;
import com.auditStudyHub.service.TCollegeService;
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
@RequestMapping("/college")
public class CollegeController {
    
    private static final Logger logger = LoggerFactory.getLogger(CollegeController.class);
    
    @Autowired
    private TCollegeService collegeService;
    
    /**
     * 获取所有学院列表
     *
     * @return 学院列表
     */
    @GetMapping("/getColleges")
    public Result<List<CollegeBaseDTO>> getColleges() {
        logger.info("获取学院列表");
        List<CollegeBaseDTO> colleges = collegeService.listAllColleges();
        return Result.success(colleges);
    }
    
    /**
     * 根据ID获取学院详情
     *
     * @param id 学院ID
     * @return 学院详情
     */
    @GetMapping("/getCollege")
    public Result<CollegeBaseDTO> getCollege(@RequestParam("id") Long id) {
        logger.info("获取学院详情，ID: {}", id);
        
        if (id == null) {
            return Result.error("学院ID不能为空");
        }
        
        CollegeBaseDTO college = collegeService.getCollegeById(id);
        if (college == null) {
            return Result.error("学院不存在");
        }
        
        return Result.success(college);
    }
}