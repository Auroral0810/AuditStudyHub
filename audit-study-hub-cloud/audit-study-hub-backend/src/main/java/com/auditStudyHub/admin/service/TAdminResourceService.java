package com.auditStudyHub.admin.service;

import com.auditStudyHub.admin.dto.AdminResourceDTO;
import com.auditStudyHub.admin.dto.AdminResourceSearchRequest;
import com.auditStudyHub.dto.PageResult;
import com.auditStudyHub.entity.TResource;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 管理员资源服务接口
 */
public interface TAdminResourceService {
    
    /**
     * 获取所有资源列表（包括已删除的）
     * 
     * @return 资源DTO列表
     */
    List<AdminResourceDTO> getAllResources();
    
    /**
     * 根据ID获取资源详情
     * 
     * @param id 资源ID
     * @return 资源DTO
     */
    AdminResourceDTO getResourceById(Long id);
    
    /**
     * 高级搜索资源
     * 
     * @param request 搜索请求
     * @return 分页结果
     */
    PageResult<AdminResourceDTO> searchResources(AdminResourceSearchRequest request);
    
    /**
     * 添加资源
     * 
     * @param resourceDTO 资源DTO
     * @return 新增的资源ID
     */
    Long addResource(AdminResourceDTO resourceDTO);
    
    /**
     * 更新资源
     * 
     * @param resourceDTO 资源DTO
     * @return 是否成功
     */
    boolean updateResource(AdminResourceDTO resourceDTO);
    
    /**
     * 更新资源状态（逻辑删除或恢复）
     * 
     * @param id 资源ID
     * @param isDeleted 是否删除（0-正常，1-已删除）
     * @return 是否成功
     */
    boolean updateResourceStatus(Long id, Integer isDeleted);
    
    /**
     * 物理删除资源
     * 
     * @param id 资源ID
     * @return 是否成功
     */
    boolean deleteResource(Long id);
    
    /**
     * 通过资源实体对象转换为DTO
     */
    AdminResourceDTO convertToDTO(TResource resource);
}