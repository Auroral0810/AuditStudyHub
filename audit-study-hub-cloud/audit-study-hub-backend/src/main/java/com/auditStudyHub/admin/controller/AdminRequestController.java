package com.auditStudyHub.admin.controller;

import com.auditStudyHub.admin.dto.AdminRequestDTO;
import com.auditStudyHub.admin.dto.AdminRequestSearchRequest;
import com.auditStudyHub.admin.service.AdminRequestService;
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
@RequestMapping("/admin/request")
@RequiredArgsConstructor
@Tag(name = "管理员资源请求接口", description = "提供资源请求管理功能")
public class AdminRequestController {

    private final AdminRequestService adminRequestService;
    
    @GetMapping("/list")
    @Operation(summary = "获取所有资源请求列表")
    public Result<List<AdminRequestDTO>> getAllRequests() {
        log.info("请求获取所有资源请求列表");
        List<AdminRequestDTO> requests = adminRequestService.getAllRequests();
        return Result.success(requests);
    }
    
    @GetMapping("/detail")
    @Operation(summary = "获取资源请求详情")
    public Result<AdminRequestDTO> getRequestDetail(@RequestParam Long id) {
        log.info("请求获取资源请求详情: id={}", id);
        AdminRequestDTO request = adminRequestService.getRequestById(id);
        if (request == null) {
            return Result.error("资源请求不存在");
        }
        return Result.success(request);
    }
    
    @PostMapping("/search")
    @Operation(summary = "搜索资源请求列表")
    public Result<PageResult<AdminRequestDTO>> searchRequests(@RequestBody AdminRequestSearchRequest request) {
        log.info("搜索资源请求: {}", request);
        PageResult<AdminRequestDTO> result = adminRequestService.searchRequests(request);
        return Result.success(result);
    }
    
    @PutMapping("/update-status")
    @Operation(summary = "更新资源请求状态")
    public Result<Boolean> updateRequestStatus(@RequestParam Long id, @RequestParam Integer status) {
        log.info("更新资源请求状态: id={}, status={}", id, status);
        boolean success = adminRequestService.updateRequestStatus(id, status);
        return success ? Result.success(true) : Result.error("更新资源请求状态失败");
    }
    
    @PutMapping("/update-delete")
    @Operation(summary = "逻辑删除或恢复资源请求")
    public Result<Boolean> updateDeleteStatus(@RequestParam Long id, @RequestParam Integer isDeleted) {
        log.info("更新资源请求删除状态: id={}, isDeleted={}", id, isDeleted);
        boolean success = adminRequestService.updateDeleteStatus(id, isDeleted);
        return success ? Result.success(true) : Result.error("更新资源请求删除状态失败");
    }
    
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "物理删除资源请求")
    public Result<Boolean> deleteRequest(@PathVariable Long id) {
        log.info("删除资源请求: id={}", id);
        boolean success = adminRequestService.deleteRequest(id);
        return success ? Result.success(true) : Result.error("删除资源请求失败");
    }
    
    @PostMapping("/create")
    @Operation(summary = "创建资源请求")
    public Result<AdminRequestDTO> createRequest(@RequestBody AdminRequestDTO requestDTO) {
        log.info("创建资源请求: {}", requestDTO);
        AdminRequestDTO createdRequest = adminRequestService.createRequest(requestDTO);
        if (createdRequest == null) {
            return Result.error("创建资源请求失败");
        }
        return Result.success(createdRequest);
    }
    
    @PutMapping("/update")
    @Operation(summary = "更新资源请求")
    public Result<AdminRequestDTO> updateRequest(@RequestBody AdminRequestDTO requestDTO) {
        log.info("更新资源请求: {}", requestDTO);
        AdminRequestDTO updatedRequest = adminRequestService.updateRequest(requestDTO);
        if (updatedRequest == null) {
            return Result.error("更新资源请求失败，请求不存在");
        }
        return Result.success(updatedRequest);
    }
    
    @PutMapping("/restore/{id}")
    @Operation(summary = "恢复已删除的资源请求")
    public Result<Boolean> restoreRequest(@PathVariable Long id) {
        log.info("恢复资源请求: id={}", id);
        boolean success = adminRequestService.updateDeleteStatus(id, 0);
        return success ? Result.success(true) : Result.error("恢复资源请求失败");
    }
} 