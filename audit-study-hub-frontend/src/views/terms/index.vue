<template>
  <div class="terms-container page-container">
    <!-- 页面顶部装饰区域 -->
    <div class="page-decoration">
      <div class="decoration-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>
    
    <!-- 返回主页按钮 -->
    <div class="back-home-btn">
      <router-link to="/" class="btn-content">
        <el-icon><Back /></el-icon>
        <span>返回主页</span>
      </router-link>
    </div>

    <div class="terms-header">
      <h1>使用条款</h1>
      <div class="divider"></div>
    </div>
    
    <div class="terms-content">
      <el-card class="terms-card">
        <div class="last-updated">
          <el-tag size="small" effect="plain" type="info">最后更新时间：2024年7月1日</el-tag>
        </div>
        
        <section class="terms-section">
          <h2>接受条款</h2>
          <p>欢迎使用审学汇学习资源共享平台（以下简称"本平台"或"审学汇"）。请仔细阅读以下条款和条件，这些条款构成您与审学汇之间具有法律约束力的协议。通过访问或使用本平台，您表示同意遵守这些条款。如果您不同意这些条款，请勿使用本平台。</p>
        </section>
        
        <section class="terms-section">
          <h2>用户账户</h2>
          <p>1. 您必须是南京审计大学的在校学生才能注册本平台账户。</p>
          <p>2. 您应当提供真实、准确、完整的个人信息，并及时更新相关信息。</p>
          <p>3. 您有责任维护账户的安全性，并对使用您账户进行的所有活动负责。</p>
          <p>4. 如发现账户未经授权的使用或任何安全漏洞，请立即通知我们。</p>
        </section>
        
        <section class="terms-section">
          <h2>平台使用</h2>
          <p>1. 本平台仅供个人学习和非商业用途使用。</p>
          <p>2. 您同意不会使用本平台进行以下活动：</p>
          <ul>
            <li>违反任何适用法律、法规或规章制度</li>
            <li>发布、上传或分享侵犯他人知识产权的内容</li>
            <li>分发恶意软件或病毒</li>
            <li>干扰或破坏本平台的服务或连接到本平台的服务器</li>
            <li>收集或存储其他用户的个人信息</li>
            <li>发布、上传或分享色情、暴力、歧视或其他不适当的内容</li>
          </ul>
        </section>
        
        <section class="terms-section">
          <h2>内容和资源</h2>
          <p>1. 用户上传的内容必须符合中国法律法规及南京审计大学校规校纪。</p>
          <p>2. 用户上传的资源应当是出于教育目的、合法获得的学习资料。</p>
          <p>3. 用户不得上传包含商业机密、版权保护材料（未经授权）或其他专有信息的内容。</p>
          <p>4. 本平台不对用户上传的内容进行全面审核，但保留删除任何违反本条款的内容的权利。</p>
          <p>5. 您理解并同意，您上传到本平台的内容可能会被其他用户访问和下载。</p>
        </section>
        
        <section class="terms-section">
          <h2>知识产权</h2>
          <p>1. 本平台及其内容（包括但不限于文本、图像、标志、按钮图标、软件和音频片段）是审学汇或其内容提供商的财产，受版权、商标和其他知识产权法律保护。</p>
          <p>2. 您上传到本平台的内容，您保留对该内容的所有权利。您授予本平台非排他性、免版税、全球性的许可，允许使用、复制、修改、改编、出版和分发该内容，仅用于提供和推广本平台服务。</p>
        </section>
        
        <section class="terms-section">
          <h2>免责声明</h2>
          <p>1. 本平台按"现状"和"可用性"提供，不提供任何明示或暗示的保证。</p>
          <p>2. 本平台不保证服务将不受中断或无错误，也不保证缺陷将被纠正。</p>
          <p>3. 您理解并同意，您使用本平台的风险完全由您自己承担。</p>
        </section>
        
        <section class="terms-section">
          <h2>责任限制</h2>
          <p>在适用法律允许的最大范围内，审学汇及其管理人员、董事、员工或代理人不对任何直接、间接、偶然、特殊、惩罚性或后果性损害负责，无论是基于合同、侵权（包括疏忽）、严格责任还是其他理论，这些损害可能来自于您使用或无法使用本平台或本平台上的任何内容。</p>
        </section>
        
        <section class="terms-section">
          <h2>条款修改</h2>
          <p>审学汇保留随时修改这些条款的权利。我们将通过在本平台上发布更新后的条款来通知您任何更改。您继续使用本平台将视为接受修改后的条款。</p>
        </section>
        
        <section class="terms-section">
          <h2>适用法律</h2>
          <p>这些条款受中华人民共和国法律管辖，并按其解释，不考虑法律冲突原则。</p>
        </section>
        
        <section class="terms-section">
          <h2>联系我们</h2>
          <p>如果您对这些条款有任何问题或意见，请通过以下方式联系我们：<el-link type="primary">contact@audit-study-hub.com</el-link></p>
        </section>
      </el-card>
      
      <!-- 返回顶部按钮 -->
      <el-backtop :right="40" :bottom="40" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { Back } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.terms-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 20px 60px;
  position: relative;
}

