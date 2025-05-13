<template>
  <div class="collections-container">
    <h2 class="page-title">我的收藏</h2>
    
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索收藏的资源"
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
    <el-empty v-else-if="collectionList.length === 0" description="暂无收藏记录" />
    
    <!-- 收藏列表 -->
    <div v-else class="collections-list">
      <el-card v-for="item in collectionList" :key="item.id" class="collection-item" shadow="hover" @click="viewResource(item.resourceId)">
        <div class="collection-content">
          <div class="collection-info">
            <div class="collection-title">{{ item.resourceTitle }}</div>
            <div class="collection-meta">
              <el-tag size="small" class="tag">{{ item.categoryName }}</el-tag>
              <span class="uploader">上传者: <span v-html="item.uploaderName"></span></span>
              <span class="file-type">{{ item.fileType }}</span>
            </div>
          </div>
          <div class="collection-actions">
            <el-button 
              type="danger" 
              size="small" 
              @click.stop="cancelCollection(item.resourceId)"
            >
              取消收藏
            </el-button>
          </div>
          <div class="collection-time">
            <el-icon><Star /></el-icon>
            <span>{{ formatDate(item.collectedAt) }}</span>
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

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useResourceStore } from '../../stores/resource'
import { Search, Star } from '@element-plus/icons-vue'

const router = useRouter()
const resourceStore = useResourceStore()

// 收藏列表数据
const collectionList = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

// 获取收藏列表
const getCollectionList = async () => {
  loading.value = true
  try {
    const { list, total: totalCount } = await resourceStore.getMyCollections({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value
    })
    
    collectionList.value = list
    total.value = totalCount
  } catch (error) {
    console.error('获取收藏列表失败', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  getCollectionList()
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  getCollectionList()
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  getCollectionList()
}

// 查看资源详情
const viewResource = (id: number) => {
  router.push(`/resource/detail/${id}`)
}


// 取消收藏
const cancelCollection = async (id: number) => {
  try {
    await resourceStore.collectResource(id)
    ElMessage.success('已取消收藏')
    // 刷新列表
    getCollectionList()
  } catch (error) {
    console.error('取消收藏失败', error)
    ElMessage.error('取消收藏失败')
  }
}

onMounted(() => {
  getCollectionList()
})
</script>

<style lang="scss" scoped>
.collections-container {
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
  
  .collections-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
    
    .collection-item {
      cursor: pointer;
      transition: transform 0.2s;
      
      &:hover {
        transform: translateX(5px);
      }
      
      .collection-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        
        .collection-info {
          flex: 1;
          
          .collection-title {
            font-size: 16px;
            font-weight: bold;
            margin-bottom: 8px;
            color: #333;
          }
          
          .collection-meta {
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
        
        .collection-actions {
          display: flex;
          gap: 8px;
          margin: 0 15px;
        }
        
        .collection-time {
          display: flex;
          align-items: center;
          gap: 5px;
          color: #909399;
          font-size: 13px;
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