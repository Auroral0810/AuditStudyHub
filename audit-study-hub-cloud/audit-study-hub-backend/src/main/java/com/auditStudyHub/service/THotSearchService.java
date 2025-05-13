package com.auditStudyHub.service;

import com.auditStudyHub.entity.THotSearch;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 搜索记录表(THotSearch)表服务接口
 *
 * @author makejava
 * @since 2025-05-07 21:29:03
 */
public interface THotSearchService extends IService<THotSearch> {

    /**
     * 记录搜索关键词
     * @param keyword 搜索关键词
     * @return 是否成功
     */
    boolean recordSearchKeyword(String keyword);
}
