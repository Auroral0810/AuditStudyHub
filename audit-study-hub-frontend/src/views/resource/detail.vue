<template>
  <div class="resource-detail-container">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="resource">
      <div class="resource-header">
        <div class="back-button">
          <el-button icon="ArrowLeft" @click="router.push('/resource')">返回列表</el-button>
        </div>
        <h1 class="resource-title">{{ resource.title }}</h1>
        <div class="resource-meta">
          <div class="meta-item">
            <span class="label">分类：</span>
            <el-tag size="small">{{ resource.categoryName }}</el-tag>
          </div>
          <div class="meta-item">
            <span class="label">课程：</span>
            <el-tag size="small" type="success" v-if="resource.courseName">{{ resource.courseName }}</el-tag>
            <span v-else>-</span>
          </div>
          <div class="meta-item">
            <span class="label">上传时间：</span>
            <span>{{ formatDate(getResourceDate(resource)) }}</span>
          </div>
          <div class="meta-item">
            <span class="label">文件大小：</span>
            <span>{{ formatFileSize(resource.fileSize) }}</span>
          </div>
          <div class="meta-item">
            <span class="label">文件类型：</span>
            <span>{{ resource.fileType }}</span>
          </div>
          <div class="meta-item">
            <span class="label">上传者：</span>
            <span v-html="resource.uploaderName"></span>
          </div>
        </div>
      </div>
      
      <div class="resource-content">
        <div class="resource-info">
          <div class="info-card">
            <div class="card-header">
              <h3>资源简介</h3>
            </div>
            <div class="card-body">
              <p class="resource-description">{{ resource.description }}</p>
              
              <div class="resource-tags" v-if="resource.tags">
                <div class="tag-title">标签：</div>
                <div class="tag-list">
                  <el-tag
                    v-for="tag in getResourceTags(resource)"
                    :key="tag"
                    size="small"
                    class="tag-item"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
              
              <div class="resource-college-major">
                <div class="college-major-item">
                  <span class="label">学院：</span>
                  <span>{{ resource.uploaderCollege || '未知' }}</span>
                </div>
                <div class="college-major-item">
                  <span class="label">专业：</span>
                  <span>{{ resource.uploaderMajor || '未知' }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="preview-card" v-if="canPreview">
            <div class="card-header">
              <h3>预览</h3>
              <div class="preview-actions">
                <span @click="toggleFullscreen" class="action-btn">
                  <el-icon><FullScreen v-if="!isFullscreen" /><Minus v-else /></el-icon>
                  {{ isFullscreen ? '退出全屏' : '全屏预览' }}
                </span>
              </div>
            </div>
            
            <div class="card-body preview-container" :class="{ 'fullscreen': isFullscreen }">
              <div v-if="isPDF" class="pdf-preview-wrapper">
                <div v-if="pdfLoading" class="pdf-loading">
                  <el-skeleton :rows="10" animated />
                  <p>PDF预览加载中，请稍候...</p>
                </div>
                
                <template v-if="!useDirectPreview">
                  <div v-if="!pdfError" class="pdf-container" :style="{ height: isFullscreen ? '100vh' : '600px' }">
                    <object
                      v-if="resource.fileUrl"
                      :data="getProxiedPdfUrl(resource.fileUrl)"
                      type="application/pdf"
                      width="100%"
                      height="100%"
                      @load="handlePdfRendered"
                      @error="handlePdfError"
                    >
                      <p>您的浏览器无法显示PDF，请<a :href="getProxiedPdfUrl(resource.fileUrl)" target="_blank">点击此处下载</a></p>
                    </object>
                  </div>
                  
                  <div v-if="pdfError" class="pdf-error">
                    <el-empty description="通过代理预览PDF失败">
                      <div class="error-actions">
                        <el-button type="primary" @click="useDirectPreview = true; pdfError = false; pdfLoading = true;">
                          尝试直接预览
                        </el-button>
                        <el-button @click="handleDownload">
                          下载查看
                        </el-button>
                      </div>
                    </el-empty>
                  </div>
                </template>
                
                <template v-else>
                  <div v-if="!pdfError" class="pdf-container" :style="{ height: isFullscreen ? '100vh' : '600px' }">
                    <PdfEmbed
                      v-if="resource.fileUrl"
                      :source="resource.fileUrl"
                      :page="1"
                      class="pdf-embed"
                      @rendered="handlePdfRendered"
                      @error="handlePdfError"
                    />
                  </div>
                  
                  <div v-if="pdfError" class="pdf-error">
                    <el-empty description="PDF预览加载失败">
                      <el-button type="primary" @click="handleDownload">
                        下载查看
                      </el-button>
                    </el-empty>
                  </div>
                </template>
              </div>
              
              <div v-else-if="isImage" class="image-preview">
                <img :src="resource.fileUrl || ''" alt="预览图片" />
              </div>
              
              <div v-else-if="isDocument" class="document-preview">
                <div v-if="isLoadingDocument" class="preview-loading">
                  <el-skeleton :rows="10" animated />
                  <p>文档加载中，请稍候...</p>
                </div>
                <iframe 
                  v-if="resource.fileUrl"
                  :src="getOfficePreviewUrl(resource.fileUrl)" 
                  width="100%" 
                  height="100%" 
                  frameborder="0"
                  @load="isLoadingDocument = false"
                  @error="documentError = true; isLoadingDocument = false"
                ></iframe>
                <div v-if="documentError" class="preview-error">
                  <el-empty description="文档预览加载失败">
                    <el-button type="primary" @click="handleDownload">
                      下载查看
                    </el-button>
                  </el-empty>
                </div>
              </div>
              
              <div v-else-if="isExcel" class="excel-preview">
                <div v-if="isLoadingExcel" class="preview-loading">
                  <el-skeleton :rows="10" animated />
                  <p>Excel文件加载中，请稍候...</p>
                </div>
                <iframe 
                  v-if="resource.fileUrl"
                  :src="getOfficePreviewUrl(resource.fileUrl)" 
                  width="100%" 
                  height="100%" 
                  frameborder="0"
                  @load="isLoadingExcel = false"
                  @error="excelError = true; isLoadingExcel = false"
                ></iframe>
                <div v-if="excelError" class="preview-error">
                  <el-empty description="Excel预览加载失败">
                    <el-button type="primary" @click="handleDownload">
                      下载查看
                    </el-button>
                  </el-empty>
                </div>
              </div>
              
              <div v-else-if="isCode" class="code-preview">
                <div v-if="isLoadingContent" class="preview-loading">
                  <el-skeleton :rows="10" animated />
                  <p>代码加载中，请稍候...</p>
                </div>
                <div v-else-if="contentLoadError" class="preview-error">
                  <el-empty description="代码预览加载失败">
                    <el-button type="primary" @click="handleDownload">
                      下载查看
                    </el-button>
                  </el-empty>
                </div>
                <div v-else class="code-container">
                  <div class="code-controls">
                    <div class="code-info">
                      <span class="code-language">{{ getCodeLanguage() }}</span>
                      <span v-if="isCodeTooWide" class="scroll-indicator">
                        <el-icon><DArrowRight /></el-icon> 内容较宽，请左右滚动查看
                      </span>
                    </div>
                    <el-button size="small" @click="handleDownload">
                      下载源文件
                    </el-button>
                  </div>
                  <div class="code-content-wrapper">
                    <pre ref="codeContentRef" class="code-content" @scroll="checkHorizontalScroll">
                      <code ref="codeElement" :class="getCodeClass()">{{ fileContent }}</code>
                    </pre>
                  </div>
                </div>
              </div>
              
              <div v-else-if="isAudio" class="audio-preview">
                <div class="audio-player-container">
                  <div class="audio-title">{{ resource.title }}</div>
                  <audio 
                    controls 
                    class="audio-player"
                    :src="getProxiedFileUrl(resource.fileUrl, 'audio')"
                    @error="audioError = true"
                  >
                    您的浏览器不支持音频播放
                  </audio>
                  <div v-if="audioError" class="preview-error">
                    <el-empty description="音频加载失败">
                      <el-button type="primary" @click="handleDownload">
                        下载收听
                      </el-button>
                    </el-empty>
                  </div>
                </div>
              </div>
              
              <div v-else-if="isVideo" class="video-preview">
                <div class="video-player-container">
                  <div class="video-title">{{ resource.title }}</div>
                  <video 
                    controls 
                    class="video-player"
                    :src="getProxiedFileUrl(resource.fileUrl, 'video')"
                    @error="videoError = true"
                  >
                    您的浏览器不支持视频播放
                  </video>
                  <div v-if="videoError" class="preview-error">
                    <el-empty description="视频加载失败">
                      <el-button type="primary" @click="handleDownload">
                        下载观看
                      </el-button>
                    </el-empty>
                  </div>
                </div>
              </div>
              
              <div v-else-if="isPPT" class="ppt-preview">
                <div v-if="isLoadingPPT" class="preview-loading">
                  <el-skeleton :rows="10" animated />
                  <p>PPT文件加载中，请稍候...</p>
                </div>
                <iframe 
                  v-if="resource.fileUrl"
                  :src="getOfficePreviewUrl(resource.fileUrl)" 
                  width="100%" 
                  height="100%" 
                  frameborder="0"
                  @load="isLoadingPPT = false"
                  @error="pptError = true; isLoadingPPT = false"
                ></iframe>
                <div v-if="pptError" class="preview-error">
                  <el-empty description="PPT预览加载失败">
                    <el-button type="primary" @click="handleDownload">
                      下载查看
                    </el-button>
                  </el-empty>
                </div>
              </div>
              
              <div v-else-if="isCSV" class="csv-preview">
                <div v-if="isLoadingContent" class="preview-loading">
                  <el-skeleton :rows="10" animated />
                  <p>CSV数据加载中，请稍候...</p>
                </div>
                <div v-else-if="contentLoadError" class="preview-error">
                  <el-empty description="CSV预览加载失败">
                    <el-button type="primary" @click="handleDownload">
                      下载查看
                    </el-button>
                  </el-empty>
                </div>
                <div v-else class="csv-container">
                  <div class="csv-controls">
                    <div class="csv-info">
                      <span class="csv-file-name">{{ getFileName(resource.fileUrl) }}</span>
                    </div>
                    <el-button size="small" @click="handleDownload">
                      下载CSV文件
                    </el-button>
                  </div>
                  <div class="csv-content-wrapper">
                    <div ref="csvContentRef" class="csv-content">
                      <el-table :data="csvData" border stripe height="100%" style="width: 100%">
                        <el-table-column 
                          v-for="(header, index) in csvHeaders" 
                          :key="index" 
                          :prop="header" 
                          :label="header" 
                          min-width="150"
                        />
                      </el-table>
                    </div>
                  </div>
                </div>
              </div>
              
              <div v-else class="no-preview">
                <el-icon :size="48"><Document /></el-icon>
                <p>该文件类型不支持在线预览，请下载后查看</p>
              </div>
            </div>
          </div>
        </div>
        
        <div class="resource-sidebar">
          <div class="download-card">
            <div class="download-stats">
              <div class="stat-item">
                <div class="stat-value">{{ resource.viewCount }}</div>
                <div class="stat-label">浏览</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ resource.downloadCount }}</div>
                <div class="stat-label">下载</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ resource.likeCount || 0 }}</div>
                <div class="stat-label">点赞</div>
              </div>
            </div>
            
            <div class="action-buttons">
              <el-button type="primary" @click="handleDownload" :loading="downloading" class="download-button">
                <el-icon><Download /></el-icon> 下载资源
              </el-button>
              
              <el-button @click="handleCollect" :type="isCollected ? 'danger' : 'default'" class="collect-button">
                <el-icon>
                  <Star v-if="isCollected" />
                  <StarFilled v-else />
                </el-icon>
                {{ isCollected ? '取消收藏' : '收藏' }}
              </el-button>
              
              <el-button @click="handleLike" :type="isLiked ? 'success' : 'default'" class="like-button">
                <el-icon>
                  <Pointer v-if="isLiked" />
                  <Pointer v-else />
                </el-icon>
                {{ isLiked ? '已点赞' : '点赞' }}
              </el-button>
            </div>
          </div>
          
          <div class="uploader-card">
            <div class="card-header">
              <h3>上传者信息</h3>
            </div>
            <div class="uploader-info">
              <el-avatar :size="64" :src="resource.uploaderAvatar || defaultAvatar"></el-avatar>
              <div class="uploader-details">
                <div class="uploader-name" v-html="resource.uploaderName || '未知用户'"></div>
                <div class="uploader-meta">{{ resource.uploaderCollege || '未知学院' }} · {{ resource.uploaderMajor || '未知专业' }}</div>
                <div class="uploader-uploads">共上传 {{ resource.uploaderTotalUploads || 0 }} 份资料</div>
              </div>
            </div>
          </div>
          
          <div class="related-card" v-if="relatedResources.length > 0">
            <div class="card-header">
              <h3>相关资源</h3>
            </div>
            <div class="related-list">
              <div 
                v-for="item in relatedResources" 
                :key="item.id" 
                class="related-item"
                @click="goToDetail(item.id)"
              >
                <div class="related-icon">
                  <el-icon>
                    <Document v-if="item.fileType && item.fileType.includes('doc')" />
                    <DataAnalysis v-else-if="item.fileType && item.fileType.includes('xls')" />
                    <Files v-else-if="item.fileType && item.fileType.includes('pdf')" />
                    <Picture v-else-if="item.fileType && item.fileType.includes('image')" />
                    <VideoPlay v-else-if="item.fileType && item.fileType.includes('video')" />
                    <Folder v-else />
                  </el-icon>
                </div>
                <div class="related-info">
                  <div class="related-title">{{ item.title }}</div>
                  <div class="related-meta">
                    <span>{{ formatResourceTime(item) }}</span>
                    <span>{{ item.downloadCount }}下载</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div v-else class="error-container">
      <el-empty description="资源不存在或已被删除" />
      <el-button type="primary" @click="router.push('/resource')">
        返回资源列表
      </el-button>
    </div>
    
    <div v-if="resource" class="comment-section">
      <div class="card-header">
        <h3>评论区 ({{ commentTotal }})</h3>
      </div>
      
      <div class="comment-input-container">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          placeholder="分享你的想法..."
          maxlength="500"
          show-word-limit
        ></el-input>
        <div class="comment-submit">
          <el-button 
            type="primary" 
            :disabled="!commentContent.trim()" 
            :loading="submittingComment"
            @click="submitComment"
          >
            发表评论
          </el-button>
        </div>
      </div>
      
      <div v-if="comments.length > 0" class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-avatar">
            <el-avatar :size="40" :src="comment.userAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"></el-avatar>
          </div>
          <div class="comment-content">
            <div class="comment-header">
              <span class="comment-author">{{ comment.username }}</span>
              <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
            </div>
            <div class="comment-text" :class="{ 'deleted': comment.isDeleted }">
              <template v-if="!comment.isDeleted">
                <p v-if="comment.replyToUsername" class="reply-info">
                  回复 <span class="reply-name">@{{ comment.replyToUsername }}</span>
                </p>
                <p>{{ comment.content }}</p>
              </template>
              <p v-else class="deleted-text">该评论已被删除</p>
            </div>
            <div class="comment-actions" v-if="!comment.isDeleted">
              <span class="action-item" @click="showReplyInput(comment)">
                <el-icon><ChatDotRound /></el-icon> 回复
              </span>
              <span 
                class="action-item" 
                :class="{ 'active': comment.liked }"
                @click="likeComment(comment)"
              >
                <el-icon><Star /></el-icon> 
                {{ comment.liked ? '已点赞' : '点赞' }} 
                <span v-if="comment.likes">({{ comment.likes }})</span>
              </span>
              <span 
                v-if="isCommentOwner(comment)" 
                class="action-item delete" 
                @click="deleteComment(comment)"
              >
                <el-icon><Delete /></el-icon> 删除
              </span>
            </div>
            
            <div v-if="replyingTo === comment.id && !replyingToChild" class="reply-input">
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="2"
                placeholder="回复评论..."
                maxlength="500"
                show-word-limit
              ></el-input>
              <div class="reply-submit">
                <el-button size="small" @click="cancelReply">取消</el-button>
                <el-button 
                  type="primary" 
                  size="small"
                  :disabled="!replyContent.trim()" 
                  :loading="submittingComment"
                  @click="submitReply(comment)"
                >
                  回复
                </el-button>
              </div>
            </div>
            
            <CommentNested 
              v-if="comment.children && comment.children.length > 0" 
              :comments="comment.children" 
              :root-comment="comment"
              :replying-to="replyingTo"
              :replying-to-child="replyingToChild"
              :reply-content="replyContent"
              :submitting-comment="submittingComment"
              @show-reply="showReplyInput"
              @cancel-reply="cancelReply"
              @submit-reply="submitReply"
              @like-comment="likeComment"
              @delete-comment="deleteComment"
            />
          </div>
        </div>
      </div>
      
      <div v-else class="no-comments">
        <el-empty description="暂无评论，快来发表第一条评论吧！"></el-empty>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, watch, nextTick, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useResourceStore } from '../../stores/resource'
