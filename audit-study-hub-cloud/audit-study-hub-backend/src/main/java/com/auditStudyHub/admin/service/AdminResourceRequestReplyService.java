package com.auditStudyHub.admin.service;

import com.auditStudyHub.admin.dto.AdminResourceRequestReplyDTO;
import com.auditStudyHub.admin.dto.AdminResourceRequestReplySearchRequest;
import com.auditStudyHub.dto.PageResult;

import java.util.List;

/**
 * 管理员资源请求回复服务接口
 */
public interface AdminResourceRequestReplyService {

    /**
     * 获取所有请求回复
     * @return 请求回复列表
     */
    List<AdminResourceRequestReplyDTO> getAllReplies();
    
    /**
     * 根据ID获取请求回复详情
     * @param id 请求回复ID
     * @return 请求回复详情
     */
    AdminResourceRequestReplyDTO getReplyById(Long id);
    
    /**
     * 搜索请求回复
     * @param request 搜索条件
     * @return 分页结果
     */
    PageResult<AdminResourceRequestReplyDTO> searchReplies(AdminResourceRequestReplySearchRequest request);
    
    /**
     * 添加请求回复
     * @param dto 请求回复信息
     * @return 新增的请求回复ID
     */
    Long addReply(AdminResourceRequestReplyDTO dto);
    
    /**
     * 更新请求回复内容
     * @param id 请求回复ID
     * @param content 新的内容
     * @return 是否成功
     */
    boolean updateReplyContent(Long id, String content);
    
    /**
     * 更新请求回复状态
     * @param id 请求回复ID
     * @param isDeleted 是否删除：0-未删除，1-已删除
     * @return 是否成功
     */
    boolean updateReplyStatus(Long id, Integer isDeleted);
    
    /**
     * 删除请求回复（物理删除）
     * @param id 请求回复ID
     * @return 是否成功
     */
    boolean deleteReply(Long id);
} 