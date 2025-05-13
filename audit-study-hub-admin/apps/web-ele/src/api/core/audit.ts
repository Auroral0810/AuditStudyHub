import { requestClient } from '#/api/request';

// 审核资源DTO
export interface AuditResourceDTO {
  id: number;
  title: string;
  categoryName: string;
  fileType: string;
  collegeName: string;
  status: number;
  createTime: string;
}

// 审核请求DTO
export interface AuditRequestDTO {
  resourceIds: number[];
  auditResult: number; // 1-通过, 2-拒绝
}

// 审核资源搜索请求
export interface AuditResourceSearchRequest {
  page: number;
  size: number;
  title?: string;
  collegeId?: number;
  categoryId?: number;
  fileType?: string;
  status?: number;
}

/**
 * 获取待审核资源列表
 */
export function getPendingAuditResourcesApi() {
  return requestClient.get<AuditResourceDTO[]>('/admin/audit/pending');
}

/**
 * 搜索审核资源
 */
export function searchAuditResourcesApi(params: AuditResourceSearchRequest) {
  return requestClient.post<{
    total: number;
    list: AuditResourceDTO[];
  }>('/admin/audit/search', params);
}

/**
 * 批量审核资源
 */
export function batchAuditResourcesApi(request: AuditRequestDTO) {
  return requestClient.post<boolean>('/admin/audit/batch', request);
}

/**
 * 获取资源详情
 */
export function getAuditResourceDetailApi(id: number) {
  return requestClient.get<any>(`/admin/audit/detail?id=${id}`);
}

/**
 * 格式化日期
 */
export function formatDate(dateStr: string): string {
  if (!dateStr) return '';
  try {
    const date = new Date(dateStr);
    return date.toISOString().replace('T', ' ').slice(0, 19);
  } catch (e) {
    return dateStr;
  }
} 