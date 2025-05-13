package com.auditStudyHub.service.impl;

import com.auditStudyHub.dto.*;
import com.auditStudyHub.entity.TResource;
import com.auditStudyHub.entity.TUserCollection;
import com.auditStudyHub.entity.TUserDownload;
import com.auditStudyHub.entity.TUserLike;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.exception.BusinessException;
import com.auditStudyHub.mapper.TResourceMapper;
import com.auditStudyHub.mapper.TUserCollectionMapper;
import com.auditStudyHub.mapper.TUserDownloadMapper;
import com.auditStudyHub.mapper.TUserLikeMapper;
import com.auditStudyHub.mapper.TUserMapper;
import com.auditStudyHub.properties.AliyunOssProperties;
import com.auditStudyHub.service.ResourceService;
import com.auditStudyHub.service.CommentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.HashMap;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.dao.DuplicateKeyException;
import com.auditStudyHub.dto.ESSyncMessage;
import com.auditStudyHub.service.impl.ResourceSyncService;
import com.auditStudyHub.dto.UserLikeDTO;
import com.auditStudyHub.mapper.TCategoryMapper;
import com.auditStudyHub.entity.TCategory;
import com.auditStudyHub.mapper.TCollegeMapper;
import com.auditStudyHub.entity.TCollege;
import com.auditStudyHub.mapper.TMajorMapper;
import com.auditStudyHub.entity.TMajor;
import com.auditStudyHub.service.SearchService;
import org.springframework.context.annotation.Lazy;

