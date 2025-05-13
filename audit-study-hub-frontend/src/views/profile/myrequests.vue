<template>
  <div class="my-requests-container">
    <h2 class="section-title">我的请求</h2>
    
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="filterForm.keyword"
            placeholder="搜索标题或描述"
            clearable
            @keyup.enter="handleSearch"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select
            v-model="filterForm.status"
            placeholder="请求状态"
            clearable
            style="width: 120px"
          >
            <el-option label="待解决" :value="0" />
            <el-option label="已解决" :value="1" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch" round>
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetFilter" round>
            <el-icon><RefreshRight /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 资源请求列表 -->
    <div class="request-list">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated :loading="loading" />
      </div>
      
      <template v-else>
        <el-empty
          v-if="requestList.length === 0"
          description="您还没有发布过资源请求"
          class="empty-state"
        >
          <template #image>
            <img src="@/assets/empty-resources.svg" class="empty-image" alt="暂无资源请求" />
          </template>
          <el-button type="primary" @click="goToResourceRequests" round>发布需求</el-button>
        </el-empty>
        
        <el-card 
          v-else
          v-for="item in requestList" 
          :key="item.id" 
          class="request-card" 
          shadow="hover"
          :body-style="{ padding: '0' }"
        >
          <div class="request-card-inner">
            <div class="status-tag" :class="item.status === 0 ? 'pending' : 'resolved'"></div>
            
            <div class="request-header">
              <div class="title-status">
                <h3 class="request-title" @click="viewRequest(item.id)">
                  {{ item.title }}
                </h3>
                <el-tag :type="item.status === 0 ? 'warning' : 'success'" size="small" effect="dark">
                  {{ item.status === 0 ? '待解决' : '已解决' }}
                </el-tag>
              </div>
              
              <div class="request-content">
                <p class="request-desc">{{ item.content }}</p>
              </div>
              
              <div class="request-meta">
                <div class="meta-item" v-if="item.collegeName">
                  <el-icon><School /></el-icon>
                  <span>{{ item.collegeName }}</span>
                </div>
                <div class="meta-item" v-if="item.majorName">
                  <el-icon><Reading /></el-icon>
                  <span>{{ item.majorName }}</span>
                </div>
                <div class="meta-item" v-if="item.courseName">
                  <el-icon><Collection /></el-icon>
                  <span>{{ item.courseName }}</span>
                </div>
                <div class="meta-item" v-if="item.viewCount !== undefined">
                  <el-icon><View /></el-icon>
                  <span>浏览 {{ item.viewCount }}</span>
                </div>
                <div class="meta-item" v-if="item.replyCount !== undefined">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>评论 {{ item.replyCount }}</span>
                </div>
              </div>
            </div>
            
            <div class="request-footer">
              <div class="request-time">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(item.createTime) }}</span>
              </div>
              
              <div class="request-actions">
                <!-- 已解决状态：查看资源、查看详情、删除请求 -->
                <template v-if="item.status === 1">
                  <el-button 
                    v-if="item.resourceId" 
                    type="success" 
                    size="small"
                    @click="goToResource(item.resourceId)"
                    round
                  >
                    <el-icon><Link /></el-icon> 查看资源
                  </el-button>
                  
                  <el-button size="small" type="primary" @click="viewRequest(item.id)" round>
                    <el-icon><View /></el-icon> 查看详情
                  </el-button>
                  
                  <el-button size="small" type="danger" @click="deleteRequest(item.id)" round>
                    <el-icon><Delete /></el-icon> 删除请求
                  </el-button>
                </template>
                
                <!-- 未解决状态：编辑请求、查看详情、删除请求 -->
                <template v-else>
                  <el-button size="small" type="warning" @click="editRequest(item.id)" round>
                    <el-icon><Edit /></el-icon> 编辑请求
                  </el-button>
                  
                  <el-button size="small" type="primary" @click="viewRequest(item.id)" round>
                    <el-icon><View /></el-icon> 查看详情
                  </el-button>
                  
                  <el-button size="small" type="danger" @click="deleteRequest(item.id)" round>
                    <el-icon><Delete /></el-icon> 删除请求
                  </el-button>
                </template>
              </div>
            </div>
          </div>
        </el-card>
      </template>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-container" v-if="requestList.length > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useResourceRequestStore } from '@/stores/resourceRequestStore'
