package com.auditStudyHub.service.impl;

import com.auditStudyHub.document.ResourceRequestDocument;
import com.auditStudyHub.entity.TResourceRequest;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.mapper.TResourceRequestMapper;
import com.auditStudyHub.mapper.TUserMapper;
import com.auditStudyHub.repository.es.ResourceRequestRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 负责资源请求文档管理和同步
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceRequestSyncService {
    
    private final TResourceRequestMapper resourceRequestMapper;
    private final TUserMapper userMapper;
    private final ResourceRequestRepository resourceRequestRepository;
    
    /**
     * 将资源请求同步到ES
     */
    public void syncToElasticsearch(TResourceRequest request) {
        if (request == null) {
            return;
        }
        
        try {
            ResourceRequestDocument document = convertToDocument(request);
            resourceRequestRepository.save(document);
            log.info("资源请求已同步到ES: ID={}", request.getId());
        } catch (Exception e) {
            log.error("同步资源请求到ES失败: ID=" + request.getId(), e);
        }
    }
    
    /**
     * 批量同步到ES
     */
    public void batchSyncToElasticsearch(List<TResourceRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }
        
        try {
            List<ResourceRequestDocument> documents = requests.stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());
            
            resourceRequestRepository.saveAll(documents);
            log.info("批量同步资源请求到ES完成，共{}条数据", requests.size());
        } catch (Exception e) {
            log.error("批量同步资源请求到ES失败", e);
        }
    }
    
    /**
     * 从ES删除资源请求
     */
    public void removeFromElasticsearch(Long requestId) {
        try {
            resourceRequestRepository.deleteById(requestId);
            log.info("从ES删除资源请求: ID={}", requestId);
        } catch (Exception e) {
            log.error("从ES删除资源请求失败: ID=" + requestId, e);
        }
    }
    
    /**
     * 将MySQL实体转换为ES文档
     */
    private ResourceRequestDocument convertToDocument(TResourceRequest request) {
        ResourceRequestDocument document = new ResourceRequestDocument();
        BeanUtils.copyProperties(request, document);
        
        // 如果请求者ID存在，尝试获取请求者名称
        if (request.getUserId() != null) {
            try {
                TUser user = userMapper.selectById(request.getUserId());
                if (user != null) {
                    document.setUsername(user.getUsername());
                }
            } catch (Exception e) {
                log.error("获取请求者名称失败: userId={}", request.getUserId(), e);
            }
        }
        
        return document;
    }
    
    /**
     * 应用启动时初始化索引
     */
    @PostConstruct
    public void initializeIndex() {
        log.info("检查是否需要初始化资源请求ES索引...");
        try {
            // 检查ES中是否已有数据
            long count = resourceRequestRepository.count();
            if (count == 0) {
                log.info("资源请求ES索引为空，开始初始化索引...");
                fullSync();
            } else {
                log.info("资源请求ES索引已存在，包含{}条数据", count);
            }
        } catch (Exception e) {
            log.error("初始化资源请求ES索引失败", e);
        }
    }
    
    /**
     * 全量同步数据，每天凌晨执行
     */
    @Scheduled(cron = "0 30 2 * * ?") // 每天凌晨2点30分执行
    public void fullSync() {
        log.info("开始全量同步资源请求数据到ES...");
        
        try {
            // 查询所有未删除的资源请求
            LambdaQueryWrapper<TResourceRequest> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(TResourceRequest::getIsDeleted, 0);
            
            List<TResourceRequest> requests = resourceRequestMapper.selectList(queryWrapper);
            log.info("查询到{}条资源请求需要同步", requests.size());
            
            // 清空当前索引
            resourceRequestRepository.deleteAll();
            
            // 重新写入所有数据
            batchSyncToElasticsearch(requests);
            
            log.info("全量同步完成，共同步{}条数据", requests.size());
        } catch (Exception e) {
            log.error("全量同步资源请求失败", e);
        }
    }
    
    /**
     * 同步浏览次数增加
     */
    public void syncViewCount(Long requestId, Integer newCount) {
        try {
            Optional<ResourceRequestDocument> documentOpt = resourceRequestRepository.findById(requestId);
            if (!documentOpt.isPresent()) {
                TResourceRequest request = resourceRequestMapper.selectById(requestId);
                if (request != null) {
                    syncToElasticsearch(request);
                }
                return;
            }
            
            ResourceRequestDocument document = documentOpt.get();
            document.setViewCount(newCount);
            resourceRequestRepository.save(document);
            log.debug("资源请求浏览次数已同步到ES: requestId={}, count={}", requestId, newCount);
        } catch (Exception e) {
            log.error("同步资源请求浏览次数到ES失败: requestId=" + requestId, e);
        }
    }
    
    /**
     * 同步回复次数增加
     */
    public void syncReplyCount(Long requestId, Integer newCount) {
        try {
            Optional<ResourceRequestDocument> documentOpt = resourceRequestRepository.findById(requestId);
            if (!documentOpt.isPresent()) {
                TResourceRequest request = resourceRequestMapper.selectById(requestId);
                if (request != null) {
                    syncToElasticsearch(request);
                }
                return;
            }
            
            ResourceRequestDocument document = documentOpt.get();
            document.setReplyCount(newCount);
            resourceRequestRepository.save(document);
            log.debug("资源请求回复次数已同步到ES: requestId={}, count={}", requestId, newCount);
        } catch (Exception e) {
            log.error("同步资源请求回复次数到ES失败: requestId=" + requestId, e);
        }
    }
}