import { useUserStore } from '../../stores/user'
import type { Resource, Comment } from '../../stores/resource'
import { Document, Download, Star, StarFilled, Pointer, DataAnalysis, Files, Picture, VideoPlay, Folder, ChatDotRound, Delete, FullScreen, Minus, DArrowRight } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import CommentNested from '@/components/CommentNested.vue'
import PdfEmbed from 'vue-pdf-embed'
import hljs from 'highlight.js/lib/core'
import python from 'highlight.js/lib/languages/python'
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import java from 'highlight.js/lib/languages/java'
import xml from 'highlight.js/lib/languages/xml'
import sql from 'highlight.js/lib/languages/sql'
import css from 'highlight.js/lib/languages/css'
import json from 'highlight.js/lib/languages/json'
import yaml from 'highlight.js/lib/languages/yaml'
import bash from 'highlight.js/lib/languages/bash'
import markdown from 'highlight.js/lib/languages/markdown'
import ruby from 'highlight.js/lib/languages/ruby'
import 'highlight.js/styles/github.css'

const route = useRoute()
const router = useRouter()
const resourceStore = useResourceStore()
const userStore = useUserStore()

const resource = ref<Resource | null>(null)
const loading = ref(true)
const downloading = ref(false)
const isCollected = ref(false)
const isLiked = ref(false)
const relatedResources = ref<Resource[]>([])

