<template>
  <div class="upload-resource-container">
    <div class="upload-header">
      <h1>上传资料</h1>
      <p class="upload-tip">上传的资料将由管理员审核后发布，请确保资料内容符合规范</p>
    </div>

    <el-card class="upload-card">
      <div class="upload-steps">
        <el-steps :active="active" finish-status="success">
          <el-step title="填写基本信息"></el-step>
          <el-step title="上传文件"></el-step>
          <el-step title="提交审核"></el-step>
        </el-steps>
      </div>

      <!-- 第一步：基本信息 -->
      <div v-if="active === 0" class="step-content">
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
        </el-form>

        <div class="step-actions">
          <el-button @click="router.back()">取消</el-button>
          <el-button type="primary" @click="nextStep">下一步</el-button>
        </div>
      </div>

      <!-- 第二步：上传文件 -->
      <div v-else-if="active === 1" class="step-content">
        <div class="upload-type-toggle">
          <el-radio-group v-model="form.uploadType" @change="toggleUploadType">
            <el-radio-button label="file">上传文件</el-radio-button>
            <el-radio-button label="url">资料链接</el-radio-button>
          </el-radio-group>
        </div>
        
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
        
        <div v-else class="url-upload-area">
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

        <div class="upload-notice">
          <h3>注意事项</h3>
          <ul>
            <li>上传的资料应为原创或已获授权的内容，不得侵犯他人知识产权</li>
            <li>禁止上传包含违法、色情、暴力等不良内容的资料</li>
            <li>上传的资料将由管理员审核后发布，请耐心等待</li>
            <li>如资料涉及侵权或违规内容，平台有权直接删除</li>
          </ul>
        </div>

        <div class="step-actions">
          <el-button @click="active = 0">上一步</el-button>
          <el-button type="primary" @click="nextStep">下一步</el-button>
        </div>
      </div>

      <!-- 第三步：提交审核 -->
      <div v-else-if="active === 2" class="step-content">
        <div class="review-summary">
          <h3>资料信息确认</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="资料标题">{{ form.title }}</el-descriptions-item>
            <el-descriptions-item label="资料分类">{{ getCategoryName(form.categoryId) }}</el-descriptions-item>
            <el-descriptions-item label="所属学院">{{ getCollegeName(form.collegeId) }}</el-descriptions-item>
            <el-descriptions-item label="所属专业">{{ getMajorName(form.majorId) }}</el-descriptions-item>
            <el-descriptions-item label="相关课程">{{ form.courseName || '无' }}</el-descriptions-item>
            <el-descriptions-item label="资料标签">
              <el-tag v-for="tag in form.tags" :key="tag" class="review-tag">{{ tag }}</el-tag>
              <span v-if="!form.tags.length">无</span>
            </el-descriptions-item>
            <el-descriptions-item label="资料描述">{{ form.description }}</el-descriptions-item>
            <el-descriptions-item label="上传方式">
              {{ form.uploadType === 'file' ? '文件上传' : '资料链接' }}
            </el-descriptions-item>
            <el-descriptions-item v-if="form.uploadType === 'file'" label="上传文件">
              <div class="file-info-small">
                <el-icon v-if="fileType === 'pdf'" color="#f56c6c"><Document /></el-icon>
                <el-icon v-else-if="fileType === 'word'" color="#409eff"><Ticket /></el-icon>
                <el-icon v-else-if="fileType === 'excel'" color="#67c23a"><DataAnalysis /></el-icon>
                <el-icon v-else-if="fileType === 'ppt'" color="#e6a23c"><PictureFilled /></el-icon>
                <el-icon v-else-if="fileType === 'image'" color="#909399"><Picture /></el-icon>
                <el-icon v-else-if="fileType === 'audio'" color="#909399"><Headset /></el-icon>
                <el-icon v-else-if="fileType === 'video'" color="#909399"><VideoCamera /></el-icon>
                <el-icon v-else color="#909399"><Files /></el-icon>
                <span>{{ form.file?.name }} ({{ formatFileSize(form.file?.size) }})</span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item v-else label="资料链接">
              <div class="file-info-small">
                <el-icon color="#409eff"><Link /></el-icon>
                <span>{{ form.fileUrl }}</span>
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="review-agreement">
          <el-checkbox v-model="agreement">
            我已阅读并同意《资料上传规范》，保证所上传资料内容合法、无侵权
          </el-checkbox>
        </div>

        <div v-if="isUploading" class="upload-progress-container">
          <h4>文件上传中...</h4>
          <el-progress :percentage="uploadProgress" :format="percentFormat" />
        </div>

        <div class="step-actions">
          <el-button @click="active = 1" :disabled="isUploading">上一步</el-button>
          <el-button 
            type="primary" 
            :loading="submitting" 
            @click="submitResource" 
            :disabled="!agreement || isUploading"
          >
            提交审核
          </el-button>
        </div>
      </div>

      <!-- 上传成功 -->
      <div v-else class="step-content success-content">
        <div class="success-icon">
          <el-icon><CircleCheckFilled /></el-icon>
        </div>
        <h2>资料上传成功！</h2>
        <p>您的资料已提交，将由管理员审核后发布</p>
        <p class="success-tip">审核通常需要1-3个工作日，请耐心等待</p>
        <div class="success-actions">
          <el-button @click="router.push('/resource')">浏览资源中心</el-button>
          <el-button type="primary" @click="router.push('/profile/uploads')">查看我的上传</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
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
  CircleCheckFilled,
  Link
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const collegeStore = useCollegeStore()
const majorStore = useMajorStore()
const categoryStore = useCategoryStore()
const courseStore = useCourseStore()
const resourceStore = useResourceStore()

