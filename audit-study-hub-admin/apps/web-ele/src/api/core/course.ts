import { requestClient } from '#/api/request';
import type { Course } from '../../views/content/course-data';

/**
 * 获取所有课程列表（管理员版本）
 */
export async function getCoursesApi() {
  // 获取原始数据
  const response = await requestClient.get<any[]>('/admin/course/getCourses');
  
  // 转换字段名格式
  return response.map(item => ({
    id: item.id,
    name: item.name,
    description: item.description,
    credit: item.credit,
    create_time: formatDate(item.createTime), // 转换字段名并格式化日期
    update_time: formatDate(item.updateTime), // 转换字段名并格式化日期
    is_deleted: item.isDeleted // 转换字段名
  })) as Course[];
}

/**
 * 获取课程详情
 */
export async function getCourseDetailApi(id: number) {
  const response = await requestClient.get<any>(`/admin/course/getCourse?id=${id}`);
  
  // 转换字段名格式
  return {
    id: response.id,
    name: response.name,
    description: response.description,
    credit: response.credit,
    create_time: formatDate(response.createTime),
    update_time: formatDate(response.updateTime),
    is_deleted: response.isDeleted
  } as Course;
}

/**
 * 搜索课程
 */
export async function searchCoursesApi(keyword: string) {
  const response = await requestClient.get<any[]>(`/admin/course/searchCourses?keyword=${encodeURIComponent(keyword)}`);
  
  // 转换字段名格式
  return response.map(item => ({
    id: item.id,
    name: item.name,
    description: item.description,
    credit: item.credit,
    create_time: formatDate(item.createTime),
    update_time: formatDate(item.updateTime),
    is_deleted: item.isDeleted
  })) as Course[];
}

/**
 * 添加课程
 */
export async function addCourseApi(data: Omit<Course, 'id' | 'create_time' | 'update_time'>) {
  // 转换为后端字段格式
  const requestData = {
    name: data.name,
    description: data.description,
    credit: data.credit,
    isDeleted: data.is_deleted
  };
  return requestClient.post<void>('/admin/course/addCourse', requestData);
}

/**
 * 更新课程
 */
export async function updateCourseApi(data: Partial<Course> & { id: number }) {
  // 转换为后端字段格式
  const requestData = {
    id: data.id,
    name: data.name,
    description: data.description,
    credit: data.credit,
    isDeleted: data.is_deleted
  };
  return requestClient.put<void>('/admin/course/updateCourse', requestData);
}

/**
 * 删除课程
 */
export async function deleteCourseApi(id: number) {
  return requestClient.delete<void>(`/admin/course/deleteCourse?id=${id}`);
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