<template>
  <div class="contact-container page-container">
    <!-- 页面顶部装饰区域 -->
    <div class="page-decoration">
      <div class="decoration-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>
    
    <!-- 返回主页按钮 -->
    <div class="back-button-container">
      <el-button 
        type="primary" 
        icon="ArrowLeft" 
        round 
        class="back-button"
        @click="$router.push('/')"
      >
        返回主页
      </el-button>
    </div>
    
    <div class="contact-header">
      <h1>联系我们</h1>
      <div class="divider"></div>
      <p class="header-subtitle">我们期待听到您的声音，共同改进审学汇平台</p>
    </div>
    
    <div class="contact-content">
      <el-row :gutter="30">
        <el-col :xs="24" :sm="24" :md="14">
          <section class="contact-section contact-form-section">
            <h2>留言咨询</h2>
            <p class="section-desc">如果您有任何问题、建议或反馈，请通过下面的表单与我们联系。我们会尽快回复您。</p>
            
            <el-form 
              ref="formRef" 
              :model="form" 
              :rules="rules" 
              label-position="top"
              class="contact-form"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="姓名" prop="name">
                    <el-input v-model="form.name" placeholder="请输入您的姓名" prefix-icon="User" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="电子邮箱" prop="email">
                    <el-input v-model="form.email" placeholder="请输入您的电子邮箱" prefix-icon="Message" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="主题" prop="subject">
                <el-select v-model="form.subject" placeholder="请选择咨询主题" class="w-100" prefix-icon="InfoFilled">
                  <el-option label="使用咨询" value="usage" />
                  <el-option label="功能建议" value="suggestion" />
                  <el-option label="错误报告" value="bug" />
                  <el-option label="资源请求" value="resource" />
                  <el-option label="其他问题" value="other" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="留言内容" prop="message">
                <el-input 
                  v-model="form.message" 
                  type="textarea" 
                  :rows="6" 
                  placeholder="请详细描述您的问题或建议..." 
                />
              </el-form-item>
              
              <el-form-item class="form-actions">
                <el-button type="primary" @click="submitForm" :loading="submitting" size="large" class="submit-btn">
                  <el-icon><Check /></el-icon> 提交留言
                </el-button>
                <el-button @click="resetForm" size="large" class="reset-btn">
                  <el-icon><Refresh /></el-icon> 重置
                </el-button>
              </el-form-item>
            </el-form>
          </section>
        </el-col>
        
        <el-col :xs="24" :sm="24" :md="10">
          <section class="contact-section contact-info">
            <h2>联系方式</h2>
            <p class="section-desc">您也可以通过以下方式直接联系我们。</p>
            
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <div class="info-content">
                <h3>电子邮箱</h3>
                <p>contact@audit-study-hub.com</p>
              </div>
            </div>
            
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <div class="info-content">
                <h3>地址</h3>
                <p>江苏省南京市浦口区江浦街道雨山西路86号</p>
                <p>南京审计大学</p>
              </div>
            </div>
            
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <div class="info-content">
                <h3>服务时间</h3>
                <p>周一至周五: 9:00 - 18:00</p>
                <p>周六至周日: 10:00 - 16:00</p>
              </div>
            </div>
            
            <div class="social-media">
              <h3>关注我们</h3>
              <div class="social-icons">
                <a href="#" class="social-icon">
                  <el-icon><ChatDotRound /></el-icon>
                </a>
                <a href="#" class="social-icon">
                  <el-icon><Share /></el-icon>
                </a>
                <a href="#" class="social-icon">
                  <el-icon><Link /></el-icon>
                </a>
              </div>
            </div>
          </section>
          
          <section class="contact-section faq-section">
            <h2>常见问题</h2>
            <el-collapse>
              <el-collapse-item title="如何上传资源？" name="1">
                <p>登录后，点击顶部导航栏的"上传资源"按钮，填写资源信息并上传文件即可。</p>
              </el-collapse-item>
              <el-collapse-item title="审学汇收费吗？" name="2">
                <p>审学汇是完全免费的学习资源分享平台，旨在促进校内学习资源的共享。</p>
              </el-collapse-item>
              <el-collapse-item title="忘记密码如何找回？" name="3">
                <p>在登录页面点击"忘记密码"，通过验证您的学校邮箱即可重置密码。</p>
              </el-collapse-item>
              <el-collapse-item title="我可以请求特定资源吗？" name="4">
                <p>可以。在"资源请求"页面发布您需要的特定资源，其他用户看到后可能会提供帮助。</p>
              </el-collapse-item>
            </el-collapse>
          </section>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { Message, Location, Clock, ChatDotRound, Share, Link, Check, Refresh, User, InfoFilled, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  name: '',
  email: '',
  subject: '',
  message: ''
})

const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入您的姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度应在2-20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入您的邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  subject: [
    { required: true, message: '请选择咨询主题', trigger: 'change' }
  ],
  message: [
    { required: true, message: '请输入留言内容', trigger: 'blur' },
    { min: 10, message: '留言内容不能少于10个字符', trigger: 'blur' }
  ]
})

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true
      
      // 这里模拟提交操作
      setTimeout(() => {
        ElMessage({
          type: 'success',
          message: '留言提交成功！我们会尽快与您联系。'
        })
        submitting.value = false
        resetForm()
      }, 1500)
    }
  })
}

