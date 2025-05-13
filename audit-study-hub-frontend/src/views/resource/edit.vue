<template>
    <div class="edit-resource-container">
      <div class="edit-header">
        <h1>编辑资料</h1>
        <p class="edit-tip">编辑后的资料将再次由管理员审核后发布，请确保资料内容符合规范</p>
      </div>
  
      <el-card class="edit-card" v-loading="loading">
        <div v-if="loading" class="loading-placeholder">
          <p>正在加载资源信息...</p>
        </div>
        
        <template v-else>
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
          >
            <el-form-item label="资料标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入资料标题" maxlength="100" show-word-limit></el-input>
            </el-form-item>
  
            <el-form-item label="资料分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择资料分类" class="w-full">
                <el-option
                  v-for="item in categoryOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
  
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="所属学院" prop="collegeId">
                  <el-select 
                    v-model="form.collegeId" 
                    placeholder="请选择所属学院" 
                    class="w-full"
                    @change="handleCollegeChange"
                  >
                    <el-option
                      v-for="item in collegeOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="所属专业" prop="majorId">
                  <el-select 
                    v-model="form.majorId" 
                    placeholder="请选择所属专业" 
                    class="w-full"
                    :disabled="!form.collegeId"
                  >
                    <el-option
                      v-for="item in majorOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
  
            <el-form-item label="相关课程" prop="courseName">
              <el-select 
                v-model="form.courseName" 
                placeholder="请选择相关课程（选填）" 
                class="w-full"
                filterable
                :loading="courseLoading"
                clearable
              >
                <el-option
                  v-for="item in courseOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.label"
                ></el-option>
              </el-select>
            </el-form-item>
  
            <el-form-item label="资料标签" prop="tags">
              <el-tag
                v-for="tag in form.tags"
                :key="tag"
                closable
                @close="handleRemoveTag(tag)"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
              <el-input
                v-if="inputTagVisible"
                ref="tagInputRef"
                v-model="tagInputValue"
                class="tag-input"
                size="small"
                @keyup.enter="handleAddTag"
                @blur="handleAddTag"
              ></el-input>
              <el-button v-else class="tag-button" @click="showTagInput">
                <el-icon><Plus /></el-icon> 添加标签
              </el-button>
              <div class="tag-tip">添加标签有助于其他用户更好地找到您的资料</div>
            </el-form-item>
  
            <el-form-item label="资料描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="5"
                placeholder="请输入资料描述，包括内容概述、适用对象等信息"
                maxlength="500"
                show-word-limit
              ></el-input>
            </el-form-item>
            
            <!-- 文件上传部分 -->
            <el-form-item label="更新资料文件">
              <div class="upload-type-toggle">
                <el-radio-group v-model="form.uploadType" @change="toggleUploadType">
                  <el-radio-button label="file">更新文件</el-radio-button>
                  <el-radio-button label="url">资料链接</el-radio-button>
                  <el-radio-button label="keep">保持原文件</el-radio-button>
                </el-radio-group>
              </div>
              
              <!-- 当前文件信息 -->
              <div v-if="form.uploadType === 'keep'" class="current-file-info">
                <h4>当前文件</h4>
                <div class="file-info">
                  <div class="file-preview">
                    <el-icon v-if="resourceType === 'pdf'" color="#f56c6c" size="40"><Document /></el-icon>
                    <el-icon v-else-if="resourceType === 'doc'" color="#409eff" size="40"><Ticket /></el-icon>
                    <el-icon v-else-if="resourceType === 'excel'" color="#67c23a" size="40"><DataAnalysis /></el-icon>
                    <el-icon v-else-if="resourceType === 'ppt'" color="#e6a23c" size="40"><PictureFilled /></el-icon>
                    <el-icon v-else-if="resourceType === 'image'" color="#909399" size="40"><Picture /></el-icon>
                    <el-icon v-else-if="resourceType === 'audio'" color="#909399" size="40"><Headset /></el-icon>
                    <el-icon v-else-if="resourceType === 'video'" color="#909399" size="40"><VideoCamera /></el-icon>
                    <el-icon v-else color="#909399" size="40"><Files /></el-icon>
                  </div>
                  <div class="file-details">
                    <div class="file-name">{{ resourceFileName }}</div>
                    <div class="file-size">{{ formatFileSize(originalResource.fileSize) }}</div>
                    <div class="file-url">
                      <a :href="originalResource.fileUrl" target="_blank">{{ originalResource.fileUrl }}</a>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 上传新文件 -->
              <div v-if="form.uploadType === 'file'" class="upload-area">
                <el-upload
                  ref="uploadRef"
                  class="file-uploader"
                  drag
                  :auto-upload="false"
                  :limit="1"
                  :on-change="handleFileChange"
                  :on-exceed="handleExceed"
                  :before-remove="beforeRemove"
                >
                  <el-icon class="el-icon--upload"><Upload /></el-icon>
                  <div class="el-upload__text">
                    将文件拖到此处，或<em>点击上传</em>
                  </div>
                  <template #tip>
                    <div class="el-upload__tip">
                      支持上传PDF、Word、Excel、PPT、TXT等文档，以及图片、音频和视频文件，单个文件大小不超过100MB
                    </div>
                  </template>
                </el-upload>
                
                <div v-if="form.file" class="file-info">
                  <div class="file-preview">
                    <el-icon v-if="fileType === 'pdf'" color="#f56c6c" size="40"><Document /></el-icon>
                    <el-icon v-else-if="fileType === 'word'" color="#409eff" size="40"><Ticket /></el-icon>
                    <el-icon v-else-if="fileType === 'excel'" color="#67c23a" size="40"><DataAnalysis /></el-icon>
                    <el-icon v-else-if="fileType === 'ppt'" color="#e6a23c" size="40"><PictureFilled /></el-icon>
                    <el-icon v-else-if="fileType === 'image'" color="#909399" size="40"><Picture /></el-icon>
                    <el-icon v-else-if="fileType === 'audio'" color="#909399" size="40"><Headset /></el-icon>
                    <el-icon v-else-if="fileType === 'video'" color="#909399" size="40"><VideoCamera /></el-icon>
                    <el-icon v-else color="#909399" size="40"><Files /></el-icon>
                  </div>
                  <div class="file-details">
                    <div class="file-name">{{ form.file.name }}</div>
                    <div class="file-size">{{ formatFileSize(form.file.size) }}</div>
                  </div>
                </div>
              </div>
              
              <!-- 输入链接 -->
              <div v-else-if="form.uploadType === 'url'" class="url-upload-area">
                <el-input
                  v-model="form.fileUrl"
                  placeholder="请输入资料链接（如CSDN、GitHub、百度网盘等）"
                  class="url-input"
                >
                  <template #prefix>
                    <el-icon><Link /></el-icon>
                  </template>
                </el-input>
                <div class="url-tip">
                  请确保链接长期有效，如为网盘链接，请正确设置访问权限和提取码（如有）
                </div>
              </div>
            </el-form-item>
          </el-form>
  
          <div v-if="isUploading" class="upload-progress-container">
            <h4>文件上传中...</h4>
            <el-progress :percentage="uploadProgress" :format="percentFormat" />
          </div>
  
          <div class="action-buttons">
            <el-button @click="router.back()">取消</el-button>
            <el-button 
              type="primary"
              :loading="submitting"
              @click="submitEdit"
              :disabled="isUploading"
            >
              提交更新
            </el-button>
          </div>
        </template>
      </el-card>
    </div>
  </template>
  
  <script lang="ts" setup>
  import { ref, reactive, computed, nextTick, onMounted } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { UploadInstance, FormInstance } from 'element-plus'
  import { useUserStore } from '../../stores/user'
  import { useCollegeStore } from '../../stores/collegeStore'
  import { useMajorStore } from '../../stores/majorStore'
  import { useCategoryStore } from '../../stores/categoryStore'
  import { useCourseStore } from '../../stores/courseStore'
  import { useResourceStore } from '../../stores/resource'
  import { 
    Upload, 
    Document, 
    DataAnalysis, 
    Files, 
    Picture, 
    VideoCamera, 
    Ticket, 
    PictureFilled, 
    Headset, 
    Plus, 
    Link
  } from '@element-plus/icons-vue'
  
  const router = useRouter()
  const route = useRoute()
  const userStore = useUserStore()
  const collegeStore = useCollegeStore()
  const majorStore = useMajorStore()
  const categoryStore = useCategoryStore()
  const courseStore = useCourseStore()
  const resourceStore = useResourceStore()
  
  // 资源ID
  const resourceId = computed(() => Number(route.params.id))
  
  // 加载状态
  const loading = ref(true)
  const submitting = ref(false)
  const isUploading = ref(false)
  const uploadProgress = ref(0)
  
  // 表单和验证
  const formRef = ref<FormInstance>()
  const uploadRef = ref<UploadInstance>()
  
  // 原始资源信息
  const originalResource = ref<any>({})
  
  // 标签相关
  const tagInputRef = ref()
  const inputTagVisible = ref(false)
  const tagInputValue = ref('')
  
  // 表单数据
  const form = reactive({
    title: '',
    categoryId: '',
    collegeId: '',
    majorId: '',
    courseName: '',
    tags: [] as string[],
    description: '',
    file: null as File | null,
    fileUrl: '',
    uploadType: 'keep', // 'keep', 'file', 'url'
    originalFileUrl: ''
  })
  
  // 验证规则
  const rules = {
    title: [
      { required: true, message: '请输入资料标题', trigger: 'blur' },
      { min: 5, max: 100, message: '标题长度应在5-100个字符之间', trigger: 'blur' }
    ],
    categoryId: [
      { required: true, message: '请选择资料分类', trigger: 'change' }
    ],
    description: [
      { required: true, message: '请输入资料描述', trigger: 'blur' },
      { min: 10, max: 500, message: '描述长度应在10-500个字符之间', trigger: 'blur' }
    ]
  }
  
  // 资源分类选项
  const categoryOptions = ref<{ value: string; label: string }[]>([])
  
  // 学院选项
  const collegeOptions = ref<{ value: string; label: string }[]>([])
  
  // 专业选项
  const majorOptions = ref<{ value: string; label: string }[]>([])
  
  // 课程选项
  const courseOptions = ref<{ value: string; label: string }[]>([])
  const courseLoading = ref(false)
  
  // 初始化数据
  const initData = async () => {
    try {
      // 获取分类数据
      const categories = await categoryStore.fetchCategories()
      categoryOptions.value = categories.map(category => ({
        value: category.id.toString(),
        label: category.name
      }))
      
      // 获取学院数据
      const colleges = await collegeStore.fetchColleges()
      collegeOptions.value = colleges.map(college => ({
        value: college.id.toString(),
        label: college.name
      }))
      
      // 获取课程数据
      await fetchCourses()
      
      // 获取资源详情
      await fetchResourceDetail()
    } catch (error) {
      console.error('初始化数据失败:', error)
      ElMessage.error('加载数据失败，请刷新页面重试')
    } finally {
      loading.value = false
    }
  }
  
  // 获取资源详情
  const fetchResourceDetail = async () => {
    try {
      const userId = userStore.userInfo?.id
      if (!userId) {
        ElMessage.error('未获取到用户ID，请重新登录')
        router.push('/login')
        return
      }
      
      // 获取资源详情
      const resource = await resourceStore.getResourceDetail(resourceId.value)
      if (!resource) {
        ElMessage.error('获取资源详情失败')
        router.back()
        return
      }
      
      // 保存原始资源信息
      originalResource.value = resource
      
      // 填充表单
      form.title = resource.title
      form.description = resource.description
      form.categoryId = resource.categoryId.toString()
      form.collegeId = resource.collegeId.toString()
      
      // 获取专业选项
      if (resource.collegeId) {
        await handleCollegeChange(resource.collegeId.toString())
        form.majorId = resource.majorId.toString()
      }
      
      form.courseName = resource.courseName || ''
      form.originalFileUrl = resource.fileUrl
      form.fileUrl = resource.fileUrl
      
      // 处理标签
      try {
        const tags = typeof resource.tags === 'string' ? JSON.parse(resource.tags) : resource.tags
        form.tags = Array.isArray(tags) ? tags : []
      } catch (e) {
        form.tags = []
      }
      
      // 检查创建者是否是当前用户
      if (resource.uploaderId !== userId) {
        ElMessage.error('您没有权限编辑此资源')
        router.back()
      }
    } catch (error) {
      console.error('获取资源详情失败:', error)
      ElMessage.error('获取资源详情失败，请返回重试')
      router.back()
    }
  }
  
  // 获取课程数据
  const fetchCourses = async () => {
    courseLoading.value = true
    try {
      const courses = await courseStore.fetchCourses()
      courseOptions.value = courses.map(course => ({
        value: course.id ? course.id.toString() : '',
        label: course.name || ''
      }))
    } catch (error) {
      console.error('获取课程数据失败:', error)
    } finally {
      courseLoading.value = false
    }
  }
  
  // 根据学院获取专业选项
  const handleCollegeChange = async (value: string) => {
    // 保存原始majorId值
    const originalMajorId = form.majorId
    form.majorId = ''
    
    if (value) {
      const majors = await majorStore.fetchMajorsByCollege(Number(value))
      majorOptions.value = majors.map(major => ({
        value: major.id.toString(),
        label: major.name
      }))
      
      // 如果原始majorId在新的专业列表中存在，则恢复它
      if (originalMajorId && majorOptions.value.some(m => m.value === originalMajorId)) {
        form.majorId = originalMajorId
      }
    } else {
      majorOptions.value = []
    }
  }
  
  // 显示标签输入框
  const showTagInput = () => {
    inputTagVisible.value = true
    nextTick(() => {
      tagInputRef.value?.focus()
    })
  }
  
  // 添加标签
  const handleAddTag = () => {
    if (tagInputValue.value) {
      if (form.tags.length >= 5) {
        ElMessage.warning('最多添加5个标签')
      } else if (form.tags.includes(tagInputValue.value)) {
        ElMessage.warning('标签已存在')
      } else {
        form.tags.push(tagInputValue.value)
      }
    }
    inputTagVisible.value = false
    tagInputValue.value = ''
  }
  
  // 移除标签
  const handleRemoveTag = (tag: string) => {
    form.tags = form.tags.filter(t => t !== tag)
  }
  
  // 切换上传类型
  const toggleUploadType = (type: string) => {
    form.uploadType = type
    if (type === 'file') {
      form.file = null
    } else if (type === 'url') {
      form.fileUrl = ''
    } else if (type === 'keep') {
      form.fileUrl = originalResource.value.fileUrl
    }
  }
  
  // 处理文件变更
  const handleFileChange = (file: any) => {
    form.file = file.raw
  }
  
  // 处理文件超限
  const handleExceed = () => {
    ElMessage.warning('只能上传一个文件')
  }
  
  // 移除文件前的处理
  const beforeRemove = () => {
    form.file = null
    return true
  }
  
  // 上传文件到OSS的方法
  const uploadFileToOSS = async (formData: FormData): Promise<any> => {
    return new Promise((resolve, reject) => {
      // 创建XMLHttpRequest对象以便跟踪上传进度
      const xhr = new XMLHttpRequest()
      
      // 监听上传进度
      xhr.upload.addEventListener('progress', (event) => {
        if (event.lengthComputable) {
          const percentComplete = Math.round((event.loaded / event.total) * 100)
          uploadProgress.value = percentComplete
        }
      }, false)
      
      // 请求完成处理
      xhr.addEventListener('load', () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          try {
            const response = JSON.parse(xhr.responseText)
            resolve(response)
          } catch (e) {
            reject(new Error('解析响应失败'))
          }
        } else {
          reject(new Error('上传失败，状态码：' + xhr.status))
        }
      })
      
      // 请求出错处理
      xhr.addEventListener('error', () => {
        reject(new Error('上传文件出错'))
      })
      
      // 请求中止处理
      xhr.addEventListener('abort', () => {
        reject(new Error('上传被中止'))
      })
      
      // 发送请求
      xhr.open('POST', '/api/resource/upload-file', true)
      
      // 设置请求头
      const token = localStorage.getItem('token')
      if (token) {
        xhr.setRequestHeader('Authorization', `Bearer ${token}`)
      }
      
      xhr.send(formData)
    })
  }
  
  // 格式化进度条百分比显示
  const percentFormat = (percentage: number) => {
    return percentage === 100 ? '完成' : `${percentage}%`
  }
  
  // 格式化文件大小
  const formatFileSize = (bytes: number | undefined) => {
    if (!bytes) return '0 B'
    
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }
  
  // 提交编辑
  const submitEdit = async () => {
    // 表单验证
    const valid = await formRef.value?.validate()
    if (!valid) return
    
    submitting.value = true
    try {
      let finalFileUrl = form.originalFileUrl
      let fileSize = originalResource.value.fileSize
      let fileType = originalResource.value.fileType
      
      // 如果需要上传新文件
      if (form.uploadType === 'file' && form.file) {
        isUploading.value = true
        uploadProgress.value = 0
        
        // 创建FormData对象用于文件上传
        const formData = new FormData()
        formData.append('file', form.file)
        
        // 获取当前用户名
        const username = userStore.userInfo?.username
        if (!username) {
          ElMessage.error('未获取到用户名，请重新登录')
          submitting.value = false
          isUploading.value = false
          return
        }
        
        formData.append('username', username)
        
        // 上传文件到OSS
        const response = await uploadFileToOSS(formData)
        isUploading.value = false
        
        if (response && response.code === 20000) {
          finalFileUrl = response.data
          fileSize = form.file.size
          fileType = getFileType(form.file.name) || 'other'
        } else {
          throw new Error('文件上传失败')
        }
      } else if (form.uploadType === 'url') {
        // 如果是URL上传，直接使用输入的URL
        finalFileUrl = form.fileUrl
        fileSize = 0 // 链接类型，默认大小为0
        fileType = 'link'
      }
      
      // 获取分类、学院和专业的名称
      const categoryOption = categoryOptions.value.find(option => option.value === form.categoryId)
      const categoryName = categoryOption ? categoryOption.label : ''
      
      const collegeOption = collegeOptions.value.find(option => option.value === form.collegeId)
      const collegeName = collegeOption ? collegeOption.label : ''
      
      const majorOption = majorOptions.value.find(option => option.value === form.majorId)
      const majorName = majorOption ? majorOption.label : ''
      
      // 创建更新请求数据
      const updateData = {
        id: resourceId.value,
        title: form.title,
        description: form.description,
        categoryId: Number(form.categoryId),
        categoryName,
        collegeId: Number(form.collegeId),
        collegeName,
        majorId: form.majorId ? Number(form.majorId) : null,
        majorName: majorName || null,
        courseName: form.courseName || null,
        tags: JSON.stringify(form.tags),
        fileUrl: finalFileUrl,
        fileSize,
        fileType
      }
      
      // 调用API更新资源
      await resourceStore.updateResource(updateData)
      
      ElMessage.success('资源更新成功，等待审核')
      router.push('/profile/uploads')
    } catch (error) {
      console.error('更新资源失败', error)
      ElMessage.error('更新资源失败，请稍后重试')
    } finally {
      submitting.value = false
      isUploading.value = false
    }
  }
  
  // 获取文件类型
  const getFileType = (fileName: string): string => {
    if (!fileName) return 'other'
    
    const name = fileName.toLowerCase()
    if (name.endsWith('.pdf')) return 'pdf'
    if (name.endsWith('.doc') || name.endsWith('.docx')) return 'word'
    if (name.endsWith('.xls') || name.endsWith('.xlsx')) return 'excel'
    if (name.endsWith('.ppt') || name.endsWith('.pptx')) return 'ppt'
    if (['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg'].some(ext => name.endsWith(ext))) return 'image'
    if (['.mp3', '.wav', '.ogg', '.flac'].some(ext => name.endsWith(ext))) return 'audio'
    if (['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm'].some(ext => name.endsWith(ext))) return 'video'
    
    return 'other'
  }
  
  // 计算文件类型图标
  const fileType = computed(() => {
    if (!form.file) return ''
    
    const name = form.file.name.toLowerCase()
    if (name.endsWith('.pdf')) return 'pdf'
    if (name.endsWith('.doc') || name.endsWith('.docx')) return 'word'
    if (name.endsWith('.xls') || name.endsWith('.xlsx')) return 'excel'
    if (name.endsWith('.ppt') || name.endsWith('.pptx')) return 'ppt'
    if (['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg'].some(ext => name.endsWith(ext))) return 'image'
    if (['.mp3', '.wav', '.ogg', '.flac'].some(ext => name.endsWith(ext))) return 'audio'
    if (['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm'].some(ext => name.endsWith(ext))) return 'video'
    
    return 'other'
  })
  
  // 获取资源文件类型
  const resourceType = computed(() => {
    const fileType = originalResource.value.fileType
    
    if (fileType === 'pdf') return 'pdf'
    if (fileType === 'doc' || fileType === 'docx') return 'doc'
    if (fileType === 'xls' || fileType === 'xlsx') return 'excel'
    if (fileType === 'ppt' || fileType === 'pptx') return 'ppt'
    if (fileType === 'image') return 'image'
    if (fileType === 'audio') return 'audio'
    if (fileType === 'video') return 'video'
    
    return 'other'
  })
  
  // 获取资源文件名
  const resourceFileName = computed(() => {
    const fileUrl = originalResource.value.fileUrl
    if (!fileUrl) return '未知文件'
    
    // 尝试从URL中提取文件名
    const parts = fileUrl.split('/')
    const fileName = parts[parts.length - 1]
    
    return fileName || '未知文件'
  })
  
  // 组件挂载时初始化数据
  onMounted(() => {
    initData()
  })
  </script>
  
  <style lang="scss" scoped>
  .edit-resource-container {
    max-width: 1000px;
    margin: 0 auto;
    padding: 20px;
  }
  
  .edit-header {
    margin-bottom: 30px;
    text-align: center;
    
    h1 {
      font-size: 28px;
      font-weight: bold;
      margin-bottom: 10px;
      color: var(--el-color-primary);
    }
    
    .edit-tip {
      color: var(--el-color-info);
      font-size: 14px;
    }
  }
  
  .edit-card {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    border-radius: 8px;
  }
  
  .loading-placeholder {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 300px;
    color: var(--el-color-info);
  }
  
  .w-full {
    width: 100%;
  }
  
  .action-buttons {
    margin-top: 40px;
    display: flex;
    justify-content: flex-end;
    gap: 15px;
  }
  
  .tag-item {
    margin-right: 8px;
    margin-bottom: 8px;
  }
  
  .tag-input {
    width: 100px;
    margin-right: 8px;
    vertical-align: bottom;
  }
  
  .tag-button {
    height: 32px;
    padding: 0 10px;
  }
  
  .tag-tip {
    margin-top: 8px;
    font-size: 12px;
    color: var(--el-color-info);
  }
  
  .upload-type-toggle {
    margin-bottom: 20px;
    
    .el-radio-group {
      margin-bottom: 20px;
    }
  }
  
  .current-file-info {
    margin-bottom: 20px;
    padding: 20px;
    background-color: var(--el-fill-color-light);
    border-radius: 8px;
    
    h4 {
      margin-bottom: 15px;
      font-weight: bold;
    }
  }
  
  .upload-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
    
    .file-uploader {
      width: 100%;
    }
  }
  
  .file-info {
    margin-top: 15px;
    display: flex;
    align-items: center;
    width: 100%;
    
    .file-preview {
      margin-right: 20px;
    }
    
    .file-details {
      flex: 1;
      
      .file-name {
        font-weight: bold;
        font-size: 16px;
        margin-bottom: 5px;
        word-break: break-all;
      }
      
      .file-size {
        color: var(--el-color-info);
        font-size: 14px;
        margin-bottom: 5px;
      }
      
      .file-url {
        word-break: break-all;
        font-size: 14px;
        
        a {
          color: var(--el-color-primary);
          text-decoration: none;
          
          &:hover {
            text-decoration: underline;
          }
        }
      }
    }
  }
  
  .url-upload-area {
    margin: 20px 0;
    padding: 20px;
    border: 1px dashed var(--el-border-color);
    border-radius: 8px;
    background-color: var(--el-fill-color-lighter);
    
    .url-input {
      width: 100%;
      margin-bottom: 12px;
      
      :deep(.el-input__inner) {
        height: 50px;
        font-size: 16px;
        padding-left: 15px;
      }
      
      :deep(.el-input__prefix) {
        font-size: 20px;
        margin-right: 5px;
      }
    }
    
    .url-tip {
      margin-top: 15px;
      font-size: 13px;
      color: var(--el-color-info);
      line-height: 1.5;
    }
  }
  
  .upload-progress-container {
    margin: 30px 0;
    padding: 20px;
    background-color: var(--el-fill-color-lighter);
    border-radius: 8px;
    text-align: center;
    
    h4 {
      margin-bottom: 15px;
      font-weight: bold;
      font-size: 16px;
      color: var(--el-color-primary);
    }
    
    :deep(.el-progress-bar__outer) {
      height: 12px !important;
      border-radius: 6px;
    }
    
    :deep(.el-progress-bar__inner) {
      border-radius: 6px;
    }
    
    :deep(.el-progress__text) {
      font-size: 14px !important;
      font-weight: bold;
    }
  }
  
  @media (max-width: 768px) {
    .edit-resource-container {
      padding: 10px;
    }
    
    .file-info {
      flex-direction: column;
      align-items: flex-start;
      
      .file-preview {
        margin-right: 0;
        margin-bottom: 10px;
      }
    }
  }
  </style>