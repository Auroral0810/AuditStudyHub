import request from '../utils/request'

// 获取资源请求列表
export function getRequestList(params: {
  current: number
  size: number
  collegeId?: number
  majorId?: number
  courseId?: number
  status?: number
  keyword?: string
}) {
  return request.get('/request/list', params)
}

// 获取资源请求详情
export function getRequestDetail(requestId: number) {
  return request.get(`/request/${requestId}`)
}

// 创建资源请求
export function createRequest(data: {
  title: string
  content: string
  collegeId?: number
  majorId?: number
  courseId?: number
}) {
  return request.post('/request/create', data)
}

// 更新资源请求
export function updateRequest(data: {
  id: number
  title: string
  content: string
  collegeId?: number
  majorId?: number
  courseId?: number
}) {
  return request.put('/request/update', data)
}

// 删除资源请求
export function deleteRequest(requestId: number) {
  return request.delete(`/request/${requestId}`)
}

// 回复资源请求
export function replyRequest(requestId: number, content: string) {
  return request.post(`/request/${requestId}/reply`, { content })
}

// 获取请求回复列表
export function getReplies(requestId: number, params: {
  current: number
  size: number
}) {
  return request.get(`/request/${requestId}/replies`, params)
}

// 获取用户发布的请求
export function getUserRequests(params: {
  current: number
  size: number
}) {
  return request.get('/request/my-requests', params)
}

// 标记请求为已解决
export function resolveRequest(requestId: number) {
  return request.post(`/request/${requestId}/resolve`)
}

// 关闭请求
export function closeRequest(requestId: number) {
  return request.post(`/request/${requestId}/close`)
}

// 管理员设置请求状态
export function setRequestStatus(requestId: number, status: number) {
  return request.post(`/request/${requestId}/status`, { status })
}
