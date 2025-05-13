<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-form-container">
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
          <p class="app-subtitle">欢迎回来</p>
        </div>
        
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
        >
          <el-form-item prop="account">
            <el-input
              v-model="loginForm.account"
              placeholder="用户名/邮箱/学号"
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
            <el-button type="text" class="forget-pwd" @click="handleForgetPassword">忘记密码？</el-button>
          </div>
          
          <el-form-item>
            <el-button
              type="primary"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-link">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-text">立即注册</router-link>
        </div>
      </div>

      <div class="decoration">
        <div class="decoration-content">
          <div class="decoration-image"></div>
          <div class="decoration-info">
            <div class="decoration-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <h2>审学汇学习平台</h2>
            <p>连接知识，分享智慧</p>
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Document, ChatDotRound, Trophy, ArrowLeft } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import type { ApiResponse, LoginResponse } from '@/api/auth'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: '',
  rememberMe: false
})

const loginRules = reactive<FormRules>({
  account: [
    { required: true, message: '请输入用户名/邮箱/学号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        // 发送登录请求
        const response = await login(loginForm)
        
        // 显示成功消息
        ElMessage({
          message: response.message || '登录成功',
          type: 'success'
        })
        
        // 保存token和用户信息
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('userInfo', JSON.stringify(response.data.user))

        // 更新用户store
        userStore.setToken(response.data.token)
        userStore.setUserInfo(response.data.user)
        
        // 登录成功后跳转到首页
        router.push('/')
      } catch (error: any) {
        // 显示错误消息
        ElMessage({
          message: error.response?.data?.message || error.message || '登录失败',
          type: 'error'
        })
      } finally {
        loading.value = false
      }
    }
  })
}

const handleForgetPassword = () => {
  router.push('/forget-password')
}

const goToHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: url('@/assets/background.jpg');
  background-size: cover;
  background-position: center;
}

.login-content {
  display: flex;
  width: 900px;
  height: 600px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.login-form-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 50px;
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
  margin-bottom: 40px;
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

.login-form {
  width: 100%;
}

.el-form-item {
  margin-bottom: 25px;
  
  :deep(.el-input__wrapper) {
    padding: 0 15px;
    height: 50px;
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    transition: all 0.3s;
    
    &:hover, &:focus {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1), 0 0 0 1px #4481eb inset !important;
    }
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.forget-pwd {
  font-size: 14px;
  padding: 0;
  color: #4481eb;
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  letter-spacing: 1px;
  border-radius: 10px;
  background: linear-gradient(90deg, #1a73e8, #4481eb);
  border: none;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 15px rgba(26, 115, 232, 0.3);
  }
}

.register-link {
  text-align: center;
  margin-top: 20px;
  
  span {
    color: #666;
  }
  
  .register-text {
    color: #4481eb;
    font-weight: 600;
    margin-left: 5px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.decoration {
  flex: 1;
  position: relative;
  background: linear-gradient(135deg, rgba(68, 129, 235, 0.8), rgba(26, 115, 232, 0.9));
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

@media (max-width: 992px) {
  .login-content {
    width: 90%;
    max-width: 600px;
    height: auto;
    flex-direction: column;
  }
  
  .decoration {
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
  .login-form-container {
    padding: 40px 25px;
  }
  
  .decoration-features {
    flex-direction: column;
  }
}
</style> 