<template>
  <div class="privacy-container page-container">
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
    
    <div class="privacy-header">
      <h1>隐私政策</h1>
      <div class="divider"></div>
    </div>
    
    <div class="privacy-content">
      <div class="content-card">
        <p class="last-updated">最后更新时间：2024年7月1日</p>
        
        <section class="privacy-section">
          <h2>引言</h2>
          <p>审学汇（以下简称"我们"或"本平台"）重视您的隐私。本隐私政策旨在向您说明我们如何收集、使用、披露和保护您的个人信息，以及您对这些信息所拥有的权利。</p>
          <p>通过使用本平台，您同意本隐私政策中描述的数据实践。请花时间仔细阅读本政策，了解我们如何处理您的个人信息。</p>
        </section>
        
        <section class="privacy-section">
          <h2>我们收集的信息</h2>
          <p>我们可能收集以下类型的信息：</p>
          <el-descriptions border direction="vertical" :column="1" class="custom-descriptions">
            <el-descriptions-item label="账户信息">
              当您注册账户时，我们收集您的学号、姓名、电子邮件地址和密码。
            </el-descriptions-item>
            <el-descriptions-item label="用户资料">
              您选择在个人资料中共享的信息，如专业、年级、头像等。
            </el-descriptions-item>
            <el-descriptions-item label="使用数据">
              有关您如何使用本平台的信息，如访问时间、访问页面、搜索内容和下载的资源。
            </el-descriptions-item>
            <el-descriptions-item label="设备信息">
              如IP地址、浏览器类型、操作系统和设备标识符。
            </el-descriptions-item>
            <el-descriptions-item label="用户内容">
              您上传到平台的内容，包括资源文件、描述、评论和消息。
            </el-descriptions-item>
          </el-descriptions>
        </section>
        
        <section class="privacy-section">
          <h2>我们如何使用您的信息</h2>
          <p>我们使用收集的信息用于：</p>
          <ul>
            <li>提供、维护和改进我们的服务</li>
            <li>创建和管理您的账户</li>
            <li>处理上传、下载和共享资源</li>
            <li>回应您的请求、评论和问题</li>
            <li>发送服务通知和更新</li>
            <li>检测、调查和防止欺诈活动和违反我们政策的行为</li>
            <li>分析和改进我们平台的性能和用户体验</li>
            <li>遵守法律义务</li>
          </ul>
        </section>
        
        <section class="privacy-section">
          <h2>信息共享与披露</h2>
          <p>我们不会出售或出租您的个人信息给第三方。但在以下情况下，我们可能会分享您的信息：</p>
          <ul>
            <li><strong>用户同意：</strong> 当您明确同意我们分享信息时。</li>
            <li><strong>平台功能：</strong> 为提供服务必要的信息共享，如用户名和上传的资源在平台上对其他用户可见。</li>
            <li><strong>服务提供商：</strong> 与帮助我们提供服务的第三方服务提供商（如云存储提供商）共享必要信息。</li>
            <li><strong>法律要求：</strong> 当法律、法规、法律程序或政府要求披露时。</li>
            <li><strong>保护权利：</strong> 为保护审学汇、用户或公众的权利、财产或安全所必需的情况。</li>
          </ul>
        </section>
        
        <section class="privacy-section">
          <h2>数据安全</h2>
          <p>我们采取适当的技术和组织措施来保护您的个人信息免受意外丢失、未经授权的访问、使用、更改或披露。这些措施包括数据加密、访问控制和定期安全评估。</p>
          <p>然而，没有任何互联网传输或电子存储方法是100%安全的。因此，虽然我们努力使用商业上可接受的方式来保护您的个人信息，但我们无法保证其绝对安全。</p>
        </section>
        
        <section class="privacy-section">
          <h2>数据保留</h2>
          <p>我们会在满足本隐私政策中概述的目的所需的时间内保留您的个人信息，除非法律要求或允许更长的保留期限。当不再需要您的个人信息时，我们将安全地删除或匿名化这些信息。</p>
        </section>
        
        <section class="privacy-section">
          <h2>您的权利</h2>
          <p>根据适用法律，您可能拥有以下权利：</p>
          <ul>
            <li><strong>访问权：</strong> 访问我们持有的关于您的个人信息。</li>
            <li><strong>更正权：</strong> 要求更正不准确或不完整的个人信息。</li>
            <li><strong>删除权：</strong> 在某些情况下要求删除您的个人信息。</li>
            <li><strong>限制处理权：</strong> 在某些情况下限制我们处理您的个人信息。</li>
            <li><strong>数据可携性权：</strong> 获取您提供给我们的个人信息的副本，以便传输给另一服务提供商。</li>
            <li><strong>反对权：</strong> 基于您的特定情况反对我们处理您的个人信息。</li>
          </ul>
          <p>如需行使这些权利，请通过本政策末尾提供的联系信息与我们联系。</p>
        </section>
        
        <section class="privacy-section">
          <h2>Cookie和类似技术</h2>
          <p>我们使用Cookie和类似技术来收集和存储有关您使用本平台的信息。这些技术帮助我们识别您，记住您的偏好，了解和改善您的用户体验，以及为您提供个性化的服务。</p>
          <p>您可以通过修改浏览器设置来控制Cookie的使用。但请注意，禁用Cookie可能会影响我们平台某些功能的可用性。</p>
        </section>
        
        <section class="privacy-section">
          <h2>未成年人隐私</h2>
          <p>本平台主要面向高校学生，但我们不会故意收集未满16岁儿童的个人信息。如果您是父母或监护人，发现您的孩子向我们提供了个人信息，请联系我们，我们将删除相关信息。</p>
        </section>
        
        <section class="privacy-section">
          <h2>隐私政策更新</h2>
          <p>我们可能会不时更新本隐私政策，以反映我们做法的变化或法律要求的变更。更新后的政策将在本页面上发布，更新日期将一并修改。我们鼓励您定期查看本政策以了解任何变更。</p>
        </section>
        
        <section class="privacy-section">
          <h2>联系我们</h2>
          <p>如果您对本隐私政策有任何问题、意见或请求，请通过以下方式联系我们：</p>
          <p>电子邮件：contact@audit-study-hub.com</p>
        </section>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