/**
 * 资源服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    
    private final TResourceMapper resourceMapper;
    private final TUserDownloadMapper userDownloadMapper;
    private final TUserCollectionMapper userCollectionMapper;
    private final TUserLikeMapper userLikeMapper;
    private final TUserMapper userMapper;
    private final AliyunOssProperties ossProperties;
    private final CommentService commentService;
    private final ResourceSyncService resourceSyncService;
    private final TCategoryMapper categoryMapper;
    private final TCollegeMapper collegeMapper;
    private final TMajorMapper majorMapper;
    
    @Autowired
    @Lazy
    private SearchService searchService;
    
    @Override
    public List<ResourceDTO> getLatestResources(int limit) {
        // 查询条件：已审核通过且未删除的资源，按创建时间降序排序
        LambdaQueryWrapper<TResource> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(TResource::getStatus, 1) // 已通过
                    .eq(TResource::getIsDeleted, 0) // 未删除
                    .orderByDesc(TResource::getCreateTime) // 按创建时间降序
                    .last("LIMIT " + limit); // 限制数量
        
        List<TResource> resources = resourceMapper.selectList(queryWrapper);
        
        // 转换为DTO
        return convertToDTOBatch(resources);
    }
    
    @Override
    public List<ResourceDTO> getPopularResources(int limit) {
        // 查询条件：已审核通过且未删除的资源，按下载量降序排序
        LambdaQueryWrapper<TResource> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(TResource::getStatus, 1) // 已通过
                    .eq(TResource::getIsDeleted, 0) // 未删除
                    .orderByDesc(TResource::getDownloadCount) // 按下载量降序
                    .last("LIMIT " + limit); // 限制数量
        
        List<TResource> resources = resourceMapper.selectList(queryWrapper);
        
        // 转换为DTO
        return convertToDTOBatch(resources);
    }
    
    @Override
    public PageResult<ResourceDTO> getResourceList(
            Integer page, Integer size, String keyword, Long collegeId, Long majorId, Long categoryId,
            String sort, Integer status, String startDate, String endDate, Integer minSize, Integer maxSize) {
        
        log.info("MySQL执行查询: page={}, size={}, keyword={}, collegeId={}, majorId={}, categoryId={}, sort={}, status={}",
                 page, size, keyword, collegeId, majorId, categoryId, sort, status);
        
        // 构建分页对象
        Page<TResource> pageParam = new Page<>(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<TResource> queryWrapper = Wrappers.lambdaQuery();
        
        // 未删除的资源
        queryWrapper.eq(TResource::getIsDeleted, 0);
        
        // 状态条件
        if (status != null) {
            queryWrapper.eq(TResource::getStatus, status);
        }
        
        // 学院条件
        if (collegeId != null) {
            queryWrapper.eq(TResource::getCollegeId, collegeId);
        }
        
        // 专业条件
        if (majorId != null) {
            queryWrapper.eq(TResource::getMajorId, majorId);
        }
        
        // 分类条件
        if (categoryId != null) {
            queryWrapper.eq(TResource::getCategoryId, categoryId);
        }
        
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            // 使用全文索引或模糊查询，增加对上传者名称的搜索
            queryWrapper.and(wrapper -> wrapper
                .like(TResource::getTitle, keyword)
                .or()
                .like(TResource::getDescription, keyword)
                .or()
                .like(TResource::getTags, keyword)
                .or()
                .inSql(TResource::getUploaderId, 
                    "SELECT id FROM t_user WHERE username LIKE '%" + keyword + "%'")
            );
        }
        
        // 日期范围
        if (StringUtils.hasText(startDate)) {
            queryWrapper.ge(TResource::getCreateTime, startDate + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            queryWrapper.le(TResource::getCreateTime, endDate + " 23:59:59");
        }
        
        // 文件大小范围（MB转换为字节）
        if (minSize != null) {
            queryWrapper.ge(TResource::getFileSize, minSize * 1024L * 1024L);
        }
        if (maxSize != null) {
            queryWrapper.le(TResource::getFileSize, maxSize * 1024L * 1024L);
        }
        
        // 排序
        switch (sort) {
            case "downloads":
                queryWrapper.orderByDesc(TResource::getDownloadCount);
                break;
            case "views":
                queryWrapper.orderByDesc(TResource::getViewCount);
                break;
            case "likes":
                queryWrapper.orderByDesc(TResource::getLikeCount);
                break;
            case "newest":
            default:
                queryWrapper.orderByDesc(TResource::getCreateTime);
                break;
        }
        
        // 执行查询
        Page<TResource> resultPage = resourceMapper.selectPage(pageParam, queryWrapper);
        
        // 转换结果
        List<ResourceDTO> resourceDTOList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 确保获取正确的总记录数
        long total = resultPage.getTotal();
        log.info("MySQL查询到的资源总数: {}", total); // 添加日志记录总数
        
        // 返回分页结果
        return new PageResult<>(resourceDTOList, total);
    }

    @Override
    @Transactional
    public boolean incrementDownloadCount(Long resourceId) {
        try {
            // 使用乐观锁增加下载次数
            LambdaUpdateWrapper<TResource> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(TResource::getId, resourceId)
                    .setSql("download_count = download_count + 1");
            
            boolean success = resourceMapper.update(null, updateWrapper) > 0;
            
            // 直接同步ES而不是发送消息
            if (success) {
                try {
                    resourceSyncService.syncResourceCountField(
                        resourceId, 
                        ESSyncMessage.SyncType.DOWNLOAD_COUNT
                    );
                } catch (Exception e) {
                    // 捕获同步异常但不影响主流程
                    log.error("同步下载计数到ES失败，将在下次计划任务中同步: resourceId={}", resourceId, e);
                }
            }
            
            return success;
        } catch (Exception e) {
            log.error("增加资源下载次数失败: resourceId={}", resourceId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean recordUserDownload(Long userId, Long resourceId) {
        try {
            log.info("记录用户下载: userId={}, resourceId={}", userId, resourceId);
            
            // 检查资源是否存在
            TResource resource = resourceMapper.selectById(resourceId);
            if (resource == null) {
                log.warn("要下载的资源不存在: resourceId={}", resourceId);
                return false;
            }
            
            // 创建新的下载记录（每次下载都记录，允许同一用户多次下载同一资源）
            TUserDownload downloadRecord = new TUserDownload();
            downloadRecord.setUserId(userId);
            downloadRecord.setResourceId(resourceId);
            downloadRecord.setCreateTime(new Date());
            
            // 插入记录
            int result = userDownloadMapper.insert(downloadRecord);
            
            log.info("用户下载记录保存结果: {}", result > 0 ? "成功" : "失败");
            return result > 0;
        } catch (Exception e) {
            log.error("记录用户下载失败: userId={}, resourceId={}", userId, resourceId, e);
            return false;
        }
    }
    
    @Override
    public boolean checkUserCollected(Long userId, Long resourceId) {
        if (userId == null || resourceId == null) {
            return false;
        }
        
        try {
            // 查询条件：用户ID、资源ID、未删除的收藏记录
            LambdaQueryWrapper<TUserCollection> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(TUserCollection::getUserId, userId)
                      .eq(TUserCollection::getResourceId, resourceId)
                      .eq(TUserCollection::getIsDeleted, 0);
            
            List<TUserCollection> collections = userCollectionMapper.selectList(queryWrapper);
            log.info("检查用户收藏状态: userId={}, resourceId={}, 未删除记录数={}", userId, resourceId, collections.size());
            
            return !collections.isEmpty();
        } catch (Exception e) {
            log.error("检查用户收藏状态失败: userId={}, resourceId={}", userId, resourceId, e);
            return false;
        }
    }
    
    @Override
    public boolean checkUserLiked(Long userId, Long resourceId) {
        if (userId == null || resourceId == null) {
            return false;
        }
        
        try {
            // 查询条件：用户ID、资源ID、未删除的点赞记录
            LambdaQueryWrapper<TUserLike> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(TUserLike::getUserId, userId)
                      .eq(TUserLike::getResourceId, resourceId)
                      .eq(TUserLike::getIsDeleted, 0);
            
            List<TUserLike> likes = userLikeMapper.selectList(queryWrapper);
            log.info("检查用户点赞状态: userId={}, resourceId={}, 未删除记录数={}", userId, resourceId, likes.size());
            
            return !likes.isEmpty();
        } catch (Exception e) {
            log.error("检查用户点赞状态失败: userId={}, resourceId={}", userId, resourceId, e);
            return false;
        }
    }
    
    @Override
    public ResourceDTO fillUserInteractionStatus(ResourceDTO dto, Long userId) {
        if (dto == null || userId == null) {
            return dto;
        }
        
        // 设置收藏和点赞状态
        dto.setIsCollected(checkUserCollected(userId, dto.getId()));
        dto.setIsLiked(checkUserLiked(userId, dto.getId()));
        
        return dto;
    }
    
    @Override
    public List<ResourceDTO> fillUserInteractionStatus(List<ResourceDTO> dtoList, Long userId) {
        if (dtoList == null || dtoList.isEmpty() || userId == null) {
            return dtoList;
        }
        
        try {
            // 提取所有资源ID
            Set<Long> resourceIds = dtoList.stream()
                .map(ResourceDTO::getId)
                .collect(Collectors.toSet());
            
            // 批量查询收藏状态
            LambdaQueryWrapper<TUserCollection> collectionWrapper = Wrappers.lambdaQuery();
            collectionWrapper.eq(TUserCollection::getUserId, userId)
                          .in(TUserCollection::getResourceId, resourceIds)
                          .eq(TUserCollection::getIsDeleted, 0);
            
            List<TUserCollection> collections = userCollectionMapper.selectList(collectionWrapper);
            Set<Long> collectedResourceIds = collections.stream()
                .map(TUserCollection::getResourceId)
                .collect(Collectors.toSet());
            
            // 批量查询点赞状态
            LambdaQueryWrapper<TUserLike> likeWrapper = Wrappers.lambdaQuery();
            likeWrapper.eq(TUserLike::getUserId, userId)
                    .in(TUserLike::getResourceId, resourceIds)
                    .eq(TUserLike::getIsDeleted, 0);
            
            List<TUserLike> likes = userLikeMapper.selectList(likeWrapper);
            Set<Long> likedResourceIds = likes.stream()
                .map(TUserLike::getResourceId)
                .collect(Collectors.toSet());
            
            // 为每个DTO设置状态
            for (ResourceDTO dto : dtoList) {
                dto.setIsCollected(collectedResourceIds.contains(dto.getId()));
                dto.setIsLiked(likedResourceIds.contains(dto.getId()));
            }
            
            return dtoList;
        } catch (Exception e) {
            log.error("批量填充用户交互状态失败: userId={}", userId, e);
            // 出错时，至少返回原始列表
            return dtoList;
        }
    }
    
    /**
     * 将实体转换为DTO
     */
    private ResourceDTO convertToDTO(TResource resource) {
        ResourceDTO dto = new ResourceDTO();
        BeanUtils.copyProperties(resource, dto);
        
        // 填充上传者基本信息
        if (resource.getUploaderId() != null) {
            try {
                TUser uploader = userMapper.selectById(resource.getUploaderId());
                if (uploader != null) {
                    dto.setUploaderName(uploader.getUsername());
                    dto.setUploaderAvatar(uploader.getAvatar());
                    dto.setUploaderCollege(uploader.getCollegeName());
                    dto.setUploaderMajor(uploader.getMajorName());
                    
                    // 获取上传者的上传总数
                    LambdaQueryWrapper<TResource> countWrapper = Wrappers.lambdaQuery();
                    countWrapper.eq(TResource::getUploaderId, resource.getUploaderId())
                              .eq(TResource::getIsDeleted, 0);
                    Integer totalUploads = Math.toIntExact(resourceMapper.selectCount(countWrapper));
                    dto.setUploaderTotalUploads(totalUploads);
                }
            } catch (Exception e) {
                log.error("获取上传者信息失败: uploaderId={}", resource.getUploaderId(), e);
            }
        }
        
        return dto;
    }
    
    /**
     * 批量转换实体为DTO并填充上传者基本信息
     */
    private List<ResourceDTO> convertToDTOBatch(List<TResource> resources) {
        if (resources == null || resources.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取所有上传者ID
        Set<Long> uploaderIds = resources.stream()
            .map(TResource::getUploaderId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        
        // 批量查询上传者信息
        Map<Long, TUser> uploaderMap = new HashMap<>();
        if (!uploaderIds.isEmpty()) {
            try {
                LambdaQueryWrapper<TUser> queryWrapper = Wrappers.lambdaQuery();
                queryWrapper.in(TUser::getId, uploaderIds);
                List<TUser> uploaders = userMapper.selectList(queryWrapper);
                uploaderMap = uploaders.stream()
                    .collect(Collectors.toMap(TUser::getId, Function.identity(), (e1, e2) -> e1));
            } catch (Exception e) {
                log.error("批量获取上传者信息失败", e);
            }
        }
        
        // 查询每个上传者的上传总数
        Map<Long, Integer> uploaderTotalMap = new HashMap<>();
        if (!uploaderIds.isEmpty()) {
            try {
                for (Long uploaderId : uploaderIds) {
                    LambdaQueryWrapper<TResource> countWrapper = Wrappers.lambdaQuery();
                    countWrapper.eq(TResource::getUploaderId, uploaderId)
                              .eq(TResource::getIsDeleted, 0);
                    Integer totalUploads = Math.toIntExact(resourceMapper.selectCount(countWrapper));
                    uploaderTotalMap.put(uploaderId, totalUploads);
                }
            } catch (Exception e) {
                log.error("批量获取上传总数失败", e);
            }
        }
        
        // 转换为DTO并填充上传者信息
        List<ResourceDTO> dtoList = new ArrayList<>(resources.size());
        for (TResource resource : resources) {
            ResourceDTO dto = new ResourceDTO();
            BeanUtils.copyProperties(resource, dto);
            
            // 填充上传者信息
            if (resource.getUploaderId() != null) {
                TUser uploader = uploaderMap.get(resource.getUploaderId());
                if (uploader != null) {
                    dto.setUploaderName(uploader.getUsername());
                    dto.setUploaderAvatar(uploader.getAvatar());
                    dto.setUploaderCollege(uploader.getCollegeName());
                    dto.setUploaderMajor(uploader.getMajorName());
                }
                
                // 设置上传总数
                dto.setUploaderTotalUploads(uploaderTotalMap.getOrDefault(resource.getUploaderId(), 0));
            }
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }

    @Override
    public ResourceDTO getResourceById(Long id) {
        if (id == null) {
            return null;
        }
        
        try {
            // 查询资源
            TResource resource = resourceMapper.selectById(id);
            if (resource == null || resource.getIsDeleted() == 1) {
                return null;
            }
            
            // 转换为DTO
            return convertToDTO(resource);
        } catch (Exception e) {
            log.error("获取资源详情失败: id={}", id, e);
            return null;
        }
    }

    @Override
    @Transactional
    public boolean incrementViewCount(Long resourceId) {
        try {
            // 使用乐观锁增加浏览次数
            LambdaUpdateWrapper<TResource> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(TResource::getId, resourceId)
                    .setSql("view_count = view_count + 1");
            
            boolean success = resourceMapper.update(null, updateWrapper) > 0;
            
            // 直接同步ES而不是发送消息
            if (success) {
                try {
                    resourceSyncService.syncResourceCountField(
                        resourceId, 
                        ESSyncMessage.SyncType.VIEW_COUNT
                    );
                } catch (Exception e) {
                    // 捕获同步异常但不影响主流程
                    log.error("同步浏览计数到ES失败，将在下次计划任务中同步: resourceId={}", resourceId, e);
                }
            }
            
            return success;
        } catch (Exception e) {
            log.error("增加资源浏览次数失败: resourceId={}", resourceId, e);
            return false;
        }
    }

    @Override
    public ResourceDetailDTO getResourceDetailWithComments(Long id, Long userId) {
        if (id == null) {
            return null;
        }
        
        try {
            // 创建资源详情DTO
            ResourceDetailDTO detailDTO = new ResourceDetailDTO();
            
            // 获取资源基本信息
            ResourceDTO resourceDTO = getResourceById(id);
            if (resourceDTO == null) {
                return null;
            }
            
            // 如果提供了用户ID，填充用户交互状态
            if (userId != null) {
                resourceDTO = fillUserInteractionStatus(resourceDTO, userId);
            }
            
            // 获取评论列表
            detailDTO.setResource(resourceDTO);
            detailDTO.setComments(commentService.getCommentsByResourceId(id));
            detailDTO.setCommentCount(commentService.getCommentCountByResourceId(id));
            
            return detailDTO;
        } catch (Exception e) {
            log.error("获取资源详情失败: id={}, userId={}", id, userId, e);
            return null;
        }
    }

    /**
     * 切换资源收藏状态
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 收藏结果DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CollectResultDTO toggleResourceCollection(Long userId, Long resourceId) {
        log.info("切换资源收藏状态：userId={}, resourceId={}", userId, resourceId);
        
        try {
            // 检查资源是否存在且未删除
            TResource resource = resourceMapper.selectById(resourceId);
            if (resource == null || resource.getIsDeleted() == 1) {
                throw new RuntimeException("资源不存在或已被删除");
            }
            
            // 检查用户是否已收藏该资源
            Long collectionId = userCollectionMapper.checkCollectionExists(userId, resourceId);
            boolean collected;
            
            if (collectionId != null) {
                // 已收藏，取消收藏
                userCollectionMapper.softDeleteCollection(userId, resourceId);
                collected = false;
                log.info("用户{}取消收藏资源{}", userId, resourceId);
            } else {
                // 检查是否存在已删除的收藏记录
                Long deletedCollectionId = userCollectionMapper.checkDeletedCollectionExists(userId, resourceId);
                
                if (deletedCollectionId != null) {
                    // 恢复已删除的收藏记录
                    userCollectionMapper.restoreCollection(deletedCollectionId);
                    log.info("恢复用户{}的资源{}收藏记录", userId, resourceId);
                } else {
                    // 创建新的收藏记录
                    userCollectionMapper.safeInsertCollection(userId, resourceId);
                    log.info("用户{}收藏资源{}", userId, resourceId);
                }
                collected = true;
            }
            
            // 构建并返回结果
            CollectResultDTO resultDTO = new CollectResultDTO();
            resultDTO.setCollected(collected);
            return resultDTO;
        } catch (Exception e) {
            log.error("切换资源收藏状态失败", e);
            throw new RuntimeException("切换收藏状态失败：" + e.getMessage());
        }
    }

    /**
     * 切换资源点赞状态
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 点赞结果DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeResultDTO toggleResourceLike(Long userId, Long resourceId) {
        log.info("切换资源点赞状态：userId={}, resourceId={}", userId, resourceId);
        
        try {
            // 检查资源是否存在且未删除
            TResource resource = resourceMapper.selectById(resourceId);
            if (resource == null || resource.getIsDeleted() == 1) {
                throw new RuntimeException("资源不存在或已被删除");
            }
            
            // 检查用户是否已点赞该资源
            Long likeId = userLikeMapper.checkLikeExists(userId, resourceId);
            boolean liked;
            
            if (likeId != null) {
                // 已点赞，取消点赞
                userLikeMapper.softDeleteLike(userId, resourceId);
                liked = false;
                log.info("用户{}取消点赞资源{}", userId, resourceId);
                
                // 减少资源点赞数
                LambdaUpdateWrapper<TResource> updateWrapper = Wrappers.lambdaUpdate();
                updateWrapper.eq(TResource::getId, resourceId)
                          .gt(TResource::getLikeCount, 0)  // 防止点赞数变为负数
                          .setSql("like_count = like_count - 1");
                resourceMapper.update(null, updateWrapper);
            } else {
                // 检查是否存在已删除的点赞记录
                Long deletedLikeId = userLikeMapper.checkDeletedLikeExists(userId, resourceId);
                
                if (deletedLikeId != null) {
                    // 恢复已删除的点赞记录
                    userLikeMapper.restoreLike(deletedLikeId);
                    log.info("恢复用户{}的资源{}点赞记录", userId, resourceId);
                } else {
                    // 创建新的点赞记录
                    userLikeMapper.safeInsertLike(userId, resourceId);
                    log.info("用户{}点赞资源{}", userId, resourceId);
                }
                liked = true;
                
                // 增加资源点赞数
                LambdaUpdateWrapper<TResource> updateWrapper = Wrappers.lambdaUpdate();
                updateWrapper.eq(TResource::getId, resourceId)
                          .setSql("like_count = like_count + 1");
                resourceMapper.update(null, updateWrapper);
            }
            
            // 直接同步ES而不是发送消息
            try {
                resourceSyncService.syncResourceCountField(
                    resourceId, 
                    ESSyncMessage.SyncType.LIKE_COUNT
                );
            } catch (Exception e) {
                // 捕获同步异常但不影响主流程
                log.error("同步点赞计数到ES失败，将在下次计划任务中同步: resourceId={}", resourceId, e);
            }
            
            // 获取资源的最新点赞数
            resource = resourceMapper.selectById(resourceId);
            int likeCount = resource != null ? resource.getLikeCount() : 0;
            
            // 构建并返回结果
            LikeResultDTO resultDTO = new LikeResultDTO();
            resultDTO.setLiked(liked);
            resultDTO.setLikeCount(likeCount);
            return resultDTO;
        } catch (Exception e) {
            log.error("切换资源点赞状态失败", e);
            throw new RuntimeException("切换点赞状态失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量收藏资源
     * @param userId 用户ID
     * @param resourceIds 资源ID列表
     * @return 操作结果，包含成功收藏的资源ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchCollectResources(Long userId, List<Long> resourceIds) {
        log.info("批量收藏资源: userId={}, resourceIds={}", userId, resourceIds);
        
        Map<String, Object> result = new HashMap<>();
        List<Long> collectedIds = new ArrayList<>();
        boolean success = false;
        
        try {
            if (userId == null || resourceIds == null || resourceIds.isEmpty()) {
                throw new IllegalArgumentException("用户ID和资源ID列表不能为空");
            }
            
            // 查询所有有效的资源
            LambdaQueryWrapper<TResource> resourceQueryWrapper = Wrappers.lambdaQuery();
            resourceQueryWrapper.in(TResource::getId, resourceIds)
                .eq(TResource::getIsDeleted, 0);
            List<TResource> validResources = resourceMapper.selectList(resourceQueryWrapper);
            
            // 获取有效的资源ID列表
            Set<Long> validResourceIds = validResources.stream()
                .map(TResource::getId)
                .collect(Collectors.toSet());
            
            // 过滤出已收藏的资源
            LambdaQueryWrapper<TUserCollection> collectionQueryWrapper = Wrappers.lambdaQuery();
            collectionQueryWrapper.eq(TUserCollection::getUserId, userId)
                .in(TUserCollection::getResourceId, validResourceIds)
                .eq(TUserCollection::getIsDeleted, 0);
            List<TUserCollection> existingCollections = userCollectionMapper.selectList(collectionQueryWrapper);
            
            Set<Long> existingCollectionResourceIds = existingCollections.stream()
                .map(TUserCollection::getResourceId)
                .collect(Collectors.toSet());
            
            // 过滤出已删除的收藏（需要恢复）
            LambdaQueryWrapper<TUserCollection> deletedCollectionQueryWrapper = Wrappers.lambdaQuery();
            deletedCollectionQueryWrapper.eq(TUserCollection::getUserId, userId)
                .in(TUserCollection::getResourceId, validResourceIds)
                .eq(TUserCollection::getIsDeleted, 1);
            List<TUserCollection> deletedCollections = userCollectionMapper.selectList(deletedCollectionQueryWrapper);
            
            Map<Long, Long> deletedCollectionMap = deletedCollections.stream()
                .collect(Collectors.toMap(TUserCollection::getResourceId, TUserCollection::getId, (e1, e2) -> e1));
            
            // 恢复已删除的收藏
            for (Map.Entry<Long, Long> entry : deletedCollectionMap.entrySet()) {
                userCollectionMapper.restoreCollection(entry.getValue());
                collectedIds.add(entry.getKey());
                log.info("恢复用户{}的资源{}收藏记录", userId, entry.getKey());
            }
            
            // 创建新的收藏记录
            for (Long resourceId : validResourceIds) {
                // 跳过已收藏的资源
                if (existingCollectionResourceIds.contains(resourceId)) {
                    collectedIds.add(resourceId);
                    continue;
                }
                
                // 跳过已恢复的资源
                if (deletedCollectionMap.containsKey(resourceId)) {
                    continue;
                }
                
                // 创建新的收藏记录
                boolean inserted = userCollectionMapper.safeInsertCollection(userId, resourceId) > 0;
                if (inserted) {
                    collectedIds.add(resourceId);
                    log.info("用户{}收藏资源{}", userId, resourceId);
                }
            }
            
            success = true;
        } catch (Exception e) {
            log.error("批量收藏资源失败: userId={}, resourceIds={}", userId, resourceIds, e);
            throw new RuntimeException("批量收藏资源失败: " + e.getMessage());
        }
        
        result.put("success", success);
        result.put("collectedIds", collectedIds);
        return result;
    }

    /**
     * 获取用户点赞记录列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词 (可选)
     * @return 分页结果
     */
    @Override
    public PageResult<UserLikeDTO> getUserLikes(Long userId, Integer page, Integer size, String keyword) {
        log.info("获取用户点赞记录: userId={}, page={}, size={}, keyword={}", userId, page, size, keyword);
        
        try {
            // 构建分页对象
            Page<TUserLike> pageParam = new Page<>(page, size);
            
            // 构建查询条件
            LambdaQueryWrapper<TUserLike> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(TUserLike::getUserId, userId)
                      .eq(TUserLike::getIsDeleted, 0)
                      .orderByDesc(TUserLike::getCreateTime);
            
            // 执行查询
            Page<TUserLike> resultPage = userLikeMapper.selectPage(pageParam, queryWrapper);
            
            // 如果查询结果为空，返回空列表
            if (resultPage.getRecords().isEmpty()) {
                return new PageResult<>(new ArrayList<>(), 0);
            }
            
            // 提取所有资源ID
            List<Long> resourceIds = resultPage.getRecords().stream()
                .map(TUserLike::getResourceId)
                .collect(Collectors.toList());
            
            // 批量查询资源信息
            LambdaQueryWrapper<TResource> resourceQueryWrapper = Wrappers.lambdaQuery();
            resourceQueryWrapper.in(TResource::getId, resourceIds);
            
            // 如果提供了关键词，增加标题过滤
            if (StringUtils.hasText(keyword)) {
                resourceQueryWrapper.and(wrapper -> wrapper.like(TResource::getTitle, keyword));
            }
            
            List<TResource> resources = resourceMapper.selectList(resourceQueryWrapper);
            
            // 将资源信息转换为Map便于查找
            Map<Long, TResource> resourceMap = resources.stream()
                .collect(Collectors.toMap(TResource::getId, Function.identity(), (e1, e2) -> e1));
            
            // 关联查询所有上传者信息
            Set<Long> uploaderIds = resources.stream()
                .map(TResource::getUploaderId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
            
            Map<Long, TUser> uploaderMap;
            if (!uploaderIds.isEmpty()) {
                LambdaQueryWrapper<TUser> userQueryWrapper = Wrappers.lambdaQuery();
                userQueryWrapper.in(TUser::getId, uploaderIds);
                List<TUser> uploaders = userMapper.selectList(userQueryWrapper);
                uploaderMap = uploaders.stream()
                    .collect(Collectors.toMap(TUser::getId, Function.identity(), (e1, e2) -> e1));
            } else {
                uploaderMap = new HashMap<>();
            }

            // 转换为DTO
            List<UserLikeDTO> likeDTOs = resultPage.getRecords().stream()
                .map(like -> {
                    UserLikeDTO dto = new UserLikeDTO();
                    dto.setId(like.getId());
                    dto.setUserId(like.getUserId());
                    dto.setResourceId(like.getResourceId());
                    dto.setLikedAt(like.getCreateTime());
                    
                    // 填充资源信息
                    TResource resource = resourceMap.get(like.getResourceId());
                    if (resource != null) {
                        dto.setResourceTitle(resource.getTitle());
                        dto.setFileType(resource.getFileType());
                        dto.setCategoryName(resource.getCategoryName());
                        
                        // 填充上传者信息
                        if (resource.getUploaderId() != null) {
                            TUser uploader = uploaderMap.get(resource.getUploaderId());
                            if (uploader != null) {
                                dto.setUploaderName(uploader.getUsername());
                            }
                        }
                    }
                    
                    return dto;
                })
                // 过滤掉没有关联资源信息的记录
                .filter(dto -> dto.getResourceTitle() != null)
                .collect(Collectors.toList());
            
            // 返回结果
            return new PageResult<>(likeDTOs, resultPage.getTotal());
        } catch (Exception e) {
            log.error("获取用户点赞记录失败: userId={}", userId, e);
            return new PageResult<>(new ArrayList<>(), 0);
        }
    }

    @Override
    public PageResult<UserCollectionDTO> getUserCollections(Long userId, Integer page, Integer size, String keyword) {
        log.info("获取用户收藏记录: userId={}, page={}, size={}, keyword={}", userId, page, size, keyword);
        
        if (userId == null) {
            return new PageResult<>(new ArrayList<>(), 0);
        }
        
        try {
            // 构建分页对象
            Page<TUserCollection> pageParam = new Page<>(page, size);
            
            // 构建查询条件
            LambdaQueryWrapper<TUserCollection> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(TUserCollection::getUserId, userId)
                       .eq(TUserCollection::getIsDeleted, 0)
                       .orderByDesc(TUserCollection::getCreateTime);
            
            // 执行查询
            Page<TUserCollection> collectionPage = userCollectionMapper.selectPage(pageParam, queryWrapper);
            
            // 如果查询结果为空，返回空列表
            if (collectionPage.getRecords().isEmpty()) {
                return new PageResult<>(new ArrayList<>(), 0);
            }
            
            // 提取所有资源ID
            List<Long> resourceIds = collectionPage.getRecords().stream()
                .map(TUserCollection::getResourceId)
                .collect(Collectors.toList());
            
            // 批量查询资源信息
            LambdaQueryWrapper<TResource> resourceQueryWrapper = Wrappers.lambdaQuery();
            resourceQueryWrapper.in(TResource::getId, resourceIds)
                              .eq(TResource::getIsDeleted, 0);
            
            // 如果提供了关键词，增加过滤条件
            if (StringUtils.hasText(keyword)) {
                resourceQueryWrapper.and(wrapper -> wrapper
                    .like(TResource::getTitle, keyword)
                    .or()
                    .like(TResource::getDescription, keyword)
                    .or()
                    .like(TResource::getCategoryName, keyword)
                    .or()
                    .like(TResource::getTags, keyword));
            }
            
            List<TResource> resources = resourceMapper.selectList(resourceQueryWrapper);
            
            // 将资源信息转换为Map便于查找
            Map<Long, TResource> resourceMap = resources.stream()
                .collect(Collectors.toMap(TResource::getId, Function.identity(), (e1, e2) -> e1));
            
            // 关联查询所有上传者信息
            Set<Long> uploaderIds = resources.stream()
                .map(TResource::getUploaderId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
            
            Map<Long, TUser> uploaderMap;
            if (!uploaderIds.isEmpty()) {
                LambdaQueryWrapper<TUser> userQueryWrapper = Wrappers.lambdaQuery();
                userQueryWrapper.in(TUser::getId, uploaderIds);
                List<TUser> uploaders = userMapper.selectList(userQueryWrapper);
                uploaderMap = uploaders.stream()
                    .collect(Collectors.toMap(TUser::getId, Function.identity(), (e1, e2) -> e1));
            } else {
                uploaderMap = new HashMap<>();
            }

            // 转换为DTO
            List<UserCollectionDTO> collectionDTOs = collectionPage.getRecords().stream()
                .map(collection -> {
                    UserCollectionDTO dto = new UserCollectionDTO();
                    dto.setId(collection.getId());
                    dto.setUserId(collection.getUserId());
                    dto.setResourceId(collection.getResourceId());
                    dto.setCollectedAt(collection.getCreateTime());
                    
                    // 填充资源信息
                    TResource resource = resourceMap.get(collection.getResourceId());
                    if (resource != null) {
                        dto.setResourceTitle(resource.getTitle());
                        dto.setFileType(resource.getFileType());
                        dto.setCategoryName(resource.getCategoryName());
                        dto.setCoverUrl("/static/images/default-cover.png"); // 使用默认封面
                        
                        // 填充上传者信息
                        if (resource.getUploaderId() != null) {
                            TUser uploader = uploaderMap.get(resource.getUploaderId());
                            if (uploader != null) {
                                dto.setUploaderName(uploader.getUsername());
                            }
                        }
                    }
                    
                    return dto;
                })
                // 过滤掉没有关联资源信息的记录
                .filter(dto -> dto.getResourceTitle() != null)
                .collect(Collectors.toList());
            
            // 返回结果
            return new PageResult<>(collectionDTOs, collectionPage.getTotal());
        } catch (Exception e) {
            log.error("获取用户收藏记录失败: userId={}", userId, e);
            return new PageResult<>(new ArrayList<>(), 0);
        }
    }

    @Override
    public PageResult<UserDownloadDTO> getUserDownloads(Long userId, Integer page, Integer size, String keyword) {
        log.info("获取用户下载记录: userId={}, page={}, size={}, keyword={}", userId, page, size, keyword);
        
        if (userId == null) {
            return new PageResult<>(new ArrayList<>(), 0);
        }
        
        try {
            // 构建分页对象
            Page<TUserDownload> pageParam = new Page<>(page, size);
            
            // 构建查询条件
            LambdaQueryWrapper<TUserDownload> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(TUserDownload::getUserId, userId)
                       .orderByDesc(TUserDownload::getCreateTime);
            
            // 执行查询
            Page<TUserDownload> downloadPage = userDownloadMapper.selectPage(pageParam, queryWrapper);
            
            // 如果查询结果为空，返回空列表
            if (downloadPage.getRecords().isEmpty()) {
                return new PageResult<>(new ArrayList<>(), 0);
            }
            
            // 提取所有资源ID
            List<Long> resourceIds = downloadPage.getRecords().stream()
                .map(TUserDownload::getResourceId)
                .collect(Collectors.toList());
            
            // 批量查询资源信息
            LambdaQueryWrapper<TResource> resourceQueryWrapper = Wrappers.lambdaQuery();
            resourceQueryWrapper.in(TResource::getId, resourceIds)
                              .eq(TResource::getIsDeleted, 0);
            
            // 如果提供了关键词，增加过滤条件
            if (StringUtils.hasText(keyword)) {
                resourceQueryWrapper.and(wrapper -> wrapper
                    .like(TResource::getTitle, keyword)
                    .or()
                    .like(TResource::getDescription, keyword)
                    .or()
                    .like(TResource::getCategoryName, keyword)
                    .or()
                    .like(TResource::getTags, keyword));
            }
            
            List<TResource> resources = resourceMapper.selectList(resourceQueryWrapper);
            
            // 将资源信息转换为Map便于查找
            Map<Long, TResource> resourceMap = resources.stream()
                .collect(Collectors.toMap(TResource::getId, Function.identity(), (e1, e2) -> e1));
            
            // 关联查询所有上传者信息
            Set<Long> uploaderIds = resources.stream()
                .map(TResource::getUploaderId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
            
            Map<Long, TUser> uploaderMap;
            if (!uploaderIds.isEmpty()) {
                LambdaQueryWrapper<TUser> userQueryWrapper = Wrappers.lambdaQuery();
                userQueryWrapper.in(TUser::getId, uploaderIds);
                List<TUser> uploaders = userMapper.selectList(userQueryWrapper);
                uploaderMap = uploaders.stream()
                    .collect(Collectors.toMap(TUser::getId, Function.identity(), (e1, e2) -> e1));
            } else {
                uploaderMap = new HashMap<>();
            }

            // 转换为DTO
            List<UserDownloadDTO> downloadDTOs = downloadPage.getRecords().stream()
                .map(download -> {
                    UserDownloadDTO dto = new UserDownloadDTO();
                    dto.setId(download.getId());
                    dto.setUserId(download.getUserId());
                    dto.setResourceId(download.getResourceId());
                    dto.setDownloadedAt(download.getCreateTime());
                    
                    // 填充资源信息
                    TResource resource = resourceMap.get(download.getResourceId());
                    if (resource != null) {
                        dto.setResourceTitle(resource.getTitle());
                        dto.setFileType(resource.getFileType());
                        dto.setCategoryName(resource.getCategoryName());
                        dto.setCoverUrl("/static/images/default-cover.png"); // 使用默认封面
                        dto.setFileSize(resource.getFileSize());
                        
                        // 填充上传者信息
                        if (resource.getUploaderId() != null) {
                            TUser uploader = uploaderMap.get(resource.getUploaderId());
                            if (uploader != null) {
                                dto.setUploaderName(uploader.getUsername());
                            }
                        }
                    }
                    
                    return dto;
                })
                // 过滤掉没有关联资源信息的记录
                .filter(dto -> dto.getResourceTitle() != null)
                .collect(Collectors.toList());
            
            // 返回结果
            return new PageResult<>(downloadDTOs, downloadPage.getTotal());
        } catch (Exception e) {
            log.error("获取用户下载记录失败: userId={}", userId, e);
            return new PageResult<>(new ArrayList<>(), 0);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceDTO uploadResource(ResourceUploadDTO uploadDTO) {
        log.info("上传新资源: {}", uploadDTO);
        
        // 验证必填参数
        if (uploadDTO == null || !StringUtils.hasText(uploadDTO.getTitle()) 
                || !StringUtils.hasText(uploadDTO.getDescription())
                || uploadDTO.getCategoryId() == null 
                || !StringUtils.hasText(uploadDTO.getFileUrl())) {
            throw new IllegalArgumentException("必填参数不能为空");
        }
        
        try {
            // 创建资源实体
            TResource resource = new TResource();
            resource.setTitle(uploadDTO.getTitle());
            resource.setDescription(uploadDTO.getDescription());
            resource.setCategoryId(uploadDTO.getCategoryId());
            resource.setFileUrl(uploadDTO.getFileUrl());
            resource.setUploaderId(uploadDTO.getUploaderId());
            
            // 设置可选参数
            resource.setCollegeId(uploadDTO.getCollegeId());
            resource.setMajorId(uploadDTO.getMajorId());
            resource.setCourseName(uploadDTO.getCourseName());
            resource.setTags(uploadDTO.getTags());
            
            // 使用前端传入的分类名称、学院名称和专业名称
            if (StringUtils.hasText(uploadDTO.getCategoryName())) {
                resource.setCategoryName(uploadDTO.getCategoryName());
            } else if (uploadDTO.getCategoryId() != null) {
                // 后备方案：从数据库查询分类名称
                resource.setCategoryName(getCategoryName(uploadDTO.getCategoryId()));
            }
            
            if (StringUtils.hasText(uploadDTO.getCollegeName())) {
                resource.setCollegeName(uploadDTO.getCollegeName());
            } else if (uploadDTO.getCollegeId() != null) {
                // 后备方案：从数据库查询学院名称
                resource.setCollegeName(getCollegeName(uploadDTO.getCollegeId()));
            }
            
            if (StringUtils.hasText(uploadDTO.getMajorName())) {
                resource.setMajorName(uploadDTO.getMajorName());
            } else if (uploadDTO.getMajorId() != null) {
                // 后备方案：从数据库查询专业名称
                resource.setMajorName(getMajorName(uploadDTO.getMajorId()));
            }
            
            // 设置文件类型，优先使用前端传入的类型
            if (StringUtils.hasText(uploadDTO.getFileType())) {
                resource.setFileType(uploadDTO.getFileType());
            } else {
                // 后备方案：从文件URL推断文件类型
                resource.setFileType(inferFileTypeFromUrl(uploadDTO.getFileUrl()));
            }
            
            // 设置文件大小，优先使用前端传入的大小
            if (uploadDTO.getFileSize() != null && uploadDTO.getFileSize() > 0) {
                resource.setFileSize(uploadDTO.getFileSize());
            } else {
                // 如果是仓库或链接类型资源，设置默认的文件大小为0
                resource.setFileSize(0L);
                log.info("设置资源默认文件大小为0: title={}, type={}", uploadDTO.getTitle(), resource.getFileType());
            }
            
            // 初始化计数字段
            resource.setDownloadCount(0);
            resource.setViewCount(0);
            resource.setLikeCount(0);
            
            // 设置状态为待审核(0)
            resource.setStatus(0);
            
            // 设置当前时间
            Date now = new Date();
            resource.setCreateTime(now);
            resource.setUpdateTime(now);
            
            // 未删除
            resource.setIsDeleted(0);
            
            // 保存资源
            resourceMapper.insert(resource);
            log.info("资源保存成功: id={}", resource.getId());
            
            // 同步到ES，不再只同步审核通过的资源
            try {
                resourceSyncService.syncResourceToES(resource.getId(), ESSyncMessage.SyncType.FULL_SYNC);
                log.info("资源同步到ES成功: id={}", resource.getId());
            } catch (Exception e) {
                log.error("同步资源到ES失败: resourceId={}", resource.getId(), e);
                // 不抛出异常，不影响主流程
            }
            
            // 返回DTO
            return convertToDTO(resource);
        } catch (Exception e) {
            log.error("上传资源失败", e);
            throw new RuntimeException("上传资源失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从URL推断文件类型
     */
    private String inferFileTypeFromUrl(String url) {
        if (url == null) {
            return "unknown";
        }
        
        String lowerUrl = url.toLowerCase();
        
        // 提取扩展名
        String extension = null;
        int queryParamIndex = lowerUrl.indexOf('?');
        int fragmentIndex = lowerUrl.indexOf('#');
        int endIndex = lowerUrl.length();
        
        if (queryParamIndex > 0) {
            endIndex = queryParamIndex;
        }
        if (fragmentIndex > 0 && fragmentIndex < endIndex) {
            endIndex = fragmentIndex;
        }
        
        String urlWithoutParams = lowerUrl.substring(0, endIndex);
        int lastDotIndex = urlWithoutParams.lastIndexOf('.');
        
        if (lastDotIndex > 0 && lastDotIndex < urlWithoutParams.length() - 1) {
            extension = urlWithoutParams.substring(lastDotIndex + 1);
        }
        
        // 常见文档类型
        if (lowerUrl.endsWith(".pdf")) {
            return "pdf";
        } else if (isExtensionIn(lowerUrl, ".doc", ".docx", ".rtf", ".odt", ".fodt", ".dot", ".dotx")) {
            return "doc";
        } else if (isExtensionIn(lowerUrl, ".xls", ".xlsx", ".xlsm", ".xlsb", ".csv", ".ods", ".fods")) {
            return "excel";
        } else if (isExtensionIn(lowerUrl, ".ppt", ".pptx", ".pptm", ".pps", ".ppsx", ".odp", ".fodp")) {
            return "ppt";
        } else if (isExtensionIn(lowerUrl, ".txt", ".text", ".md", ".markdown", ".log")) {
            return "text";
        }
        
        // 图像类型
        if (isExtensionIn(lowerUrl, ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg", ".tif", ".tiff", 
                ".ico", ".heic", ".heif", ".raw", ".cr2", ".nef", ".psd", ".ai", ".eps")) {
            return "image";
        }
        
        // 音频类型
        if (isExtensionIn(lowerUrl, ".mp3", ".wav", ".ogg", ".flac", ".aac", ".wma", ".m4a", 
                ".opus", ".alac", ".aiff", ".ape")) {
            return "audio";
        }
        
        // 视频类型
        if (isExtensionIn(lowerUrl, ".mp4", ".avi", ".mov", ".wmv", ".flv", ".webm", ".mkv", ".m4v", 
                ".3gp", ".mpeg", ".mpg", ".ts", ".mts", ".vob")) {
            return "video";
        }
        
        // 压缩文件类型
        if (isExtensionIn(lowerUrl, ".zip", ".rar", ".7z", ".tar", ".gz", ".bz2", ".xz", ".cab", 
                ".iso", ".tgz", ".tbz2", ".lz", ".lzma", ".lzo", ".z", ".Z")) {
            return "archive";
        }
        
        // 编程和代码文件类型
        if (isExtensionIn(lowerUrl, ".java", ".cpp", ".cc", ".cxx", ".c", ".h", ".hpp", ".js", ".ts", 
                ".jsx", ".tsx", ".py", ".rb", ".php", ".html", ".htm", ".css", ".scss", ".sass", ".less", 
                ".json", ".xml", ".yaml", ".yml", ".go", ".rs", ".swift", ".kt", ".cs", ".vb", ".pl", ".sh", 
                ".bat", ".ps1", ".sql")) {
            return "code";
        }
        
        // 字体类型
        if (isExtensionIn(lowerUrl, ".ttf", ".otf", ".woff", ".woff2", ".eot")) {
            return "font";
        }
        
        // 3D模型和CAD类型
        if (isExtensionIn(lowerUrl, ".obj", ".fbx", ".stl", ".blend", ".dae", ".3ds", ".dwg", ".dxf")) {
            return "model";
        }
        
        // 电子书类型
        if (isExtensionIn(lowerUrl, ".epub", ".mobi", ".azw", ".azw3", ".ibooks")) {
            return "ebook";
        }
        
        // 数据库类型
        if (isExtensionIn(lowerUrl, ".db", ".sqlite", ".dbf", ".mdb", ".accdb")) {
            return "database";
        }
        
        // 检查是否为代码仓库链接
        if (lowerUrl.contains("github.com") || lowerUrl.contains("gitlab.com") || 
                lowerUrl.contains("bitbucket.org") || lowerUrl.contains("gitee.com")) {
            return "repository";
        }
        
        // 如果有扩展名但不在上述列表中，直接返回扩展名作为类型
        if (extension != null && !extension.isEmpty()) {
            return extension;
        }
        
        // 检查常见网站特征
        if (lowerUrl.contains("blog") || lowerUrl.contains("article")) {
            return "blog";
        }
        
        // 默认返回链接类型
        return "link";
    }
    
    /**
     * 辅助方法：检查URL是否以指定扩展名结尾
     */
    private boolean isExtensionIn(String url, String... extensions) {
        for (String ext : extensions) {
            // 检查确切的扩展名匹配
            if (url.endsWith(ext)) {
                // 确保它是真正的扩展名结尾或者后面跟着查询参数
                int extPos = url.lastIndexOf(ext);
                if (extPos + ext.length() == url.length() || 
                        (extPos + ext.length() < url.length() && 
                        (url.charAt(extPos + ext.length()) == '?' || 
                         url.charAt(extPos + ext.length()) == '#'))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取分类名称的辅助方法
     */
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "未知分类";
        }
        // 使用categoryMapper查询分类实体，返回其名称
        TCategory category = categoryMapper.selectById(categoryId);
        return category != null ? category.getName() : "未知分类";
    }

    /**
     * 获取学院名称的辅助方法
     */
    private String getCollegeName(Long collegeId) {
        if (collegeId == null) {
            return "未知学院";
        }
        // 使用collegeMapper查询学院实体，返回其名称
        TCollege college = collegeMapper.selectById(collegeId);
        return college != null ? college.getName() : "未知学院";
    }

    /**
     * 获取专业名称的辅助方法
     */
    private String getMajorName(Long majorId) {
        if (majorId == null) {
            return "未知专业";
        }
        // 使用majorMapper查询专业实体，返回其名称
        TMajor major = majorMapper.selectById(majorId);
        return major != null ? major.getName() : "未知专业";
    }

    @Override
    public PageResult<UserUploadDTO> getUserUploads(Long userId, Integer page, Integer size, String keyword, Integer status) {
        log.info("获取用户上传记录: userId={}, page={}, size={}, keyword={}, status={}", userId, page, size, keyword, status);
        
        if (userId == null) {
            return new PageResult<>(new ArrayList<>(), 0);
        }
        
        try {
            // 构建分页对象
            Page<TResource> pageParam = new Page<>(page, size);
            
            // 构建查询条件
            LambdaQueryWrapper<TResource> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(TResource::getUploaderId, userId)
                       .eq(TResource::getIsDeleted, 0)
                       .orderByDesc(TResource::getCreateTime);
            
            // 如果提供了关键词，增加过滤条件
            if (StringUtils.hasText(keyword)) {
                queryWrapper.and(wrapper -> wrapper
                    .like(TResource::getTitle, keyword)
                    .or()
                    .like(TResource::getDescription, keyword)
                    .or()
                    .like(TResource::getCategoryName, keyword)
                    .or()
                    .like(TResource::getTags, keyword));
            }
            
            // 如果提供了状态，增加状态过滤条件
            if (status != null) {
                queryWrapper.eq(TResource::getStatus, status);
                log.info("根据状态过滤资源: status={}", status);
            }
            
            // 执行查询
            Page<TResource> resourcePage = resourceMapper.selectPage(pageParam, queryWrapper);
            
            // 如果查询结果为空，返回空列表
            if (resourcePage.getRecords().isEmpty()) {
                return new PageResult<>(new ArrayList<>(), 0);
            }
            
            // 转换为DTO
            List<UserUploadDTO> uploadDTOs = resourcePage.getRecords().stream()
                .map(resource -> {
                    UserUploadDTO dto = new UserUploadDTO();
                    dto.setId(resource.getId());
                    dto.setUserId(resource.getUploaderId());
                    dto.setTitle(resource.getTitle());
                    dto.setDescription(resource.getDescription());
                    dto.setCategoryName(resource.getCategoryName());
                    dto.setFileType(resource.getFileType());
                    dto.setFileSize(resource.getFileSize());
                    dto.setDownloadCount(resource.getDownloadCount());
                    dto.setViewCount(resource.getViewCount());
                    dto.setLikeCount(resource.getLikeCount());
                    dto.setStatus(resource.getStatus());
                    dto.setCreateTime(resource.getCreateTime());
                    return dto;
                })
                .collect(Collectors.toList());
            
            // 返回结果
            return new PageResult<>(uploadDTOs, resourcePage.getTotal());
        } catch (Exception e) {
            log.error("获取用户上传记录失败: userId={}", userId, e);
            // 尝试从ES中查询
            try {
                return searchUserUploadsFromES(userId, page, size, keyword, status);
            } catch (Exception esException) {
                log.error("从ES查询用户上传记录失败: userId={}", userId, esException);
                return new PageResult<>(new ArrayList<>(), 0);
            }
        }
    }
    
    /**
     * 从ES中查询用户上传记录
     */
    private PageResult<UserUploadDTO> searchUserUploadsFromES(Long userId, Integer page, Integer size, String keyword, Integer status) {
        log.info("从ES查询用户上传记录: userId={}, page={}, size={}, keyword={}, status={}", userId, page, size, keyword, status);
        try {
            // 构建ES查询条件
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setPage(page);
            searchRequest.setSize(size);
            searchRequest.setKeyword(keyword);
            searchRequest.setUserId(userId);
            searchRequest.setStatus(status); // 设置状态过滤
            searchRequest.setSort("newest"); // 按时间降序排序
            
            // 执行ES查询
            PageResult<ResourceDTO> esResult = searchService.search(searchRequest);
            
            // 转换结果
            List<UserUploadDTO> uploadDTOs = esResult.getList().stream()
                .map(resourceDTO -> {
                    UserUploadDTO dto = new UserUploadDTO();
                    dto.setId(resourceDTO.getId());
                    dto.setUserId(resourceDTO.getUploaderId());
                    dto.setTitle(resourceDTO.getTitle());
                    dto.setDescription(resourceDTO.getDescription());
                    dto.setCategoryName(resourceDTO.getCategoryName());
                    dto.setFileType(resourceDTO.getFileType());
                    dto.setFileSize(resourceDTO.getFileSize());
                    dto.setDownloadCount(resourceDTO.getDownloadCount());
                    dto.setViewCount(resourceDTO.getViewCount());
                    dto.setLikeCount(resourceDTO.getLikeCount());
                    dto.setStatus(resourceDTO.getStatus());
                    dto.setCreateTime(resourceDTO.getCreateTime());
                    return dto;
                })
                .collect(Collectors.toList());
            
            return new PageResult<>(uploadDTOs, esResult.getTotal());
        } catch (Exception e) {
            log.error("从ES查询用户上传记录失败: userId={}", userId, e);
            throw e;
        }
    }

    @Override
    public boolean deleteUserDownload(Long userId, Long downloadId) {
        log.info("删除用户下载记录: userId={}, downloadId={}", userId, downloadId);
        
        if (userId == null || downloadId == null) {
            log.warn("删除下载记录失败：用户ID或下载记录ID为空");
            return false;
        }
        
        try {
            // 查询下载记录以确认是否存在且属于该用户
            LambdaQueryWrapper<TUserDownload> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(TUserDownload::getId, downloadId)
                      .eq(TUserDownload::getUserId, userId);
            
            TUserDownload download = userDownloadMapper.selectOne(queryWrapper);
            
            if (download == null) {
                log.warn("下载记录不存在或不属于该用户: userId={}, downloadId={}", userId, downloadId);
                return false;
            }
            
            // 直接删除记录
            int result = userDownloadMapper.deleteById(downloadId);
            
            log.info("删除下载记录结果: userId={}, downloadId={}, 删除结果={}", userId, downloadId, result > 0);
            return result > 0;
        } catch (Exception e) {
            log.error("删除下载记录异常: userId={}, downloadId={}", userId, downloadId, e);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserUpload(Long userId, Long resourceId) {
        log.info("删除用户上传记录: userId={}, resourceId={}", userId, resourceId);
        
        if (userId == null || resourceId == null) {
            log.warn("删除上传记录失败：用户ID或资源ID为空");
            return false;
        }
        
        try {
            // 查询资源以确认是否存在且属于该用户
            LambdaQueryWrapper<TResource> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(TResource::getId, resourceId)
                      .eq(TResource::getUploaderId, userId)
                      .eq(TResource::getIsDeleted, 0);
            
            TResource resource = resourceMapper.selectById(resourceId);
            
            if (resource == null) {
                log.warn("资源不存在: resourceId={}", resourceId);
                return false;
            }
            
            if (!resource.getUploaderId().equals(userId)) {
                log.warn("资源不属于该用户: userId={}, resourceId={}, actualUploaderId={}", 
                        userId, resourceId, resource.getUploaderId());
                return false;
            }
            
            // 逻辑删除资源
            LambdaUpdateWrapper<TResource> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(TResource::getId, resourceId)
                       .set(TResource::getIsDeleted, 1)
                       .set(TResource::getUpdateTime, new Date());
            
            int result = resourceMapper.update(null, updateWrapper);
            
            if (result > 0) {
                log.info("资源逻辑删除成功: resourceId={}", resourceId);
                
                // 同步到ES
                try {
                    resourceSyncService.removeFromElasticsearch(resourceId);
                    log.info("从ES中删除资源成功: resourceId={}", resourceId);
                } catch (Exception e) {
                    // 捕获同步异常但不影响主流程
                    log.error("从ES中删除资源失败: resourceId={}", resourceId, e);
                }
            }
            
            return result > 0;
        } catch (Exception e) {
            log.error("删除上传记录异常: userId={}, resourceId={}", userId, resourceId, e);
            return false;
        }
    }
} 