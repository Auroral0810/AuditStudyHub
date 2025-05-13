import type { Resource } from '../../views/resource/list-data';

import { requestClient } from '#/api/request';
// 管理员资源搜索请求参数定义
export interface AdminResourceSearchRequest {
    page: number; // 页码
    size: number; // 每页数量
    title?: string; // 标题
    collegeId?: number; // 学院ID
    majorId?: number; // 专业ID
    categoryId?: number; // 分类ID
    fileType?: string; // 文件类型
    status?: number; // 状态
    isDeleted?: number; // 是否删除
    uploaderName?: string; // 上传者名称
    startDate?: string; // 开始日期
    endDate?: string; // 结束日期
    minSize?: number; // 最小文件大小
    maxSize?: number; // 最大文件大小
    sort?: string; // 排序方式
    userId?: number;
} 

/**
 * 获取所有资源列表（管理员版本，包含所有资源，包括已删除的）
 */
export function getResourcesApi() {
  return requestClient.get<any[]>("/admin/resource/list");
}

/**
 * 获取资源详情
 */
export function getResourceDetailApi(id: number) {
  return requestClient.get<any>(`/admin/resource/detail?id=${id}`);
}

/**
 * 搜索资源列表，支持多条件
 */
export function searchResourcesApi(params: AdminResourceSearchRequest) {
  return requestClient.post<{
    total: number;
    items: any[];
  }>("/admin/resource/search", params);
}

/**
 * 更新资源状态（逻辑删除或恢复）
 */
export function updateResourceStatusApi(id: number, isDeleted: number) {
  return requestClient.put<boolean>("/admin/resource/update-status", null, {
    params: { id, isDeleted }
  });
}

/**
 * 删除资源
 */
export function deleteResourceApi(id: number) {
  return requestClient.delete<boolean>(`/admin/resource/delete/${id}`);
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

/**
 * 添加新资源
 */
export function addResourceApi(data: any) {
  return requestClient.post<number>("/admin/resource/add", data);
}

/**
 * 更新资源信息
 */
export function updateResourceApi(data: any) {
  return requestClient.put<boolean>("/admin/resource/update", data);
}

/**
 * 触发全量ES同步
 */
export function triggerSyncApi() {
  return new Promise<string>((resolve, reject) => {
    // 使用原始XHR处理，以便更好地控制响应处理
    const xhr = new XMLHttpRequest();
    xhr.open('POST', '/api/admin/resource/sync');
    
    xhr.onload = function() {
      if (xhr.status >= 200 && xhr.status < 300) {
        // 成功状态
        resolve(xhr.responseText);
      } else {
        // 如果返回的是成功消息但状态码不对，也视为成功
        if (xhr.responseText && xhr.responseText.includes('成功')) {
          resolve(xhr.responseText);
        } else {
          reject(new Error(`请求失败: ${xhr.status} ${xhr.statusText}`));
        }
      }
    };
    
    xhr.onerror = function() {
      // 尝试解析错误响应
      try {
        if (xhr.responseText && xhr.responseText.includes('成功')) {
          resolve(xhr.responseText);
        } else {
          reject(new Error('网络请求失败'));
        }
      } catch (e) {
        reject(new Error('网络请求失败'));
      }
    };
    
    xhr.send();
  });
}
