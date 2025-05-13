<template>
  <div class="app-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <!-- 左侧LOGO和菜单区 -->
        <div class="left-section">
          <!-- LOGO -->
          <div class="logo">
            <router-link to="/">
              <h1>审学汇</h1>
            </router-link>
          </div>
          
          <!-- 导航菜单 -->
          <div class="main-nav">
            <router-link to="/home" class="nav-item" :class="{ 'active': activeIndex === '/home' }">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </router-link>
            <router-link to="/resource" class="nav-item" :class="{ 'active': activeIndex === '/resource' }">
              <el-icon><Files /></el-icon>
              <span>资源中心</span>
            </router-link>
            <router-link to="/resource-request" class="nav-item" :class="{ 'active': activeIndex === '/resource-request' }">
              <el-icon><ChatDotRound /></el-icon>
              <span>资源请求</span>
            </router-link>
            <router-link v-if="token" to="/profile" class="nav-item" :class="{ 'active': activeIndex === '/profile' }">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </router-link>

            
            <!-- 添加南审印象导航项 -->
            <router-link to="/nau-view" class="nav-item" :class="{ 'active': activeIndex === '/nau-view' }">
              <el-icon><Picture /></el-icon>
              <span>南审印象</span>
            </router-link>
            
            <router-link to="/about" class="nav-item" :class="{ 'active': activeIndex === '/about' }">
              <el-icon><InfoFilled /></el-icon>
              <span>关于</span>
            </router-link>
            <!-- 上传资料按钮 -->
            <div v-if="token" class="upload-nav-button" @click="toUploadResource">
              <el-icon><Upload /></el-icon>
              <span>上传资料</span>
            </div>
          </div>
        </div>
        
        <!-- 右侧移动端菜单按钮和用户信息区 -->
        <div class="right-section">
          <!-- 移动端显示的菜单按钮 -->
          <el-button
            class="mobile-menu-button"
            icon="Menu"
            @click="drawerVisible = true"
            text
          ></el-button>
          
          <!-- 用户信息/登录注册按钮 -->
          <div class="user-info">
            <template v-if="token">
              <el-dropdown @command="handleCommand" trigger="click">
                <div class="user-avatar">
                  <el-avatar :size="32" :src="userInfo?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                  <span class="user-name hidden-sm-only">{{ userInfo?.username }}</span>
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">
                      <el-icon><User /></el-icon>个人中心
                    </el-dropdown-item>
                    <el-dropdown-item command="uploads">
                      <el-icon><Upload /></el-icon>我的上传
                    </el-dropdown-item>
                    <el-dropdown-item command="downloads">
                      <el-icon><Download /></el-icon>下载历史
                    </el-dropdown-item>
                    <el-dropdown-item command="collections">
                      <el-icon><Star /></el-icon>我的收藏
                    </el-dropdown-item>
                    <el-dropdown-item command="myrequests">
                      <el-icon><ChatLineRound /></el-icon>我的请求
                    </el-dropdown-item>
                    <el-dropdown-item command="mylikes">
                      <el-icon><StarFilled /></el-icon>我的点赞
                    </el-dropdown-item>
                    <el-dropdown-item divided command="logout">
                      <el-icon><SwitchButton /></el-icon>退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <div class="auth-buttons">
                <el-button type="primary" @click="toLogin" class="login-button">登录</el-button>
                <el-button @click="toRegister" class="register-button">注册</el-button>
              </div>
            </template>
          </div>
        </div>
      </div>
    </el-header>

    <!-- 移动端侧边导航菜单 -->
    <el-drawer
      v-model="drawerVisible"
      title="菜单"
      direction="ltr"
      size="70%"
    >
      <div class="mobile-menu">
        <el-menu
          :default-active="activeIndex"
          @select="handleSelect"
          router
        >
          <el-menu-item index="/home">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/resource">
            <el-icon><Files /></el-icon>
            <span>资源中心</span>
          </el-menu-item>
          <el-menu-item index="/resource-request">
            <el-icon><ChatDotRound /></el-icon>
            <span>资源请求</span>
          </el-menu-item>
          <el-menu-item index="/profile" v-if="token">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>

  
          <!-- 添加南审印象导航项 -->
          <el-menu-item index="/nau-view">
            <el-icon><Picture /></el-icon>
            <span>南审印象</span>
          </el-menu-item>
          <el-menu-item index="/about">
            <el-icon><InfoFilled /></el-icon>
            <span>关于</span>
          </el-menu-item>
          <!-- 上传资料菜单项 -->
          <el-menu-item v-if="token" index="/resource/upload">
            <el-icon><Upload /></el-icon>
            <span>上传资料</span>
          </el-menu-item>
          
          <!-- 未登录时显示登录注册菜单项 -->
          <template v-if="!token">
            <el-menu-item index="/login">
              <el-icon><Key /></el-icon>
              <span>登录</span>
            </el-menu-item>
            <el-menu-item index="/register">
              <el-icon><EditPen /></el-icon>
              <span>注册</span>
            </el-menu-item>
          </template>
          
          <!-- 已登录用户显示的菜单项 -->
          <template v-else>
            <el-sub-menu index="user" v-if="token">
              <template #title>
                <el-icon><UserFilled /></el-icon>
                <span>{{ userInfo?.username }}</span>
              </template>
              <el-menu-item index="/profile/info">
                <el-icon><User /></el-icon>个人信息
              </el-menu-item>
              <el-menu-item index="/profile/uploads">
                <el-icon><Upload /></el-icon>我的上传
              </el-menu-item>
              <el-menu-item index="/profile/downloads">
                <el-icon><Download /></el-icon>下载历史
              </el-menu-item>
              <el-menu-item index="/profile/collections">
                <el-icon><Star /></el-icon>我的收藏
              </el-menu-item>
              <el-menu-item index="/profile/myrequests">
                <el-icon><ChatLineRound /></el-icon>我的请求
              </el-menu-item>
              <el-menu-item index="/profile/mylikes">
                <el-icon><StarFilled /></el-icon>我的点赞
              </el-menu-item>
            </el-sub-menu>
            
            <el-menu-item @click="confirmLogout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </el-menu-item>
          </template>
        </el-menu>
      </div>
    </el-drawer>

    <!-- 主要内容区域 -->
    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>

    <!-- 底部栏 -->
    <el-footer class="footer">
      <div class="footer-content">
        <div class="footer-section">
          <p>© {{ new Date().getFullYear() }} 审学汇 - 版权所有</p>
        </div>
        <div class="footer-section footer-center">
          <p class="icp-info"><a href="https://beian.miit.gov.cn/" target="_blank">浙ICP备2024128766号-1</a></p>
        </div>
        <div class="footer-section footer-links">
          <router-link to="/about" class="footer-link">关于我们</router-link>
          <router-link to="/terms" class="footer-link">使用条款</router-link>
          <router-link to="/privacy" class="footer-link">隐私政策</router-link>
          <router-link to="/contact" class="footer-link">联系我们</router-link>
        </div>
      </div>
    </el-footer>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'
