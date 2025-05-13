<script lang="ts" setup>
import { ref, reactive } from "vue";
import { Page } from "@vben/common-ui";
import {
  ElCard,
  ElTabs,
  ElTabPane,
  ElTable,
  ElTableColumn,
  ElSelect,
  ElOption,
  ElButton,
  ElMessage,
  ElPopconfirm,
  ElTag,
  ElEmpty,
  ElDatePicker
} from "element-plus";
import type { FormInstance } from "element-plus";

import {
  MOCK_USERS,
  MOCK_RESOURCES,
  MOCK_USER_DOWNLOADS,
  MOCK_USER_LIKES,
  MOCK_USER_COLLECTIONS,
  getUserDownloads,
  getUserActiveLikes,
  getUserActiveCollections,
  getDownloadsWithResources,
  getLikesWithResources,
  getCollectionsWithResources
} from "./user-data";
import type { UserDownload, UserLike, UserCollection, User, ResourceBrief } from "./user-data";

// 当前活动的标签页
const activeTab = ref("downloads");

// 当前选中的用户
const currentUserId = ref<number>(0);

// 用户下载记录
const userDownloads = ref<UserDownload[]>([]);
// 用户点赞记录
const userLikes = ref<UserLike[]>([]);
// 用户收藏记录
const userCollections = ref<UserCollection[]>([]);

// 过滤条件
const filters = reactive({
  downloads: {
    startDate: "",
    endDate: "",
    resourceId: ""
  },
  likes: {
    startDate: "",
    endDate: "",
    resourceId: ""
  },
  collections: {
    startDate: "",
    endDate: "",
    resourceId: ""
  }
});

// 加载某个用户的活动记录
function loadUserRecords(userId: number) {
  if (userId === 0) {
    userDownloads.value = [];
    userLikes.value = [];
    userCollections.value = [];
    return;
  }
  
  // 加载下载记录
  userDownloads.value = getUserDownloads(userId);
  
  // 加载点赞记录
  userLikes.value = getUserActiveLikes(userId);
  
  // 加载收藏记录
  userCollections.value = getUserActiveCollections(userId);

  ElMessage.success(`已加载用户ID ${userId} 的活动记录`);
}

// 用户选择改变
function handleUserChange(userId: number) {
  currentUserId.value = userId;
  loadUserRecords(userId);
  
  // 清空过滤条件
  resetFilters();
}

// 删除下载记录
function handleDeleteDownload(record: UserDownload) {
  const index = userDownloads.value.findIndex((item) => item.id === record.id);
  if (index !== -1) {
    userDownloads.value.splice(index, 1);
    ElMessage.success("删除下载记录成功");
  }
}

// 删除点赞记录
function handleDeleteLike(record: UserLike) {
  const index = userLikes.value.findIndex((item) => item.id === record.id);
  if (index !== -1) {
    userLikes.value.splice(index, 1);
    ElMessage.success("删除点赞记录成功");
  }
}

// 删除收藏记录
function handleDeleteCollection(record: UserCollection) {
  const index = userCollections.value.findIndex((item) => item.id === record.id);
  if (index !== -1) {
    userCollections.value.splice(index, 1);
    ElMessage.success("删除收藏记录成功");
  }
}

// 过滤下载记录
function filterDownloads() {
  if (currentUserId.value === 0) return;

  // 获取原始数据
  const allDownloads = getUserDownloads(currentUserId.value);
  
  // 应用过滤条件
  userDownloads.value = allDownloads.filter(item => {
    let match = true;
    
    // 按资源ID过滤
    if (filters.downloads.resourceId && item.resource_id !== Number(filters.downloads.resourceId)) {
      match = false;
    }
    
    // 按日期范围过滤
    if (filters.downloads.startDate && new Date(item.create_time) < new Date(filters.downloads.startDate)) {
      match = false;
    }
    
    if (filters.downloads.endDate && new Date(item.create_time) > new Date(filters.downloads.endDate)) {
      match = false;
    }
    
    return match;
  });
}

// 过滤点赞记录
function filterLikes() {
  if (currentUserId.value === 0) return;
  
  // 获取原始数据
  const allLikes = getLikesWithResources(
    MOCK_USER_LIKES.filter(l => l.user_id === currentUserId.value)
  );
  
  // 应用过滤条件
  userLikes.value = allLikes.filter(item => {
    let match = true;
    
    // 按资源ID过滤
    if (filters.likes.resourceId && item.resource_id !== Number(filters.likes.resourceId)) {
      match = false;
    }
    
    // 按日期范围过滤
    if (filters.likes.startDate && new Date(item.create_time) < new Date(filters.likes.startDate)) {
      match = false;
    }
    
    if (filters.likes.endDate && new Date(item.create_time) > new Date(filters.likes.endDate)) {
      match = false;
    }
    
    // 只显示未删除的
    if (item.is_deleted !== 0) {
      match = false;
    }
    
    return match;
  });
}