import { 
  Search, 
  RefreshRight, 
  School, 
  Reading, 
  Collection, 
  View, 
  ChatDotRound, 
  Calendar, 
  Link, 
  Delete, 
  Edit 
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const resourceRequestStore = useResourceRequestStore()

// 确保用户已登录
if (!userStore.token) {
  router.push('/login')
}

// 获取用户ID
const userId = userStore.userInfo?.id

// 列表数据
const loading = ref(false)
const requestList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选表单
const filterForm = reactive({
  keyword: '',
  status: ''
})

// 获取我的资源请求列表
const fetchUserRequests = async () => {
  if (!userId) {
    ElMessage.error('用户未登录')
    return
  }
  
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      keyword: filterForm.keyword || undefined,
      status: filterForm.status !== '' ? filterForm.status : undefined
    }
    
    const res = await resourceRequestStore.getUserRequests(userId, params)
    requestList.value = res.records
    total.value = res.total
  } catch (error) {
    console.error('获取用户资源请求失败:', error)
    ElMessage.error('获取资源请求列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchUserRequests()
}

// 重置筛选条件
const resetFilter = () => {
  filterForm.keyword = ''
  filterForm.status = ''
  currentPage.value = 1
  fetchUserRequests()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchUserRequests()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchUserRequests()
}

// 查看资源请求详情
const viewRequest = (id) => {
  router.push(`/resource-request/${id}`)
}

// 查看资源
const goToResource = (resourceId) => {
  router.push(`/resources/${resourceId}`)
}

// 前往资源请求页面
const goToResourceRequests = () => {
  router.push('/resource-request')
}

// 编辑请求
const editRequest = (id) => {
  router.push(`/resource-request/edit/${id}`)
}

// 删除请求
const deleteRequest = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个请求吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await resourceRequestStore.deleteRequest(id)
    ElMessage.success('删除成功')
    fetchUserRequests()
  } catch (error) {
    if(error !== 'cancel') {
      console.error('删除请求失败', error)
      ElMessage.error('删除请求失败')
    }
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchUserRequests()
})
</script>

<style lang="scss" scoped>
.section-title {
  margin-bottom: 20px;
  font-size: 22px;
  color: #303133;
}

.filter-section {
  margin-bottom: 20px;
  padding: 15px 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.request-list {
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px;
}

.empty-state {
  padding: 40px 0;
  
  .empty-image {
    width: 200px;
    height: 200px;
  }
}

.request-card {
  margin-bottom: 15px;
  border-radius: 8px;
  overflow: hidden;
  
  .request-card-inner {
    position: relative;
    padding: 20px;
  }
  
  .status-tag {
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    
    &.pending {
      background-color: #e6a23c;
    }
    
    &.resolved {
      background-color: #67c23a;
    }
  }
  
  .request-header {
    padding-left: 15px;
  }
  
  .title-status {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
    
    .request-title {
      margin: 0 10px 0 0;
      font-size: 18px;
      color: #303133;
      cursor: pointer;
      
      &:hover {
        color: #409eff;
      }
    }
  }
  
  .request-content {
    margin-bottom: 10px;
    
    .request-desc {
      margin: 0;
      color: #606266;
      line-height: 1.5;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }
  
  .request-meta {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 10px;
    
    .meta-item {
      display: flex;
      align-items: center;
      margin-right: 15px;
      margin-bottom: 5px;
      font-size: 13px;
      color: #909399;
      
      .el-icon {
        margin-right: 4px;
      }
    }
  }
  
  .request-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 15px;
    border-top: 1px solid #ebeef5;
    
    .request-time {
      display: flex;
      align-items: center;
      font-size: 13px;
      color: #909399;
      
      .el-icon {
        margin-right: 4px;
      }
    }
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 