import { requestClient } from '#/api/request';
import type { Major } from '../../views/content/major-data';

/**
 * 获取所有专业列表（管理员版本）
 */
export async function getMajorsApi() {
  // 获取原始数据
  const response = await requestClient.get<any[]>('/admin/major/list');
  
  // 转换字段名格式
  return response.map(item => ({
    id: item.id,
    name: item.name,
    college_id: item.collegeId,        // 转换字段名
    college_name: item.collegeName,    // 转换字段名
    degree: item.degree,
    xl: item.xl,
    xz: item.xz,
    create_time: formatDate(item.createTime), // 转换字段名并格式化日期
    update_time: formatDate(item.updateTime), // 转换字段名并格式化日期
    is_deleted: item.isDeleted         // 转换字段名
  })) as Major[];
}

/**
 * 获取专业详情
 */
export async function getMajorDetailApi(id: number) {
  const response = await requestClient.get<any>(`/admin/major/getMajor?id=${id}`);
  
  // 转换字段名格式
  return {
    id: response.id,
    name: response.name,
    college_id: response.collegeId,
    college_name: response.collegeName,
    degree: response.degree,
    xl: response.xl,
    xz: response.xz,
    create_time: formatDate(response.createTime),
    update_time: formatDate(response.updateTime),
    is_deleted: response.isDeleted
  } as Major;
}

/**
 * 根据学院ID获取专业列表
 */
export async function getMajorsByCollegeApi(collegeId: number) {
  const response = await requestClient.get<any[]>(`/admin/major/getMajorsByCollege?collegeId=${collegeId}`);
  
  // 转换字段名格式
  return response.map(item => ({
    id: item.id,
    name: item.name,
    college_id: item.collegeId,
    college_name: item.collegeName,
    degree: item.degree,
    xl: item.xl,
    xz: item.xz,
    create_time: formatDate(item.createTime),
    update_time: formatDate(item.updateTime),
    is_deleted: item.isDeleted
  })) as Major[];
}

/**
 * 添加专业
 */
export async function addMajorApi(data: Omit<Major, 'id' | 'create_time' | 'update_time' | 'college_name'>) {
  // 转换为后端字段格式
  const requestData = {
    name: data.name,
    collegeId: data.college_id,
    degree: data.degree,
    xl: data.xl,
    xz: data.xz,
    isDeleted: data.is_deleted
  };
  return requestClient.post<Major>('/admin/major/add', requestData);
}

/**
 * 更新专业
 */
export async function updateMajorApi(data: Partial<Major> & { id: number }) {
  // 转换为后端字段格式
  const requestData = {
    id: data.id,
    name: data.name,
    collegeId: data.college_id,
    degree: data.degree,
    xl: data.xl,
    xz: data.xz,
    isDeleted: data.is_deleted
  };
  return requestClient.put<Major>('/admin/major/update', requestData);
}

/**
 * 删除专业
 */
export async function deleteMajorApi(id: number) {
  return requestClient.delete<boolean>(`/admin/major/delete/${id}`);
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