import { 
  House, 
  Files, 
  ChatDotRound, 
  User, 
  UserFilled, 
  Upload, 
  Download, 
  Star, 
  StarFilled,
  Setting, 
  SwitchButton, 
  Key, 
  EditPen,
  InfoFilled,
  ArrowDown,
  Picture,
  ChatLineRound
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 获取用户信息和token
const token = computed(() => userStore.token)
const userInfo = ref(userStore.userInfo)

// 侧边菜单显示控制
const drawerVisible = ref(false)

// 当前活跃的导航菜单项
const activeIndex = ref('')

// 监听路由变化，更新活跃的菜单项
watch(
  () => route.path,
  (path) => {
    // 获取路径的第一部分，同时处理/profile/子路由的情况
    const pathSegments = path.split('/')
    const mainPath = `/${pathSegments[1]}`
    activeIndex.value = mainPath
  },
  { immediate: true }
)

// 添加事件监听函数
const handleUserInfoUpdated = () => {
  console.log('布局组件：用户信息已更新')
  // 强制从store获取最新用户信息
  userInfo.value = JSON.parse(JSON.stringify(userStore.userInfo))
  console.log('布局组件中更新后的用户信息:', userInfo.value)
}

// 组件挂载时添加事件监听
onMounted(() => {
  // 初始加载时从localStorage获取最新信息
  const storedInfo = localStorage.getItem('userInfo')
  if (storedInfo) {
    try {
      userInfo.value = JSON.parse(storedInfo)
      console.log('布局组件从localStorage加载的用户信息:', userInfo.value)
    } catch (error) {
      console.error('解析localStorage中的用户信息失败', error)
    }
  }

  window.addEventListener('user-info-updated', handleUserInfoUpdated)
  window.addEventListener('user-profile-updated', handleUserInfoUpdated)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('user-info-updated', handleUserInfoUpdated)
  window.removeEventListener('user-profile-updated', handleUserInfoUpdated)
})

// 登录页面
const toLogin = () => {
  router.push('/login')
}

// 注册页面
const toRegister = () => {
  router.push('/register')
}

// 上传资料页面
const toUploadResource = () => {
  if (!token.value) {
    ElMessageBox.confirm('您需要登录才能上传资料', '提示', {
      confirmButtonText: '立即登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    }).catch(() => {
      // 用户取消登录
    })
    return
  }
  router.push('/resource/upload')
}

// 处理下拉菜单选项
const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile/info')
      break
    case 'uploads':
      router.push('/profile/uploads')
      break
    case 'downloads':
      router.push('/profile/downloads')
      break
    case 'collections':
      router.push('/profile/collections')
      break
    case 'myrequests':
      router.push('/profile/myrequests')
      break
    case 'mylikes':
      router.push('/profile/mylikes')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      confirmLogout()
      break
  }
}

