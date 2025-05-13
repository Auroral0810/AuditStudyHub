package com.auditStudyHub.service;

import java.util.List;

import com.auditStudyHub.admin.dto.AdminSearchRequest;
import com.auditStudyHub.dto.PageResult;
import com.auditStudyHub.dto.ResourceDTO;
import com.auditStudyHub.dto.SearchRequest;
import com.auditStudyHub.dto.SearchSuggestionDTO;

/**
 * 搜索服务接口
 */
public interface SearchService {
    
    /**
     * 高级搜索
     * @param request 搜索请求参数
     * @return 分页结果
     */
    PageResult<ResourceDTO> search(SearchRequest request);

    PageResult<ResourceDTO> search(AdminSearchRequest request);

    /**
     * 获取搜索建议
     * @param prefix 搜索前缀
     * @param categoryId 分类ID（可选）
     * @param limit 限制返回数量
     * @return 搜索建议列表
     */
    List<SearchSuggestionDTO> getSuggestions(String prefix, Long categoryId, int limit);
}