// 检查登录状态
if (!userStore.token) {
  ElMessageBox.confirm('您需要登录才能上传资料', '提示', {
    confirmButtonText: '去登录',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.push('/login')
  }).catch(() => {
    router.push('/')
  })
}

// 步骤控制
const active = ref(0)
const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const submitting = ref(false)
const agreement = ref(false)

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
  uploadType: 'file'
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

// 上传文件相关
const isUploading = ref(false)
const uploadProgress = ref(0)
const fileOssUrl = ref('')

// 初始化学院、专业、分类和课程数据
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
  } catch (error) {
    console.error('初始化数据失败:', error)
    ElMessage.error('加载数据失败，请刷新页面重试')
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
  form.majorId = ''
  if (value) {
    const majors = await majorStore.fetchMajorsByCollege(Number(value))
    majorOptions.value = majors.map(major => ({
      value: major.id.toString(),
      label: major.name
    }))
  } else {
    majorOptions.value = []
  }
}

// 下一步
const nextStep = async () => {
  if (active.value === 0) {
    // 验证基本信息
    const valid = await formRef.value?.validate()
    if (!valid) return
  } else if (active.value === 1) {
    // 验证文件上传或链接
    if (form.uploadType === 'file' && !form.file) {
      ElMessage.warning('请上传资料文件')
      return
    } else if (form.uploadType === 'url' && !form.fileUrl) {
      ElMessage.warning('请输入资料链接')
      return
    }
  }
  active.value++
}

// 切换上传类型
const toggleUploadType = (type: 'file' | 'url') => {
  form.uploadType = type
  if (type === 'file') {
    form.fileUrl = ''
  } else {
    form.file = null
    fileOssUrl.value = ''
  }
}