const comments = ref<Comment[]>([])
const commentTotal = ref(0)
const commentContent = ref('')
const replyingTo = ref<number | undefined>(undefined)
const replyingToUser = ref<string | null>(null)
const replyingToChild = ref<boolean>(false)
const replyContent = ref('')
const submittingComment = ref(false)

const resourceId = computed(() => Number(route.params.id))

const canPreview = computed(() => {
  if (!resource.value) return false;
  return isPDF.value || isImage.value || isDocument.value || isExcel.value || isCode.value || isAudio.value || isVideo.value || isPPT.value || isCSV.value;
})

const isPDF = computed(() => {
  if (!resource.value) return false
  return resource.value.fileType === 'pdf' || resource.value.fileType === 'application/pdf'
})

const isImage = computed(() => {
  if (!resource.value) return false
  const imageTypes = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico', 'image/jpeg', 'image/png', 'image/gif', 'image/svg+xml', 'image/x-icon']
  
  // 检查文件类型
  if (imageTypes.some(type => resource.value?.fileType.toLowerCase().includes(type))) {
    return true;
  }
  
  // 检查文件扩展名
  if (resource.value.fileUrl) {
    const ext = resource.value.fileUrl.split('.').pop()?.toLowerCase();
    return ext ? imageTypes.includes(ext) : false;
  }
  
  return false;
})

