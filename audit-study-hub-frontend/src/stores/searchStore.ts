import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getHotSearchKeywords, recordSearchKeyword, getSearchSuggestions } from '../api/resource'

// 自定义API响应类型
interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

// 搜索建议类型
export interface SearchSuggestion {
  keyword: string
  resourceId?: number
  title?: string
  categoryId?: number
  categoryName?: string
  score?: number
}

export const useSearchStore = defineStore('search', () => {
  const hotKeywords = ref<string[]>([])
  const suggestions = ref<SearchSuggestion[]>([])
  const loading = ref(false)
  const suggestionLoading = ref(false)
  const lastFetchTime = ref<number>(0)
  
  // 刷新时间间隔 - 10分钟
  const REFRESH_INTERVAL = 10 * 60 * 1000
  
  // 获取热门搜索词
  const fetchHotKeywords = async (limit = 5, forceRefresh = false) => {
    // 如果不强制刷新且数据已存在且未过期，直接返回缓存数据
    const now = Date.now()
    if (!forceRefresh && 
        hotKeywords.value.length > 0 && 
        now - lastFetchTime.value < REFRESH_INTERVAL) {
      return hotKeywords.value
    }

    loading.value = true
    try {
      const response = await getHotSearchKeywords(limit)
      // 直接获取响应数据，不需要再次解析
      const res = response as unknown as ApiResponse<string[]>
      
      if (res.code === 20000 && res.data) {
        hotKeywords.value = res.data
        lastFetchTime.value = now
        return hotKeywords.value
      }
      return []
    } catch (error) {
      console.error('获取热门搜索词失败', error)
      return []
    } finally {
      loading.value = false
    }
  }
  
  // 获取搜索建议
  const fetchSuggestions = async (prefix: string, categoryId?: number, limit = 5) => {
    if (!prefix || prefix.trim().length < 1) {
      suggestions.value = []
      return []
    }
    
    suggestionLoading.value = true
    try {
      
      const response = await getSearchSuggestions(prefix.trim(), categoryId, limit)
      
      
      // 将响应转为任意类型以便处理不同格式的响应
      const resp = response as any;
      
      // 处理不同可能的响应格式
      if (resp && resp.code === 20000 && Array.isArray(resp.data)) {
        // 处理直接响应格式: { code: 20000, message: "操作成功", data: [...] }
        
        suggestions.value = [...resp.data]; // 使用展开运算符创建新数组
        
        return [...resp.data]; // 返回新数组而不是响应式对象
      } else if (resp && resp.data && resp.data.code === 20000 && Array.isArray(resp.data.data)) {
        // 处理嵌套格式: response.data = { code: 20000, message: "操作成功", data: [...] }
        
        suggestions.value = [...resp.data.data]; // 使用展开运算符创建新数组
        
        return [...resp.data.data]; // 返回新数组而不是响应式对象
      }
      
      console.warn('[Store] 搜索建议响应格式不匹配预期:', JSON.stringify(resp));
      suggestions.value = []; // 清空建议
      return []
    } catch (error) {
      console.error('[Store] 获取搜索建议失败', error)
      suggestions.value = []; // 确保出错时清空建议
      return []
    } finally {
      suggestionLoading.value = false
    }
  }
  
  // 清除搜索建议
  const clearSuggestions = () => {
    suggestions.value = []
  }
  
  // 记录用户搜索
  const recordSearch = async (keyword: string) => {
    if (!keyword.trim()) return

    // 最多尝试两次
    let retries = 0;
    const maxRetries = 1;
    
    const tryRecordSearch = async () => {
      try {
        const response = await recordSearchKeyword(keyword);
        
        // 处理响应，兼容不同的响应格式
        const data = (response as any).data || response;
        
        if (data && (data.code === 20000 || data.code === 200)) {
          
          return true;
        } else {
          console.error('搜索记录失败: 服务器返回非成功状态', data);
          return false;
        }
      } catch (error: any) {
        console.error(`记录搜索关键词失败 (尝试 ${retries + 1}/${maxRetries + 1})`);
        console.error(error);
        
        if (retries < maxRetries) {
          retries++;
          
          // 短暂延迟后重试
          await new Promise(resolve => setTimeout(resolve, 500));
          return tryRecordSearch();
        }
        
        return false;
      }
    };
    
    // 执行记录搜索，但不等待结果
    tryRecordSearch().then(success => {
      if (!success) {
        console.warn('记录搜索关键词失败，热门搜索可能不会更新');
      }
    });
  }
  
  return {
    hotKeywords,
    suggestions,
    loading,
    suggestionLoading,
    fetchHotKeywords,
    fetchSuggestions,
    clearSuggestions,
    recordSearch
  }
})
