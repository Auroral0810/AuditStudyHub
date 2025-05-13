package com.auditStudyHub.service;

import com.auditStudyHub.dto.*;

import java.util.List;

public interface ResourceRequestService {
    
    /**
     * 获取资源请求列表
     */
    PageResult<ResourceRequestDTO> getResourceRequestList(
            Integer page, Integer size, String keyword, Long collegeId, Long majorId, 
            Integer status, String startDate, String endDate, String sort);
    
    /**
     * 根据搜索请求获取资源请求列表
     */
    PageResult<ResourceRequestDTO> searchResourceRequests(ResourceRequestSearchRequest searchRequest);
    
    /**
     * 获取资源请求详情
     */
    ResourceRequestDetailDTO getResourceRequestDetail(Long id);
    
    /**
     * 增加资源请求浏览次数
     */
    boolean incrementViewCount(Long id);
    
    /**
     * 添加回复并增加回复计数
     */
    boolean addReplyAndIncrementCount(Long requestId);
    
    /**
     * 删除评论并减少评论计数
     */
    boolean decrementReplyCount(Long requestId);
    
    /**
     * 根据用户ID获取其发布的资源请求列表
     */
    PageResult<ResourceRequestDTO> getUserResourceRequests(Long userId, Integer page, Integer size);
    
    /**
     * 创建资源请求
     * 
     * @param request 创建资源请求的请求参数
     * @param userId 当前登录用户ID
     * @return 创建的资源请求信息
     */
    ResourceRequestDTO createRequest(ResourceRequestCreateRequest request, Long userId);

    /**
     * 获取用户发布的资源请求简要信息列表
     */
    PageResult<ResourceRequestSimpleDTO> getUserResourceRequestsSimple(Long userId, Integer current, Integer size);

    /**
     * 删除资源请求
     * @param id 资源请求ID
     * @param userId 用户ID（用于权限验证）
     * @return 删除是否成功
     */
    boolean deleteRequest(Long id, Long userId);
     /**
     * 更新资源请求
     * 
     * @param id 资源请求ID
     * @param request 更新请求数据
     * @param userId 用户ID（用于权限验证）
     * @return 更新后的资源请求，如果无权限或请求不存在则返回null
     */
    ResourceRequestDTO updateRequest(Long id, ResourceRequestUpdateRequest request, Long userId);
}
