import type { RouteRecordRaw } from "vue-router";

import { $t } from "#/locales";

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: "lucide:settings",
      order: 500,
      title: $t("page.system.title", { fallback: "系统管理" }),
    },
    name: "System",
    path: "/system",
    children: [
      {
        path: "/system/site",
        name: "SystemSite",
        meta: {
          icon: "lucide:monitor",
          title: $t("page.system.site.title", { fallback: "站点管理" }),
        },
        component: () => import("#/views/system/site.vue"),
      },
      {
        path: "/system/permission",
        name: "SystemPermission",
        meta: {
          icon: "lucide:shield",
          title: $t("page.system.permission.title", { fallback: "权限管理" }),
        },
        component: () => import("#/views/system/permission.vue"),
      },
      {
        path: "/system/interface",
        name: "SystemInterface",
        meta: {
          icon: "lucide:plug",
          title: $t("page.system.interface.title", { fallback: "接口管理" }),
        },
        component: () => import("#/views/system/api-interface.vue"),
      },
      {
        path: "/system/dictionary",
        name: "SystemDictionary",
        meta: {
          icon: "lucide:book",
          title: $t("page.system.dictionary.title", { fallback: "数据字典" }),
        },
        component: () => import("#/views/system/dictionary.vue"),
      },
      {
        path: "/system/logs",
        name: "SystemLogs",
        meta: {
          icon: "lucide:file-text",
          title: $t("page.system.logs.title", { fallback: "日志管理" }),
        },
        component: () => import("#/views/system/logs.vue"),
      },
      {
        path: "/system/scheduler",
        name: "SystemScheduler",
        meta: {
          icon: "lucide:clock",
          title: $t("page.system.scheduler.title", { fallback: "定时任务" }),
        },
        component: () => import("#/views/system/scheduler.vue"),
      }
    ],
  },
];

export default routes;