// 页面顶部装饰区域
.page-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 300px;
  overflow: hidden;
  pointer-events: none;
  z-index: -1;
  
  .decoration-shapes {
    position: relative;
    width: 100%;
    height: 100%;
    
    .shape {
      position: absolute;
      border-radius: 50%;
      opacity: 0.05;
      
      &-1 {
        width: 300px;
        height: 300px;
        background: var(--el-color-primary);
        top: -150px;
        left: -100px;
      }
      
      &-2 {
        width: 200px;
        height: 200px;
        background: var(--el-color-success);
        top: -50px;
        right: -50px;
      }
      
      &-3 {
        width: 150px;
        height: 150px;
        background: var(--el-color-warning);
        top: 100px;
        right: 10%;
      }
    }
  }
}

// 返回主页按钮
.back-home-btn {
  position: absolute;
  top: 30px;
  left: 20px;
  z-index: 10;
  
  .btn-content {
    display: flex;
    align-items: center;
    padding: 10px 16px;
    background: rgba(var(--el-color-primary-rgb), 0.1);
    border-radius: 20px;
    color: var(--el-color-primary);
    text-decoration: none;
    transition: all 0.3s ease;
    font-weight: 500;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    
    .el-icon {
      margin-right: 6px;
    }
    
    &:hover {
      background: var(--el-color-primary);
      color: white;
      transform: translateX(-5px);
    }
  }
}

.terms-header {
  text-align: center;
  margin-bottom: 40px;
  margin-top: 40px;
  
  h1 {
    font-size: 36px;
    font-weight: 600;
    color: var(--el-color-text-primary);
    margin-bottom: 16px;
    position: relative;
    display: inline-block;
    
    &::after {
      content: '';
      position: absolute;
      width: 100%;
      height: 8px;
      bottom: -4px;
      left: 0;
      background: rgba(var(--el-color-primary-rgb), 0.1);
      z-index: -1;
      border-radius: 4px;
    }
  }
  
  .divider {
    width: 60px;
    height: 4px;
    background: linear-gradient(to right, var(--el-color-primary), #36d1dc);
    margin: 0 auto;
    border-radius: 2px;
  }
}

.terms-content {
  .terms-card {
    border-radius: 10px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
    margin-bottom: 40px;
    
    .last-updated {
      text-align: right;
      margin-bottom: 30px;
    }
    
    .terms-section {
      margin-bottom: 40px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      h2 {
        font-size: 22px;
        font-weight: 600;
        color: var(--el-color-text-primary);
        margin-bottom: 16px;
        position: relative;
        padding-left: 16px;
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 4px;
          height: 22px;
          background: var(--el-color-primary);
          border-radius: 2px;
        }
      }
      
      p {
        font-size: 16px;
        line-height: 1.7;
        color: var(--el-color-text-regular);
        margin-bottom: 12px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
      
      ul {
        padding-left: 20px;
        margin-bottom: 16px;
        
        li {
          font-size: 16px;
          line-height: 1.7;
          color: var(--el-color-text-regular);
          margin-bottom: 8px;
          position: relative;
          
          &::marker {
            color: var(--el-color-primary);
          }
          
          &:last-child {
            margin-bottom: 0;
          }
        }
      }
    }
  }
}

@media screen and (max-width: 768px) {
  .back-home-btn {
    top: 20px;
    left: 20px;
    
    .btn-content {
      padding: 8px 12px;
      font-size: 14px;
    }
  }
  
  .terms-header {
    margin-top: 60px;
    
    h1 {
      font-size: 28px;
    }
  }
}

@media screen and (max-width: 576px) {
  .terms-header h1 {
    font-size: 24px;
  }
  
  .terms-content .terms-card .terms-section h2 {
    font-size: 20px;
  }
}
</style> 