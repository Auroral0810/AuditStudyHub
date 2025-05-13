import { requestClient } from '#/api/request';

// 评论DTO类型定义
export interface AdminCommentDTO {
  id: number;
  content: string;
  resourceId: number;
  resourceTitle: string;
  userId: number;
  userName: string;
  parentId: number | null;
  parentContent: string | null;
  createTime: string;
  updateTime: string;
  isDeleted: number;
}

// 评论搜索请求类型
export interface AdminCommentSearchRequest {
  page: number;
  size: number;
  content?: string;
  resourceTitle?: string;
  userName?: string;
  parentId?: number;
  isDeleted?: number;
  startDate?: string;
  endDate?: string;
}

/**
 * 获取所有评论列表
 */
export function getCommentsApi() {
  return requestClient.get<AdminCommentDTO[]>('/admin/comment/list');
}

/**
 * 获取评论详情
 */
export function getCommentDetailApi(id: number) {
  return requestClient.get<AdminCommentDTO>(`/admin/comment/detail?id=${id}`);
}

/**
 * 搜索评论列表
 */
export function searchCommentsApi(params: AdminCommentSearchRequest) {
  return requestClient.post<{
    total: number;
    list: AdminCommentDTO[];
  }>('/admin/comment/search', params);
}

/**
 * 更新评论状态（逻辑删除或恢复）
 */
export function updateCommentStatusApi(id: number, isDeleted: number) {
  return requestClient.put<boolean>('/admin/comment/update-status', null, {
    params: { id, isDeleted }
  });
}

/**
 * 删除评论
 */
export function deleteCommentApi(id: number) {
  return requestClient.delete<boolean>(`/admin/comment/delete/${id}`);
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