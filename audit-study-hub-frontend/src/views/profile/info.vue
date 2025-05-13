<template>
  <div class="user-info">
    <h2 class="page-title">个人资料</h2>
    
    <el-tabs type="border-card" class="info-tabs">
      <el-tab-pane label="基本信息">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="info-form"
        >
          <el-form-item label="头像">
            <div class="avatar-container">
              <el-avatar :size="100" :src="form.avatar || defaultAvatar" class="user-avatar" />
              <el-upload
                class="avatar-uploader"
                action="#"
                :http-request="uploadAvatar"
                :show-file-list="false"
                accept="image/*"
              >
                <el-button size="small" type="primary" class="upload-btn" :icon="Upload">
                  更换头像
                </el-button>
              </el-upload>
            </div>
          </el-form-item>
          
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" class="input-with-prefix">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" class="input-with-prefix" disabled>
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" class="input-with-prefix" disabled>
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="学院" prop="collegeId">
            <el-input v-model="collegeDisplayName" placeholder="学院信息" class="input-with-prefix" disabled>
            </el-input>
          </el-form-item>
          
          <el-form-item label="专业" prop="majorId" >
            <el-input v-model="majorDisplayName" placeholder="专业信息" class="input-with-prefix" disabled>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="saveUserInfo" :loading="loading" class="submit-btn">
              <el-icon><Check /></el-icon> 保存修改
            </el-button>
            <el-button @click="resetForm" class="reset-btn">
              <el-icon><RefreshRight /></el-icon> 重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="修改密码">
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="100px"
          class="info-form"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              placeholder="请输入原密码"
              type="password"
              show-password
              class="input-with-prefix"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              placeholder="请输入新密码"
              type="password"
              show-password
              class="input-with-prefix"
            >
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              placeholder="请再次输入新密码"
              type="password"
              show-password
              class="input-with-prefix"
            >
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="changePassword" :loading="passwordLoading" class="submit-btn">
              <el-icon><Check /></el-icon> 修改密码
            </el-button>
            <el-button @click="resetPasswordForm" class="reset-btn">
              <el-icon><RefreshRight /></el-icon> 重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="账号绑定">
        <div class="binding-container">
          <!-- 邮箱绑定 -->
          <div class="binding-item">
            <div class="binding-info">
              <div class="binding-icon email">
                <el-icon><Message /></el-icon>
              </div>
              <div class="binding-details">
                <h3>邮箱验证</h3>
                <p>{{ form.email ? '已绑定邮箱：' + form.email : '未绑定邮箱' }}</p>
              </div>
            </div>
            <div class="binding-action">
              <el-button 
                type="primary" 
                @click="handleChangeEmail"
              >
                {{ form.email ? '更换邮箱' : '绑定邮箱' }}
              </el-button>
            </div>
          </div>
          
          <el-divider />
          
          <!-- 手机绑定 -->
          <div class="binding-item">
            <div class="binding-info">
              <div class="binding-icon phone">
                <el-icon><Phone /></el-icon>
              </div>
              <div class="binding-details">
                <h3>手机号绑定</h3>
                <p>{{ form.phone ? '已绑定手机：' + maskPhone(form.phone) : '未绑定手机' }}</p>
              </div>
            </div>
            <div class="binding-action">
              <el-button 
                :type="form.phone ? 'primary' : 'primary'" 
                @click="handlePhoneBinding"
              >
                {{ form.phone ? '更换手机号' : '绑定手机号' }}
              </el-button>
            </div>
          </div>
          
          <el-divider />
          
          <!-- 微信账号绑定 -->
          <div class="binding-item">
            <div class="binding-info">
              <div class="binding-icon wechat">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="binding-details">
                <h3>微信账号</h3>
                <p>{{ thirdPartyAccounts.wechat ? '已绑定微信账号' : '未绑定微信账号' }}</p>
              </div>
            </div>
            <div class="binding-action">
              <el-button 
                :type="thirdPartyAccounts.wechat ? 'danger' : 'primary'" 
                @click="handleThirdPartyBinding('wechat')"
              >
                {{ thirdPartyAccounts.wechat ? '解绑' : '绑定' }}
              </el-button>
            </div>
          </div>
          
          <el-divider />
          
          <!-- QQ账号绑定 -->
          <div class="binding-item">
            <div class="binding-info">
              <div class="binding-icon qq">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="binding-details">
                <h3>QQ账号</h3>
                <p>{{ thirdPartyAccounts.qq ? '已绑定QQ账号' : '未绑定QQ账号' }}</p>
              </div>
            </div>
            <div class="binding-action">
              <el-button 
                :type="thirdPartyAccounts.qq ? 'danger' : 'primary'" 
                @click="handleThirdPartyBinding('qq')"
              >
                {{ thirdPartyAccounts.qq ? '解绑' : '绑定' }}
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 验证码弹窗 -->
    <el-dialog
      v-model="verifyDialogVisible"
      :title="verifyDialogTitle"
      width="400px"
      class="verify-dialog"
    >
      <el-form :model="verifyForm" :rules="verifyRules" ref="verifyFormRef" label-width="80px">
        <el-form-item label="新邮箱" prop="email" v-if="verifyType === 'email-change'">
          <el-input v-model="verifyForm.email" placeholder="请输入新邮箱地址"></el-input>
        </el-form-item>
        
        <el-form-item label="新手机号" prop="phone" v-if="verifyType === 'phone-bind' || verifyType === 'phone-change'">
          <el-input v-model="verifyForm.phone" placeholder="请输入手机号码"></el-input>
        </el-form-item>
        
        <el-form-item label="验证码" prop="code">
          <div class="verify-code-input">
            <el-input v-model="verifyForm.code" placeholder="请输入验证码"></el-input>
            <el-button 
              type="primary" 
              :disabled="codeButtonDisabled" 
              @click="sendVerificationCode"
            >{{ codeButtonText }}</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="verifyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitVerification" :loading="verifyLoading">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { Check, RefreshRight, User, Message, Lock, Key, Upload, Connection, Phone } from '@element-plus/icons-vue'
