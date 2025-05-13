import request from '@/utils/request'
import type { ApiResponse } from './auth'
import type { CategoryBaseDTO } from '@/types/category'

/**
 * 获取所有分类列表
 */
export function getCategories(): Promise<ApiResponse<CategoryBaseDTO[]>> {
  return request({
    url: '/category/getCategories',
    method: 'get'
  })
}

/**
 * 根据ID获取分类详情
 * @param id 分类ID
 */
export function getCategory(id: number): Promise<ApiResponse<CategoryBaseDTO>> {
  return request({
    url: '/category/getCategory',
    method: 'get',
    params: { id }
  })
}
