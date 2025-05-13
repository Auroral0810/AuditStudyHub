import { requestClient } from '#/api/request';
import type { College } from '../../views/content/college-data';

/**
 * 获取所有学院列表（管理员版本，包含所有字段）
 */
export async function getCollegesApi() {
  // 获取原始数据
  const response = await requestClient.get<any[]>('/admin/college/list');
  
  // 转换字段名格式（驼峰命名转下划线命名）
  return response.map(item => ({
    id: item.id,
    name: item.name,
    cover_url: item.coverUrl,   // 转换字段名
    logo_url: item.logoUrl,     // 转换字段名
    create_time: formatDate(item.createTime), // 转换字段名并格式化日期
    update_time: formatDate(item.updateTime), // 转换字段名并格式化日期
    is_deleted: item.isDeleted  // 转换字段名
  })) as College[];
}

/**
 * 获取学院详情
 */
export async function getCollegeDetailApi(id: number) {
  const response = await requestClient.get<any>(`/admin/college/getAdminCollege?id=${id}`);
  
  // 转换字段名格式
  return {
    id: response.id,
    name: response.name,
    cover_url: response.coverUrl,
    logo_url: response.logoUrl,
    create_time: formatDate(response.createTime),
    update_time: formatDate(response.updateTime),
    is_deleted: response.isDeleted
  } as College;
}

/**
 * 添加学院
 */
export async function addCollegeApi(data: Omit<College, 'id' | 'create_time' | 'update_time'>) {
  // 转换为后端字段格式
  const requestData = {
    name: data.name,
    coverUrl: data.cover_url,
    logoUrl: data.logo_url,
    isDeleted: data.is_deleted
  };
  return requestClient.post<College>('/admin/college/add', requestData);
}

/**
 * 更新学院
 */
export async function updateCollegeApi(data: Partial<College> & { id: number }) {
  // 转换为后端字段格式
  const requestData = {
    id: data.id,
    name: data.name,
    coverUrl: data.cover_url,
    logoUrl: data.logo_url,
    isDeleted: data.is_deleted
  };
  return requestClient.put<College>('/admin/college/update', requestData);
}

/**
 * 删除学院
 */
export async function deleteCollegeApi(id: number) {
  console.log(`发送删除请求: /api/admin/college/delete/${id}`); // 添加日志
  return requestClient.delete<boolean>(`/admin/college/delete/${id}`);
}

/**
 * 格式化日期
 */
function formatDate(dateStr: string): string {
  if (!dateStr) return '';
  try {
    const date = new Date(dateStr);
    return date.toISOString().replace('T', ' ').slice(0, 19);
  } catch (e) {
    return dateStr;
  }
}
