<template>
  <div class="register-container">
    <div class="register-content">
      <div class="register-form-container">
        <div class="back-button-container">
          <el-button 
            class="back-button" 
            type="text" 
            @click="goToHome"
          >
            <el-icon><ArrowLeft /></el-icon> 返回首页
          </el-button>
        </div>
        
        <div class="logo-container">
          <h1 class="app-title">审学汇</h1>
          <p class="app-subtitle">欢迎注册</p>
        </div>
        
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
          label-position="top"
          size="default"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model="registerForm.username"
                  placeholder="请输入用户名"
                  :prefix-icon="User"
                  @blur="checkUsernameExists"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="真实姓名" prop="realName">
                <el-input
                  v-model="registerForm.realName"
                  placeholder="请输入真实姓名"
                  :prefix-icon="UserFilled"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  show-password
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  :prefix-icon="Lock"
                  show-password
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="学号" prop="studentId">
                <el-input
                  v-model="registerForm.studentId"
                  placeholder="请输入学号"
                  :prefix-icon="Ticket"
                  @blur="checkStudentIdExists"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input
                  v-model="registerForm.phone"
                  placeholder="请输入手机号"
                  :prefix-icon="Phone"
                  @blur="checkPhoneExists"
                />
              </el-form-item>
            </el-col>
          </el-row>
          

                    
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="学院" prop="collegeId">
                <el-select
                  v-model="registerForm.collegeId"
                  placeholder="请选择学院"
                  @change="handleCollegeChange"
                  style="width: 100%"
                  :loading="collegeStore.loading"
                >
                  <el-option
                    v-for="item in collegeOptions"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="专业" prop="majorId">
                <el-select
                  v-model="registerForm.majorId"
                  placeholder="请选择专业"
                  :disabled="!registerForm.collegeId"
                  style="width: 100%"
                  :loading="majorLoading"
                >
                  <el-option
                    v-for="item in majorOptions"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input
                  v-model="registerForm.email"
                  placeholder="请输入邮箱"
                  :prefix-icon="Message"
                  @blur="checkEmailExists"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="验证码" prop="captchaCode">
                <div class="captcha-container">
                  <el-input
                    v-model="registerForm.captchaCode"
                    placeholder="请输入验证码"
                    :prefix-icon="Key"
                  />
                  <div class="captcha-image" @click="refreshCaptcha" title="点击刷新验证码">
                    <img v-if="captchaImage && captchaImage !== 'error'" :src="captchaImage" alt="验证码" />
                    <div v-else-if="captchaImage === 'error'" class="captcha-error">
                      <el-icon><Warning /></el-icon>
                      <span>点击重试</span>
                    </div>
                    <div v-else class="captcha-loading">
                      <el-icon class="is-loading"><Loading /></el-icon>
                    </div>
                    <div class="refresh-icon">
                      <el-icon><RefreshRight /></el-icon>
                    </div>
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <el-button
              type="primary"
              class="register-button"
              :loading="loading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-link">
          <span>已有账号？</span>
          <router-link to="/login" class="login-text">返回登录</router-link>
        </div>
      </div>

      <div class="decoration">
        <div class="decoration-content">
          <div class="decoration-image"></div>
          <div class="decoration-info">
            <div class="decoration-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <h2>加入审学汇</h2>
            <p>创建账号以享受全部功能</p>
            <div class="decoration-features">
              <div class="feature-item">
                <el-icon><Document /></el-icon>
                <span>便捷资料查阅</span>
              </div>
              <div class="feature-item">
                <el-icon><ChatDotRound /></el-icon>
                <span>实时交流讨论</span>
              </div>
              <div class="feature-item">
                <el-icon><Trophy /></el-icon>
                <span>学习成果展示</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  User, Lock, UserFilled, Phone, Message, Key, Ticket, 
  Loading, RefreshRight, Document, ChatDotRound, Trophy, ArrowLeft, Warning 
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { register, checkUsername, checkStudentId, checkEmail, checkPhone, getCaptcha } from '@/api/auth'
import type { ApiResponse } from '@/api/auth'
import { useCollegeStore } from '@/stores/collegeStore'
import { useMajorStore } from '@/stores/majorStore'

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const captchaImage = ref('')

// 使用学院和专业Store
const collegeStore = useCollegeStore()
const majorStore = useMajorStore()

// 学院和专业选项
const collegeOptions = ref<{ id: number, name: string }[]>([])
const majorOptions = ref<{ id: number, name: string, collegeId?: number }[]>([])
const majorLoading = ref(false)

