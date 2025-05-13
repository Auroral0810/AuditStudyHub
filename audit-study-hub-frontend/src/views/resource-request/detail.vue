<template>
  <div class="request-detail-container">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <template v-else-if="request">
      <div class="back-nav">
        <el-button type="default" @click="router.push('/resource-request')">
          <el-icon><ArrowLeft /></el-icon> 返回列表
        </el-button>
      </div>
      
      <el-card class="request-card" shadow="hover">
        <div class="request-header">
          <div class="request-title-status">
            <div class="status-indicator" :class="request.status === 0 ? 'pending' : 'resolved'"></div>
            <h1 class="request-title">{{ request.title }}</h1>
            <el-tag :type="request.status === 0 ? 'warning' : 'success'" size="large" effect="dark">
              {{ request.status === 0 ? '待解决' : '已解决' }}
            </el-tag>
          </div>
          
          <div class="request-meta">
            <div class="meta-item" v-if="request.createTime">
              <el-icon><Calendar /></el-icon>
              <span class="label">发布时间：</span>
              <span>{{ formatDate(request.createTime) }}</span>
            </div>
            <div class="meta-item" v-if="request.courseName">
              <el-icon><Reading /></el-icon>
              <span class="label">需求课程：</span>
              <span>{{ request.courseName }}</span>
            </div>
            <div class="meta-item" v-if="request.majorName">
              <el-icon><Collection /></el-icon>
              <span class="label">所属专业：</span>
              <span>{{ request.majorName }}</span>
            </div>
            <div class="meta-item" v-if="request.collegeName">
              <el-icon><School /></el-icon>
              <span class="label">所属学院：</span>
              <span>{{ request.collegeName }}</span>
            </div>
            <div class="meta-item" v-if="request.viewCount !== undefined && request.viewCount !== null">
              <el-icon><View /></el-icon>
              <span class="label">浏览数：</span>
              <span>{{ request.viewCount }}</span>
            </div>
            <div class="meta-item" v-if="request.replyCount !== undefined && request.replyCount !== null">
              <el-icon><ChatDotRound /></el-icon>
              <span class="label">回复数：</span>
              <span>{{ replyCount }}</span>
            </div>
            <div class="meta-item" v-if="request.categoryName">
              <el-icon><Files /></el-icon>
              <span class="label">分类：</span>
              <span>{{ request.categoryName }}</span>
            </div>
          </div>
        </div>
        
        <div class="section-title">
          <el-icon><Document /></el-icon>
          <span>需求详情</span>
        </div>
        
        <div class="request-content">
          <div class="content-text">{{ request.content }}</div>
        </div>
        
        <div class="section-title">
          <el-icon><User /></el-icon>
          <span>发布者信息</span>
        </div>
        
        <div class="requester-info">          
          <div class="user-card">
            <el-avatar :size="60" :src="request.userAvatar || defaultAvatar"></el-avatar>
            <div class="user-info">
              <div class="user-name">{{ request.username }}</div>
              <div class="user-college">{{ request.publisherCollegeName }}</div>
              <div class="user-major" v-if="request.publisherMajorName">{{ request.publisherMajorName }}</div>
            </div>
          </div>
        </div>
        
        <div class="request-actions">
          <el-button 
            v-if="request.status === 1 && request.resourceId" 
            type="success" 
            size="large"
            @click="goToResource(request.resourceId)"
            round
          >
            <el-icon><Link /></el-icon> 查看解决资源
          </el-button>
          
          <el-button 
            v-if="request.status === 0" 
            type="primary" 
            size="large"
            @click="openUploadDialog"
            round
          >
            <el-icon><Upload /></el-icon> 提供解决资源
          </el-button>
        </div>
      </el-card>
      
      <div class="comments-section" v-if="comments.length > 0 || userStore.token">
        <el-card shadow="hover">
          <template #header>
            <div class="comments-header">
              <div class="header-title">
                <el-icon><ChatLineRound /></el-icon>
                <h3>留言讨论</h3>
              </div>
              <span class="comment-count">共 {{ replyCount || 0 }} 条留言</span>
            </div>
          </template>
          
          <div class="comment-form" v-if="userStore.token">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="添加留言..."
              maxlength="500"
              show-word-limit
            />
            <div class="form-actions">
              <el-button 
                type="primary" 
                @click="submitComment" 
                :loading="commenting"
                :disabled="!commentContent.trim()"
                round
              >
                发布留言
              </el-button>
            </div>
          </div>
          
          <div class="comment-list">
            <div class="empty-comments" v-if="comments.length === 0">
              <el-empty description="暂无留言" />
            </div>
            
            <div v-else>
              <div class="comment-item" v-for="comment in comments" :key="comment.id">
                <div class="comment-avatar">
                  <el-avatar :size="40" :src="comment.userAvatar || defaultAvatar"></el-avatar>
                </div>
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-author">{{ comment.username }}</span>
                    <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                  </div>
                  <div class="comment-text" :class="{ 'deleted': comment.isDeleted === 1 }">
                    <template v-if="comment.isDeleted !== 1">
                      <p v-if="comment.replyToUsername" class="reply-info">
                        回复 <span class="reply-name">@{{ comment.replyToUsername }}</span>
                      </p>
                      <p>{{ comment.content }}</p>
                    </template>
                    <p v-else class="deleted-text">该回复已被删除</p>
                  </div>
                  
                  <div class="resource-link" v-if="comment.resourceId">
                    <el-button type="success" size="small" @click="goToResource(comment.resourceId)">
                      <el-icon><Link /></el-icon> 查看提供的资源: {{ comment.resourceTitle }}
                    </el-button>
                  </div>
                  
                  <div class="comment-actions" v-if="comment.isDeleted !== 1">
                    <span class="action-item" @click="showReplyInput(comment)">
                      <el-icon><ChatDotRound /></el-icon> 回复
                    </span>
                    <span 
                      v-if="isCommentOwner(comment)" 
                      class="action-item delete" 
                      @click="deleteComment(comment)"
                    >
                      <el-icon><Delete /></el-icon> 删除
                    </span>
                  </div>
                  
                  <div v-if="replyingTo === comment.id && !replyingToChild" class="reply-input">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      placeholder="回复评论..."
                      maxlength="500"
                      show-word-limit
                    ></el-input>
                    <div class="reply-submit">
                      <el-button size="small" @click="cancelReply">取消</el-button>
                      <el-button 
                        type="primary" 
                        size="small"
                        :disabled="!replyContent.trim()" 
                        :loading="commenting"
                        @click="submitReply(comment)"
                      >
                        回复
                      </el-button>
                    </div>
                  </div>
                  
                  <!-- 嵌套评论 -->
                  <CommentNested 
                    v-if="comment.children && comment.children.length > 0" 
                    :comments="adaptComments(comment.children)" 
                    :root-comment="adaptComment(comment)"
                    :replying-to="replyingTo"
                    :replying-to-child="replyingToChild"
                    :reply-content="replyContent"
                    @update:reply-content="val => replyContent = val"
                    :submitting-comment="commenting"
                    @show-reply="showReplyInput"
                    @cancel-reply="cancelReply"
                    @submit-reply="submitReply"
                    @delete-comment="deleteComment"
                    @like-comment="handleLikeComment"
                  />
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </template>
    
    <div v-else class="error-container">
      <el-empty description="需求不存在或已被删除" />
      <el-button type="primary" @click="router.push('/resource-request')" round>
        返回需求列表
      </el-button>
    </div>
    
    <!-- 提供资源对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="提供解决资源"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="uploadFormRef"
        :model="uploadForm"
        :rules="uploadRules"
        label-width="100px"
      >
        <el-form-item label="资源ID" prop="resourceId">
          <el-input v-model="uploadForm.resourceId" placeholder="请输入已上传资源的ID" />
          <div class="form-tip">请先上传资源，再填写对应的资源ID</div>
        </el-form-item>
        
        <el-form-item label="资源类型" prop="categoryId">
          <el-select v-model="uploadForm.categoryId" placeholder="请选择资源类型" class="w-full">
            <el-option
              v-for="category in categoryOptions"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="备注说明" prop="remark">
          <el-input
            v-model="uploadForm.remark"
            type="textarea"
            rows="3"
            placeholder="可选备注说明"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResourceLink" :loading="submitting" round>提交资源</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { useResourceRequestStore } from '../../stores/resourceRequestStore'
