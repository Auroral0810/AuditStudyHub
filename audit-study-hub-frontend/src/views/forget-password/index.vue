<template>
  <div class="forget-password-container">
    <div class="forget-password-content">
      <div class="form-container">
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
          <p class="app-subtitle">找回密码</p>
        </div>
        
        <el-steps :active="activeStep" finish-status="success" simple class="steps">
          <el-step title="填写账号信息" />
          <el-step title="验证身份" />
          <el-step title="设置新密码" />
          <el-step title="完成" />
        </el-steps>
        
        <!-- 步骤1: 填写账号信息 -->
        <div v-if="activeStep === 0">
          <el-form
            ref="userInfoFormRef"
            :model="formData"
            :rules="userInfoRules"
            label-position="top"
            class="reset-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="formData.username" 
                placeholder="请输入用户名" 
                :prefix-icon="User"
              />
            </el-form-item>
            
            <el-form-item label="学号" prop="studentId">
              <el-input 
                v-model="formData.studentId" 
                placeholder="请输入学号" 
                :prefix-icon="Ticket"
              />
            </el-form-item>
            
            <el-form-item label="真实姓名" prop="realName">
              <el-input 
                v-model="formData.realName" 
                placeholder="请输入真实姓名" 
                :prefix-icon="UserFilled"
              />
            </el-form-item>
            
            <div class="form-buttons">
              <el-button 
                type="primary" 
                class="action-button" 
                :loading="loading" 
                @click="validateUserInfo"
              >
                下一步
              </el-button>
              <el-button 
                class="action-button" 
                @click="goToLogin"
              >
                返回登录
              </el-button>
            </div>
          </el-form>
        </div>
        
        <!-- 步骤2: 验证身份 -->
        <div v-else-if="activeStep === 1">
          <el-form
            ref="verifyFormRef"
            :model="formData"
            :rules="verifyRules"
            label-position="top"
            class="reset-form"
          >
            <el-form-item label="验证方式">
              <el-radio-group v-model="formData.contactType">
                <el-radio label="phone">通过手机验证</el-radio>
                <el-radio label="email">通过邮箱验证</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item 
              :label="formData.contactType === 'phone' ? '手机号' : '邮箱'" 
              prop="contact"
            >
              <el-input 
                v-model="formData.contact" 
                :placeholder="formData.contactType === 'phone' ? '请输入手机号' : '请输入邮箱'" 
                :prefix-icon="formData.contactType === 'phone' ? Phone : Message"
              />
            </el-form-item>
            
            <el-form-item label="验证码" prop="verifyCode">
              <div class="verify-code-container">
                <el-input 
                  v-model="formData.verifyCode" 
                  placeholder="请输入验证码" 
                  :prefix-icon="Key"
                />
                <el-button 
                  class="send-code-btn" 
                  :disabled="countdown > 0" 
                  @click="sendVerifyCode"
                >
                  {{ countdown > 0 ? `${countdown}秒后重新获取` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            
            <div class="form-buttons">
              <el-button 
                type="primary" 
                class="action-button" 
                :loading="loading" 
                @click="verifyIdentity"
              >
                下一步
              </el-button>
              <el-button 
                class="action-button"
                @click="prevStep"
              >
                上一步
              </el-button>
            </div>
          </el-form>
        </div>
        
        <!-- 步骤3: 设置新密码 -->
        <div v-else-if="activeStep === 2">
          <el-form
            ref="passwordFormRef"
            :model="formData"
            :rules="passwordRules"
            label-position="top"
            class="reset-form"
          >
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="formData.newPassword" 
                type="password" 
                placeholder="请输入新密码" 
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input 
                v-model="formData.confirmPassword" 
                type="password" 
                placeholder="请再次输入新密码" 
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            
            <div class="form-buttons">
              <el-button 
                type="primary" 
                class="action-button" 
                :loading="loading" 
                @click="resetPassword"
              >
                重置密码
              </el-button>
              <el-button 
                class="action-button"
                @click="prevStep"
              >
                上一步
              </el-button>
            </div>
          </el-form>
        </div>
        
        <!-- 步骤4: 完成 -->
        <div v-else-if="activeStep === 3" class="success-container">
          <el-result
            icon="success"
            title="密码重置成功"
            sub-title="您的密码已成功重置，现在可以使用新密码登录了"
          >
            <template #extra>
              <el-button type="primary" @click="goToLogin">立即登录</el-button>
            </template>
          </el-result>
        </div>
      </div>

      <div class="decoration">
        <div class="decoration-content">
          <div class="decoration-image"></div>
          <div class="decoration-info">
            <div class="decoration-icon">
              <el-icon><Key /></el-icon>
            </div>
            <h2>找回密码</h2>
            <p>简单几步，轻松重置</p>
            <div class="decoration-steps">
              <div class="step-item">
                <div class="step-number">1</div>
                <div class="step-text">验证账号信息</div>
              </div>
              <div class="step-item">
                <div class="step-number">2</div>
                <div class="step-text">获取验证码</div>
              </div>
              <div class="step-item">
                <div class="step-number">3</div>
                <div class="step-text">设置新密码</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { 
  User, UserFilled, Lock, Key, Message, Phone, Ticket, ArrowLeft
} from '@element-plus/icons-vue'
import { 
  validateUserInfo as validateUserInfoApi, 
  sendEmailVerifyCode, 
  sendPhoneVerifyCode, 
  resetPassword as resetPasswordApi,
  verifyEmailCode,
  type VerifyEmailCodeRequest
} from '@/api/auth'

const router = useRouter()
const userInfoFormRef = ref<FormInstance>()
const verifyFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const loading = ref(false)
const activeStep = ref(0)
const countdown = ref(0)

// 用户信息验证成功后设置用户数据
const userVerified = ref(false)
const validatedUser = ref<any>(null)

// 表单数据
const formData = reactive({
  username: '',
  studentId: '',
  realName: '',
  contactType: 'phone' as 'phone' | 'email',
  contact: '',
  verifyCode: '',
  newPassword: '',
  confirmPassword: ''
})

// 监听联系方式变化，清空验证码和联系方式
watch(() => formData.contactType, () => {
  formData.contact = ''
  formData.verifyCode = ''
})

// 验证手机号
const validatePhone = (_: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

// 验证邮箱
const validateEmail = (_: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!/^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}

// 验证确认密码
const validateConfirmPassword = (_: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== formData.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 用户信息验证规则
const userInfoRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ]
})

// 验证身份规则
const verifyRules = reactive<FormRules>({
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (formData.contactType === 'phone') {
          validatePhone(_, value, callback)
        } else {
          validateEmail(_, value, callback)
        }
      },
      trigger: 'blur'
    }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
})