const isDocument = computed(() => {
  if (!resource.value) return false;
  const docTypes = ['doc', 'docx', 'rtf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
  return docTypes.some(type => resource.value?.fileType.toLowerCase().includes(type));
});

const isExcel = computed(() => {
  if (!resource.value) return false;
  const excelTypes = ['xls', 'xlsx', 'application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'];
  return excelTypes.some(type => resource.value?.fileType.toLowerCase().includes(type));
});

const isCode = computed(() => {
  if (!resource.value) return false;
  // 如果已经判断为图片或CSV，则不是代码
  if (isImage.value || isCSV.value) return false;
  
  const codeTypes = ['js', 'ts', 'java', 'py', 'c', 'cpp', 'cs', 'html', 'css', 'php', 'rb', 'go', 'json', 'xml', 'txt', 'yml', 'yaml', 'sql', 'md', 'erb'];
  // 检查文件扩展名
  if (resource.value.fileUrl) {
    const ext = resource.value.fileUrl.split('.').pop()?.toLowerCase();
    if (ext && codeTypes.includes(ext)) return true;
  }
  // 检查MIME类型
  const codeMimeTypes = ['text/plain', 'application/json', 'text/html', 'text/css', 'text/javascript', 
                         'application/x-yaml', 'application/sql', 'text/markdown', 'application/x-ruby-erb'];
  return codeMimeTypes.some(type => resource.value?.fileType.toLowerCase().includes(type));
});

const isAudio = computed(() => {
  if (!resource.value) return false;
  const audioTypes = ['mp3', 'wav', 'ogg', 'flac', 'm4a', 'audio/mpeg', 'audio/wav', 'audio/ogg', 'audio/flac'];
  return audioTypes.some(type => resource.value?.fileType.toLowerCase().includes(type));
});

const isVideo = computed(() => {
  if (!resource.value) return false;
  const videoTypes = ['mp4', 'webm', 'avi', 'mov', 'flv', 'video/mp4', 'video/webm', 'video/quicktime'];
  return videoTypes.some(type => resource.value?.fileType.toLowerCase().includes(type));
});

const isPPT = computed(() => {
  if (!resource.value) return false;
  const pptTypes = ['ppt', 'pptx', 'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation'];
  return pptTypes.some(type => resource.value?.fileType.toLowerCase().includes(type));
});

const isCSV = computed(() => {
  if (!resource.value) return false;
  
  // 检查文件类型
  if (resource.value.fileType.toLowerCase().includes('csv') || 
      resource.value.fileType.toLowerCase().includes('text/csv')) {
    return true;
  }
  
  // 检查文件扩展名
  if (resource.value.fileUrl) {
    const ext = resource.value.fileUrl.split('.').pop()?.toLowerCase();
    return ext === 'csv';
  }
  
  return false;
})

const isFullscreen = ref(false)
const useDirectPreview = ref(false)
const pdfLoading = ref(true)
const pdfError = ref(false)

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const fileContent = ref('');
const isLoadingContent = ref(false);
const contentLoadError = ref(false);

const codeContentRef = ref<HTMLElement | null>(null);
const codeElement = ref<HTMLElement | null>(null);
const isCodeTooWide = ref(false);

const csvData = ref<any[]>([]);
const csvHeaders = ref<string[]>([]);
const csvContentRef = ref<HTMLElement | null>(null);

const isLoadingDocument = ref(true);
const documentError = ref(false);
const isLoadingExcel = ref(true);
const excelError = ref(false);
const audioError = ref(false);
const videoError = ref(false);
const isLoadingPPT = ref(true);
const pptError = ref(false);

const fetchResourceData = async () => {
  try {
    loading.value = true
    const result = await resourceStore.getResourceDetailWithComments(resourceId.value)
    if (result.resource) {
      resource.value = result.resource
      comments.value = result.comments || []
      commentTotal.value = result.commentCount || 0
      isCollected.value = result.resource.isCollected === true
      isLiked.value = result.resource.isLiked === true
      
      // 如果是PDF，重置加载状态
      if (isPDF.value) {
        pdfLoading.value = true;
        pdfError.value = false;
      }
      
      getRelatedResources()
      if (resource.value && resource.value.title) {
        document.title = `${resource.value.title} - AuditStudyHub`
      }
    } else {
      resource.value = null
      ElMessage.error('资源不存在或已被删除')
    }
  } catch (error) {
    console.error('获取资源详情失败', error)
    resource.value = null
    ElMessage.error('获取资源详情失败')
  } finally {
    loading.value = false
  }
}

const getRelatedResources = async () => {
  if (!resource.value) return
  try {
    const { list } = await resourceStore.getResourceList({
      page: 1,
      size: 4,
      categoryId: resource.value.categoryId,
      status: 1
    })
    relatedResources.value = list.filter((item: Resource) => item.id !== resource.value?.id)
  } catch (error) {
    console.error('获取相关资源失败', error)
  }
}

const handleDownload = async () => {
  if (!resource.value) return
  if (!userStore.token) {
    ElMessage.warning('请先登录再下载资源')
    router.push('/login')
    return
  }
  downloading.value = true
  try {
    await resourceStore.downloadResource(resourceId.value)
  } catch (error) {
    console.error('下载资源失败', error)
    ElMessage.error('下载资源失败')
  } finally {
    downloading.value = false
  }
}

const handleCollect = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const { collected } = await resourceStore.collectResource(resourceId.value)
    isCollected.value = collected
    ElMessage.success(collected ? '收藏成功' : '取消收藏成功')
  } catch (error) {
    console.error('收藏操作失败', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const handleLike = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const { liked, likeCount } = await resourceStore.likeResource(resourceId.value)
    isLiked.value = liked
    if (resource.value) {
      resource.value.likeCount = likeCount
    }
    ElMessage.success(liked ? '点赞成功' : '取消点赞成功')
  } catch (error) {
    console.error('点赞操作失败', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

/**
 * 格式化日期显示
 */
const formatDate = (date?: string) => {
  if (!date) return '-'
  
  try {
    // 使用dayjs处理ISO格式日期
    return dayjs(date).format('YYYY-MM-DD HH:mm')
  } catch (error) {
    console.error('日期格式化错误', error, date)
    return '-'
  }
}

const formatFileSize = (bytes?: number) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const goToDetail = (id: number) => {
  router.push(`/resource/${id}`)
}

const submitComment = async () => {
  if (!resourceId.value) return
  if (!userStore.token) {
    ElMessage.warning('请先登录再发表评论')
    router.push('/login')
    return
  }
  if (!commentContent.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  submittingComment.value = true
  try {
    const newComment = await resourceStore.addComment({
      resourceId: resourceId.value,
      content: commentContent.value.trim()
    })
    
    console.log('后端返回的评论数据:', newComment)
    
    if (newComment) {
      // 直接添加新评论到评论列表
      const formattedComment = {
        id: newComment.id,
        resourceId: newComment.resourceId || resourceId.value,
        userId: newComment.userId,
        username: newComment.username,
        userAvatar: newComment.userAvatar,
        content: newComment.content,
        createdAt: newComment.createdAt || new Date().toISOString(),
        updatedAt: newComment.updatedAt || new Date().toISOString(),
        replyTo: newComment.parentId,
        replyToUsername: newComment.replyToUsername,
        children: []
      }
      
      comments.value.push(formattedComment)
      commentTotal.value += 1
      
      // 清空评论内容
      commentContent.value = ''
      ElMessage.success('评论发表成功')
    }
  } catch (error) {
    console.error('评论失败', error)
    ElMessage.error('评论失败，请稍后再试')
  } finally {
    submittingComment.value = false
  }
}

const showReplyInput = (rootComment: Comment, replyToChild?: Comment) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录再回复评论')
    router.push('/login')
    return
  }
  
  if (replyToChild) {
    replyingTo.value = replyToChild.id
    replyingToUser.value = replyToChild.username
    replyingToChild.value = true
  } else {
    replyingTo.value = rootComment.id
    replyingToUser.value = rootComment.username
    replyingToChild.value = false
  }
  
  replyContent.value = ''
}

const cancelReply = () => {
  replyingTo.value = undefined
  replyingToUser.value = null
  replyingToChild.value = false
  replyContent.value = ''
}

const submitReply = async (rootComment: Comment, childComment?: Comment) => {
  if (!resourceId.value) return
  if (!userStore.token) {
    ElMessage.warning('请先登录再回复评论')
    router.push('/login')
    return
  }
  if (!replyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  submittingComment.value = true
  try {
    // 确定要回复的是哪条评论
    const replyToId = childComment ? childComment.id : rootComment.id
    
    // 使用replyTo参数，后端期望的就是这个字段名
    const response = await resourceStore.addComment({
      resourceId: resourceId.value,
      content: replyContent.value.trim(),
      replyTo: replyToId
    })
    
    console.log('后端返回的回复数据:', response)
    
    const newReply = response || {}
    
    // 格式化新回复，确保字段映射正确
    const formattedReply = {
      id: newReply.id,
      resourceId: newReply.resourceId || resourceId.value,
      userId: newReply.userId,
      username: newReply.username,
      userAvatar: newReply.userAvatar,
      content: newReply.content,
      createdAt: newReply.createdAt || new Date().toISOString(),
      updatedAt: newReply.updatedAt || new Date().toISOString(),
      replyTo: newReply.replyTo || newReply.parentId || replyToId,
      replyToUsername: newReply.replyToUsername || (childComment ? childComment.username : rootComment.username),
      children: [],
      likes: 0,
      liked: false
    }
    
    // 递归查找目标评论并添加回复
    const findAndAddReply = (commentsList: Comment[], targetId: number, reply: Comment): boolean => {
      // 直接查找当前层级
      const directTarget = commentsList.find(c => c.id === targetId);
      if (directTarget) {
        if (!directTarget.children) {
          directTarget.children = [];
        }
        directTarget.children.push(reply);
        return true;
      }
      
      // 递归查找子评论
      for (const comment of commentsList) {
        if (comment.children && comment.children.length > 0) {
          if (findAndAddReply(comment.children, targetId, reply)) {
            return true;
          }
        }
      }
      
      return false;
    };
    
    // 根据层级关系添加回复
    if (childComment) {
      // 回复子评论
      findAndAddReply(comments.value, replyToId, formattedReply);
    } else {
      // 回复根评论
      const rootTarget = comments.value.find(c => c.id === rootComment.id);
      if (rootTarget) {
        if (!rootTarget.children) {
          rootTarget.children = [];
        }
        rootTarget.children.push(formattedReply);
      }
    }
    
    commentTotal.value += 1
    
    // 清空回复内容并关闭回复框
    replyContent.value = ''
    replyingTo.value = undefined
    replyingToUser.value = null
    replyingToChild.value = false
    
    ElMessage.success('回复发表成功')
  } catch (error) {
    console.error('回复失败', error)
    ElMessage.error('回复失败，请稍后再试')
  } finally {
    submittingComment.value = false
  }
}

const likeComment = async (comment: Comment) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录再点赞评论')
    router.push('/login')
    return
  }
  try {
    const result = await resourceStore.likeComment(comment.id)
    comment.liked = result.liked
    comment.likes = result.likeCount
    ElMessage.success(comment.liked ? '点赞成功' : '取消点赞')
  } catch (error) {
    console.error('评论点赞失败', error)
    ElMessage.error('操作失败，请稍后再试')
  }
}

const deleteComment = async (comment: Comment) => {
  ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const success = await resourceStore.deleteComment(comment.id, resourceId.value);
      
      // 如果删除成功，更新UI
      if (success) {
        // 根据评论类型处理UI更新
        if (comment.replyTo) {
          // 子评论: 从父评论的children数组中移除
          const parentComment = comments.value.find(item => item.id === comment.replyTo || 
                                                 item.children?.some(child => child.id === comment.id));
          if (parentComment && parentComment.children) {
            parentComment.children = parentComment.children.filter(child => child.id !== comment.id);
          }
        } else {
          // 根评论: 从评论列表中移除或标记为已删除
          // 策略1: 如果有子评论，则保留结构但内容改为"该评论已删除"
          if (comment.children && comment.children.length > 0) {
            comment.content = '该评论已被删除';
            comment.isDeleted = true;  // 添加一个标记字段
          } else {
            // 策略2: 如果没有子评论，直接从列表中移除
            comments.value = comments.value.filter(item => item.id !== comment.id);
          }
        }
        
        // 更新评论计数
        commentTotal.value = Math.max(0, commentTotal.value - 1);
        
        ElMessage.success('评论已删除');
      }
    } catch (error) {
      console.error('删除评论失败', error);
      ElMessage.error('删除失败，请稍后再试');
    }
  }).catch(() => {});
}