import { useCategoryStore } from '../../stores/categoryStore'
import { 
  Calendar, Reading, Collection, School, View, Document, User, 
  ChatDotRound, ChatLineRound, ArrowLeft, Upload, Link, Delete, Files
} from '@element-plus/icons-vue'
import CommentNested from '@/components/CommentNested.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const resourceRequestStore = useResourceRequestStore()
const categoryStore = useCategoryStore()

// 数据定义
interface ResourceRequest {
  id: number
  title: string
  content: string
  courseName: string | null
  collegeId: number
  collegeName: string
  majorId?: number
  majorName?: string
  publisherCollegeId?: number
  publisherCollegeName?: string
  publisherMajorId?: number
  publisherMajorName?: string
  status: number // 0: 待解决, 1: 已解决
  resourceId?: number // 解决该需求的资源ID
  userId: number
  username: string
  userAvatar?: string
  viewCount: number
  replyCount: number
  createTime: string
  updateTime: string
  categoryName?: string
}

interface Comment {
  id: number
  content: string
  userId: number
  username: string
  userAvatar?: string
  requestId: number
  resourceId?: number
  resourceTitle?: string
  resourceFileType?: string
  createTime: string
  updateTime: string
  replyToUsername?: string
  isDeleted?: number
  parentId?: number
  children?: Comment[]
  createdAt?: string
  updatedAt?: string
  liked?: boolean
  likes?: number
}