// 无需导入额外组件
</script>

<style lang="scss" scoped>
.privacy-container {
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

.privacy-header {
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
}

.privacy-content {
  position: relative;
  z-index: 2;
  
  .content-card {
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    padding: 40px;
  }
  
  .last-updated {
    text-align: right;
    font-style: italic;
    color: var(--el-color-text-secondary);
    margin-bottom: 30px;
  }
  
  .privacy-section {
    margin-bottom: 40px;
    
    h2 {
      font-size: 24px;
      font-weight: 600;
      color: var(--el-color-primary);
      margin-bottom: 20px;
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
    
    p {
      font-size: 16px;
      line-height: 1.8;
      color: var(--el-color-text-regular);
      margin-bottom: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
    
    ul {
      padding-left: 20px;
      margin-bottom: 16px;
      
      li {
        font-size: 16px;
        line-height: 1.8;
        color: var(--el-color-text-regular);
        margin-bottom: 10px;
        position: relative;
        
        strong {
          font-weight: 600;
          color: var(--el-color-primary-dark-1);
        }
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
    
    .custom-descriptions {
      margin: 20px 0;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
      
      :deep(.el-descriptions__label) {
        background-color: var(--el-color-primary-light-9);
        font-weight: 600;
      }
      
      :deep(.el-descriptions__content) {
        line-height: 1.8;
      }
    }
  }
}

// 底部返回按钮
.bottom-actions {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  position: relative;
  z-index: 2;
  
  .back-button {
    padding: 12px 24px;
    font-size: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

@media screen and (max-width: 768px) {
  .privacy-header h1 {
    font-size: 32px;
  }
  
  .privacy-content .content-card {
    padding: 30px 20px;
  }
  
  .privacy-content .privacy-section h2 {
    font-size: 20px;
  }
  
  .page-decoration {
    height: 200px;
  }
}

@media screen and (max-width: 576px) {
  .privacy-header h1 {
    font-size: 28px;
  }
  
  .privacy-content .content-card {
    padding: 20px 15px;
  }
  
  .privacy-content .privacy-section h2 {
    font-size: 18px;
  }
  
  .page-decoration {
    height: 180px;
  }
}
</style> 