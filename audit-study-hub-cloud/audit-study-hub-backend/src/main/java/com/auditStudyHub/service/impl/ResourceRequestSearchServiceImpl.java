package com.auditStudyHub.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.auditStudyHub.document.ResourceRequestDocument;
import com.auditStudyHub.dto.PageResult;
import com.auditStudyHub.dto.ResourceRequestDTO;
import com.auditStudyHub.dto.ResourceRequestSearchRequest;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.mapper.TUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceRequestSearchServiceImpl {

    private final ElasticsearchOperations elasticsearchOperations;
    private final TUserMapper userMapper;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public PageResult<ResourceRequestDTO> search(ResourceRequestSearchRequest request) {
        log.info("ES执行资源请求搜索：keyword={}, page={}, size={}, userId={}", 
                 request.getKeyword(), request.getPage(), request.getSize(), request.getUserId());
        
        try {
            // 构建查询
            NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
            
            // 构建布尔查询
            List<Query> mustQueries = new ArrayList<>();
            List<Query> shouldQueries = new ArrayList<>();
            List<Query> filterQueries = new ArrayList<>();
            
            // 关键词搜索
            if (StringUtils.hasText(request.getKeyword())) {
                Query multiMatchQuery = Query.of(q -> q
                    .multiMatch(mm -> mm
                        .query(request.getKeyword())
                        .fields("title^3", "content^1", "username^2")
                        .fuzziness("AUTO")
                        .prefixLength(3)
                    )
                );
                
                shouldQueries.add(multiMatchQuery);
            }
            
            // 未删除条件（在ES中不需要，因为只同步未删除的数据）
            
            // 状态条件
            if (request.getStatus() != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t
                        .field("status")
                        .value(v -> v.longValue(request.getStatus().longValue()))
                    )
                ));
            }
            
            // 学院条件
            if (request.getCollegeId() != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t
                        .field("collegeId")
                        .value(v -> v.longValue(request.getCollegeId()))
                    )
                ));
            }
            
            // 专业条件
            if (request.getMajorId() != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t
                        .field("majorId")
                        .value(v -> v.longValue(request.getMajorId()))
                    )
                ));
            }
            
            // 分类条件
            if (request.getCategoryId() != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t
                        .field("categoryId")
                        .value(v -> v.longValue(request.getCategoryId()))
                    )
                ));
            }
            
            // 用户条件
            if (request.getUserId() != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t
                        .field("userId")
                        .value(v -> v.longValue(request.getUserId()))
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
            
            // 设置高亮 - 修复版本
            if (StringUtils.hasText(request.getKeyword())) {
                List<HighlightField> highlightFields = new ArrayList<>();
                highlightFields.add(new HighlightField("title"));
                highlightFields.add(new HighlightField("content"));
                highlightFields.add(new HighlightField("username"));
                
                // 创建高亮参数 - 确保与ElasticsearchSearchService保持一致
                HighlightParameters highlightParameters = HighlightParameters.builder()
                    .withPreTags("<em class=\"highlight\">")
                    .withPostTags("</em>")
                    .build();
                
                // 创建高亮配置
                Highlight highlight = new Highlight(highlightParameters, highlightFields);
                
                queryBuilder.withHighlightQuery(new HighlightQuery(highlight, ResourceRequestDocument.class));
            }
            
            // 执行查询 - 修复版本
            NativeQuery searchQuery = queryBuilder.build();
            SearchHits<ResourceRequestDocument> searchHits = 
                elasticsearchOperations.search(searchQuery, ResourceRequestDocument.class);
            
            // 处理查询结果 - 修复版本
            List<ResourceRequestDTO> dtoList = new ArrayList<>();
            for (SearchHit<ResourceRequestDocument> hit : searchHits) {
                ResourceRequestDocument document = hit.getContent();
                ResourceRequestDTO dto = new ResourceRequestDTO();
                BeanUtils.copyProperties(document, dto);
                
                // 处理高亮内容
                Map<String, List<String>> resultHighlightFields = hit.getHighlightFields();
                if (resultHighlightFields != null) {
                    if (resultHighlightFields.containsKey("title")) {
                        dto.setTitle(resultHighlightFields.get("title").get(0));
                    }
                    if (resultHighlightFields.containsKey("content")) {
                        dto.setContent(resultHighlightFields.get("content").get(0));
                    }
                    if (resultHighlightFields.containsKey("username")) {
                        dto.setUsername(resultHighlightFields.get("username").get(0));
                    }
                }
                
                // 查询并设置用户头像信息
                if (dto.getUserId() != null) {
                    TUser user = userMapper.selectById(dto.getUserId());
                    if (user != null && StringUtils.hasText(user.getAvatar())) {
                        dto.setUserAvatar(user.getAvatar());
                    }
                }
                
                dtoList.add(dto);
            }
            
            // 返回分页结果
            return new PageResult<>(dtoList, searchHits.getTotalHits());
        } catch (Exception e) {
            log.error("ES搜索资源请求失败", e);
            return new PageResult<>(new ArrayList<>(), 0);
        }
    }

    private Query buildDateRangeQuery(String startDate, String endDate) {
        if (StringUtils.hasText(startDate) || StringUtils.hasText(endDate)) {
            Date start = null;
            Date end = null;
            try {
                if (StringUtils.hasText(startDate)) {
                    start = dateFormat.parse(startDate);
                }
                if (StringUtils.hasText(endDate)) {
                    end = dateFormat.parse(endDate);
                }
            } catch (ParseException e) {
                log.error("解析日期失败", e);
            }
            Date finalStart = start;
            Date finalEnd = end;
            return Query.of(q -> q
                .range(r -> r
                    .field("createTime")
                    .gte(JsonData.of(finalStart))
                    .lte(JsonData.of(finalEnd))
                )
            );
        }
        return null;
    }

    private Sort getSortForQuery(String sort) {
        if ("hot".equals(sort)) {
            return Sort.by(Sort.Order.desc("viewCount"));
        } else {
            return Sort.by(Sort.Order.desc("createTime"));
        }
    }
}