import { updatePassword, uploadAvatar as uploadAvatarApi, updateUserInfo, sendNewEmailVerifyCode, sendPhoneVerifyCode, updateUserEmail } from '@/api/auth'
import { useRouter } from 'vue-router'
import { useCollegeStore } from '@/stores/collegeStore'
import { useMajorStore } from '@/stores/majorStore'
import type { UserInfo } from '@/types/user'

const userStore = useUserStore()
const router = useRouter()
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const verifyFormRef = ref<FormInstance>()
const loading = ref(false)
const passwordLoading = ref(false)
const verifyLoading = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 验证弹窗相关
const verifyDialogVisible = ref(false)
const verifyDialogTitle = ref('')
const verifyType = ref<'email-change' | 'phone-bind' | 'phone-change'>('email-change')
const codeButtonText = ref('获取验证码')
const codeButtonDisabled = ref(false)
const countDown = ref(60)
let timerId: number | null = null

// 表单数据
const form = reactive<UserInfo>({
  id: 0,
  username: '',
  realName: '',
  studentId: '',
  email: '',
  phone: '',
  avatar: '',
  collegeId: 0,
  majorId: 0,
  role: 0,
  status: 0
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证码表单
const verifyForm = reactive({
  email: '',
  phone: '',
  code: ''
})

// 第三方账号绑定状态
const thirdPartyAccounts = reactive({
  wechat: false,
  qq: false
})

// 学院专业显示名称
const collegeDisplayName = ref('')
const majorDisplayName = ref('')

// 使用学院和专业Store
const collegeStore = useCollegeStore()
const majorStore = useMajorStore()

// 学院和专业选项及加载状态
const collegeOptions = ref<{ id: number, name: string }[]>([])
const majorOptions = ref<{ id: number, name: string }[]>([])
const collegeLoading = ref(false)
const majorLoading = ref(false)

// 加载学院列表
const loadColleges = async () => {
  collegeLoading.value = true
  try {
    const colleges = await collegeStore.fetchColleges()
    collegeOptions.value = colleges.map(college => ({
      id: college.id,
      name: college.name
    }))
  } catch (error) {
    console.error('加载学院列表失败:', error)
    ElMessage.error('加载学院列表失败')
  } finally {
    collegeLoading.value = false
  }
}

// 根据学院ID加载专业列表
const loadMajors = async (collegeId: number) => {
  if (!collegeId) {
    majorOptions.value = []
    return
  }
  
  majorLoading.value = true
  try {
    const majors = await majorStore.fetchMajorsByCollege(collegeId)
    majorOptions.value = majors.map(major => ({
      id: major.id,
      name: major.name
    }))
  } catch (error) {
    console.error('加载专业列表失败:', error)
    ElMessage.error('加载专业列表失败')
  } finally {
    majorLoading.value = false
  }
}

// 验证密码一致性
const validatePass = (_: any, value: string, callback: any) => {
  if (value === '') {
    callback()
  } else {
    if (passwordForm.confirmPassword !== '') {
      if (passwordFormRef.value) {
        passwordFormRef.value.validateField('confirmPassword')
      }
    }
    callback()
  }
}

// 验证手机号
const validatePhone = (_: any, value: string, callback: any) => {
  if (value === '') {
    callback()
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号码'))
  } else {
    callback()
  }
}

// 基本信息表单验证规则
const rules = reactive({
  username: [
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ]
})

// 密码表单验证规则
const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度不少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ]
})

