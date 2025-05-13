<template>
  <div class="request-edit-container page-container">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">编辑资源需求</h1>
        <p class="page-desc">更新您的资源需求信息</p>
      </div>
    </div>
    
    <el-card class="edit-form-card" v-loading="loading">
      <template v-if="!requestData">
        <div class="empty-state">
          <el-empty description="未找到请求数据" />
          <el-button type="primary" @click="goBack" round>返回</el-button>
        </div>
      </template>
      
      <template v-else>
        <el-form
          ref="editFormRef"
          :model="editForm"
          :rules="formRules"
          label-width="100px"
        >
          <el-form-item label="需求标题" prop="title">
            <el-input v-model="editForm.title" placeholder="请输入需求标题" />
          </el-form-item>
          
          <el-form-item label="详细描述" prop="content">
            <el-input
              v-model="editForm.content"
              type="textarea"
              rows="4"
              placeholder="请详细描述您需要的资源，例如课程、章节、类型等"
            />
          </el-form-item>
          
          <el-form-item label="课程名称" prop="courseName">
            <el-select
              v-model="editForm.courseId"
              filterable
              remote
              :remote-method="searchCourseName"
              placeholder="请选择或搜索课程"
              :loading="courseLoading"
              clearable
              class="w-full"
              @change="handleCourseChange"
            >
              <el-option
                v-for="(course, index) in courseOptions"
                :key="course.id ? course.id : index"
                :label="course.name || ''"
                :value="course.id ? course.id.toString() : ''"
              >
                <span>{{ course.name || '' }}</span>
                <span v-if="course.credit" class="course-credit">({{ course.credit }}学分)</span>
              </el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="资源类型" prop="resourceType">
            <el-select v-model="editForm.resourceType" placeholder="请选择资源类型" class="w-full">
              <el-option
                v-for="item in resourceTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="学院" prop="collegeId">
            <el-select
              v-model="editForm.collegeId"
              placeholder="请选择学院"
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
          
          <el-form-item label="专业" prop="majorId" v-if="editForm.collegeId">
            <el-select v-model="editForm.majorId" placeholder="请选择专业" class="w-full">
              <el-option
                v-for="item in majorOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="submitting" round>
              更新需求
            </el-button>
            <el-button @click="goBack" round>返回</el-button>
          </el-form-item>
        </el-form>
      </template>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCollegeStore } from '@/stores/collegeStore'
import { useMajorStore } from '@/stores/majorStore'
import { useResourceRequestStore } from '@/stores/resourceRequestStore'
import { useCategoryStore } from '@/stores/categoryStore'
import { useCourseStore } from '@/stores/courseStore'
import type { CourseBaseDTO } from '@/types/course'
import type { ResourceRequest, ResourceRequestUpdateParams } from '@/types/resource-request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collegeStore = useCollegeStore()
const majorStore = useMajorStore()
const resourceRequestStore = useResourceRequestStore()
const categoryStore = useCategoryStore()
const courseStore = useCourseStore()

// 确保用户已登录
if (!userStore.token || !userStore.userInfo) {
  ElMessage.warning('请先登录')
  router.push('/login')
}

// 获取请求ID
const requestId = Number(route.params.id)
const loading = ref(true)
const submitting = ref(false)
const requestData = ref<ResourceRequest | null>(null)
const editFormRef = ref<FormInstance>()

// 课程下拉框相关
const courseLoading = ref(false)
const courseOptions = ref<CourseBaseDTO[]>([])
const allCourses = ref<CourseBaseDTO[]>([])

// 编辑表单
const editForm = reactive({
  title: '',
  content: '',
  courseName: '',
  courseId: '',
  resourceType: '',
  collegeId: '',
  majorId: ''
})

