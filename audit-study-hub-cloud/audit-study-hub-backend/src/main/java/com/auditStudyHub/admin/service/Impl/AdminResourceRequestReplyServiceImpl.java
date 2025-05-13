package com.auditStudyHub.admin.service.Impl;

import com.auditStudyHub.admin.dto.AdminResourceRequestReplyDTO;
import com.auditStudyHub.admin.dto.AdminResourceRequestReplySearchRequest;
import com.auditStudyHub.admin.service.AdminResourceRequestReplyService;
import com.auditStudyHub.dto.PageResult;
import com.auditStudyHub.entity.TResource;
import com.auditStudyHub.entity.TResourceRequest;
import com.auditStudyHub.entity.TResourceRequestReply;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.mapper.TResourceMapper;
import com.auditStudyHub.mapper.TResourceRequestMapper;
import com.auditStudyHub.mapper.TResourceRequestReplyMapper;
import com.auditStudyHub.mapper.TUserMapper;
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
public class AdminResourceRequestReplyServiceImpl implements AdminResourceRequestReplyService {

    private final TResourceRequestReplyMapper replyMapper;
    private final TResourceRequestMapper requestMapper;
    private final TResourceMapper resourceMapper;
    private final TUserMapper userMapper;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public List<AdminResourceRequestReplyDTO> getAllReplies() {
        log.info("获取所有资源请求回复");
        
        List<TResourceRequestReply> replies = replyMapper.selectAllIncludeDeleted();
        
        return replies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AdminResourceRequestReplyDTO getReplyById(Long id) {
        log.info("获取资源请求回复详情: id={}", id);
        
        TResourceRequestReply reply = replyMapper.selectByIdWithDeleted(id);
        if (reply == null) {
            log.warn("资源请求回复不存在: id={}", id);
            return null;
        }
        
        return convertToDTO(reply);
    }

    @Override
    public PageResult<AdminResourceRequestReplyDTO> searchReplies(AdminResourceRequestReplySearchRequest request) {
        log.info("搜索资源请求回复: request={}", request);
        
        try {
            // 使用自定义查询方法
            List<TResourceRequestReply> replies = replyMapper.searchAllIncludeDeleted(
                request.getContent(),
                request.getStartDate(),
                request.getEndDate(),
                request.getIsDeleted(),
                request.getParentId()
            );
            
            // 转换为DTO并应用过滤条件
            List<AdminResourceRequestReplyDTO> replyDTOs = replies.stream()
                .map(this::convertToDTO)
                // 过滤回复内容
                .filter(dto -> {
                    if (StringUtils.hasText(request.getReplyContent())) {
                        return dto.getReplyContent() != null && 
                            dto.getReplyContent().contains(request.getReplyContent());
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
                // 过滤资源名称
                .filter(dto -> {
                    if (StringUtils.hasText(request.getResourceName())) {
                        return dto.getResourceName() != null && 
                            dto.getResourceName().contains(request.getResourceName());
                    }
                    return true;
                })
                .collect(Collectors.toList());
            
            // 分页处理
            int total = replyDTOs.size();
            int page = request.getPage() != null ? request.getPage() : 1;
            int size = request.getSize() != null ? request.getSize() : 10;
            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, total);
            
            List<AdminResourceRequestReplyDTO> pageData = fromIndex < total 
                ? replyDTOs.subList(fromIndex, toIndex) 
                : new ArrayList<>();
            
            return new PageResult<>(pageData, total);
            
        } catch (Exception e) {
            log.error("搜索资源请求回复失败", e);
            return new PageResult<>(new ArrayList<>(), 0);
        }
    }

    @Override
    @Transactional
    public Long addReply(AdminResourceRequestReplyDTO dto) {
        log.info("添加资源请求回复: {}", dto);
        
        TResourceRequestReply reply = new TResourceRequestReply();
        reply.setRequestId(dto.getRequestId());
        reply.setUserId(dto.getUserId());
        reply.setContent(dto.getContent());
        reply.setParentId(dto.getParentId());
        reply.setResourceId(dto.getResourceId());
        reply.setCreateTime(new Date());
        reply.setUpdateTime(new Date());
        reply.setIsDeleted(0);
        
        replyMapper.insert(reply);
        
        return reply.getId();
    }

    @Override
    @Transactional
    public boolean updateReplyContent(Long id, String content) {
        log.info("更新资源请求回复内容: id={}, content={}", id, content);
        
        try {
            int rows = replyMapper.updateReplyContent(id, content, new Date());
            return rows > 0;
        } catch (Exception e) {
            log.error("更新资源请求回复内容失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateReplyStatus(Long id, Integer isDeleted) {
        log.info("更新资源请求回复状态: id={}, isDeleted={}", id, isDeleted);
        
        try {
            int rows = replyMapper.updateReplyStatus(id, isDeleted, new Date());
            return rows > 0;
        } catch (Exception e) {
            log.error("更新资源请求回复状态失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteReply(Long id) {
        log.info("删除资源请求回复: id={}", id);
        
        try {
            int rows = replyMapper.deleteById(id);
            return rows > 0;
        } catch (Exception e) {
            log.error("删除资源请求回复失败", e);
            return false;
        }
    }
    
    /**
     * 将实体转换为DTO
     */
    private AdminResourceRequestReplyDTO convertToDTO(TResourceRequestReply reply) {
        if (reply == null) {
            return null;
        }
        
        AdminResourceRequestReplyDTO dto = new AdminResourceRequestReplyDTO();
        
        // 复制基本字段
        dto.setId(reply.getId());
        dto.setRequestId(reply.getRequestId());
        dto.setContent(reply.getContent());
        dto.setUserId(reply.getUserId());
        dto.setReplyContent(reply.getContent()); // 回复内容就是content字段
        dto.setResourceId(reply.getResourceId());
        dto.setParentId(reply.getParentId());
        dto.setCreateTime(reply.getCreateTime());
        dto.setReplyTime(reply.getUpdateTime()); // 回复时间使用更新时间
        dto.setIsDeleted(reply.getIsDeleted());
        
        // 设置用户名称
        if (reply.getUserId() != null) {
            TUser user = userMapper.selectById(reply.getUserId());
            if (user != null) {
                dto.setUserName(user.getUsername());
            }
        }
        
        // 设置资源名称
        if (reply.getResourceId() != null) {
            TResource resource = resourceMapper.selectById(reply.getResourceId());
            if (resource != null) {
                dto.setResourceName(resource.getTitle());
            }
        }
        
        // 设置父回复内容
        if (reply.getParentId() != null) {
            TResourceRequestReply parentReply = replyMapper.selectByIdWithDeleted(reply.getParentId());
            if (parentReply != null) {
                dto.setParentContent(parentReply.getContent());
            }
        }
        
        return dto;
    }
} 