const isCommentOwner = (comment: Comment) => {
  return userStore.userInfo && userStore.userInfo.id === comment.userId
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  
  if (isFullscreen.value) {
    document.body.style.overflow = 'hidden'
    nextTick(() => {
      const container = document.querySelector('.preview-container')
      container?.scrollIntoView({ behavior: 'smooth' })
    })
  } else {
    document.body.style.overflow = ''
  }
}

const handlePdfRendered = () => {
  pdfLoading.value = false;
  pdfError.value = false;
}

const handlePdfError = (error: Event) => {
  console.error('PDF渲染错误', error);
  pdfLoading.value = false;
  pdfError.value = true;
  
  if (useDirectPreview.value) {
    ElMessage.error('PDF预览加载失败，请尝试下载后查看');
  }
}

const formatResourceTime = (item: Resource) => {
  return formatDate(item.createdAt);
}

const getResourceDate = (resource: any) => {
  return resource.createdAt || resource.createTime;
}

const getResourceTags = (resource: any) => {
  if (!resource.tags) return [];
  
  // 如果已经是数组，直接返回
  if (Array.isArray(resource.tags)) {
    return resource.tags;
  }
  
  // 如果是字符串，按逗号分隔
  if (typeof resource.tags === 'string') {
    return resource.tags.split(',');
  }
  
  return [];
}

// 获取代理PDF URL的函数，更新为使用通用文件代理
const getProxiedPdfUrl = (url: string | null) => {
  if (!url) return '';
  return `/api/resource/proxy-file?url=${encodeURIComponent(url)}&type=pdf`;
}

// 检查代码内容是否需要水平滚动
const checkHorizontalScroll = () => {
  if (codeContentRef.value) {
    const element = codeContentRef.value;
    isCodeTooWide.value = element.scrollWidth > element.clientWidth;
  }
};

// 修改fetchFileContent函数，使用代理API获取文件内容
const fetchFileContent = async () => {
  if (!resource.value?.fileUrl) return;
  
  if (!isCode.value && !isCSV.value) return;
  
  isLoadingContent.value = true;
  contentLoadError.value = false;
  
  try {
    // 根据文件类型确定代理类型
    let proxyType = 'code';
    const ext = resource.value.fileUrl.split('.').pop()?.toLowerCase();
    
    if (ext === 'txt') {
      proxyType = 'text';
    } else if (ext === 'csv') {
      proxyType = 'csv';
    } else if (['yml', 'yaml'].includes(ext || '')) {
      proxyType = 'yml';
    } else if (ext === 'sql') {
      proxyType = 'sql';
    } else if (ext === 'md') {
      proxyType = 'markdown';
    } else if (ext === 'erb') {
      proxyType = 'code';
    }
    
    const response = await fetch(`/api/resource/proxy-file?url=${encodeURIComponent(resource.value.fileUrl)}&type=${proxyType}`);
    if (!response.ok) throw new Error('无法加载文件内容');
    
    const text = await response.text();
    
    if (isCSV.value) {
      // 处理CSV数据
      parseCSVData(text);
    } else {
      // 处理代码文件并应用高亮
      fileContent.value = text;
      
      // 延迟一帧，确保DOM已更新再应用高亮
      setTimeout(() => {
        if (codeElement.value) {
          try {
            console.log('应用高亮前:', codeElement.value.className);
            
            // 获取语言类型
            const language = getHighlightLanguage(ext || '');
            console.log('当前文件类型:', ext, '对应高亮语言:', language);
            
            // 手动设置语言类
            codeElement.value.className = `language-${language}`;
            
            // 应用高亮
            hljs.highlightElement(codeElement.value);
            console.log('高亮应用后:', codeElement.value.className);
            
            // 检查是否需要水平滚动
            checkHorizontalScroll();
          } catch (e) {
            console.error('高亮处理失败', e);
          }
        } else {
          console.error('代码元素不存在');
        }
      }, 100);
    }
  } catch (error) {
    console.error('获取文件内容失败', error);
    contentLoadError.value = true;
    fileContent.value = '';
    csvData.value = [];
    csvHeaders.value = [];
  } finally {
    isLoadingContent.value = false;
  }
};

