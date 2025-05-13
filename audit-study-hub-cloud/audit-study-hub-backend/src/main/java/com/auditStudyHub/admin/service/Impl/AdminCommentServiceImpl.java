package com.auditStudyHub.admin.service.Impl;

import com.auditStudyHub.admin.dto.AdminCommentDTO;
import com.auditStudyHub.admin.dto.AdminCommentSearchRequest;
import com.auditStudyHub.admin.service.AdminCommentService;
import com.auditStudyHub.dto.PageResult;
import com.auditStudyHub.entity.TResource;
import com.auditStudyHub.entity.TResourceComment;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.mapper.TResourceCommentMapper;
import com.auditStudyHub.mapper.TResourceMapper;
import com.auditStudyHub.mapper.TUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCommentServiceImpl implements AdminCommentService {

    private final TResourceCommentMapper commentMapper;
    private final TResourceMapper resourceMapper;
    private final TUserMapper userMapper;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public List<AdminCommentDTO> getAllComments() {
        log.info("获取所有评论");
        
        // 使用新的Mapper方法获取所有评论，包括已删除的
        List<TResourceComment> comments = commentMapper.selectAllIncludeDeleted();
        
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AdminCommentDTO getCommentById(Long id) {
        log.info("获取评论详情: id={}", id);
        
        // 使用新的Mapper方法获取评论详情，包括已删除的
        TResourceComment comment = commentMapper.selectByIdWithDeleted(id);
        
        if (comment == null) {
            log.warn("评论不存在: id={}", id);
            return null;
        }
        
        return convertToDTO(comment);
    }

    @Override
    public PageResult<AdminCommentDTO> searchComments(AdminCommentSearchRequest request) {
        log.info("搜索评论: request={}", request);
        
        // 使用自定义方法查询评论
        List<TResourceComment> comments = commentMapper.searchAllIncludeDeleted(
            request.getContent(), 
            request.getStartDate(), 
            request.getEndDate(),
            request.getIsDeleted(),
            request.getParentId()
        );
        
        // 分页处理
        int page = request.getPage() != null ? request.getPage() : 1;
        int size = request.getSize() != null ? request.getSize() : 10;
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, comments.size());
        
        // 转换并过滤结果
        List<AdminCommentDTO> commentDTOs = comments.stream()
                .map(this::convertToDTO)
                // 过滤资源标题
                .filter(dto -> {
                    if (StringUtils.hasText(request.getResourceTitle())) {
                        return dto.getResourceTitle() != null && 
                               dto.getResourceTitle().contains(request.getResourceTitle());
                    }
                    return true;
                })
                // 过滤用户名称
                .filter(dto -> {
                    if (StringUtils.hasText(request.getUserName())) {
                        return dto.getUserName() != null && 
                               dto.getUserName().contains(request.getUserName());
                    }
                    return true;
                })
                .collect(Collectors.toList());
        
        // 应用分页
        int total = commentDTOs.size();
        List<AdminCommentDTO> pageData = fromIndex < total 
                ? commentDTOs.subList(fromIndex, toIndex) 
                : new ArrayList<>();
        
        // 返回结果
        return new PageResult<>(pageData, total);
    }

    @Override
    @Transactional
    public boolean updateCommentStatus(Long id, Integer isDeleted) {
        log.info("更新评论状态: id={}, isDeleted={}", id, isDeleted);
        
        // 使用自定义方法更新评论状态，不考虑当前状态
        Date updateTime = new Date();
        int rows = commentMapper.updateCommentStatus(id, isDeleted, updateTime);
        
        if (rows > 0) {
            log.info("成功更新评论状态: id={}, isDeleted={}", id, isDeleted);
            return true;
        } else {
            log.warn("评论不存在或更新失败: id={}", id);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteComment(Long id) {
        log.info("删除评论: id={}", id);
        return commentMapper.deleteById(id) > 0;
    }
    
    /**
     * 将评论实体转换为DTO
     */
    private AdminCommentDTO convertToDTO(TResourceComment comment) {
        if (comment == null) {
            return null;
        }
        
        AdminCommentDTO dto = new AdminCommentDTO();
        BeanUtils.copyProperties(comment, dto);
        
        // 设置资源ID和资源标题
        if (comment.getResourceId() != null) {
            dto.setResourceId(comment.getResourceId());
            TResource resource = resourceMapper.selectById(comment.getResourceId());
            if (resource != null) {
                dto.setResourceTitle(resource.getTitle());
            }
        }
        
        // 设置用户ID和用户名称
        if (comment.getUserId() != null) {
            dto.setUserId(comment.getUserId());
            TUser user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                dto.setUserName(user.getUsername());
            }
        }
        
        // 设置父评论ID和父评论内容
        if (comment.getParentId() != null) {
            dto.setParentId(comment.getParentId());
            TResourceComment parentComment = commentMapper.selectById(comment.getParentId());
            if (parentComment != null) {
                dto.setParentContent(parentComment.getContent());
            }
        }
        
        return dto;
    }
} 