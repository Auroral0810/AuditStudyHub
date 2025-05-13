<template>
  <div class="resource-container page-container">
    <!-- 顶部搜索区域 -->
    <div class="search-header">
      <div class="search-box">
        <el-input
          v-model="filterForm.keyword"
          placeholder="搜索资源名称、描述、上传者..."
          clearable
          prefix-icon="Search"
          @keyup.enter="handleSearch"
          class="search-input"
        >
          <template #suffix>
            <el-button type="primary" @click="handleSearch" :icon="Search" circle></el-button>
          </template>
        </el-input>
        
        <div class="filter-buttons">
          <el-button :icon="Filter" circle @click="filterVisible = true"></el-button>
          
          <el-dropdown>
            <el-button :icon="Sort" circle></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="changeSortOption('newest')">
                  <el-icon><Calendar /></el-icon> 最新上传
                </el-dropdown-item>
                <el-dropdown-item @click="changeSortOption('downloads')">
                  <el-icon><Download /></el-icon> 下载最多
                </el-dropdown-item>
                <el-dropdown-item @click="changeSortOption('views')">
                  <el-icon><View /></el-icon> 浏览最多
                </el-dropdown-item>
                <el-dropdown-item @click="changeSortOption('likes')">
                  <el-icon><Star /></el-icon> 点赞最多
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <el-button-group>
            <el-button :type="layoutType === 'waterfall' ? 'primary' : ''" :icon="Grid" @click="layoutType = 'waterfall'"></el-button>
            <el-button :type="layoutType === 'grid' ? 'primary' : ''" :icon="Menu" @click="layoutType = 'grid'"></el-button>
            <el-button :type="layoutType === 'list' ? 'primary' : ''" :icon="List" @click="layoutType = 'list'"></el-button>
          </el-button-group>
        </div>
      </div>
      
      <!-- 热门搜索标签 -->
      <div class="hot-search-tags" v-if="hotKeywords.length > 0">
        <span class="hot-tag-label">热门搜索:</span>
        <el-tag 
          v-for="keyword in hotKeywords" 
          :key="keyword" 
          class="tag-item"
          size="small"
          effect="light"
          @click="applyHotKeyword(keyword)"
        >
          {{ keyword }}
        </el-tag>
      </div>
    </div>
    
    <!-- 分类页签区域 -->
    <div class="category-tabs">
      <el-tabs v-model="activeCategory" @tab-click="handleCategoryChange" type="border-card">
        <el-tab-pane label="全部资源" name="all">
          <div class="tab-counter">{{ total }} 个资源</div>
        </el-tab-pane>
        <el-tab-pane 
          v-for="category in categoryOptions" 
          :key="category.value" 
          :label="category.label" 
          :name="category.value"
        >
          <div class="tab-counter">{{ getCategoryCount(category.value) }} 个资源</div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 批量操作栏 -->
    <div class="batch-operations" v-if="selectMode">
      <div class="batch-selection">
        <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
        <span class="selection-counter">已选择 {{ selectedResources.length }} 个资源</span>
      </div>
      
      <div class="batch-actions">
        <el-button type="primary" size="small" @click="batchDownload" :disabled="!selectedResources.length">
          <el-icon><Download /></el-icon> 批量下载
        </el-button>
        <el-button type="success" size="small" @click="batchCollect" :disabled="!selectedResources.length">
          <el-icon><Star /></el-icon> 批量收藏
        </el-button>
        <el-button type="danger" size="small" @click="selectMode = false">
          <el-icon><Close /></el-icon> 取消
        </el-button>
      </div>
    </div>
    
    <!-- 功能提示横幅 -->
    <div class="feature-banner" v-if="!selectMode">
      <div class="banner-content">
        <el-icon class="banner-icon"><InfoFilled /></el-icon>
        <span>想要一次下载多个资源？</span>
        <el-button type="primary" size="small" @click="selectMode = true">开启批量选择模式</el-button>
      </div>
    </div>
    
    <!-- 资源列表展示区域 -->
    <div class="resource-list-container">
      <!-- 瀑布流布局 -->
      <div v-if="layoutType === 'waterfall'" class="waterfall-layout">
        <div v-if="resourceList.length > 0" class="waterfall-container">
          <div 
            v-for="(resource, index) in resourceList" 
            :key="resource.id" 
            class="waterfall-item"
            :style="{ '--index': index % 4 }"
          >
            <div class="resource-card-wrapper">
              <el-checkbox 
                v-if="selectMode" 
                v-model="resource.selected" 
                @change="handleResourceSelect"
                class="resource-select-checkbox"
              ></el-checkbox>
              
              <div 
                class="resource-card" 
                :class="[`category-${resource.categoryId}`, { 'selected': resource.selected }]"
              >
                <!-- 顶部标签栏 -->
                <div class="waterfall-card-header">
                  <div class="resource-tags">
                    <div 
                      class="category-tag"
                      :style="{backgroundColor: getCategoryColor(resource.categoryId)}"
                    >
                      {{ getCategoryName(resource.categoryId) }}
                    </div>
                    <div class="file-type-tag">{{ resource.fileType.toUpperCase() }}</div>
                  </div>
                  
                  <!-- 右上角详情按钮 -->
                  <div class="detail-btn-corner" @click.stop="goToDetail(resource.id)">
                    <el-tooltip content="查看详情" placement="top">
                      <el-icon><ArrowRight /></el-icon>
                    </el-tooltip>
                  </div>
                </div>

                <!-- 资源预览区 -->
                <div class="resource-preview" @click="openPreviewDialog(resource)">
                  <div class="file-type-icon" :class="getFileTypeClass(resource.fileType)">
                    <el-icon :size="40"><component :is="getFileTypeIcon(resource.fileType)" /></el-icon>
                  </div>
                  <h3 class="resource-title" v-html="resource.title"></h3>
                  <p class="resource-desc" v-html="resource.description"></p>
                </div>
                
                <!-- 资源信息区 -->
                <div class="resource-info">
                  <div class="user-info">
                    <el-avatar :size="24" :src="getUserAvatar(resource)"></el-avatar>
                    <span v-html="resource.uploaderName || `用户${resource.uploaderId}`"></span>
                  </div>
                  <div class="date-info">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ formatDate(resource.createTime || resource.updateTime || resource.createdAt || resource.updatedAt) }}</span>
                  </div>
                </div>
                
                <!-- 资源统计区 -->
                <div class="resource-stats">
                  <div class="stat-item">
                    <el-icon><View /></el-icon>
                    <span>{{ formatNumber(resource.viewCount) }}</span>
                  </div>
                  <div class="stat-item">
                    <el-icon><Download /></el-icon>
                    <span>{{ formatNumber(resource.downloadCount) }}</span>
                  </div>
                  <div class="stat-item">
                    <el-icon><Pointer /></el-icon>
                    <span>{{ formatNumber(resource.likeCount) }}</span>
                  </div>
                </div>
                
                <!-- 资源操作区 -->
                <div class="resource-actions">
                  <el-tooltip content="下载" placement="top">
                    <el-button type="primary" circle size="small" @click.stop="downloadResource(resource.id)">
                      <el-icon><Download /></el-icon>
                    </el-button>
                  </el-tooltip>
                  
                  <el-tooltip content="收藏" placement="top">
                    <el-button 
                      :type="resource.isCollected ? 'success' : 'default'" 
                      circle 
                      size="small" 
                      @click.stop="toggleCollect(resource)"
                    >
                      <el-icon><Star /></el-icon>
                    </el-button>
                  </el-tooltip>
                  
                  <el-tooltip content="点赞" placement="top">
                    <el-button 
                      :type="resource.isLiked ? 'danger' : 'default'" 
                      circle 
                      size="small" 
                      @click.stop="toggleLike(resource)"
                    >
                      <el-icon><Pointer /></el-icon>
                    </el-button>
                  </el-tooltip>
                  
                  <el-tooltip content="预览" placement="top">
                    <el-button circle size="small" @click.stop="openPreviewDialog(resource)">
                      <el-icon><View /></el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
        
      <!-- 网格布局 -->
      <div v-else-if="layoutType === 'grid'" class="grid-layout">
        <el-row :gutter="20">
          <el-col 
            v-for="resource in resourceList" 
            :key="resource.id" 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6" 
            :xl="4"
            class="grid-column"
          >
            <div class="grid-card-wrapper">
              <el-checkbox 
                v-if="selectMode" 
                v-model="resource.selected" 
                @change="handleResourceSelect"
                class="resource-select-checkbox"
              ></el-checkbox>
              
              <el-card shadow="hover" class="resource-card" :class="{ 'selected': resource.selected }">
                <div class="grid-ribbon" :style="{backgroundColor: getCategoryColor(resource.categoryId)}"></div>
                
                <!-- 右上角详情按钮 -->
                <div class="detail-btn-corner" @click.stop="goToDetail(resource.id)">
                  <el-tooltip content="查看详情" placement="top">
                    <el-icon><ArrowRight /></el-icon>
                  </el-tooltip>
                </div>
                
                <div class="resource-icon" @click="openPreviewDialog(resource)">
                  <div class="icon-circle" :style="{backgroundColor: `${getCategoryColor(resource.categoryId)}15`}">
                    <el-icon :size="40" :style="{color: getCategoryColor(resource.categoryId)}">
                      <component :is="getFileTypeIcon(resource.fileType)" />
                    </el-icon>
                  </div>
                  <div class="file-type-tag grid-file-tag">{{ resource.fileType.toUpperCase() }}</div>
                </div>
                
                <h3 class="resource-title" @click="openPreviewDialog(resource)" v-html="resource.title"></h3>
                <p class="resource-desc" v-html="resource.description"></p>
                
                <div class="resource-meta">
                  <div class="meta-item user-info">
                    <el-avatar :size="24" :src="getUserAvatar(resource)"></el-avatar>
                    <span v-html="resource.uploaderName || `用户${resource.uploaderId}`"></span>
                  </div>
                  <div class="meta-item">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ formatDate(resource.createTime || resource.updateTime || resource.createdAt || resource.updatedAt) }}</span>
                  </div>
                </div>
                
                <div class="resource-stats">
                  <span><el-icon><View /></el-icon> {{ resource.viewCount || 0 }}</span>
                  <span><el-icon><Download /></el-icon> {{ resource.downloadCount || 0 }}</span>
                  <span><el-icon><Pointer /></el-icon> {{ resource.likeCount || 0 }}</span>
                </div>
                
                <div class="grid-actions">
                  <el-button type="primary" @click.stop="downloadResource(resource.id)">
                    <el-icon><Download /></el-icon> 下载
                  </el-button>
                  
                  <div class="button-group">
                    <el-button 
                      :type="resource.isCollected ? 'success' : ''"
                      @click.stop="toggleCollect(resource)"
                    >
                      <el-icon><Star /></el-icon>
                    </el-button>
                    
                    <el-button 
                      :type="resource.isLiked ? 'danger' : ''"
                      @click.stop="toggleLike(resource)"
                    >
                      <el-icon><Pointer /></el-icon>
                    </el-button>
                    
                    <el-button 
                      @click.stop="openPreviewDialog(resource)"
                    >
                      <el-icon><View /></el-icon>
                    </el-button>
                  </div>
                </div>
              </el-card>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <!-- 列表布局 -->
      <el-table 
        v-else-if="layoutType === 'list'" 
        :data="resourceList" 
        style="width: 100%" 
        v-loading="loading"
        class="resource-table"
      >
        <el-table-column v-if="selectMode" type="selection" width="55" align="center" />

        <el-table-column label="资源名称" min-width="240">
          <template #default="{ row }">
            <div class="list-title-container">
              <div class="list-icon" :class="getFileTypeClass(row.fileType)">
                <el-icon :size="24"><component :is="getFileTypeIcon(row.fileType)" /></el-icon>
              </div>
              <div class="list-info">
                <div class="list-title" @click="openPreviewDialog(row)" v-html="row.title"></div>
                <div class="list-desc" v-if="row.description" v-html="row.description.substring(0, 50) + (row.description.length > 50 ? '...' : '')"></div>
                <div class="category-tag-small" :style="{backgroundColor: getCategoryColor(row.categoryId)}">
                  {{ getCategoryName(row.categoryId) }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="上传者" width="120">
          <template #default="{ row }">
            <div class="list-user">
              <el-avatar :size="24" :src="getUserAvatar(row)"></el-avatar>
              <span v-html="row.uploaderName || `用户${row.uploaderId}`"></span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="上传时间" width="100">
          <template #default="{ row }">
            {{ formatDate(row.createTime || row.updateTime || row.createdAt || row.updatedAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="统计" width="160">
          <template #default="{ row }">
            <div class="list-stats">
              <span><el-icon><View /></el-icon> {{ row.viewCount || 0 }}</span>
              <span><el-icon><Download /></el-icon> {{ row.downloadCount || 0 }}</span>
              <span><el-icon><Pointer /></el-icon> {{ row.likeCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <div class="list-actions">
              <el-button-group class="list-action-group">
                <el-button type="primary" @click="downloadResource(row.id)">
                  <el-icon><Download /></el-icon> 下载
                </el-button>
                
                <el-button 
                  :type="row.isCollected ? 'success' : ''"
                  @click="toggleCollect(row)"
                >
                  <el-icon><Star /></el-icon>
                </el-button>
                
                <el-button 
                  :type="row.isLiked ? 'danger' : ''"
                  @click="toggleLike(row)"
                >
                  <el-icon><Pointer /></el-icon>
                </el-button>
                
                <el-button
                  @click="goToDetail(row.id)"
                >
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </el-button-group>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空状态展示 -->
      <div v-if="resourceList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无相关资源">
          <template #image>
            <img src="@/assets/empty-resources.svg" class="empty-image" alt="暂无资源" />
          </template>
          <template #description>
            <p>没有找到符合条件的资源</p>
            <p class="empty-suggestion">您可以尝试更改筛选条件或查看以下推荐</p>
          </template>
          <div class="empty-actions">
            <el-button type="primary" @click="resetFilter">重置筛选条件</el-button>
            <el-button @click="handleHotResourcesView">查看热门资源</el-button>
          </div>
        </el-empty>
      </div>
    </div>
    
    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <!-- 分页控件 -->
    <div class="pagination-container" v-if="resourceList.length > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 资源预览弹窗 -->
    <el-dialog
      v-model="previewDialogVisible"
      width="70%"
      class="resource-preview-dialog"
      :show-close="true"
      :destroy-on-close="true"
    >
      <template #header>
        <div class="preview-dialog-header">
          <h2 v-if="previewResource">{{ previewResource.title }}</h2>
        </div>
      </template>
      
      <div v-if="previewResource" class="preview-content">
        <div class="preview-main">
          <div class="preview-left">
            <div class="file-preview-container">
              <div class="file-icon-large" :class="getFileTypeClass(previewResource.fileType)">
                <el-icon :size="80"><component :is="getFileTypeIcon(previewResource.fileType)" /></el-icon>
              </div>
              <div class="file-type-label">{{ previewResource.fileType.toUpperCase() }}</div>
              <div class="file-size-label">{{ formatFileSize(previewResource.fileSize) }}</div>
            </div>
            
            <div class="preview-stats-card">
              <div class="stats-header">资源统计</div>
              <div class="stats-content">
                <div class="stat-row">
                  <div class="stat-item">
                    <el-icon><View /></el-icon>
                    <div class="stat-details">
                      <div class="stat-value">{{ formatNumber(previewResource.viewCount) }}</div>
                      <div class="stat-label">浏览</div>
                    </div>
                  </div>
                  <div class="stat-item">
                    <el-icon><Download /></el-icon>
                    <div class="stat-details">
                      <div class="stat-value">{{ formatNumber(previewResource.downloadCount) }}</div>
                      <div class="stat-label">下载</div>
                    </div>
                  </div>
                </div>
                <div class="stat-row">
                  <div class="stat-item">
                    <el-icon><Pointer /></el-icon>
                    <div class="stat-details">
                      <div class="stat-value">{{ formatNumber(previewResource.likeCount) }}</div>
                      <div class="stat-label">点赞</div>
                    </div>
                  </div>
                  <div class="stat-item">
                    <el-icon><Calendar /></el-icon>
                    <div class="stat-details">
                      <div class="stat-value">{{ formatDate(previewResource.createTime || previewResource.updateTime || previewResource.createdAt || previewResource.updatedAt) }}</div>
                      <div class="stat-label">上传日期</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="preview-right">
            <div class="resource-meta-card">
              <div class="meta-header">资源信息</div>
              <div class="meta-content">
                <div class="meta-row">
                  <div class="meta-label">资源分类</div>
                  <div class="meta-value">
                    <el-tag size="small" :style="{backgroundColor: getCategoryColor(previewResource.categoryId), color: '#fff'}">
                      {{ getCategoryName(previewResource.categoryId) }}
                    </el-tag>
                  </div>
                </div>
                <div class="meta-row">
                  <div class="meta-label">所属学院</div>
                  <div class="meta-value">{{ previewResource.collegeName || "未知学院" }}</div>
                </div>
                <div class="meta-row" v-if="previewResource.majorName">
                  <div class="meta-label">所属专业</div>
                  <div class="meta-value">{{ previewResource.majorName }}</div>
                </div>
                <div class="meta-row" v-if="previewResource.courseName">
                  <div class="meta-label">相关课程</div>
                  <div class="meta-value">{{ previewResource.courseName }}</div>
                </div>
                <div class="meta-row">
                  <div class="meta-label">上传者</div>
                  <div class="meta-value uploader-info">
                    <el-avatar :size="24" :src="getUserAvatar(previewResource)"></el-avatar>
                    <span v-html="previewResource.uploaderName || previewResource.username || `用户${previewResource.uploaderId}`"></span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="description-card">
              <div class="description-header">资源描述</div>
              <div class="description-content">
                <p v-if="previewResource.description" v-html="previewResource.description"></p>
                <p v-else class="no-description">暂无描述</p>
              </div>
            </div>
            
            <div class="tags-card" v-if="previewResource.tags && typeof previewResource.tags === 'string'">
              <div class="tags-header">资源标签</div>
              <div class="tags-content">
                <el-tag
                  v-for="tag in previewResource.tags.split(',')"
                  :key="tag"
                  class="resource-tag"
                  effect="light"
                  size="small"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
            <div class="tags-card" v-else-if="previewResource.tags && Array.isArray(previewResource.tags)">
              <div class="tags-header">资源标签</div>
              <div class="tags-content">
                <el-tag
                  v-for="tag in previewResource.tags"
                  :key="tag"
                  class="resource-tag"
                  effect="light"
                  size="small"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="preview-footer" v-if="previewResource">
          <el-button type="primary" @click="downloadResource(previewResource.id)" size="large">
            <el-icon><Download /></el-icon> 下载资源
          </el-button>
          <el-button 
            :type="previewResource.isCollected ? 'success' : ''" 
            @click="toggleCollect(previewResource)"
            size="large"
          >
            <el-icon><Star /></el-icon> {{ previewResource.isCollected ? '已收藏' : '收藏' }}
          </el-button>
          <el-button 
            :type="previewResource.isLiked ? 'danger' : ''" 
            @click="toggleLike(previewResource)"
            size="large"
          >
            <el-icon><Pointer /></el-icon> {{ previewResource.isLiked ? '已点赞' : '点赞' }}
          </el-button>
          <el-button @click="goToDetail(previewResource.id)" size="large" type="info">
            <el-icon><InfoFilled /></el-icon> 查看详情
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 筛选对话框 -->
    <el-dialog 
      v-model="filterVisible"
      title="高级筛选"
      width="400px"
      destroy-on-close
    >
      <div class="filter-panel">
        <el-form :model="filterForm" label-position="top" size="small">
          <el-form-item label="学院">
            <el-select
              v-model="filterForm.collegeId"
              placeholder="选择学院"
              clearable
              @change="handleCollegeChange"
              class="w-full"
            >
              <el-option
                v-for="item in collegeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="专业" v-if="filterForm.collegeId">
            <el-select
              v-model="filterForm.majorId"
              placeholder="选择专业"
              clearable
              class="w-full"
            >
              <el-option
                v-for="item in majorOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="上传时间">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              class="w-full"
            />
          </el-form-item>
          
          <el-form-item label="文件大小">
            <el-slider
              v-model="fileSizeRange"
              range
              :min="0"
              :max="100"
              :marks="{0:'0MB', 25:'25MB', 50:'50MB', 75:'75MB', 100:'100MB+'}"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetFilter">重置</el-button>
          <el-button type="primary" @click="handleFilterApply">应用筛选</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useResourceStore } from '../../stores/resource'
import { useSearchStore } from '../../stores/searchStore'
import { ElMessage } from 'element-plus'
import { 
  Search, Filter, Sort, Grid, Menu, List, View, Download, Star, Calendar,
  Document, Picture, Files, DataAnalysis, VideoPlay,
  Folder, InfoFilled, Close, PictureRounded, ArrowRight, Pointer
} from '@element-plus/icons-vue'
import type { Resource } from '../../stores/resource'

// 添加userAvatar可选属性到Resource类型
interface LocalResource extends Resource {
  userAvatar?: string;
  username?: string;
}

const route = useRoute()
const router = useRouter()
const resourceStore = useResourceStore()
const searchStore = useSearchStore()

// 布局类型
const layoutType = ref<'waterfall' | 'grid' | 'list'>(localStorage.getItem('layoutType') as any || 'waterfall')

// 资源列表数据
const resourceList = ref<(LocalResource & { selected?: boolean, isCollected?: boolean, isLiked?: boolean })[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(Number(localStorage.getItem('currentPage')) || 1)
const pageSize = ref(Number(localStorage.getItem('pageSize')) || 24)

// 分类选择
const activeCategory = ref(localStorage.getItem('activeCategory') || 'all')

// 排序选项
const sortOption = ref(localStorage.getItem('sortOption') || 'newest')

// 批量选择模式
const selectMode = ref(false)
const selectAll = ref(false)

// 过滤面板显示状态
const filterVisible = ref(false)

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 预览弹窗
const previewDialogVisible = ref(false)
const previewResource = ref<LocalResource | null>(null)

// 热门搜索关键词
const hotKeywords = ref<string[]>([])

// 高级筛选字段
const dateRange = ref<[Date, Date] | null>(null)
const fileSizeRange = ref([0, 100])

// 筛选表单
const filterForm = reactive({
  keyword: '',
  collegeId: '',
  majorId: '',
  categoryId: '',
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined,
  minSize: undefined as number | undefined,
  maxSize: undefined as number | undefined
})

// 获取被选中的资源
const selectedResources = computed(() => {
  return resourceList.value.filter(resource => resource.selected)
})

// 模拟学院数据
const collegeOptions = ref([
  { value: '1', label: '计算机学院' },
  { value: '2', label: '文学院' },
  { value: '3', label: '理学院' },
  { value: '4', label: '经济学院' },
  { value: '5', label: '法学院' }
])

// 模拟专业数据
const majorMap = {
  '1': [
    { value: '101', label: '计算机科学与技术' },
    { value: '102', label: '软件工程' },
    { value: '103', label: '网络工程' },
    { value: '104', label: '信息安全' }
  ],
  '2': [
    { value: '201', label: '汉语言文学' },
    { value: '202', label: '英语' },
    { value: '203', label: '日语' }
  ],
  '3': [
    { value: '301', label: '数学与应用数学' },
    { value: '302', label: '物理学' },
    { value: '303', label: '化学' }
  ],
  '4': [
    { value: '401', label: '经济学' },
    { value: '402', label: '金融学' },
    { value: '403', label: '国际经济与贸易' }
  ],
  '5': [
    { value: '501', label: '法学' },
    { value: '502', label: '知识产权' }
  ]
}

const majorOptions = ref<{ value: string; label: string }[]>([])

// 分类数据
const categoryOptions = ref([
  { value: '1', label: '学习笔记', color: '#67c23a' },
  { value: '2', label: '课件PPT', color: '#409eff' },
  { value: '3', label: '试卷资料', color: '#e6a23c' },
  { value: '4', label: '电子书籍', color: '#9254de' },
  { value: '5', label: '课程视频', color: '#f56c6c' },
  { value: '6', label: '实验报告', color: '#909399' }
])

// 定义默认头像函数
const getUserAvatar = (resource: Resource) => {
  return (resource as any).uploaderAvatar || (resource as any).userAvatar || defaultAvatar
}

// 处理学院变化
const handleCollegeChange = () => {
  filterForm.majorId = ''
  if (filterForm.collegeId) {
    majorOptions.value = majorMap[filterForm.collegeId as keyof typeof majorMap] || []
  } else {
    majorOptions.value = []
  }
}

// 日期格式化
const formatDate = (dateString?: string | number) => {
  if (!dateString) return '未知时间';
  
  // 处理时间戳
  const date = typeof dateString === 'number' ? 
    new Date(dateString) : 
    new Date(dateString);
    
  return date.toLocaleDateString();
}

// 获取资源列表
const getResourceList = async () => {
  loading.value = true
  try {
    const { list, total: totalCount } = await resourceStore.getResourceList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: filterForm.keyword,
      collegeId: filterForm.collegeId ? Number(filterForm.collegeId) : undefined,
      majorId: filterForm.majorId ? Number(filterForm.majorId) : undefined,
      categoryId: filterForm.categoryId ? Number(filterForm.categoryId) : undefined,
      sort: sortOption.value,
      status: 1, // 已审核通过的
      startDate: filterForm.startDate,
      endDate: filterForm.endDate,
      minSize: filterForm.minSize,
      maxSize: filterForm.maxSize
    })
    
    // 确保每次返回的列表顺序一致
    resourceList.value = list.sort((a: Resource, b: Resource) => {
      if (sortOption.value === 'newest') {
        return new Date(b.createTime || b.updatedAt || 0).getTime() - new Date(a.createTime || a.updatedAt || 0).getTime();
      } else if (sortOption.value === 'downloads') {
        return (b.downloadCount || 0) - (a.downloadCount || 0);
      } else if (sortOption.value === 'views') {
        return (b.viewCount || 0) - (a.viewCount || 0);
      } else if (sortOption.value === 'likes') {
        return (b.likeCount || 0) - (a.likeCount || 0);
      }
      return 0;
    });
    
    total.value = totalCount
  } catch (error) {
    console.error('获取资源列表失败', error)
    ElMessage.error('获取资源列表失败')
  } finally {
    loading.value = false
  }
}

// 应用热门搜索关键词
const applyHotKeyword = (keyword: string) => {
  filterForm.keyword = keyword
  handleSearch()
}

// 获取热门搜索词
const fetchHotKeywords = async (forceRefresh = false) => {
  try {
    const keywords = await searchStore.fetchHotKeywords(5, forceRefresh)
    if (keywords && keywords.length > 0) {
      hotKeywords.value = keywords
    }
  } catch (error) {
    console.error('获取热门搜索词失败', error)
  }
}

// 搜索资源
const handleSearch = () => {
  // 记录搜索关键词
  if (filterForm.keyword.trim()) {
    searchStore.recordSearch(filterForm.keyword.trim())
    // 搜索后强制刷新热门搜索列表，用户可以立即看到变化
    setTimeout(() => fetchHotKeywords(true), 1000)
  }
  
  currentPage.value = 1
  getResourceList()
}

// 重置筛选条件
const resetFilter = () => {
  Object.keys(filterForm).forEach(key => {
    (filterForm as any)[key] = ''
  })
  majorOptions.value = []
  sortOption.value = 'newest'
  currentPage.value = 1
  getResourceList()
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  localStorage.setItem('currentPage', page.toString())
  getResourceList()
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  localStorage.setItem('pageSize', size.toString())
  currentPage.value = 1
  getResourceList()
}

// 下载资源
const downloadResource = async (id: number) => {
  try {
    await resourceStore.downloadResource(id)
    // ElMessage.success('下载已开始')
  } catch (error) {
    console.error('下载资源失败', error)
    ElMessage.error('下载失败')
  }
}

// 收藏资源
const collectResource = async (id: number) => {
  try {
    const res = await resourceStore.collectResource(id)
    if (res && res.collected !== undefined) {
      ElMessage.success(res.collected ? '收藏成功' : '取消收藏成功')
    } else {
      ElMessage.success('操作成功')
    }
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error('操作失败')
  }
}

// 从URL参数中获取筛选条件
const getFilterFromQuery = () => {
  const query = route.query
  
  if (query.keyword) {
    filterForm.keyword = query.keyword as string
  }
  
  if (query.categoryId) {
    filterForm.categoryId = query.categoryId as string
  }
  
  if (query.collegeId) {
    filterForm.collegeId = query.collegeId as string
    handleCollegeChange()
    
    if (query.majorId) {
      filterForm.majorId = query.majorId as string
    }
  }
  
  if (query.sort) {
    sortOption.value = query.sort as string
  }
}

// 监听路由变化
watch(
  () => route.query,
  () => {
    getFilterFromQuery()
    getResourceList()
  },
  { immediate: true }
)

const refreshInterval = ref<number | null>(null)

onMounted(() => {
  getFilterFromQuery()
  getResourceList()
  
  // 获取热门搜索词
  fetchHotKeywords()
  
  // 每10分钟刷新一次热门搜索
  refreshInterval.value = window.setInterval(() => {
    fetchHotKeywords(true)
  }, 10 * 60 * 1000)
})

onUnmounted(() => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
    refreshInterval.value = null
  }
})

// 应用筛选条件
const handleFilterApply = () => {
  // 处理日期范围
  if (dateRange.value && dateRange.value.length === 2) {
    filterForm.startDate = dateRange.value[0]?.toISOString().split('T')[0]
    filterForm.endDate = dateRange.value[1]?.toISOString().split('T')[0]
  } else {
    filterForm.startDate = undefined
    filterForm.endDate = undefined
  }
  
  // 处理文件大小范围
  if (fileSizeRange.value && fileSizeRange.value.length === 2) {
    filterForm.minSize = fileSizeRange.value[0]
    filterForm.maxSize = fileSizeRange.value[1]
  } else {
    filterForm.minSize = undefined
    filterForm.maxSize = undefined
  }
  
  currentPage.value = 1
  filterVisible.value = false
  getResourceList()
}

// 更改排序选项
const changeSortOption = (option: string) => {
  sortOption.value = option
  localStorage.setItem('sortOption', option)
  getResourceList()
}

// 处理分类变化
const handleCategoryChange = (tab: any) => {
  if (tab.props.name === 'all') {
    filterForm.categoryId = ''
    activeCategory.value = 'all'
  } else {
    filterForm.categoryId = tab.props.name
    activeCategory.value = tab.props.name
  }
  localStorage.setItem('activeCategory', activeCategory.value)
  getResourceList()
}

// 获取分类数量
const getCategoryCount = (categoryId: string) => {
  return resourceList.value.filter(item => String(item.categoryId) === categoryId).length
}

// 获取分类颜色
const getCategoryColor = (categoryId: number | string) => {
  const category = categoryOptions.value.find(item => item.value === String(categoryId))
  return category ? category.color : '#909399'
}

// 获取分类名称
const getCategoryName = (categoryId: number | string) => {
  const category = categoryOptions.value.find(item => item.value === String(categoryId))
  return category ? category.label : '未知分类'
}

// 处理全选
const handleSelectAll = (val: boolean) => {
  resourceList.value.forEach(resource => {
    resource.selected = val
  })
}

// 处理资源选择
const handleResourceSelect = () => {
  const allSelected = resourceList.value.every(resource => resource.selected)
  if (allSelected !== selectAll.value) {
    selectAll.value = allSelected
  }
}

// 批量下载
const batchDownload = async () => {
  const selectedIds = selectedResources.value.map(resource => resource.id)
  if (selectedIds.length === 0) {
    ElMessage.warning('请选择要下载的资源')
    return
  }
  
  try {
    await resourceStore.batchDownloadResources(selectedIds)
    ElMessage.success('批量下载成功')
  } catch (error) {
    console.error('批量下载失败', error)
    ElMessage.error('批量下载失败')
  }
}

// 批量收藏
const batchCollect = async () => {
  const selectedIds = selectedResources.value.map(resource => resource.id)
  if (selectedIds.length === 0) {
    ElMessage.warning('请选择要收藏的资源')
    return
  }
  
  try {
    await resourceStore.batchCollectResources(selectedIds)
    ElMessage.success('批量收藏成功')
  } catch (error) {
    console.error('批量收藏失败', error)
    ElMessage.error('批量收藏失败')
  }
}

// 获取文件类型图标
const getFileTypeIcon = (fileType: string) => {
  if (!fileType) return Document;
  const type = fileType.toLowerCase();
  if (type.includes('doc')) return Document;
  if (type.includes('xls')) return DataAnalysis;
  if (type.includes('pdf')) return Files;
  if (type.includes('ppt')) return PictureRounded;
  if (type.includes('image') || type.includes('jpg') || type.includes('png')) return Picture;
  if (type.includes('video')) return VideoPlay;
  return Folder;
}

// 获取文件类型样式类
const getFileTypeClass = (fileType: string) => {
  if (!fileType) return 'file-type-default';
  return `file-type-${fileType.toLowerCase()}`;
}

// 格式化数字
const formatNumber = (num: number | undefined) => {
  if (num === undefined) return 0;
  return num > 999 ? (num / 1000).toFixed(1) + 'k' : num;
}

// 格式化文件大小
const formatFileSize = (size: number | undefined) => {
  if (size === undefined) return '0 B';
  if (size < 1024) return size + ' B';
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB';
  if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB';
  return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
}

// 切换收藏状态
const toggleCollect = async (resource: Resource & { isCollected?: boolean }) => {
  try {
    const res = await resourceStore.collectResource(resource.id)
    if (res && res.collected !== undefined) {
      resource.isCollected = res.collected
      ElMessage.success(res.collected ? '收藏成功' : '取消收藏成功')
    } else {
      ElMessage.success('操作成功')
    }
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error('操作失败')
  }
}

// 切换点赞状态
const toggleLike = async (resource: Resource & { isLiked?: boolean }) => {
  try {
    const res = await resourceStore.likeResource(resource.id)
    if (res && res.liked !== undefined) {
      resource.isLiked = res.liked
      // 更新点赞计数
      if (res.likeCount !== undefined) {
        resource.likeCount = res.likeCount
      }
      ElMessage.success(res.liked ? '点赞成功' : '取消点赞成功')
    } else {
      ElMessage.success('操作成功')
    }
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error('操作失败')
  }
}

// 预览资源
const openPreviewDialog = (resource: Resource) => {
  previewResource.value = resource
  previewDialogVisible.value = true
}

// 查看热门资源
const handleHotResourcesView = () => {
  sortOption.value = 'downloads'
  resetFilter()
}

// 跳转到详情页
const goToDetail = (id: number) => {
  router.push(`/resource/detail/${id}`)
}

// 监听布局类型变化并保存到本地存储
watch(layoutType, (newValue) => {
  localStorage.setItem('layoutType', newValue)
}, { immediate: true })
</script>

<style lang="scss" scoped>
.resource-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f9fafc;
}

/* Elasticsearch高亮样式 */
:deep(.highlight) {
  color: #409eff;
  font-weight: 600;
  background-color: rgba(64, 158, 255, 0.1);
  padding: 0 2px;
  border-radius: 2px;
}

/* 搜索区域样式 */
.search-header {
  margin-bottom: 20px;
  background-color: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  
  .search-box {
    display: flex;
    align-items: center;
    gap: 10px;
    
    .search-input {
      flex: 1;
    }
    
    .filter-buttons {
      display: flex;
      gap: 8px;
    }
  }
  
  .hot-search-tags {
    margin-top: 10px;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    
    .hot-tag-label {
      margin-right: 10px;
      color: #606266;
      font-size: 14px;
    }
    
    .tag-item {
      margin: 4px;
      cursor: pointer;
      transition: all 0.2s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
      }
    }
  }
}

