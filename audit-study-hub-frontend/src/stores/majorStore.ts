import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMajors, getMajor, getMajorsByCollege } from '@/api/major'
import type { MajorBaseDTO } from '@/types/major'

export const useMajorStore = defineStore('major', () => {
  // 所有专业列表
  const majors = ref<MajorBaseDTO[]>([])
  
  // 按学院ID分组的专业缓存
  const majorsByCollege = ref<Record<number, MajorBaseDTO[]>>({})
  
  // 加载状态
  const loading = ref(false)
  
  // 获取所有专业列表
  const fetchMajors = async () => {
    if (majors.value.length > 0) {
      return majors.value // 如果已经加载过，直接返回缓存数据
    }
    
    loading.value = true
    try {
      const response = await getMajors()
      majors.value = response.data || []
      return majors.value
    } catch (error) {
      console.error('获取专业列表失败:', error)
      return []
    } finally {
      loading.value = false
    }
  }
  
  // 获取单个专业详情
  const fetchMajor = async (id: number) => {
    // 先从缓存中查找
    const cached = majors.value.find(item => item.id === id)
    if (cached) {
      return cached
    }
    
    loading.value = true
    try {
      const response = await getMajor(id)
      return response.data
    } catch (error) {
      console.error(`获取专业ID=${id}的详情失败:`, error)
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 根据学院ID获取专业列表
  const fetchMajorsByCollege = async (collegeId: number) => {
    // 先从缓存中查找
    if (majorsByCollege.value[collegeId]) {
      return majorsByCollege.value[collegeId]
    }
    
    loading.value = true
    try {
      const response = await getMajorsByCollege(collegeId)
      const data = response.data || []
      // 更新缓存
      majorsByCollege.value[collegeId] = data
      return data
    } catch (error) {
      console.error(`获取学院ID=${collegeId}的专业列表失败:`, error)
      return []
    } finally {
      loading.value = false
    }
  }
  
  // 根据学院ID获取格式化的专业选项（用于下拉框）
  const getMajorOptions = async (collegeId: number) => {
    const majorList = await fetchMajorsByCollege(collegeId)
    return majorList.map(major => ({
      value: major.id,
      label: major.name
    }))
  }
  
  return {
    majors,
    loading,
    fetchMajors,
    fetchMajor,
    fetchMajorsByCollege,
    getMajorOptions
  }
})
