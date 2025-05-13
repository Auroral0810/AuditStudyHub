<template>
  <div class="resource-request-container page-container">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">资源需求</h1>
        <p class="page-desc">在这里发布您需要的资源，让其他用户帮助您</p>
      </div>
      
      <el-button type="primary" @click="openPublishDialog" class="publish-btn" round>
        <el-icon><Plus /></el-icon> 发布需求
      </el-button>
    </div>
    
    <!-- 筛选区域 -->
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
        
        <el-form-item label="学院">
          <el-select
            v-model="filterForm.collegeId"
            placeholder="选择学院"
            clearable
            @change="handleCollegeChange"
            style="width: 160px"
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
            style="width: 160px"
          >
            <el-option
              v-for="item in majorOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select
            v-model="filterForm.status"
            placeholder="请求状态"
            clearable
            style="width: 100px"
          >
            <el-option label="待解决" :value="0" />
            <el-option label="已解决" :value="1" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="资源分类">
          <el-select
            v-model="filterForm.categoryId"
            placeholder="选择资源分类"
            clearable
            class="w-full"
            style="width: 120px"
          >
            <el-option
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
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
      
      <!-- 排序选项 -->
      <div class="sort-options">
        <span>排序：</span>
        <el-radio-group v-model="sortOption" size="small" @change="handleSearch">
          <el-radio-button label="newest">最新发布</el-radio-button>
          <el-radio-button label="popular">热门需求</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    
    <!-- 资源需求列表 -->
    <div class="request-list">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated :loading="loading" />
      </div>
      
      <template v-else>
        <el-empty
          v-if="requestList.length === 0"
          description="暂无资源需求"
          class="empty-state"
        >
          <template #image>
            <img src="@/assets/empty-resources.svg" class="empty-image" alt="暂无资源" />
          </template>
          <el-button type="primary" @click="openPublishDialog" round>发布需求</el-button>
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
                  <span v-html="item.title"></span>
                </h3>
                <el-tag :type="item.status === 0 ? 'warning' : 'success'" size="small" effect="dark">
                  {{ item.status === 0 ? '待解决' : '已解决' }}
                </el-tag>
              </div>
              
              <div class="request-content">
                <p class="request-desc"><span v-html="item.content"></span></p>
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
                <div class="meta-item" v-if="item.categoryName">
                  <el-icon><Files /></el-icon>
                  <span>{{ item.categoryName }}</span>
                </div>
                <div class="meta-item" v-if="item.courseName">
                  <el-icon><Collection /></el-icon>
                  <span>{{ item.courseName }}</span>
                </div>
                <div class="meta-item" v-if="item.viewCount !== undefined && item.viewCount !== null">
                  <el-icon><View /></el-icon>
                  <span>浏览 {{ item.viewCount }}</span>
                </div>
                <div class="meta-item" v-if="item.replyCount !== undefined && item.replyCount !== null">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>评论 {{ item.replyCount }}</span>
                </div>
              </div>
            </div>
            
            <div class="card-divider"></div>
            
            <div class="request-footer">
              <div class="user-info">
                <el-avatar :size="32" :src="item.userAvatar || defaultAvatar"></el-avatar>
                <div class="user-detail">
                  <div class="username"><span v-html="item.username"></span></div>
                  <div class="request-time">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ formatDate(item.createTime) }}</span>
                  </div>
                </div>
              </div>
              
              <div class="request-actions">
                <el-button 
                  v-if="item.status === 1 && item.resourceId" 
                  type="success" 
                  size="small"
                  @click="goToResource(item.resourceId)"
                  round
                >
                  <el-icon><Link /></el-icon> 查看资源
                </el-button>
                
                <el-button 
                  v-if="item.status === 0" 
                  type="primary" 
                  size="small"
                  @click="openUploadDialog(item.id)"
                  round
                >
                  <el-icon><Upload /></el-icon> 提供资源
                </el-button>
                
                <el-button size="small" @click="viewRequest(item.id)" round>
                  <el-icon><View /></el-icon> 查看详情
                </el-button>
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
    
    <!-- 发布需求对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布资源需求"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="publishFormRef"
        :model="publishForm"
        :rules="publishRules"
        label-width="100px"
      >
        <el-form-item label="需求标题" prop="title">
          <el-input v-model="publishForm.title" placeholder="请输入需求标题" />
        </el-form-item>
        
        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="publishForm.description"
            type="textarea"
            rows="4"
            placeholder="请详细描述您需要的资源，例如课程、章节、类型等"
          />
        </el-form-item>
        
        <el-form-item label="课程名称" prop="courseId">
          <el-select
            v-model="publishForm.courseId"
            filterable
            remote
            :remote-method="searchCourseName"
            placeholder="请选择或搜索课程"
            :loading="courseLoading"
            clearable
            class="w-full"
          >
            <el-option
              v-for="course in courseOptions"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            >
              <span>{{ course.name }}</span>
              <span v-if="course.credit" class="course-credit">({{ course.credit }}学分)</span>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="资源类型" prop="categoryId">
          <el-select v-model="publishForm.categoryId" placeholder="请选择资源类型" class="w-full">
            <el-option
              v-for="category in categoryOptions"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="学院" prop="collegeId">
          <el-select
            v-model="publishForm.collegeId"
            placeholder="请选择学院"
            @change="handlePublishCollegeChange"
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
        
        <el-form-item label="专业" prop="majorId" v-if="publishForm.collegeId">
          <el-select v-model="publishForm.majorId" placeholder="请选择专业" class="w-full">
            <el-option
              v-for="item in publishMajorOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRequest" :loading="submitting" round>发布需求</el-button>
      </template>
    </el-dialog>
    
    <!-- 提供资源对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="提供资源"
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
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { useCollegeStore } from '../../stores/collegeStore'
import { useMajorStore } from '../../stores/majorStore'
import { useResourceRequestStore } from '../../stores/resourceRequestStore'
import { useCategoryStore } from '../../stores/categoryStore'
import { useCourseStore } from '../../stores/courseStore'
import { 
  Search, Plus, RefreshRight, View, Calendar, School, Reading, Collection,
  Link, Upload, ChatDotRound, Files
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const collegeStore = useCollegeStore()
const majorStore = useMajorStore()
const resourceRequestStore = useResourceRequestStore()
const categoryStore = useCategoryStore()
const courseStore = useCourseStore()

// 数据定义 - 更新为与后端DTO匹配的结构
interface ResourceRequest {
  id: number
  title: string
  content: string // 后端是content而不是description
  courseName: string | null
  resourceType?: string
  collegeId: number
  collegeName: string
  majorId?: number
  majorName?: string
  status: number // 0: 待解决, 1: 已解决
  resourceId?: number // 解决该需求的资源ID
  userId: number
  username: string // 直接使用用户名
  userAvatar?: string
  viewCount: number
  replyCount: number // 后端使用replyCount
  createTime: string // 后端使用createTime
  updateTime: string // 后端使用updateTime
  categoryName?: string
}

// 列表数据
const loading = ref(false)
const requestList = ref<ResourceRequest[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const sortOption = ref('newest')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 筛选表单
const filterForm = reactive({
  keyword: '',
  collegeId: '',
  majorId: '',
  status: '',
  categoryId: undefined
})

// 发布需求
const publishDialogVisible = ref(false)
const publishFormRef = ref<FormInstance>()
const submitting = ref(false)
const publishForm = reactive({
  title: '',
  description: '', // 前端表单使用description，但提交时映射为content
  courseId: '', // 改为courseId
  categoryId: '',
  collegeId: '',
  majorId: ''
})

// 提供资源
const uploadDialogVisible = ref(false)
const uploadFormRef = ref<FormInstance>()
const uploadForm = reactive({
  requestId: '',
  resourceId: '',
  remark: ''
})

// 表单校验规则
const publishRules = reactive({
  title: [
    { required: true, message: '请输入需求标题', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入详细描述', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ],
})

const uploadRules = reactive({
  resourceId: [
    { required: true, message: '请输入资源ID', trigger: 'blur' }
  ]
})

// 学院选项 - 从 store 中获取
const collegeOptions = computed(() => {
  const colleges = collegeStore.colleges || []
  return colleges.map(college => ({
    value: String(college.id),
    label: college.name
  }))
})

// 专业选项
const majorOptions = ref<{ value: string; label: string }[]>([])
const publishMajorOptions = ref<{ value: string; label: string }[]>([])

// 资源类型选项 - 从 store 中获取
const resourceTypeOptions = ref<{ value: string; label: string }[]>([])

// 课程下拉框相关
const courseLoading = ref(false)
const courseOptions = ref([])
const allCourses = ref([])

// 分类选项 - 从store中动态获取
const categoryOptions = computed(() => {
  return categoryStore.categories.map(category => ({
    value: category.id.toString(),
    label: category.name,
    color: getCategoryColorByStyleType(category.styleType)
  }))
})

// 获取分类颜色的辅助函数
const getCategoryColorByStyleType = (styleType) => {
  const styleColors = {
    'success': '#67c23a',
    'primary': '#409eff',
    'warning': '#e6a23c',
    'danger': '#f56c6c',
    'info': '#909399',
    'purple': '#9254de'
  }
  return styleColors[styleType] || styleColors.info
}

// 处理学院变化
const handleCollegeChange = async () => {
  filterForm.majorId = ''
  if (filterForm.collegeId) {
    const majors = await majorStore.fetchMajorsByCollege(Number(filterForm.collegeId))
    majorOptions.value = majors.map(major => ({
      value: String(major.id),
      label: major.name
    }))
  } else {
    majorOptions.value = []
  }
}

// 处理发布表单学院变化
const handlePublishCollegeChange = async () => {
  publishForm.majorId = ''
  if (publishForm.collegeId) {
    const majors = await majorStore.fetchMajorsByCollege(Number(publishForm.collegeId))
    publishMajorOptions.value = majors.map(major => ({
      value: String(major.id),
      label: major.name
    }))
  } else {
    publishMajorOptions.value = []
  }
}

// 初始化资源类型数据
const initResourceTypes = async () => {
  try {
    // 从分类 store 中获取资源类型数据
    const categories = await categoryStore.fetchCategories()
    resourceTypeOptions.value = categories.map(category => ({
      value: category.id.toString(), // 只使用ID作为value，去掉code属性
      label: category.name
    }))
    
    // 如果没有获取到分类数据，则使用默认值
    if (resourceTypeOptions.value.length === 0) {
      resourceTypeOptions.value = [
        { value: 'ppt', label: '课件PPT' },
        { value: 'exercise', label: '习题解析' },
        { value: 'exam', label: '考试资料' },
        { value: 'note', label: '学习笔记' },
        { value: 'report', label: '实验报告' },
        { value: 'other', label: '其他' }
      ]
    }
  } catch (error) {
    console.error('获取资源类型失败:', error)
    // 使用默认值作为备选
    resourceTypeOptions.value = [
      { value: 'ppt', label: '课件PPT' },
      { value: 'exercise', label: '习题解析' },
      { value: 'exam', label: '考试资料' },
      { value: 'note', label: '学习笔记' },
      { value: 'report', label: '实验报告' },
      { value: 'other', label: '其他' }
    ]
  }
}

// 获取所有课程并初始化下拉框
const initCoursesDropdown = async () => {
  courseLoading.value = true
  try {
    const response = await courseStore.fetchCourses()
    allCourses.value = response
    courseOptions.value = response
    console.log('获取课程数据成功:', allCourses.value)
  } catch (error) {
    console.error('获取课程数据失败:', error)
  } finally {
    courseLoading.value = false
  }
}

// 搜索课程
const searchCourseName = (query) => {
  if (query) {
    // 如果有搜索词则筛选匹配的课程
    courseOptions.value = allCourses.value.filter(course => 
      course.name.toLowerCase().includes(query.toLowerCase())
    )
  } else {
    // 如果没有搜索词则显示所有课程
    courseOptions.value = allCourses.value
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

// 获取资源需求列表
const getRequestList = async () => {
  loading.value = true
  try {
    const { list, total: totalCount } = await resourceRequestStore.getList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: filterForm.keyword,
      collegeId: filterForm.collegeId ? Number(filterForm.collegeId) : undefined,
      majorId: filterForm.majorId ? Number(filterForm.majorId) : undefined,
      status: filterForm.status !== '' ? Number(filterForm.status) : undefined,
      sort: sortOption.value === 'popular' ? 'hot' : 'newest'
    })
    
    requestList.value = list
    total.value = totalCount
  } catch (error) {
    console.error('获取资源需求列表失败', error)
    ElMessage.error('获取资源需求列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  getRequestList()
}

// 重置筛选条件
const resetFilter = () => {
  Object.keys(filterForm).forEach(key => {
    (filterForm as any)[key] = ''
  })
  majorOptions.value = []
  sortOption.value = 'newest'
  currentPage.value = 1
  getRequestList()
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  getRequestList()
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  getRequestList()
}

// 查看资源需求详情
const viewRequest = (id: number) => {
  router.push(`/resource-request/${id}`)
}

// 打开发布需求对话框
const openPublishDialog = () => {
  // 检查用户是否登录
  if (!userStore.token) {
    ElMessage.warning('请先登录再发布需求')
    router.push('/login')
    return
  }
  
  // 重置表单
  if (publishFormRef.value) {
    publishFormRef.value.resetFields()
  }
  
  publishDialogVisible.value = true
}

// 提交资源需求
const submitRequest = async () => {
  if (!publishFormRef.value) return
  
  await publishFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 如果有选择课程，获取课程名称
        let selectedCourseName = '';
        if (publishForm.courseId) {
          const selectedCourse = courseOptions.value.find(course => course.id === publishForm.courseId);
          if (selectedCourse) {
            selectedCourseName = selectedCourse.name;
          }
        }
        
        // 创建提交数据对象
        const requestData = {
          title: publishForm.title,
          content: publishForm.description, 
          courseId: publishForm.courseId ? Number(publishForm.courseId) : undefined,
          courseName: selectedCourseName,
          categoryId: Number(publishForm.categoryId),
          collegeId: Number(publishForm.collegeId),
          majorId: publishForm.majorId ? Number(publishForm.majorId) : undefined
        };
        
        // 获取当前用户ID
        const userId = userStore.userInfo?.id;
        
        if (!userId) {
          ElMessage.warning('请先登录再发布需求');
          return;
        }
        
        await resourceRequestStore.createRequest(requestData, userId);
        
        publishDialogVisible.value = false
        getRequestList()
      } catch (error) {
        console.error('发布需求失败', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 打开上传资源对话框
const openUploadDialog = (requestId: number) => {
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
  
  uploadForm.requestId = requestId.toString()
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
          remark: uploadForm.remark
        })
        
        uploadDialogVisible.value = false
        getRequestList()
      } catch (error) {
        console.error('提交资源失败', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 跳转到资源页面
const goToResource = (resourceId: number) => {
  router.push(`/resource/${resourceId}`)
}

// 初始化学院和专业数据
const initCollegesAndMajors = async () => {
  await collegeStore.fetchColleges()
}

// 初始化分类数据
const initCategories = async () => {
  try {
    await categoryStore.fetchCategories()
  } catch (error) {
    console.error('获取分类数据失败:', error)
  }
}

onMounted(async () => {
  await Promise.all([
    initCollegesAndMajors(),
    initCategories(),
    initResourceTypes(),
    initCoursesDropdown()
  ])
  getRequestList()
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

.resource-request-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #fff;
    border-radius: 10px;
    padding: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    
    .header-content {
      .page-title {
        font-size: 26px;
        margin-bottom: 8px;
        color: #303133;
      }
      
      .page-desc {
        color: #606266;
        margin-bottom: 0;
      }
    }
    
    .publish-btn {
      font-size: 15px;
      padding: 10px 20px;
    }
  }
  
  .filter-section {
    margin-bottom: 20px;
    background-color: #fff;
    border-radius: 10px;
    padding: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    
    .filter-form {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      margin-bottom: 15px;
      
      .el-form-item {
        margin-bottom: 10px;
        margin-right: 15px;
      }
      
      .search-input {
        width: 280px;
      }
    }
    
    .sort-options {
      display: flex;
      align-items: center;
      padding-top: 15px;
      border-top: 1px solid #ebeef5;
      
      span {
        margin-right: 15px;
        color: #606266;
        font-weight: 500;
      }
    }
  }
  
  .loading-container {
    padding: 20px;
    background-color: #fff;
    border-radius: 10px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  }
  
  .empty-state {
    padding: 40px 0;
    background-color: #fff;
    border-radius: 10px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    
    .empty-image {
      width: 200px;
      height: 200px;
    }
  }
  
  .request-list {
    margin-bottom: 20px;
    
    .request-card {
      margin-bottom: 20px;
      border-radius: 10px;
      overflow: hidden;
      transition: all 0.3s ease;
      position: relative;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
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
      
      .request-card-inner {
        padding: 20px 20px 20px 24px;
      }
      
      .request-header {
        margin-bottom: 15px;
        
        .title-status {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;
          
          .request-title {
            font-size: 18px;
            font-weight: 600;
            margin: 0;
            cursor: pointer;
            color: #303133;
            
            &:hover {
              color: #409eff;
            }
          }
        }
        
        .request-content {
          margin-bottom: 15px;
          
          .request-desc {
            color: #606266;
            margin-bottom: 12px;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            line-height: 1.6;
            font-size: 15px;
          }
        }
        
        .request-meta {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
          
          .meta-item {
            display: flex;
            align-items: center;
            padding: 3px 10px;
            background-color: #f5f7fa;
            border-radius: 15px;
            color: #606266;
            font-size: 13px;
            
            .el-icon {
              margin-right: 5px;
              color: #909399;
            }
          }
        }
      }
      
      .card-divider {
        height: 1px;
        background-color: #f0f2f5;
        margin: 15px -20px;
      }
      
      .request-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .user-info {
          display: flex;
          align-items: center;
          
          .user-detail {
            margin-left: 10px;
            
            .username {
              font-size: 14px;
              font-weight: 500;
              color: #303133;
            }
            
            .request-time {
              display: flex;
              align-items: center;
              color: #909399;
              font-size: 12px;
              margin-top: 3px;
              
              .el-icon {
                margin-right: 5px;
                font-size: 12px;
              }
            }
          }
        }
        
        .request-actions {
          display: flex;
          gap: 10px;
        }
      }
    }
  }
  
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    background-color: #fff;
    padding: 15px;
    border-radius: 10px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  }
  
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
  }
  
  .w-full {
    width: 100%;
  }

  .course-credit {
    color: #909399;
    margin-left: 8px;
    font-size: 12px;
  }

  :deep(.el-select-dropdown__item) {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .resource-request-container {
    padding: 15px;
    
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      
      .publish-btn {
        margin-top: 15px;
        width: 100%;
      }
    }
    
    .filter-form {
      flex-direction: column;
      align-items: stretch;
      
      .el-form-item {
        width: 100%;
        margin-right: 0 !important;
      }
      
      .search-input {
        width: 100% !important;
      }
    }
    
    .sort-options {
      flex-direction: column;
      align-items: flex-start;
      
      span {
        margin-bottom: 10px;
      }
    }
    
    .request-footer {
      flex-direction: column;
      
      .user-info {
        margin-bottom: 15px;
      }
      
      .request-actions {
        width: 100%;
        flex-wrap: wrap;
        justify-content: center;
        
        .el-button {
          flex: 1;
          min-width: 100px;
        }
      }
    }
  }
}

.resource-category-tag {
  margin-top: 8px;
}
</style> 