/* 过滤面板样式 */
.filter-panel {
  .filter-title {
    margin-top: 0;
    margin-bottom: 15px;
    color: #303133;
    font-size: 16px;
    border-bottom: 1px solid #ebeef5;
    padding-bottom: 10px;
  }
  
  .filter-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
  
  .w-full {
    width: 100%;
  }
}

/* 对话框内部样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

:deep(.el-date-editor.el-input__wrapper) {
  width: 100%;
}

:deep(.el-range-editor.el-input__wrapper) {
  width: 100%;
}

/* 分类页签样式 */
.category-tabs {
  margin-bottom: 20px;
  
  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }
  
  :deep(.el-tabs__item) {
    height: 50px;
    line-height: 50px;
    font-size: 15px;
    transition: all 0.3s;
    
    &.is-active {
      font-weight: 600;
    }
    
    &:hover {
      color: var(--el-color-primary);
    }
  }
  
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }
  
  .tab-counter {
    color: #909399;
    font-size: 13px;
    padding: 10px 0;
    border-top: 1px solid #ebeef5;
  }
}

/* 批量操作区域 */
.batch-operations {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  padding: 10px 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  
  .batch-selection {
    display: flex;
    align-items: center;
    gap: 15px;
    
    .selection-counter {
      font-size: 14px;
      color: #606266;
    }
  }
  
  .batch-actions {
    display: flex;
    gap: 10px;
  }
}