// 表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  studentId: '',
  phone: '',
  email: '',
  collegeId: undefined as number | undefined,
  majorId: undefined as number | undefined,
  captchaCode: '',
  captchaId: ''
})

// 表单验证规则
const registerRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名长度不能小于3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  collegeId: [
    { required: true, message: '请选择学院', trigger: 'change' }
  ],
  majorId: [
    { required: true, message: '请选择专业', trigger: 'change' }
  ],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
})

// 加载学院列表
const loadColleges = async () => {
  try {
    // 使用store获取学院列表
    const colleges = await collegeStore.fetchColleges()
    collegeOptions.value = colleges.map(college => ({
      id: college.id,
      name: college.name
    }))
  } catch (error) {
    console.error('加载学院列表失败:', error)
    ElMessage.error('加载学院列表失败，请刷新页面重试')
  }
}

// 根据学院ID加载专业列表
const loadMajors = async (collegeId: number) => {
  if (!collegeId) {
    majorOptions.value = []
    return
  }
  
  majorLoading.value = true
  try {
    const majors = await majorStore.fetchMajorsByCollege(collegeId)
    majorOptions.value = majors.map(major => ({
      id: major.id,
      name: major.name,
      collegeId: major.collegeId
    }))
  } catch (error) {
    console.error('加载专业列表失败:', error)
    ElMessage.error('加载专业列表失败，请刷新页面重试')
  } finally {
    majorLoading.value = false
  }
}

// 处理学院变更
const handleCollegeChange = (collegeId: number) => {
  registerForm.majorId = undefined
  loadMajors(collegeId)
}

// 获取验证码
const fetchCaptcha = async () => {
  captchaImage.value = '' // 清空验证码图片，显示加载状态
  try {
    const res = await getCaptcha()
    if (res && res.data) {
      // 使用后端返回的完整base64图片和验证码ID
      captchaImage.value = res.data.imageBase64
      registerForm.captchaId = res.data.captchaId
    } else {
      captchaImage.value = 'error'
      ElMessage.error('获取验证码失败，请刷新重试')
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    captchaImage.value = 'error'
    ElMessage.error('获取验证码失败，请刷新重试')
  }
}

// 刷新验证码
const refreshCaptcha = () => {
  fetchCaptcha()
}

// 检查用户名是否存在
const checkUsernameExists = async () => {
  if (!registerForm.username) return
  try {
    const exists = await checkUsername(registerForm.username)
    if (exists) {
      ElMessage.warning('该用户名已被注册')
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
  }
}

// 检查学号是否存在
const checkStudentIdExists = async () => {
  if (!registerForm.studentId) return
  try {
    const exists = await checkStudentId(registerForm.studentId)
    if (exists) {
      ElMessage.warning('该学号已被注册')
    }
  } catch (error) {
    console.error('检查学号失败:', error)
  }
}

// 检查邮箱是否存在
const checkEmailExists = async () => {
  if (!registerForm.email) return
  try {
    const exists = await checkEmail(registerForm.email)
    if (exists) {
      ElMessage.warning('该邮箱已被注册')
    }
  } catch (error) {
    console.error('检查邮箱失败:', error)
  }
}

// 检查手机号是否存在
const checkPhoneExists = async () => {
  if (!registerForm.phone) return
  try {
    const exists = await checkPhone(registerForm.phone)
    if (exists) {
      ElMessage.warning('该手机号已被注册')
    }
  } catch (error) {
    console.error('检查手机号失败:', error)
  }
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        await register({
          username: registerForm.username,
          password: registerForm.password,
          realName: registerForm.realName,
          studentId: registerForm.studentId,
          phone: registerForm.phone,
          email: registerForm.email,
          collegeId: registerForm.collegeId!,
          majorId: registerForm.majorId!,
          captchaCode: registerForm.captchaCode,
          captchaId: registerForm.captchaId
        })
        
        // 显示注册成功消息
        ElMessage.success('注册成功')
        
        // 跳转到登录页
        router.push('/login')
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || error.message || '注册失败')
        // 刷新验证码
        refreshCaptcha()
      } finally {
        loading.value = false
      }
    }
  })
}

// 组件挂载时获取验证码和学院列表
onMounted(() => {
  fetchCaptcha()
  loadColleges()
})

const goToHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: url('@/assets/background.jpg');
  background-size: cover;
  background-position: center;
  padding: 30px 0;
}

