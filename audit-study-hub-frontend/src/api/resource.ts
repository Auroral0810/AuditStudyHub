import request from '../utils/request'
import axios from 'axios'

// 获取最新资源
export function getLatestResources(limit = 4) {
  return request.get('/resource/latest', { params: { limit } })
}

// 获取热门资源
export function getPopularResources(limit = 4) {
  return request.get('/resource/popular', { params: { limit } })
}

// 获取资源列表
export function getResourceList(params: {
  current: number
  size: number
  categoryId?: number
  collegeId?: number
  majorId?: number
  courseId?: number
  keyword?: string
}) {
  return request.get('/resource/list', { params })
}

// 获取资源详情
export function getResourceDetail(resourceId: number) {
  return request.get(`/resource/detail/${resourceId}`)
}

// 获取资源详情（包含评论）
export function getResourceDetailWithComments(resourceId: number, userId?: number) {
  const params = userId ? { userId } : {};
  return request.get(`/resource/detail-with-comments/${resourceId}`, { params });
}

// 上传资源
export function uploadResource(data: FormData) {
  return request.post('/resource/upload', data, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新资源
export function updateResource(resourceId: number, data: {
  title: string
  description: string
  categoryId: number
  collegeId?: number
  majorId?: number
  courseId?: number
  tags?: string
}) {
  return request.put(`/resource/update/${resourceId}`, data)
}

// 删除资源
export function deleteResource(resourceId: number) {
  return request.delete(`/resource/delete/${resourceId}`)
}

// 下载资源
export function downloadResource(resourceId: number) {
  return request.get(`/resource/download/${resourceId}`)
}

// 点赞/取消点赞资源
export function likeResource(userId: number, resourceId: number) {
  return request.post(`/resource/like/${resourceId}`, { userId })
}

// 收藏/取消收藏资源
export function collectResource(userId: number, resourceId: number) {
  return request.post(`/resource/collect/${resourceId}`, { userId })
}

// 获取用户上传的资源
export function getUserUploadResources(params: {
  userId?: number
  current: number
  size: number
}) {
  return request.get('/resource/user/uploads', { params })
}

// 获取用户收藏的资源
export function getUserCollectedResources(params: {
  userId?: number
  current: number
  size: number
}) {
  return request.get('/resource/user/collections', { params })
}

// 获取相关资源
export function getRelatedResources(resourceId: number, limit: number = 5) {
  return request.get(`/resource/related/${resourceId}`, { params: { limit } })
}

// 审核资源
export function reviewResource(resourceId: number, status: number, reason?: string) {
  return request.post(`/resource/review/${resourceId}`, { status, reason })
}

// 获取热门搜索词
export function getHotSearchKeywords(limit = 5) {
  return request.get('/resource/hot-search', { params: { limit } })
}

// 获取搜索建议
export function getSearchSuggestions(prefix: string, categoryId?: number, limit = 5) {
  
  return request.get('/resource/suggest', { 
    params: { 
      prefix,
      categoryId,
      limit 
    } 
  }).then(response => {
    
    
    // 检查response对象的结构
    if (response && typeof response === 'object') {
      
      
      // 检查是否有data字段
      if ('data' in response) {
        
      }
    }
    
    return response; // 返回原始响应让store处理
  }).catch(error => {
    console.error('[API] 获取搜索建议API错误:', error);
    throw error;
  });
}

// 记录搜索关键词
export function recordSearchKeyword(keyword: string) {
  // 使用JSON格式数据
  const data = { keyword };
  
  
  
  // 使用封装的request以保持一致的错误处理
  return request.post('/resource/record-search', data, {
    headers: {
      'Content-Type': 'application/json'
    }
  });
}

// 获取用户上传记录
export function getUserUploads(params: {
  userId: number,
  page?: number,
  size?: number,
  keyword?: string
}) {
  return request.get('/resource/my-uploads', { params })
} 