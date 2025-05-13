import request from '@/utils/request'
import { AxiosResponse } from 'axios'

export interface LoginRequest {
  account: string
  password: string
  rememberMe?: boolean
}

export interface RegisterRequest {
  username: string
  password: string
  realName: string
  studentId: string
  phone: string
  email: string
  collegeId: number
  majorId: number
  captchaCode: string
  captchaId: string
}

export interface ResetPasswordRequest {
  username: string
  studentId: string
  realName: string
  contactType: 'phone' | 'email'
  contact: string  // 手机号或邮箱
  newPassword: string
  verifyCode: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface LoginResponse {
  token: string
  user: {
    id: number
    username: string
    realName: string
    studentId: string
    phone: string
    email: string
    collegeId: number
    collegeName: string
    majorId: number
    majorName: string
    role: number
    status: number
    avatar: string
  }
}

export interface CaptchaResponse {
  captchaId: string
  imageBase64: string
}

// 用户登录
export const login = (data: LoginRequest) => {
  return request.post<ApiResponse<LoginResponse>>('/auth/login', data)
}

// 用户注册
export const register = (data: RegisterRequest) => {
  return request.post<ApiResponse<any>>('/auth/register', data)
}

// 获取验证码
export const getCaptcha = () => {
  return request.get<ApiResponse<CaptchaResponse>>('/auth/captcha')
}

// 获取当前用户信息
export const getCurrentUser = () => {
  return request.get<ApiResponse<any>>('/auth/current')
}

// 退出登录
export const logout = async (): Promise<ApiResponse<any>> => {
  try {
    // 尝试调用后端接口清除Redis缓存
    const response = await request.post<ApiResponse<any>>('/auth/logout')
    return response.data
  } catch (error) {
    console.error('后端退出登录请求失败', error)
    // 即使后端请求失败，也返回成功状态，确保前端能正常清除本地存储
    return {
      code: 20000,
      message: '退出登录成功',
      data: null
    }
  }
}

// 通过邮箱找回密码
export interface SendEmailCodeRequest {
  email: string
  username: string
}

export const sendEmailVerifyCode = (data: SendEmailCodeRequest) => {
  return request.post<ApiResponse<any>>('/auth/send-email-code', data)
}

// 通过手机号找回密码
export interface SendPhoneCodeRequest {
  phone: string
  username: string
}

export const sendPhoneVerifyCode = (data: SendPhoneCodeRequest) => {
  return request.post<ApiResponse<any>>('/auth/send-phone-code', data)
}

// 验证邮箱验证码
export interface VerifyEmailCodeRequest {
  email: string
  verifyCode: string
}

export const verifyEmailCode = (data: VerifyEmailCodeRequest) => {
  return request.post<ApiResponse<any>>('/auth/verify-email-code', data)
}

// 重置密码
export const resetPassword = (data: ResetPasswordRequest) => {
  return request.post<ApiResponse<any>>('/auth/reset-password', data)
}

// 验证用户是否存在并有效
export const validateUserInfo = (username: string, studentId: string, realName: string) => {
  return request.post<ApiResponse<any>>('/auth/validate-user', { username, studentId, realName })
}

// 检查用户名是否存在
export const checkUsername = (username: string) => {
  return request.get<ApiResponse<boolean>>('/auth/check-username', { params: { username } })
}

// 检查学号是否存在
export const checkStudentId = (studentId: string) => {
  return request.get<ApiResponse<boolean>>('/auth/check-student-id', { params: { studentId } })
}

// 检查邮箱是否存在
export const checkEmail = (email: string) => {
  return request.get<ApiResponse<boolean>>('/auth/check-email', { params: { email } })
}

// 检查手机号是否存在
export const checkPhone = (phone: string) => {
  return request.get<ApiResponse<boolean>>('/auth/check-phone', { params: { phone } })
}

/**
 * 更新密码
 * @param data 更新密码请求
 * @returns Promise<ApiResponse<any>>
 */
export const updatePassword = (data: {
  username: string;
  oldPassword: string;
  newPassword: string;
}): Promise<AxiosResponse<ApiResponse<any>>> => {
  return request.post<ApiResponse<any>>('/auth/password', data);
};

/**
 * 上传用户头像
 * @param formData 包含头像文件和用户名的表单数据
 * @returns Promise<AxiosResponse<ApiResponse<string>>>
 */
export const uploadAvatar = (formData: FormData): Promise<AxiosResponse<ApiResponse<string>>> => {
  return request.post<ApiResponse<string>>('/auth/uploadAvatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

/**
 * 更新用户基本信息
 * @param data 包含用户ID, 用户名和头像的更新请求
 * @returns Promise<AxiosResponse<ApiResponse<any>>>
 */
export const updateUserInfo = (data: {
  id: number;
  username: string;
  avatar?: string;
}): Promise<AxiosResponse<ApiResponse<any>>> => {
  return request.post<ApiResponse<any>>('/auth/updateUserInfo', data);
};

// 发送新邮箱验证码
export interface SendNewEmailCodeRequest {
  newEmail: string
  username: string
}

export const sendNewEmailVerifyCode = (data: SendNewEmailCodeRequest) => {
  return request.post<ApiResponse<any>>('/auth/send-new-email-code', data)
}

// 更新用户邮箱
export interface UpdateEmailRequest {
  userId: number
  username: string
  newEmail: string
  verifyCode: string
}

export const updateUserEmail = (data: UpdateEmailRequest) => {
  return request.post<ApiResponse<any>>('/auth/update-email', data)
} 