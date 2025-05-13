import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { CourseBaseDTO, CourseQueryParams } from '@/types/course';
import * as courseApi from '@/api/course';
import { ElMessage } from 'element-plus';

export const useCourseStore = defineStore('course', () => {
  // 课程列表
  const courseList = ref<CourseBaseDTO[]>([]);
  // 当前课程详情
  const currentCourse = ref<CourseBaseDTO | null>(null);
  // 加载状态
  const loading = ref<boolean>(false);
  
  /**
   * 获取所有课程
   */
  async function fetchCourses() {
    loading.value = true;
    try {
      const response = await courseApi.getCourses();
      if (response.code === 20000 && response.data) {
        courseList.value = response.data;
        return response.data;
      } else {
        ElMessage.error(response.msg || '获取课程列表失败');
        return [];
      }
    } catch (error) {
      console.error('获取课程列表出错:', error);
      ElMessage.error('获取课程列表失败，请稍后重试');
      return [];
    } finally {
      loading.value = false;
    }
  }
  
  /**
   * 根据ID获取课程详情
   * @param id 课程ID
   */
  async function fetchCourseById(id: number) {
    loading.value = true;
    try {
      const response = await courseApi.getCourseById(id);
      if (response.code === 20000 && response.data) {
        currentCourse.value = response.data;
        return response.data;
      } else {
        ElMessage.error(response.msg || '获取课程详情失败');
        return null;
      }
    } catch (error) {
      console.error('获取课程详情出错:', error);
      ElMessage.error('获取课程详情失败，请稍后重试');
      return null;
    } finally {
      loading.value = false;
    }
  }
  
  /**
   * 搜索课程
   * @param keyword 搜索关键词
   */
  async function searchCourses(keyword: string) {
    loading.value = true;
    try {
      const response = await courseApi.searchCourses(keyword);
      if (response.code === 20000 && response.data) {
        // 只更新搜索结果，不修改主列表
        return response.data;
      } else {
        ElMessage.error(response.msg || '课程搜索失败');
        return [];
      }
    } catch (error) {
      console.error('课程搜索出错:', error);
      ElMessage.error('课程搜索失败，请稍后重试');
      return [];
    } finally {
      loading.value = false;
    }
  }
  
  /**
   * 添加课程
   * @param course 课程数据
   */
  async function addCourse(course: CourseBaseDTO) {
    loading.value = true;
    try {
      const response = await courseApi.addCourse(course);
      if (response.code === 20000) {
        ElMessage.success('添加课程成功');
        // 重新获取课程列表以保持数据同步
        await fetchCourses();
        return true;
      } else {
        ElMessage.error(response.msg || '添加课程失败');
        return false;
      }
    } catch (error) {
      console.error('添加课程出错:', error);
      ElMessage.error('添加课程失败，请稍后重试');
      return false;
    } finally {
      loading.value = false;
    }
  }
  
  /**
   * 更新课程
   * @param course 课程数据
   */
  async function updateCourse(course: CourseBaseDTO) {
    loading.value = true;
    try {
      const response = await courseApi.updateCourse(course);
      if (response.code === 20000) {
        ElMessage.success('更新课程成功');
        // 更新本地列表中的课程数据
        const index = courseList.value.findIndex(item => item.id === course.id);
        if (index !== -1) {
          courseList.value[index] = { ...courseList.value[index], ...course };
        }
        // 如果更新的是当前课程，也更新当前课程
        if (currentCourse.value && currentCourse.value.id === course.id) {
          currentCourse.value = { ...currentCourse.value, ...course };
        }
        return true;
      } else {
        ElMessage.error(response.msg || '更新课程失败');
        return false;
      }
    } catch (error) {
      console.error('更新课程出错:', error);
      ElMessage.error('更新课程失败，请稍后重试');
      return false;
    } finally {
      loading.value = false;
    }
  }
  
  /**
   * 删除课程
   * @param id 课程ID
   */
  async function deleteCourse(id: number) {
    loading.value = true;
    try {
      const response = await courseApi.deleteCourse(id);
      if (response.code === 20000) {
        ElMessage.success('删除课程成功');
        // 从本地列表中移除该课程
        courseList.value = courseList.value.filter(item => item.id !== id);
        // 如果删除的是当前课程，清空当前课程
        if (currentCourse.value && currentCourse.value.id === id) {
          currentCourse.value = null;
        }
        return true;
      } else {
        ElMessage.error(response.msg || '删除课程失败');
        return false;
      }
    } catch (error) {
      console.error('删除课程出错:', error);
      ElMessage.error('删除课程失败，请稍后重试');
      return false;
    } finally {
      loading.value = false;
    }
  }
  
  return {
    courseList,
    currentCourse,
    loading,
    fetchCourses,
    fetchCourseById,
    searchCourses,
    addCourse,
    updateCourse,
    deleteCourse
  };
});
