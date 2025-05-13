import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: '/api', // 使用代理
  timeout: 15000,
  withCredentials: true // 允许跨域请求携带cookie
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    
    // 如果状态码是20000，表示成功
    if (res.code === 20000) {
      return res
    }
    
    // 如果返回的状态码不是20000，说明接口请求有误
    if (res.code !== 20000) {
      
      // 401: 未登录或token过期
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        // router.push('/')
      }
      
      return Promise.reject({ response: { data: res } })
    }
    
    return res
  },
  (error) => {
    console.error('响应错误:', error)
    // 不在这里显示错误消息，由具体业务处理
    return Promise.reject(error)
  }
)

// 封装GET请求
export const get = <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> => {
  return service.get(url, config)
}

// 封装POST请求
export const post = <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
  return service.post(url, data, config)
}

// 封装PUT请求
export const put = <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
  return service.put(url, data, config)
}

// 封装DELETE请求
export const del = <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> => {
  return service.delete(url, config)
}

// 导出默认实例
export default service 