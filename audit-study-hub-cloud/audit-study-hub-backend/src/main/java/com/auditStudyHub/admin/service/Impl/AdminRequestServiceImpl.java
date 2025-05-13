package com.auditStudyHub.admin.service.Impl;

import com.auditStudyHub.admin.dto.AdminRequestDTO;
import com.auditStudyHub.admin.dto.AdminRequestSearchRequest;
import com.auditStudyHub.admin.service.AdminRequestService;
import com.auditStudyHub.dto.PageResult;
import com.auditStudyHub.entity.TResource;
import com.auditStudyHub.entity.TResourceRequest;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.mapper.TResourceMapper;
import com.auditStudyHub.mapper.TResourceRequestMapper;
import com.auditStudyHub.mapper.TUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRequestServiceImpl implements AdminRequestService {

    private final TResourceRequestMapper requestMapper;
    private final TUserMapper userMapper;
    private final TResourceMapper resourceMapper;

    @Override
    public List<AdminRequestDTO> getAllRequests() {
        log.info("获取所有资源请求");
        
        // 使用自定义Mapper方法获取所有资源请求，包括已删除的
        List<TResourceRequest> requests = requestMapper.selectAllIncludeDeleted();
        
        return requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AdminRequestDTO getRequestById(Long id) {
        log.info("获取资源请求详情: id={}", id);
        
        // 使用自定义Mapper方法获取资源请求，包括已删除的
        TResourceRequest request = requestMapper.selectByIdWithDeleted(id);
        
        if (request == null) {
            log.warn("资源请求不存在: id={}", id);
            return null;
        }
        
        return convertToDTO(request);
    }

    @Override
    public PageResult<AdminRequestDTO> searchRequests(AdminRequestSearchRequest request) {
        log.info("搜索资源请求: request={}", request);
        
        // 使用自定义Mapper方法搜索资源请求
        List<TResourceRequest> requests = requestMapper.searchAllIncludeDeleted(
            request.getTitle(),
            request.getContent(),
            request.getCollegeId(),
            request.getMajorId(),
            request.getCategoryId(),
            request.getStatus(),
            request.getIsDeleted(),
            request.getStartDate(),
            request.getEndDate()
        );
        
        // 转换为DTO
        List<AdminRequestDTO> requestDTOs = requests.stream()
                .map(this::convertToDTO)
                // 如果请求了用户名筛选，对结果进行过滤
                .filter(dto -> {
                    if (StringUtils.hasText(request.getUserName())) {
                        return dto.getUserName() != null && 
                               dto.getUserName().contains(request.getUserName());
                    }
                    return true;
                })
                .collect(Collectors.toList());
        
        // 分页处理
        int page = request.getPage() != null ? request.getPage() : 1;
        int size = request.getSize() != null ? request.getSize() : 10;
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, requestDTOs.size());
        
        // 构建分页结果
        int total = requestDTOs.size();
        List<AdminRequestDTO> pageData = fromIndex < total 
                ? requestDTOs.subList(fromIndex, toIndex) 
                : new ArrayList<>();
        
        return new PageResult<>(pageData, total);
    }

    @Override
    @Transactional
    public boolean updateRequestStatus(Long id, Integer status) {
        log.info("更新资源请求状态: id={}, status={}", id, status);
        
        // 使用自定义Mapper方法更新状态
        Date updateTime = new Date();
        int rows = requestMapper.updateRequestStatus(id, status, updateTime);
        
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean updateDeleteStatus(Long id, Integer isDeleted) {
        log.info("更新资源请求删除状态: id={}, isDeleted={}", id, isDeleted);
        
        // 使用自定义Mapper方法更新删除状态
        Date updateTime = new Date();
        int rows = requestMapper.updateDeleteStatus(id, isDeleted, updateTime);
        
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean deleteRequest(Long id) {
        log.info("物理删除资源请求: id={}", id);
        
        // 使用MyBatis-Plus提供的删除方法
        return requestMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public AdminRequestDTO createRequest(AdminRequestDTO requestDTO) {
        log.info("创建资源请求: {}", requestDTO);
        
        // 创建资源请求实体
        TResourceRequest request = new TResourceRequest();
        
        // 从DTO复制基本属性
        request.setTitle(requestDTO.getTitle());
        request.setContent(requestDTO.getContent());
        request.setUserId(requestDTO.getUserId());
        request.setCollegeId(requestDTO.getCollegeId());
        request.setCollegeName(requestDTO.getCollegeName());
        request.setMajorId(requestDTO.getMajorId());
        request.setMajorName(requestDTO.getMajorName());
        request.setCourseId(requestDTO.getCourseId());
        request.setCourseName(requestDTO.getCourseName());
        request.setCategoryId(requestDTO.getCategoryId());
        request.setCategoryName(requestDTO.getCategoryName());
        
        // 设置默认值
        request.setStatus(0); // 未解决
        request.setReplyCount(0);
        request.setViewCount(0);
        request.setCreateTime(new Date());
        request.setUpdateTime(new Date());
        request.setIsDeleted(0); // 正常状态
        
        // 保存到数据库
        requestMapper.insert(request);
        
        // 返回完整的DTO
        return convertToDTO(request);
    }

    @Override
    @Transactional
    public AdminRequestDTO updateRequest(AdminRequestDTO requestDTO) {
        log.info("更新资源请求: {}", requestDTO);
        
        // 验证请求是否存在
        TResourceRequest existingRequest = requestMapper.selectByIdWithDeleted(requestDTO.getId());
        if (existingRequest == null) {
            log.warn("资源请求不存在: id={}", requestDTO.getId());
            return null;
        }
        
        // 更新基本属性
        existingRequest.setTitle(requestDTO.getTitle());
        existingRequest.setContent(requestDTO.getContent());
        existingRequest.setCollegeId(requestDTO.getCollegeId());
        existingRequest.setCollegeName(requestDTO.getCollegeName());
        existingRequest.setMajorId(requestDTO.getMajorId());
        existingRequest.setMajorName(requestDTO.getMajorName());
        existingRequest.setCategoryId(requestDTO.getCategoryId());
        existingRequest.setCategoryName(requestDTO.getCategoryName());
        existingRequest.setStatus(requestDTO.getStatus());
        existingRequest.setUpdateTime(new Date());
        
        // 保存更新
        requestMapper.updateById(existingRequest);
        
        // 返回更新后的结果
        return convertToDTO(existingRequest);
    }
    
    /**
     * 将实体转换为DTO，并补充额外字段
     * 
     * @param request 资源请求实体
     * @return 资源请求DTO
     */
    private AdminRequestDTO convertToDTO(TResourceRequest request) {
        if (request == null) {
            return null;
        }
        
        AdminRequestDTO dto = new AdminRequestDTO();
        BeanUtils.copyProperties(request, dto);
        
        // 设置用户名称
        if (request.getUserId() != null) {
            TUser user = userMapper.selectById(request.getUserId());
            if (user != null) {
                dto.setUserName(user.getUsername());
            }
        }
        
        // 设置资源名称（如果关联了资源）
        // 这里假设资源请求中可能有resourceId字段，如果没有可以移除这部分代码
        if (dto.getResourceId() != null) {
            TResource resource = resourceMapper.selectById(dto.getResourceId());
            if (resource != null) {
                dto.setResourceTitle(resource.getTitle());
            }
        }
        
        // 父评论内容默认为空，因为资源请求通常不存在父评论关系
        // 如果有需要可以在这里设置
        dto.setParentContent("");
        
        return dto;
    }
} 