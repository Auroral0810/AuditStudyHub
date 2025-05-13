package com.auditStudyHub.service.impl;

import com.auditStudyHub.dto.*;
import com.auditStudyHub.entity.TResourceRequest;
import com.auditStudyHub.entity.TResourceRequestReply;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.entity.TResource;
import com.auditStudyHub.mapper.TResourceRequestMapper;
import com.auditStudyHub.mapper.TResourceRequestReplyMapper;
import com.auditStudyHub.mapper.TUserMapper;
import com.auditStudyHub.mapper.TResourceMapper;
import com.auditStudyHub.service.ResourceRequestService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceRequestServiceImpl implements ResourceRequestService {

    private final TResourceRequestMapper resourceRequestMapper;
    private final TResourceRequestReplyMapper resourceRequestReplyMapper;
    private final TUserMapper userMapper;
    private final TResourceMapper resourceMapper;
    private final ResourceRequestSearchServiceImpl searchService;
    private final ResourceRequestSyncService syncService;
    
    @Override
    public PageResult<ResourceRequestDTO> getResourceRequestList(
            Integer page, Integer size, String keyword, Long collegeId, Long majorId,
            Integer status, String startDate, String endDate, String sort) {
        
        ResourceRequestSearchRequest searchRequest = new ResourceRequestSearchRequest();
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setKeyword(keyword);
        searchRequest.setCollegeId(collegeId);
        searchRequest.setMajorId(majorId);
        searchRequest.setStatus(status);
        searchRequest.setStartDate(startDate);
        searchRequest.setEndDate(endDate);
        searchRequest.setSort(sort);
        
        return searchResourceRequests(searchRequest);
    }
    
    @Override
    public PageResult<ResourceRequestDTO> searchResourceRequests(ResourceRequestSearchRequest searchRequest) {
        try {
            // 优先使用ES搜索
            return searchService.search(searchRequest);
        } catch (Exception e) {
            log.error("ES搜索资源请求失败，降级到MySQL查询", e);
            return fallbackToMySQLSearch(searchRequest);
        }
    }
    
    /**
     * 降级到MySQL查询
     */
    private PageResult<ResourceRequestDTO> fallbackToMySQLSearch(ResourceRequestSearchRequest request) {
        log.info("MySQL执行查询: page={}, size={}, keyword={}, collegeId={}, majorId={}, status={}",
                 request.getPage(), request.getSize(), request.getKeyword(), 
                 request.getCollegeId(), request.getMajorId(), request.getStatus());
        
        // 构建分页对象
        Page<TResourceRequest> pageParam = new Page<>(request.getPage(), request.getSize());
        
        // 构建查询条件
        LambdaQueryWrapper<TResourceRequest> queryWrapper = Wrappers.lambdaQuery();
        
        // 未删除的资源请求
        queryWrapper.eq(TResourceRequest::getIsDeleted, 0);
        
        // 状态条件
        if (request.getStatus() != null) {
            queryWrapper.eq(TResourceRequest::getStatus, request.getStatus());
        }
        
        // 学院条件
        if (request.getCollegeId() != null) {
            queryWrapper.eq(TResourceRequest::getCollegeId, request.getCollegeId());
        }
        
        // 专业条件
        if (request.getMajorId() != null) {
            queryWrapper.eq(TResourceRequest::getMajorId, request.getMajorId());
        }
        
        // 用户条件
        if (request.getUserId() != null) {
            queryWrapper.eq(TResourceRequest::getUserId, request.getUserId());
        }
        
        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                .like(TResourceRequest::getTitle, request.getKeyword())
                .or()
                .like(TResourceRequest::getContent, request.getKeyword()));
        }
        
        // 日期范围
        if (StringUtils.hasText(request.getStartDate())) {
            queryWrapper.ge(TResourceRequest::getCreateTime, request.getStartDate() + " 00:00:00");
        }
        if (StringUtils.hasText(request.getEndDate())) {
            queryWrapper.le(TResourceRequest::getCreateTime, request.getEndDate() + " 23:59:59");
        }
        
        // 排序
        if ("hot".equals(request.getSort())) {
            queryWrapper.orderByDesc(TResourceRequest::getViewCount);
        } else {
            queryWrapper.orderByDesc(TResourceRequest::getCreateTime);
        }
        
        // 执行查询
        Page<TResourceRequest> resultPage = resourceRequestMapper.selectPage(pageParam, queryWrapper);
        
        // 提取所有用户ID
        Set<Long> userIds = resultPage.getRecords().stream()
            .map(TResourceRequest::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        // 批量查询用户信息
        Map<Long, TUser> userMap;
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<TUser> userQueryWrapper = Wrappers.lambdaQuery();
            userQueryWrapper.in(TUser::getId, userIds);
            List<TUser> users = userMapper.selectList(userQueryWrapper);
            userMap = users.stream()
                .collect(Collectors.toMap(TUser::getId, Function.identity()));
        } else {
            userMap = new HashMap<>();
        }

        // 转换为DTO
        List<ResourceRequestDTO> dtoList = resultPage.getRecords().stream()
            .map(request1 -> {
                ResourceRequestDTO dto = new ResourceRequestDTO();
                BeanUtils.copyProperties(request1, dto);
                
                // 填充用户信息
                TUser user = userMap.get(request1.getUserId());
                if (user != null) {
                    dto.setUsername(user.getUsername());
                    dto.setUserAvatar(user.getAvatar());
                }
                
                return dto;
            })
            .collect(Collectors.toList());
        
        // 返回分页结果
        return new PageResult<>(dtoList, resultPage.getTotal());
    }
    
    @Override
    public ResourceRequestDetailDTO getResourceRequestDetail(Long id) {
        if (id == null) {
            return null;
        }
        
        // 查询请求信息
        TResourceRequest request = resourceRequestMapper.selectById(id);
        if (request == null || request.getIsDeleted() == 1) {
            return null;
        }
        
        // 查询用户信息
        TUser user = userMapper.selectById(request.getUserId());
        
        // 查询回复信息 - 使用新方法获取回复树
        List<TResourceRequestReply> replies = resourceRequestReplyMapper.getAllRepliesWithDeleted(id);
        
        // 转换为DTO
        ResourceRequestDTO requestDTO = new ResourceRequestDTO();
        BeanUtils.copyProperties(request, requestDTO);
        
        if (user != null) {
            // 设置基本用户信息
            requestDTO.setUsername(user.getUsername());
            requestDTO.setUserAvatar(user.getAvatar());
            
            // 设置发布者的学院和专业信息（从用户表获取）
            requestDTO.setPublisherCollegeId(user.getCollegeId());
            requestDTO.setPublisherCollegeName(user.getCollegeName());
            requestDTO.setPublisherMajorId(user.getMajorId());
            requestDTO.setPublisherMajorName(user.getMajorName());
        }
        
        // 构建回复树并转换为DTO
        List<ResourceRequestReplyDTO> replyDTOs = buildReplyTree(replies);
        
        // 构建详情DTO
        ResourceRequestDetailDTO detailDTO = new ResourceRequestDetailDTO();
        detailDTO.setRequest(requestDTO);
        detailDTO.setReplies(replyDTOs);
        detailDTO.setReplyCount((int) replies.stream().filter(r -> r.getIsDeleted() == 0).count());
        
        return detailDTO;
    }
    
    /**
     * 构建回复树
     */
    private List<ResourceRequestReplyDTO> buildReplyTree(List<TResourceRequestReply> replies) {
        if (replies == null || replies.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 转换为DTO
        List<ResourceRequestReplyDTO> dtoList = replies.stream()
            .map(this::convertReplyToDTO)
            .collect(Collectors.toList());
        
        // 用户ID集合
        List<Long> userIds = dtoList.stream()
            .map(ResourceRequestReplyDTO::getUserId)
            .distinct()
            .collect(Collectors.toList());
        
        // 批量查询用户信息
        Map<Long, TUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<TUser> userQueryWrapper = Wrappers.lambdaQuery();
            userQueryWrapper.in(TUser::getId, userIds);
            
            List<TUser> users = userMapper.selectList(userQueryWrapper);
            userMap = users.stream()
                .collect(Collectors.toMap(TUser::getId, user -> user));
        }
        
        // 填充用户信息
        for (ResourceRequestReplyDTO dto : dtoList) {
            TUser user = userMap.get(dto.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setUserAvatar(user.getAvatar());
            }
            
            // 标记已删除的回复
            if (dto.getIsDeleted() == 1) {
                // 将内容设置为已删除标识，但保留结构
                dto.setContent("该回复已被删除");
            }
        }
        
        // 构建回复树
        Map<Long, List<ResourceRequestReplyDTO>> replyMap = dtoList.stream()
            .filter(dto -> dto.getParentId() != null)
            .collect(Collectors.groupingBy(ResourceRequestReplyDTO::getParentId));
        
        // 查找回复对象的用户名
        for (ResourceRequestReplyDTO dto : dtoList) {
            if (dto.getParentId() != null) {
                ResourceRequestReplyDTO parentDto = dtoList.stream()
                    .filter(d -> d.getId().equals(dto.getParentId()))
                    .findFirst()
                    .orElse(null);
                
                if (parentDto != null) {
                    dto.setReplyToUsername(parentDto.getUsername());
                }
            }
            
            // 设置子回复
            dto.setChildren(replyMap.getOrDefault(dto.getId(), new ArrayList<>()));
        }
        
        // 过滤出顶层回复（没有父回复的回复）
        return dtoList.stream()
            .filter(dto -> dto.getParentId() == null)
            .collect(Collectors.toList());
    }
    
    /**
     * 将回复实体转换为DTO
     */
    private ResourceRequestReplyDTO convertReplyToDTO(TResourceRequestReply reply) {
        ResourceRequestReplyDTO dto = new ResourceRequestReplyDTO();
        BeanUtils.copyProperties(reply, dto);
        
        // 如果有关联资源，查询资源信息
        if (reply.getResourceId() != null) {
            TResource resource = resourceMapper.selectById(reply.getResourceId());
            if (resource != null && resource.getIsDeleted() == 0) {
                dto.setResourceTitle(resource.getTitle());
            }
        }
        
        return dto;
    }
    
    @Override
    @Transactional
    public boolean incrementViewCount(Long id) {
        if (id == null) {
            return false;
        }
        
        try {
            // 使用乐观锁增加浏览次数
            LambdaUpdateWrapper<TResourceRequest> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(TResourceRequest::getId, id)
                      .setSql("view_count = view_count + 1");
            
            boolean success = resourceRequestMapper.update(null, updateWrapper) > 0;
            
            // 同步到ES
            if (success) {
                // 获取最新数据
                TResourceRequest request = resourceRequestMapper.selectById(id);
                if (request != null) {
                    syncService.syncViewCount(id, request.getViewCount());
                }
            }
            
            return success;
        } catch (Exception e) {
            log.error("增加资源请求浏览次数失败: id={}", id, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean addReplyAndIncrementCount(Long requestId) {
        if (requestId == null) {
            return false;
        }
        
        try {
            // 使用乐观锁增加回复次数
            LambdaUpdateWrapper<TResourceRequest> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(TResourceRequest::getId, requestId)
                      .setSql("reply_count = reply_count + 1");
            
            boolean success = resourceRequestMapper.update(null, updateWrapper) > 0;
            
            // 同步到ES
            if (success) {
                // 获取最新数据
                TResourceRequest request = resourceRequestMapper.selectById(requestId);
                if (request != null) {
                    syncService.syncReplyCount(requestId, request.getReplyCount());
                }
            }
            
            return success;
        } catch (Exception e) {
            log.error("增加资源请求回复次数失败: requestId={}", requestId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean decrementReplyCount(Long requestId) {
        if (requestId == null) {
            return false;
        }
        
        try {
            // 使用乐观锁减少回复次数，确保不会小于0
            LambdaUpdateWrapper<TResourceRequest> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(TResourceRequest::getId, requestId)
                      .gt(TResourceRequest::getReplyCount, 0)  // 防止计数变为负数
                      .setSql("reply_count = reply_count - 1");
            
            boolean success = resourceRequestMapper.update(null, updateWrapper) > 0;
            
            // 同步到ES
            if (success) {
                // 获取最新数据
                TResourceRequest request = resourceRequestMapper.selectById(requestId);
                if (request != null) {
                    syncService.syncReplyCount(requestId, request.getReplyCount());
                }
            }
            
            return success;
        } catch (Exception e) {
            log.error("减少资源请求回复次数失败: requestId={}", requestId, e);
            return false;
        }
    }
    
    @Override
    public PageResult<ResourceRequestDTO> getUserResourceRequests(Long userId, Integer page, Integer size) {
        if (userId == null) {
            return new PageResult<>(new ArrayList<>(), 0);
        }
        
        ResourceRequestSearchRequest searchRequest = new ResourceRequestSearchRequest();
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setUserId(userId);
        searchRequest.setSort("newest");
        
        return searchResourceRequests(searchRequest);
    }

    @Override
    @Transactional
    public ResourceRequestDTO createRequest(ResourceRequestCreateRequest request, Long userId) {
        // 参数校验
        if (request == null || userId == null) {
            throw new IllegalArgumentException("请求参数不完整");
        }
        
        if (!StringUtils.hasText(request.getTitle()) || !StringUtils.hasText(request.getContent())) {
            throw new IllegalArgumentException("标题和内容不能为空");
        }
        
        try {
            // 查询用户信息
            TUser user = userMapper.selectById(userId);
            if (user == null || user.getIsDeleted() == 1) {
                throw new IllegalArgumentException("用户不存在或已被禁用");
            }
            
            // 创建资源请求实体
            TResourceRequest resourceRequest = new TResourceRequest();
            resourceRequest.setTitle(request.getTitle());
            resourceRequest.setContent(request.getContent());
            resourceRequest.setUserId(userId);
            
            // 设置可选字段
            if (request.getCourseId() != null) {
                resourceRequest.setCourseId(request.getCourseId());
            }
            if (StringUtils.hasText(request.getCourseName())) {
                resourceRequest.setCourseName(request.getCourseName());
            }
            if (request.getCollegeId() != null) {
                resourceRequest.setCollegeId(request.getCollegeId());
            }
            if (request.getMajorId() != null) {
                resourceRequest.setMajorId(request.getMajorId());
            }
            if (request.getCategoryId() != null) {
                resourceRequest.setCategoryId(request.getCategoryId());
                // 设置分类名称
                TResource resource = resourceMapper.selectById(request.getCategoryId());
                if (resource != null) {
                    resourceRequest.setCategoryName(resource.getTitle());
                }
            }

            // 设置初始状态
            resourceRequest.setStatus(0); // 0-未解决
            resourceRequest.setReplyCount(0);
            resourceRequest.setViewCount(0);
            resourceRequest.setIsDeleted(0);
            
            // 设置时间
            Date now = new Date();
            resourceRequest.setCreateTime(now);
            resourceRequest.setUpdateTime(now);
            
            // 保存到数据库
            resourceRequestMapper.insert(resourceRequest);
            
            // 同步到ES
            syncService.syncToElasticsearch(resourceRequest);
            
            // 转换为DTO返回
            ResourceRequestDTO dto = new ResourceRequestDTO();
            BeanUtils.copyProperties(resourceRequest, dto);
            dto.setUsername(user.getUsername());
            dto.setUserAvatar(user.getAvatar());
            
            log.info("用户{}成功创建资源请求，ID={}", userId, resourceRequest.getId());
            return dto;
        } catch (Exception e) {
            log.error("创建资源请求失败", e);
            throw new RuntimeException("创建资源请求失败: " + e.getMessage(), e);
        }
    }

    @Override
    public PageResult<ResourceRequestSimpleDTO> getUserResourceRequestsSimple(Long userId, Integer current, Integer size) {
        if (userId == null) {
            return new PageResult<>(new ArrayList<>(), 0);
        }
        
        // 获取用户请求列表
        ResourceRequestSearchRequest searchRequest = new ResourceRequestSearchRequest();
        searchRequest.setPage(current);
        searchRequest.setSize(size);
        searchRequest.setUserId(userId);
        searchRequest.setSort("newest");
        
        // 调用已有方法获取分页结果
        PageResult<ResourceRequestDTO> result = searchResourceRequests(searchRequest);
        
        // 转换为简化DTO
        List<ResourceRequestSimpleDTO> simpleList = result.getList().stream()
            .map(this::convertToSimpleDTO)
            .collect(Collectors.toList());
        
        return new PageResult<>(simpleList, result.getTotal());
    }

    /**
     * 将完整的ResourceRequestDTO转换为简化版
     */
    private ResourceRequestSimpleDTO convertToSimpleDTO(ResourceRequestDTO dto) {
        ResourceRequestSimpleDTO simpleDTO = new ResourceRequestSimpleDTO();
        BeanUtils.copyProperties(dto, simpleDTO);
        return simpleDTO;
    }

    @Override
    @Transactional
    public boolean deleteRequest(Long id, Long userId) {
        if (id == null || userId == null) {
            log.warn("删除资源请求失败：资源请求ID或用户ID为空");
            return false;
        }
        
        try {
            // 查询资源请求
            TResourceRequest request = resourceRequestMapper.selectById(id);
            if (request == null || request.getIsDeleted() == 1) {
                log.warn("资源请求不存在或已被删除: id={}", id);
                return false;
            }
            
            // 检查权限（只有请求的创建者可以删除）
            if (!request.getUserId().equals(userId)) {
                log.warn("用户无权删除此资源请求: userId={}, requestId={}, ownerUserId={}", 
                         userId, id, request.getUserId());
                return false;
            }
            
            // 执行逻辑删除
            LambdaUpdateWrapper<TResourceRequest> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(TResourceRequest::getId, id)
                         .set(TResourceRequest::getIsDeleted, 1)
                         .set(TResourceRequest::getUpdateTime, new Date());
            
            boolean success = resourceRequestMapper.update(null, updateWrapper) > 0;
            
            if (success) {
                // 从ES中删除
                syncService.removeFromElasticsearch(id);
                log.info("资源请求删除成功，已从ES中删除: id={}", id);
            }
            
            return success;
        } catch (Exception e) {
            log.error("删除资源请求失败: id={}", id, e);
            return false;
        }
    }

    @Override
    @Transactional
    public ResourceRequestDTO updateRequest(Long id, ResourceRequestUpdateRequest request, Long userId) {
        // 参数校验
        if (id == null || request == null || userId == null) {
            throw new IllegalArgumentException("请求参数不完整");
        }
        
        if (!StringUtils.hasText(request.getTitle()) || !StringUtils.hasText(request.getContent())) {
            throw new IllegalArgumentException("标题和内容不能为空");
        }
        
        try {
            // 查询资源请求
            TResourceRequest resourceRequest = resourceRequestMapper.selectById(id);
            if (resourceRequest == null || resourceRequest.getIsDeleted() == 1) {
                log.warn("资源请求不存在或已被删除: id={}", id);
                return null;
            }
            
            // 检查权限（只有请求的创建者可以更新）
            if (!resourceRequest.getUserId().equals(userId)) {
                log.warn("用户无权更新此资源请求: userId={}, requestId={}, ownerUserId={}", 
                        userId, id, resourceRequest.getUserId());
                return null;
            }
            
            // 更新资源请求信息
            resourceRequest.setTitle(request.getTitle());
            resourceRequest.setContent(request.getContent());
            
            // 更新可选字段
            if (request.getCourseId() != null) {
                resourceRequest.setCourseId(request.getCourseId());
            }
            if (StringUtils.hasText(request.getCourseName())) {
                resourceRequest.setCourseName(request.getCourseName());
            }
            if (request.getCollegeId() != null) {
                resourceRequest.setCollegeId(request.getCollegeId());
            }
            if (request.getMajorId() != null) {
                resourceRequest.setMajorId(request.getMajorId());
            }
            if (request.getCategoryId() != null) {
                resourceRequest.setCategoryId(request.getCategoryId());
                // 更新分类名称 - 实际项目中应该从分类服务获取
                // 这里先通过资源表获取分类名称，实际应该有专门的分类表
                TResource resource = resourceMapper.selectById(request.getCategoryId());
                if (resource != null) {
                    resourceRequest.setCategoryName(resource.getTitle());
                }
            }
            
            // 更新时间
            resourceRequest.setUpdateTime(new Date());
            
            // 保存到数据库
            resourceRequestMapper.updateById(resourceRequest);
            
            // 同步到ES
            syncService.syncToElasticsearch(resourceRequest);
            
            // 查询用户信息
            TUser user = userMapper.selectById(resourceRequest.getUserId());
            
            // 转换为DTO返回
            ResourceRequestDTO dto = new ResourceRequestDTO();
            BeanUtils.copyProperties(resourceRequest, dto);
            
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setUserAvatar(user.getAvatar());
            }
            
            log.info("用户{}成功更新资源请求，ID={}", userId, id);
            return dto;
        } catch (Exception e) {
            log.error("更新资源请求失败: id={}", id, e);
            throw new RuntimeException("更新资源请求失败: " + e.getMessage(), e);
        }
    }
}
