package com.auditStudyHub.admin.service;

import com.auditStudyHub.admin.dto.AdminRequestDTO;
import com.auditStudyHub.admin.dto.AdminRequestSearchRequest;
import com.auditStudyHub.dto.PageResult;

import java.util.List;

public interface AdminRequestService {
    
    /**
     * 获取所有资源请求
     * 
     * @return 资源请求列表
     */
    List<AdminRequestDTO> getAllRequests();
    
    /**
     * 根据ID获取资源请求
     * 
     * @param id 请求ID
     * @return 资源请求DTO
     */
    AdminRequestDTO getRequestById(Long id);
    
    /**
     * 搜索资源请求
     * 
     * @param request 搜索条件
     * @return 分页结果
     */
    PageResult<AdminRequestDTO> searchRequests(AdminRequestSearchRequest request);
    
    /**
     * 创建资源请求
     * 
     * @param requestDTO 请求信息
     * @return 创建后的资源请求
     */
    AdminRequestDTO createRequest(AdminRequestDTO requestDTO);
    
    /**
     * 更新资源请求
     * 
     * @param requestDTO 请求信息
     * @return 更新后的资源请求
     */
    AdminRequestDTO updateRequest(AdminRequestDTO requestDTO);
    
    /**
     * 更新资源请求状态
     * 
     * @param id 请求ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateRequestStatus(Long id, Integer status);
    
    /**
     * 更新资源请求删除状态 (逻辑删除或恢复)
     * 
     * @param id 请求ID
     * @param isDeleted 是否删除 (0-正常, 1-已删除)
     * @return 是否成功
     */
    boolean updateDeleteStatus(Long id, Integer isDeleted);
    
    /**
     * 物理删除资源请求
     * 
     * @param id 请求ID
     * @return 是否成功
     */
    boolean deleteRequest(Long id);
} 