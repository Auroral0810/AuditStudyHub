import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo } from '@/types/user'
import { login, logout } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // 从LocalStorage初始化用户信息
  const initUserInfoFromStorage = () => {
    const storedInfo = localStorage.getItem('userInfo')
    if (storedInfo) {
      try {
        userInfo.value = JSON.parse(storedInfo)
      } catch (error) {
        console.error('Failed to parse user info from local storage', error)
        localStorage.removeItem('userInfo')
      }
    }
  }

  // 初始化时从LocalStorage获取用户信息
  initUserInfoFromStorage()

  // 设置token
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 设置用户信息
  const setUserInfo = (info: UserInfo) => {
    console.log('更新Store中的用户信息:', info)
    
    // 确保是一个新的引用，强制视图更新
    userInfo.value = JSON.parse(JSON.stringify(info))
    
    // 更新本地存储
    localStorage.setItem('userInfo', JSON.stringify(info))
    
    // 发送两个事件，确保所有组件都能收到通知
    window.dispatchEvent(new CustomEvent('user-info-updated', { 
      detail: info 
    }))
    
    window.dispatchEvent(new Event('user-profile-updated'))
    
    // 记录一条消息，确认事件已发出
    console.log('用户信息更新事件已触发')
  }

  // 清除用户信息
  const clearUserInfo = () => {
    userInfo.value = null
    localStorage.removeItem('userInfo')
  }

  // 登出
  const logout = () => {
    token.value = ''
    clearUserInfo()
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    clearUserInfo,
    logout
  }
}) 