// 验证码表单验证规则
const verifyRules = reactive({
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 6, message: '验证码长度在 4 到 6 个字符', trigger: 'blur' }
  ]
})

// 手机号脱敏处理
const maskPhone = (phone: string): string => {
  if (!phone || phone.length < 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 开始倒计时
const startCountDown = () => {
  codeButtonDisabled.value = true
  countDown.value = 60
  codeButtonText.value = `${countDown.value}秒后重试`
  
  timerId = window.setInterval(() => {
    countDown.value--
    codeButtonText.value = `${countDown.value}秒后重试`
    
    if (countDown.value <= 0) {
      clearInterval(timerId!)
      timerId = null
      codeButtonDisabled.value = false
      codeButtonText.value = '获取验证码'
    }
  }, 1000)
}

// 发送验证码
const sendVerificationCode = async () => {
  if (!verifyFormRef.value) return
  
  try {
    if (verifyType.value === 'email-change') {
      // 使用新邮箱发送验证码，只验证email字段
      await verifyFormRef.value.validateField('email')
      
      const username = userStore.userInfo?.username
      if (!username) {
        ElMessage.error('未获取到用户名，请重新登录')
        return
      }
      
      // 调用API发送验证码到新邮箱
      await sendNewEmailVerifyCode({
        username,
        newEmail: verifyForm.email
      })
      
      ElMessage.success('验证码已发送至新邮箱')
      startCountDown()
    } else if (verifyType.value === 'phone-bind' || verifyType.value === 'phone-change') {
      // 使用手机号发送验证码，只验证phone字段
      await verifyFormRef.value.validateField('phone')
      
      const username = userStore.userInfo?.username
      if (!username) {
        ElMessage.error('未获取到用户名，请重新登录')
        return
      }
      
      // 调用API发送验证码到手机
      await sendPhoneVerifyCode({
        username,
        phone: verifyForm.phone
      })
      
      ElMessage.success('验证码已发送至手机')
      startCountDown()
    }
  } catch (error: any) {
    console.error('发送验证码失败', error)
    ElMessage.error(error.response?.data?.message || '发送验证码失败，请稍后重试')
  }
}

// 提交验证
const submitVerification = async () => {
  if (!verifyFormRef.value) return
  
  await verifyFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        verifyLoading.value = true
        
        if (verifyType.value === 'email-change') {
          const userId = userStore.userInfo?.id
          const username = userStore.userInfo?.username
          
          if (!userId || !username) {
            ElMessage.error('未获取到用户信息，请重新登录')
            return
          }
          
          // 使用专门的更新邮箱API
          await updateUserEmail({
            userId: userId,
            username: username,
            newEmail: verifyForm.email,
            verifyCode: verifyForm.code
          })
          
          // 验证成功，更换邮箱
          const oldEmail = form.email
          form.email = verifyForm.email
          
          // 更新用户信息到本地store
          if (userStore.userInfo) {
            const updatedInfo = {
              ...userStore.userInfo,
              email: verifyForm.email
            }
            userStore.setUserInfo(updatedInfo)
          }
          
          ElMessage.success(oldEmail ? '邮箱更换成功' : '邮箱绑定成功')
          
        } else if (verifyType.value === 'phone-bind' || verifyType.value === 'phone-change') {
          // 验证手机验证码 - 这部分可以日后实现
          ElMessage.info('手机验证功能开发中，敬请期待')
        }
        
        // 关闭弹窗并重置表单
        verifyDialogVisible.value = false
        verifyForm.email = ''
        verifyForm.phone = ''
        verifyForm.code = ''
        
      } catch (error: any) {
        console.error('验证失败', error)
        ElMessage.error(error.response?.data?.message || '验证失败，请稍后重试')
      } finally {
        verifyLoading.value = false
      }
    } else {
      console.log('表单验证失败', fields)
    }
  })
}

