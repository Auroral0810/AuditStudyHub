<template>
  <div class="nested-comments">
    <!-- 一级子评论 -->
    <div 
      v-for="comment in comments" 
      :key="comment.id" 
      class="comment-item nested"
    >
      <div class="comment-avatar">
        <el-avatar :size="30" :src="comment.userAvatar || defaultAvatar"></el-avatar>
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
          <span class="action-item" @click="handleShowReply(rootComment, comment)">
            <el-icon><ChatDotRound /></el-icon> 回复
          </span>
          <span 
            class="action-item" 
            :class="{ 'active': comment.liked }"
            @click="handleLike(comment)"
          >
            <el-icon><Star /></el-icon> 
            {{ comment.liked ? '已点赞' : '点赞' }}
            <span v-if="comment.likes">({{ comment.likes }})</span>
          </span>
          <span 
            v-if="isCommentOwner(comment)" 
            class="action-item delete" 
            @click="handleDelete(comment)"
          >
            <el-icon><Delete /></el-icon> 删除
          </span>
        </div>
        
        <div v-if="replyingTo === comment.id && replyingToChild" class="reply-input">
          <el-input
            v-model="replyContentModel"
            type="textarea"
            :rows="2"
            placeholder="回复评论..."
            maxlength="500"
            show-word-limit
          ></el-input>
          <div class="reply-submit">
            <el-button size="small" @click="handleCancelReply">取消</el-button>
            <el-button 
              type="primary" 
              size="small"
              :disabled="!replyContentModel.trim()" 
              :loading="submittingComment"
              @click="handleSubmitReply(rootComment, comment)"
            >
              回复
            </el-button>
          </div>
        </div>
        
        <!-- 处理子评论的子评论（深层级评论） -->
        <template v-if="comment.children && comment.children.length > 0">
          <!-- 超过两层的评论显示抽屉控件 -->
          <div 
            v-if="!expandedComments[comment.id]" 
            class="comment-drawer-toggle"
            @click="expandComment(comment.id)"
          >
            <el-icon><Plus /></el-icon>
            查看{{ comment.children.length }}条回复
          </div>
          
          <!-- 抽屉内容 -->
          <div v-if="expandedComments[comment.id]" class="comment-drawer-content">
            <!-- 递归调用当前组件，实现无限层级嵌套 -->
            <CommentNested
              :comments="comment.children"
              :root-comment="rootComment"
              :replying-to="replyingTo"
              :replying-to-child="replyingToChild"
              :reply-content="replyContent"
              :submitting-comment="submittingComment"
              :level="level + 1"
              @show-reply="handleShowReply"
              @cancel-reply="handleCancelReply"
              @submit-reply="handleSubmitReply"
              @like-comment="handleLike"
              @delete-comment="handleDelete"
              @update:reply-content="val => emit('update:reply-content', val)"
            />
            
            <!-- 收起抽屉 -->
            <div 
              class="comment-drawer-toggle"
              @click="collapseComment(comment.id)"
            >
              <el-icon><Minus /></el-icon>
              收起回复
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, defineProps, defineEmits, reactive } from 'vue'
import dayjs from 'dayjs'
import { useUserStore } from '../stores/user'
import type { Comment } from '../stores/resource'
import { ChatDotRound, Star, Delete, Plus, Minus } from '@element-plus/icons-vue'

const userStore = useUserStore()

const props = defineProps({
  comments: {
    type: Array as () => Comment[],
    required: true
  },
  rootComment: {
    type: Object as () => Comment,
    required: true
  },
  replyingTo: {
    type: Number,
    default: null
  },
  replyingToChild: {
    type: Boolean,
    default: false
  },
  replyContent: {
    type: String,
    default: ''
  },
  submittingComment: {
    type: Boolean,
    default: false
  },
  level: {
    type: Number,
    default: 1
  },
  defaultAvatar: {
    type: String,
    default: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
})

const emit = defineEmits([
  'show-reply',
  'cancel-reply',
  'submit-reply',
  'like-comment',
  'delete-comment',
  'update:reply-content'
])

// 跟踪展开的评论
const expandedComments = reactive<Record<number, boolean>>({})

// 展开评论抽屉
const expandComment = (commentId: number) => {
  expandedComments[commentId] = true
}

// 收起评论抽屉
const collapseComment = (commentId: number) => {
  expandedComments[commentId] = false
}

// 同步props中的replyContent到本地
const replyContentModel = computed({
  get: () => props.replyContent,
  set: (value) => {
    emit('update:reply-content', value)
  }
})

// 格式化日期
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

// 判断是否是评论所有者
const isCommentOwner = (comment: Comment) => {
  return userStore.userInfo && userStore.userInfo.id === comment.userId
}

// 处理回复按钮点击
const handleShowReply = (rootComment: Comment, childComment: Comment) => {
  emit('show-reply', rootComment, childComment)
}

// 处理取消回复
const handleCancelReply = () => {
  emit('cancel-reply')
}

// 处理提交回复
const handleSubmitReply = (rootComment: Comment, childComment: Comment) => {
  emit('submit-reply', rootComment, childComment)
}

// 处理点赞
const handleLike = (comment: Comment) => {
  emit('like-comment', comment)
}

// 处理删除
const handleDelete = (comment: Comment) => {
  emit('delete-comment', comment)
}
</script>

<style lang="scss" scoped>
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
    
    .comment-actions {
      display: flex;
      gap: 10px;
      
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
  }
}

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
  margin-left: 5px;
  padding-left: 10px;
  border-left: 2px solid #eef0f5;
  margin-top: 6px;
}
</style> 