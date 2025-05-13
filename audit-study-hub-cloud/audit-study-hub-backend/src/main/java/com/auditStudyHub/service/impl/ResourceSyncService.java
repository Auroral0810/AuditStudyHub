package com.auditStudyHub.service.impl;

import com.auditStudyHub.config.RabbitMQConfig;
import com.auditStudyHub.document.ResourceDocument;
import com.auditStudyHub.dto.ESSyncMessage;
import com.auditStudyHub.entity.TResource;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.mapper.TResourceMapper;
import com.auditStudyHub.mapper.TUserMapper;
import com.auditStudyHub.repository.es.ResourceRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 负责资源文档管理和同步
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceSyncService {
    
    private final TResourceMapper resourceMapper;
    private final TUserMapper userMapper;
    private final ResourceRepository resourceRepository;
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * 将资源同步到ES
     */
    public void syncToElasticsearch(TResource resource) {
        if (resource == null) {
            return;
        }
        
        try {
            ResourceDocument document = convertToDocument(resource);
            resourceRepository.save(document);
            log.info("资源已同步到ES: ID={}", resource.getId());
        } catch (Exception e) {
            log.error("同步资源到ES失败: ID=" + resource.getId(), e);
        }
    }
    
    /**
     * 批量同步到ES
     */
    public void batchSyncToElasticsearch(List<TResource> resources) {
        if (resources == null || resources.isEmpty()) {
            return;
        }
        
        try {
            List<ResourceDocument> documents = resources.stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());
            
            resourceRepository.saveAll(documents);
            log.info("批量同步到ES完成，共{}条数据", resources.size());
        } catch (Exception e) {
            log.error("批量同步到ES失败", e);
        }
    }
    
    /**
     * 从ES删除资源
     */
    public void removeFromElasticsearch(Long resourceId) {
        try {
            resourceRepository.deleteById(resourceId);
            log.info("从ES删除资源: ID={}", resourceId);
        } catch (Exception e) {
            log.error("从ES删除资源失败: ID=" + resourceId, e);
        }
    }
    
    /**
     * 将MySQL实体转换为ES文档
     */
    private ResourceDocument convertToDocument(TResource resource) {
        ResourceDocument document = new ResourceDocument();
        BeanUtils.copyProperties(resource, document);
        
        // 如果上传者ID存在，尝试获取上传者名称
        if (resource.getUploaderId() != null) {
            try {
                TUser uploader = userMapper.selectById(resource.getUploaderId());
                if (uploader != null) {
                    document.setUploaderName(uploader.getUsername());
                }
            } catch (Exception e) {
                log.error("获取上传者名称失败: uploaderId={}", resource.getUploaderId(), e);
            }
        }
        
        return document;
    }
    
    /**
     * 应用启动时初始化索引
     */
    @PostConstruct
    public void initializeIndex() {
        log.info("检查是否需要初始化ES索引...");
        try {
            // 检查ES中是否已有数据
            long count = resourceRepository.count();
            if (count == 0) {
                log.info("ES索引为空，开始初始化索引...");
                fullSync();
            } else {
                log.info("ES索引已存在，包含{}条数据", count);
            }
        } catch (Exception e) {
            log.error("初始化ES索引失败", e);
        }
    }
    
    /**
     * 全量同步数据，每天凌晨执行
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void fullSync() {
        log.info("开始全量同步数据到ES...");
        
        try {
            // 查询所有资源（包括已删除的）
            List<TResource> resources = resourceMapper.selectAllWithDeleted();
            log.info("查询到{}条资源需要同步", resources.size());

            // 清空当前索引
            resourceRepository.deleteAll();

            log.info("全量同步完成，共同步{}条数据", resources.size());
        } catch (Exception e) {
            log.error("全量同步失败", e);
        }
    }
    
    /**
     * 异步发送资源计数同步消息
     * 
     * @param resourceId 资源ID
     * @param syncType 同步类型
     * @param valueChange 值变化（增量）
     */
    public void sendResourceCountSyncMessage(Long resourceId, ESSyncMessage.SyncType syncType, Integer valueChange) {
        try {
            ESSyncMessage message = ESSyncMessage.builder()
                    .resourceId(resourceId)
                    .syncType(syncType)
                    .valueChange(valueChange)
                    .build();
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.AUDIT_STUDY_EXCHANGE,
                    RabbitMQConfig.ES_SYNC_ROUTING_KEY,
                    message
            );
            
            log.debug("发送资源计数同步消息: resourceId={}, syncType={}, valueChange={}", 
                     resourceId, syncType, valueChange);
        } catch (Exception e) {
            log.error("发送ES同步消息失败: resourceId=" + resourceId, e);
        }
    }
    
    /**
     * 监听ES同步队列，处理资源计数同步消息
     */
    @RabbitListener(queues = RabbitMQConfig.ES_SYNC_QUEUE)
    public void handleESSyncMessage(ESSyncMessage message) {
        if (message == null || message.getResourceId() == null) {
            log.warn("收到无效的ES同步消息");
            return;
        }
        
        log.debug("处理ES同步消息: resourceId={}, syncType={}", 
                 message.getResourceId(), message.getSyncType());
        
        try {
            // 获取资源ID
            Long resourceId = message.getResourceId();
            
            // 查询ES中是否存在该文档
            Optional<ResourceDocument> documentOpt = resourceRepository.findById(resourceId);
            
            if (!documentOpt.isPresent()) {
                // 如果ES中不存在该文档，尝试从MySQL获取并进行全量同步
                TResource resource = resourceMapper.selectById(resourceId);
                if (resource != null) {
                    syncToElasticsearch(resource);
                } else {
                    log.warn("资源不存在，无法同步: resourceId={}", resourceId);
                }
                return;
            }
            
            ResourceDocument document = documentOpt.get();
            
            // 判断同步类型，进行相应处理
            if (message.getSyncType() == ESSyncMessage.SyncType.FULL_SYNC) {
                // 全量同步
                TResource resource = resourceMapper.selectById(resourceId);
                if (resource != null) {
                    syncToElasticsearch(resource);
                }
            } else {
                // 部分字段增量更新
                switch (message.getSyncType()) {
                    case VIEW_COUNT:
                        if (message.getValueChange() != null) {
                            // 增量更新
                            document.setViewCount(document.getViewCount() + message.getValueChange());
                        } else {
                            // 从数据库获取最新值
                            TResource resource = resourceMapper.selectById(resourceId);
                            if (resource != null) {
                                document.setViewCount(resource.getViewCount());
                            }
                        }
                        break;
                    case DOWNLOAD_COUNT:
                        if (message.getValueChange() != null) {
                            document.setDownloadCount(document.getDownloadCount() + message.getValueChange());
                        } else {
                            TResource resource = resourceMapper.selectById(resourceId);
                            if (resource != null) {
                                document.setDownloadCount(resource.getDownloadCount());
                            }
                        }
                        break;
                    case LIKE_COUNT:
                        if (message.getValueChange() != null) {
                            document.setLikeCount(document.getLikeCount() + message.getValueChange());
                        } else {
                            TResource resource = resourceMapper.selectById(resourceId);
                            if (resource != null) {
                                document.setLikeCount(resource.getLikeCount());
                            }
                        }
                        break;
                    default:
                        log.warn("未知的同步类型: {}", message.getSyncType());
                        return;
                }
                
                // 保存更新后的文档
                resourceRepository.save(document);
                log.debug("ES文档计数字段已更新: resourceId={}, syncType={}", 
                         resourceId, message.getSyncType());
            }
        } catch (Exception e) {
            log.error("处理ES同步消息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 同步指定资源的单个计数字段到ES
     * 
     * @param resourceId 资源ID
     * @param syncType 同步类型
     */
    public void syncResourceCountField(Long resourceId, ESSyncMessage.SyncType syncType) {
        try {
            // 从MySQL获取最新数据
            TResource resource = resourceMapper.selectById(resourceId);
            if (resource == null) {
                log.warn("资源不存在，无法同步计数字段: resourceId={}", resourceId);
                return;
            }
            
            // 查询ES中是否存在该文档
            Optional<ResourceDocument> documentOpt = resourceRepository.findById(resourceId);
            
            if (!documentOpt.isPresent()) {
                // 如果ES中不存在该文档，进行全量同步
                syncToElasticsearch(resource);
                return;
            }
            
            ResourceDocument document = documentOpt.get();
            
            // 根据同步类型更新相应字段
            switch (syncType) {
                case VIEW_COUNT:
                    document.setViewCount(resource.getViewCount());
                    break;
                case DOWNLOAD_COUNT:
                    document.setDownloadCount(resource.getDownloadCount());
                    break;
                case LIKE_COUNT:
                    document.setLikeCount(resource.getLikeCount());
                    break;
                default:
                    log.warn("未知的同步类型: {}", syncType);
                    return;
            }
            
            // 保存更新后的文档
            resourceRepository.save(document);
            log.debug("ES文档计数字段已直接同步: resourceId={}, syncType={}", resourceId, syncType);
        } catch (Exception e) {
            log.error("同步计数字段到ES失败: resourceId=" + resourceId, e);
        }
    }
    
    /**
     * 同步指定资源到ES
     * 
     * @param resourceId 资源ID
     * @param syncType 同步类型
     */
    public void syncResourceToES(Long resourceId, ESSyncMessage.SyncType syncType) {
        try {
            // 从MySQL获取最新数据
            TResource resource = resourceMapper.selectById(resourceId);
            if (resource == null) {
                log.warn("资源不存在，无法同步: resourceId={}", resourceId);
                return;
            }
            
            // 同步到ES，移除之前审核状态的过滤
            syncToElasticsearch(resource);
            log.info("资源已同步到ES: resourceId={}, syncType={}", resourceId, syncType);
        } catch (Exception e) {
            log.error("同步资源到ES失败: resourceId={}, syncType={}", resourceId, syncType, e);
        }
    }
}
