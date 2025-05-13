<script setup lang="ts">
</script>

<template>
  <router-view v-slot="{ Component }">
    <keep-alive>
      <component :is="Component" />
    </keep-alive>
  </router-view>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  font-family: 'SF Pro Display', 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
}

#app {
  height: 100%;
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.el-menu--horizontal {
  border-bottom: none !important;
}

.el-menu--horizontal > .el-menu-item.is-active {
  border-bottom-color: var(--el-color-primary) !important;
}

/* 移除el-input的默认边框 */
.el-input__wrapper {
  border: none !important;
}

/* 确保元素不会意外超出容器 */
img, video, iframe {
  max-width: 100%;
}

/* 修复导航菜单在移动设备上的显示 */
@media screen and (max-width: 768px) {
  .el-menu--horizontal {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .el-menu--horizontal > .el-menu-item {
    margin: 0 5px;
  }
}
</style>
