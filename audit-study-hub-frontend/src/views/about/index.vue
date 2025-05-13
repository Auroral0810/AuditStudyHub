<template>
  <div class="about-container">
    <div class="about-header">
      <h1 class="about-title">关于审学汇</h1>
      <p class="about-subtitle">校园资料共享平台</p>
    </div>
    
    <div class="about-content">
      <el-card class="about-section">
        <template #header>
          <div class="section-header">
            <el-icon><InfoFilled /></el-icon>
            <h2>项目介绍</h2>
          </div>
        </template>
        <div class="section-content">
          <p>审学汇是一个专为高校学生设计的资源共享平台，旨在解决学生在学习过程中获取专业资料和教学资源的痛点。</p>
          <p>平台集中了各个专业的学习资料，包括课件PPT、学习笔记、试卷资料、电子书籍、课程视频和实验报告等多种类型的资源，帮助学生高效获取所需的学习材料。</p>
          <p>我们相信知识共享的力量，通过搭建这个平台，希望能够促进校园学习资源的有效流通，提高学习效率，让每一位学生都能便捷地获取优质学习资源。</p>
        </div>
      </el-card>
      
      <el-card class="about-section">
        <template #header>
          <div class="section-header">
            <el-icon><Connection /></el-icon>
            <h2>平台功能</h2>
          </div>
        </template>
        <div class="section-content">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="feature-item">
                <el-icon><Search /></el-icon>
                <h3>资源搜索</h3>
                <p>强大的搜索功能，快速找到所需资料</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <el-icon><Upload /></el-icon>
                <h3>资源分享</h3>
                <p>用户可上传优质学习资料，共享知识</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <el-icon><ChatDotRound /></el-icon>
                <h3>资源需求</h3>
                <p>发布需求，寻找特定学习资料</p>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" class="mt-20">
            <el-col :span="8">
              <div class="feature-item">
                <el-icon><Collection /></el-icon>
                <h3>资源收藏</h3>
                <p>收藏常用资料，便于后续查找使用</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <el-icon><User /></el-icon>
                <h3>个人中心</h3>
                <p>管理个人资料、上传和收藏的资源</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <el-icon><Rank /></el-icon>
                <h3>资源评分</h3>
                <p>评价和筛选优质资源</p>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
      
      <el-card class="about-section">
        <template #header>
          <div class="section-header">
            <el-icon><Document /></el-icon>
            <h2>使用指南</h2>
          </div>
        </template>
        <div class="section-content">
          <el-steps :active="5" finish-status="success" direction="vertical">
            <el-step title="注册账号" description="使用学生ID、邮箱和密码注册平台账号" />
            <el-step title="浏览资源" description="浏览或搜索您感兴趣的资源" />
            <el-step title="下载资源" description="找到所需资源后，点击下载按钮获取" />
            <el-step title="上传分享" description="分享您的优质学习资料，帮助其他同学" />
            <el-step title="发布需求" description="如果找不到所需资源，可以发布需求请求" />
          </el-steps>
        </div>
      </el-card>
      
      <el-card class="about-section">
        <template #header>
          <div class="section-header">
            <el-icon><Service /></el-icon>
            <h2>联系我们</h2>
          </div>
        </template>
        <div class="section-content contact-section">
          <div class="contact-item">
            <el-icon><Message /></el-icon>
            <span>邮箱：15968588744@163.com</span>
          </div>
          <div class="contact-item">
            <el-icon><PhoneFilled /></el-icon>
            <span>电话：(+86) 15968588744</span>
          </div>
          <div class="contact-item">
            <el-icon><Location /></el-icon>
            <span>地址：中国 江苏省南京市浦口区雨山西路 86号</span>
          </div>
          <div class="contact-social">
            <a href="#" class="social-icon"><el-icon><Platform /></el-icon></a>
            <a href="#" class="social-icon"><el-icon><ChatDotRound /></el-icon></a>
            <a href="#" class="social-icon"><el-icon><ChatDotSquare /></el-icon></a>
          </div>
        </div>
      </el-card>
    </div>
  
  </div>
</template>

<script lang="ts" setup>
import { 
  InfoFilled, 
  Connection, 
  Search, 
  Upload, 
  ChatDotRound, 
  Collection, 
  User, 
  Rank,
  Document,
  Service,
  Message,
  PhoneFilled,
  Location,
  Platform,
  ChatDotSquare
} from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.about-container {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.about-header {
  text-align: center;
  margin-bottom: 40px;
  
  .about-title {
    font-size: 36px;
    color: var(--el-color-primary);
    margin-bottom: 10px;
    font-weight: 600;
  }
  
  .about-subtitle {
    font-size: 18px;
    color: var(--el-text-color-secondary);
    margin: 0;
  }
}

.about-section {
  margin-bottom: 30px;
  border-radius: 8px;
  box-shadow: var(--el-box-shadow-light);
  
  .section-header {
    display: flex;
    align-items: center;
    gap: 10px;
    
    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
    
    .el-icon {
      color: var(--el-color-primary);
      font-size: 22px;
    }
  }
  
  .section-content {
    padding: 10px 0;
    color: var(--el-text-color-regular);
    
    p {
      margin: 0 0 12px;
      line-height: 1.6;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.feature-item {
  padding: 20px;
  text-align: center;
  background-color: var(--el-background-color-base);
  border-radius: 8px;
  height: 100%;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: var(--el-box-shadow-light);
    background-color: rgba(var(--el-color-primary-rgb), 0.05);
  }
  
  .el-icon {
    font-size: 32px;
    color: var(--el-color-primary);
    margin-bottom: 15px;
  }
  
  h3 {
    font-size: 18px;
    margin: 0 0 10px;
    color: var(--el-text-color-primary);
  }
  
  p {
    margin: 0;
    color: var(--el-text-color-secondary);
    font-size: 14px;
  }
}

.mt-20 {
  margin-top: 20px;
}

.contact-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
  
  .contact-item {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 15px;
    
    .el-icon {
      color: var(--el-color-primary);
      font-size: 18px;
    }
  }
  
  .contact-social {
    display: flex;
    gap: 20px;
    margin-top: 10px;
    
    .social-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background-color: var(--el-color-primary);
      color: white;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 5px 15px rgba(var(--el-color-primary-rgb), 0.3);
      }
      
      .el-icon {
        font-size: 20px;
      }
    }
  }
}

.about-footer {
  text-align: center;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-lighter);
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

@media (max-width: 768px) {
  .about-header {
    .about-title {
      font-size: 28px;
    }
    
    .about-subtitle {
      font-size: 16px;
    }
  }
  
  .feature-item {
    margin-bottom: 20px;
  }
}
</style> 