package com.auditStudyHub.admin.service;

import com.auditStudyHub.dto.ESSyncMessage;
import com.auditStudyHub.entity.TResource;
import java.util.List;

/**
 * 资源同步服务接口
 */
public interface AdminResourceSyncService {

    /**
     * 将资源同步到ES
     */
    void syncToElasticsearch(TResource resource);
    
    /**
     * 批量同步到ES
     */
    void batchSyncToElasticsearch(List<TResource> resources);
    
    /**
     * 从ES删除资源
     */
    void removeFromElasticsearch(Long resourceId);
    
    /**
     * 全量同步数据
     */
    void fullSync();
    
    /**
     * 异步发送资源计数同步消息
     */
    void sendResourceCountSyncMessage(Long resourceId, ESSyncMessage.SyncType syncType, Integer valueChange);
    
    /**
     * 同步指定资源的单个计数字段到ES
     */
    void syncResourceCountField(Long resourceId, ESSyncMessage.SyncType syncType);
    
    /**
     * 同步指定资源到ES
     */
    void syncResourceToES(Long resourceId, ESSyncMessage.SyncType syncType);
} 