// 过滤收藏记录
function filterCollections() {
  if (currentUserId.value === 0) return;
  
  // 获取原始数据
  const allCollections = getCollectionsWithResources(
    MOCK_USER_COLLECTIONS.filter(c => c.user_id === currentUserId.value)
  );
  
  // 应用过滤条件
  userCollections.value = allCollections.filter(item => {
    let match = true;
    
    // 按资源ID过滤
    if (filters.collections.resourceId && item.resource_id !== Number(filters.collections.resourceId)) {
      match = false;
    }
    
    // 按日期范围过滤
    if (filters.collections.startDate && new Date(item.create_time) < new Date(filters.collections.startDate)) {
      match = false;
    }
    
    if (filters.collections.endDate && new Date(item.create_time) > new Date(filters.collections.endDate)) {
      match = false;
    }
    
    // 只显示未删除的
    if (item.is_deleted !== 0) {
      match = false;
    }
    
    return match;
  });
}

// 重置过滤条件
function resetFilters() {
  // 重置下载记录过滤
  filters.downloads.startDate = "";
  filters.downloads.endDate = "";
  filters.downloads.resourceId = "";
  
  // 重置点赞记录过滤
  filters.likes.startDate = "";
  filters.likes.endDate = "";
  filters.likes.resourceId = "";
  
  // 重置收藏记录过滤
  filters.collections.startDate = "";
  filters.collections.endDate = "";
  filters.collections.resourceId = "";
  
  // 重新加载数据
  if (currentUserId.value > 0) {
    loadUserRecords(currentUserId.value);
  }
}

// 格式化文件大小
function formatFileSize(size?: number): string {
  if (!size) return "未知";
  
  if (size < 1024) {
    return size + " B";
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + " KB";
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + " MB";
  } else {
    return (size / (1024 * 1024 * 1024)).toFixed(2) + " GB";
  }
}
</script>

