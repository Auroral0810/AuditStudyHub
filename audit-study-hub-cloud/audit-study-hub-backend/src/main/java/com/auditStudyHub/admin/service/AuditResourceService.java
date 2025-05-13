package com.auditStudyHub.admin.service;

import com.auditStudyHub.admin.dto.AdminResourceDTO;
import com.auditStudyHub.admin.dto.AuditRequestDTO;
import com.auditStudyHub.admin.dto.AuditResourceDTO;
import com.auditStudyHub.admin.dto.AuditResourceSearchRequest;
import com.auditStudyHub.dto.PageResult;

import java.util.List;

public interface AuditResourceService {
    
    /**
     * 获取待审核资源列表
     */
    List<AuditResourceDTO> getPendingAuditResources();
    
    /**
     * 根据条件搜索资源
     */
    PageResult<AuditResourceDTO> searchAuditResources(AuditResourceSearchRequest request);
    
    /**
     * 批量审核资源
     */
    boolean batchAuditResources(AuditRequestDTO request);
    
    /**
     * 获取资源详情
     */
    AdminResourceDTO getResourceDetail(Long id);
} 