// 数据初始化
const loading = ref(true)
const request = ref<ResourceRequest | null>(null)
const comments = ref<Comment[]>([])
const replyCount = ref(0)
const commentContent = ref('')
const commenting = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 获取请求ID
const requestId = computed(() => {
  return Number(route.params.id)
})

// 提供资源表单
const uploadDialogVisible = ref(false)
const uploadFormRef = ref<FormInstance>()
const submitting = ref(false)
const uploadForm = reactive({
  requestId: '',
  resourceId: '',
  categoryId: '',
  remark: ''
})

// 表单校验规则
const uploadRules = reactive({
  resourceId: [
    { required: true, message: '请输入资源ID', trigger: 'blur' }
  ]
})

// 回复评论相关状态
const replyingTo = ref<number | undefined>(undefined)
const replyingToChild = ref<boolean>(false)
const replyContent = ref('')

// 获取需求详情
const getRequestDetail = async () => {
  loading.value = true
  try {
    const data = await resourceRequestStore.getDetail(requestId.value)
    if (data) {
      request.value = data.request
      comments.value = data.replies || []
      replyCount.value = data.replyCount || 0
    } else {
      request.value = null
    }
  } catch (error) {
    console.error('获取需求详情失败', error)
    ElMessage.error('获取需求详情失败')
  } finally {
    loading.value = false
  }
}

// 日期格式化
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  
  try {
    const date = new Date(dateString);
    const options: Intl.DateTimeFormatOptions = { 
      year: 'numeric', 
      month: '2-digit', 
      day: '2-digit',
      hour: '2-digit', 
      minute: '2-digit'
    };
    return new Intl.DateTimeFormat('zh-CN', options).format(date);
  } catch (error) {
    console.error('日期格式化失败', error);
    return dateString; // 返回原始字符串作为降级处理
  }
}

// 打开上传资源对话框
const openUploadDialog = () => {
  // 检查用户是否登录
  if (!userStore.token) {
    ElMessage.warning('请先登录再提供资源')
    router.push('/login')
    return
  }
  
  // 重置表单
  if (uploadFormRef.value) {
    uploadFormRef.value.resetFields()
  }
  
  uploadForm.requestId = requestId.value.toString()
  uploadDialogVisible.value = true
}

