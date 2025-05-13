import { requestClient } from '#/api/request';

// 资源请求回复DTO
export interface AdminResourceRequestReplyDTO {
  id: number;
  requestId: number;
  content: string;
  userId: number;
  userName: string;
  replyContent: string;
  resourceId?: number;
  resourceName?: string;
  parentId?: number;
  parentContent?: string;
  createTime: string;
  replyTime: string;
  isDeleted: number;
}

// 搜索请求参数
export interface AdminResourceRequestReplySearchRequest {
  page: number;
  size: number;
  content?: string;
  replyContent?: string;
  userName?: string;
  resourceName?: string;
  parentId?: number;
  isDeleted?: number;
  startDate?: string;
  endDate?: string;
}

/**
 * 获取所有资源请求回复
 */
export function getAllRepliesApi() {
  return requestClient.get<AdminResourceRequestReplyDTO[]>('/admin/resource-request-reply/list');
}

/**
 * 获取资源请求回复详情
 */
export function getReplyDetailApi(id: number) {
  return requestClient.get<AdminResourceRequestReplyDTO>(`/admin/resource-request-reply/detail?id=${id}`);
}

/**
 * 搜索资源请求回复
 */
export function searchRepliesApi(params: AdminResourceRequestReplySearchRequest) {
  return requestClient.post<{
    total: number;
    list: AdminResourceRequestReplyDTO[];
  }>('/admin/resource-request-reply/search', params);
}

/**
 * 添加资源请求回复
 */
export function addReplyApi(data: Partial<AdminResourceRequestReplyDTO>) {
  return requestClient.post<number>('/admin/resource-request-reply/add', data);
}

/**
 * 更新资源请求回复内容
 */
export function updateReplyContentApi(id: number, content: string) {
  return requestClient.put<boolean>('/admin/resource-request-reply/update-content', null, {
    params: { id, content }
  });
}

/**
 * 更新资源请求回复状态（逻辑删除/恢复）
 */
export function updateReplyStatusApi(id: number, isDeleted: number) {
  return requestClient.put<boolean>('/admin/resource-request-reply/update-status', null, {
    params: { id, isDeleted }
  });
}

/**
 * 删除资源请求回复（物理删除）
 */
export function deleteReplyApi(id: number) {
  return requestClient.delete<boolean>(`/admin/resource-request-reply/delete/${id}`);
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
