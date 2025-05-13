import type { RouteRecordRaw } from 'vue-router';

import { $t } from '#/locales';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'mdi:account-group',
      order: 100,
      title: $t('page.user.title', { fallback: '用户管理' }),
    },
    name: 'User',
    path: '/user',
    children: [
      {
        path: '/user/list',
        name: 'UserList',
        meta: {
          icon: 'mdi:account-multiple',
          title: $t('page.user.list.title', { fallback: '用户列表' }),
        },
        component: () => import('#/views/user/user.vue'),
      },
      {
        path: '/user/role',
        name: 'UserRole',
        meta: {
          icon: 'mdi:shield-account',
          title: $t('page.user.role.title', { fallback: '用户角色' }),
        },
        component: () => import('#/views/user/role.vue'),
      },
      {
        path: '/user/records',
        name: 'UserRecords',
        meta: {
          icon: 'mdi:history',
          title: $t('page.user.records.title', { fallback: '用户活动记录' }),
        },
        component: () => import('#/views/user/user-records.vue'),
      },
    ],
  },
];
export default routes; 