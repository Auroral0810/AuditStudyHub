// src/types/course.ts

// 课程基础数据类型（用于前端展示）
export interface CourseBaseDTO {
  id?: number;
  name: string;
  credit?: number;
  description?: string;
}

// 课程完整数据类型（包含系统字段）
export interface CourseDTO extends CourseBaseDTO {
  createTime?: string;
  updateTime?: string;
  isDeleted?: number;
}

// 课程搜索查询参数
export interface CourseQueryParams {
  keyword?: string;
  pageNum?: number;
  pageSize?: number;
}