// 处理移动端菜单选择
const handleSelect = (index: string) => {
  // 如果是路由路径，跳转后关闭抽屉
  if (index.startsWith('/')) {
    drawerVisible.value = false
  }
}

// 确认退出登录
const confirmLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await userStore.logout()
    router.push('/')
    drawerVisible.value = false
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--el-background-color-base);
}

.header {
  padding: 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  background-color: var(--el-color-white);
  position: sticky;
  top: 0;
  z-index: 1000;
  height: auto;
  display: flex;
  flex-direction: column;
  
  .header-content {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 16px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}

.left-section {
  display: flex;
  align-items: center;
}

.logo {
  margin-right: 30px;
  h1 {
    margin: 0;
    font-size: 22px;
    background: linear-gradient(to right, var(--el-color-primary), #36d1dc);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    font-weight: bold;
  }
  a {
    text-decoration: none;
  }
}

.main-nav {
  display: flex;
  align-items: center;
  height: 60px;
  
  .nav-item {
    display: flex;
    align-items: center;
    height: 100%;
    padding: 0 15px;
    color: var(--el-color-text-regular);
    font-size: 14px;
    text-decoration: none;
    transition: all 0.3s;
    position: relative;
    
    .el-icon {
      margin-right: 4px;
      font-size: 16px;
    }
    
    &:hover {
      color: var(--el-color-primary);
    }
    
    &.active {
      color: var(--el-color-primary);
      font-weight: 500;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 15px;
        right: 15px;
        height: 2px;
        background-color: var(--el-color-primary);
        border-radius: 1px;
      }
    }
  }
  
  .upload-nav-button {
    display: flex;
    align-items: center;
    height: 36px;
    padding: 0 16px;
    margin-left: 15px;
    border-radius: 18px;
    cursor: pointer;
    background: linear-gradient(to right, #1890ff, #36d1dc);
    color: white;
    font-size: 14px;
    transition: all 0.3s;
    
    .el-icon {
      margin-right: 4px;
      font-size: 16px;
    }
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.25);
    }
  }
}

