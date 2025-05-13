package com.auditStudyHub.service;

import com.auditStudyHub.dto.CommentDTO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {
    
    /**
     * 获取资源评论列表
     * @param resourceId 资源ID
     * @return 评论列表
     */
    List<CommentDTO> getCommentsByResourceId(Long resourceId);
    
    /**
     * 获取资源评论总数
     * @param resourceId 资源ID
     * @return 评论总数
     */
    Long getCommentCountByResourceId(Long resourceId);
    
    /**
     * 添加评论
     * @param commentDTO 评论信息
     * @return 添加后的评论信息
     */
    CommentDTO addComment(CommentDTO commentDTO);
    
    /**
     * 删除评论
     * @param id 评论ID
     * @param userId 用户ID（校验权限）
     * @return 是否成功
     */
    boolean deleteComment(Long id, Long userId);
} 