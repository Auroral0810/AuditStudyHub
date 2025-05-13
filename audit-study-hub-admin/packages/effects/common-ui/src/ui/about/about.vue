<script setup lang="ts">
import type { AboutProps, DescriptionItem } from './about';

import { h } from 'vue';

import { VbenRenderContent } from '@vben-core/shadcn-ui';

import { Page } from '../../components';

interface Props extends AboutProps {}

defineOptions({
  name: 'AboutUI',
});

withDefaults(defineProps<Props>(), {
  description:
    '是一个专为高校学生设计的资源共享平台，旨在解决学生在学习过程中获取专业资料和教学资源的痛点。平台集中了各个专业的学习资料，帮助学生高效获取所需的学习材料。我们相信知识共享的力量，希望能够促进校园学习资源的有效流通，提高学习效率。',
  name: '审学汇',
  title: '关于审学汇',
});

declare global {
  const __VBEN_ADMIN_METADATA__: {
    authorEmail: string;
    authorName: string;
    authorUrl: string;
    buildTime: string;
    dependencies: Record<string, string>;
    description: string;
    devDependencies: Record<string, string>;
    homepage: string;
    license: string;
    repositoryUrl: string;
    version: string;
  };
}

const renderLink = (href: string, text: string) =>
  h(
    'a',
    { href, target: '_blank', class: 'vben-link hover:underline text-primary' },
    { default: () => text },
  );

const {
  buildTime,
  dependencies = {},
  devDependencies = {},
  license,
  version,
} = __VBEN_ADMIN_METADATA__ || {};

const auditStudyHubInfoItems: (DescriptionItem & { colSpan?: number })[] = [
  {
    title: '项目名称',
    content: '审学汇 - 校园资料共享平台',
  },
  {
    title: '版本号',
    content: version || '1.0.0',
  },
  {
    title: '项目描述',
    content: '审学汇是一个专为高校学生设计的资源共享平台，旨在解决学生在学习过程中获取专业资料和教学资源的痛点。平台集中了各个专业的学习资料，包括课件PPT、学习笔记、试卷资料、电子书籍、课程视频和实验报告等多种类型的资源，帮助学生高效获取所需的学习材料。我们相信知识共享的力量，通过搭建这个平台，希望能够促进校园学习资源的有效流通，提高学习效率，让每一位学生都能便捷地获取优质学习资源。',
    colSpan: 4,
  },
  {
    title: 'GitHub仓库',
    content: renderLink('https://github.com/Auroral0810/AuditStudyHub', 'https://github.com/Auroral0810/AuditStudyHub'),
  },
  {
    title: '文档地址',
    content: renderLink('https://github.com/Auroral0810/AuditStudyHub/blob/main/README.md', '查看文档 (GitHub README)'),
  },
  {
    title: '作者',
    content: 'Auroral0810',
  },
  {
    title: '联系邮箱',
    content: renderLink('mailto:15968588744@163.com', '15968588744@163.com'),
  },
  {
    title: '联系QQ',
    content: '1957689514',
  },
  {
    title: '最后构建时间',
    content: buildTime || 'N/A',
  },
   {
    title: '开源许可协议',
    content: license || 'MIT',
  },
];

const dependenciesItems = Object.keys(dependencies).map((key) => ({
  content: dependencies[key],
  title: key,
}));

const devDependenciesItems = Object.keys(devDependencies).map((key) => ({
  content: devDependencies[key],
  title: key,
}));
</script>

<template>
  <Page :title="title">
    <template #description>
      <p class="text-foreground mt-3 text-sm leading-6">
        <a :href="'https://github.com/Auroral0810/AuditStudyHub'" class="vben-link font-semibold hover:underline text-primary" target="_blank">
          {{ name }}
        </a>
        {{ description }}
      </p>
    </template>
    <div class="card-box p-5">
      <div>
        <h5 class="text-foreground text-lg font-semibold">项目信息</h5>
      </div>
      <div class="mt-4">
        <dl class="grid grid-cols-1 gap-x-6 gap-y-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          <template v-for="item in auditStudyHubInfoItems" :key="item.title">
            <div
              class="border-border border-t px-2 py-4 sm:px-0"
              :class="[
                item.colSpan && item.colSpan >=4 ? 'md:col-span-2 lg:col-span-3 xl:col-span-4' :
                item.colSpan && item.colSpan >=3 ? 'md:col-span-2 lg:col-span-3' :
                item.colSpan && item.colSpan >=2 ? 'md:col-span-2' :
                'sm:col-span-1',
              ]"
            >
              <dt class="text-foreground text-sm font-medium leading-6">
                {{ item.title }}
              </dt>
              <dd class="text-foreground/90 mt-1 text-sm leading-6 sm:mt-2">
                <VbenRenderContent :content="item.content" />
              </dd>
            </div>
          </template>
        </dl>
      </div>
    </div>

    <div class="card-box mt-6 p-5">
      <div>
        <h5 class="text-foreground text-lg font-semibold">生产环境依赖</h5>
      </div>
      <div class="mt-4">
        <dl class="grid grid-cols-2 gap-x-6 gap-y-1 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
          <template v-for="item in dependenciesItems" :key="item.title">
            <div class="border-border border-t px-2 py-3 sm:col-span-1 sm:px-0">
              <dt class="text-foreground text-sm">
                {{ item.title }}
              </dt>
              <dd class="text-foreground/80 mt-1 text-xs sm:mt-2">
                <VbenRenderContent :content="item.content" />
              </dd>
            </div>
          </template>
        </dl>
      </div>
    </div>
    <div class="card-box mt-6 p-5">
      <div>
        <h5 class="text-foreground text-lg font-semibold">开发环境依赖</h5>
      </div>
      <div class="mt-4">
        <dl class="grid grid-cols-2 gap-x-6 gap-y-1 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
          <template v-for="item in devDependenciesItems" :key="item.title">
            <div class="border-border border-t px-2 py-3 sm:col-span-1 sm:px-0">
              <dt class="text-foreground text-sm">
                {{ item.title }}
              </dt>
              <dd class="text-foreground/80 mt-1 text-xs sm:mt-2">
                <VbenRenderContent :content="item.content" />
              </dd>
            </div>
          </template>
        </dl>
      </div>
    </div>
  </Page>
</template>
