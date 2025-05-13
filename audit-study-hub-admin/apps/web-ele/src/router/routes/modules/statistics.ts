import type { RouteRecordRaw } from "vue-router";

import { $t } from "#/locales";

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: "lucide:line-chart",
      order: 400,
      title: $t("page.statistics.title", { fallback: "统计分析" }),
    },
    name: "Statistics",
    path: "/statistics",
    children: [
      {
        path: "/statistics/hot-search",
        name: "StatisticsHotSearch",
        meta: {
          icon: "lucide:search",
          title: $t("page.statistics.hotSearch.title", { fallback: "热门搜索" }),
        },
        component: () => import("#/views/statistics/hot-search.vue"),
      },
      {
        path: "/statistics/popular-search",
        name: "StatisticsPopularSearch",
        meta: {
          icon: "lucide:trending-up",
          title: $t("page.statistics.popularSearch.title", { fallback: "流行搜索" }),
        },
        component: () => import("#/views/statistics/popular-search.vue"),
      },
      {
        path: "/statistics/user-behavior",
        name: "StatisticsUserBehavior",
        meta: {
          icon: "lucide:activity",
          title: $t("page.statistics.userBehavior.title", { fallback: "用户行为分析" }),
        },
        component: () => import("#/views/statistics/user-behavior.vue"),
      },
    ],
  },
];

export default routes; 