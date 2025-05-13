package com.auditStudyHub.service;

import com.auditStudyHub.dto.ResourceRequestCommentDTO;
import com.auditStudyHub.dto.ResourceRequestCommentAddRequest;

/**
 * 资源请求评论服务接口
 * 
 * @author auroral
 */
public interface ResourceRequestCommentService {
    
    /**
     * 添加评论
     *
     * @param request 评论请求
     * @return 评论DTO
     */
    ResourceRequestCommentDTO addComment(ResourceRequestCommentAddRequest request);
    
    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId 用户ID（用于权限验证）
     * @return 是否删除成功
     */
    boolean deleteComment(Long commentId, Long userId);
} 