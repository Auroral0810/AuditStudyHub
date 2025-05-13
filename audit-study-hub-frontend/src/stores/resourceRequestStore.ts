import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getResourceRequestList, getResourceRequestDetail, createResourceRequest, updateResourceRequest, provideResource, addComment, deleteComment, getUserResourceRequests, deleteResourceRequest } from '../api/resourceRequest'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

export const useResourceRequestStore = defineStore('resourceRequest', () => {
  const loading = ref(false)
  const currentRequest = ref(null)
  
  // 获取资源请求列表
  async function getList(params: any) {
    loading.value = true
    try {
      const res = await getResourceRequestList(params)
      if (res.code === 20000) {
        return res.data
      } else {
        ElMessage.error(res.message || '获取资源请求列表失败')
        return {
          records: [],
          total: 0
        }
      }
    } catch (error) {
      console.error('获取资源请求列表失败:', error)
      ElMessage.error('获取资源请求列表失败')
      return {
        records: [],
        total: 0
      }
    } finally {
      loading.value = false
    }
  }
  
  // 获取资源请求详情
  async function getDetail(id: number) {
    loading.value = true
    try {
      const res = await getResourceRequestDetail(id)
      if (res.code === 20000) {
        currentRequest.value = res.data.request
        return res.data
      } else {
        ElMessage.error(res.message || '获取资源请求详情失败')
        return null
      }
    } catch (error) {
      console.error('获取资源请求详情失败:', error)
      ElMessage.error('获取资源请求详情失败')
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 创建资源请求
  async function createRequest(data: any, userId: number) {
    loading.value = true
    try {
      const res = await createResourceRequest(data, userId)
      if (res.code === 20000) {
        ElMessage.success('发布资源请求成功')
        return res.data
      } else {
        ElMessage.error(res.message || '发布资源请求失败')
        return null
      }
    } catch (error) {
      console.error('发布资源请求失败:', error)
      ElMessage.error('发布资源请求失败')
      return null
    } finally {
      loading.value = false
    }
  }

  // 更新资源请求
  async function updateRequest(id: number, data: any, userId: number) {
    loading.value = true
    try {
      const res = await updateResourceRequest(id, data, userId)
      if (res.code === 20000) {
        ElMessage.success('更新资源请求成功')
        return res.data
      } else {
        ElMessage.error(res.message || '更新资源请求失败')
        return null
      }
    } catch (error) {
      console.error('更新资源请求失败:', error)
      ElMessage.error('更新资源请求失败')
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 提供资源
  async function provideResourceSolution(data: any) {
    loading.value = true
    try {
      const res = await provideResource(data)
      if (res.code === 20000) {
        ElMessage.success('提供资源成功')
        return res.data
      } else {
        ElMessage.error(res.message || '提供资源失败')
        return null
      }
    } catch (error) {
      console.error('提供资源失败:', error)
      ElMessage.error('提供资源失败')
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 添加评论
  async function addCommentToRequest(data: any) {
    loading.value = true
    try {
      const res = await addComment(data)
      if (res.code === 20000) {
        ElMessage.success('评论发布成功')
        return res.data
      } else {
        ElMessage.error(res.message || '评论失败')
        return null
      }
    } catch (error) {
      console.error('发布评论失败', error)
      ElMessage.error('发布评论失败')
      return null
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 删除评论
   * @param commentId 评论ID
   * @param userId 用户ID
   */
  const deleteCommentRequest = async (commentId: number, userId: number) => {
    loading.value = true
    try {
      const result = await deleteComment(commentId, userId)
      if (result.code === 20000) {
        ElMessage.success('评论删除成功')
        return result.data
      }
      throw new Error(result.message || '操作失败')
    } catch (error) {
      console.error('删除评论失败:', error)
      ElMessage.error('删除评论失败')
      return false
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 获取用户的资源请求列表
   * @param userId 用户ID
   * @param params 分页和筛选参数
   */
  const getUserRequests = async (userId: number, params: any) => {
    loading.value = true
    try {
      const res = await getUserResourceRequests(userId, params)
      if (res.code === 20000) {
        return {
          records: res.data.list,
          total: res.data.total
        }
      } else {
        ElMessage.error(res.message || '获取用户资源请求列表失败')
        return {
          records: [],
          total: 0
        }
      }
    } catch (error) {
      console.error('获取用户资源请求列表失败:', error)
      ElMessage.error('获取用户资源请求列表失败')
      return {
        records: [],
        total: 0
      }
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 删除资源请求
   * @param id 资源请求ID
   */
  const deleteRequest = async (id: number) => {
    loading.value = true
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录');
        return false;
      }
      
      const userId = userStore.userInfo.id;
      const res = await deleteResourceRequest(id, userId);
      
      if (res.code === 20000) {
        ElMessage.success('删除资源请求成功');
        return true;
      } else {
        ElMessage.error(res.message || '删除资源请求失败');
        return false;
      }
    } catch (error) {
      console.error('删除资源请求失败:', error);
      ElMessage.error('删除资源请求失败');
      return false;
    } finally {
      loading.value = false;
    }
  }
  
  return {
    loading,
    currentRequest,
    getList,
    getDetail,
    createRequest,
    updateRequest,
    provideResourceSolution,
    addCommentToRequest,
    deleteComment: deleteCommentRequest,
    getUserRequests,
    deleteRequest
  }
})