.right-section {
  display: flex;
  align-items: center;
}

.mobile-menu-button {
  display: none;
  font-size: 24px;
  margin-right: 10px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: var(--el-border-radius-base);
  transition: background-color 0.3s ease;
  
  &:hover {
    background-color: rgba(0, 0, 0, 0.03);
  }
}

.user-name {
  margin-left: 6px;
  margin-right: 3px;
  font-size: 14px;
  font-weight: 500;
  color: var(--el-color-text-primary);
}

.auth-buttons {
  display: flex;
  gap: 10px;
  
  .el-button {
    padding: 8px 15px;
    border-radius: var(--el-border-radius-base);
    font-weight: 500;
    font-size: 14px;
    
    &.el-button--primary {
      background: linear-gradient(to right, var(--el-color-primary), #36d1dc);
      border: none;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.25);
      }
    }
  }
}

.mobile-menu {
  .el-menu {
    border-right: none;
  }
  
  .el-menu-item, .el-sub-menu__title {
    height: 50px;
    line-height: 50px;
  }
}

.main-content {
  flex: 1;
  padding: 30px 20px;
  
  &> div {
    max-width: 1400px;
    margin: 0 auto;
  }
}

.footer {
  padding: 0;
  background-color: var(--el-color-white);
  border-top: 1px solid var(--el-border-color-lighter);
  
  .footer-content {
    max-width: 1400px;
    margin: 0 auto;
    padding: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .footer-section {
    flex: 1;
    display: flex;
    
    p {
      color: var(--el-color-text-secondary);
      font-size: 14px;
      margin: 0;
    }
    
    &.footer-center {
      justify-content: center;
      text-align: center;
    }
    
    .icp-info {
      font-size: 12px;
      
      a {
        color: var(--el-color-text-secondary);
        
        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }
  
  .footer-links {
    display: flex;
    gap: 15px;
    justify-content: flex-end;
    
    a {
      color: var(--el-color-text-secondary);
      font-size: 14px;
      
      &:hover {
        color: var(--el-color-primary);
      }
    }
  }
}

/* 上传资料按钮区域 */
.upload-button-container {
  width: 100%;
  background: linear-gradient(to right, #f0f9ff, #e6f7ff);
  padding: 8px 0;
  display: flex;
  justify-content: center;
  align-items: center;
  border-bottom: 1px solid #e0f0ff;
  
  .upload-button {
    background: linear-gradient(to right, #1890ff, #36d1dc);
    border: none;
    padding: 8px 20px;
    font-weight: 500;
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.25);
    transition: all 0.3s;
    
    .el-icon {
      margin-right: 5px;
    }
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(24, 144, 255, 0.35);
    }
  }
}

// 路由转场动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式样式 */
@media screen and (max-width: 992px) {
  .main-nav {
    display: none;
  }
  
  .mobile-menu-button {
    display: block;
  }
  
  .hidden-sm-only {
    display: none;
  }
  
  .auth-buttons {
    .register-button {
      display: none;
    }
  }
}

@media screen and (max-width: 768px) {
  .header-content {
    padding: 0 12px;
  }
  
  .logo h1 {
    font-size: 20px;
  }
  
  .auth-buttons {
    .login-button {
      padding: 6px 12px;
    }
  }
  
  .footer {
    .footer-content {
      flex-direction: column;
      gap: 15px;
    }
    
    .footer-section {
      justify-content: center;
      text-align: center;
      
      &.footer-links {
        justify-content: center;
      }
    }
  }
}

@media screen and (max-width: 576px) {
  .logo h1 {
    font-size: 18px;
  }
}
</style> 