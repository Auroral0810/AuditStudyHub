<template>
  <div class="admin-layout">
    <div class="admin-header">
      <h1>校园资料共享平台 - 管理后台</h1>
      <div class="user-info">
        <span>{{ userInfo?.username }}</span>
        <el-dropdown>
          <span class="el-dropdown-link">
            <el-avatar :size="32" :src="userInfo?.avatar || defaultAvatar"></el-avatar>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="backToFront">返回前台</el-dropdown-item>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <div class="admin-container">
      <el-empty description="管理界面正在开发中..."></el-empty>
      <el-button type="primary" @click="backToFront">返回前台</el-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 监听用户信息更新事件
const handleUserInfoUpdated = (event: CustomEvent) => {
  // 事件处理器，实际更新由computed属性自动完成
  console.log('管理后台：用户信息已更新:', event.detail)
}

// 组件挂载时添加事件监听
onMounted(() => {
  window.addEventListener('user-info-updated', handleUserInfoUpdated as EventListener)
})

// 组件卸载时移除事件监听，防止内存泄漏
onUnmounted(() => {
  window.removeEventListener('user-info-updated', handleUserInfoUpdated as EventListener)
})

// 返回前台
const backToFront = () => {
  router.push('/')
}

// 退出登录
const logout = async () => {
  try {
    await userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/')
  } catch (error) {
    console.error('退出登录失败', error)
  }
}
</script>

<style lang="scss" scoped>
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.admin-header {
  height: 60px;
  background-color: #409eff;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  
  h1 {
    font-size: 18px;
    margin: 0;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    
    span {
      margin-right: 10px;
    }
    
    .el-dropdown-link {
      cursor: pointer;
    }
  }
}

.admin-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  .el-button {
    margin-top: 20px;
  }
}
</style> 