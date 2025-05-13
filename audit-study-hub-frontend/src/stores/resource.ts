import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../utils/request'
import { getLatestResources, getPopularResources } from '../api/resource'
import { ElMessage } from 'element-plus'
import { useUserStore } from './user'

// 资源类型定义
export interface Resource {
  id: number
  title: string
  description: string
  fileUrl: string | null
  fileType: string
  fileSize: number
  downloadCount: number
  viewCount: number
  likeCount: number
  uploaderId: number
  collegeId: number
  collegeName?: string
  majorId: number
  majorName?: string
  courseId?: number
  courseName?: string
  tagIds?: string
  tags: string[] | string
  categoryId: number
  categoryName?: string
  category?: string
  status: number
  createdAt?: string
  updatedAt?: string
  createTime?: string
  updateTime?: string
  isLiked?: boolean
  isCollected?: boolean
  uploaderName?: string
  uploaderAvatar?: string
  uploaderCollege?: string
  uploaderMajor?: string
  uploaderTotalUploads?: number
  comments?: Comment[]
  commentCount?: number
}

export interface Comment {
  id: number
  resourceId: number
  userId: number
  username: string
  userAvatar: string
  content: string
  createdAt: string
  updatedAt: string
  replyTo?: number
  replyToUsername?: string
  children?: Comment[]
  likes?: number
  liked?: boolean
  isDeleted?: number | boolean
}

// 用户点赞记录数据类型
export interface UserLikeRecord {
  id: number
  userId: number
  resourceId: number
  resourceTitle: string
  categoryName: string
  coverUrl: string | null
  uploaderName: string
  fileType: string
  likedAt: string
}

// 用户收藏记录数据类型
export interface UserCollectionRecord {
  id: number
  userId: number
  resourceId: number
  resourceTitle: string
  categoryName: string
  coverUrl: string | null
  uploaderName: string
  fileType: string
  collectedAt: string
}

// 用户下载记录数据类型
export interface UserDownloadRecord {
  id: number
  userId: number
  resourceId: number
  resourceTitle: string
  categoryName: string
  coverUrl: string | null
  uploaderName: string
  fileType: string
  fileSize: number
  downloadedAt: string
}