// 初始化用户信息
const initUserInfo = async () => {
  try {
    const userInfo = userStore.userInfo
    
    if (!userInfo) {
      ElMessage.error('未获取到用户信息，请重新登录')
      return
    }
    
    // 复制用户信息到表单
    Object.assign(form, userInfo)
    
    // 直接使用用户信息中的学院和专业名称
    collegeDisplayName.value = userInfo.collegeName || ''
    majorDisplayName.value = userInfo.majorName || ''
    
    // 设置脱敏手机号显示
    if (form.phone) {
      form.phone = maskPhone(form.phone)
    }
  } catch (error) {
    console.error('初始化用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 上传头像
const uploadAvatar = async (options: any) => {
  const file = options.file
  
  // 验证文件类型和大小
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('上传头像图片只能是图片格式!')
    return
  }
  
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return
  }
  
  try {
    loading.value = true
    
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', file)
    
    // 获取当前用户名
    const username = userStore.userInfo?.username
    if (!username) {
      ElMessage.error('未获取到用户名，请重新登录')
      loading.value = false
      return
    }
    
    formData.append('username', username)
    
    // 调用API上传头像
    const response = await uploadAvatarApi(formData)
    
    
    // 处理响应，从响应中获取头像URL
    if (response && response.data && response.code === 20000) {
      const avatarUrl = response.data
      console.log('获取到的头像URL:', avatarUrl)
      
      // 更新表单数据
      form.avatar = avatarUrl
      
      // 更新用户信息
      if (userStore.userInfo) {
        const updatedUserInfo = {
          ...userStore.userInfo,
          avatar: avatarUrl
        }
        
        // 直接更新store，不再调用saveUserInfo
        userStore.setUserInfo(updatedUserInfo)
        
        // 触发更新事件
        window.dispatchEvent(new Event('user-profile-updated'))
        
        ElMessage.success('头像上传成功')
      }
    } else {
      console.error('上传头像响应异常:', response)
      const errorMessage = (response && response.data && response.data.message) || '上传头像失败'
      throw new Error(errorMessage)
    }
  } catch (error: any) {
    console.error('上传头像失败', error)
    ElMessage.error(error.message || '上传头像失败')
  } finally {
    loading.value = false
  }
}

// 保存用户信息
const saveUserInfo = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        loading.value = true
        
        // 获取用户ID和原始用户信息
        const userId = userStore.userInfo?.id
        const originalUserInfo = userStore.userInfo
        
        if (!userId || !originalUserInfo) {
          ElMessage.error('未获取到用户ID，请重新登录')
          return
        }
        
        // 判断用户名和头像是否有变化
        const usernameChanged = form.username !== originalUserInfo.username
        const avatarChanged = form.avatar !== originalUserInfo.avatar
        
        // 如果用户名和头像都没有变化，直接提示成功
        if (!usernameChanged && !avatarChanged) {
          ElMessage.success('更新成功')
          loading.value = false
          return
        }
        
        // 构建更新数据
        const updateData = {
          id: userId,
          username: form.username,
          avatar: form.avatar
        }
        
        // 调用API更新用户信息
        const response = await updateUserInfo(updateData)
        
        // 获取响应数据体
        const responseBody = response.data
        
        
        // 检查响应状态码是否成功
        if (responseBody && (response.code === 20000 || response.code === 200)) {
          // 获取返回的用户数据
          const userData = responseBody
          
          
          if (!userData) {
            console.error('响应中没有用户数据')
            ElMessage.success('更新成功，但返回的用户数据为空')
            loading.value = false
            return
          }
          
          // 创建更新后的用户信息对象
          const updatedUserInfo: UserInfo = {
            ...originalUserInfo,  // 保留原始信息
            ...userData           // 使用服务器返回的更新信息覆盖
          }
          
          
          // 更新本地存储
          localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))
          
          // 更新本地表单
          Object.assign(form, updatedUserInfo)
          
          // 更新store中的用户信息
          userStore.setUserInfo(updatedUserInfo)
          
          // 直接用新用户名替换当前的localStorage中的数据
          try {
            const storedUserInfo = localStorage.getItem('userInfo')
            if (storedUserInfo) {
              const parsedInfo = JSON.parse(storedUserInfo)
              parsedInfo.username = userData.username
              localStorage.setItem('userInfo', JSON.stringify(parsedInfo))
            }
          } catch (e) {
            console.error('更新localStorage失败', e)
          }
          
          // 手动触发两种事件确保所有组件都能感知到更新
          window.dispatchEvent(new Event('user-profile-updated'))
          window.dispatchEvent(new CustomEvent('user-info-updated', { 
            detail: updatedUserInfo 
          }))
          
          ElMessage.success('个人资料更新成功')
        } else {
          // 如果状态码不是20000，表示更新失败
          throw new Error(responseBody?.message || '更新个人资料失败')
        }
      } catch (error: any) {
        ElMessage.error(error.message || '更新个人资料失败')
      } finally {
        loading.value = false
      }
    } else {
      console.log('表单验证失败', fields)
    }
  })
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        passwordLoading.value = true
        
        const username = userStore.userInfo?.username || '';
        if (!username) {
          ElMessage.error('未获取到用户名，请重新登录');
          return;
        }
        
        // 调用API更新密码
        await updatePassword({
          username,
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        });
        
        // 设置静态成功消息
        ElMessage.success('密码修改成功，请重新登录！');
          
        // 清空密码字段
        passwordForm.oldPassword = '';
        passwordForm.newPassword = '';
        passwordForm.confirmPassword = '';
        //需要清空并重新登录
        userStore.logout();
        router.push('/login');
      } catch (error: any) {
        console.error('密码修改失败', error);
        ElMessage.error(error.response?.data?.message || '密码修改失败，请稍后重试');
      } finally {
        passwordLoading.value = false;
      }
    } else {
      
    }
  });
};

