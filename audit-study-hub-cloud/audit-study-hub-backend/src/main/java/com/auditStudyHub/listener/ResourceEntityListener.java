package com.auditStudyHub.listener;

import com.auditStudyHub.entity.TResource;
import com.auditStudyHub.service.impl.ResourceSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

/**
 * 资源实体监听器，用于同步数据到Elasticsearch
 */
@Slf4j
@Component
public class ResourceEntityListener {
    
    private static ResourceSyncService resourceSyncService;
    
    @Autowired
    public void setResourceSyncService(ResourceSyncService service) {
        ResourceEntityListener.resourceSyncService = service;
    }
    
    /**
     * 资源保存后，同步到ES
     */
    @PostPersist
    public void postPersist(TResource resource) {
        log.debug("资源保存后触发同步: ID={}", resource.getId());
        resourceSyncService.syncToElasticsearch(resource);
    }
    
    /**
     * 资源更新后，同步到ES
     */
    @PostUpdate
    public void postUpdate(TResource resource) {
        log.debug("资源更新后触发同步: ID={}", resource.getId());
        
        // 如果资源已删除，则从ES中删除
        if (resource.getIsDeleted() != null && resource.getIsDeleted() == 1) {
            resourceSyncService.removeFromElasticsearch(resource.getId());
        } else {
            resourceSyncService.syncToElasticsearch(resource);
        }
    }
    
    /**
     * 资源删除后，从ES中删除
     */
    @PostRemove
    public void postRemove(TResource resource) {
        log.debug("资源删除后触发同步: ID={}", resource.getId());
        resourceSyncService.removeFromElasticsearch(resource.getId());
    }
}
