package com.auditStudyHub.admin.controller;

import com.auditStudyHub.admin.service.TAdminCollegeService;
import com.auditStudyHub.common.Result;
import com.auditStudyHub.dto.college.CollegeDTO;
import com.auditStudyHub.entity.TCollege;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author auroral
 * @date 2025/5/7 12:31
 */
@RestController
@RequestMapping("/admin/college")
public class AdminCollegeController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminCollegeController.class);
    
    @Autowired
    private TAdminCollegeService adminCollegeService;
    
    /**
     * 获取所有学院列表
     *
     * @return 学院列表
     */
    @GetMapping("/list")
    public Result<List<CollegeDTO>> getColleges() {
        logger.info("获取学院列表");
        List<CollegeDTO> colleges = adminCollegeService.listAllCollegesForAdmin();
        return Result.success(colleges);
    }


    /**
     * 添加分类
     *
     * @param college 分类信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result<CollegeDTO> addCollege(@RequestBody TCollege college) {
        CollegeDTO result = adminCollegeService.addCollege(college);
        return Result.success(result);
    }

    /**
     * 更新分类
     *
     * @param college 分类信息
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<CollegeDTO> updateCollege(@RequestBody TCollege college) {
        CollegeDTO result = adminCollegeService.updateCollege(college);
        return Result.success(result);
    }


    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteCollege(@PathVariable Long id) {
        logger.info("接收到删除学院请求，ID: {}", id);
        try {
            boolean result = adminCollegeService.deleteCollege(id);
            logger.info("删除学院成功，ID: {}", id);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("删除学院失败，ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("删除学院失败：" + e.getMessage());
        }
    }

}