// 更换邮箱处理
const handleChangeEmail = () => {
  verifyType.value = 'email-change'
  verifyDialogTitle.value = form.email ? '更换邮箱' : '绑定邮箱'
  verifyForm.email = '' // 清空之前可能填写的邮箱
  verifyDialogVisible.value = true
}

// 手机号绑定/更换处理
const handlePhoneBinding = () => {
  if (form.phone) {
    verifyType.value = 'phone-change'
    verifyDialogTitle.value = '更换手机号'
  } else {
    verifyType.value = 'phone-bind'
    verifyDialogTitle.value = '绑定手机号'
  }
  verifyDialogVisible.value = true
}

// 处理第三方账号绑定/解绑
const handleThirdPartyBinding = async (type: 'wechat' | 'qq') => {
  try {
    const isBound = thirdPartyAccounts[type]
    
    if (isBound) {
      // 解绑逻辑
      ElMessageBox.confirm(`确定要解绑${type === 'wechat' ? '微信' : 'QQ'}账号吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        // 这里应该有实际的解绑逻辑
        // 模拟解绑成功
        thirdPartyAccounts[type] = false
        localStorage.removeItem(`${type}_bound`)
        ElMessage.success(`${type === 'wechat' ? '微信' : 'QQ'}账号解绑成功`)
      }).catch(() => {})
    } else {
      // 绑定逻辑
      // 这里应该打开第三方授权页面，现在只是模拟
      ElMessage.info(`正在跳转到${type === 'wechat' ? '微信' : 'QQ'}授权页面...`)
      
      // 模拟绑定成功
      setTimeout(() => {
        thirdPartyAccounts[type] = true
        localStorage.setItem(`${type}_bound`, 'true')
        ElMessage.success(`${type === 'wechat' ? '微信' : 'QQ'}账号绑定成功`)
      }, 2000)
    }
  } catch (error) {
    console.error('第三方账号操作失败', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 重置表单
const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  initUserInfo()
}

// 重置密码表单
const resetPasswordForm = () => {
  if (!passwordFormRef.value) return
  passwordFormRef.value.resetFields()
}

// 页面加载时获取用户信息
onMounted(async () => {
  try {
    await initUserInfo()
  } catch (error: any) {
    console.error('初始化用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
  
  // 清理定时器
  return () => {
    if (timerId) {
      clearInterval(timerId)
    }
  }
})
</script>

<style lang="scss" scoped>
.user-info {
  padding: 20px;
  
  .page-title {
    font-size: 22px;
    color: #303133;
    margin-bottom: 20px;
    font-weight: 600;
  }
  
  .info-tabs {
    box-shadow: var(--el-box-shadow-light);
    background-color: #fff;
    
    .info-form {
      max-width: 600px;
      margin: 20px auto;
      
      .avatar-container {
        display: flex;
        align-items: center;
        
        .user-avatar {
          margin-right: 20px;
          box-shadow: var(--el-box-shadow-light);
        }
        
        .avatar-uploader {
          .upload-btn {
            border-radius: 4px;
          }
        }
      }
      
      .input-with-prefix {
        width: 100%;
      }
      
      .w-full {
        width: 100%;
      }
      
      .submit-btn, .reset-btn {
        min-width: 120px;
      }
    }
    
    .binding-container {
      max-width: 600px;
      margin: 0 auto;
      
      .binding-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px 0;
        
        .binding-info {
          display: flex;
          align-items: center;
          
          .binding-icon {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 50px;
            height: 50px;
            border-radius: 50%;
            margin-right: 15px;
            font-size: 24px;
            color: #fff;
            
            &.wechat {
              background-color: #07c160;
            }
            
            &.qq {
              background-color: #12b7f5;
            }
            
            &.email {
              background-color: #ff9800;
            }
            
            &.phone {
              background-color: #f56c6c;
            }
          }
          
          .binding-details {
            h3 {
              margin: 0 0 5px;
              font-size: 18px;
              color: #303133;
            }
            
            p {
              margin: 0;
              font-size: 14px;
              color: #909399;
            }
          }
        }
        
        .binding-action {
          .el-button {
            min-width: 80px;
          }
          
          .el-button + .el-button {
            margin-left: 10px;
          }
        }
      }
    }
  }
}

// 验证码弹窗样式
.verify-dialog {
  .verify-code-input {
    display: flex;
    
    .el-input {
      flex: 1;
      margin-right: 10px;
    }
    
    .el-button {
      width: 120px;
    }
  }
}

// 添加全局样式修复弹窗定位问题
:deep(.el-overlay) {
  overflow: hidden;

  .el-dialog {
    position: relative !important;
    transform: none !important;
    margin: 0 auto !important;
    top: 15vh;
  }
}
</style> 