const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
}
</script>

<style lang="scss" scoped>
.contact-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px 60px;
  position: relative;
  min-height: 100vh;
  background-color: #f9fafc;
}

// 页面顶部装饰
.page-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 280px;
  background: linear-gradient(135deg, var(--el-color-primary-light-7), var(--el-color-primary-light-3));
  z-index: 0;
  overflow: hidden;
  
  .decoration-shapes {
    position: relative;
    width: 100%;
    height: 100%;
    
    .shape {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      
      &.shape-1 {
        width: 300px;
        height: 300px;
        top: -100px;
        left: 10%;
      }
      
      &.shape-2 {
        width: 200px;
        height: 200px;
        top: 50px;
        right: 15%;
      }
      
      &.shape-3 {
        width: 150px;
        height: 150px;
        bottom: -50px;
        left: 40%;
      }
    }
  }
}

// 返回按钮容器
.back-button-container {
  position: relative;
  z-index: 2;
  margin-bottom: 20px;
  
  .back-button {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    font-weight: 500;
  }
}

.contact-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
  z-index: 2;
  
  h1 {
    font-size: 42px;
    font-weight: 700;
    color: #fff;
    margin-bottom: 16px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .divider {
    width: 80px;
    height: 4px;
    background: #fff;
    margin: 0 auto;
    border-radius: 2px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .header-subtitle {
    color: #fff;
    font-size: 18px;
    margin-top: 16px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  }
}

.contact-content {
  position: relative;
  z-index: 2;
}

.contact-section {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  padding: 30px;
  margin-bottom: 30px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
  }
  
  h2 {
    font-size: 24px;
    font-weight: 600;
    color: var(--el-color-primary);
    margin-bottom: 16px;
    position: relative;
    padding-left: 18px;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 6px;
      height: 22px;
      background: var(--el-color-primary);
      border-radius: 3px;
    }
  }
  
  .section-desc {
    color: var(--el-color-text-secondary);
    margin-bottom: 24px;
    font-size: 16px;
    line-height: 1.6;
  }
}

.contact-form-section {
  .form-actions {
    margin-top: 30px;
    display: flex;
    justify-content: flex-start;
    gap: 15px;
    
    .submit-btn {
      padding: 12px 30px;
      font-weight: 500;
      box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.3);
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(var(--el-color-primary-rgb), 0.4);
      }
    }
    
    .reset-btn {
      padding: 12px 30px;
      font-weight: 500;
    }
  }
  
  .w-100 {
    width: 100%;
  }
}

.contact-info {
  .info-item {
    display: flex;
    margin-bottom: 24px;
    
    .el-icon {
      font-size: 24px;
      color: var(--el-color-primary);
      margin-right: 16px;
      margin-top: 4px;
      background: var(--el-color-primary-light-9);
      padding: 10px;
      border-radius: 50%;
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .info-content {
      h3 {
        font-size: 18px;
        font-weight: 600;
        color: var(--el-color-text-primary);
        margin: 0 0 8px 0;
      }
      
      p {
        margin: 0 0 4px 0;
        color: var(--el-color-text-regular);
        font-size: 15px;
        line-height: 1.5;
      }
    }
  }
  
  .social-media {
    margin-top: 30px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--el-color-text-primary);
      margin-bottom: 16px;
    }
    
    .social-icons {
      display: flex;
      gap: 16px;
      
      .social-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 48px;
        height: 48px;
        border-radius: 50%;
        background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-light-3));
        color: white;
        transition: all 0.3s ease;
        box-shadow: 0 4px 10px rgba(var(--el-color-primary-rgb), 0.3);
        
        &:hover {
          transform: translateY(-3px) rotate(5deg);
          box-shadow: 0 8px 20px rgba(var(--el-color-primary-rgb), 0.4);
        }
        
        .el-icon {
          font-size: 22px;
        }
      }
    }
  }
}

.faq-section {
  .el-collapse {
    --el-collapse-header-height: auto;
    border: none;
    
    .el-collapse-item {
      margin-bottom: 10px;
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 8px;
      overflow: hidden;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .el-collapse-item__header {
        font-size: 16px;
        font-weight: 500;
        padding: 16px;
        background-color: var(--el-color-primary-light-9);
        border-bottom: none;
      }
      
      .el-collapse-item__content {
        padding: 16px;
        
        p {
          margin: 0;
          color: var(--el-color-text-regular);
          font-size: 15px;
          line-height: 1.6;
        }
      }
    }
  }
}

@media screen and (max-width: 992px) {
  .contact-section {
    padding: 24px;
  }
  
  .contact-header h1 {
    font-size: 36px;
  }
}

@media screen and (max-width: 576px) {
  .contact-header h1 {
    font-size: 28px;
  }
  
  .contact-section h2 {
    font-size: 20px;
  }
  
  .contact-section {
    padding: 20px;
  }
  
  .contact-form-section .form-actions {
    flex-direction: column;
    
    .submit-btn, .reset-btn {
      width: 100%;
    }
  }
}
</style> 