package com.auditStudyHub.controller;

import com.auditStudyHub.common.Result;
import com.auditStudyHub.dto.*;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.mapper.TUserMapper;
import com.auditStudyHub.service.*;
import com.auditStudyHub.utils.OSSUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

/**
 * 资源控制器
 * @author auroral
 * @date 2025/5/7 12:32
 */
@Slf4j
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
@Tag(name = "资源接口", description = "资源相关的API")
public class ResourceController {

    private final ResourceService resourceService;
    private final SearchService searchService;
    private final THotSearchService tHotSearchService;
    private final TPopularSearchService tPopularSearchService;
    private final CommentService commentService;
    private final OSSUtils ossUtils;
    private final ObjectMapper objectMapper;
    private final TUserMapper userMapper;

    @GetMapping("/latest")
    @Operation(summary = "获取最新资源", description = "获取最新的资源列表，默认返回4条")
    public Result<List<ResourceDTO>> getLatestResources(
            @Parameter(description = "限制返回数量，默认4条") @RequestParam(defaultValue = "4") int limit) {
        List<ResourceDTO> resources = resourceService.getLatestResources(limit);
        return Result.success(resources);
    }
    
    @GetMapping("/popular")
    @Operation(summary = "获取热门资源", description = "获取下载量最多的资源列表，默认返回4条")
    public Result<List<ResourceDTO>> getPopularResources(
            @Parameter(description = "限制返回数量，默认4条") @RequestParam(defaultValue = "4") int limit) {
        List<ResourceDTO> resources = resourceService.getPopularResources(limit);
        return Result.success(resources);
    }
    
    @GetMapping("/hot-search")
    @Operation(summary = "获取热门搜索词", description = "获取系统热门搜索词列表，默认返回5条")
    public Result<List<String>> getHotSearchKeywords(
            @Parameter(description = "限制返回数量，默认5条") @RequestParam(defaultValue = "5") int limit) {
        try {
            List<String> keywords = tPopularSearchService.getHotSearchKeywords(limit);
            return Result.success(keywords);
        } catch (Exception e) {
            log.error("获取热门搜索词出错", e);
            return Result.success(Collections.emptyList());
        }
    }
    
    @Data
    @Schema(description = "搜索关键词请求")
    static class SearchKeywordRequest {
        @Schema(description = "搜索关键词", required = true)
        private String keyword;
    }

