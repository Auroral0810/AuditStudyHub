package com.auditStudyHub.admin.controller;

import com.auditStudyHub.admin.dto.AdminResourceDTO;
import com.auditStudyHub.admin.dto.AuditRequestDTO;
import com.auditStudyHub.admin.dto.AuditResourceDTO;
import com.auditStudyHub.admin.dto.AuditResourceSearchRequest;
import com.auditStudyHub.admin.service.AuditResourceService;
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
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
@Tag(name = "资源审核接口", description = "提供资源审核相关功能")
public class AuditResourceController {
    
    private final AuditResourceService auditResourceService;
    
    @GetMapping("/pending")
    @Operation(summary = "获取待审核资源列表")
    public Result<List<AuditResourceDTO>> getPendingAuditResources() {
        log.info("获取待审核资源列表");
        List<AuditResourceDTO> resources = auditResourceService.getPendingAuditResources();
        return Result.success(resources);
    }
    
    @PostMapping("/search")
    @Operation(summary = "搜索审核资源")
    public Result<PageResult<AuditResourceDTO>> searchAuditResources(@RequestBody AuditResourceSearchRequest request) {
        log.info("搜索审核资源: {}", request);
        PageResult<AuditResourceDTO> result = auditResourceService.searchAuditResources(request);
        return Result.success(result);
    }
    
    @PostMapping("/batch")
    @Operation(summary = "批量审核资源")
    public Result<Boolean> batchAuditResources(@RequestBody AuditRequestDTO request) {
        log.info("批量审核资源: {}", request);
        boolean success = auditResourceService.batchAuditResources(request);
        return success ? Result.success(true) : Result.error("审核失败");
    }
    
    @GetMapping("/detail")
    @Operation(summary = "获取资源详情")
    public Result<AdminResourceDTO> getResourceDetail(@RequestParam Long id) {
        log.info("获取资源详情: id={}", id);
        AdminResourceDTO resource = auditResourceService.getResourceDetail(id);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        return Result.success(resource);
    }
} 