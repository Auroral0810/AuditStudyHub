<template>
  <div class="home-container">
  
    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-content">
        <h1 class="search-title">探索海量校园学习资源</h1>
        <p class="search-subtitle">快速查找课件、习题、笔记和考试资料</p>
        
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="输入关键词搜索资源..."
            class="search-input"
            @keyup.enter="handleSearch"
            @input="handleInputChange"
            @blur="handleBlur"
            @keydown.down.prevent="handleKeyDown"
            @keydown.up.prevent="handleKeyUp"
            ref="searchInputRef"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <div class="search-type-selector">
            <el-select v-model="searchType" placeholder="全部" class="search-select" @change="handleTypeChange">
              <el-option label="全部" value="all" />
              <el-option 
                v-for="category in categories" 
                :key="category.id" 
                :label="category.name" 
                :value="category.id.toString()" 
              />
            </el-select>
          </div>
          
          <el-button 
            type="primary" 
            class="search-button"
            @click="handleSearch"
          >搜索</el-button>
          
          <!-- 搜索建议下拉框 - 直接放在搜索框内部以确保正确定位 -->
          <div v-if="suggestions.length > 0" class="search-suggestions">
            <ul>
              <li 
                v-for="(suggestion, index) in suggestions" 
                :key="index"
                :class="{ active: activeIndex === index }"
                @mousedown.prevent="selectSuggestion(suggestion)"
                @mouseover="activeIndex = index"
              >
                <div class="suggestion-content" v-html="suggestion.title || suggestion.keyword"></div>
                <div class="suggestion-category" v-if="suggestion.categoryName">{{ suggestion.categoryName }}</div>
              </li>
            </ul>
          </div>
        </div>
        
        <div class="search-tags">
          <span>热门搜索:</span>
          <el-tag 
            v-for="tag in hotTags" 
            :key="tag" 
            class="tag-item"
            @click="searchKeyword = tag; handleSearch()"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </div>
    
    <!-- 资源分类 -->
    <div class="category-section card-container">
      <div class="section-header">
        <h2>资源分类</h2>
        <router-link to="/resource" class="view-more">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
      
      <div class="category-list">
        <el-row :gutter="20">
          <el-col :span="4" v-for="category in categories" :key="category.id">
            <div class="category-item" @click="navigateToCategory(category.id)">
              <el-icon :size="32"><component :is="category.icon" /></el-icon>
              <span>{{ category.name }}</span>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
    
    <!-- 最新资源 -->
    <div class="latest-resources card-container">
      <div class="section-header">
        <h2>最新资源</h2>
        <router-link to="/resource?sort=newest" class="view-more">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="6" v-for="resource in latestResources" :key="resource.id">
          <el-card shadow="hover" class="resource-card">
            <div class="resource-header">
              <div class="resource-icon" :class="getIconClass(resource.category, resource.categoryId)">
                <el-icon><component :is="getFileTypeIcon(resource.fileType)" /></el-icon>
              </div>
              <el-tag size="small" :type="getTagType(resource.type, resource.categoryId)" effect="light" class="category-tag">{{ resource.type }}</el-tag>
            </div>
            <h3 class="resource-title" @click="goToDetail(resource.id)">{{ resource.title }}</h3>
            <div class="resource-meta">
              <span>{{ resource.collegeName || '未知学院' }} - {{ resource.majorName || '未知专业' }}</span>
              <div class="resource-stats">
                <span><el-icon><View /></el-icon> {{ resource.viewCount }}</span>
                <span><el-icon><Download /></el-icon> {{ resource.downloadCount }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 热门资源 -->
    <div class="popular-resources card-container">
      <div class="section-header">
        <h2>热门资源</h2>
        <router-link to="/resource?sort=downloads" class="view-more">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="6" v-for="resource in popularResources" :key="resource.id">
          <el-card shadow="hover" class="resource-card">
            <div class="resource-header">
              <div class="resource-icon" :class="getIconClass(resource.category, resource.categoryId)">
                <el-icon><component :is="getFileTypeIcon(resource.fileType)" /></el-icon>
              </div>
              <el-tag size="small" :type="getTagType(resource.type, resource.categoryId)" effect="light" class="category-tag">{{ resource.type }}</el-tag>
            </div>
            <h3 class="resource-title" @click="goToDetail(resource.id)">{{ resource.title }}</h3>
            <div class="resource-meta">
              <span>{{ resource.collegeName || '未知学院' }} - {{ resource.majorName || '未知专业' }}</span>
              <div class="resource-stats">
                <span><el-icon><View /></el-icon> {{ resource.viewCount }}</span>
                <span><el-icon><Download /></el-icon> {{ resource.downloadCount }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useResourceStore } from '../../stores/resource'
import { useSearchStore } from '../../stores/searchStore'
import { useCategoryStore } from '../../stores/categoryStore'
import { useCourseStore } from '../../stores/courseStore'
import type { Resource } from '../../stores/resource'
import type { SearchSuggestion } from '../../stores/searchStore'
import type { CourseBaseDTO } from '../../types/course'
import { Document, Files, View, Download, Search, 
  ArrowRight, DataAnalysis, Picture, VideoPlay, Folder, PictureFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const resourceStore = useResourceStore()
const searchStore = useSearchStore()
const categoryStore = useCategoryStore()
const courseStore = useCourseStore()
// 搜索相关
const searchKeyword = ref('')
const searchType = ref<string>('all')
const hotTags = ref<string[]>([])
const searchInputRef = ref<HTMLElement | null>(null)

// 搜索建议相关
const suggestions = ref<SearchSuggestion[]>([])
const activeIndex = ref(-1)
const debounceTimer = ref<number | null>(null)

// 资源分类,调用api接口实现
const categories = ref<CategoryBaseDTO[]>([])

// 添加课程相关
const courseList = ref<CourseBaseDTO[]>([])
const selectedCourse = ref<number | null>(null)
const courseResources = ref<ResourceWithType[]>([])

// 监听搜索框输入
const handleInputChange = () => {
  
  
  // 清除之前的定时器
  if (debounceTimer.value !== null) {
    clearTimeout(debounceTimer.value);
    debounceTimer.value = null;
  }
  
  // 如果输入为空，立即清空建议
  if (!searchKeyword.value.trim()) {
    
    suggestions.value = [];
    return;
  }
  
  // 设置新的定时器，100ms防抖(减少延迟)
  debounceTimer.value = window.setTimeout(async () => {
    try {
      const keyword = searchKeyword.value.trim();
      if (!keyword) return;
      
      
      const categoryId = searchType.value !== 'all' ? parseInt(searchType.value) : undefined;
      
      // 使用store获取搜索建议
      const result = await searchStore.fetchSuggestions(keyword, categoryId, 5);
      
      
      // 检查结果
      if (Array.isArray(result) && result.length > 0) {
        suggestions.value = [...result];
        
        
      } else {
        
        suggestions.value = [];
      }
      
      // 重置选中索引
      activeIndex.value = -1;
    } catch (error) {
      console.error('获取搜索建议失败:', error);
      suggestions.value = [];
    }
  }, 100); // 减少延迟时间为100ms，提高响应速度
}

// 处理搜索框失焦
const handleBlur = () => {
  // 延迟关闭建议框，允许点击建议项
  setTimeout(() => {
    suggestions.value = [];
    
  }, 300); // 增加延迟时间，确保能够点击建议
}

// 处理键盘上下键
const handleKeyDown = () => {
  if (suggestions.value.length === 0) return
  
  activeIndex.value = 
    activeIndex.value < suggestions.value.length - 1 ? 
    activeIndex.value + 1 : 0
}

const handleKeyUp = () => {
  if (suggestions.value.length === 0) return
  
  activeIndex.value = 
    activeIndex.value > 0 ? 
    activeIndex.value - 1 : suggestions.value.length - 1
}

// 选择搜索建议
const selectSuggestion = (suggestion: SearchSuggestion) => {
  searchKeyword.value = suggestion.keyword
  suggestions.value = []
  
  // 如果有分类信息，设置搜索类型
  if (suggestion.categoryId) {
    searchType.value = suggestion.categoryId.toString()
  }
  
  // 立即执行搜索
  handleSearch()
}

// 分类变更时清空建议并重新获取
const handleTypeChange = () => {
  if (searchKeyword.value.trim()) {
    handleInputChange()
  }
}

// 获取资源分类
const getCategories = async () => {
  const res = await categoryStore.fetchCategories()
  categories.value = res
}

// 更新资源类型接口定义
interface ResourceWithType extends Resource {
  type: string;
  collegeName?: string;
  majorName?: string;
  categoryName?: string;
}

// 最新资源
const latestResources = ref<ResourceWithType[]>([])

// 热门资源
const popularResources = ref<ResourceWithType[]>([])

// 获取课程数据
const getCourses = async () => {
  try {
    const res = await courseStore.fetchCourses()
    courseList.value = res || []
  } catch (error) {
    console.error('获取课程数据失败', error)
    ElMessage.error('获取课程数据失败')
  }
}

// 处理课程选择变化
const handleCourseChange = async (courseId: number | null) => {
  if (!courseId) {
    courseResources.value = []
    return
  }
  
  try {
    // 获取选定课程的资源
    const response = await resourceStore.getResourceList({
      page: 1,
      size: 4,
      courseId,
      status: 1
    })
    
    courseResources.value = (response.list as Resource[]).map(item => ({
      ...item,
      type: item.category || ''
    })) as ResourceWithType[]
  } catch (error) {
    console.error('获取课程资源失败', error)
    ElMessage.error('获取课程资源失败')
    courseResources.value = []
  }
}

// 查看课程资源按钮点击事件
const viewCourseResources = () => {
  if (!selectedCourse.value) {
    ElMessage.warning('请先选择一门课程')
    return
  }
  
  router.push({
    path: '/resource',
    query: { courseId: selectedCourse.value.toString() }
  })
}

// 处理搜索
const handleSearch = () => {
  const trimmedKeyword = searchKeyword.value.trim()
  if (trimmedKeyword) {
    // 记录搜索关键词
    searchStore.recordSearch(trimmedKeyword)
    
    router.push({
      path: '/resource',
      query: {
        keyword: trimmedKeyword,
        categoryId: searchType.value !== 'all' ? searchType.value : undefined
      }
    })
  } else {
    // 如果关键词为空，提示用户
    ElMessage.warning('请输入搜索关键词')
  }
}

// 导航到分类
const navigateToCategory = (categoryId: number) => {
  router.push({
    path: '/resource',
    query: { categoryId }
  })
}

// 跳转到详情页
const goToDetail = (id: number) => {
  router.push(`/resource/detail/${id}`)
}

// 获取最新资源
const getLatestResources = async () => {
  try {
    const response = await resourceStore.getLatestResourceList(4)
    // 使用API返回的category作为类型标识
    latestResources.value = (response as Resource[]).map(item => ({
      ...item,
      type: item.category || ''
    })) as ResourceWithType[]
  } catch (error) {
    console.error('获取最新资源失败', error)
  }
}

// 获取热门资源
const getPopularResources = async () => {
  try {
    const response = await resourceStore.getPopularResourceList(4)
    // 使用API返回的category作为类型标识
    popularResources.value = (response as Resource[]).map(item => ({
      ...item,
      type: item.category || ''
    })) as ResourceWithType[]
  } catch (error) {
    console.error('获取热门资源失败', error)
  }
}

// 获取图标样式类
const getIconClass = (category: string | undefined, categoryId?: number) => {
  // 优先从分类集合中查找
  if (categoryId) {
    const categoryObj = categories.value.find(cat => cat.id === categoryId);
    if (categoryObj && categoryObj.iconClass) {
      return categoryObj.iconClass;
    }
    
    // 如果没有自定义样式类，则使用算法生成
    const styleIndex = categoryId % 6;
    const styleClasses = [
      'icon-note',   // 蓝色背景
      'icon-ppt',    // 绿色背景
      'icon-doc',    // 橙色背景
      'icon-book',   // 紫色背景
      'icon-lab',    // 红色背景
      'icon-exam'    // 灰色背景
    ];
    return styleClasses[styleIndex];
  }
  
  // 如果没有分类ID但有分类名，则尝试通过名称查找分类ID
  if (category) {
    const categoryObj = categories.value.find(cat => cat.name === category);
    if (categoryObj) {
      return getIconClass(undefined, categoryObj.id);
    }
  }
  
  return 'icon-default';
}

// 获取标签类型
const getTagType = (type: string, categoryId?: number) => {
  // 优先从分类集合中查找
  if (categoryId) {
    const categoryObj = categories.value.find(cat => cat.id === categoryId);
    if (categoryObj && categoryObj.styleType) {
      return categoryObj.styleType;
    }
    
    // 如果没有自定义样式类型，则使用算法生成
    const tagIndex = categoryId % 5;
    const tagTypes = [
      'success',  // 绿色
      'warning',  // 橙色
      'info',     // 蓝色
      'danger',   // 红色
      'primary'   // 紫蓝色
    ];
    return tagTypes[tagIndex];
  }
  
  // 如果只有分类名，则尝试从分类对象中查找对应的ID
  if (type) {
    const categoryObj = categories.value.find(cat => cat.name === type);
    if (categoryObj) {
      return getTagType('', categoryObj.id);
    }
  }
  
  return '';
}

// 获取热门搜索词与默认值
const loadHotTags = async () => {
  // 默认热搜词，当API失败时作为备用
  const defaultTags = ['高等数学', '大学英语', '数据结构', '操作系统', '计算机网络'];
  
  try {
    // 添加加载状态
    const isLoading = ref(true);
    
    // 尝试从API获取热门搜索词
    console.log('开始获取热门搜索词...');
    const response = await searchStore.fetchHotKeywords(5, true); // 强制刷新
    isLoading.value = false;
    
    console.log('API返回热门搜索词数据:', response);
    
    // 直接赋值，不做额外处理，因为searchStore已经处理好了API响应
    if (response && Array.isArray(response) && response.length > 0) {
      hotTags.value = response;
      console.log('成功更新热门搜索词:', hotTags.value);
    } else {
      console.warn('API返回的热门搜索词为空或格式不正确，使用默认值:', response);
      hotTags.value = defaultTags;
    }
  } catch (error) {
    console.error('获取热门搜索词失败，使用默认热搜词', error);
    hotTags.value = defaultTags;
  }
};

// 获取文件类型图标
const getFileTypeIcon = (fileType: string) => {
  if (!fileType) return Document;
  const type = fileType.toLowerCase();
  if (type.includes('doc')) return Document;
  if (type.includes('xls')) return DataAnalysis;
  if (type.includes('pdf')) return Files;
  if (type.includes('ppt')) return PictureFilled;
  if (type.includes('image') || type.includes('jpg') || type.includes('png')) return Picture;
  if (type.includes('video')) return VideoPlay;
  return Folder;
}

onMounted(async () => {
  // 先加载热门搜索词，确保UI显示优先
  await loadHotTags();
  
  // 并行加载其他数据
  Promise.all([
    getLatestResources(),
    getPopularResources(),
    getCategories(),
    getCourses() // 添加获取课程数据
  ])
})
</script>

<style lang="scss" scoped>
.home-container {
  padding-bottom: 40px;
}

.banner {
  margin-bottom: 60px;
  border-radius: 0 0 var(--el-border-radius-large) var(--el-border-radius-large);
  overflow: hidden;
  box-shadow: var(--el-box-shadow-light);
  
  .carousel-item {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #fff;
    height: 100%;
    background-image: linear-gradient(135deg, rgba(59, 130, 246, 0.8), rgba(16, 185, 129, 0.8));
    
    h2 {
      font-size: 42px;
      margin-bottom: 16px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
      font-weight: bold;
    }
    
    p {
      font-size: 20px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }
  }
}

.search-section {
  background: linear-gradient(120deg, var(--el-color-primary), #36d1dc);
  color: #fff;
  padding: 80px 0;
  margin-bottom: 40px;
  position: relative;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border-radius: 0 0 30px 30px;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url('@/assets/pattern.svg');
    background-size: cover;
    opacity: 0.1;
  }
  
  .search-content {
    max-width: 800px;
    margin: 0 auto;
    text-align: center;
    position: relative;
    z-index: 1;
    padding: 0 20px;
  }
  
  .search-title {
    font-size: 2.5rem;
    font-weight: 700;
    margin-bottom: 16px;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  }
  
  .search-subtitle {
    font-size: 1.2rem;
    margin-bottom: 32px;
    opacity: 0.9;
  }
  
  .search-box {
    display: flex;
    align-items: stretch;
    margin-bottom: 20px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
    border-radius: 8px;
    background-color: #fff;
    position: relative;
    z-index: 10;
    overflow: visible; /* 修改为visible，允许下拉菜单溢出 */
    
    .search-input {
      flex-grow: 1;
      :deep(.el-input__wrapper) {
        box-shadow: none !important;
        padding-left: 15px;
        background: transparent;
        border-radius: 0;
        height: 52px;
      }
      
      :deep(.el-input__inner) {
        height: 52px;
        font-size: 16px;
      }
      
      :deep(.el-input__prefix) {
        display: flex;
        align-items: center;
      }
    }
    
    .search-type-selector {
      display: flex;
      align-items: center;
      background-color: #f8f9fa;
      height: 52px;
    }
    
    .search-select {
      width: 100px;
      text-align: center;
      :deep(.el-input__wrapper) {
        box-shadow: none !important;
        background-color: transparent;
        border-radius: 0;
        height: 52px;
        line-height: 52px;
        padding: 0 8px;
      }
      
      :deep(.el-input__inner) {
        height: 52px;
        line-height: 52px;
        font-size: 14px;
        color: #606266;
        text-align: center;
      }
      
      :deep(.el-select__caret) {
        line-height: 52px;
        color: #606266;
      }
    }
    
    .search-button {
      height: 52px;
      font-size: 16px;
      padding: 0 25px;
      border-radius: 0 8px 8px 0;
      margin-left: 0;
      z-index: 1;
      background: linear-gradient(to right, var(--el-color-primary), #36d1dc);
      border: none;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &:hover {
        background: linear-gradient(to right, #36d1dc, var(--el-color-primary));
      }
    }
  }
  
  .search-tags {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    color: rgba(255, 255, 255, 0.9);
    
    span {
      font-size: 14px;
    }
    
    .tag-item {
      cursor: pointer;
      background-color: rgba(255, 255, 255, 0.2);
      border: none;
      color: #fff;
      font-size: 13px;
      transition: all 0.3s ease;
      
      &:hover {
        background-color: rgba(255, 255, 255, 0.3);
        transform: translateY(-2px);
      }
    }
  }
}

.card-container {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  h2 {
    font-size: 24px;
    color: var(--el-color-text-primary);
    font-weight: 600;
    position: relative;
    padding-left: 16px;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 20px;
      background: var(--el-color-primary);
      border-radius: 2px;
    }
  }
  
  .view-more {
    display: flex;
    align-items: center;
    font-size: 15px;
    color: var(--el-color-primary);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateX(5px);
    }
    
    .el-icon {
      margin-left: 5px;
    }
  }
}

.category-list {
  margin-bottom: 20px;
  
  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 30px 20px;
    background-color: var(--el-background-color-base);
    border-radius: var(--el-border-radius-base);
    cursor: pointer;
    transition: all 0.3s;
    height: 100%;
    
    &:hover {
      background-color: rgba(59, 130, 246, 0.05);
      transform: translateY(-8px);
      box-shadow: var(--el-box-shadow);
    }
    
    .el-icon {
      margin-bottom: 15px;
      color: var(--el-color-primary);
      font-size: 40px;
      transition: transform 0.3s ease;
    }
    
    span {
      font-size: 16px;
      font-weight: 500;
      color: var(--el-color-text-primary);
    }
  }
}

.resource-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  border-radius: var(--el-border-radius-base);
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.09);
    
    .resource-icon {
      transform: scale(1.1);
    }
  }
  
  .resource-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    padding: 15px 15px 0;
    
    .resource-icon {
      font-size: 48px;
      transition: transform 0.3s ease;
      padding: 10px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &.icon-note {
        color: #409EFF;
        background-color: rgba(64, 158, 255, 0.1);
      }
      
      &.icon-ppt {
        color: #67C23A;
        background-color: rgba(103, 194, 58, 0.1);
      }
      
      &.icon-doc {
        color: #E6A23C;
        background-color: rgba(230, 162, 60, 0.1);
      }
      
      &.icon-book {
        color: #9254DE;
        background-color: rgba(146, 84, 222, 0.1);
      }
      
      &.icon-lab {
        color: #F56C6C;
        background-color: rgba(245, 108, 108, 0.1);
      }
      
      &.icon-exam {
        color: #909399;
        background-color: rgba(144, 147, 153, 0.1);
      }
      
      &.icon-default {
        color: var(--el-color-primary);
        background-color: rgba(64, 158, 255, 0.1);
      }
    }
    
    .category-tag {
      border-radius: 20px;
      padding: 0 12px;
      height: 24px;
      line-height: 22px;
      font-weight: 500;
    }
  }
  
  .resource-title {
    font-size: 16px;
    margin-bottom: 15px;
    cursor: pointer;
    color: var(--el-color-text-primary);
    font-weight: 600;
    padding: 0 15px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.5;
    height: 48px;
    
    &:hover {
      color: var(--el-color-primary);
    }
  }
  
  .resource-meta {
    margin-top: auto;
    font-size: 13px;
    color: var(--el-color-text-secondary);
    padding: 15px;
    border-top: 1px solid var(--el-border-color-lighter);
    background-color: var(--el-background-color-base);
    
    span {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      display: block;
    }
    
    .resource-stats {
      display: flex;
      justify-content: space-between;
      margin-top: 10px;
      
      span {
        display: flex;
        align-items: center;
        
        .el-icon {
          margin-right: 5px;
          font-size: 14px;
        }
      }
    }
  }
}