/* 横幅提示 */
.feature-banner {
  background-color: #ecf5ff;
  border-radius: 8px;
  padding: 12px 20px;
  margin-bottom: 20px;
  
  .banner-content {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #409eff;
    
    .banner-icon {
      font-size: 18px;
    }
  }
}

/* 资源列表样式 */
.resource-list-container {
  margin-bottom: 20px;
}

.waterfall-layout {
  .waterfall-container {
    column-count: 4;
    column-gap: 20px;
    width: 100%;
  }
  
  .waterfall-item {
    break-inside: avoid;
    margin-bottom: 20px;
  }
  
  .resource-card-wrapper {
    position: relative;
    
    .resource-select-checkbox {
      position: absolute;
      top: 10px;
      left: 50%;
      transform: translateX(-50%);
      z-index: 11;
    }
  }
  
  .resource-card {
    display: flex;
    flex-direction: column;
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    padding: 15px;
    transition: all 0.3s;
    height: 100%;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
    }
    
    .waterfall-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      position: relative;
    }
    
    .resource-tags {
      display: flex;
      gap: 8px;
      z-index: 1;
      
      .category-tag {
        padding: 2px 8px;
        border-radius: 4px;
        color: white;
        font-size: 12px;
        font-weight: 500;
      }
      
      .file-type-tag {
        padding: 2px 8px;
        border-radius: 4px;
        background-color: #f2f6fc;
        color: #909399;
        font-size: 12px;
      }
    }
    
    .detail-btn-corner {
      position: absolute;
      top: 8px;
      right: 8px;
      width: 26px;
      height: 26px;
      border-radius: 50%;
      background-color: rgba(64, 158, 255, 0.1);
      color: #409EFF;
      display: flex;
      justify-content: center;
      align-items: center;
      cursor: pointer;
      transition: all 0.2s;
      z-index: 2;
      
      &:hover {
        background-color: #409EFF;
        color: white;
        transform: rotate(45deg);
      }
    }
    
    .resource-preview {
      cursor: pointer;
      margin-bottom: 15px;
      
      .file-type-icon {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 80px;
        height: 80px;
        border-radius: 8px;
        margin: 0 auto 15px;
        
        &.file-type-pdf {
          background-color: rgba(245, 108, 108, 0.1);
          color: #F56C6C;
        }
        
        &.file-type-doc, &.file-type-docx {
          background-color: rgba(64, 158, 255, 0.1);
          color: #409EFF;
        }
        
        &.file-type-ppt, &.file-type-pptx {
          background-color: rgba(230, 162, 60, 0.1);
          color: #E6A23C;
        }
        
        &.file-type-default {
          background-color: rgba(144, 147, 153, 0.1);
          color: #909399;
        }
      }
      
      .resource-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        text-align: center;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        max-height: 3em;
        line-height: 1.5;
      }
      
      .resource-desc {
        font-size: 14px;
        color: #606266;
        margin: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        max-height: 4.5em;
        line-height: 1.5;
        text-align: center;
      }
    }
    
    .resource-info {
      margin-bottom: 15px;
      display: flex;
      flex-direction: column;
      gap: 8px;
      
      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
        overflow: hidden;
        
        span {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
      
      .date-info {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #909399;
      }
    }
    
    .resource-stats {
      display: flex;
      justify-content: space-between;
      margin-bottom: 15px;
      font-size: 12px;
      color: #909399;
      
      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
    
    .resource-actions {
      padding-top: 10px;
      border-top: 1px solid #ebeef5;
      margin-top: auto;
      display: flex;
      justify-content: space-between;
    }
  }
}