<template>
  <Page title="用户活动记录" description="管理用户的下载、点赞和收藏记录">
    <ElCard>
      <div class="user-selector">
        <span class="label">选择用户：</span>
        <ElSelect
          v-model="currentUserId"
          placeholder="请选择用户"
          style="width: 300px"
          @change="handleUserChange"
        >
          <ElOption
            v-for="user in MOCK_USERS"
            :key="user.id"
            :label="`${user.nickname}(${user.username})`"
            :value="user.id"
          />
        </ElSelect>
      </div>
      
      <div v-if="currentUserId === 0" class="empty-placeholder">
        <ElEmpty description="请先选择一个用户" />
      </div>
      
      <div v-else>
        <ElTabs v-model="activeTab">
          <!-- 下载记录标签页 -->
          <ElTabPane label="下载记录" name="downloads">
            <div class="filter-container">
              <ElDatePicker
                v-model="filters.downloads.startDate"
                type="date"
                placeholder="开始日期"
                value-format="YYYY-MM-DD"
                style="width: 180px"
              />
              <ElDatePicker
                v-model="filters.downloads.endDate"
                type="date"
                placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 180px"
              />
              <ElSelect
                v-model="filters.downloads.resourceId"
                placeholder="选择资源"
                style="width: 250px"
                clearable
              >
                <ElOption
                  v-for="resource in MOCK_RESOURCES"
                  :key="resource.id"
                  :label="resource.title"
                  :value="resource.id"
                />
              </ElSelect>
              <ElButton type="primary" @click="filterDownloads">筛选</ElButton>
              <ElButton @click="resetFilters">重置</ElButton>
            </div>
            
            <ElTable :data="userDownloads" stripe style="width: 100%">
              <ElTableColumn prop="id" label="ID" width="80" />
              <ElTableColumn label="资源" min-width="200">
                <template #default="{ row }">
                  {{ row.resource?.title || `资源${row.resource_id}` }}
                </template>
              </ElTableColumn>
              <ElTableColumn label="文件大小" width="120">
                <template #default="{ row }">
                  {{ formatFileSize(row.resource?.file_size) }}
                </template>
              </ElTableColumn>
              <ElTableColumn prop="create_time" label="下载时间" width="180" />
              <ElTableColumn label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <ElPopconfirm
                    title="确认删除该下载记录吗？"
                    @confirm="handleDeleteDownload(row)"
                  >
                    <template #reference>
                      <ElButton type="danger" size="small">删除</ElButton>
                    </template>
                  </ElPopconfirm>
                </template>
              </ElTableColumn>
            </ElTable>
            
            <div v-if="userDownloads.length === 0" class="empty-placeholder">
              <ElEmpty description="暂无下载记录" />
            </div>
          </ElTabPane>
          
          <!-- 点赞记录标签页 -->
          <ElTabPane label="点赞记录" name="likes">
            <div class="filter-container">
              <ElDatePicker
                v-model="filters.likes.startDate"
                type="date"
                placeholder="开始日期"
                value-format="YYYY-MM-DD"
                style="width: 180px"
              />
              <ElDatePicker
                v-model="filters.likes.endDate"
                type="date"
                placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 180px"
              />
              <ElSelect
                v-model="filters.likes.resourceId"
                placeholder="选择资源"
                style="width: 250px"
                clearable
              >
                <ElOption
                  v-for="resource in MOCK_RESOURCES"
                  :key="resource.id"
                  :label="resource.title"
                  :value="resource.id"
                />
              </ElSelect>
              <ElButton type="primary" @click="filterLikes">筛选</ElButton>
              <ElButton @click="resetFilters">重置</ElButton>
            </div>
            
            <ElTable :data="userLikes" stripe style="width: 100%">
              <ElTableColumn prop="id" label="ID" width="80" />
              <ElTableColumn label="资源" min-width="200">
                <template #default="{ row }">
                  {{ row.resource?.title || `资源${row.resource_id}` }}
                </template>
              </ElTableColumn>
              <ElTableColumn prop="create_time" label="点赞时间" width="180" />
              <ElTableColumn label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <ElPopconfirm
                    title="确认删除该点赞记录吗？"
                    @confirm="handleDeleteLike(row)"
                  >
                    <template #reference>
                      <ElButton type="danger" size="small">删除</ElButton>
                    </template>
                  </ElPopconfirm>
                </template>
              </ElTableColumn>
            </ElTable>
            
            <div v-if="userLikes.length === 0" class="empty-placeholder">
              <ElEmpty description="暂无点赞记录" />
            </div>
          </ElTabPane>
          
          <!-- 收藏记录标签页 -->
          <ElTabPane label="收藏记录" name="collections">
            <div class="filter-container">
              <ElDatePicker
                v-model="filters.collections.startDate"
                type="date"
                placeholder="开始日期"
                value-format="YYYY-MM-DD"
                style="width: 180px"
              />
              <ElDatePicker
                v-model="filters.collections.endDate"
                type="date"
                placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 180px"
              />
              <ElSelect
                v-model="filters.collections.resourceId"
                placeholder="选择资源"
                style="width: 250px"
                clearable
              >
                <ElOption
                  v-for="resource in MOCK_RESOURCES"
                  :key="resource.id"
                  :label="resource.title"
                  :value="resource.id"
                />
              </ElSelect>
              <ElButton type="primary" @click="filterCollections">筛选</ElButton>
              <ElButton @click="resetFilters">重置</ElButton>
            </div>
            
            <ElTable :data="userCollections" stripe style="width: 100%">
              <ElTableColumn prop="id" label="ID" width="80" />
              <ElTableColumn label="资源" min-width="200">
                <template #default="{ row }">
                  {{ row.resource?.title || `资源${row.resource_id}` }}
                </template>
              </ElTableColumn>
              <ElTableColumn prop="create_time" label="收藏时间" width="180" />
              <ElTableColumn label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <ElPopconfirm
                    title="确认删除该收藏记录吗？"
                    @confirm="handleDeleteCollection(row)"
                  >
                    <template #reference>
                      <ElButton type="danger" size="small">删除</ElButton>
                    </template>
                  </ElPopconfirm>
                </template>
              </ElTableColumn>
            </ElTable>
            
            <div v-if="userCollections.length === 0" class="empty-placeholder">
              <ElEmpty description="暂无收藏记录" />
            </div>
          </ElTabPane>
        </ElTabs>
      </div>
    </ElCard>
  </Page>
</template>

<style scoped>
.filter-container {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.user-selector {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.user-selector .label {
  margin-right: 10px;
  font-weight: 500;
}

.empty-placeholder {
  margin: 40px 0;
}
</style> 