.register-content {
  display: flex;
  width: 1200px;
  min-height: 600px;
  max-height: 100%;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.register-form-container {
  width: 60%;
  display: flex;
  flex-direction: column;
  padding: 30px 40px;
}

.back-button-container {
  text-align: left;
  margin-bottom: 20px;
}

.back-button {
  font-size: 14px;
  padding: 0;
  color: #4481eb;
  
  &:hover {
    color: #1a73e8;
  }
}

.logo-container {
  text-align: center;
  margin-bottom: 20px;
}

.app-title {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin-bottom: 5px;
  background: linear-gradient(90deg, #1a73e8, #4481eb);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.app-subtitle {
  font-size: 18px;
  color: #666;
  margin-bottom: 5px;
}

.register-form {
  width: 100%;
  
  :deep(.el-form-item__label) {
    padding-bottom: 2px;
    font-weight: 500;
    color: #333;
  }
}

:deep(.el-form-item) {
  margin-bottom: 15px;
}

:deep(.el-input__wrapper) {
  padding: 0 15px;
  height: 42px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  
  &:hover, &:focus {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1), 0 0 0 1px #4481eb inset !important;
  }
}

:deep(.el-select .el-input__wrapper) {
  padding-right: 30px;
}

.captcha-container {
  display: flex;
  align-items: center;
}

.captcha-image {
  width: 130px;
  height: 45px;
  margin-left: 10px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;
  background-color: #fff;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    display: block;
  }
  
  .captcha-loading {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    background-color: #f5f7fa;
    font-size: 18px;
    color: #4481eb;
  }

  .captcha-error {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    background-color: #fff0f0;
    font-size: 14px;
    color: #f56c6c;
    
    .el-icon {
      font-size: 18px;
      margin-bottom: 5px;
    }
    
    span {
      font-size: 12px;
    }
  }

  .refresh-icon {
    position: absolute;
    right: 5px;
    bottom: 5px;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background-color: rgba(255, 255, 255, 0.8);
    display: flex;
    justify-content: center;
    align-items: center;
    color: #4481eb;
    font-size: 12px;
    opacity: 0;
    transition: opacity 0.3s;
  }

  &:hover .refresh-icon {
    opacity: 1;
  }
}

.register-button {
  width: 100%;
  height: 46px;
  font-size: 16px;
  letter-spacing: 1px;
  border-radius: 10px;
  background: linear-gradient(90deg, #1a73e8, #4481eb);
  border: none;
  transition: all 0.3s;
  margin-top: 5px;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 15px rgba(26, 115, 232, 0.3);
  }
}

.login-link {
  text-align: center;
  margin-top: 15px;
  
  span {
    color: #666;
  }
  
  .login-text {
    color: #4481eb;
    font-weight: 600;
    margin-left: 5px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.decoration {
  width: 40%;
  background: linear-gradient(135deg, rgba(68, 129, 235, 0.8), rgba(26, 115, 232, 0.9));
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.decoration-content {
  position: relative;
  z-index: 2;
  padding: 30px;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.decoration-image {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background: url('@/assets/background.jpg') center/cover;
  opacity: 0.1;
  mix-blend-mode: overlay;
}

.decoration-info {
  text-align: center;
  color: white;
  max-width: 300px;
}

.decoration-icon {
  font-size: 48px;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  
  .el-icon {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    padding: 15px;
  }
}

.decoration-features {
  margin-top: 40px;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
  
  &:hover {
    transform: translateX(5px);
    background: rgba(255, 255, 255, 0.2);
  }
  
  .el-icon {
    font-size: 18px;
    margin-right: 10px;
  }
  
  span {
    font-size: 14px;
    font-weight: 500;
  }
}

@media (max-width: 1200px) {
  .register-content {
    width: 90%;
  }
}

@media (max-width: 992px) {
  .register-content {
    flex-direction: column;
    width: 90%;
    max-width: 600px;
  }
  
  .register-form-container {
    width: 100%;
  }
  
  .decoration {
    width: 100%;
    padding: 30px 0;
  }
  
  .decoration-features {
    display: flex;
    gap: 10px;
    
    .feature-item {
      flex: 1;
      flex-direction: column;
      text-align: center;
      
      .el-icon {
        margin-right: 0;
        margin-bottom: 5px;
      }
    }
  }
}

@media (max-width: 576px) {
  .register-container {
    padding: 15px;
  }
  
  .register-form-container {
    padding: 20px;
  }
  
  .decoration-features {
    flex-direction: column;
  }
}
</style> 