// 提交资源链接
const submitResourceLink = async () => {
  if (!uploadFormRef.value) return
  
  await uploadFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await resourceRequestStore.provideResourceSolution({
          requestId: Number(uploadForm.requestId),
          resourceId: Number(uploadForm.resourceId),
          categoryId: Number(uploadForm.categoryId),
          remark: uploadForm.remark
        })
        
        uploadDialogVisible.value = false
        getRequestDetail()
      } catch (error) {
        console.error('提交资源失败', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 提交评论 - 不刷新页面
const submitComment = async () => {
  if (!commentContent.value.trim()) return
  
  // 检查用户是否登录
  if (!userStore.token || !userStore.userInfo) {
    ElMessage.warning('请先登录再发表评论')
    router.push('/login')
    return
  }
  
  commenting.value = true
  try {
    const commentData = {
      requestId: requestId.value,
      content: commentContent.value.trim(),
      userId: userStore.userInfo!.id
    }
    
    const result = await resourceRequestStore.addCommentToRequest(commentData)
    
    if (result) {
      // 平滑更新UI，不重新加载页面
      const newComment: Comment = {
        id: result.id,
        requestId: requestId.value,
        userId: userStore.userInfo!.id,
        username: userStore.userInfo!.username,
        userAvatar: userStore.userInfo!.avatar || defaultAvatar,
        content: commentContent.value.trim(),
        parentId: undefined,
        replyToUsername: undefined,
        resourceId: undefined,
        resourceTitle: undefined,
        createTime: new Date().toISOString(),
        updateTime: new Date().toISOString(),
        isDeleted: 0,
        children: []
      }
      
      comments.value.unshift(newComment) // 添加到评论列表最前面
      replyCount.value++ // 更新评论计数
      
      commentContent.value = '' // 清空评论内容
      ElMessage.success('评论发布成功')
    }
  } catch (error) {
    console.error('发布评论失败', error)
    ElMessage.error('发布评论失败')
  } finally {
    commenting.value = false
  }
}

// 跳转到资源页面
const goToResource = (resourceId: number) => {
  router.push(`/resource/detail/${resourceId}`)
}

// 显示回复输入框 - 修复嵌套评论回复问题
const showReplyInput = (rootComment: any, childComment?: any) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录再回复评论')
    router.push('/login')
    return
  }
  
  replyContent.value = '' // 清空回复内容
  
  if (childComment) {
    // 如果是回复嵌套评论
    replyingTo.value = childComment.id
    replyingToChild.value = true
  } else {
    // 如果是回复一级评论
    replyingTo.value = rootComment.id
    replyingToChild.value = false
  }
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = undefined
  replyingToChild.value = false
  replyContent.value = ''
}

