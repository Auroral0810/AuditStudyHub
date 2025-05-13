package com.auditStudyHub.admin.service;

import com.auditStudyHub.admin.dto.AdminCommentDTO;
import com.auditStudyHub.admin.dto.AdminCommentSearchRequest;
import com.auditStudyHub.dto.PageResult;

import java.util.List;

public interface AdminCommentService {
    
    /**
     * 获取所有评论
     * @return 评论列表
     */
    List<AdminCommentDTO> getAllComments();
    
    /**
     * 根据ID获取评论详情
     * @param id 评论ID
     * @return 评论详情
     */
    AdminCommentDTO getCommentById(Long id);
    
    /**
     * 搜索评论
     * @param request 搜索请求
     * @return 分页评论结果
     */
    PageResult<AdminCommentDTO> searchComments(AdminCommentSearchRequest request);
    
    /**
     * 更新评论状态（逻辑删除或恢复）
     * @param id 评论ID
     * @param isDeleted 是否删除，0-未删除，1-已删除
     * @return 是否成功
     */
    boolean updateCommentStatus(Long id, Integer isDeleted);
    
    /**
     * 删除评论
     * @param id 评论ID
     * @return 是否成功
     */
    boolean deleteComment(Long id);
} 