package com.auditStudyHub.service.impl;

import com.auditStudyHub.dto.ResourceRequestCommentDTO;
import com.auditStudyHub.dto.ResourceRequestCommentAddRequest;
import com.auditStudyHub.entity.TResourceRequestReply;
import com.auditStudyHub.entity.TResourceRequest;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.mapper.TResourceRequestReplyMapper;
import com.auditStudyHub.mapper.TResourceRequestMapper;
import com.auditStudyHub.mapper.TUserMapper;
import com.auditStudyHub.service.ResourceRequestCommentService;
import com.auditStudyHub.service.ResourceRequestService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 资源请求评论服务实现类
 * 
 * @author auroral
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceRequestCommentServiceImpl implements ResourceRequestCommentService {
    
    private final TResourceRequestReplyMapper resourceRequestReplyMapper;
    private final TResourceRequestMapper resourceRequestMapper;
    private final TUserMapper userMapper;
    private final ResourceRequestService resourceRequestService;
    
    @Override
    @Transactional
    public ResourceRequestCommentDTO addComment(ResourceRequestCommentAddRequest request) {
        if (request == null || request.getRequestId() == null || 
            request.getUserId() == null || request.getContent() == null || 
            request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("评论信息不完整");
        }
        
        try {
            // 验证资源请求是否存在
            TResourceRequest resourceRequest = resourceRequestMapper.selectById(request.getRequestId());
            if (resourceRequest == null || resourceRequest.getIsDeleted() == 1) {
                throw new IllegalArgumentException("资源请求不存在或已被删除");
            }
            
            // 验证用户是否存在
            TUser user = userMapper.selectById(request.getUserId());
            if (user == null || user.getIsDeleted() == 1) {
                throw new IllegalArgumentException("用户不存在或已被禁用");
            }
            
            // 创建评论
            TResourceRequestReply reply = new TResourceRequestReply();
            reply.setRequestId(request.getRequestId());
            reply.setUserId(request.getUserId());
            reply.setContent(request.getContent().trim());
            reply.setParentId(request.getParentId());
            
            // 设置创建时间和更新时间
            Date now = new Date();
            reply.setCreateTime(now);
            reply.setUpdateTime(now);
            reply.setIsDeleted(0);
            
            // 保存评论
            resourceRequestReplyMapper.insert(reply);
            
            // 增加资源请求的评论计数
            resourceRequestService.addReplyAndIncrementCount(request.getRequestId());
            
            // 转换为DTO
            ResourceRequestCommentDTO commentDTO = new ResourceRequestCommentDTO();
            BeanUtils.copyProperties(reply, commentDTO);
            
            // 设置用户信息
            commentDTO.setUsername(user.getUsername());
            commentDTO.setUserAvatar(user.getAvatar());
            
            // 如果是回复评论，获取被回复用户的信息
            if (reply.getParentId() != null) {
                TResourceRequestReply parentReply = resourceRequestReplyMapper.selectById(reply.getParentId());
                if (parentReply != null) {
                    TUser replyToUser = userMapper.selectById(parentReply.getUserId());
                    if (replyToUser != null) {
                        commentDTO.setReplyToUsername(replyToUser.getUsername());
                    }
                }
            }
            
            return commentDTO;
        } catch (Exception e) {
            log.error("添加资源请求评论失败", e);
            throw new RuntimeException("添加评论失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            return false;
        }
        
        try {
            // 查询评论
            TResourceRequestReply reply = resourceRequestReplyMapper.selectById(commentId);
            
            if (reply == null || reply.getIsDeleted() == 1) {
                return false;
            }
            
            // 检查权限（只有评论作者可以删除）
            if (!reply.getUserId().equals(userId)) {
                log.warn("用户无权删除此评论: userId={}, commentId={}", userId, commentId);
                return false;
            }
            
            // 保存requestId用于后续减少计数
            Long requestId = reply.getRequestId();
            
            // 使用UpdateWrapper直接更新is_deleted字段
            LambdaUpdateWrapper<TResourceRequestReply> updateWrapper = Wrappers.lambdaUpdate(TResourceRequestReply.class)
                .eq(TResourceRequestReply::getId, commentId)
                .set(TResourceRequestReply::getIsDeleted, 1)
                .set(TResourceRequestReply::getUpdateTime, new Date());
                
            // 执行更新
            boolean success = resourceRequestReplyMapper.update(null, updateWrapper) > 0;
            
            // 如果成功删除评论，减少评论计数并同步至ES
            if (success && requestId != null) {
                resourceRequestService.decrementReplyCount(requestId);
                log.info("评论删除成功，已减少评论计数: commentId={}, requestId={}", commentId, requestId);
            }
            
            return success;
        } catch (Exception e) {
            log.error("删除资源请求评论失败: id={}", commentId, e);
            return false;
        }
    }
} 