.grid-layout {
  .grid-card-wrapper {
    position: relative;
    height: 100%;
    
    .resource-select-checkbox {
      position: absolute;
      top: 10px;
      left: 10px;
      z-index: 11;
    }
    
    .resource-card {
      height: 100%;
      display: flex;
      flex-direction: column;
      position: relative;
      overflow: hidden;
      transition: all 0.3s;
      padding-top: 20px;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      }
      
      &.selected {
        border: 2px solid var(--el-color-primary);
      }
      
      .grid-ribbon {
        height: 6px;
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        z-index: 1;
      }
      
      .detail-btn-corner {
        position: absolute;
        top: 8px;
        right: 8px;
        width: 26px;
        height: 26px;
        border-radius: 50%;
        background-color: rgba(64, 158, 255, 0.1);
        color: #409EFF;
        display: flex;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        transition: all 0.2s;
        z-index: 2;
        
        &:hover {
          background-color: #409EFF;
          color: white;
          transform: rotate(45deg);
        }
      }
      
      .resource-title {
        text-align: center;
        margin: 10px 0;
        font-size: 16px;
        font-weight: 600;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        max-height: 3em;
        line-height: 1.5;
        padding: 0 10px;
      }
      
      .resource-desc {
        text-align: center;
        font-size: 14px;
        color: #606266;
        margin: 0 0 15px 0;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        max-height: 4.5em;
        line-height: 1.5;
        padding: 0 10px;
      }
      
      .resource-meta {
        display: flex;
        flex-direction: column;
        gap: 8px;
        margin-bottom: 15px;
        padding: 0 15px;
        
        .meta-item {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 13px;
          overflow: hidden;
          
          &.user-info span {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }
      }
      
      .resource-stats {
        display: flex;
        justify-content: space-between;
        padding: 0 15px;
        margin-bottom: 15px;
        font-size: 12px;
        color: #909399;
      }
      
      .grid-actions {
        margin-top: auto;
        padding: 15px;
        display: flex;
        flex-direction: column;
        gap: 10px;
        
        .button-group {
          display: flex;
          justify-content: space-between;
          gap: 5px;
          
          .el-button {
            flex: 1;
          }
        }
      }
    }
    
    .resource-icon {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 15px;
      
      .icon-circle {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        display: flex;
        justify-content: center;
        align-items: center;
      }
      
      .grid-file-tag {
        margin-top: 5px;
      }
    }
  }
}