// 解析CSV数据
const parseCSVData = (csvText: string) => {
  try {
    const lines = csvText.split('\n');
    if (lines.length === 0) return;
    
    // 处理第一行作为表头
    const headers = lines[0].split(',').map(header => header.trim().replace(/^"|"$/g, ''));
    csvHeaders.value = headers;
    
    // 解析数据行
    const data = [];
    for (let i = 1; i < lines.length; i++) {
      if (!lines[i].trim()) continue;
      
      // 简单拆分CSV（不考虑引号内的逗号）
      const values = lines[i].split(',').map(value => value.trim().replace(/^"|"$/g, ''));
      
      // 创建行对象
      const row: Record<string, string> = {};
      headers.forEach((header, index) => {
        row[header] = values[index] || '';
      });
      
      data.push(row);
    }
    
    csvData.value = data;
  } catch (error) {
    console.error('CSV解析失败', error);
    contentLoadError.value = true;
    csvData.value = [];
    csvHeaders.value = [];
  }
};

// 获取文件名的辅助函数
const getFileName = (url: string | null) => {
  if (!url) return '';
  const parts = url.split('/');
  return parts[parts.length - 1];
};

// 添加获取代码语言的辅助函数
const getCodeLanguage = () => {
  if (!resource.value?.fileUrl) return '文本';
  
  const ext = resource.value.fileUrl.split('.').pop()?.toLowerCase();
  if (!ext) return '文本';
  
  const languageMap: Record<string, string> = {
    'js': 'JavaScript',
    'ts': 'TypeScript',
    'java': 'Java',
    'py': 'Python',
    'rb': 'Ruby',
    'php': 'PHP',
    'c': 'C',
    'cpp': 'C++',
    'cs': 'C#',
    'html': 'HTML',
    'css': 'CSS',
    'json': 'JSON',
    'xml': 'XML',
    'yml': 'YAML',
    'yaml': 'YAML',
    'sql': 'SQL',
    'svg': 'SVG',
    'ico': 'ICO图标',
    'txt': '文本',
    'go': 'Go',
    'swift': 'Swift',
    'md': 'Markdown',
    'erb': 'Ruby ERB',
    'sh': 'Shell'
  };
  
  return languageMap[ext] || ext.toUpperCase();
};

// 添加回缺失的获取文件高亮语言的辅助函数
const getHighlightLanguage = (extension: string): string => {
  const languageMap: Record<string, string> = {
    'js': 'javascript',
    'ts': 'typescript',
    'java': 'java',
    'py': 'python',
    'rb': 'ruby',
    'erb': 'ruby', // ERB文件使用Ruby高亮
    'php': 'php',
    'c': 'c',
    'cpp': 'cpp',
    'cs': 'csharp',
    'html': 'html',
    'css': 'css',
    'json': 'json',
    'xml': 'xml',
    'yml': 'yaml',
    'yaml': 'yaml',
    'sql': 'sql',
    'txt': 'plaintext',
    'md': 'markdown',
    'sh': 'bash',
  };
  
  return languageMap[extension] || 'plaintext';
};

// 修改获取Office文档预览URL的方法，增加对PPT的特殊处理
const getOfficePreviewUrl = (url: string | null) => {
  if (!url) return '';
  
  const encodedUrl = encodeURIComponent(url);
  
  // 对PPT类文件使用Google Docs Viewer作为备选方案
  if (isPPT.value) {
    // 尝试使用Microsoft Office Online
    return `https://view.officeapps.live.com/op/embed.aspx?src=${encodedUrl}`;
    
    // 如果失败，前端可以提供切换到Google Docs预览的选项
    // return `https://docs.google.com/viewer?url=${encodedUrl}&embedded=true`;
  }
  
  // 其他Office文档仍使用Microsoft Office Online
  return `https://view.officeapps.live.com/op/embed.aspx?src=${encodedUrl}`;
};

// 添加回获取代理的文件URL的通用方法
const getProxiedFileUrl = (url: string | null, fileType?: string) => {
  if (!url) return '';
  return `/api/resource/proxy-file?url=${encodeURIComponent(url)}&type=${fileType || ''}`;
};

onMounted(() => {
  fetchResourceData()
  
  // 手动注册常用的语言
  hljs.registerLanguage('python', python);
  hljs.registerLanguage('javascript', javascript);
  hljs.registerLanguage('typescript', typescript);
  hljs.registerLanguage('java', java);
  hljs.registerLanguage('xml', xml);
  hljs.registerLanguage('sql', sql);
  hljs.registerLanguage('css', css);
  hljs.registerLanguage('json', json);
  hljs.registerLanguage('yaml', yaml);
  hljs.registerLanguage('bash', bash);
  hljs.registerLanguage('markdown', markdown);
  hljs.registerLanguage('ruby', ruby);
  
  // 设置配置项
  hljs.configure({
    ignoreUnescapedHTML: true,
    throwUnescapedHTML: false
  });
  
  console.log('highlight.js已初始化，支持的语言:', hljs.listLanguages());
})

onUnmounted(() => {
  document.body.style.overflow = ''
})

// 更新watch监听文件类型变化的逻辑
watch(() => resource.value, () => {
  if (resource.value) {
    // 重置所有加载状态
    isLoadingDocument.value = isDocument.value;
    isLoadingExcel.value = isExcel.value;
    isLoadingPPT.value = isPPT.value;
    pdfLoading.value = isPDF.value;
    
    documentError.value = false;
    excelError.value = false;
    audioError.value = false;
    videoError.value = false;
    pptError.value = false;
    contentLoadError.value = false;
    
    // 如果是代码或CSV文件，加载内容
    if (isCode.value || isCSV.value) {
      fetchFileContent();
    }
  }
}, { immediate: true });

// 添加获取代码高亮类名的方法
const getCodeClass = () => {
  if (!resource.value?.fileUrl) return '';
  
  const ext = resource.value.fileUrl.split('.').pop()?.toLowerCase();
  if (!ext) return '';
  
  const highlightLanguage = getHighlightLanguage(ext);
  return highlightLanguage ? `language-${highlightLanguage}` : '';
};
</script>

<style lang="scss" scoped>
.resource-detail-container {
  background-color: #f8f9fa;
  min-height: 100vh;
  padding: 30px;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', Arial, sans-serif;
}

.resource-header {
  background: linear-gradient(135deg, #fff, #f8fbff);
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 6px 16px rgba(0,0,0,0.07);
  margin-bottom: 30px;
  border: 1px solid rgba(230,235,245,0.8);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #4e9fff, #28d17c);
  }
  
  .back-button {
    margin-bottom: 20px;
    .el-button {
      border-radius: 8px;
      font-weight: 500;
      transition: all 0.3s;
      &:hover {
        transform: translateX(-5px);
      }
    }
  }
  
  .resource-title {
    font-size: 32px;
    font-weight: 700;
    color: #1a1c20;
    margin-bottom: 25px;
    line-height: 1.3;
    position: relative;
    
    &::after {
      content: '';
      display: block;
      width: 60px;
      height: 4px;
      margin-top: 12px;
      background: linear-gradient(90deg, #4e9fff, #28d17c);
      border-radius: 2px;
    }
  }
  
  .resource-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 22px;
    align-items: center;
    
    .meta-item {
      display: flex;
      align-items: center;
      background-color: rgba(240,245,255,0.6);
      padding: 8px 14px;
      border-radius: 6px;
      transition: all 0.3s;
      
      &:hover {
        background-color: rgba(230,240,255,1);
        transform: translateY(-3px);
      }
      
      .label {
        color: #5c6b8a;
        margin-right: 8px;
        font-weight: 500;
      }
    }
  }
}

.resource-content {
  display: flex;
  gap: 25px;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
}

.resource-info {
  flex: 2;
  max-width: 100%;
  width: 100%;
  overflow: hidden;
}

.resource-sidebar {
  flex: 1;
  min-width: 280px;
}

