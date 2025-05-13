<template>
  <div class="not-found">
    <div class="error-content">
      <h1 class="error-code">404</h1>
      <h2 class="error-title">页面不存在</h2>
      <p class="error-desc">
        抱歉，您访问的页面不存在或已被删除
      </p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
  
  .error-content {
    text-align: center;
    
    .error-code {
      font-size: 120px;
      color: #409eff;
      margin: 0;
      line-height: 1;
    }
    
    .error-title {
      font-size: 30px;
      color: #303133;
      margin: 20px 0;
    }
    
    .error-desc {
      font-size: 16px;
      color: #606266;
      margin-bottom: 30px;
    }
  }
}
</style> 