// 密码规则
const passwordRules = reactive<FormRules>({
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

// 开始倒计时
const startCountdown = () => {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 验证用户信息
const validateUserInfo = async () => {
  if (!userInfoFormRef.value) return
  
  await userInfoFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        // 调用验证用户信息API
        const response = await validateUserInfoApi(
          formData.username, 
          formData.studentId, 
          formData.realName
        )
        
        // 保存验证成功的用户信息
        userVerified.value = true
        validatedUser.value = response.data
        
        // 如果用户有邮箱，自动填充到邮箱字段
        if (response.data && response.data.email) {
          formData.contactType = 'email'
          formData.contact = response.data.email
        }
        
        ElMessage.success('账号信息验证成功')
        nextStep()
      } catch (error: any) {
        if (error.response && error.response.data) {
          ElMessage.error(error.response.data.message || '账号信息验证失败')
        } else {
          ElMessage.error(error.message || '账号信息验证失败')
        }
        // 验证失败，清除状态
        userVerified.value = false
        validatedUser.value = null
      } finally {
        loading.value = false
      }
    }
  })
}

// 发送验证码
const sendVerifyCode = async () => {
  try {
    // 确保用户已完成第一步验证
    if (!userVerified.value) {
      return ElMessage.warning('请先完成账号信息验证')
    }
    
    if (formData.contactType === 'phone' && !/^1[3-9]\d{9}$/.test(formData.contact)) {
      return ElMessage.warning('请输入正确的手机号')
    }
    
    if (formData.contactType === 'email' && !/^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/.test(formData.contact)) {
      return ElMessage.warning('请输入正确的邮箱地址')
    }
    
    loading.value = true
    
    if (formData.contactType === 'phone') {
      // 发送手机验证码
      await sendPhoneVerifyCode({
        username: formData.username,
        phone: formData.contact
      })
      ElMessage.success('验证码已发送到你的手机')
    } else {
      // 发送邮箱验证码
      await sendEmailVerifyCode({
        username: formData.username,
        email: formData.contact
      })
      ElMessage.success('验证码已发送到你的邮箱')
    }
    
    startCountdown()
  } catch (error: any) {
    console.error('发送验证码失败:', error)
    if (error.response && error.response.data) {
      ElMessage.error(error.response.data.message || '验证码发送失败')
    } else {
      ElMessage.error(error.message || '验证码发送失败')
    }
  } finally {
    loading.value = false
  }
}