.info-card, .preview-card, .download-card, .uploader-card, .related-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 25px;
  transition: transform 0.2s;
  
  &:hover {
    transform: translateY(-2px);
  }
}

.card-header {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
  
  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }
}

.card-body {
  padding: 20px;
}

.resource-description {
  font-size: 15px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

.resource-tags {
  margin-top: 20px;
  
  .tag-title {
    margin-bottom: 10px;
    color: #606266;
    font-weight: 500;
  }
  
  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
}

.resource-college-major {
  margin-top: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  
  .college-major-item {
    display: flex;
    .label {
      color: #606266;
      margin-right: 8px;
      font-weight: 500;
    }
  }
}

.preview-card {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 25px;
  overflow: hidden;
  width: 100%;
  max-width: 100%;
}

.preview-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #409eff;
  padding: 4px 8px;
  border-radius: 4px;
  background-color: rgba(64, 158, 255, 0.1);
  transition: all 0.2s;
}

.action-btn:hover {
  background-color: rgba(64, 158, 255, 0.2);
}

.preview-container {
  height: 600px;
  position: relative;
  background-color: #f5f7fa;
  overflow: hidden;
  width: 100%;
  max-width: 100%;
}

.preview-container.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
  background-color: white;
}

.pdf-preview-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pdf-container {
  width: 100%;
  height: 600px;
  overflow: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #f5f7fa;
}

.pdf-embed {
  width: 100%;
  height: 100%;
  max-width: 100%;
}

.pdf-loading, .pdf-error {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.9);
  z-index: 10;
}

.pdf-loading p {
  margin-top: 16px;
  color: #606266;
}

.download-card {
  background: linear-gradient(135deg, #4e9fff, #28d17c);
  border-radius: 16px;
  padding: 0;
  overflow: hidden;
  color: white;
  box-shadow: 0 6px 16px rgba(0,0,0,0.12);
  margin-bottom: 30px;
  
  .download-stats {
    display: flex;
    padding: 25px;
    background-color: rgba(255,255,255,0.1);
    
    .stat-item {
      flex: 1;
      text-align: center;
      position: relative;
      
      &:not(:last-child)::after {
        content: '';
        position: absolute;
        right: 0;
        top: 10%;
        height: 80%;
        width: 1px;
        background-color: rgba(255,255,255,0.2);
      }
      
      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: white;
      }
      
      .stat-label {
        font-size: 14px;
        color: rgba(255,255,255,0.8);
        margin-top: 6px;
      }
    }
  }
  
  .action-buttons {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 25px;
    
    .download-button {
      background-color: white;
      color: #4e9fff;
      border: none;
      height: 46px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 8px;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      }
    }
    
    .collect-button, .like-button {
      background-color: rgba(255,255,255,0.2);
      color: white;
      border: none;
      border-radius: 8px;
      height: 40px;
      font-weight: 500;
      
      &:hover {
        background-color: rgba(255,255,255,0.3);
      }
    }
  }
}

.uploader-info {
  padding: 20px;
  display: flex;
  align-items: center;
  
  .uploader-details {
    margin-left: 20px;
    .uploader-name {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 8px;
      color: #303133;
    }
    .uploader-meta, .uploader-uploads {
      font-size: 14px;
      color: #606266;
      margin-bottom: 6px;
    }
  }
}

.related-list {
  .related-item {
    display: flex;
    padding: 15px 20px;
    cursor: pointer;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child { border-bottom: none; }
    &:hover {
      background: #f5f7fa;
      .related-title { color: #409eff; }
    }
    
    .related-icon {
      color: #409eff;
      margin-right: 15px;
    }
    
    .related-info {
      flex: 1;
      overflow: hidden;
      .related-title {
        font-size: 15px;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .related-meta {
        font-size: 13px;
        color: #909399;
        display: flex;
        justify-content: space-between;
      }
    }
  }
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  
  .el-button {
    margin-top: 25px;
    padding: 12px 20px;
  }
}

.comment-section {
  background: white;
  border-radius: 16px;
  box-shadow: 0 6px 16px rgba(0,0,0,0.07);
  overflow: hidden;
  margin-top: 30px;
  border: 1px solid rgba(230,235,245,0.8);
  
  .card-header {
    padding: 12px 20px;
    background: linear-gradient(90deg, #f8faff, #f0f5ff);
    border-bottom: 1px solid #edf1fa;
    
    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #1a1c20;
    }
  }
  
  .comment-input-container {
    padding: 15px;
    border-bottom: 1px solid #edf1fa;
    
    .el-input {
      border-radius: 8px;
      font-size: 14px;
      
      :deep(.el-textarea__inner) {
        padding: 12px;
        border-color: #e0e5f0;
      }
    }
    
    .comment-submit {
      display: flex;
      justify-content: flex-end;
      margin-top: 12px;
      
      .el-button {
        padding: 8px 16px;
        font-size: 14px;
        font-weight: 500;
        border-radius: 6px;
      }
    }
  }
  
  .comment-list {
    padding: 0 15px;
    
    .comment-item {
      display: flex;
      padding: 12px 0;
      border-bottom: 1px solid #edf1fa;
      
      &:last-child {
        border-bottom: none;
      }
      
      .comment-avatar {
        margin-right: 12px;
      }
      
      .comment-content {
        flex: 1;
        
        .comment-header {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          
          .comment-author {
            font-weight: 600;
            color: #1a1c20;
            font-size: 14px;
          }
          
          .comment-time {
            font-size: 12px;
            color: #8e9aaf;
          }
        }
        
        .comment-text {
          background-color: #f8faff;
          padding: 10px;
          border-radius: 6px;
          margin-bottom: 8px;
          font-size: 14px;
          line-height: 1.5;
          color: #384860;
          
          &.deleted {
            color: #999;
            font-style: italic;
            background-color: #f5f5f5;
          }
          
          .deleted-text {
            margin: 0;
            color: #999;
            font-style: italic;
          }
          
          .reply-info {
            color: #8e9aaf;
            margin-bottom: 4px;
            font-size: 12px;
            
            .reply-name {
              color: #4e9fff;
              font-weight: 500;
            }
          }
          
          p {
            margin: 0;
          }
        }
        
        .comment-actions {
          display: flex;
          gap: 12px;
          
          .action-item {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 12px;
            color: #8e9aaf;
            cursor: pointer;
            transition: all 0.2s;
            
            &:hover {
              color: #4e9fff;
            }
            
            &.active {
              color: #4e9fff;
              font-weight: 500;
            }
            
            &.delete:hover {
              color: #f56c6c;
            }
          }
        }
        
        .reply-input {
          margin-top: 8px;
          background-color: #f8faff;
          padding: 10px;
          border-radius: 6px;
          
          .reply-submit {
            display: flex;
            justify-content: flex-end;
            margin-top: 8px;
            gap: 8px;
          }
        }
        
        .nested-comments {
          margin-top: 8px;
          
          .comment-item {
            padding: 8px 0;
            padding-left: 10px;
            border-bottom: 1px dashed #edf1fa;
            
            &:last-child {
              border-bottom: none;
              padding-bottom: 0;
            }
            
            .comment-avatar {
              margin-top: 2px;
            }
            
            .comment-text {
              padding: 8px;
              margin-bottom: 6px;
              font-size: 13px;
            }
            
            .comment-actions {
              gap: 10px;
              
              .action-item {
                font-size: 12px;
              }
            }
            
            &.deeper {
              margin-left: 5px;
              padding-left: 5px;
              border-left: 2px solid #eef0f5;
              
              .comment-text {
                padding: 6px;
                font-size: 12px;
                margin-bottom: 4px;
              }
              
              .comment-actions {
                gap: 8px;
                
                .action-item {
                  font-size: 11px;
                }
              }
              
              .reply-input {
                margin-top: 4px;
                padding: 8px;
              }
            }
          }
          
          &.deeper {
            margin-top: 6px;
            margin-left: 5px;
            padding-left: 5px;
            border-left: 2px solid #eef0f5;
          }
        }
      }
    }
  }
  
  .no-comments {
    padding: 15px;
  }
}

/* 响应式调整 */
@media (max-width: 992px) {
  .resource-detail-container {
    padding: 20px;
  }
  
  .resource-header {
    padding: 20px;
  }
  
  .resource-content {
    flex-direction: column;
  }
  
  .preview-container {
    height: 450px !important;
  }
}

@media (max-width: 768px) {
  .resource-detail-container {
    padding: 15px;
  }
  
  .resource-header {
    padding: 15px;
  }
  
  .resource-header .resource-title {
    font-size: 24px;
  }
  
  .preview-container {
    height: 350px !important;
  }
}

/* 评论区抽屉式展开样式 */
.comment-drawer-toggle {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #4e9fff;
  cursor: pointer;
  padding: 4px 0;
  margin-top: 4px;
  
  &:hover {
    text-decoration: underline;
  }
}

.comment-drawer-content {
  margin-left: 10px;
  padding-left: 10px;
  border-left: 2px solid #eef0f5;
}

.error-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.document-preview, .excel-preview, .code-preview, .audio-preview, .ppt-preview {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  overflow: hidden;
  max-width: 100%;
}

.image-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: auto;
  background-color: #f5f7fa;
}

.image-preview img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.video-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #000;
  overflow: hidden;
}

