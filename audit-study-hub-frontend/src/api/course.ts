import { get, post, put, del } from '@/utils/request';
import type { CourseBaseDTO, CourseDTO, CourseQueryParams } from '@/types/course';

/**
 * 获取所有课程
 */
export function getCourses() {
  return get<{
    code: number;
    msg: string;
    data: CourseBaseDTO[];
  }>('/course/getCourses');
}

/**
 * 根据ID获取课程详情
 * @param id 课程ID
 */
export function getCourseById(id: number) {
  return get<{
    code: number;
    msg: string;
    data: CourseBaseDTO;
  }>('/course/getCourse', {
    params: { id }
  });
}

/**
 * 添加课程
 * @param course 课程数据
 */
export function addCourse(course: CourseBaseDTO) {
  return post<{
    code: number;
    msg: string;
  }>('/course/addCourse', course);
}

/**
 * 更新课程
 * @param course 课程数据
 */
export function updateCourse(course: CourseBaseDTO) {
  return put<{
    code: number;
    msg: string;
  }>('/course/updateCourse', course);
}

/**
 * 删除课程
 * @param id 课程ID
 */
export function deleteCourse(id: number) {
  return del<{
    code: number;
    msg: string;
  }>('/course/deleteCourse', {
    params: { id }
  });
}

/**
 * 搜索课程
 * @param keyword 搜索关键词
 */
export function searchCourses(keyword: string) {
  return get<{
    code: number;
    msg: string;
    data: CourseBaseDTO[];
  }>('/course/searchCourses', {
    params: { keyword }
  });
}
