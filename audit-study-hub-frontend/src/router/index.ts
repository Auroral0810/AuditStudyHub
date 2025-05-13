import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('../layout/index.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/home/index.vue'),
        meta: { title: '首页', icon: 'House' }
      },
      {
        path: 'resource',
        name: 'Resource',
        component: () => import('../views/resource/index.vue'),
        meta: { title: '资源中心', icon: 'Files' }
      },
      {
        path: '/resource/detail/:id',
        name: 'ResourceDetail',
        component: () => import('../views/resource/detail.vue'),
        meta: {
          title: '资源详情',
          requiresAuth: false
        }
      },
      {
        path: '/resource/upload',
        name: 'ResourceUpload',
        component: () => import('../views/resource/upload.vue'),
        meta: {
          title: '上传资料',
          requiresAuth: true
        }
      },
      {
        path: '/resource/edit/:id',
        name: 'ResourceEdit',
        component: () => import('../views/resource/edit.vue'),
        meta: {
          requiresAuth: true,
          title: '编辑资源'
        }
      },
      {
        path: 'resource-request',
        name: 'ResourceRequest',
        component: () => import('../views/resource-request/index.vue'),
        meta: { title: '资源需求', icon: 'ChatDotRound' }
      },
      {
        path: 'resource-request/:id',
        name: 'ResourceRequestDetail',
        component: () => import('../views/resource-request/detail.vue'),
        meta: { title: '需求详情', hidden: true }
      },
      {
        path: 'resource-request/edit/:id',
        name: 'ResourceRequestEdit',
        component: () => import('../views/resource-request/edit.vue'),
        meta: { title: '编辑需求', hidden: true, requiresAuth: true }
      },
      {
        path: 'about',
        name: 'About',
        component: () => import('../views/about/index.vue'),
        meta: { title: '关于', icon: 'InfoFilled' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'User' },
        redirect: '/profile/info',
        children: [
          {
            path: 'info',
            name: 'UserInfo',
            component: () => import('../views/profile/info.vue'),
            meta: { title: '个人资料' }
          },
          {
            path: 'uploads',
            name: 'UserUploads',
            component: () => import('../views/profile/uploads.vue'),
            meta: { title: '我的上传' }
          },
          {
            path: 'downloads',
            name: 'UserDownloads',
            component: () => import('../views/profile/downloads.vue'),
            meta: { title: '下载历史' }
          },
          {
            path: 'collections',
            name: 'UserCollections',
            component: () => import('../views/profile/collections.vue'),
            meta: { title: '我的收藏' }
          },
          {
            path: 'likes',
            name: 'UserLikes',
            component: () => import('../views/profile/likes.vue'),
            meta: { title: '我的点赞' }
          },
          {
            path: 'myrequests',
            name: 'UserRequests',
            component: () => import('../views/profile/myrequests.vue'),
            meta: { title: '我的请求' }
          }
        ]
      },
      {
        path: 'nau-view',
        name: 'NAUView',
        component: () => import('../views/nau-view/index.vue'),
        meta: { title: '南审印象', icon: 'Picture' }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/register/index.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forget-password',
    name: 'ForgetPassword',
    component: () => import('../views/forget-password/index.vue'),
    meta: { title: '忘记密码' }
  },
  {
    path: '/terms',
    name: 'Terms',
    component: () => import('@/views/terms/index.vue'),
    meta: {
      title: '使用条款'
    }
  },
  {
    path: '/privacy',
    name: 'Privacy',
    component: () => import('@/views/privacy/index.vue'),
    meta: {
      title: '隐私政策'
    }
  },
  {
    path: '/contact',
    name: 'Contact',
    component: () => import('@/views/contact/index.vue'),
    meta: {
      title: '联系我们'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/error/404.vue'),
    meta: { title: '404', hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title as string || '首页'} - 校园资料共享平台`
  
  // 获取token和用户信息
  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')
  const userInfo = userInfoStr ? JSON.parse(userInfoStr) : null

  // 处理token无效的情况（例如token过期或被篡改）
  if (token && !userInfo) {
    // token存在但用户信息不存在，清除token并重定向到登录页
    localStorage.removeItem('token')
    if (to.meta.requiresAuth || to.meta.requiresAdmin) {
      return next('/')
    }
  }
  
  // 需要管理员权限的页面
  if (to.meta.requiresAdmin) {
    if (!token) {
      return next('/login')
    } else {
      if (userInfo && userInfo.role === 'admin') {
        return next()
      } else {
        return next('/403')
      }
    }
  } 
  // 需要登录权限的页面
  else if (to.meta.requiresAuth) {
    if (!token) {
      return next('/login')
    } else {
      return next()
    }
  }
  // 登录和注册页面，已登录用户直接跳转到首页
  else if (to.path === '/login' || to.path === '/register' || to.path === '/forget-password') {
    if (token) {
      return next('/')
    } else {
      return next()
    }
  } 
  else {
    return next()
  }
})

export default router 