// 表单验证规则
const formRules = reactive({
  title: [
    { required: true, message: '请输入需求标题', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入详细描述', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  resourceType: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ]
})

// 学院选项
const collegeOptions = computed(() => {
  const colleges = collegeStore.colleges || []
  return colleges.map(college => ({
    value: String(college.id),
    label: college.name
  }))
})

// 专业选项
const majorOptions = ref<{ value: string; label: string }[]>([])

// 资源类型选项
const resourceTypeOptions = ref<{ value: string; label: string }[]>([])

// 处理学院变化，加载对应专业
const handleCollegeChange = async () => {
  // 先记住原来的专业ID
  const originalMajorId = editForm.majorId;
  
  // 临时清空
  editForm.majorId = '';
  
  if (editForm.collegeId) {
    const majors = await majorStore.fetchMajorsByCollege(Number(editForm.collegeId));
    majorOptions.value = majors.map(major => ({
      value: String(major.id),
      label: major.name
    }));
    
    // 恢复之前的专业ID
    if (originalMajorId && majors.some(major => String(major.id) === originalMajorId)) {
      editForm.majorId = originalMajorId;
    }
  } else {
    majorOptions.value = [];
  }
}

// 初始化资源类型数据
const initResourceTypes = async () => {
  try {
    // 从分类 store 中获取资源类型数据
    const categories = await categoryStore.fetchCategories()
    resourceTypeOptions.value = categories.map(category => ({
      value: category.id.toString(),
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
    allCourses.value = response as CourseBaseDTO[]
    courseOptions.value = response as CourseBaseDTO[]
    console.log('获取课程数据成功:', allCourses.value)
  } catch (error) {
    console.error('获取课程数据失败:', error)
  } finally {
    courseLoading.value = false
  }
}

// 搜索课程
const searchCourseName = (query: string) => {
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

// 获取请求详情
const fetchRequestDetail = async () => {
  loading.value = true
  try {
    const res = await resourceRequestStore.getDetail(requestId)
    if (res && res.request) {
      requestData.value = res.request
      
      // 填充表单数据
      editForm.title = res.request.title || ''
      editForm.content = res.request.content || ''
      editForm.courseId = res.request.courseId ? String(res.request.courseId) : ''
      editForm.courseName = res.request.courseName || ''
      editForm.resourceType = res.request.categoryId ? String(res.request.categoryId) : ''
      editForm.collegeId = String(res.request.collegeId || '')
      editForm.majorId = String(res.request.majorId || '')
      
      console.log('表单填充数据:', editForm)
      
      // 如果有学院ID，加载对应的专业列表
      if (editForm.collegeId) {
        await handleCollegeChange()
      }
    } else {
      ElMessage.error('获取请求详情失败')
    }
  } catch (error) {
    console.error('获取请求详情错误:', error)
    ElMessage.error('获取请求详情失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 获取当前用户ID
      if (!userStore.userInfo) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }
      
      const userId = userStore.userInfo.id
      
      // 构建更新数据
      const updateData: ResourceRequestUpdateParams = {
        title: editForm.title,
        content: editForm.content,
        courseId: editForm.courseId ? Number(editForm.courseId) : undefined,
        courseName: editForm.courseName || undefined,
        categoryId: Number(editForm.resourceType),
        collegeId: Number(editForm.collegeId),
        majorId: editForm.majorId ? Number(editForm.majorId) : undefined
      }
      
      // 调用更新接口
      const result = await resourceRequestStore.updateRequest(requestId, updateData, userId)
      
      if (result) {
        ElMessage.success('更新资源需求成功')
        // 更新成功后返回到请求详情页
        router.push(`/resource-request/${requestId}`)
      }
    } catch (error) {
      console.error('更新资源需求失败:', error)
      ElMessage.error('更新资源需求失败')
    } finally {
      submitting.value = false
    }
  })
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 初始化
onMounted(async () => {
  await Promise.all([
    collegeStore.fetchColleges(),
    initResourceTypes(),
    initCoursesDropdown()
  ])
  await fetchRequestDetail()
})

// 处理课程变化的函数
const handleCourseChange = (courseId: string | null | undefined) => {
  if (courseId) {
    const course = courseOptions.value.find(c => c.id && c.id.toString() === courseId);
    if (course && course.name) {
      editForm.courseName = course.name;
    }
  } else {
    editForm.courseName = '';
  }
}
</script>

<style lang="scss" scoped>
.request-edit-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
    
    .header-content {
      .page-title {
        font-size: 24px;
        margin-bottom: 8px;
        color: #303133;
      }
      
      .page-desc {
        color: #606266;
        margin-bottom: 0;
      }
    }
  }
  
  .edit-form-card {
    margin-bottom: 20px;
    border-radius: 8px;
    
    .empty-state {
      padding: 40px 0;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
  }
  
  .w-full {
    width: 100%;
  }
  
  .course-credit {
    color: #909399;
    margin-left: 8px;
    font-size: 12px;
  }
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .request-edit-container {
    padding: 15px;
    
    .edit-form-card {
      padding: 15px;
    }
  }
}
</style> 