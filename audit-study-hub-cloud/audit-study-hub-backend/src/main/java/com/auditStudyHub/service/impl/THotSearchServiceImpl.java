package com.auditStudyHub.service.impl;

import com.auditStudyHub.entity.THotSearch;
import com.auditStudyHub.mapper.THotSearchMapper;
import com.auditStudyHub.service.THotSearchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 搜索记录表(THotSearch)表服务实现类
 *
 * @author makejava
 * @since 2025-05-07 21:29:03
 */
@Slf4j
@Service("tHotSearchService")
public class THotSearchServiceImpl extends ServiceImpl<THotSearchMapper, THotSearch> implements THotSearchService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recordSearchKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("尝试记录空的搜索关键词");
            return false;
        }
        
        // 处理关键词，去除两端空格
        keyword = keyword.trim();
        
        try {
            // 查询是否已存在该关键词
            LambdaQueryWrapper<THotSearch> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(THotSearch::getKeyword, keyword)
                        .eq(THotSearch::getIsDeleted, 0);
            
            THotSearch hotSearch = this.getOne(queryWrapper);
            
            if (hotSearch != null) {
                // 更新已有记录
                log.debug("更新搜索关键词计数: {}, 当前次数: {}", keyword, hotSearch.getSearchCount());
                hotSearch.setSearchCount(hotSearch.getSearchCount() + 1);
                hotSearch.setLastSearchTime(new Date());
                hotSearch.setUpdateTime(new Date());
                return this.updateById(hotSearch);
            } else {
                // 插入新记录
                log.debug("新增搜索关键词: {}", keyword);
                THotSearch newHotSearch = new THotSearch();
                newHotSearch.setKeyword(keyword);
                newHotSearch.setSearchCount(1);
                newHotSearch.setLastSearchTime(new Date());
                newHotSearch.setCreateTime(new Date());
                newHotSearch.setUpdateTime(new Date());
                newHotSearch.setIsDeleted(0);
                boolean result = this.save(newHotSearch);
                
                if (result) {
                    log.debug("新增搜索关键词成功，ID: {}", newHotSearch.getId());
                }
                
                return result;
            }
        } catch (Exception e) {
            log.error("记录搜索关键词失败: {}", keyword, e);
            return false;
        }
    }
}

