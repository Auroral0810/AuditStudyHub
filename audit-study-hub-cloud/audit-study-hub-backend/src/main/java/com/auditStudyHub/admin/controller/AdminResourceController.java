package com.auditStudyHub.admin.controller;

import com.auditStudyHub.admin.service.AdminResourceSyncService;
import com.auditStudyHub.admin.service.TAdminResourceService;
import com.auditStudyHub.admin.service.Impl.AdminResourceSyncServiceImpl;
import com.auditStudyHub.common.Result;
import com.auditStudyHub.admin.dto.AdminResourceDTO;
import com.auditStudyHub.admin.dto.AdminResourceSearchRequest;
import com.auditStudyHub.dto.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员资源控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/resource")
@RequiredArgsConstructor
@Tag(name = "管理员资源接口", description = "提供资源管理功能")
public class AdminResourceController {
    
    private final TAdminResourceService adminResourceService;
    private final AdminResourceSyncService adminResourceSyncService;
    
    @GetMapping("/list")
    @Operation(summary = "获取所有资源列表")
    public Result<List<AdminResourceDTO>> getAllResources() {
        log.info("请求获取所有资源列表");
        List<AdminResourceDTO> resources = adminResourceService.getAllResources();
        return Result.success(resources);
    }
    
    @GetMapping("/detail")
    @Operation(summary = "获取资源详情")
    public Result<AdminResourceDTO> getResourceDetail(@RequestParam Long id) {
        log.info("请求获取资源详情: id={}", id);
        AdminResourceDTO resource = adminResourceService.getResourceById(id);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        return Result.success(resource);
    }
    
    @PostMapping("/search")
    @Operation(summary = "搜索资源列表")
    public Result<PageResult<AdminResourceDTO>> searchResources(@RequestBody AdminResourceSearchRequest request) {
        log.info("管理员搜索资源: {}", request);
        
        PageResult<AdminResourceDTO> result = adminResourceService.searchResources(request);
        
        // 确保所有资源对象都有tags字段
        if (result != null && result.getList() != null) {
            result.getList().forEach(dto -> {
                if (dto.getTags() == null) {
                    dto.setTags("");
                }
            });
        }
        
        return Result.success(result);
    }
    
    @PutMapping("/update-status")
    @Operation(summary = "更新资源状态（逻辑删除或恢复）")
    public Result<Boolean> updateResourceStatus(@RequestParam Long id, @RequestParam Integer isDeleted) {
        log.info("更新资源状态: id={}, isDeleted={}", id, isDeleted);
        boolean success = adminResourceService.updateResourceStatus(id, isDeleted);
        return success ? Result.success(true) : Result.error("更新资源状态失败");
    }
    
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除资源")
    public Result<Boolean> deleteResource(@PathVariable Long id) {
        log.info("删除资源: id={}", id);
        boolean success = adminResourceService.deleteResource(id);
        return success ? Result.success(true) : Result.error("删除资源失败");
    }

    /**
     * 触发全量同步
     */
    @PostMapping("/sync")
    @Operation(summary = "手动触发全量同步")
    public ResponseEntity<String> triggerSync() {
        try {
            adminResourceSyncService.fullSync();
            return ResponseEntity.ok("全量同步成功");
        } catch (Exception e) {
            log.error("触发全量同步失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("同步失败：" + e.getMessage());
        }
    }

    /**
     * 添加资源
     */
    @PostMapping("/add")
    @Operation(summary = "添加资源")
    public Result<Long> addResource(@RequestBody AdminResourceDTO resourceDTO) {
        log.info("添加资源: {}", resourceDTO);
        Long resourceId = adminResourceService.addResource(resourceDTO);
        return Result.success( "添加资源成功");
    }

    /**
     * 更新资源信息
     */
    @PutMapping("/update")
    @Operation(summary = "更新资源信息")
    public Result<Boolean> updateResource(@RequestBody AdminResourceDTO resourceDTO) {
        log.info("更新资源信息: {}", resourceDTO);
        boolean success = adminResourceService.updateResource(resourceDTO);
        return success ? Result.success( "更新资源成功") : Result.error("更新资源失败");
    }
} 