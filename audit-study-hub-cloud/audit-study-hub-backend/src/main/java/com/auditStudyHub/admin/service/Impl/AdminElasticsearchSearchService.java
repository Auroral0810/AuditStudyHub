package com.auditStudyHub.admin.service.Impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.json.JsonData;
import com.auditStudyHub.admin.dto.AdminSearchRequest;
import com.auditStudyHub.document.ResourceDocument;
import com.auditStudyHub.dto.PageResult;
import com.auditStudyHub.dto.ResourceDTO;
import com.auditStudyHub.dto.SearchRequest;
import com.auditStudyHub.dto.SearchSuggestionDTO;
import com.auditStudyHub.entity.TResource;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.mapper.TResourceMapper;
import com.auditStudyHub.mapper.TUserMapper;
import com.auditStudyHub.service.ResourceService;
import com.auditStudyHub.service.SearchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 高级搜索服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminElasticsearchSearchService implements SearchService {
    
    private final ElasticsearchOperations elasticsearchOperations;
    private final TResourceMapper resourceMapper; // 用于获取完整数据
    
    @Autowired
    @Lazy
    private ResourceService resourceService; // 用于降级查询
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final TUserMapper userMapper; // 添加用户Mapper依赖
    
    @Override
    public PageResult<ResourceDTO> search(AdminSearchRequest request) {
        log.info("执行管理员高级搜索：keyword={}, page={}, size={}, userId={}", 
                 request.getKeyword(), request.getPage(), request.getSize(), request.getUserId());
        
        try {
            // 构建查询
            NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
            
            // 构建布尔查询
            List<Query> mustQueries = new ArrayList<>();
            List<Query> shouldQueries = new ArrayList<>();
            List<Query> filterQueries = new ArrayList<>();
            
            // 1. 关键词搜索（必须有）
            if (StringUtils.hasText(request.getKeyword())) {
                // 多字段匹配，对不同字段赋予不同权重
                Query multiMatchQuery = Query.of(q -> q
                    .multiMatch(mm -> mm
                        .query(request.getKeyword())
                        .fields("title^3", "description^1", "tags^2", "uploaderName^2")
                        .fuzziness("AUTO")
                        .prefixLength(3)
                    )
                );
                
                shouldQueries.add(multiMatchQuery);
            }
            
            // 2. 过滤条件
            
            // 状态过滤
            if (request.getStatus() != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t
                        .field("status")
                        .value(v -> v.longValue(request.getStatus().longValue()))
                    )
                ));
            } else {
                // 注意：管理员界面不默认过滤状态，需要显示所有状态的资源
                // 以下代码被注释掉，因为管理员应当看到所有状态
                /*
                // 默认只显示已审核通过的资源
                filterQueries.add(Query.of(q -> q
                    .term(t -> t
                        .field("status")
                        .value(v -> v.longValue(1L))
                    )
                ));
                */
            }
            
            // 学院过滤
            if (request.getCollegeId() != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t
                        .field("collegeId")
                        .value(v -> v.longValue(request.getCollegeId()))
                    )
                ));
            }
            
            // 专业过滤
            if (request.getMajorId() != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t
                        .field("majorId")
                        .value(v -> v.longValue(request.getMajorId()))
                    )
                ));
            }
            
            // 分类过滤
            if (request.getCategoryId() != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t
                        .field("categoryId")
                        .value(v -> v.longValue(request.getCategoryId()))
                    )
                ));
            }
            
            // 日期范围过滤
            if (StringUtils.hasText(request.getStartDate()) || StringUtils.hasText(request.getEndDate())) {
                Query rangeQuery = buildDateRangeQuery(request.getStartDate(), request.getEndDate());
                if (rangeQuery != null) {
                    filterQueries.add(rangeQuery);
                }
            }
            
            // 文件大小范围过滤（MB转为字节）
            if (request.getMinSize() != null || request.getMaxSize() != null) {
                Query sizeRangeQuery = Query.of(q -> q
                    .range(r -> {
                        r.field("fileSize");
                        if (request.getMinSize() != null) {
                            r.gte(JsonData.of(request.getMinSize() * 1024L * 1024L));
                        }
                        if (request.getMaxSize() != null) {
                            r.lte(JsonData.of(request.getMaxSize() * 1024L * 1024L));
                        }
                        return r;
                    })
                );
                filterQueries.add(sizeRangeQuery);
            }
            
            // 添加isDeleted过滤，只有当明确指定时才过滤
            if (request.getIsDeleted() != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t
                        .field("isDeleted")
                        .value(v -> v.longValue(request.getIsDeleted().longValue()))
                    )
                ));
            }
            
            // 组合所有查询条件
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            
            if (!mustQueries.isEmpty()) {
                boolQueryBuilder.must(mustQueries);
            }
            
            if (!shouldQueries.isEmpty()) {
                boolQueryBuilder.should(shouldQueries);
                if (StringUtils.hasText(request.getKeyword())) {
                    // 要求至少有一个should条件满足
                    boolQueryBuilder.minimumShouldMatch("1");
                }
            }
            
            if (!filterQueries.isEmpty()) {
                boolQueryBuilder.filter(filterQueries);
            }
            
            Query boolQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));
            queryBuilder.withQuery(boolQuery);
            
            // 设置分页参数
            int pageIndex = Math.max(0, request.getPage() - 1); // 前端从1开始，ES从0开始
            queryBuilder.withPageable(PageRequest.of(pageIndex, request.getSize()));
            
            // 设置排序
            Sort sort = getSortForQuery(request.getSort());
            queryBuilder.withSort(sort);
            
            // 设置高亮
            if (StringUtils.hasText(request.getKeyword())) {
                List<HighlightField> highlightFields = new ArrayList<>();
                highlightFields.add(new HighlightField("title"));
                highlightFields.add(new HighlightField("description"));
                highlightFields.add(new HighlightField("uploaderName"));
                
                // 创建高亮参数
                HighlightParameters highlightParameters = HighlightParameters.builder()
                    .withPreTags("<em class=\"highlight\">")
                    .withPostTags("</em>")
                    .build();
                
                // 创建高亮配置
                Highlight highlight = new Highlight(highlightParameters, highlightFields);
                
                queryBuilder.withHighlightQuery(new HighlightQuery(highlight, ResourceDocument.class));
            }
            
            // 执行查询
            NativeQuery searchQuery = queryBuilder.build();
            SearchHits<ResourceDocument> searchHits = 
                elasticsearchOperations.search(searchQuery, ResourceDocument.class);
            
            // 处理查询结果
            List<ResourceDTO> resources = new ArrayList<>();
            for (SearchHit<ResourceDocument> hit : searchHits) {
                ResourceDocument document = hit.getContent();
                ResourceDTO dto = convertToDTO(document);
                
                // 处理高亮内容
                Map<String, List<String>> resultHighlightFields = hit.getHighlightFields();
                if (resultHighlightFields != null) {
                    if (resultHighlightFields.containsKey("title")) {
                        dto.setTitle(resultHighlightFields.get("title").get(0));
                    }
                    if (resultHighlightFields.containsKey("description")) {
                        dto.setDescription(resultHighlightFields.get("description").get(0));
                    }
                    if (resultHighlightFields.containsKey("uploaderName")) {
                        dto.setUploaderName(resultHighlightFields.get("uploaderName").get(0));
                    }
                }
                
                resources.add(dto);
            }
            
            // 返回结果前，如果有用户ID，则填充用户交互状态
            Long userId = request.getUserId();
            if (userId != null && !resources.isEmpty()) {
                resources = resourceService.fillUserInteractionStatus(resources, userId);
            }
            
            // 返回结果
            return new PageResult<>(resources, searchHits.getTotalHits());
            
        } catch (Exception e) {
            log.error("执行ES高级搜索失败", e);
            // 出错时降级到MySQL查询
            return fallbackToMySQLSearch(request);
        }
    }
    
    /**
     * 构建日期范围查询
     */
    private Query buildDateRangeQuery(String startDate, String endDate) {
        return Query.of(q -> q
            .range(r -> {
                r.field("createTime");
                if (StringUtils.hasText(startDate)) {
                    Date start = null;
                    try {
                        start = dateFormat.parse(startDate);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    r.gte(JsonData.of(start.getTime()));
                }
                if (StringUtils.hasText(endDate)) {
                    Date end = null;
                    try {
                        end = dateFormat.parse(endDate);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    // 设置为当天结束时间
                    end.setHours(23);
                    end.setMinutes(59);
                    end.setSeconds(59);
                    r.lte(JsonData.of(end.getTime()));
                }
                return r;
            })
        );
    }
    
    /**
     * 根据排序方式获取排序对象
     */
    private Sort getSortForQuery(String sort) {
        switch (sort) {
            case "downloads":
                return Sort.by(Sort.Direction.DESC, "downloadCount");
            case "views":
                return Sort.by(Sort.Direction.DESC, "viewCount");
            case "likes":
                return Sort.by(Sort.Direction.DESC, "likeCount");
            case "newest":
            default:
                return Sort.by(Sort.Direction.DESC, "createTime");
        }
    }
    
    /**
     * 将ES文档转换为DTO
     */
    private ResourceDTO convertToDTO(ResourceDocument document) {
        ResourceDTO dto = new ResourceDTO();
        BeanUtils.copyProperties(document, dto);
        
        // 确保 isDeleted 字段被正确设置
        dto.setIsDeleted(document.getIsDeleted() != null ? document.getIsDeleted() : 0);
        
        // 填充上传者基本信息
        if (dto.getUploaderId() != null) {
            try {
                TUser uploader = userMapper.selectById(dto.getUploaderId());
                if (uploader != null) {
                    dto.setUploaderName(uploader.getUsername());
                    dto.setUploaderAvatar(uploader.getAvatar());
                    dto.setUploaderCollege(uploader.getCollegeName());
                    dto.setUploaderMajor(uploader.getMajorName());
                    
                    // 获取上传者的上传总数
                    LambdaQueryWrapper<TResource> countWrapper = Wrappers.lambdaQuery();
                    countWrapper.eq(TResource::getUploaderId, dto.getUploaderId())
                              .eq(TResource::getIsDeleted, 0);
                    Integer totalUploads = Math.toIntExact(resourceMapper.selectCount(countWrapper));
                    dto.setUploaderTotalUploads(totalUploads);
                }
            } catch (Exception e) {
                log.error("ES获取上传者信息失败: uploaderId={}", dto.getUploaderId(), e);
            }
        }
        
        return dto;
    }
    
    /**
     * ES查询失败时降级到MySQL查询（兜底方案）
     */
    private PageResult<ResourceDTO> fallbackToMySQLSearch(AdminSearchRequest request) {
        log.info("降级到MySQL查询，调用原始的ResourceService.getResourceList方法");
        
        try {
            // 调用原有的MySQL查询实现
            PageResult<ResourceDTO> result = resourceService.getResourceList(
                request.getPage(),
                request.getSize(),
                request.getKeyword(),
                request.getCollegeId(),
                request.getMajorId(),
                request.getCategoryId(),
                request.getSort(),
                request.getStatus(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMinSize(),
                request.getMaxSize()
            );
            
            // 如果有用户ID，填充用户交互状态
            Long userId = request.getUserId();
            if (userId != null && result != null && result.getList() != null && !result.getList().isEmpty()) {
                result.setList(resourceService.fillUserInteractionStatus(result.getList(), userId));
            }
            
            return result;
        } catch (Exception e) {
            log.error("MySQL降级查询也失败了", e);
            return new PageResult<>(new ArrayList<>(), 0);
        }
    }

    @Override
    public List<SearchSuggestionDTO> getSuggestions(String prefix, Long categoryId, int limit) {
        log.info("获取搜索建议: prefix={}, categoryId={}, limit={}", prefix, categoryId, limit);
        
        if (!StringUtils.hasText(prefix)) {
            return new ArrayList<>();
        }
        
        if (limit <= 0) {
            limit = 5; // 默认获取5个建议
        }
        
        try {
            // 构建前缀查询
            NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
            
            // 创建布尔查询
            List<Query> mustQueries = new ArrayList<>();
            List<Query> shouldQueries = new ArrayList<>();
            List<Query> filterQueries = new ArrayList<>();
            
            // 前缀查询 - 改用更宽松的查询方式
            // 使用should组合各种匹配方式以提高命中率
            
            // 1. 标题前缀匹配 (权重最高)
            Query titlePrefixQuery = Query.of(q -> q
                .prefix(p -> p
                    .field("title")
                    .value(prefix)
                    .boost(3.0f)
                )
            );
            shouldQueries.add(titlePrefixQuery);
            
            // 2. 标题模糊匹配
            Query titleFuzzyQuery = Query.of(q -> q
                .match(m -> m
                    .field("title")
                    .query(prefix)
                    .fuzziness("AUTO")
                    .boost(2.0f)
                )
            );
            shouldQueries.add(titleFuzzyQuery);
            
            // 3. 描述和标签中的匹配
            Query descAndTagsQuery = Query.of(q -> q
                .multiMatch(mm -> mm
                    .query(prefix)
                    .fields("description", "tags", "uploaderName")
                    .type(TextQueryType.BestFields)
                    .fuzziness("AUTO")
                )
            );
            shouldQueries.add(descAndTagsQuery);
            
            // 如果指定了分类ID，添加分类过滤
            if (categoryId != null && categoryId > 0) {
                Query categoryFilter = Query.of(q -> q
                    .term(t -> t
                        .field("categoryId")
                        .value(categoryId)
                    )
                );
                filterQueries.add(categoryFilter);
            }
            
            // 审核状态过滤 - 管理员界面不默认过滤已删除资源
            // 注释掉原有代码：
            /*
            Query statusFilter = Query.of(q -> q
                .term(t -> t
                    .field("status")
                    .value(1)
                )
            );
            filterQueries.add(statusFilter);
            */
            
            // 组装布尔查询
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            
            if (!mustQueries.isEmpty()) {
                boolQueryBuilder.must(mustQueries);
            }
            
            if (!shouldQueries.isEmpty()) {
                boolQueryBuilder.should(shouldQueries);
                // 至少有一个should条件匹配
                boolQueryBuilder.minimumShouldMatch("1");
            }
            
            if (!filterQueries.isEmpty()) {
                boolQueryBuilder.filter(filterQueries);
            }
            
            Query boolQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));
            
            // 构建查询
            queryBuilder.withQuery(boolQuery);
            
            // 设置分页
            queryBuilder.withPageable(PageRequest.of(0, limit));
            
            // 设置高亮
            List<HighlightField> highlightFields = new ArrayList<>();
            highlightFields.add(new HighlightField("title"));
            highlightFields.add(new HighlightField("description"));
            
            HighlightParameters parameters = HighlightParameters.builder()
                .withPreTags("<em class=\"highlight\">")
                .withPostTags("</em>")
                .build();
            
            Highlight highlight = new Highlight(parameters, highlightFields);
            queryBuilder.withHighlightQuery(new HighlightQuery(highlight, ResourceDocument.class));
            
            // 执行查询
            NativeQuery searchQuery = queryBuilder.build();
            log.debug("搜索建议查询: {}", searchQuery);
            SearchHits<ResourceDocument> searchHits = elasticsearchOperations.search(
                searchQuery, ResourceDocument.class
            );
            
            log.info("搜索建议结果数量: {}", searchHits.getTotalHits());
            
            // 构建建议列表
            List<SearchSuggestionDTO> suggestions = new ArrayList<>();
            for (SearchHit<ResourceDocument> hit : searchHits) {
                ResourceDocument document = hit.getContent();
                Map<String, List<String>> resultHighlightFields = hit.getHighlightFields();
                
                String highlightedTitle = null;
                if (resultHighlightFields.containsKey("title") && !resultHighlightFields.get("title").isEmpty()) {
                    highlightedTitle = resultHighlightFields.get("title").get(0);
                }
                
                SearchSuggestionDTO suggestion = new SearchSuggestionDTO();
                
                // 确定关键词（如果有高亮，使用高亮的标题，否则使用原标题的前30个字符）
                String keyword = highlightedTitle != null ? 
                        highlightedTitle.replaceAll("<em class=\"highlight\">|</em>", "") : 
                        (document.getTitle() != null ? 
                            (document.getTitle().length() > 30 ? 
                                document.getTitle().substring(0, 30) : document.getTitle()) : 
                            prefix);
                
                suggestion.setKeyword(keyword);
                suggestion.setResourceId(document.getId());
                suggestion.setTitle(highlightedTitle != null ? highlightedTitle : document.getTitle());
                suggestion.setCategoryId(document.getCategoryId());
                suggestion.setCategoryName(document.getCategoryName());
                suggestion.setScore(hit.getScore());
                
                suggestions.add(suggestion);
            }
            
            return suggestions;
        } catch (Exception e) {
            log.error("获取搜索建议失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 实现 SearchService 接口的 search 方法
     */
    @Override
    public PageResult<ResourceDTO> search(SearchRequest request) {
        // 将 SearchRequest 转换为 AdminSearchRequest
        AdminSearchRequest adminRequest = new AdminSearchRequest();
        adminRequest.setPage(request.getPage());
        adminRequest.setSize(request.getSize());
        adminRequest.setKeyword(request.getKeyword());
        adminRequest.setCollegeId(request.getCollegeId());
        adminRequest.setMajorId(request.getMajorId());
        adminRequest.setCategoryId(request.getCategoryId());
        adminRequest.setStatus(request.getStatus());
        adminRequest.setSort(request.getSort());
        adminRequest.setStartDate(request.getStartDate());
        adminRequest.setEndDate(request.getEndDate());
        adminRequest.setMinSize(request.getMinSize());
        adminRequest.setMaxSize(request.getMaxSize());
        adminRequest.setUserId(request.getUserId());
        
        // 调用自定义的 AdminSearchRequest 搜索方法
        return search(adminRequest);
    }
}