.video-player-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.video-title {
  color: white;
  font-size: 16px;
  padding: 10px;
  background-color: rgba(0, 0, 0, 0.5);
  width: 100%;
  text-align: center;
}

.video-player {
  width: 100%;
  height: calc(100% - 40px);
  object-fit: contain;
}

.ppt-preview {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}

/* 添加自定义滚动条样式，使其在各浏览器中表现一致 */
.code-content::-webkit-scrollbar,
.pdf-container::-webkit-scrollbar,
.image-preview::-webkit-scrollbar {
  height: 10px;
  width: 10px;
}

.code-content::-webkit-scrollbar-track,
.pdf-container::-webkit-scrollbar-track,
.image-preview::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.code-content::-webkit-scrollbar-thumb,
.pdf-container::-webkit-scrollbar-thumb,
.image-preview::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.code-content::-webkit-scrollbar-thumb:hover,
.pdf-container::-webkit-scrollbar-thumb:hover,
.image-preview::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 确保SQL和长文本内容可以水平滚动 */
pre {
  white-space: pre;
  overflow-x: auto;
  max-width: 100%;
  width: 100%;
  margin: 0;
  box-sizing: border-box;
}

/* 在全屏模式下调整滚动条的显示位置和大小 */
.fullscreen .code-content::-webkit-scrollbar,
.fullscreen .pdf-container::-webkit-scrollbar,
.fullscreen .image-preview::-webkit-scrollbar {
  height: 12px;
  width: 12px;
}

/* 为移动设备优化滚动体验 */
@media (max-width: 768px) {
  .code-content,
  .pdf-container,
  .image-preview {
    -webkit-overflow-scrolling: touch;
  }
  
  .code-content::-webkit-scrollbar,
  .pdf-container::-webkit-scrollbar,
  .image-preview::-webkit-scrollbar {
    height: 6px;
    width: 6px;
  }
}

.code-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: white;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  max-width: 100%;
}

.code-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background-color: #f6f8fa;
  border-bottom: 1px solid #e1e4e8;
  min-height: 42px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 5;
  width: 100%;
}

.code-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.code-language {
  font-size: 14px;
  color: #586069;
  font-weight: 600;
}

.scroll-indicator {
  font-size: 12px;
  color: #e6a23c;
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: rgba(230, 162, 60, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 1; }
  100% { opacity: 0.6; }
}

.code-content-wrapper {
  flex: 1;
  overflow: hidden;
  position: relative;
  width: 100%;
  height: calc(100% - 42px);
}

.code-content {
  margin: 0;
  padding: 16px;
  overflow: auto;
  background-color: white;
  border-radius: 0 0 6px 6px;
  white-space: pre;
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.code-content code {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  font-size: 14px;
  line-height: 1.5;
  tab-size: 4;
  white-space: pre;
  word-break: normal;
  word-wrap: normal;
  display: block;  /* 确保代码块为块级元素 */
  color: #333;     /* 确保默认颜色可见 */
}

.audio-player-container {
  width: 100%;
  max-width: 600px;
  padding: 30px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
}

.audio-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
  text-align: center;
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.audio-player {
  width: 100%;
  margin-bottom: 15px;
}

.preview-loading, .preview-error {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.9);
  z-index: 10;
}

.preview-loading p {
  margin-top: 16px;
  color: #606266;
}

/* 添加额外的样式覆盖highlight.js可能造成的布局问题 */
.hljs {
  background: transparent !important;
  padding: 0 !important;
  max-width: none !important;
  overflow: visible !important;
}

.csv-preview {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  overflow: hidden;
  max-width: 100%;
}

.csv-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: white;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  max-width: 100%;
}

.csv-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background-color: #f6f8fa;
  border-bottom: 1px solid #e1e4e8;
  min-height: 42px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 5;
  width: 100%;
}

.csv-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.csv-file-name {
  font-size: 14px;
  color: #586069;
  font-weight: 600;
}

.csv-content-wrapper {
  flex: 1;
  overflow: hidden;
  position: relative;
  width: 100%;
  height: calc(100% - 42px);
}

.csv-content {
  margin: 0;
  padding: 0;
  overflow: auto;
  background-color: white;
  border-radius: 0 0 6px 6px;
  width: 100%;
  height: 100%;
}

/* 修复代码高亮样式 */
:deep(.hljs) {
  background: transparent !important;
  padding: 0 !important;
  overflow: visible !important;
}

:deep(.hljs-keyword),
:deep(.hljs-selector-tag),
:deep(.hljs-title),
:deep(.hljs-section),
:deep(.hljs-doctag),
:deep(.hljs-name),
:deep(.hljs-strong) {
  font-weight: bold;
  color: #0550ae;
}

:deep(.hljs-comment) {
  color: #6a737d;
}

:deep(.hljs-string),
:deep(.hljs-title),
:deep(.hljs-section),
:deep(.hljs-built_in),
:deep(.hljs-literal),
:deep(.hljs-type),
:deep(.hljs-addition),
:deep(.hljs-tag),
:deep(.hljs-quote),
:deep(.hljs-name),
:deep(.hljs-selector-id),
:deep(.hljs-selector-class) {
  color: #032f62;
}

:deep(.hljs-attr),
:deep(.hljs-variable),
:deep(.hljs-template-variable),
:deep(.hljs-number),
:deep(.hljs-symbol) {
  color: #005cc5;
}

:deep(.hljs-function) {
  color: #6f42c1;
}

:deep(.hljs-keyword),
:deep(.hljs-selector-tag) {
  color: #d73a49;
}

:deep(.hljs-literal) {
  color: #0086b3;
}

:deep(.hljs-meta) {
  color: #6a737d;
}

:deep(.hljs-emphasis) {
  font-style: italic;
}
</style>