// 提交回复 - 不刷新页面
const submitReply = async (rootComment: any, childComment?: any) => {
  if (!requestId.value) return
  if (!userStore.token || !userStore.userInfo) {
    ElMessage.warning('请先登录再回复评论')
    router.push('/login')
    return
  }
  if (!replyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  
  commenting.value = true
  try {
    // 确定要回复的是哪条评论
    const replyToId = childComment ? childComment.id : rootComment.id
    const replyToName = childComment ? childComment.username : rootComment.username
    
    const replyData = {
      requestId: requestId.value,
      content: replyContent.value.trim(),
      parentId: replyToId,
      userId: userStore.userInfo!.id
    }
    
    const result = await resourceRequestStore.addCommentToRequest(replyData)
    
    if (result) {
      // 创建新回复对象
      const newReply: Comment = {
        id: result.id,
        requestId: requestId.value,
        userId: userStore.userInfo!.id,
        username: userStore.userInfo!.username,
        userAvatar: userStore.userInfo!.avatar || defaultAvatar,
        content: replyContent.value.trim(),
        parentId: replyToId,
        replyToUsername: replyToName,
        resourceId: undefined,
        resourceTitle: undefined,
        createTime: new Date().toISOString(),
        updateTime: new Date().toISOString(),
        isDeleted: 0,
        children: []
      }
      
      // 查找并更新评论树
      if (childComment) {
        // 如果是回复嵌套评论
        const findAndAddComment = (items: any[], parentId: number, newItem: any): boolean => {
          for (const item of items) {
            if (item.id === parentId) {
              if (!item.children) {
                item.children = []
              }
              item.children.push(newItem)
              return true
            }
            if (item.children && item.children.length > 0) {
              if (findAndAddComment(item.children, parentId, newItem)) {
                return true
              }
            }
          }
          return false
        }
        
        const found = findAndAddComment(comments.value, replyToId, newReply)
        // 如果在嵌套评论中找不到，尝试在一级评论中添加
        if (!found) {
          for (const comment of comments.value) {
            if (comment.children) {
              const foundInChildren = findAndAddComment(comment.children, replyToId, newReply)
              if (foundInChildren) break
            }
          }
        }
      } else {
        // 如果是回复一级评论
        const targetComment = comments.value.find(c => c.id === replyToId)
        if (targetComment) {
          if (!targetComment.children) {
            targetComment.children = []
          }
          targetComment.children.push(newReply)
        }
      }
      
      // 更新评论数量
      replyCount.value++
      
      // 清空回复内容并关闭回复框
      replyContent.value = ''
      replyingTo.value = undefined
      replyingToChild.value = false
      
    }
  } catch (error) {
    console.error('回复失败', error)
    ElMessage.error('回复失败，请稍后再试')
  } finally {
    commenting.value = false
  }
}

// 删除评论
const deleteComment = (comment: any) => {
  ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 使用当前登录用户ID作为删除权限验证
      const result = await resourceRequestStore.deleteComment(comment.id, userStore.userInfo!.id)
      
      if (result) {
        // 检查是否有子评论
        const hasChildren = comment.children && comment.children.length > 0;
        
        if (hasChildren) {
          // 如果有子评论，将评论标记为已删除，而不是移除它
          comment.isDeleted = 1;
          comment.content = "该回复已被删除";
          // 只减少一条评论数（当前评论）
          replyCount.value -= 1;
        } else {
          // 如果没有子评论，可以安全地移除评论
          const removeComment = (items: any[], commentId: number): boolean => {
            // 查找并删除评论
            const index = items.findIndex(item => item.id === commentId);
            if (index !== -1) {
              items.splice(index, 1);
              return true;
            }
            
            // 递归查找子评论
            for (const item of items) {
              if (item.children && item.children.length > 0) {
                if (removeComment(item.children, commentId)) {
                  return true;
                }
              }
            }
            return false;
          }
          
          removeComment(comments.value, comment.id);
          // 只减少一条评论数（当前评论）
          replyCount.value -= 1;
        }
        
      }
    } catch (error) {
      console.error('删除评论失败', error);
      ElMessage.error('删除失败，请稍后再试');
    }
  }).catch(() => {});
}

// 计算嵌套评论数
const countNestedComments = (comments: any[]): number => {
  let count = 0
  for (const comment of comments) {
    count += 1
    if (comment.children && comment.children.length > 0) {
      count += countNestedComments(comment.children)
    }
  }
  return count
}

// 判断当前用户是否是评论所有者
const isCommentOwner = (comment: any) => {
  return userStore.userInfo && userStore.userInfo.id === comment.userId
}

// 将评论数据适配成CommentNested组件所需的格式
const adaptComment = (comment: any): any => {
  return {
    ...comment,
    createdAt: comment.createTime,
    updatedAt: comment.updateTime,
    isDeleted: comment.isDeleted === 1,
    content: comment.isDeleted === 1 ? "该回复已被删除" : comment.content,
    liked: false, // 资源请求评论没有点赞功能
    likes: 0
  }
}

