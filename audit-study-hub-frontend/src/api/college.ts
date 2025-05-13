import request from '@/utils/request'
import type { ApiResponse } from './auth'
import type { CollegeBaseDTO } from '@/types/college'

/**
 * 获取所有学院列表
 */
export function getColleges(): Promise<ApiResponse<CollegeBaseDTO[]>> {
  return request({
    url: '/college/getColleges',
    method: 'get'
  })
}

/**
 * 根据ID获取学院详情
 * @param id 学院ID
 */
export function getCollege(id: number): Promise<ApiResponse<CollegeBaseDTO>> {
  return request({
    url: '/college/getCollege',
    method: 'get',
    params: { id }
  })
}
