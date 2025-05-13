package com.auditStudyHub.admin.controller;

import com.auditStudyHub.admin.dto.AdminCommentDTO;
import com.auditStudyHub.admin.dto.AdminCommentSearchRequest;
import com.auditStudyHub.admin.service.AdminCommentService;
import com.auditStudyHub.common.Result;
import com.auditStudyHub.dto.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/comment")
@RequiredArgsConstructor
@Tag(name = "管理员评论接口", description = "提供评论管理功能")
public class AdminCommentController {

    private final AdminCommentService adminCommentService;
    
    @GetMapping("/list")
    @Operation(summary = "获取所有评论列表")
    public Result<List<AdminCommentDTO>> getAllComments() {
        log.info("请求获取所有评论列表");
        List<AdminCommentDTO> comments = adminCommentService.getAllComments();
        return Result.success(comments);
    }
    
    @GetMapping("/detail")
    @Operation(summary = "获取评论详情")
    public Result<AdminCommentDTO> getCommentDetail(@RequestParam Long id) {
        log.info("请求获取评论详情: id={}", id);
        AdminCommentDTO comment = adminCommentService.getCommentById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        return Result.success(comment);
    }
    
    @PostMapping("/search")
    @Operation(summary = "搜索评论列表")
    public Result<PageResult<AdminCommentDTO>> searchComments(@RequestBody AdminCommentSearchRequest request) {
        log.info("搜索评论: {}", request);
        PageResult<AdminCommentDTO> result = adminCommentService.searchComments(request);
        
        // 确保返回数据中的日期格式正确
        if (result != null && result.getList() != null) {
            // 日期格式已经由Jackson自动处理
        }
        
        return Result.success(result);
    }
    
    @PutMapping("/update-status")
    @Operation(summary = "更新评论状态（逻辑删除或恢复）")
    public Result<Boolean> updateCommentStatus(@RequestParam Long id, @RequestParam Integer isDeleted) {
        log.info("更新评论状态: id={}, isDeleted={}", id, isDeleted);
        boolean success = adminCommentService.updateCommentStatus(id, isDeleted);
        return success ? Result.success(true) : Result.error("更新评论状态失败");
    }
    
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除评论")
    public Result<Boolean> deleteComment(@PathVariable Long id) {
        log.info("删除评论: id={}", id);
        boolean success = adminCommentService.deleteComment(id);
        return success ? Result.success(true) : Result.error("删除评论失败");
    }
} 