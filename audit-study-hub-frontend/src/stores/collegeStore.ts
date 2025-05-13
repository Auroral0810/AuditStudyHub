import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getColleges, getCollege } from '@/api/college'
import type { CollegeBaseDTO } from '@/types/college'

export const useCollegeStore = defineStore('college', () => {
  // 所有学院列表
  const colleges = ref<CollegeBaseDTO[]>([])
  
  // 加载状态
  const loading = ref(false)
  
  // 获取所有学院列表
  const fetchColleges = async () => {
    if (colleges.value.length > 0) {
      return colleges.value // 如果已经加载过，直接返回缓存数据
    }
    
    loading.value = true
    try {
      const response = await getColleges()
      colleges.value = response.data || []
      return colleges.value
    } catch (error) {
      console.error('获取学院列表失败:', error)
      return []
    } finally {
      loading.value = false
    }
  }
  
  // 获取单个学院详情
  const fetchCollege = async (id: number) => {
    // 先从缓存中查找
    const cached = colleges.value.find(item => item.id === id)
    if (cached) {
      return cached
    }
    
    loading.value = true
    try {
      const response = await getCollege(id)
      return response.data
    } catch (error) {
      console.error(`获取学院ID=${id}的详情失败:`, error)
      return null
    } finally {
      loading.value = false
    }
  }
  
  return {
    colleges,
    loading,
    fetchColleges,
    fetchCollege
  }
})