// 批量适配评论数组
const adaptComments = (comments: any[]): any[] => {
  return comments.map(comment => adaptComment(comment))
}

// 处理点赞评论的方法(兼容组件接口，实际不做操作)
const handleLikeComment = () => {
  ElMessage.info('资源请求评论暂不支持点赞功能')
}

// 在onMounted中初始化分类数据
onMounted(async () => {
  await Promise.all([
    getRequestDetail(),
    categoryStore.fetchCategories() // 获取所有分类
  ])
})

// 获取分类颜色的函数
const getCategoryColor = (categoryId) => {
  if (!categoryId) return '#909399'
  
  const category = categoryStore.categories.find(c => c.id === Number(categoryId))
  if (!category) return '#909399'
  
  // 根据分类的styleType或默认值来设置颜色
  const styleColors = {
    'success': '#67c23a',
    'primary': '#409eff',
    'warning': '#e6a23c',
    'danger': '#f56c6c',
    'info': '#909399',
    'purple': '#9254de'
  }
  
  return styleColors[category.styleType] || styleColors.info
}

// 分类选项 - 从store中动态获取
const categoryOptions = computed(() => {
  return categoryStore.categories.map(category => ({
    value: category.id.toString(),
    label: category.name,
    color: getCategoryColor(category.id)
  }))
})
</script>

<style lang="scss" scoped>
/* Elasticsearch高亮样式 */
:deep(.highlight) {
  color: #409eff;
  font-weight: 600;
  background-color: rgba(64, 158, 255, 0.1);
  padding: 0 2px;
  border-radius: 2px;
}