export const useResourceStore = defineStore('resource', () => {
  const loading = ref(false)
  const resourceList = ref<Resource[]>([])
  const total = ref(0)
  const currentResource = ref<Resource | null>(null)
  
  // 获取最新资源
  async function getLatestResourceList(limit: number = 4) {
    try {
      const res = await request.get('/resource/latest', { 
        params: { limit } 
      })
      if (res.code === 20000) {
        return res.data
      }
      return []
    } catch (error) {
      console.error('获取最新资源列表失败', error)
      return []
    }
  }
  
  // 获取热门资源
  async function getPopularResourceList(limit: number = 4) {
    try {
      const res = await request.get('/resource/popular', { 
        params: { limit } 
      })
      if (res.code === 20000) {
        console.log(res.data)
        return res.data
      }
      return []
    } catch (error) {
      console.error('获取热门资源列表失败', error)
      return []
    }
  }
  
  // 获取资源列表
  async function getResourceList(params: any) {
    loading.value = true
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      // 如果用户已登录，添加userId参数
      if (userStore.token && userStore.userInfo?.id) {
        params.userId = userStore.userInfo.id;
        console.log("添加用户ID到参数:", params); // 输出完整参数对象
      }
      
      const res = await request.get('/resource/list', { params })
      console.log(res)
      if (res.code === 20000) {
        console.log(res.data)
        // 处理数据，确保每个资源对象具有所需的UI属性
        const processedList = res.data.list.map((item: Resource) => ({
          ...item,
          // 添加前端需要的默认属性，使用后端返回的isCollected和isLiked
          selected: false,
          isCollected: item.isCollected === null ? false : item.isCollected,
          isLiked: item.isLiked === null ? false : item.isLiked,
          // 使用后端返回的上传者用户名，如果没有则使用默认值
          username: item.uploaderName || `用户${item.uploaderId}`
        }))
        
        resourceList.value = processedList
        total.value = res.data.total
        
        return {
          list: processedList,
          total: res.data.total
        }
      }
      return { list: [], total: 0 }
    } catch (error) {
      console.error('获取资源列表失败', error)
      return { list: [], total: 0 }
    } finally {
      loading.value = false
    }
  }
  
  // 获取资源详情
  const getResourceDetail = async (id: number) => {
    loading.value = true
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      let params = {};
      
      // 如果用户已登录，添加userId参数
      if (userStore.token && userStore.userInfo?.id) {
        params = { userId: userStore.userInfo.id };
      }
      
      const res = await request.get<{
        code: number
        data: Resource
        message: string
      }>(`/resource/detail/${id}`, { params })
      
      if (res.code === 200) {
        currentResource.value = res.data
        return Promise.resolve(res.data)
      }
      return Promise.reject(res.message)
    } catch (error) {
      return Promise.reject(error)
    } finally {
      loading.value = false
    }
  }
  
  // 获取资源详情(包含评论)
  const getResourceDetailWithComments = async (id: number) => {
    loading.value = true
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      let params = {};
      
      // 如果用户已登录，添加userId参数
      if (userStore.token && userStore.userInfo?.id) {
        params = { userId: userStore.userInfo.id };
      }
      
      const res = await request.get(`/resource/detail-with-comments/${id}`, { params })
      
      if (res.code === 20000) {
        // 设置当前资源信息
        currentResource.value = res.data.resource;
        
        // 处理评论数据格式，确保和界面展示兼容
        const processedComments = res.data.comments.map((comment: any) => {
          // 确保各种日期字段的存在和转换
          const createdAt = comment.createTime || comment.createdAt || null;
          const updatedAt = comment.updateTime || comment.updatedAt || null;
          
          return {
            ...comment,
            id: comment.id,
            resourceId: comment.resourceId,
            userId: comment.userId,
            username: comment.username,
            userAvatar: comment.userAvatar,
            content: comment.content,
            createdAt: createdAt,
            updatedAt: updatedAt,
            replyTo: comment.parentId,
            replyToUsername: comment.replyToUsername,
            isDeleted: comment.isDeleted,
            children: comment.children ? comment.children.map((child: any) => {
              // 子评论也进行同样的日期字段处理
              const childCreatedAt = child.createTime || child.createdAt || null;
              const childUpdatedAt = child.updateTime || child.updatedAt || null;
              
              return {
                ...child,
                createdAt: childCreatedAt,
                updatedAt: childUpdatedAt,
                replyTo: child.parentId,
                isDeleted: child.isDeleted
              };
            }) : []
          };
        });
        
        return {
          resource: res.data.resource,
          comments: processedComments,
          commentCount: res.data.commentCount
        };
      }
      return { resource: null, comments: [], commentCount: 0 };
    } catch (error) {
      console.error('获取资源详情(包含评论)失败', error);
      return { resource: null, comments: [], commentCount: 0 };
    } finally {
      loading.value = false;
    }
  }
  
  // 上传资源
  const uploadResource = async (data: FormData | {
    title: string
    description: string
    categoryId: number
    categoryName?: string
    collegeId: number
    collegeName?: string
    majorId?: number
    majorName?: string
    courseName?: string
    tags?: string[]
    file?: File
    fileUrl?: string
    fileSize?: number
    fileType?: string
  }) => {
    loading.value = true
    
    let formData: FormData;
    
    // 检查data是否已经是FormData实例
    if (data instanceof FormData) {
      formData = data;
      
      // 添加用户ID
      const userStore = useUserStore();
      if (userStore.token && userStore.userInfo?.id) {
        // 检查formData中是否已有uploaderId
        if (!formData.has('uploaderId')) {
          formData.append('uploaderId', userStore.userInfo.id.toString());
        }
      }
    } else {
      // 如果不是FormData，则创建一个并填充数据
      formData = new FormData();
      formData.append('title', data.title);
      formData.append('description', data.description);
      formData.append('categoryId', data.categoryId.toString());
      
      // 添加用户ID
      const userStore = useUserStore();
      if (userStore.token && userStore.userInfo?.id) {
        formData.append('uploaderId', userStore.userInfo.id.toString());
      }
      
      if (data.categoryName) {
        formData.append('categoryName', data.categoryName);
      }
      
      formData.append('collegeId', data.collegeId.toString());
      
      if (data.collegeName) {
        formData.append('collegeName', data.collegeName);
      }
      
      if (data.majorId) {
        formData.append('majorId', data.majorId.toString());
      }
      
      if (data.majorName) {
        formData.append('majorName', data.majorName);
      }
      
      if (data.courseName) {
        formData.append('courseName', data.courseName);
      }
      
      if (data.tags && data.tags.length > 0) {
        formData.append('tags', JSON.stringify(data.tags));
      }
      
      // 如果有文件，添加文件
      if (data.file) {
        formData.append('file', data.file);
      }
      
      // 如果有文件URL，添加文件URL
      if (data.fileUrl) {
        formData.append('fileUrl', data.fileUrl);
      }
      
      // 添加文件大小和类型
      if (data.fileSize) {
        formData.append('fileSize', data.fileSize.toString());
      }
      
      if (data.fileType) {
        formData.append('fileType', data.fileType);
      }
    }
    
    try {
      const response = await request.post('/resource/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      
      // 检查响应的结构，确保正确访问code字段
      if (response && typeof response === 'object' && 'code' in response) {
        if (response.code === 20000) {
          return Promise.resolve(response.data)
        }
        return Promise.reject(response.message || '上传失败')
      }
      
      // 如果响应没有预期的结构，返回通用错误
      return Promise.reject('服务器返回数据格式异常')
    } catch (error) {
      console.error('上传资源失败', error)
      return Promise.reject(error)
    } finally {
      loading.value = false
    }
  }
  
  // 删除资源
  const deleteResource = async (id: number) => {
    loading.value = true
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录');
        return Promise.reject('用户未登录');
      }
      
      const userId = userStore.userInfo.id;
      
      const res = await request.delete<{
        code: number
        message: string
      }>(`/resource/my-uploads/${id}`, {
        params: { userId }
      })
      
      if (res.code === 20000) {
        // 从列表中移除
        resourceList.value = resourceList.value.filter(item => item.id !== id)
        
        // 如果当前正在查看这个资源，清空currentResource
        if (currentResource.value && currentResource.value.id === id) {
          currentResource.value = null
        }
        
        return Promise.resolve(res)
      }
      return Promise.reject(res.message)
    } catch (error) {
      return Promise.reject(error)
    } finally {
      loading.value = false
    }
  }
  
  // 下载资源
  async function downloadResource(id: number) {
    try {
      // 首先尝试从已加载的资源列表中查找
      const resourceInList = resourceList.value.find(item => item.id === id);
      // 或者从当前查看的资源中获取
      const resourceInCurrent = currentResource.value?.id === id ? currentResource.value : null;
      
      // 获取文件URL
      let fileUrl = null;
      if (resourceInList && resourceInList.fileUrl) {
        fileUrl = resourceInList.fileUrl;
      } else if (resourceInCurrent && resourceInCurrent.fileUrl) {
        fileUrl = resourceInCurrent.fileUrl;
      }
      
      if (fileUrl) {
        // 使用a标签直接下载文件，避免打开新窗口
        const link = document.createElement('a');
        link.href = fileUrl;
        link.setAttribute('download', ''); // 强制下载而不是导航
        link.style.display = 'none';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        ElMessage.success('下载已开始，请稍候...');
        
        // 增加前端显示的下载数
        if (resourceInList) resourceInList.downloadCount++;
        if (resourceInCurrent) resourceInCurrent.downloadCount++;
        
        // 异步调用后端接口记录下载
        recordDownload(id);
        
        return { downloadUrl: fileUrl };
      }
      
      ElMessage.error('无法获取资源下载链接');
      return null;
    } catch (error) {
      console.error('下载资源失败', error);
      ElMessage.error('下载失败，请稍后重试');
      return null;
    }
  }
  
  // 异步记录下载统计和用户下载记录
  async function recordDownload(resourceId: number) {
    try {
      // 调用后端接口增加下载次数
      await request.post(`/resource/record-download/${resourceId}`);
      
      // 如果用户已登录，还需要记录用户下载历史
      const userStore = useUserStore();
      if (userStore.token && userStore.userInfo?.id) {
        await request.post('/resource/user/record-download', {
          resourceId: resourceId,
          userId: userStore.userInfo.id
        });
      }
    } catch (error) {
      console.error('记录下载统计失败', error);
      // 这里不需要向用户显示错误，因为下载已经开始
    }
  }
  
  // 批量下载资源
  async function batchDownloadResources(ids: number[]) {
    try {
      // 查找已加载的资源列表中有下载链接的资源
      const resourcesInList = resourceList.value.filter(item => ids.includes(item.id));
      const resourcesWithUrl = resourcesInList.filter(item => item.fileUrl);
      
      // 提示用户即将下载多个文件
      if (resourcesWithUrl.length === 0) {
        ElMessage.warning('没有可下载的资源');
        return { success: false, count: 0 };
      }
      
      if (resourcesWithUrl.length > 1) {
        ElMessage.warning(`即将下载${resourcesWithUrl.length}个文件，请注意浏览器设置可能会阻止批量下载`);
        await new Promise(resolve => setTimeout(resolve, 1000));
      }
      
      // 逐个处理每个下载项
      let downloadedCount = 0;
      for (let i = 0; i < resourcesWithUrl.length; i++) {
        const resource = resourcesWithUrl[i];
        if (resource.fileUrl) {
          // 使用a标签下载
          const link = document.createElement('a');
          link.href = resource.fileUrl;
          link.setAttribute('download', '');
          link.style.display = 'none';
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          
          downloadedCount++;
          
          // 增加下载数
          resource.downloadCount++;
          
          // 异步记录下载
          recordDownload(resource.id);
          
          // 每个下载之间稍作延迟，避免浏览器阻止批量下载
          if (i < resourcesWithUrl.length - 1) {
            await new Promise(resolve => setTimeout(resolve, 500));
          }
        }
      }
      
      // 显示下载成功消息
      if (downloadedCount > 0) {
        ElMessage.success(`${downloadedCount}个文件下载已开始`);
      } else {
        ElMessage.warning('没有可下载的资源');
      }
      
      return { success: true, count: downloadedCount };
    } catch (error) {
      console.error('批量下载资源失败', error);
      ElMessage.error('下载失败，请稍后重试');
      return null;
    }
  }
  
  // 点赞/取消点赞资源
  async function likeResource(resourceId: number) {
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录再点赞');
        return { liked: false, likeCount: 0 };
      }
      
      const userId = userStore.userInfo.id;
      
      // 调用API
      const res = await request.post(`/resource/like/${resourceId}`, { userId });
      
      if (res.code === 20000) {
        // 获取返回的点赞状态和点赞数
        const liked = res.data?.liked === true;
        const likeCount = res.data?.likeCount || 0;
        
        // 更新资源点赞状态和计数
        const index = resourceList.value.findIndex(item => item.id === resourceId);
        if (index !== -1) {
          resourceList.value[index].likeCount = likeCount;
          resourceList.value[index].isLiked = liked;
        }
        
        // 如果当前正在查看这个资源，更新currentResource
        if (currentResource.value && currentResource.value.id === resourceId) {
          currentResource.value.likeCount = likeCount;
          currentResource.value.isLiked = liked;
        }
        
        return { liked, likeCount };
      }
      return { liked: false, likeCount: 0 };
    } catch (error) {
      console.error('点赞资源失败', error);
      return { liked: false, likeCount: 0 };
    }
  }
  
  // 收藏/取消收藏资源
  async function collectResource(resourceId: number) {
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录再收藏资源');
        return { collected: false };
      }
      
      const userId = userStore.userInfo.id;
      
      // 调用API
      const res = await request.post(`/resource/collect/${resourceId}`, { userId });
      
      if (res.code === 20000) {
        // 获取返回的收藏状态
        const collected = res.data?.collected === true;
        
        // 更新资源收藏状态
        const index = resourceList.value.findIndex(item => item.id === resourceId);
        if (index !== -1) {
          resourceList.value[index].isCollected = collected;
        }
        
        // 如果当前正在查看这个资源，更新currentResource
        if (currentResource.value && currentResource.value.id === resourceId) {
          currentResource.value.isCollected = collected;
        }
        
        return { collected };
      }
      return { collected: false };
    } catch (error) {
      console.error('收藏资源失败', error);
      return { collected: false };
    }
  }
  
  // 检查收藏状态
  const checkCollectionStatus = async (id: number) => {
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        return Promise.resolve(false);
      }
      
      const res = await request.get<{
        code: number
        data: {
          collected: boolean
        }
        message: string
      }>(`/resource/collect/check/${id}`, {
        params: { userId: userStore.userInfo.id }
      })
      
      if (res.code === 200) {
        return Promise.resolve(res.data.collected)
      }
      return Promise.resolve(false)
    } catch (error) {
      return Promise.resolve(false)
    }
  }
  
  // 检查点赞状态
  const checkLikeStatus = async (id: number) => {
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        return Promise.resolve(false);
      }
      
      const res = await request.get<{
        code: number
        data: {
          liked: boolean
        }
        message: string
      }>(`/resource/like/check/${id}`, {
        params: { userId: userStore.userInfo.id }
      })
      
      if (res.code === 200) {
        return Promise.resolve(res.data.liked)
      }
      return Promise.resolve(false)
    } catch (error) {
      return Promise.resolve(false)
    }
  }
  
  // 获取评论列表
  const getComments = async (resourceId: number) => {
    loading.value = true
    try {
      const res = await request.get<{
        code: number
        data: {
          list: Comment[]
          total: number
        }
        message: string
      }>(`/resource/comments/${resourceId}`)
      
      if (res.code === 200) {
        return Promise.resolve(res.data)
      }
      return Promise.reject(res.message)
    } catch (error) {
      return Promise.reject(error)
    } finally {
      loading.value = false
    }
  }
  
  // 添加评论
  const addComment = async (data: {
    resourceId: number
    content: string
    replyTo?: number
  }) => {
    loading.value = true
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录');
        return Promise.reject('用户未登录');
      }
      
      const userId = userStore.userInfo.id;
      const commentData = {
        ...data,
        userId
      };
      
      const res = await request.post('/resource/comment/add', commentData)
      
      // 检查直接返回的响应
      if (res.code === 20000 || (res.data && res.data.code === 20000)) {
        console.log('评论添加成功', res.code === 20000 ? res.data : res.data.data)
        // 返回评论对象
        return Promise.resolve(res.code === 20000 ? res.data : res.data.data)
      }
      
      // 检查响应中是否包含成功信息
      if (res.message && res.message.includes('操作成功') && res.data) {
        return Promise.resolve(res.data);
      }
      
      return Promise.reject(res.message || '添加评论失败')
    } catch (error: any) {
      console.error('评论失败详细错误:', error)
      
      // 检查错误响应中是否包含成功信息
      if (error.response && error.response.data) {
        const responseData = error.response.data;
        if (responseData.code === 20000 && responseData.data) {
          console.log('从错误响应中提取成功数据', responseData.data);
          return Promise.resolve(responseData.data);
        }
      }
      
      // 检查错误消息是否实际上是成功响应
      if (error.message && typeof error.message === 'string') {
        if (error.message.includes('操作成功') || error.message.includes('"code":20000')) {
          try {
            // 尝试从错误消息中提取JSON数据
            const jsonStartIndex = error.message.indexOf('{');
            if (jsonStartIndex >= 0) {
              const jsonStr = error.message.substring(jsonStartIndex);
              const responseData = JSON.parse(jsonStr);
              if (responseData.code === 20000 && responseData.data) {
                console.log('从错误消息中提取成功数据', responseData.data);
                return Promise.resolve(responseData.data);
              }
            }
          } catch (parseError) {
            console.error('解析错误消息中的JSON失败', parseError);
          }
        }
      }
      
      return Promise.reject(error)
    } finally {
      loading.value = false
    }
  }
  
  // 删除评论
  const deleteComment = async (commentId: number, resourceId: number) => {
    loading.value = true
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录');
        return Promise.reject('用户未登录');
      }
      
      const userId = userStore.userInfo.id;
      const res = await request.delete(`/resource/comment/delete/${commentId}`, {
        data: { userId }
      })
      
      if (res.code === 20000) {
        console.log('评论删除成功响应:', res);
        // 更新评论数量
        if (currentResource.value && currentResource.value.id === resourceId) {
          // 假设删除一条评论减少一个评论计数
          if (currentResource.value.commentCount !== undefined) {
            currentResource.value.commentCount = Math.max(0, currentResource.value.commentCount - 1)
          }
        }
        
        return Promise.resolve(true)
      }
      return Promise.reject(res.message || '删除评论失败')
    } catch (error: any) {
      console.error('删除评论失败详细错误:', error)
      
      // 检查错误响应中是否包含成功信息
      if (error.response && error.response.data) {
        const responseData = error.response.data;
        if (responseData.code === 20000) {
          console.log('从错误响应中提取成功数据', responseData);
          return Promise.resolve(true);
        }
      }
      
      // 检查错误消息是否实际上是成功响应
      if (error.message && typeof error.message === 'string') {
        if (error.message.includes('评论删除成功') || error.message.includes('"code":20000')) {
          return Promise.resolve(true);
        }
      }
      
      return Promise.reject(error)
    } finally {
      loading.value = false
    }
  }
  
  // 点赞评论
  const likeComment = async (commentId: number) => {
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录');
        return { liked: false, likeCount: 0 };
      }
      
      const userId = userStore.userInfo.id;
      const res = await request.post<{
        code: number
        data: {
          liked: boolean
          likeCount: number
        }
        message: string
      }>(`/resource/comment/like/${commentId}`, { userId })
      
      if (res.code === 20000) {
        return { liked: res.data?.liked || false, likeCount: res.data?.likeCount || 0 };
      }
      return { liked: false, likeCount: 0 };
    } catch (error) {
      console.error('点赞评论失败', error);
      return { liked: false, likeCount: 0 };
    }
  }
  
  // 获取我上传的资源列表
  const getMyUploads = async (params: {
    page?: number
    size?: number
    status?: number
  }) => {
    loading.value = true
    try {
      const res = await request.get<{
        code: number
        data: {
          list: Resource[]
          total: number
        }
        message: string
      }>('/resource/my-uploads', { params })
      
      if (res.code === 200) {
        return Promise.resolve(res.data)
      }
      return Promise.reject(res.message)
    } catch (error) {
      return Promise.reject(error)
    } finally {
      loading.value = false
    }
  }
  
  // 获取我收藏的资源列表
  const getMyCollections = async (params: {
    page?: number
    size?: number
    keyword?: string
  }) => {
    loading.value = true
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录');
        return { list: [], total: 0 };
      }
      
      // 构建请求参数
      const requestParams = {
        ...params,
        userId: userStore.userInfo.id
      };
      
      const res = await request.get('/resource/my-collections', { params: requestParams });
      
      if (res.code === 20000) {
        return {
          list: res.data.list,
          total: res.data.total
        };
      }
      return { list: [], total: 0 };
    } catch (error) {
      console.error('获取收藏记录失败', error);
      return { list: [], total: 0 };
    } finally {
      loading.value = false;
    }
  }
  
  // 获取我的下载历史
  const getMyDownloads = async (params: {
    page?: number
    size?: number
    keyword?: string
  }) => {
    loading.value = true
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录');
        return { list: [], total: 0 };
      }
      
      // 构建请求参数
      const requestParams = {
        ...params,
        userId: userStore.userInfo.id
      };
      
      const res = await request.get('/resource/my-downloads', { params: requestParams });
      
      if (res.code === 20000) {
        return {
          list: res.data.list,
          total: res.data.total
        };
      }
      return { list: [], total: 0 };
    } catch (error) {
      console.error('获取下载记录失败', error);
      return { list: [], total: 0 };
    } finally {
      loading.value = false;
    }
  }
  
  // 删除下载记录
  const deleteDownloadRecord = async (downloadId: number) => {
    loading.value = true
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录');
        return false;
      }
      
      const userId = userStore.userInfo.id;
      
      const res = await request.delete(`/resource/my-downloads/${downloadId}`, {
        params: { userId }
      });
      
      if (res.code === 20000) {
        return true;
      }
      return false;
    } catch (error) {
      console.error('删除下载记录失败', error);
      return false;
    } finally {
      loading.value = false;
    }
  }
  
  // 删除上传记录
  const deleteUploadRecord = async (resourceId: number) => {
    loading.value = true
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录');
        return false;
      }
      
      const userId = userStore.userInfo.id;
      
      const res = await request.delete(`/resource/my-uploads/${resourceId}`, {
        params: { userId }
      });
      
      if (res.code === 20000) {
        ElMessage.success('删除成功');
        return true;
      }
      ElMessage.error(res.message || '删除失败');
      return false;
    } catch (error) {
      console.error('删除上传记录失败', error);
      ElMessage.error('删除失败，请稍后重试');
      return false;
    } finally {
      loading.value = false;
    }
  }
  
  // 获取我的点赞记录
  const getMyLikes = async (params: {
    page?: number
    size?: number
    keyword?: string
  }) => {
    loading.value = true
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录');
        return { list: [], total: 0 };
      }
      
      // 构建请求参数
      const requestParams = {
        ...params,
        userId: userStore.userInfo.id
      };
      
      const res = await request.get('/resource/my-likes', { params: requestParams });
      
      if (res.code === 20000) {
        return {
          list: res.data.list,
          total: res.data.total
        };
      }
      return { list: [], total: 0 };
    } catch (error) {
      console.error('获取点赞记录失败', error);
      return { list: [], total: 0 };
    } finally {
      loading.value = false;
    }
  }
  
  // 批量收藏资源
  async function batchCollectResources(ids: number[]) {
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录');
        return null;
      }
      
      const userId = userStore.userInfo.id;
      const res = await request.post<{
        code: number
        data: { success: boolean, collectedIds: number[] }
        message: string
      }>('/resource/batch-collect', { userId, ids })
      
      if (res.code === 20000) {
        // 更新列表中已收藏资源的状态
        if (res.data?.collectedIds && res.data.collectedIds.length > 0) {
          resourceList.value.forEach(resource => {
            if (res.data.collectedIds.includes(resource.id)) {
              (resource as any).isCollected = true;
            }
          });
        }
        return res.data;
      }
      return null;
    } catch (error) {
      console.error('批量收藏资源失败', error);
      return null;
    }
  }
  
  // 获取用户上传记录列表
  const getUserResources = async (params: {
    page?: number
    size?: number
    keyword?: string
    status?: number
  }) => {
    loading.value = true
    try {
      // 获取当前用户ID
      const userStore = useUserStore();
      if (!userStore.token || !userStore.userInfo?.id) {
        ElMessage.warning('请先登录');
        return { list: [], total: 0 };
      }
      
      // 构建请求参数
      const requestParams = {
        ...params,
        userId: userStore.userInfo.id
      };
      
      console.log('发送请求参数:', JSON.stringify(requestParams));
      
      // 调用API获取用户上传记录
      const response = await request.get('/resource/my-uploads', { params: requestParams });
      
      console.log('接收到响应:', response);
      
      // 根据实际请求返回的数据格式进行处理
      let responseData;
      if (response.data && response.data.code === 20000) {
        responseData = response.data.data;
      } else if (response.code === 20000) {
        responseData = response.data;
      } else {
        console.error('响应数据格式不符合预期', response);
        return { list: [], total: 0 };
      }
      
      // 确保有数据
      if (!responseData || !responseData.list) {
        return { list: [], total: 0 };
      }
      
      // 处理返回的数据，将createTime映射为前端需要的createdAt
      const processedList = responseData.list.map((item: any) => ({
        ...item,
        createdAt: item.createTime,
        selected: false
      }));
      
      return {
        list: processedList,
        total: responseData.total || 0
      };
    } catch (error) {
      console.error('获取上传记录失败', error);
      return { list: [], total: 0 };
    } finally {
      loading.value = false;
    }
  }
  
  // 更新资源
  const updateResource = async (resourceData: any) => {
    try {
      const userStore = useUserStore();
      const { data } = await request.put(`/resource/update/${resourceData.id}?userId=${userStore.userInfo?.id}`, resourceData);
      return data.data;
    } catch (error) {
      console.error('更新资源失败', error);
      throw error;
    }
  }

  
  return {
    loading,
    resourceList,
    total,
    currentResource,
    getLatestResourceList,
    getPopularResourceList,
    likeResource,
    getResourceList,
    getResourceDetail,
    getResourceDetailWithComments,
    uploadResource,
    updateResource,
    deleteResource,
    downloadResource,
    batchDownloadResources,
    collectResource,
    checkCollectionStatus,
    getComments,
    addComment,
    deleteComment,
    likeComment,
    getMyUploads,
    getMyCollections,
    getMyDownloads,
    batchCollectResources,
    checkLikeStatus,
    getMyLikes,
    getUserResources,
    deleteDownloadRecord,
    deleteUploadRecord
  }
})
