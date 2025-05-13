<template>
  <div class="downloads-container">
    <h2 class="page-title">下载历史</h2>
    
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索下载记录"
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
    <el-empty v-else-if="downloadList.length === 0" description="暂无下载记录" />
    
    <!-- 下载列表 -->
    <div v-else class="downloads-list">
      <el-card v-for="item in downloadList" :key="item.id" class="download-item" shadow="hover">
        <div class="download-content">
          <div class="download-info" @click="viewResource(item.resourceId)">
            <div class="download-title">{{ item.resourceTitle }}</div>
            <div class="download-meta">
              <el-tag size="small" class="tag">{{ item.categoryName }}</el-tag>
              <span class="uploader">上传者: <span v-html="item.uploaderName"></span></span>
              <span class="file-type">{{ item.fileType }}</span>
              <span class="download-time">
                <el-icon><Download /></el-icon>
                <span>{{ formatDate(item.downloadedAt) }}</span>
              </span>
            </div>
          </div>
          <div class="download-actions">
            <el-button size="small" type="primary" @click="viewResource(item.resourceId)">查看</el-button>
            <el-button size="small" type="danger" @click="deleteDownloadRecord(item.id)">删除记录</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useResourceStore } from '../../stores/resource'
import { Search, Download } from '@element-plus/icons-vue'

const router = useRouter()
const resourceStore = useResourceStore()

// 下载列表数据
const downloadList = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

// 获取下载历史
const getDownloadHistory = async () => {
  loading.value = true
  try {
    const { list, total: totalCount } = await resourceStore.getMyDownloads({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value
    })
    
    downloadList.value = list
    total.value = totalCount
  } catch (error) {
    console.error('获取下载历史失败', error)
    ElMessage.error('获取下载历史失败')
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
  getDownloadHistory()
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  getDownloadHistory()
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  getDownloadHistory()
}

// 查看资源详情
const viewResource = (id: number) => {
  router.push(`/resource/detail/${id}`)
}

// 删除下载记录
const deleteDownloadRecord = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除此下载记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用API删除下载记录
    const success = await resourceStore.deleteDownloadRecord(id);
    
    if (success) {
      ElMessage.success('删除下载记录成功');
      // 从前端列表中移除
      downloadList.value = downloadList.value.filter(item => item.id !== id);
    } else {
      ElMessage.error('删除下载记录失败');
    }
  } catch (error) {
    console.error('删除下载记录失败', error);
    if ((error as any) !== 'cancel') {
      ElMessage.error('删除下载记录失败');
    }
  }
}

onMounted(() => {
  getDownloadHistory()
})
</script>

<style lang="scss" scoped>
.downloads-container {
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
  
  .downloads-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
    
    .download-item {
      cursor: pointer;
      transition: transform 0.2s;
      
      &:hover {
        transform: translateX(5px);
      }
      
      .download-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .download-info {
          flex: 1;
          
          .download-title {
            font-size: 16px;
            font-weight: bold;
            margin-bottom: 8px;
            color: #333;
          }
          
          .download-meta {
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
        
        .download-time {
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