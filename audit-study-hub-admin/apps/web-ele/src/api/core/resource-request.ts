import { requestClient } from '#/api/request';

// 资源请求DTO接口
export interface AdminRequestDTO {
  id: number;
  title: string;
  content: string;
  userId: number;
  userName: string;
  collegeId: number;
  collegeName: string;
  majorId?: number;
  majorName?: string;
  courseId?: number;
  courseName?: string;
  categoryId: number;
  categoryName: string;
  resourceId?: number;
  resourceTitle?: string;
  status: number;
  replyCount: number;
  viewCount: number;
  createTime: string;
  updateTime: string;
  isDeleted: number;
  parentContent?: string;
}

// 资源请求搜索请求接口
export interface AdminRequestSearchRequest {
  page?: number;
  size?: number;
  title?: string;
  content?: string;
  userName?: string;
  collegeId?: number;
  majorId?: number;
  categoryId?: number;
  status?: number;
  isDeleted?: number;
  startDate?: string;
  endDate?: string;
}

// 格式化日期
export function formatDate(dateStr: string) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });
}

// 获取所有资源请求列表
export async function getRequestsApi() {
  const response = await requestClient.get<AdminRequestDTO[]>('/admin/request/list');
  return response;
}

// 获取资源请求详情
export async function getRequestDetailApi(id: number) {
  const response = await requestClient.get<AdminRequestDTO>('/admin/request/detail', {
    params: { id }
  });
  return response;
}

// 搜索资源请求
export async function searchRequestsApi(params: AdminRequestSearchRequest) {
  const response = await requestClient.post<{list: AdminRequestDTO[], total: number}>('/admin/request/search', params);
  return response;
}

// 更新资源请求状态
export async function updateRequestStatusApi(id: number, status: number) {
  const response = await requestClient.put<boolean>('/admin/request/update-status', null, {
    params: { id, status }
  });
  return response;
}

// 更新资源请求删除状态
export async function updateRequestDeleteStatusApi(id: number, isDeleted: number) {
  const response = await requestClient.put<boolean>('/admin/request/update-delete', null, {
    params: { id, isDeleted }
  });
  return response;
}

// 删除资源请求
export async function deleteRequestApi(id: number) {
  const response = await requestClient.delete<boolean>(`/admin/request/delete/${id}`);
  return response;
}

// 创建资源请求
export async function createRequestApi(data: Omit<AdminRequestDTO, 'id' | 'createTime' | 'updateTime'>) {
  const response = await requestClient.post<AdminRequestDTO>('/admin/request/create', data);
  return response;
}

// 更新资源请求
export async function updateRequestApi(data: AdminRequestDTO) {
  const response = await requestClient.put<AdminRequestDTO>('/admin/request/update', data);
  return response;
}

// 恢复已删除的资源请求
export async function restoreRequestApi(id: number) {
  const response = await requestClient.put<boolean>(`/admin/request/restore/${id}`, null);
  return response;
}
