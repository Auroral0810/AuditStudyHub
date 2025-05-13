package com.auditStudyHub.service.impl;

import com.auditStudyHub.entity.TPopularSearch;
import com.auditStudyHub.mapper.TPopularSearchMapper;
import com.auditStudyHub.service.TPopularSearchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 热门搜索词表(TPopularSearch)表服务实现类
 *
 * @author makejava
 * @since 2025-05-07 21:27:45
 */
@Slf4j
@Service("tPopularSearchService")
public class TPopularSearchServiceImpl extends ServiceImpl<TPopularSearchMapper, TPopularSearch> implements TPopularSearchService {

    @Override
    public List<String> getHotSearchKeywords(int limit) {
        if (limit <= 0) {
            limit = 5; // 默认获取5个热门搜索词
        }
        
        try {
            // 构建查询条件
            LambdaQueryWrapper<TPopularSearch> queryWrapper = new LambdaQueryWrapper<>();
            Date now = new Date();
            
            // 查询有效期内的热门搜索词
            queryWrapper.eq(TPopularSearch::getStatus, 1)
                        .eq(TPopularSearch::getIsDeleted, 0)
                        .and(w -> 
                            // 有时间限制的搜索词
                            w.nested(nw -> 
                                nw.le(TPopularSearch::getStartTime, now)
                                  .ge(TPopularSearch::getEndTime, now)
                            )
                            // 或无时间限制的搜索词
                            .or(nw -> 
                                nw.isNull(TPopularSearch::getStartTime)
                                  .isNull(TPopularSearch::getEndTime)
                            )
                        )
                        .orderByAsc(TPopularSearch::getRank)
                        .last("LIMIT " + limit);
            
            // 执行查询并转换为关键词列表
            List<TPopularSearch> popularSearches = this.list(queryWrapper);
            
            if (popularSearches != null && !popularSearches.isEmpty()) {
                return popularSearches.stream()
                        .map(TPopularSearch::getKeyword)
                        .collect(Collectors.toList());
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取热门搜索词失败", e);
            return new ArrayList<>();
        }
    }
}

