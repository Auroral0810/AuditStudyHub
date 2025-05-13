package com.auditStudyHub.controller;

import com.auditStudyHub.common.Result;
import com.auditStudyHub.dto.*;
import com.auditStudyHub.service.ResourceRequestService;
import com.auditStudyHub.service.ResourceRequestCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/resource-request")
@RequiredArgsConstructor
@Tag(name = "资源请求接口", description = "资源请求相关接口")
public class ResourceRequestController {

    private final ResourceRequestService resourceRequestService;
    private final ResourceRequestCommentService resourceRequestCommentService;

    @GetMapping("/list")
    @Operation(summary = "获取资源请求列表", description = "获取资源请求列表，根据条件筛选")
    public Result<PageResult<ResourceRequestDTO>> getResourceRequestList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        try {
            log.info("获取资源请求列表: page={}, size={}, keyword={}, sort={}", page, size, keyword, sort);
            
            // 构建搜索请求
            ResourceRequestSearchRequest searchRequest = new ResourceRequestSearchRequest();
            searchRequest.setPage(page);
            searchRequest.setSize(size);
            searchRequest.setKeyword(keyword);
            searchRequest.setCollegeId(collegeId);
            searchRequest.setMajorId(majorId);
            searchRequest.setStatus(status);
            searchRequest.setSort(sort);
            searchRequest.setStartDate(startDate);
            searchRequest.setEndDate(endDate);
            
            // 执行搜索
            PageResult<ResourceRequestDTO> result = resourceRequestService.searchResourceRequests(searchRequest);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取资源请求列表失败", e);
            return Result.error("获取资源请求列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取资源请求详情", description = "根据ID获取资源请求详情及回复列表")
    public Result<ResourceRequestDetailDTO> getResourceRequestDetail(@PathVariable Long id) {
        try {
            log.info("获取资源请求详情: id={}", id);
            
            // 增加浏览次数
            resourceRequestService.incrementViewCount(id);
            
            // 获取详情
            ResourceRequestDetailDTO detail = resourceRequestService.getResourceRequestDetail(id);
            
            if (detail == null) {
                return Result.error("资源请求不存在");
            }
            
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取资源请求详情失败", e);
            return Result.error("获取资源请求详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/comment/add")
    @Operation(summary = "添加评论", description = "为资源请求添加评论")
    public Result<ResourceRequestCommentDTO> addComment(@RequestBody ResourceRequestCommentAddRequest request) {
        try {
            log.info("添加资源请求评论: requestId={}, userId={}, parentId={}", 
                     request.getRequestId(), request.getUserId(), request.getParentId());
            
            if (request.getRequestId() == null || request.getUserId() == null || 
                request.getContent() == null || request.getContent().trim().isEmpty()) {
                return Result.error("评论信息不完整");
            }
            
            ResourceRequestCommentDTO result = resourceRequestCommentService.addComment(request);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("添加资源请求评论失败", e);
            return Result.error("添加评论失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/comment/delete/{id}")
    @Operation(summary = "删除评论", description = "删除资源请求评论，仅评论作者可删除")
    public Result<Boolean> deleteComment(@PathVariable Long id, @RequestBody ResourceRequestCommentDeleteRequest request) {
        try {
            log.info("删除资源请求评论: id={}, userId={}", id, request.getUserId());
            
            if (id == null || !id.equals(request.getId()) || request.getUserId() == null) {
                return Result.error("请求参数不完整");
            }
            
            boolean success = resourceRequestCommentService.deleteComment(id, request.getUserId());
            
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("删除评论失败，可能无权限或评论不存在");
            }
        } catch (Exception e) {
            log.error("删除资源请求评论失败: id={}", id, e);
            return Result.error("删除评论失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    @Operation(summary = "创建资源请求", description = "创建新的资源请求")
    public Result<ResourceRequestDTO> createResourceRequest(@RequestBody ResourceRequestCreateRequest request, @RequestParam Long userId) {
        try {
            log.info("创建资源请求: title={}, userId={}", request.getTitle(), userId);
            
            if (request.getTitle() == null || request.getContent() == null) {
                return Result.error("请求标题和内容不能为空");
            }
            
            ResourceRequestDTO result = resourceRequestService.createRequest(request, userId);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建资源请求失败", e);
            return Result.error("创建资源请求失败: " + e.getMessage());
        }
    }
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户发布的资源请求", description = "获取指定用户发布的资源请求列表")
    public Result<PageResult<ResourceRequestSimpleDTO>> getUserResourceRequests(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        try {
            log.info("获取用户资源请求列表: userId={}, page={}, size={}", userId, current, size);
            
            // 转换参数名，与现有方法保持一致（current -> page）
            PageResult<ResourceRequestSimpleDTO> result = resourceRequestService.getUserResourceRequestsSimple(userId, current, size);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户资源请求列表失败", e);
            return Result.error("获取用户资源请求列表失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除资源请求", description = "删除指定的资源请求，只有创建者可以删除")
    public Result<Boolean> deleteRequest(@PathVariable Long id, @RequestParam Long userId) {
        try {
            log.info("删除资源请求: id={}, userId={}", id, userId);
            
            if (id == null || userId == null) {
                return Result.error("请求ID和用户ID不能为空");
            }
            
            boolean success = resourceRequestService.deleteRequest(id, userId);
            
            if (success) {
                return Result.success("删除资源请求成功");
            } else {
                return Result.error("删除资源请求失败，可能无权限或请求不存在");
            }
        } catch (Exception e) {
            log.error("删除资源请求失败: id={}", id, e);
            return Result.error("删除资源请求失败: " + e.getMessage());
        }
    }
    @PutMapping("/update/{id}")
    @Operation(summary = "更新资源请求", description = "更新已有的资源请求，需要验证用户身份")
    public Result<ResourceRequestDTO> updateResourceRequest(
            @PathVariable Long id,
            @RequestBody ResourceRequestUpdateRequest request,
            @RequestParam Long userId) {
        try {
            log.info("更新资源请求: id={}, userId={}", id, userId);

            if (id == null || request == null || userId == null) {
                return Result.error("请求参数不完整");
            }

            ResourceRequestDTO result = resourceRequestService.updateRequest(id, request, userId);

            if (result != null) {
                return Result.success(result);
            } else {
                return Result.error("更新资源请求失败，可能无权限或请求不存在");
            }
        } catch (Exception e) {
            log.error("更新资源请求失败: id={}", id, e);
            return Result.error("更新资源请求失败: " + e.getMessage());
        }
    }
}
