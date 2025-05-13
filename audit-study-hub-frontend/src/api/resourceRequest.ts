import request from '../utils/request'

// 获取资源请求列表
export function getResourceRequestList(params: any) {
  return request.get('/resource-request/list', { params })
}

// 获取资源请求详情
export function getResourceRequestDetail(id: number) {
  return request.get(`/resource-request/detail/${id}`)
}

// 创建资源请求
export function createResourceRequest(data: any, userId: number) {
  return request.post(`/resource-request/create?userId=${userId}`, data)
}

// 更新资源请求
export function updateResourceRequest(id: number, data: any, userId: number) {
  return request.put(`/resource-request/update/${id}?userId=${userId}`, data)
}

// 提供资源解决方案
export function provideResource(data: any) {
  return request.post('/resource-request/provide-resource', data)
}

// 添加评论
export function addComment(data: any) {
  return request.post('/resource-request/comment/add', data)
}

// 删除评论
export function deleteComment(id: number, userId: number) {
  return request.post(`/resource-request/comment/delete/${id}`, { id, userId })
}

// 获取用户资源请求
export function getUserResourceRequests(userId: number, params: any) {
  return request.get(`/resource-request/user/${userId}`, { params })
}

// 删除资源请求
export function deleteResourceRequest(id: number, userId: number) {
  return request.delete(`/resource-request/${id}?userId=${userId}`)
}
