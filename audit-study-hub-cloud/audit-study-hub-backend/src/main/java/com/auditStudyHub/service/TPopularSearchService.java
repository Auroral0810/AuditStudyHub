package com.auditStudyHub.service;

import com.auditStudyHub.entity.TPopularSearch;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 热门搜索词表(TPopularSearch)表服务接口
 *
 * @author makejava
 * @since 2025-05-07 21:27:45
 */
public interface TPopularSearchService extends IService<TPopularSearch> {

    /**
     * 获取热门搜索词列表
     * @param limit 限制返回数量
     * @return 热门搜索词列表
     */
    List<String> getHotSearchKeywords(int limit);
}