<template>
  <div class="likes-container">
    <h2 class="page-title">我的点赞</h2>
    
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索点赞的资源"
        @keyup.enter="handleSearch"
        clearable
        @clear="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>
    
    <!-- 数据加载中 -->
    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="5" animated />
    </div>
    
    <!-- 无数据 -->
    <el-empty v-else-if="likeList.length === 0" description="暂无点赞记录" />
    
    <!-- 点赞列表 -->
    <div v-else class="likes-list">
      <el-card v-for="item in likeList" :key="item.id" class="like-item" shadow="hover">
        <div class="like-content">
          <div class="like-info" @click="goToResourceDetail(item.resourceId)">
            <div class="like-title">{{ item.resourceTitle }}</div>
            <div class="like-meta">
              <el-tag size="small" class="tag">{{ item.categoryName }}</el-tag>
              <span class="uploader">上传者: <span v-html="item.uploaderName"></span></span>
              <span class="file-type">{{ item.fileType }}</span>
              <span class="like-time">
                <el-icon><Pointer /></el-icon>
                <span>{{ formatDate(item.likedAt) }}</span>
              </span>
            </div>
          </div>
          <div class="like-actions">
            <el-button size="small" type="primary" @click="goToResourceDetail(item.resourceId)">查看</el-button>
            <el-button size="small" type="danger" @click="unlikeResource(item.resourceId)">取消点赞</el-button>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
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

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Pointer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useResourceStore } from '@/stores/resource'

const router = useRouter()
const resourceStore = useResourceStore()

// 状态变量
const loading = ref(false)
const keyword = ref('')
const likeList = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 简单日期格式化函数
const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

// 加载点赞列表
const loadLikeList = async () => {
  loading.value = true
  try {
    const result = await resourceStore.getMyLikes({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    })
    
    likeList.value = result.list || []
    total.value = result.total || 0
  } catch (error) {
    console.error('加载点赞记录失败', error)
    ElMessage.error('加载点赞记录失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  loadLikeList()
}

// 处理页码变更
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadLikeList()
}

// 处理每页条数变更
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadLikeList()
}

// 跳转到资源详情
const goToResourceDetail = (resourceId: number) => {
  router.push(`/resource/detail/${resourceId}`)
}

// 取消点赞
const unlikeResource = async (resourceId: number) => {
  try {
    await resourceStore.likeResource(resourceId)
    ElMessage.success('取消点赞成功')
    loadLikeList()
  } catch (error) {
    console.error('取消点赞失败', error)
    ElMessage.error('取消点赞失败')
  }
}

// 初始加载
onMounted(() => {
  loadLikeList()
})
</script>

<style scoped lang="scss">
.likes-container {
  padding: 20px;
  
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: bold;
  }
  
  .search-bar {
    margin-bottom: 20px;
    max-width: 400px;
  }
  
  .loading-wrapper {
    padding: 20px 0;
  }
  
  .likes-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
    
    .like-item {
      cursor: pointer;
      transition: transform 0.2s;
      
      &:hover {
        transform: translateX(5px);
      }
      
      .like-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .like-info {
          flex: 1;
          
          .like-title {
            font-size: 16px;
            font-weight: bold;
            margin-bottom: 8px;
            color: #333;
          }
          
          .like-meta {
            display: flex;
            align-items: center;
            gap: 10px;
            color: #666;
            font-size: 14px;
            
            .tag {
              margin-right: 5px;
            }
          }
        }
        
        .like-actions {
          display: flex;
          gap: 10px;
        }
      }
    }
  }
  
  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style> 