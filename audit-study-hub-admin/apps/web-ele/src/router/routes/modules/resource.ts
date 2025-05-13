import type { RouteRecordRaw } from 'vue-router';

import { $t } from '#/locales';
const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'lucide:file-archive',
      order: 200,
      title: $t('page.resource.title', { fallback: '资源管理' }),
    },
    name: 'Resource',
    path: '/resource',
    children: [
      {
        path: '/resource/list',
        name: 'ResourceList',
        meta: {
          icon: 'lucide:files',
          title: $t('page.resource.list.title', { fallback: '资源列表' }),
        },
        component: () => import('#/views/resource/list.vue'),
      },
      {
        path: '/resource/audit',
        name: 'ResourceAudit',
        meta: {
          icon: 'lucide:file-check',
          title: $t('page.resource.audit.title', { fallback: '资源审核' }),
        },
        component: () => import('#/views/resource/audit.vue'),
      },
      {
        path: '/resource/comment',
        name: 'ResourceComment',
        meta: {
          icon: 'lucide:message-square',
          title: $t('page.resource.comment.title', { fallback: '资源评论' }),
        },
        component: () => import('#/views/resource/comment.vue'),
      },
      {
        path: '/resource/request',
        name: 'ResourceRequest',
        meta: {
          icon: 'lucide:file-question',
          title: $t('page.resource.request.title', { fallback: '资源请求' }),
        },
        component: () => import('#/views/resource/request.vue'),
      },
      {
        path: '/resource/request-reply',
        name: 'ResourceRequestReply',
        meta: {
          icon: 'lucide:message-circle',
          title: $t('page.resource.request.reply.title', { fallback: '请求回复' }),
        },
        component: () => import('#/views/resource/request-reply.vue'),
      },
    ],
  },
];

export default routes; 