.request-detail-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
  
  .back-nav {
    margin-bottom: 20px;
  }
  
  .loading-container {
    background: white;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }
  
  .request-card {
    margin-bottom: 20px;
    border-radius: 12px;
    overflow: hidden;
    
    .request-header {
      margin-bottom: 25px;
      
      .request-title-status {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        margin-bottom: 20px;
        gap: 15px;
        
        .status-indicator {
          min-width: 10px;
          height: 10px;
          border-radius: 50%;
          flex-shrink: 0;
          
          &.pending {
            background-color: #e6a23c;
            box-shadow: 0 0 10px rgba(230, 162, 60, 0.5);
          }
          
          &.resolved {
            background-color: #67c23a;
            box-shadow: 0 0 10px rgba(103, 194, 58, 0.5);
          }
        }
        
        .request-title {
          font-size: 24px;
          font-weight: 600;
          margin: 0;
          flex: 1;
          word-break: break-word;
          min-width: 200px;
        }
      }
      
      .request-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        background-color: #f8f9fa;
        border-radius: 8px;
        padding: 15px;
        
        .meta-item {
          display: flex;
          align-items: center;
          font-size: 14px;
          padding: 5px 12px;
          background-color: white;
          border-radius: 20px;
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
          flex-shrink: 0;
          
          .el-icon {
            margin-right: 5px;
            color: #409EFF;
            flex-shrink: 0;
          }
          
          .label {
            color: #606266;
            margin-right: 5px;
            font-weight: 500;
            white-space: nowrap;
          }
        }
      }
    }
    
    .section-title {
      display: flex;
      align-items: center;
      margin: 30px 0 15px;
      position: relative;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      padding-bottom: 10px;
      border-bottom: 1px solid #EBEEF5;
      
      .el-icon {
        margin-right: 8px;
        color: #409EFF;
        font-size: 20px;
        flex-shrink: 0;
      }
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        bottom: -1px;
        width: 80px;
        height: 3px;
        background-color: #409EFF;
        border-radius: 3px;
      }
    }
    
    .request-content {
      background-color: #f8f9fa;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 20px;
      
      .content-text {
        font-size: 15px;
        line-height: 1.8;
        color: #303133;
        white-space: pre-line;
        word-break: break-word;
        overflow-wrap: break-word;
      }
    }
    
    .requester-info {
      margin-bottom: 30px;
      
      .user-card {
        display: flex;
        align-items: center;
        padding: 20px;
        background-color: #f8f9fa;
        border-radius: 12px;
        border-left: 4px solid #409EFF;
        
        .user-info {
          margin-left: 20px;
          overflow: hidden;
          
          .user-name {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 8px;
            color: #303133;
            word-break: break-word;
          }
          
          .user-college,
          .user-major {
            font-size: 14px;
            color: #606266;
            margin-bottom: 5px;
            word-break: break-word;
          }
        }
      }
    }
    
    .request-actions {
      display: flex;
      justify-content: center;
      flex-wrap: wrap;
      gap: 20px;
      margin-top: 30px;
      
      .el-button {
        padding: 12px 25px;
        font-size: 16px;
      }
    }
  }
  
  .comments-section {
    background: white;
    border-radius: 16px;
    box-shadow: 0 6px 16px rgba(0,0,0,0.07);
    overflow: hidden;
    margin-top: 30px;
    border: 1px solid rgba(230,235,245,0.8);
    
    .comments-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-title {
        display: flex;
        align-items: center;
        
        .el-icon {
          margin-right: 8px;
          color: #409EFF;
        }
        
        h3 {
          margin: 0;
          font-size: 18px;
        }
      }
      
      .comment-count {
        color: #909399;
        font-size: 14px;
        background-color: #f0f2f5;
        padding: 5px 10px;
        border-radius: 15px;
      }
    }
    
    .comment-form {
      margin: 20px 0;
      background-color: #f8f9fa;
      border-radius: 8px;
      padding: 15px;
      
      .form-actions {
        margin-top: 15px;
        display: flex;
        justify-content: flex-end;
      }
    }
    
    .comment-list {
      .comment-item {
        display: flex;
        padding: 20px;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child { 
          border-bottom: none; 
        }
        
        .comment-avatar {
          margin-right: 15px;
        }
        
        .comment-content {
          flex: 1;
          min-width: 0;
          
          .comment-header {
            display: flex;
            justify-content: space-between;
            margin-bottom: 8px;
            
            .comment-author {
              font-weight: 600;
              color: #303133;
            }
            
            .comment-time {
              font-size: 12px;
              color: #909399;
            }
          }
          
          .comment-text {
            background-color: #f8faff;
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 10px;
            font-size: 14px;
            line-height: 1.5;
            
            &.deleted {
              color: #999;
              font-style: italic;
              background-color: #f5f5f5;
            }
            
            .deleted-text {
              margin: 0;
              color: #999;
              font-style: italic;
            }
            
            .reply-info {
              color: #8e9aaf;
              margin-bottom: 4px;
              font-size: 12px;
              
              .reply-name {
                color: #4e9fff;
                font-weight: 500;
              }
            }
            
            p {
              margin: 0;
            }
          }
          
          .resource-link {
            margin-top: 8px;
          }
          
          .comment-actions {
            display: flex;
            gap: 15px;
            margin-top: 8px;
            
            .action-item {
              display: flex;
              align-items: center;
              gap: 4px;
              font-size: 13px;
              color: #8e9aaf;
              cursor: pointer;
              transition: all 0.2s;
              
              &:hover {
                color: #4e9fff;
              }
              
              &.delete:hover {
                color: #f56c6c;
              }
            }
          }
          
          .reply-input {
            margin-top: 12px;
            background-color: #f8faff;
            padding: 12px;
            border-radius: 6px;
            
            .reply-submit {
              display: flex;
              justify-content: flex-end;
              margin-top: 8px;
              gap: 8px;
            }
          }
        }
      }
    }
    
    .empty-comments {
      padding: 20px;
      text-align: center;
    }
  }
  
  .error-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 50px 0;
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    
    .el-button {
      margin-top: 20px;
    }
  }
  
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .comments-section {
    .comment-item {
      flex-direction: column;
      
      .comment-avatar {
        margin-right: 0;
        margin-bottom: 10px;
      }
      
      .comment-content {
        .comment-actions {
          flex-wrap: wrap;
        }
      }
    }
  }
}
</style> 