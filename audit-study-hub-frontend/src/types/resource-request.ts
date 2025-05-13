// 资源请求类型定义
export interface ResourceRequest {
  id: number
  title: string
  content: string
  userId: number
  username: string
  userAvatar?: string
  collegeId?: number
  collegeName?: string
  majorId?: number
  majorName?: string
  publisherCollegeId?: number
  publisherCollegeName?: string
  publisherMajorId?: number
  publisherMajorName?: string
  courseId?: number | null
  courseName?: string | null
  categoryId?: number | null
  categoryName?: string | null
  resourceType?: string
  status: number // 0: 待解决, 1: 已解决
  resourceId?: number
  replyCount: number
  viewCount: number
  createTime: string
  updateTime: string
}

// 资源请求创建参数
export interface ResourceRequestCreateParams {
  title: string
  content: string
  courseName?: string
  categoryId?: number
  collegeId: number
  majorId?: number
}

// 资源请求更新参数
export interface ResourceRequestUpdateParams {
  title: string
  content: string
  courseName?: string
  categoryId?: number
  collegeId: number
  majorId?: number
}

// 资源请求查询参数
export interface ResourceRequestQueryParams {
  page: number
  size: number
  keyword?: string
  collegeId?: number
  majorId?: number
  categoryId?: number
  status?: number
  sort?: string
}

// 提供资源解决方案参数
export interface ProvideResourceParams {
  requestId: number
  resourceId: number
  remark?: string
} 