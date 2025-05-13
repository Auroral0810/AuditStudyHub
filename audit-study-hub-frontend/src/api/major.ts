import request from '@/utils/request'
import type { ApiResponse } from './auth'
import type { MajorBaseDTO } from '@/types/major'

/**
 * 获取所有专业列表
 */
export function getMajors(): Promise<ApiResponse<MajorBaseDTO[]>> {
  return request({
    url: '/major/getMajors',
    method: 'get'
  })
}

/**
 * 根据ID获取专业详情
 * @param id 专业ID
 */
export function getMajor(id: number): Promise<ApiResponse<MajorBaseDTO>> {
  return request({
    url: '/major/getMajor',
    method: 'get',
    params: { id }
  })
}

/**
 * 根据学院ID获取专业列表
 * @param collegeId 学院ID
 */
export function getMajorsByCollege(collegeId: number): Promise<ApiResponse<MajorBaseDTO[]>> {
  return request({
    url: '/major/getMajorsByCollege',
    method: 'get',
    params: { collegeId }
  })
}