.resource-icon {
  .category-icon-img {
    width: 48px;
    height: 48px;
    object-fit: contain;
  }
}

// 搜索建议样式修改
.search-suggestions {
  position: absolute;
  z-index: 9999; // 确保最高层级
  top: 100%; // 紧贴搜索框底部
  left: 0;
  width: 100%;
  margin-top: 5px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  max-height: 350px;
  overflow-y: auto;
  text-align: left;
  border: 1px solid #ebeef5;
  
  ul {
    list-style: none;
    padding: 0;
    margin: 0;
    
    li {
      padding: 12px 16px;
      cursor: pointer;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 14px;
      transition: all 0.2s;
      color: #333;
      border-bottom: 1px solid #f2f6fc;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:hover, &.active {
        background-color: #f5f7fa;
      }
      
      .suggestion-content {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        
        :deep(em.highlight) {
          font-style: normal;
          color: #409EFF;
          font-weight: 600;
          background-color: rgba(64, 158, 255, 0.1);
          padding: 0 2px;
          border-radius: 2px;
        }
      }
      
      .suggestion-category {
        margin-left: 10px;
        color: #909399;
        font-size: 12px;
        padding: 0 6px;
        border-radius: 10px;
        background: #f2f6fc;
      }
    }
  }
}

// 添加课程选择相关样式
.course-section {
  margin-bottom: 30px;
}

.course-selector {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
  
  .course-select {
    flex: 1;
  }
  
  .el-button {
    height: 40px;
  }
}

.course-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  
  .course-name {
    font-weight: 500;
  }
  
  .course-credit {
    color: #67c23a;
    font-size: 12px;
    background: rgba(103, 194, 58, 0.1);
    padding: 2px 6px;
    border-radius: 4px;
  }
}

.course-resources {
  margin-top: 20px;
}

/* 确保在移动设备上的响应式布局 */
@media (max-width: 768px) {
  .course-selector {
    flex-direction: column;
    gap: 10px;
  }
}
</style> 