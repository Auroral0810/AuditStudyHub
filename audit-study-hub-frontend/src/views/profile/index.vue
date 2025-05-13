<template>
  <div class="profile-container page-container">
    <el-row :gutter="20">
      <!-- 左侧边栏 -->
      <el-col :span="6">
        <div class="sidebar card-container">
          <div class="user-card">
            <el-avatar :size="80" :src="userInfo?.avatar || defaultAvatar" />
            <h2 class="username">{{ userInfo?.username || '用户名' }}</h2>
            <p class="email">{{ userInfo?.email || 'email@example.com' }}</p>
          </div>
          
          <el-menu
            class="profile-menu"
            :default-active="activeMenu"
            router
          >
            <el-menu-item index="/profile/info">
              <el-icon><User /></el-icon>
              <span>个人资料</span>
            </el-menu-item>
            <el-menu-item index="/profile/uploads">
              <el-icon><Upload /></el-icon>
              <span>我的上传</span>
            </el-menu-item>
            <el-menu-item index="/profile/downloads">
              <el-icon><Download /></el-icon>
              <span>下载历史</span>
            </el-menu-item>
            <el-menu-item index="/profile/collections">
              <el-icon><Star /></el-icon>
              <span>我的收藏</span>
            </el-menu-item>
            <el-menu-item index="/profile/likes">
              <el-icon><Pointer /></el-icon>
              <span>我的点赞</span>
            </el-menu-item>
            <el-menu-item index="/profile/myrequests">
              <el-icon><ChatLineRound /></el-icon>
              <span>我的请求</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-col>
      
      <!-- 右侧内容区 -->
      <el-col :span="18">
        <div class="content card-container">
          <router-view />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { User, Upload, Download, Star, Pointer, ChatLineRound } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 用户信息 - 使用ref而不是computed，便于手动更新
const userInfo = ref(userStore.userInfo)

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 监听用户信息全局更新事件
const handleUserInfoUpdated = (event: CustomEvent) => {
  console.log('个人中心：用户信息已更新', event.detail)
  // 更新本地用户信息引用，确保是新的引用以触发视图更新
  userInfo.value = JSON.parse(JSON.stringify(userStore.userInfo))
  console.log('个人中心更新后的用户信息:', userInfo.value)
}

// 监听个人资料页面特定的更新事件
const handleProfileUpdated = () => {
  console.log('个人资料更新事件触发，刷新侧边栏数据')
  // 强制从store获取最新用户信息
  userInfo.value = JSON.parse(JSON.stringify(userStore.userInfo))
  
  // 尝试从localStorage获取最新信息
  const storedInfo = localStorage.getItem('userInfo')
  if (storedInfo) {
    try {
      const parsedInfo = JSON.parse(storedInfo)
      console.log('localStorage中的用户信息:', parsedInfo)
      userInfo.value = parsedInfo
    } catch (error) {
      console.error('解析localStorage中的用户信息失败', error)
    }
  }
  
  console.log('个人资料页面最终使用的用户信息:', userInfo.value)
}

// 组件挂载时添加事件监听
onMounted(() => {
  window.addEventListener('user-info-updated', handleUserInfoUpdated as EventListener)
  window.addEventListener('user-profile-updated', handleProfileUpdated)
})

// 组件卸载时移除事件监听，防止内存泄漏
onUnmounted(() => {
  window.removeEventListener('user-info-updated', handleUserInfoUpdated as EventListener)
  window.removeEventListener('user-profile-updated', handleProfileUpdated)
})

// 检查登录状态
watch(
  () => userStore.token,
  (val) => {
    if (!val) {
      router.push('/login')
    }
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.profile-container {
  padding: 20px;
}

.sidebar {
  padding: 0;
  
  .user-card {
    padding: 30px 20px;
    text-align: center;
    border-bottom: 1px solid #ebeef5;
    
    .username {
      margin: 15px 0 5px;
      font-size: 18px;
      color: #303133;
    }
    
    .email {
      margin: 0;
      font-size: 14px;
      color: #909399;
    }
  }
  
  .profile-menu {
    border-right: none;
  }
}

.content {
  min-height: 500px;
}
</style> 