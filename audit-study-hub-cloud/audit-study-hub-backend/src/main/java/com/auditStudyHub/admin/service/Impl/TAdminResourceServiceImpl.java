package com.auditStudyHub.admin.service.Impl;

import com.auditStudyHub.admin.dto.AdminSearchRequest;
import com.auditStudyHub.admin.service.TAdminResourceService;
import com.auditStudyHub.document.ResourceDocument;
import com.auditStudyHub.admin.dto.AdminResourceDTO;
import com.auditStudyHub.admin.dto.AdminResourceSearchRequest;
import com.auditStudyHub.dto.PageResult;
import com.auditStudyHub.dto.ResourceDTO;
import com.auditStudyHub.dto.SearchRequest;
import com.auditStudyHub.entity.TCategory;
import com.auditStudyHub.entity.TCollege;
import com.auditStudyHub.entity.TResource;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.mapper.TCategoryMapper;
import com.auditStudyHub.mapper.TCollegeMapper;
import com.auditStudyHub.mapper.TResourceMapper;
import com.auditStudyHub.mapper.TUserMapper;
import com.auditStudyHub.repository.es.ResourceRepository;
import com.auditStudyHub.service.SearchService;
import com.auditStudyHub.service.impl.ResourceSyncService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 管理员资源服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TAdminResourceServiceImpl implements TAdminResourceService {

    private final TResourceMapper resourceMapper;
    private final TUserMapper userMapper;
    private final TCategoryMapper categoryMapper;
    private final TCollegeMapper collegeMapper;
    private final ResourceRepository resourceRepository;
    private final ResourceSyncService resourceSyncService;

    @Autowired
    @Qualifier("adminElasticsearchSearchService")
    private SearchService searchService;

    @Override
    public List<AdminResourceDTO> getAllResources() {
        log.info("获取所有资源列表");
        
        // 使用自定义mapper方法查询所有资源，包括已删除的
        List<TResource> resources = resourceMapper.selectAllWithDeleted();
        
        return resources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AdminResourceDTO getResourceById(Long id) {
        log.info("获取资源详情: id={}", id);
        
        TResource resource = resourceMapper.selectById(id);
        if (resource == null) {
            log.warn("资源不存在: id={}", id);
            return null;
        }
        
        return convertToDTO(resource);
    }

    @Override
    public PageResult<AdminResourceDTO> searchResources(AdminResourceSearchRequest request) {
        log.info("搜索资源: request={}", request);
        
        // 处理查询参数
        String title = request.getTitle();
        String uploaderName = request.getUploaderName();
        Integer isDeleted = request.getIsDeleted();
        
        // 使用自定义mapper方法搜索资源
        List<TResource> resources = resourceMapper.selectAllWithCondition(title, uploaderName, isDeleted);
        
        // 应用额外的筛选条件
        List<TResource> filteredResources = resources.stream()
            .filter(resource -> {
                if (request.getCollegeId() != null && !Objects.equals(resource.getCollegeId(), request.getCollegeId())) {
                    return false;
                }
                if (request.getCategoryId() != null && !Objects.equals(resource.getCategoryId(), request.getCategoryId())) {
                    return false;
                }
                if (StringUtils.hasText(request.getFileType()) && !resource.getFileType().equals(request.getFileType())) {
                    return false;
                }
                if (request.getStatus() != null && !Objects.equals(resource.getStatus(), request.getStatus())) {
                    return false;
                }
                if (request.getMinSize() != null && resource.getFileSize() < request.getMinSize() * 1024 * 1024) {
                    return false;
                }
                if (request.getMaxSize() != null && resource.getFileSize() > request.getMaxSize() * 1024 * 1024) {
                    return false;
                }
                
                // 日期筛选
                if (StringUtils.hasText(request.getStartDate())) {
                    try {
                        Date startDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(request.getStartDate());
                        if (resource.getCreateTime().before(startDate)) {
                            return false;
                        }
                    } catch (Exception e) {
                        log.error("日期解析错误: {}", e.getMessage());
                    }
                }
                if (StringUtils.hasText(request.getEndDate())) {
                    try {
                        Date endDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(request.getEndDate());
                        // 设置为当天结束时间
                        endDate.setHours(23);
                        endDate.setMinutes(59);
                        endDate.setSeconds(59);
                        if (resource.getCreateTime().after(endDate)) {
                            return false;
                        }
                    } catch (Exception e) {
                        log.error("日期解析错误: {}", e.getMessage());
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // 排序处理
        String sort = request.getSort();
        if (StringUtils.hasText(sort)) {
            switch (sort) {
                case "downloads":
                    filteredResources.sort((r1, r2) -> r2.getDownloadCount() - r1.getDownloadCount());
                    break;
                case "views":
                    filteredResources.sort((r1, r2) -> r2.getViewCount() - r1.getViewCount());
                    break;
                case "likes":
                    filteredResources.sort((r1, r2) -> r2.getLikeCount() - r1.getLikeCount());
                    break;
                default:
                    // 默认按创建时间降序排序
                    filteredResources.sort((r1, r2) -> r2.getCreateTime().compareTo(r1.getCreateTime()));
            }
        }
        
        // 分页处理
        int page = request.getPage() != null ? request.getPage() : 1;
        int size = request.getSize() != null ? request.getSize() : 10;
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, filteredResources.size());
        
        List<TResource> pageResources = fromIndex < filteredResources.size() ? 
                filteredResources.subList(fromIndex, toIndex) : 
                List.of();
        
        // 转换为DTO
        List<AdminResourceDTO> resourceDTOs = pageResources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageResult<>(resourceDTOs, filteredResources.size());
    }

    @Override
    @Transactional
    public Long addResource(AdminResourceDTO resourceDTO) {
        log.info("添加资源: {}", resourceDTO);
        
        TResource resource = new TResource();
        
        // 从DTO复制属性
        resource.setTitle(resourceDTO.getTitle());
        resource.setDescription(resourceDTO.getDescription());
        resource.setFileUrl(resourceDTO.getFileUrl());
        resource.setFileType(resourceDTO.getFileType());
        resource.setFileSize(resourceDTO.getFileSize());
        resource.setDownloadCount(0);
        resource.setViewCount(0);
        resource.setLikeCount(0);
        resource.setUploaderId(resourceDTO.getUploaderId());
        resource.setCollegeId(resourceDTO.getCollegeId());
        resource.setMajorId(resourceDTO.getMajorId());
        resource.setCourseId(resourceDTO.getCourseId());
        resource.setCategoryId(resourceDTO.getCategoryId());
        resource.setTags(resourceDTO.getTags());
        resource.setStatus(resourceDTO.getStatus());
        resource.setIsDeleted(resourceDTO.getIsDeleted() != null ? resourceDTO.getIsDeleted() : 0);
        
        // 设置创建时间和更新时间
        Date now = new Date();
        resource.setCreateTime(now);
        resource.setUpdateTime(now);
        
        // 补充名称信息
        this.complementResourceNames(resource);
        
        // 保存到数据库
        resourceMapper.insert(resource);
        
        // 同步到ES
        try {
            resourceSyncService.syncToElasticsearch(resource);
        } catch (Exception e) {
            log.error("同步资源到ES失败: {}", e.getMessage());
        }
        
        return resource.getId();
    }

    @Override
    @Transactional
    public boolean updateResource(AdminResourceDTO resourceDTO) {
        log.info("更新资源: {}", resourceDTO);
        
        if (resourceDTO.getId() == null) {
            log.warn("更新资源ID为空");
            return false;
        }
        
        // 先查询现有资源
        TResource existingResource = resourceMapper.selectById(resourceDTO.getId());
        if (existingResource == null) {
            log.warn("资源不存在: id={}", resourceDTO.getId());
            return false;
        }
        
        // 从DTO更新字段
        existingResource.setTitle(resourceDTO.getTitle());
        existingResource.setDescription(resourceDTO.getDescription());
        existingResource.setFileUrl(resourceDTO.getFileUrl());
        existingResource.setFileType(resourceDTO.getFileType());
        existingResource.setFileSize(resourceDTO.getFileSize());
        existingResource.setUploaderId(resourceDTO.getUploaderId());
        existingResource.setCollegeId(resourceDTO.getCollegeId());
        existingResource.setMajorId(resourceDTO.getMajorId());
        existingResource.setCourseId(resourceDTO.getCourseId());
        existingResource.setCategoryId(resourceDTO.getCategoryId());
        existingResource.setTags(resourceDTO.getTags());
        existingResource.setStatus(resourceDTO.getStatus());
        
        // 设置更新时间
        existingResource.setUpdateTime(new Date());
        
        // 补充名称信息
        this.complementResourceNames(existingResource);
        
        // 保存更新
        int rows = resourceMapper.updateById(existingResource);
        
        // 同步到ES
        if (rows > 0) {
            try {
                resourceSyncService.syncToElasticsearch(existingResource);
            } catch (Exception e) {
                log.error("同步资源到ES失败: {}", e.getMessage());
            }
        }
        
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean updateResourceStatus(Long id, Integer isDeleted) {
        log.info("更新资源状态: id={}, isDeleted={}", id, isDeleted);
        
        if (id == null || isDeleted == null) {
            return false;
        }
        
        // 更新数据库
        int rows = resourceMapper.updateResourceStatus(id,isDeleted,new Date());
        TResource resource = resourceMapper.selectByIdWithDeleted(id);
        if (resource == null) {
            log.warn("资源不存在: id={}", id);
            return false;
        }

        // 同步到ES
        if (rows > 0) {
            try {
                if (isDeleted == 1) {
                    // 从ES中逻辑删除
                    resourceSyncService.syncToElasticsearch(resource);
                } else {
                    // 恢复到ES
                    resourceSyncService.syncToElasticsearch(resource);
                }
            } catch (Exception e) {
                log.error("同步资源状态到ES失败: {}", e.getMessage());
            }
        }
        
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean deleteResource(Long id) {
        log.info("物理删除资源: id={}", id);
        
        if (id == null) {
            return false;
        }
        
        // 先从ES删除
        try {
            resourceSyncService.removeFromElasticsearch(id);
        } catch (Exception e) {
            log.error("从ES删除资源失败: {}", e.getMessage());
        }
        
        // 从数据库删除
        int rows = resourceMapper.updateResourceStatus(id,1,new Date());
        
        return rows > 0;
    }

    @Override
    public AdminResourceDTO convertToDTO(TResource resource) {
        if (resource == null) {
            return null;
        }
        
        AdminResourceDTO dto = new AdminResourceDTO();
        BeanUtils.copyProperties(resource, dto);
        
        // 补充上传者名称
        if (resource.getUploaderId() != null) {
            TUser uploader = userMapper.selectById(resource.getUploaderId());
            if (uploader != null) {
                dto.setUploaderName(uploader.getUsername());
            }
        }
        
        return dto;
    }
    
    /**
     * 补充资源的名称信息
     */
    private void complementResourceNames(TResource resource) {
        // 补充学院名称
        if (resource.getCollegeId() != null) {
            TCollege college = collegeMapper.selectById(resource.getCollegeId());
            if (college != null) {
                resource.setCollegeName(college.getName());
            }
        }
        
        // 补充分类名称
        if (resource.getCategoryId() != null) {
            TCategory category = categoryMapper.selectById(resource.getCategoryId());
            if (category != null) {
                resource.setCategoryName(category.getName());
            }
        }
        
        // 其他名称补充（专业名称、课程名称等）可按需添加
    }
}