.list-title-container {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .list-icon {
    width: 40px;
    height: 40px;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 6px;
    flex-shrink: 0;
    
    &.file-type-pdf {
      background-color: rgba(245, 108, 108, 0.1);
      color: #F56C6C;
    }
    
    &.file-type-doc, &.file-type-docx {
      background-color: rgba(64, 158, 255, 0.1);
      color: #409EFF;
    }
    
    &.file-type-ppt, &.file-type-pptx {
      background-color: rgba(230, 162, 60, 0.1);
      color: #E6A23C;
    }
    
    &.file-type-default {
      background-color: rgba(144, 147, 153, 0.1);
      color: #909399;
    }
  }
  
  .list-info {
    flex: 1;
    min-width: 0;
    
    .list-title {
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin-bottom: 4px;
      
      &:hover {
        color: #409EFF;
      }
    }
    
    .list-desc {
      font-size: 12px;
      color: #909399;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin-bottom: 4px;
    }
    
    .category-tag-small {
      display: inline-block;
      padding: 1px 6px;
      border-radius: 3px;
      color: white;
      font-size: 10px;
    }
  }
}

.list-user {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  
  .el-avatar {
    flex-shrink: 0;
  }
  
  span {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.list-stats {
  display: flex;
  align-items: center;
  gap: 10px;
  
  span {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
  }
}

.list-actions {
  display: flex;
  gap: 5px;
  
  .list-action-group {
    width: 100%;
    display: flex;
    flex-wrap: nowrap;
    
    .el-button {
      &:first-child {
        flex: 1.5;
      }
      
      &:not(:first-child) {
        flex: 1;
        padding: 8px 0;
      }
    }
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  
  .empty-image {
    width: 200px;
    height: 200px;
    margin-bottom: 20px;
  }
  
  .empty-suggestion {
    color: #909399;
    margin-top: 10px;
  }
  
  .empty-actions {
    margin-top: 20px;
    display: flex;
    gap: 10px;
  }
}

.resource-preview-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 20px 0;
    margin-right: 0;
  }
  
  :deep(.el-dialog__headerbtn) {
    top: 20px;
  }
  
  :deep(.el-dialog__body) {
    padding: 20px;
  }
  
  :deep(.el-dialog__footer) {
    padding: 10px 20px 20px;
    border-top: 1px solid #f0f0f0;
  }
  
  .preview-dialog-header {
    h2 {
      margin: 0;
      font-size: 20px;
      color: #303133;
      font-weight: 600;
    }
  }
  
  .preview-content {
    padding: 0;
  }
  
  .preview-main {
    display: flex;
    gap: 24px;
  }
  
  .preview-left {
    width: 280px;
    flex-shrink: 0;
  }
  
  .preview-right {
    flex: 1;
    min-width: 0;
  }
  
  .file-preview-container {
    background-color: #f8f9fa;
    border-radius: 8px;
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
    
    .file-icon-large {
      width: 160px;
      height: 160px;
      display: flex;
      justify-content: center;
      align-items: center;
      border-radius: 8px;
      margin-bottom: 15px;
      
      &.file-type-pdf {
        background-color: rgba(245, 108, 108, 0.1);
        color: #F56C6C;
      }
      
      &.file-type-doc, &.file-type-docx {
        background-color: rgba(64, 158, 255, 0.1);
        color: #409EFF;
      }
      
      &.file-type-ppt, &.file-type-pptx {
        background-color: rgba(230, 162, 60, 0.1);
        color: #E6A23C;
      }
      
      &.file-type-default {
        background-color: rgba(144, 147, 153, 0.1);
        color: #909399;
      }
    }
    
    .file-type-label {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 8px;
    }
    
    .file-size-label {
      font-size: 14px;
      color: #909399;
    }
  }
  
  .preview-stats-card, .resource-meta-card, .description-card, .tags-card {
    background-color: #fff;
    border-radius: 8px;
    overflow: hidden;
    margin-bottom: 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  }
  
  .stats-header, .meta-header, .description-header, .tags-header {
    background-color: #f8f9fa;
    padding: 12px 16px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .stats-content, .meta-content, .description-content, .tags-content {
    padding: 16px;
  }
  
  .stat-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .stat-item {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
    
    .el-icon {
      font-size: 24px;
      color: #409EFF;
    }
    
    .stat-details {
      display: flex;
      flex-direction: column;
    }
    
    .stat-value {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      line-height: 1.2;
    }
    
    .stat-label {
      font-size: 12px;
      color: #909399;
    }
  }
  
  .meta-row {
    display: flex;
    margin-bottom: 12px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .meta-label {
      width: 80px;
      font-size: 14px;
      color: #606266;
      flex-shrink: 0;
    }
    
    .meta-value {
      flex: 1;
      font-size: 14px;
      color: #303133;
      
      &.uploader-info {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
  }
  
  .description-content {
    p {
      margin: 0;
      line-height: 1.6;
      font-size: 14px;
      
      &.no-description {
        color: #909399;
        font-style: italic;
      }
    }
  }
  
  .tags-content {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    
    .resource-tag {
      margin-right: 0;
    }
  }
  
  .preview-footer {
    display: flex;
    justify-content: center;
    gap: 12px;
  }
}

/* 网格布局优化样式 */
.grid-column {
  margin-bottom: 20px;
}

/* 响应式优化 */
@media screen and (max-width: 768px) {
  .waterfall-layout .waterfall-container {
    column-count: 1;
  }
  
  .grid-column {
    width: 100%;
  }
  
  .list-action-group {
    flex-wrap: wrap;
    
    .el-button {
      margin-right: 0 !important;
      margin-left: 0 !important;
      border-radius: 4px !important;
      width: 100%;
      margin-bottom: 4px;
    }
  }
}
</style> 