package com.auditStudyHub.service;

import com.auditStudyHub.dto.PageResult;
import com.auditStudyHub.dto.ResourceDTO;
import com.auditStudyHub.dto.ResourceDetailDTO;
import com.auditStudyHub.dto.LikeResultDTO;
import com.auditStudyHub.dto.CollectResultDTO;
import com.auditStudyHub.dto.UserLikeDTO;
import com.auditStudyHub.dto.UserCollectionDTO;
import com.auditStudyHub.dto.UserDownloadDTO;
import com.auditStudyHub.dto.ResourceUploadDTO;
import com.auditStudyHub.dto.UserUploadDTO;
import com.auditStudyHub.dto.ResourceRequestUpdateRequest;
import com.auditStudyHub.dto.ResourceRequestDTO;
import java.util.List;
import java.util.Map;

/**
 * 资源服务接口
 */
public interface ResourceService {
    
    /**
     * 获取最新资源列表
     * @param limit 限制数量
     * @return 资源DTO列表
     */
    List<ResourceDTO> getLatestResources(int limit);
    
    /**
     * 获取热门资源列表
     * @param limit 限制数量
     * @return 资源DTO列表
     */
    List<ResourceDTO> getPopularResources(int limit);
    
    /**
     * 获取资源列表，支持多条件筛选、排序和分页
     * 
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词
     * @param collegeId 学院ID
     * @param majorId 专业ID
     * @param categoryId 分类ID
     * @param sort 排序方式：newest, downloads, views, likes
     * @param status 资源状态：0待审核，1已通过，2已拒绝
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param minSize 最小文件大小(MB)
     * @param maxSize 最大文件大小(MB)
     * @return 分页结果
     */
    PageResult<ResourceDTO> getResourceList(Integer page, Integer size, String keyword,
                                          Long collegeId, Long majorId, Long categoryId,
                                          String sort, Integer status, String startDate,
                                          String endDate, Integer minSize, Integer maxSize);
    
    /**
     * 根据ID获取单个资源
     * @param id 资源ID
     * @return 资源DTO对象
     */
    ResourceDTO getResourceById(Long id);
    
    /**
     * 增加资源下载次数
     * @param resourceId 资源ID
     * @return 是否成功
     */
    boolean incrementDownloadCount(Long resourceId);
    
    /**
     * 增加资源浏览次数
     * @param resourceId 资源ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long resourceId);
    
    /**
     * 记录用户下载历史
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 是否成功
     */
    boolean recordUserDownload(Long userId, Long resourceId);
    
    /**
     * 检查用户是否收藏了指定资源
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 是否已收藏
     */
    boolean checkUserCollected(Long userId, Long resourceId);
    
    /**
     * 检查用户是否点赞了指定资源
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 是否已点赞
     */
    boolean checkUserLiked(Long userId, Long resourceId);
    
    /**
     * 填充资源DTO的收藏和点赞状态
     * @param dto 资源DTO
     * @param userId 用户ID
     * @return 填充后的资源DTO
     */
    ResourceDTO fillUserInteractionStatus(ResourceDTO dto, Long userId);
    
    /**
     * 批量填充资源DTO的收藏和点赞状态
     * @param dtoList 资源DTO列表
     * @param userId 用户ID
     * @return 填充后的资源DTO列表
     */
    List<ResourceDTO> fillUserInteractionStatus(List<ResourceDTO> dtoList, Long userId);
    
    /**
     * 获取资源详情（包含评论信息）
     * @param id 资源ID
     * @param userId 用户ID (可选)
     * @return 资源详情DTO
     */
    ResourceDetailDTO getResourceDetailWithComments(Long id, Long userId);

    /**
     * 切换资源点赞状态
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 点赞结果DTO
     */
    LikeResultDTO toggleResourceLike(Long userId, Long resourceId);
    
    /**
     * 切换资源收藏状态
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 收藏结果DTO
     */
    CollectResultDTO toggleResourceCollection(Long userId, Long resourceId);
    
    /**
     * 批量收藏资源
     * @param userId 用户ID
     * @param resourceIds 资源ID列表
     * @return 操作结果，包含成功收藏的资源ID列表
     */
    Map<String, Object> batchCollectResources(Long userId, List<Long> resourceIds);
    
    /**
     * 获取用户点赞记录列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词 (可选)
     * @return 分页结果
     */
    PageResult<UserLikeDTO> getUserLikes(Long userId, Integer page, Integer size, String keyword);
    
    /**
     * 获取用户收藏记录列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词 (可选)
     * @return 分页结果
     */
    PageResult<UserCollectionDTO> getUserCollections(Long userId, Integer page, Integer size, String keyword);
    
    /**
     * 获取用户下载记录列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词 (可选)
     * @return 分页结果
     */
    PageResult<UserDownloadDTO> getUserDownloads(Long userId, Integer page, Integer size, String keyword);

    /**
     * 获取用户上传记录列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词 (可选)
     * @param status 资源状态：0待审核，1已通过，2已拒绝 (可选)
     * @return 分页结果
     */
    PageResult<UserUploadDTO> getUserUploads(Long userId, Integer page, Integer size, String keyword, Integer status);

    /**
     * 删除用户下载记录
     * @param userId 用户ID
     * @param downloadId 下载记录ID
     * @return 是否删除成功
     */
    boolean deleteUserDownload(Long userId, Long downloadId);

    /**
     * 删除用户上传记录（逻辑删除）
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 是否删除成功
     */
    boolean deleteUserUpload(Long userId, Long resourceId);
    
    /**
     * 上传新资源
     * @param uploadDTO 资源上传请求DTO
     * @return 资源DTO
     */
    ResourceDTO uploadResource(ResourceUploadDTO uploadDTO);

   
}