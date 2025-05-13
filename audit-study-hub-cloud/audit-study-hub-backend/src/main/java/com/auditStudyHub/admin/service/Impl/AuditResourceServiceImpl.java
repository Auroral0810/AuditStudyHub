package com.auditStudyHub.admin.service.Impl;

import com.auditStudyHub.admin.dto.AdminResourceDTO;
import com.auditStudyHub.admin.dto.AuditRequestDTO;
import com.auditStudyHub.admin.dto.AuditResourceDTO;
import com.auditStudyHub.admin.dto.AuditResourceSearchRequest;
import com.auditStudyHub.admin.service.AuditResourceService;
import com.auditStudyHub.admin.service.Impl.AdminResourceSyncServiceImpl;
import com.auditStudyHub.document.ResourceDocument;
import com.auditStudyHub.dto.PageResult;
import com.auditStudyHub.entity.TResource;
import com.auditStudyHub.mapper.TResourceMapper;
import com.auditStudyHub.repository.es.ResourceRepository;
import com.auditStudyHub.service.impl.ResourceSyncService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditResourceServiceImpl implements AuditResourceService {

    private final TResourceMapper resourceMapper;
    private final ResourceRepository resourceRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ResourceSyncService resourceSyncService;
    private final AdminResourceSyncServiceImpl adminResourceSyncService;

    @Override
    public List<AuditResourceDTO> getPendingAuditResources() {
        log.info("获取待审核资源列表");
        
        try {
            // 首先尝试从ES获取
            NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
            
            // 只查询未删除且状态为待审核的资源
            queryBuilder.withQuery(q -> q.bool(b -> b
                .must(m -> m.term(t -> t.field("status").value(0)))
                .must(m -> m.term(t -> t.field("isDeleted").value(0)))
            ));
            
            // 按上传时间倒序
            queryBuilder.withSort(Sort.by(Sort.Direction.DESC, "createTime"));
            
            // 查询前100条
            queryBuilder.withPageable(PageRequest.of(0, 100));
            
            NativeQuery searchQuery = queryBuilder.build();
            SearchHits<ResourceDocument> searchHits = 
                elasticsearchOperations.search(searchQuery, ResourceDocument.class);
            
            if (searchHits.getTotalHits() > 0) {
                log.info("从ES获取到{}条待审核资源", searchHits.getTotalHits());
                return searchHits.getSearchHits().stream()
                    .map(hit -> convertToAuditDTO(hit.getContent()))
                    .collect(Collectors.toList());
            }
            
            // 如果ES中没有数据，则从MySQL获取并同步到ES
            log.info("ES中无待审核数据，从MySQL获取");
            LambdaQueryWrapper<TResource> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(TResource::getStatus, 0)
                       .eq(TResource::getIsDeleted, 0)
                       .orderByDesc(TResource::getCreateTime)
                       .last("LIMIT 100");
            
            List<TResource> resources = resourceMapper.selectList(queryWrapper);
            
            // 同步到ES
            adminResourceSyncService.batchSyncToElasticsearch(resources);
            
            // 返回结果
            return resources.stream()
                .map(this::convertToAuditDTO)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("获取待审核资源失败", e);
            
            // 直接从MySQL获取
            LambdaQueryWrapper<TResource> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(TResource::getStatus, 0)
                       .eq(TResource::getIsDeleted, 0)
                       .orderByDesc(TResource::getCreateTime)
                       .last("LIMIT 100");
            
            List<TResource> resources = resourceMapper.selectList(queryWrapper);
            
            return resources.stream()
                .map(this::convertToAuditDTO)
                .collect(Collectors.toList());
        }
    }

    @Override
    public PageResult<AuditResourceDTO> searchAuditResources(AuditResourceSearchRequest request) {
        log.info("搜索审核资源: request={}", request);
        
        try {
            // 检查ES中是否有数据
            long count = resourceRepository.count();
            if (count == 0) {
                log.info("ES中无数据，进行全量同步");
                List<TResource> resources = resourceMapper.selectList(null);
                adminResourceSyncService.batchSyncToElasticsearch(resources);
            }
            
            // 构建ES查询
            NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
            
            // 构建布尔查询
            queryBuilder.withQuery(q -> q.bool(b -> {
                // 必须是未删除的资源
                b.must(m -> m.term(t -> t.field("isDeleted").value(0)));
                
                // 添加过滤条件
                if (StringUtils.hasText(request.getTitle())) {
                    b.must(m -> m.match(match -> match
                        .field("title")
                        .query(request.getTitle())
                        .fuzziness("AUTO")
                    ));
                }
                
                if (request.getCollegeId() != null) {
                    b.must(m -> m.term(t -> t.field("collegeId").value(request.getCollegeId())));
                }
                
                if (request.getCategoryId() != null) {
                    b.must(m -> m.term(t -> t.field("categoryId").value(request.getCategoryId())));
                }
                
                if (StringUtils.hasText(request.getFileType())) {
                    b.must(m -> m.term(t -> t.field("fileType").value(request.getFileType())));
                }
                
                if (request.getStatus() != null) {
                    b.must(m -> m.term(t -> t.field("status").value(request.getStatus())));
                }
                
                return b;
            }));
            
            // 设置分页
            int pageIndex = Math.max(0, request.getPage() - 1);
            queryBuilder.withPageable(PageRequest.of(pageIndex, request.getSize()));
            
            // 设置排序
            queryBuilder.withSort(Sort.by(Sort.Direction.DESC, "createTime"));
            
            // 执行查询
            NativeQuery searchQuery = queryBuilder.build();
            SearchHits<ResourceDocument> searchHits = 
                elasticsearchOperations.search(searchQuery, ResourceDocument.class);
            
            // 转换结果
            List<AuditResourceDTO> resources = searchHits.getSearchHits().stream()
                .map(hit -> convertToAuditDTO(hit.getContent()))
                .collect(Collectors.toList());
            
            return new PageResult<>(resources, searchHits.getTotalHits());
            
        } catch (Exception e) {
            log.error("从ES搜索审核资源失败，降级到MySQL查询", e);
            return searchFromMySQL(request);
        }
    }

    /**
     * 从MySQL中搜索资源（降级方案）
     */
    private PageResult<AuditResourceDTO> searchFromMySQL(AuditResourceSearchRequest request) {
        log.info("从MySQL搜索审核资源");
        
        // 构建查询条件
        LambdaQueryWrapper<TResource> queryWrapper = Wrappers.lambdaQuery();
        
        // 只查询未删除的资源
        queryWrapper.eq(TResource::getIsDeleted, 0);
        
        // 添加过滤条件
        if (StringUtils.hasText(request.getTitle())) {
            queryWrapper.like(TResource::getTitle, request.getTitle());
        }
        
        if (request.getCollegeId() != null) {
            queryWrapper.eq(TResource::getCollegeId, request.getCollegeId());
        }
        
        if (request.getCategoryId() != null) {
            queryWrapper.eq(TResource::getCategoryId, request.getCategoryId());
        }
        
        if (StringUtils.hasText(request.getFileType())) {
            queryWrapper.eq(TResource::getFileType, request.getFileType());
        }
        
        if (request.getStatus() != null) {
            queryWrapper.eq(TResource::getStatus, request.getStatus());
        }
        
        // 按创建时间排序
        queryWrapper.orderByDesc(TResource::getCreateTime);
        
        // 执行分页查询
        Page<TResource> page = new Page<>(request.getPage(), request.getSize());
        Page<TResource> resultPage = resourceMapper.selectPage(page, queryWrapper);
        
        // 转换结果
        List<AuditResourceDTO> resources = resultPage.getRecords().stream()
            .map(this::convertToAuditDTO)
            .collect(Collectors.toList());
        
        return new PageResult<>(resources, resultPage.getTotal());
    }

    @Override
    @Transactional
    public boolean batchAuditResources(AuditRequestDTO request) {
        log.info("批量审核资源: resourceIds={}, auditResult={}", request.getResourceIds(), request.getAuditResult());
        
        if (request.getResourceIds() == null || request.getResourceIds().isEmpty()) {
            log.warn("资源ID列表为空，无法审核");
            return false;
        }
        
        if (request.getAuditResult() == null || (request.getAuditResult() != 1 && request.getAuditResult() != 2)) {
            log.warn("审核结果无效: {}", request.getAuditResult());
            return false;
        }
        
        try {
            // 批量更新MySQL中的资源状态
            LambdaUpdateWrapper<TResource> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.in(TResource::getId, request.getResourceIds())
                       .set(TResource::getStatus, request.getAuditResult())
                       .set(TResource::getUpdateTime, new Date());
            
            int updatedCount = resourceMapper.update(null, updateWrapper);
            log.info("已更新{}条资源状态", updatedCount);
            
            // 同步到ES
            for (Long resourceId : request.getResourceIds()) {
                // 从MySQL获取最新数据
                TResource resource = resourceMapper.selectById(resourceId);
                if (resource != null) {
                    adminResourceSyncService.syncToElasticsearch(resource);
                }
            }
            
            return updatedCount > 0;
            
        } catch (Exception e) {
            log.error("批量审核资源失败", e);
            throw new RuntimeException("批量审核资源失败", e);
        }
    }

    @Override
    public AdminResourceDTO getResourceDetail(Long id) {
        log.info("获取资源详情: id={}", id);
        
        try {
            // 尝试从ES获取
            var documentOpt = resourceRepository.findById(id);
            
            if (documentOpt.isPresent()) {
                // 从ES中获取到数据
                ResourceDocument document = documentOpt.get();
                AdminResourceDTO dto = new AdminResourceDTO();
                BeanUtils.copyProperties(document, dto);
                return dto;
            }
            
            // 从MySQL获取
            TResource resource = resourceMapper.selectById(id);
            if (resource == null) {
                return null;
            }
            
            // 同步到ES
            adminResourceSyncService.syncToElasticsearch(resource);
            
            // 转换返回
            AdminResourceDTO dto = new AdminResourceDTO();
            BeanUtils.copyProperties(resource, dto);
            return dto;
            
        } catch (Exception e) {
            log.error("获取资源详情失败: id=" + id, e);
            
            // 直接从MySQL获取
            TResource resource = resourceMapper.selectById(id);
            if (resource == null) {
                return null;
            }
            
            AdminResourceDTO dto = new AdminResourceDTO();
            BeanUtils.copyProperties(resource, dto);
            return dto;
        }
    }
    
    /**
     * 将ES文档转换为审核DTO
     */
    private AuditResourceDTO convertToAuditDTO(ResourceDocument document) {
        if (document == null) {
            return null;
        }
        
        AuditResourceDTO dto = new AuditResourceDTO();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setCategoryName(document.getCategoryName());
        dto.setFileType(document.getFileType());
        dto.setCollegeName(document.getCollegeName());
        dto.setStatus(document.getStatus());
        dto.setCreateTime(document.getCreateTime());
        
        return dto;
    }
    
    /**
     * 将MySQL实体转换为审核DTO
     */
    private AuditResourceDTO convertToAuditDTO(TResource resource) {
        if (resource == null) {
            return null;
        }
        
        AuditResourceDTO dto = new AuditResourceDTO();
        dto.setId(resource.getId());
        dto.setTitle(resource.getTitle());
        dto.setCategoryName(resource.getCategoryName());
        dto.setFileType(resource.getFileType());
        dto.setCollegeName(resource.getCollegeName());
        dto.setStatus(resource.getStatus());
        dto.setCreateTime(resource.getCreateTime());
        
        return dto;
    }
} 