<template>
  <div class="uploads-container">
    <h2 class="page-title">我的上传</h2>
    
    <div class="action-bar">
      <el-button type="primary" @click="handleUpload">
        <el-icon><Plus /></el-icon> 上传资源
      </el-button>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索我的资源"
        clearable
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #suffix>
          <el-icon class="el-input__icon" @click="handleSearch">
            <Search />
          </el-icon>
        </template>
      </el-input>
    </div>
    
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="全部" name="all"></el-tab-pane>
      <el-tab-pane label="审核中" name="pending"></el-tab-pane>
      <el-tab-pane label="已通过" name="approved"></el-tab-pane>
      <el-tab-pane label="未通过" name="rejected"></el-tab-pane>
    </el-tabs>
    
    <div class="resource-list" v-loading="loading">
      <el-empty v-if="resourceList.length === 0" description="暂无上传资源" />
      
      <el-table v-else :data="resourceList" style="width: 100%">
        <el-table-column label="资源名称" min-width="180">
          <template #default="{ row }">
            <div class="resource-title">
              <el-icon :size="20" class="icon">
                <Document v-if="row.fileType.includes('doc')" />
                <DataAnalysis v-else-if="row.fileType.includes('xls')" />
                <Files v-else-if="row.fileType.includes('pdf')" />
                <Picture v-else-if="row.fileType.includes('image')" />
                <VideoPlay v-else-if="row.fileType.includes('video')" />
                <Folder v-else />
              </el-icon>
              <span class="title-text">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="分类" prop="categoryName" width="120" />
        <el-table-column label="上传时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="统计" width="200">
          <template #default="{ row }">
            <div class="resource-stats">
              <span><el-icon><View /></el-icon> {{ row.viewCount }}</span>
              <span><el-icon><Download /></el-icon> {{ row.downloadCount }}</span>
              <span><el-icon><Star /></el-icon> {{ row.likeCount }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <!-- 已通过的资源：查看、编辑、删除 -->
            <div v-if="row.status === 1" class="action-buttons">
              <el-button
                size="small"
                type="primary"
                @click="previewResource(row.id)"
              >
                查看
              </el-button>
              <el-button
                size="small"
                type="warning"
                @click="editResource(row.id)"
              >
                编辑
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="deleteResource(row.id)"
              >
                删除
              </el-button>
            </div>
            
            <!-- 已拒绝的资源：编辑重新上传、删除 -->
            <div v-else-if="row.status === 2" class="action-buttons">
              <el-button
                size="small"
                type="warning"
                @click="editResource(row.id)"
              >
                编辑重新上传
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="deleteResource(row.id)"
              >
                删除
              </el-button>
            </div>
            
            <!-- 待审核的资源：编辑、删除 -->
            <div v-else-if="row.status === 0" class="action-buttons">
              <el-button
                size="small"
                type="warning"
                @click="editResource(row.id)"
              >
                编辑
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="deleteResource(row.id)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
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
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import { useResourceStore } from '../../stores/resource'
import type { Resource } from '../../stores/resource'

const router = useRouter()
const resourceStore = useResourceStore()

// 资源列表数据
const resourceList = ref<Resource[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const activeTab = ref('all')

// 获取资源列表
const getResourceList = async () => {
  loading.value = true
  try {
    // 转换状态值
    let status: number | undefined
    if (activeTab.value === 'pending') status = 0
    else if (activeTab.value === 'approved') status = 1
    else if (activeTab.value === 'rejected') status = 2
    else status = undefined // 'all' 状态下status为undefined，显示所有资源
    
    console.log('====== 获取资源列表 ======');
    console.log('标签页:', activeTab.value);
    console.log('状态值:', status);
    
    const requestParams = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value,
      status
    };
    
    console.log('发送请求参数:', JSON.stringify(requestParams));
    
    const { list, total: totalCount } = await resourceStore.getUserResources(requestParams);
    
    console.log('后端返回资源列表:', list.length, '条记录');
    
    // 检查资源列表中的状态分布
    if (list && list.length > 0) {
      const statusCounts = {
        '0': 0, // 审核中
        '1': 0, // 已通过
        '2': 0, // 未通过
        'other': 0
      };
      
      (list as Resource[]).forEach(item => {
        if (item.status === 0) statusCounts['0']++;
        else if (item.status === 1) statusCounts['1']++;
        else if (item.status === 2) statusCounts['2']++;
        else statusCounts['other']++;
      });
      
      console.log('资源状态分布:', JSON.stringify(statusCounts));
    }
    
    // 检查是否需要在前端进一步过滤
    if (activeTab.value !== 'all' && status !== undefined) {
      const originalLength = list.length;
      resourceList.value = (list as Resource[]).filter(item => item.status === status);
      
      // 如果过滤前后数量不同，说明后端没有正确过滤
      if (originalLength !== resourceList.value.length) {
        console.log(`前端过滤: 从${originalLength}条记录过滤为${resourceList.value.length}条`);
        total.value = resourceList.value.length;
      } else {
        total.value = totalCount;
      }
    } else {
      resourceList.value = list;
      total.value = totalCount;
    }
    
    console.log('====== 获取完成 ======');
  } catch (error) {
    console.error('获取上传资源列表失败', error)
    ElMessage.error('获取资源列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态样式类型
const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'warning'  // 审核中
    case 1: return 'success'  // 已通过
    case 2: return 'danger'   // 未通过
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: number) => {
  switch (status) {
    case 0: return '审核中'
    case 1: return '已通过'
    case 2: return '未通过'
    default: return '未知'
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  getResourceList()
}

// 处理标签页切换
const handleTabClick = (tab: any) => {
  console.log('====== 标签页点击事件 ======');
  console.log('点击的标签:', tab);
  console.log('标签名称:', tab.props.name);
  console.log('标签标题:', tab.props.label);
  
  // 确保activeTab值正确设置
  activeTab.value = tab.props.name;
  console.log('设置activeTab为:', activeTab.value);
  
  // 根据标签页映射状态值
  let statusValue;
  if (activeTab.value === 'pending') statusValue = 0;        // 审核中 -> 0
  else if (activeTab.value === 'approved') statusValue = 1;  // 已通过 -> 1
  else if (activeTab.value === 'rejected') statusValue = 2;  // 未通过 -> 2
  else statusValue = undefined;                              // 全部 -> undefined
  
  console.log('当前标签页: ' + activeTab.value);
  console.log('对应状态值: ' + statusValue);
  console.log('=======================');
  
  // 重置页码并获取数据
  currentPage.value = 1;
  getResourceList();
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  getResourceList()
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  getResourceList()
}

// 跳转到资源上传页面
const handleUpload = () => {
  router.push('/resource/upload')
}

// 预览资源
const previewResource = (id: number) => {
  router.push(`/resource/detail/${id}`)
}

// 编辑资源
const editResource = (resourceId: number) => {
  router.push(`/resource/edit/${resourceId}`);
}

// 删除资源
const deleteResource = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个资源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await resourceStore.deleteResource(id)
    ElMessage.success('删除成功')
    
    // 刷新列表
    getResourceList()
  } catch (error) {
    console.error('删除资源失败', error)
  }
}

// 在现有代码后添加对activeTab的监听
watch(activeTab, (newVal, oldVal) => {
  console.log('====== activeTab变化 ======');
  console.log('旧值:', oldVal);
  console.log('新值:', newVal);
  console.log('tab元素DOM内容:', document.querySelector('.el-tabs__nav')?.textContent);
  console.log('=======================');
});

onMounted(() => {
  getResourceList()
})
</script>

<style lang="scss" scoped>
.uploads-container {
  padding: 0 10px;
  
  .page-title {
    font-size: 22px;
    margin-bottom: 20px;
    color: #303133;
  }
  
  .action-bar {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
    
    .search-input {
      width: 300px;
    }
  }
  
  .resource-title {
    display: flex;
    align-items: center;
    
    .icon {
      margin-right: 8px;
      color: #409eff;
    }
    
    .title-text {
      cursor: pointer;
      
      &:hover {
        color: #409eff;
      }
    }
  }
  
  .resource-stats {
    display: flex;
    
    span {
      display: flex;
      align-items: center;
      margin-right: 10px;
      font-size: 13px;
      
      .el-icon {
        margin-right: 3px;
      }
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
  
  .action-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
    
    .el-button {
      margin-left: 0;
    }
  }
}
</style> 