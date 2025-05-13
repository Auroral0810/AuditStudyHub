import { requestClient } from '#/api/request';
import type { Category } from '../../views/content/category-data';

/**
 * 获取所有分类列表（管理员版本，包含所有字段）
 */
export async function getCategoriesApi() {
  // 获取原始数据
  const response = await requestClient.get<any[]>('/admin/category/list');
  
  // 转换字段名格式
  return response.map(item => ({
    id: item.id,
    name: item.name,
    icon: item.icon,
    sort: item.sort,
    create_time: item.createTime, // 转换字段名
    update_time: item.updateTime, // 转换字段名
    is_deleted: item.isDeleted // 转换字段名
  })) as Category[];
}

/**
 * 添加分类
 */
export async function addCategoryApi(data: Omit<Category, 'id' | 'create_time' | 'update_time'>) {
  return requestClient.post<Category>('/admin/category/add', data);
}

/**
 * 更新分类
 */
export async function updateCategoryApi(data: Partial<Category> & { id: number }) {
  return requestClient.put<Category>('/admin/category/update', data);
}

/**
 * 删除分类
 */
export async function deleteCategoryApi(id: number) {
  return requestClient.delete<boolean>(`/admin/category/delete/${id}`);
}
