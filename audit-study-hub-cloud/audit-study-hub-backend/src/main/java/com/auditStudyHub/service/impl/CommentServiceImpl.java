package com.auditStudyHub.service.impl;

import com.auditStudyHub.dto.CommentDTO;
import com.auditStudyHub.entity.TResourceComment;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.mapper.TResourceCommentMapper;
import com.auditStudyHub.mapper.TUserMapper;
import com.auditStudyHub.service.CommentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    
    private final TResourceCommentMapper commentMapper;
    private final TUserMapper userMapper;
    
    @Override
    public List<CommentDTO> getCommentsByResourceId(Long resourceId) {
        if (resourceId == null) {
            return new ArrayList<>();
        }
        
        try {
            // 使用自定义方法获取所有评论（包括已删除的）
            List<TResourceComment> allComments = commentMapper.selectCommentsByResourceId(resourceId);
            
            
            if (allComments.isEmpty()) {
                return new ArrayList<>();
            }
            
            log.info("获取资源评论: resourceId={}, 总评论数={}, 包含已删除评论", 
                resourceId, allComments.size());
            
            // 转换为DTO并组织评论树
            return buildCommentTree(allComments);
        } catch (Exception e) {
            log.error("获取资源评论失败: resourceId={}", resourceId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Long getCommentCountByResourceId(Long resourceId) {
        if (resourceId == null) {
            return 0L;
        }
        
        try {
            // 查询条件：资源ID、未删除的评论
            LambdaQueryWrapper<TResourceComment> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(TResourceComment::getResourceId, resourceId)
                      .eq(TResourceComment::getIsDeleted, 0);
            
            return commentMapper.selectCount(queryWrapper);
        } catch (Exception e) {
            log.error("获取评论数量失败: resourceId={}", resourceId, e);
            return 0L;
        }
    }
    
    @Override
    @Transactional
    public CommentDTO addComment(CommentDTO commentDTO) {
        if (commentDTO == null || commentDTO.getResourceId() == null || 
            commentDTO.getUserId() == null || commentDTO.getContent() == null) {
            throw new IllegalArgumentException("评论信息不完整");
        }
        
        try {
            // 创建评论实体
            TResourceComment comment = new TResourceComment();
            comment.setResourceId(commentDTO.getResourceId());
            comment.setUserId(commentDTO.getUserId());
            comment.setContent(commentDTO.getContent());
            comment.setParentId(commentDTO.getParentId());
            
            Date now = new Date();
            comment.setCreateTime(now);
            comment.setUpdateTime(now);
            comment.setIsDeleted(0);
            
            // 保存评论
            commentMapper.insert(comment);
            
            // 获取评论用户信息
            TUser user = userMapper.selectById(commentDTO.getUserId());
            
            // 转换为DTO
            CommentDTO resultDTO = convertToDTO(comment);
            
            if (user != null) {
                resultDTO.setUsername(user.getUsername());
                resultDTO.setUserAvatar(user.getAvatar());
            }
            
            // 如果是回复评论，获取被回复用户的用户名
            if (comment.getParentId() != null) {
                TResourceComment parentComment = commentMapper.selectById(comment.getParentId());
                if (parentComment != null) {
                    TUser replyToUser = userMapper.selectById(parentComment.getUserId());
                    if (replyToUser != null) {
                        resultDTO.setReplyToUsername(replyToUser.getUsername());
                    }
                }
            }
            
            return resultDTO;
        } catch (Exception e) {
            log.error("添加评论失败", e);
            throw new RuntimeException("添加评论失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean deleteComment(Long id, Long userId) {
        if (id == null || userId == null) {
            return false;
        }
        
        try {
            // 查询评论
            TResourceComment comment = commentMapper.selectById(id);
            
            if (comment == null || comment.getIsDeleted() == 1) {
                return false;
            }
            
            // 检查权限（只有评论作者可以删除）
            if (!comment.getUserId().equals(userId)) {
                log.warn("用户无权删除此评论: userId={}, commentId={}", userId, id);
                return false;
            }
            
            // 使用UpdateWrapper直接更新is_deleted字段
            LambdaUpdateWrapper<TResourceComment> updateWrapper = Wrappers.lambdaUpdate(TResourceComment.class)
                .eq(TResourceComment::getId, id)
                .set(TResourceComment::getIsDeleted, 1)
                .set(TResourceComment::getUpdateTime, new Date());
                
            // 执行更新
            return commentMapper.update(null, updateWrapper) > 0;
        } catch (Exception e) {
            log.error("删除评论失败: id={}", id, e);
            return false;
        }
    }
    
    /**
     * 将评论实体转换为DTO
     */
    private CommentDTO convertToDTO(TResourceComment comment) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);
        
        // 获取评论用户信息
        TUser user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setUserAvatar(user.getAvatar());
        }
        
        return dto;
    }
    
    /**
     * 构建评论树
     */
    private List<CommentDTO> buildCommentTree(List<TResourceComment> comments) {
        // 转换为DTO
        List<CommentDTO> dtoList = comments.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        // 用户ID集合
        List<Long> userIds = dtoList.stream()
            .map(CommentDTO::getUserId)
            .distinct()
            .collect(Collectors.toList());
        
        // 批量查询用户信息
        Map<Long, TUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<TUser> userQueryWrapper = Wrappers.lambdaQuery();
            userQueryWrapper.in(TUser::getId, userIds);
            
            List<TUser> users = userMapper.selectList(userQueryWrapper);
            userMap = users.stream()
                .collect(Collectors.toMap(TUser::getId, user -> user));
        }
        
        // 填充用户信息
        for (CommentDTO dto : dtoList) {
            TUser user = userMap.get(dto.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setUserAvatar(user.getAvatar());
            }
            
            // 标记已删除的评论
            if (dto.getIsDeleted() == 1) {
                // 将内容设置为已删除标识，但保留结构
                dto.setContent("该评论已被删除");
            }
        }
        
        // 构建评论树
        Map<Long, List<CommentDTO>> commentMap = dtoList.stream()
            .filter(dto -> dto.getParentId() != null)
            .collect(Collectors.groupingBy(CommentDTO::getParentId));
        
        // 查找回复对象的用户名
        for (CommentDTO dto : dtoList) {
            if (dto.getParentId() != null) {
                CommentDTO parentDto = dtoList.stream()
                    .filter(d -> d.getId().equals(dto.getParentId()))
                    .findFirst()
                    .orElse(null);
                
                if (parentDto != null) {
                    dto.setReplyToUsername(parentDto.getUsername());
                }
            }
            
            // 设置子评论
            dto.setChildren(commentMap.getOrDefault(dto.getId(), new ArrayList<>()));
        }
        
        // 过滤出顶层评论
        return dtoList.stream()
            .filter(dto -> dto.getParentId() == null)
            .collect(Collectors.toList());
    }
} 