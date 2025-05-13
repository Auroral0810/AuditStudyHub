import type { RouteRecordRaw } from "vue-router";

import { $t } from "#/locales";


const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: "lucide:layout-grid",
      order: 300,
      title: $t("page.content.title", { fallback: "内容管理" }),
    },
    name: "Content",
    path: "/content",
    children: [
      {
        path: "/content/category",
        name: "ContentCategory",
        meta: {
          icon: "lucide:bookmark",
          title: $t("page.content.category.title", { fallback: "分类管理" }),
        },
        component: () => import("#/views/content/category.vue"),
      },
      {
        path: "/content/college",
        name: "ContentCollege",
        meta: {
          icon: "lucide:building",
          title: $t("page.content.college.title", { fallback: "学院管理" }),
        },
        component: () => import("#/views/content/college.vue"),
      },
      {
        path: "/content/major",
        name: "ContentMajor",
        meta: {
          icon: "lucide:book-open",
          title: $t("page.content.major.title", { fallback: "专业管理" }),
        },
        component: () => import("#/views/content/major.vue"),
      },
      {
        path: "/content/course",
        name: "ContentCourse",
        meta: {
          icon: "lucide:book-type",
          title: $t("page.content.course.title", { fallback: "课程管理" }),
        },
        component: () => import("#/views/content/course.vue"),
      },
    ],
  },
];

export default routes; 