// 提交资源
const submitResource = async () => {
  if (!agreement.value) {
    ElMessage.warning('请阅读并同意资料上传规范')
    return
  }
  
  // 检查是否有文件或URL
  if (form.uploadType === 'file' && !form.file && !fileOssUrl.value) {
    ElMessage.warning('请上传资料文件')
    return
  }
  
  if (form.uploadType === 'url' && !form.fileUrl) {
    ElMessage.warning('请输入资料链接')
    return
  }
  
  // 校验必填字段
  if (!form.title || !form.description || !form.categoryId || !form.collegeId) {
    ElMessage.warning('请填写完整的资料信息')
    return
  }
  
  submitting.value = true
  try {
    let finalFileUrl = ''
    let fileSize = 0
    let fileType = ''
    
    // 获取文件的类型
    if (form.uploadType === 'file' && form.file) {
      // 从文件名中获取文件类型和扩展名
      const fileName = form.file.name.toLowerCase()
      const fileExtMatch = fileName.match(/\.([^.]+)$/)
      const fileExt = fileExtMatch ? fileExtMatch[1] : ''
      
      // 常见文档类型
      if (fileName.endsWith('.pdf')) fileType = 'pdf'
      else if (['.doc', '.docx', '.rtf', '.odt', '.fodt', '.dot', '.dotx'].some(ext => fileName.endsWith(ext))) fileType = 'doc'
      else if (['.xls', '.xlsx', '.xlsm', '.xlsb', '.csv', '.ods', '.fods'].some(ext => fileName.endsWith(ext))) fileType = 'excel'
      else if (['.ppt', '.pptx', '.pptm', '.pps', '.ppsx', '.odp', '.fodp'].some(ext => fileName.endsWith(ext))) fileType = 'ppt'
      else if (['.txt', '.text', '.md', '.markdown', '.log'].some(ext => fileName.endsWith(ext))) fileType = 'text'
      // 图像类型
      else if (['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg', '.tif', '.tiff', '.ico', '.heic', '.heif', '.raw', '.cr2', '.nef', '.psd', '.ai', '.eps'].some(ext => fileName.endsWith(ext))) fileType = 'image'
      // 音频类型
      else if (['.mp3', '.wav', '.ogg', '.flac', '.aac', '.wma', '.m4a', '.opus', '.alac', '.aiff', '.ape'].some(ext => fileName.endsWith(ext))) fileType = 'audio'
      // 视频类型
      else if (['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm', '.mkv', '.m4v', '.3gp', '.mpeg', '.mpg', '.ts', '.mts', '.vob'].some(ext => fileName.endsWith(ext))) fileType = 'video'
      // 压缩文件类型
      else if (['.zip', '.rar', '.7z', '.tar', '.gz', '.bz2', '.xz', '.cab', '.iso', '.tgz', '.tbz2', '.lz', '.lzma', '.lzo', '.z', '.Z'].some(ext => fileName.endsWith(ext))) fileType = 'archive'
      // 编程和代码文件类型
      else if (['.java', '.cpp', '.cc', '.cxx', '.c', '.h', '.hpp', '.js', '.ts', '.jsx', '.tsx', '.py', '.rb', '.php', '.html', '.htm', '.css', '.scss', '.sass', '.less', '.json', '.xml', '.yaml', '.yml', '.go', '.rs', '.swift', '.kt', '.cs', '.vb', '.pl', '.sh', '.bat', '.ps1', '.sql'].some(ext => fileName.endsWith(ext))) fileType = 'code'
      // 字体类型
      else if (['.ttf', '.otf', '.woff', '.woff2', '.eot'].some(ext => fileName.endsWith(ext))) fileType = 'font'
      // 3D模型和CAD类型
      else if (['.obj', '.fbx', '.stl', '.blend', '.dae', '.3ds', '.dwg', '.dxf'].some(ext => fileName.endsWith(ext))) fileType = 'model'
      // 电子书类型
      else if (['.epub', '.mobi', '.azw', '.azw3', '.ibooks'].some(ext => fileName.endsWith(ext))) fileType = 'ebook'
      // 数据库类型
      else if (['.db', '.sqlite', '.dbf', '.mdb', '.accdb'].some(ext => fileName.endsWith(ext))) fileType = 'database'
      // 其他类型 - 保留原始扩展名
      else if (fileExt) fileType = fileExt
      else fileType = 'other'
      
      // 获取文件大小
      fileSize = form.file.size
    } else if (form.uploadType === 'url') {
      // 从URL获取推测文件类型和扩展名
      const url = form.fileUrl.toLowerCase()
      const urlExtMatch = url.match(/\.([^.?#]+)(?:[?#]|$)/)
      const urlExt = urlExtMatch ? urlExtMatch[1] : ''
      
      // 常见文档类型
      if (url.endsWith('.pdf')) fileType = 'pdf'
      else if (['.doc', '.docx', '.rtf', '.odt', '.fodt', '.dot', '.dotx'].some(ext => url.endsWith(ext))) fileType = 'doc'
      else if (['.xls', '.xlsx', '.xlsm', '.xlsb', '.csv', '.ods', '.fods'].some(ext => url.endsWith(ext))) fileType = 'excel'
      else if (['.ppt', '.pptx', '.pptm', '.pps', '.ppsx', '.odp', '.fodp'].some(ext => url.endsWith(ext))) fileType = 'ppt'
      else if (['.txt', '.text', '.md', '.markdown', '.log'].some(ext => url.endsWith(ext))) fileType = 'text'
      // 图像类型
      else if (['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg', '.tif', '.tiff', '.ico', '.heic', '.heif', '.raw', '.cr2', '.nef', '.psd', '.ai', '.eps'].some(ext => url.endsWith(ext))) fileType = 'image'
      // 音频类型
      else if (['.mp3', '.wav', '.ogg', '.flac', '.aac', '.wma', '.m4a', '.opus', '.alac', '.aiff', '.ape'].some(ext => url.endsWith(ext))) fileType = 'audio'
      // 视频类型
      else if (['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm', '.mkv', '.m4v', '.3gp', '.mpeg', '.mpg', '.ts', '.mts', '.vob'].some(ext => url.endsWith(ext))) fileType = 'video'
      // 压缩文件类型
      else if (['.zip', '.rar', '.7z', '.tar', '.gz', '.bz2', '.xz', '.cab', '.iso', '.tgz', '.tbz2', '.lz', '.lzma', '.lzo', '.z', '.Z'].some(ext => url.endsWith(ext))) fileType = 'archive'
      // 编程和代码文件类型
      else if (['.java', '.cpp', '.cc', '.cxx', '.c', '.h', '.hpp', '.js', '.ts', '.jsx', '.tsx', '.py', '.rb', '.php', '.html', '.htm', '.css', '.scss', '.sass', '.less', '.json', '.xml', '.yaml', '.yml', '.go', '.rs', '.swift', '.kt', '.cs', '.vb', '.pl', '.sh', '.bat', '.ps1', '.sql'].some(ext => url.endsWith(ext))) fileType = 'code'
      // 字体类型
      else if (['.ttf', '.otf', '.woff', '.woff2', '.eot'].some(ext => url.endsWith(ext))) fileType = 'font'
      // 3D模型和CAD类型
      else if (['.obj', '.fbx', '.stl', '.blend', '.dae', '.3ds', '.dwg', '.dxf'].some(ext => url.endsWith(ext))) fileType = 'model'
      // 电子书类型
      else if (['.epub', '.mobi', '.azw', '.azw3', '.ibooks'].some(ext => url.endsWith(ext))) fileType = 'ebook'
      // 数据库类型
      else if (['.db', '.sqlite', '.dbf', '.mdb', '.accdb'].some(ext => url.endsWith(ext))) fileType = 'database'
      // 检查是否是代码仓库链接
      else if (url.includes('github.com') || url.includes('gitlab.com') || url.includes('bitbucket.org') || url.includes('gitee.com')) fileType = 'repository'
      // 其他类型 - 保留原始扩展名
      else if (urlExt) fileType = urlExt
      else fileType = 'link'
    }
    
    // 如果是文件上传，先上传到OSS
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
        fileOssUrl.value = finalFileUrl
        console.log('文件上传成功:', finalFileUrl)
      } else {
        throw new Error('文件上传失败')
      }
    } else if (form.uploadType === 'file' && fileOssUrl.value) {
      // 如果已经上传了文件且获取到了OSS URL，直接使用
      finalFileUrl = fileOssUrl.value
    } else if (form.uploadType === 'url') {
      // 如果是URL上传，直接使用输入的URL
      finalFileUrl = form.fileUrl
    }
    
    if (!finalFileUrl) {
      throw new Error('未获取到文件URL，请重试')
    }
    
    // 获取分类、学院和专业的名称
    const categoryOption = categoryOptions.value.find(option => option.value === form.categoryId.toString())
    const categoryName = categoryOption ? categoryOption.label : ''
    
    const collegeOption = collegeOptions.value.find(option => option.value === form.collegeId.toString())
    const collegeName = collegeOption ? collegeOption.label : ''
    
    const majorOption = majorOptions.value.find(option => option.value === form.majorId?.toString())
    const majorName = majorOption ? majorOption.label : ''
    
    // 创建资源请求数据
    const resourceData = new FormData()
    resourceData.append('title', form.title)
    resourceData.append('description', form.description)
    
    // 确保categoryId和collegeId是字符串类型
    const categoryId = form.categoryId ? form.categoryId.toString() : ''
    const collegeId = form.collegeId ? form.collegeId.toString() : ''
    
    if (!categoryId || !collegeId) {
      throw new Error('分类ID或学院ID不能为空')
    }
    
    // 添加用户ID
    const userId = userStore.userInfo?.id
    if (userId) {
      resourceData.append('uploaderId', userId.toString())
    } else {
      throw new Error('用户ID不能为空，请重新登录')
    }
    
    resourceData.append('categoryId', categoryId)
    resourceData.append('categoryName', categoryName)
    resourceData.append('collegeId', collegeId)
    resourceData.append('collegeName', collegeName)
    
    if (form.majorId) {
      resourceData.append('majorId', form.majorId.toString())
      resourceData.append('majorName', majorName)
    }
    
    if (form.courseName) {
      resourceData.append('courseName', form.courseName)
    }
    
    if (form.tags && form.tags.length > 0) {
      resourceData.append('tags', JSON.stringify(form.tags))
    }
    
    // 添加文件URL，不管是OSS上传的还是用户提供的链接
    resourceData.append('fileUrl', finalFileUrl)
    
    // 添加文件大小和类型信息
    if (fileSize > 0) {
      resourceData.append('fileSize', fileSize.toString())
    } else {
      // 链接类型资源，设置默认文件大小为0
      resourceData.append('fileSize', '0')
    }
    
    if (fileType) {
      resourceData.append('fileType', fileType)
    }
    
    console.log('准备上传资源:', {
      title: form.title,
      description: form.description,
      categoryId: categoryId,
      categoryName: categoryName,
      collegeId: collegeId,
      collegeName: collegeName,
      majorId: form.majorId ? form.majorId.toString() : undefined,
      majorName: majorName,
      courseName: form.courseName,
      tags: form.tags && form.tags.length > 0 ? JSON.stringify(form.tags) : undefined,
      fileUrl: finalFileUrl,
      fileSize: fileSize,
      fileType: fileType
    })
    
    // 发送API请求创建资源，使用FormData对象
    await resourceStore.uploadResource(resourceData)
    
    // 提交成功，进入下一步
    active.value++
    ElMessage.success('资料提交成功，等待审核')
  } catch (error) {
    console.error('上传资料失败', error)
    ElMessage.error('上传资料失败，请稍后重试')
  } finally {
    submitting.value = false
    isUploading.value = false
  }
}

// 格式化进度条百分比显示
const percentFormat = (percentage: number) => {
  return percentage === 100 ? '完成' : `${percentage}%`
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

// 获取分类名称
const getCategoryName = (id: string) => {
  const category = categoryOptions.value.find(item => item.value === id)
  return category ? category.label : ''
}

// 获取学院名称
const getCollegeName = (id: string) => {
  const college = collegeOptions.value.find(item => item.value === id)
  return college ? college.label : ''
}

// 获取专业名称
const getMajorName = (id: string) => {
  const major = majorOptions.value.find(item => item.value === id)
  return major ? major.label : ''
}

// 格式化文件大小
const formatFileSize = (bytes: number | undefined) => {
  if (!bytes) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
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

// 组件挂载时初始化数据
onMounted(() => {
  initData()
})
</script>

<style lang="scss" scoped>
.upload-resource-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.upload-header {
  margin-bottom: 30px;
  text-align: center;
  
  h1 {
    font-size: 28px;
    font-weight: bold;
    margin-bottom: 10px;
    color: var(--el-color-primary);
  }
  
  .upload-tip {
    color: var(--el-color-info);
    font-size: 14px;
  }
}

.upload-card {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.upload-steps {
  padding: 30px 0;
  margin-bottom: 20px;
}

.step-content {
  padding: 20px;
  min-height: 400px;
}

.w-full {
  width: 100%;
}

.step-actions {
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
  text-align: center;
  
  .el-radio-group {
    margin-bottom: 20px;
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
  
  .file-info {
    margin-top: 30px;
    display: flex;
    align-items: center;
    background-color: var(--el-fill-color-light);
    padding: 20px;
    border-radius: 8px;
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
      }
    }
  }
}

.url-upload-area {
  margin: 30px 0;
  padding: 30px;
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

.upload-notice {
  margin-top: 40px;
  padding: 15px;
  background-color: rgba(24, 144, 255, 0.1);
  border-radius: 8px;
  
  h3 {
    margin-bottom: 10px;
    color: var(--el-color-primary);
    font-weight: bold;
  }
  
  ul {
    padding-left: 20px;
    
    li {
      margin-bottom: 8px;
      font-size: 14px;
      color: var(--el-color-text-secondary);
    }
  }
}

.review-summary {
  h3 {
    margin-bottom: 20px;
    font-weight: bold;
  }
}

.review-tag {
  margin-right: 5px;
}

.review-agreement {
  margin: 30px 0;
}

.file-info-small {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .el-icon {
    font-size: 18px;
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

.success-content {
  text-align: center;
  padding: 40px 20px;
  
  .success-icon {
    font-size: 80px;
    color: #67c23a;
    margin-bottom: 20px;
  }
  
  h2 {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 15px;
    color: #67c23a;
  }
  
  p {
    color: var(--el-color-text-secondary);
    font-size: 16px;
    margin-bottom: 10px;
  }
  
  .success-tip {
    font-size: 14px;
    color: var(--el-color-info);
    margin-bottom: 30px;
  }
  
  .success-actions {
    display: flex;
    justify-content: center;
    gap: 15px;
  }
}

@media (max-width: 768px) {
  .upload-resource-container {
    padding: 10px;
  }
  
  .upload-steps {
    padding: 20px 0;
  }
  
  .step-content {
    padding: 10px;
  }
  
  .upload-area {
    padding: 10px 0;
  }
  
  .url-upload-area {
    padding: 15px;
    margin: 15px 0;
  }
  
  .upload-progress-container {
    margin: 15px 0;
    padding: 15px;
  }
  
  .file-info {
    flex-direction: column;
    align-items: flex-start;
    
    .file-preview {
      margin-right: 0;
      margin-bottom: 10px;
    }
  }
  
  .success-actions {
    flex-direction: column;
    gap: 10px;
    
    .el-button {
      width: 100%;
    }
  }
}
</style> 