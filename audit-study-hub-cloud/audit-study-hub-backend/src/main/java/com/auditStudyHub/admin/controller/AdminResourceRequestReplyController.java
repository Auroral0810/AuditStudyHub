package com.auditStudyHub.admin.controller;

import com.auditStudyHub.admin.dto.AdminResourceRequestReplyDTO;
import com.auditStudyHub.admin.dto.AdminResourceRequestReplySearchRequest;
import com.auditStudyHub.admin.service.AdminResourceRequestReplyService;
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
@RequestMapping("/admin/resource-request-reply")
@RequiredArgsConstructor
@Tag(name = "管理员资源请求回复接口", description = "提供资源请求回复管理功能")
public class AdminResourceRequestReplyController {

    private final AdminResourceRequestReplyService replyService;
    
    @GetMapping("/list")
    @Operation(summary = "获取所有资源请求回复")
    public Result<List<AdminResourceRequestReplyDTO>> getAllReplies() {
        log.info("获取所有资源请求回复");
        List<AdminResourceRequestReplyDTO> replies = replyService.getAllReplies();
        return Result.success(replies);
    }
    
    @GetMapping("/detail")
    @Operation(summary = "获取资源请求回复详情")
    public Result<AdminResourceRequestReplyDTO> getReplyDetail(@RequestParam Long id) {
        log.info("获取资源请求回复详情: id={}", id);
        AdminResourceRequestReplyDTO reply = replyService.getReplyById(id);
        if (reply == null) {
            return Result.error("资源请求回复不存在");
        }
        return Result.success(reply);
    }
    
    @PostMapping("/search")
    @Operation(summary = "搜索资源请求回复")
    public Result<PageResult<AdminResourceRequestReplyDTO>> searchReplies(@RequestBody AdminResourceRequestReplySearchRequest request) {
        log.info("搜索资源请求回复: {}", request);
        PageResult<AdminResourceRequestReplyDTO> result = replyService.searchReplies(request);
        return Result.success(result);
    }
    
    @PostMapping("/add")
    @Operation(summary = "添加资源请求回复")
    public Result<Long> addReply(@RequestBody AdminResourceRequestReplyDTO dto) {
        log.info("添加资源请求回复: {}", dto);
        Long id = replyService.addReply(dto);
        return Result.success( "添加成功");
    }
    
    @PutMapping("/update-content")
    @Operation(summary = "更新资源请求回复内容")
    public Result<Boolean> updateReplyContent(@RequestParam Long id, @RequestParam String content) {
        log.info("更新资源请求回复内容: id={}, content={}", id, content);
        boolean success = replyService.updateReplyContent(id, content);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    @PutMapping("/update-status")
    @Operation(summary = "更新资源请求回复状态")
    public Result<Boolean> updateReplyStatus(@RequestParam Long id, @RequestParam Integer isDeleted) {
        log.info("更新资源请求回复状态: id={}, isDeleted={}", id, isDeleted);
        boolean success = replyService.updateReplyStatus(id, isDeleted);
        return success ? Result.success( "更新成功") : Result.error("更新失败");
    }
    
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除资源请求回复")
    public Result<Boolean> deleteReply(@PathVariable Long id) {
        log.info("删除资源请求回复: id={}", id);
        boolean success = replyService.deleteReply(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
} 