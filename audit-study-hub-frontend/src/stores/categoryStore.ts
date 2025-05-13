import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCategories, getCategory } from '@/api/category'
import type { CategoryBaseDTO } from '@/types/category'

export const useCategoryStore = defineStore('category', () => {
  // 所有分类列表
  const categories = ref<CategoryBaseDTO[]>([])
  
  // 加载状态
  const loading = ref(false)
  
  // 获取所有分类列表
  const fetchCategories = async () => {
    if (categories.value.length > 0) {
      return categories.value // 如果已经加载过，直接返回缓存数据
    }
    
    loading.value = true
    try {
      const response = await getCategories()
      categories.value = response.data || []
      return categories.value
    } catch (error) {
      console.error('获取分类列表失败:', error)
      return []
    } finally {
      loading.value = false
    }
  }
  
  // 获取单个分类详情
  const fetchCategory = async (id: number) => {
    // 先从缓存中查找
    const cached = categories.value.find(item => item.id === id)
    if (cached) {
      return cached
    }
    
    loading.value = true
    try {
      const response = await getCategory(id)
      return response.data
    } catch (error) {
      console.error(`获取分类ID=${id}的详情失败:`, error)
      return null
    } finally {
      loading.value = false
    }
  }
  
  return {
    categories,
    loading,
    fetchCategories,
    fetchCategory
  }
})