// 验证身份
const verifyIdentity = async () => {
  if (!verifyFormRef.value) return
  
  await verifyFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        // 根据联系方式选择验证方法
        if (formData.contactType === 'email') {
          // 调用验证码验证API
          const req = {
            email: formData.contact,
            verifyCode: formData.verifyCode
          }
          
          try {
            const response = await verifyEmailCode(req)
            
            // 打印响应详情以便调试
            console.log('验证码验证响应:', response)
            
            // 直接使用响应中的message
            ElMessage.success(response.message || '验证码验证成功')
            // 验证通过，进入下一步
            nextStep()
          } catch (err: any) {
            console.error('验证码验证请求失败:', err)
            // 处理验证失败情况
            const errMsg = err.response?.data?.message || '验证码验证失败'
            ElMessage.error(errMsg)
          }
        } else if (formData.contactType === 'phone') {
          // 调用手机验证码验证API (如果已实现)
          // 这里可以实现与邮箱验证类似的逻辑
          ElMessage.info('手机验证功能暂未实现，跳过验证')
          nextStep()
        }
      } catch (error: any) {
        console.error('验证失败:', error)
        if (error.response && error.response.data) {
          ElMessage.error(error.response.data.message || '验证失败')
        } else {
          ElMessage.error(error.message || '验证失败')
        }
      } finally {
        loading.value = false
      }
    }
  })
}

// 重置密码
const resetPassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        await resetPasswordApi({
          username: formData.username,
          studentId: formData.studentId,
          realName: formData.realName,
          contactType: formData.contactType,
          contact: formData.contact,
          verifyCode: formData.verifyCode,
          newPassword: formData.newPassword
        })
        
        // 密码重置成功显示静态成功消息
        ElMessage.success('密码重置成功，即将跳转到登录页面')
        
        // 进入完成步骤
        nextStep()
        
        // 设置3秒后自动跳转到登录页
        setTimeout(() => {
          goToLogin()
        }, 3000)
      } catch (error: any) {
        console.error('密码重置失败:', error)
        if (error.response && error.response.data) {
          ElMessage.error(error.response.data.message || '密码重置失败')
        } else {
          ElMessage.error(error.message || '密码重置失败')
        }
      } finally {
        loading.value = false
      }
    }
  })
}

// 下一步
const nextStep = () => {
  activeStep.value++
}

// 上一步
const prevStep = () => {
  activeStep.value--
}

// 去登录页
const goToLogin = () => {
  router.push('/login')
}

// 去首页
const goToHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.forget-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: url('@/assets/background.jpg');
  background-size: cover;
  background-position: center;
}

.forget-password-content {
  display: flex;
  width: 1000px;
  min-height: 600px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.form-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 40px;
}

.back-button-container {
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
  margin-bottom: 10px;
  background: linear-gradient(90deg, #1a73e8, #4481eb);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.app-subtitle {
  font-size: 18px;
  color: #666;
  margin-bottom: 20px;
}

.steps {
  margin-bottom: 30px;
}

.reset-form {
  width: 100%;
  
  :deep(.el-form-item__label) {
    padding-bottom: 5px;
    font-weight: 500;
    color: #333;
  }
}

:deep(.el-input__wrapper) {
  padding: 0 15px;
  height: 45px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  
  &:hover, &:focus {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1), 0 0 0 1px #4481eb inset !important;
  }
}

.verify-code-container {
  display: flex;
  align-items: center;
  
  .el-input {
    flex: 1;
  }
  
  .send-code-btn {
    margin-left: 10px;
    width: 140px;
    height: 45px;
  }
}

.form-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
}

.action-button {
  width: 140px;
  height: 45px;
  font-size: 16px;
  border-radius: 8px;
  transition: all 0.3s;
  
  &:first-child {
    background: linear-gradient(90deg, #1a73e8, #4481eb);
    border: none;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 15px rgba(26, 115, 232, 0.3);
    }
  }
}

.success-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.decoration {
  flex: 1;
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

.decoration-steps {
  margin-top: 40px;
}

.step-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  padding: 15px;
  transition: all 0.3s;
  
  &:hover {
    transform: translateX(5px);
    background: rgba(255, 255, 255, 0.2);
  }
}

.step-number {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  margin-right: 15px;
}

.step-text {
  font-size: 16px;
  font-weight: 500;
}

@media (max-width: 992px) {
  .forget-password-content {
    width: 90%;
    max-width: 600px;
    flex-direction: column;
  }
  
  .decoration {
    display: none;
  }
}

@media (max-width: 576px) {
  .form-container {
    padding: 20px;
  }
  
  .steps {
    display: none;
  }
  
  .verify-code-container {
    flex-direction: column;
    
    .send-code-btn {
      margin-left: 0;
      margin-top: 10px;
      width: 100%;
    }
  }
}
</style> 