    @PostMapping("/record-search")
    @Operation(summary = "记录搜索关键词", description = "记录用户搜索的关键词")
    public Result<Boolean> recordSearchKeyword(
            @Parameter(description = "搜索关键词请求") @RequestBody SearchKeywordRequest request) {
        try {
            String keyword = request.getKeyword();
            
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.error("关键词不能为空");
            }
            
            boolean success = tHotSearchService.recordSearchKeyword(keyword);
            return Result.success(success);
        } catch (Exception e) {
            log.error("记录搜索关键词出错", e);
            return Result.success(false);
        }
    }

    /**
     * 高级搜索API
     */
    @PostMapping("/search")
    @Operation(summary = "高级搜索", description = "支持多条件组合的高级搜索")
    public Result<PageResult<ResourceDTO>> advancedSearch(@RequestBody SearchRequest request) {
        try {
            log.info("执行高级搜索: {}", request);
            PageResult<ResourceDTO> result = searchService.search(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("高级搜索失败", e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "获取资源列表", description = "获取资源列表，根据条件筛选")
    public Result<PageResult<ResourceDTO>> getResourceList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer minSize,
            @RequestParam(required = false) Integer maxSize,
            @RequestParam(required = false) Long userId) {
        
        try {
            log.info("获取资源列表: page={}, size={}, keyword={}, sort={}, userId={}", page, size, keyword, sort, userId);
            
            // 构建搜索请求
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setPage(page);
            searchRequest.setSize(size);
            searchRequest.setKeyword(keyword);
            searchRequest.setCollegeId(collegeId);
            searchRequest.setMajorId(majorId);
            searchRequest.setCategoryId(categoryId);
            searchRequest.setSort(sort);
            searchRequest.setStatus(status);
            searchRequest.setStartDate(startDate);
            searchRequest.setEndDate(endDate);
            searchRequest.setMinSize(minSize);
            searchRequest.setMaxSize(maxSize);
            searchRequest.setUserId(userId);  // 设置用户ID
            
            // 优先使用ES搜索
            PageResult<ResourceDTO> result = searchService.search(searchRequest);
            
            // 如果传入了用户ID，为资源列表填充用户交互状态
            if (userId != null && result != null && result.getList() != null && !result.getList().isEmpty()) {
                result.setList(resourceService.fillUserInteractionStatus(result.getList(), userId));
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取资源列表失败", e);
            // 如果ES搜索失败，则使用传统方式获取
            try {
                PageResult<ResourceDTO> fallbackResult = resourceService.getResourceList(
                    page, size, keyword, collegeId, majorId, categoryId, 
                    sort, status, startDate, endDate, minSize, maxSize
                );
                
                // 如果传入了用户ID，为资源列表填充用户交互状态
                if (userId != null && fallbackResult != null && fallbackResult.getList() != null && !fallbackResult.getList().isEmpty()) {
                    fallbackResult.setList(resourceService.fillUserInteractionStatus(fallbackResult.getList(), userId));
                }
                
                return Result.success(fallbackResult);
            } catch (Exception ex) {
                log.error("使用传统方式获取资源列表也失败", ex);
                return Result.error("获取资源列表失败: " + e.getMessage());
            }
        }
    }

    @GetMapping("/suggest")
    @Operation(summary = "获取搜索建议", description = "根据输入前缀返回搜索建议列表")
    public Result<List<SearchSuggestionDTO>> getSuggestions(
            @Parameter(description = "搜索前缀") @RequestParam String prefix,
            @Parameter(description = "分类ID（可选）") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "限制返回数量，默认5条") @RequestParam(defaultValue = "5") int limit) {
        try {
            if (prefix == null || prefix.trim().isEmpty()) {
                return Result.success(Collections.emptyList());
            }
            
            List<SearchSuggestionDTO> suggestions = searchService.getSuggestions(prefix.trim(), categoryId, limit);
            return Result.success(suggestions);
        } catch (Exception e) {
            log.error("获取搜索建议出错", e);
            return Result.error("获取搜索建议失败: " + e.getMessage());
        }
    }
    /**
     * 记录资源下载统计
     */
    @PostMapping("/record-download/{resourceId}")
    @Operation(summary = "记录资源下载", description = "记录资源被下载的次数")
    public Result<Boolean> recordResourceDownload(
            @Parameter(description = "资源ID") @PathVariable Long resourceId) {
        try {
            log.info("记录资源下载: resourceId={}", resourceId);
            boolean success = resourceService.incrementDownloadCount(resourceId);
            return Result.success(success);
        } catch (Exception e) {
            log.error("记录资源下载失败", e);
            return Result.error("记录下载失败: " + e.getMessage());
        }
    }

    
    @PostMapping("/user/record-download")
    @Operation(summary = "记录用户下载历史", description = "保存用户下载资源的历史记录")
    public Result<Boolean> recordUserDownload(
            @Parameter(description = "用户下载记录请求") @RequestBody UserDownloadRequest request) {
        try {
            log.info("记录用户下载历史: userId={}, resourceId={}", request.getUserId(), request.getResourceId());
            
            if (request.getResourceId() == null || request.getUserId() == null) {
                return Result.error("资源ID和用户ID不能为空");
            }
            
            boolean success = resourceService.recordUserDownload(request.getUserId(), request.getResourceId());
            return Result.success(success);
        } catch (Exception e) {
            log.error("记录用户下载历史失败", e);
            return Result.error("记录下载历史失败: " + e.getMessage());
        }
    }

    @GetMapping("/like/check/{resourceId}")
    @Operation(summary = "检查用户是否点赞资源", description = "查询当前用户是否点赞了指定资源")
    public Result<Boolean> checkLikeStatus(
            @PathVariable("resourceId") Long resourceId,
            @RequestParam(value = "userId", required = false) Long userId) {
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        boolean liked = resourceService.checkUserLiked(userId, resourceId);
        return Result.success(liked);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取资源详情", description = "根据ID获取资源详情信息")
    public Result<ResourceDTO> getResourceDetail(
            @PathVariable("id") Long id,
            @RequestParam(value = "userId", required = false) Long userId) {
        
        // 根据ID查询资源详情
        ResourceDTO resource = resourceService.getResourceById(id);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        
        // 增加浏览次数（异步操作）
        resourceService.incrementViewCount(id);
        
        // 如果提供了用户ID，则填充用户交互状态
        if (userId != null) {
            resource = resourceService.fillUserInteractionStatus(resource, userId);
        }
        
        return Result.success(resource);
    }

    /**
     * 获取资源详情（包含评论）
     */
    @GetMapping("/detail-with-comments/{id}")
    @Operation(summary = "获取资源详情（包含评论）", description = "根据资源ID获取资源详细信息和评论列表")
    public Result<ResourceDetailDTO> getResourceDetailWithComments(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {

        try {
            log.info("获取资源详情（含评论）: id={}, userId={}", id, userId);

            // 获取资源详情和评论
            ResourceDetailDTO detailDTO = resourceService.getResourceDetailWithComments(id, userId);

            if (detailDTO == null) {
                return Result.notFound("资源不存在");
            }

            // 自动增加浏览次数（异步，不等待结果）
            CompletableFuture.runAsync(() -> {
                resourceService.incrementViewCount(id);
            });

            return Result.success(detailDTO);
        } catch (Exception e) {
            log.error("获取资源详情失败: id={}", id, e);
            return Result.error("获取资源详情失败: " + e.getMessage());
        }
    }

    /**
     * 点赞/取消点赞资源
     */
    @PostMapping("/like/{resourceId}")
    @Operation(summary = "点赞/取消点赞资源", description = "如果用户未点赞，则点赞；如果已点赞，则取消点赞")
    public Result<LikeResultDTO> toggleResourceLike(
            @PathVariable Long resourceId,
            @RequestBody UserInteractionRequest request) {
        
        try {
            Long userId = request.getUserId();
            log.info("处理资源点赞: resourceId={}, userId={}", resourceId, userId);
            
            if (resourceId == null || userId == null) {
                return Result.error("资源ID和用户ID不能为空");
            }
            
            LikeResultDTO result = resourceService.toggleResourceLike(userId, resourceId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("处理资源点赞失败", e);
            return Result.error("处理点赞失败: " + e.getMessage());
        }
    }

    /**
     * 收藏/取消收藏资源
     */
    @PostMapping("/collect/{resourceId}")
    @Operation(summary = "收藏/取消收藏资源", description = "如果用户未收藏，则收藏；如果已收藏，则取消收藏")
    public Result<CollectResultDTO> toggleResourceCollection(
            @PathVariable Long resourceId,
            @RequestBody UserInteractionRequest request) {
        
        try {
            Long userId = request.getUserId();
            log.info("处理资源收藏: resourceId={}, userId={}", resourceId, userId);
            
            if (resourceId == null || userId == null) {
                return Result.error("资源ID和用户ID不能为空");
            }
            
            CollectResultDTO result = resourceService.toggleResourceCollection(userId, resourceId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("处理资源收藏失败", e);
            return Result.error("处理收藏失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量收藏资源
     */
    @PostMapping("/batch-collect")
    @Operation(summary = "批量收藏资源", description = "批量收藏多个资源")
    public Result<Map<String, Object>> batchCollectResources(@RequestBody BatchCollectRequest request) {
        try {
            Long userId = request.getUserId();
            List<Long> ids = request.getIds();
            
            log.info("批量收藏资源: userId={}, ids={}", userId, ids);
            
            if (userId == null || ids == null || ids.isEmpty()) {
                return Result.error("用户ID和资源ID列表不能为空");
            }
            
            Map<String, Object> result = resourceService.batchCollectResources(userId, ids);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量收藏资源失败", e);
            return Result.error("批量收藏失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户点赞记录
     */
    @GetMapping("/my-likes")
    @Operation(summary = "获取用户点赞记录", description = "获取当前用户点赞的资源列表")
    public Result<PageResult<UserLikeDTO>> getUserLikes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = true) Long userId) {
        
        try {
            log.info("获取用户点赞记录: userId={}, page={}, size={}, keyword={}", userId, page, size, keyword);
            
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            PageResult<UserLikeDTO> result = resourceService.getUserLikes(userId, page, size, keyword);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户点赞记录失败", e);
            return Result.error("获取点赞记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户收藏记录
     */
    @GetMapping("/my-collections")
    @Operation(summary = "获取用户收藏记录", description = "获取当前用户收藏的资源列表")
    public Result<PageResult<UserCollectionDTO>> getUserCollections(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = true) Long userId) {
        
        try {
            log.info("获取用户收藏记录: userId={}, page={}, size={}, keyword={}", userId, page, size, keyword);
            
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            PageResult<UserCollectionDTO> result = resourceService.getUserCollections(userId, page, size, keyword);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户收藏记录失败", e);
            return Result.error("获取收藏记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户下载记录
     */
    @GetMapping("/my-downloads")
    @Operation(summary = "获取用户下载记录", description = "获取当前用户下载的资源列表")
    public Result<PageResult<UserDownloadDTO>> getUserDownloads(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = true) Long userId) {
        
        try {
            log.info("获取用户下载记录: userId={}, page={}, size={}, keyword={}", userId, page, size, keyword);
            
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            PageResult<UserDownloadDTO> result = resourceService.getUserDownloads(userId, page, size, keyword);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户下载记录失败", e);
            return Result.error("获取下载记录失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户下载记录
     */
    @DeleteMapping("/my-downloads/{downloadId}")
    @Operation(summary = "删除用户下载记录", description = "删除指定的用户下载记录")
    public Result<Boolean> deleteUserDownload(
            @PathVariable Long downloadId,
            @RequestParam(required = true) Long userId) {
        
        try {
            log.info("删除用户下载记录: userId={}, downloadId={}", userId, downloadId);
            
            if (userId == null || downloadId == null) {
                return Result.error("用户ID和下载记录ID不能为空");
            }
            
            boolean success = resourceService.deleteUserDownload(userId, downloadId);
            if (success) {
                return Result.success("删除下载记录成功");
            } else {
                return Result.error("删除下载记录失败，可能记录不存在或无权限删除");
            }
        } catch (Exception e) {
            log.error("删除用户下载记录失败", e);
            return Result.error("删除下载记录失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户上传记录
     */
    @DeleteMapping("/my-uploads/{resourceId}")
    @Operation(summary = "删除用户上传记录", description = "删除指定的用户上传记录（逻辑删除）")
    public Result<Boolean> deleteUserUpload(
            @PathVariable Long resourceId,
            @RequestParam(required = true) Long userId) {
        
        try {
            log.info("删除用户上传记录: userId={}, resourceId={}", userId, resourceId);
            
            if (userId == null || resourceId == null) {
                return Result.error("用户ID和资源ID不能为空");
            }
            
            boolean success = resourceService.deleteUserUpload(userId, resourceId);
            if (success) {
                return Result.success("删除上传记录成功");
            } else {
                return Result.error("删除上传记录失败，可能记录不存在或无权限删除");
            }
        } catch (Exception e) {
            log.error("删除用户上传记录失败", e);
            return Result.error("删除上传记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户上传记录
     */
    @GetMapping("/my-uploads")
    @Operation(summary = "获取用户上传记录", description = "获取当前用户上传的资源列表，包括待审核、已通过和已拒绝状态")
    public Result<PageResult<UserUploadDTO>> getUserUploads(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = true) Long userId) {
        
        try {
            log.info("获取用户上传记录: userId={}, page={}, size={}, keyword={}, status={}", userId, page, size, keyword, status);
            
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            PageResult<UserUploadDTO> result = resourceService.getUserUploads(userId, page, size, keyword, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户上传记录失败", e);
            return Result.error("获取上传记录失败: " + e.getMessage());
        }
    }

    /**
     * 添加评论
     */
    @PostMapping("/comment/add")
    @Operation(summary = "添加评论", description = "添加资源评论，支持回复其他评论")
    public Result<CommentDTO> addComment(@RequestBody CommentAddRequest request) {
        try {
            log.info("添加评论: resourceId={}, userId={}, replyTo={}", 
                request.getResourceId(), request.getUserId(), request.getReplyTo());
            
            if (request.getResourceId() == null || request.getUserId() == null || 
                request.getContent() == null || request.getContent().trim().isEmpty()) {
                return Result.error("评论信息不完整");
            }
            
            // 转换为CommentDTO对象
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setResourceId(request.getResourceId());
            commentDTO.setUserId(request.getUserId());
            commentDTO.setContent(request.getContent().trim());
            commentDTO.setParentId(request.getReplyTo());  // 设置回复的评论ID
            
            // 调用服务添加评论
            CommentDTO result = commentService.addComment(commentDTO);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("添加评论失败", e);
            return Result.error("添加评论失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comment/delete/{commentId}")
    @Operation(summary = "删除评论", description = "根据评论ID删除评论，只有评论作者可以删除自己的评论")
    public Result<Boolean> deleteComment(
            @PathVariable("commentId") Long commentId,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        try {
            if (commentId == null) {
                return Result.error("评论ID不能为空");
            }
            
            // 获取用户ID
            Long userId = null;
            if (requestBody != null && requestBody.containsKey("userId")) {
                userId = Long.valueOf(requestBody.get("userId").toString());
            }
            
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            log.info("删除评论: commentId={}, userId={}", commentId, userId);
            
            // 调用评论服务删除评论
            boolean success = commentService.deleteComment(commentId, userId);
            
            if (success) {
                return Result.success("评论删除成功");
            } else {
                return Result.error("评论删除失败，请检查评论ID或权限");
            }
        } catch (Exception e) {
            log.error("删除评论失败: commentId={}", commentId, e);
            return Result.error("删除评论失败: " + e.getMessage());
        }
    }

    /**
     * 通用文件代理API，支持各种文件类型
     */
    @GetMapping("/proxy-file")
    public void proxyFile(@RequestParam("url") String url,
                         @RequestParam(value = "type", required = false) String type,
                         HttpServletResponse response) {
        log.info("代理文件请求: url={}, type={}", url, type);
        
        try {
            // 从URL推断内容类型
            String contentType = "application/octet-stream";
            
            if (type != null) {
                switch (type.toLowerCase()) {
                    case "pdf":
                        contentType = "application/pdf";
                        break;
                    case "image":
                    case "jpg":
                    case "jpeg":
                        contentType = "image/jpeg";
                        break;
                    case "png":
                        contentType = "image/png";
                        break;
                    case "svg":
                        contentType = "image/svg+xml";
                        break;
                    case "ico":
                        contentType = "image/x-icon";
                        break;
                    case "gif":
                        contentType = "image/gif";
                        break;
                    case "txt":
                    case "text":
                        contentType = "text/plain";
                        break;
                    case "html":
                        contentType = "text/html";
                        break;
                    case "css":
                        contentType = "text/css";
                        break;
                    case "js":
                        contentType = "application/javascript";
                        break;
                    case "json":
                        contentType = "application/json";
                        break;
                    case "xml":
                        contentType = "application/xml";
                        break;
                    case "yml":
                    case "yaml":
                        contentType = "application/x-yaml";
                        break;
                    case "sql":
                        contentType = "application/sql";
                        break;
                    case "csv":
                        contentType = "text/csv";
                        break;
                    case "doc":
                    case "docx":
                        contentType = "application/msword";
                        break;
                    case "ppt":
                        contentType = "application/vnd.ms-powerpoint";
                        break;
                    case "pptx":
                        contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                        break;
                    case "xls":
                    case "xlsx":
                        contentType = "application/vnd.ms-excel";
                        break;
                    case "audio":
                    case "mp3":
                        contentType = "audio/mpeg";
                        break;
                    case "video":
                    case "mp4":
                        contentType = "video/mp4";
                        break;
                    case "code":
                        contentType = "text/plain";
                        break;
                }
            } else {
                // 从URL推断
                if (url.toLowerCase().endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (url.toLowerCase().endsWith(".jpg") || url.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (url.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (url.toLowerCase().endsWith(".svg")) {
                    contentType = "image/svg+xml";
                } else if (url.toLowerCase().endsWith(".ico")) {
                    contentType = "image/x-icon";
                } else if (url.toLowerCase().endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (url.toLowerCase().endsWith(".txt")) {
                    contentType = "text/plain";
                } else if (url.toLowerCase().endsWith(".html") || url.toLowerCase().endsWith(".htm")) {
                    contentType = "text/html";
                } else if (url.toLowerCase().endsWith(".css")) {
                    contentType = "text/css";
                } else if (url.toLowerCase().endsWith(".js")) {
                    contentType = "application/javascript";
                } else if (url.toLowerCase().endsWith(".json")) {
                    contentType = "application/json";
                } else if (url.toLowerCase().endsWith(".xml")) {
                    contentType = "application/xml";
                } else if (url.toLowerCase().endsWith(".yml") || url.toLowerCase().endsWith(".yaml")) {
                    contentType = "application/x-yaml";
                } else if (url.toLowerCase().endsWith(".sql")) {
                    contentType = "application/sql";
                } else if (url.toLowerCase().endsWith(".csv")) {
                    contentType = "text/csv";
                } else if (url.toLowerCase().endsWith(".doc") || url.toLowerCase().endsWith(".docx")) {
                    contentType = "application/msword";
                } else if (url.toLowerCase().endsWith(".ppt")) {
                    contentType = "application/vnd.ms-powerpoint";
                } else if (url.toLowerCase().endsWith(".pptx")) {
                    contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                } else if (url.toLowerCase().endsWith(".xls") || url.toLowerCase().endsWith(".xlsx")) {
                    contentType = "application/vnd.ms-excel";
                } else if (url.toLowerCase().endsWith(".mp3") || url.toLowerCase().endsWith(".wav") || url.toLowerCase().endsWith(".ogg")) {
                    contentType = "audio/mpeg";
                } else if (url.toLowerCase().endsWith(".mp4") || url.toLowerCase().endsWith(".avi") || url.toLowerCase().endsWith(".webm")) {
                    contentType = "video/mp4";
                }
            }
            
            response.setContentType(contentType);
            response.setHeader("Access-Control-Allow-Origin", "*");
            
            // 使用增强的HTTP客户端
            fetchAndStreamContent(url, response);
            
        } catch (Exception e) {
            log.error("文件代理失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("文件代理失败: " + e.getMessage());
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }
    
    /**
     * 使用增强的HTTP客户端获取并传输内容
     */
    private void fetchAndStreamContent(String url, HttpServletResponse response) throws Exception {
        // 创建一个配置了完全信任的HTTP客户端
        java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
            .sslContext(createTrustAllSSLContext())
            .connectTimeout(java.time.Duration.ofSeconds(15))  // 增加连接超时时间
            .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)  // 自动跟随重定向
            .build();
        
        // 构建请求，添加必要的请求头
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(url))
            .timeout(java.time.Duration.ofSeconds(30))  // 设置请求超时
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .header("Accept", "*/*")
            .header("Accept-Encoding", "gzip, deflate")
            .GET()
            .build();
        
        // 发送请求并获取响应
        java.net.http.HttpResponse<java.io.InputStream> httpResponse = 
            client.send(request, java.net.http.HttpResponse.BodyHandlers.ofInputStream());
        
        // 如果响应状态码不是2xx，抛出异常
        if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
            throw new IOException("HTTP请求失败，状态码: " + httpResponse.statusCode());
        }
        
        // 将响应内容流式写入输出流
        try (InputStream in = httpResponse.body();
             OutputStream out = response.getOutputStream()) {
            
            byte[] buffer = new byte[8192];  // 增大缓冲区
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }
    
    /**
     * 创建一个信任所有证书的SSL上下文
     */
    private javax.net.ssl.SSLContext createTrustAllSSLContext() throws Exception {
        // 优化TLS版本和密码套件支持
        javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, new javax.net.ssl.TrustManager[] {
            new javax.net.ssl.X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
            }
        }, new java.security.SecureRandom());
        
        // 启用所有支持的密码套件
        javax.net.ssl.SSLParameters params = sslContext.getDefaultSSLParameters();
        params.setProtocols(new String[] { "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3" });
        
        return sslContext;
    }

    /**
     * PDF代理API端点 - 为了兼容现有代码保留此端点
     */
    @GetMapping("/proxy-pdf")
    public void proxyPdf(@RequestParam("url") String url, HttpServletResponse response) {
        // 直接调用通用文件代理API，传入pdf类型
        proxyFile(url, "pdf", response);
    }

    /**
     * 上传文件到OSS并返回文件URL
     * 
     * @param file    文件
     * @param username 用户名，用于生成文件路径
     * @return 文件URL
     */
    @PostMapping("/upload-file")
    public Result<String> uploadFileToOSS(@RequestParam("file") MultipartFile file,
                                         @RequestParam("username") String username) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件为空，请重新上传");
            }
            
            // 检查文件大小
            long maxSize = 100 * 1024 * 1024; // 100MB
            if (file.getSize() > maxSize) {
                return Result.error("文件大小超过限制");
            }
            
            // 生成文件夹路径，例如 resources/username/
            String folder = "resources/" + username + "/";
            
            // 调用OSS工具类上传文件
            String fileUrl = ossUtils.uploadFile(file, folder);
            
            if (fileUrl != null) {
                return Result.success( "文件上传成功",fileUrl);
            } else {
                return Result.error("文件上传失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    @Operation(summary = "上传资源", description = "上传新资源，默认状态为待审核")
    public Result<ResourceDTO> uploadResource(
            @RequestParam(value = "title") String title,
            @RequestParam(value = "description") String description,
            @RequestParam(value = "categoryId") Long categoryId,
            @RequestParam(value = "categoryName", required = false) String categoryName,
            @RequestParam(value = "collegeId", required = false) Long collegeId,
            @RequestParam(value = "collegeName", required = false) String collegeName,
            @RequestParam(value = "majorId", required = false) Long majorId,
            @RequestParam(value = "majorName", required = false) String majorName,
            @RequestParam(value = "courseName", required = false) String courseName,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "fileUrl") String fileUrl,
            @RequestParam(value = "fileSize", required = false) Long fileSize,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId
    ) {
        log.info("上传资源请求: title={}, categoryId={}, uploaderId={}, fileUrl={}, fileSize={}KB",
                title, categoryId, uploaderId, fileUrl, fileSize != null ? fileSize / 1024 : 0);
        
        // 如果uploaderId为null，从当前登录用户获取
        if (uploaderId == null) {
            // 从SecurityContext获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();
                
                // 根据用户名查询用户
                LambdaQueryWrapper<TUser> queryWrapper = Wrappers.lambdaQuery();
                queryWrapper.eq(TUser::getUsername, username);
                TUser user = userMapper.selectOne(queryWrapper);
                if (user != null) {
                    uploaderId = user.getId();
                    log.info("从当前登录用户获取uploaderId: {}", uploaderId);
                }
            }
        }
        
        // 创建上传DTO
        ResourceUploadDTO uploadDTO = new ResourceUploadDTO();
        uploadDTO.setTitle(title);
        uploadDTO.setDescription(description);
        uploadDTO.setCategoryId(categoryId);
        uploadDTO.setCategoryName(categoryName);
        uploadDTO.setCollegeId(collegeId);
        uploadDTO.setCollegeName(collegeName);
        uploadDTO.setMajorId(majorId);
        uploadDTO.setMajorName(majorName);
        uploadDTO.setCourseName(courseName);
        uploadDTO.setTags(tags);
        uploadDTO.setFileUrl(fileUrl);
        uploadDTO.setFileSize(fileSize);
        uploadDTO.setFileType(fileType);
        uploadDTO.setUploaderId(uploaderId);
        
        // 调用服务上传资源
        ResourceDTO result = resourceService.uploadResource(uploadDTO);
        
        return Result.success("资源上传成功，待审核", result);
    }

}