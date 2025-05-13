import dayjs from "dayjs";

// 站点基本信息接口
export interface SiteBasicInfo {
  id: number;
  site_name: string;
  site_logo: string;
  site_favicon: string;
  site_description: string;
  site_keywords: string;
  site_copyright: string;
  icp_number: string;
  public_security_record: string;
  technical_support: string;
  create_time: string;
  update_time: string;
}

// SEO设置接口
export interface SeoSetting {
  id: number;
  page_name: string;
  page_title: string;
  meta_keywords: string;
  meta_description: string;
  create_time: string;
  update_time: string;
}

// 上传配置接口
export interface UploadSetting {
  id: number;
  storage_type: string; // local, oss, cos, qiniu
  bucket_name: string;
  region: string;
  domain: string;
  access_key: string;
  secret_key: string;
  max_size: number; // MB
  allowed_types: string;
  create_time: string;
  update_time: string;
}

// 学院数据接口
export interface College {
  id: number;
  name: string;
  cover_url: string;
  logo_url: string;
  create_time: string;
  update_time: string;
  is_deleted: number;
}

// 站点数据结构
export interface SiteData {
  id: number;
  name: string;
  code: string;
  domain: string;
  logo: string;
  description: string;
  status: number; // 1-启用；0-禁用
  sort: number;
  create_time: string;
  update_time?: string;
}

// 站点状态选项
export const SITE_STATUS_OPTIONS = [
  { label: "启用", value: 1 },
  { label: "禁用", value: 0 },
];

// 模拟站点基本信息数据
export const MOCK_SITE_BASIC_INFO: SiteBasicInfo = {
  id: 1,
  site_name: "学术研究资源共享平台",
  site_logo: "https://study-cdn-img.jobpi.cn/upload/2025-04-15/logo.png",
  site_favicon: "https://study-cdn-img.jobpi.cn/upload/2025-04-15/favicon.ico",
  site_description: "高校学术资源共享与知识传播平台",
  site_keywords: "学术资源,高校,研究,知识共享,学习资料",
  site_copyright: "© 2025 学术研究资源共享平台 版权所有",
  icp_number: "京ICP备12345678号-1",
  public_security_record: "京公网安备11010802030294号",
  technical_support: "技术支持：XXX科技有限公司",
  create_time: "2025-04-15 09:00:00",
  update_time: "2025-05-01 10:30:15",
};

// 模拟SEO设置数据
export const MOCK_SEO_SETTINGS: SeoSetting[] = [
  {
    id: 1,
    page_name: "首页",
    page_title: "学术研究资源共享平台 - 高校学术资源共享与知识传播平台",
    meta_keywords: "学术资源,高校,研究,知识共享,学习资料,首页",
    meta_description: "学术研究资源共享平台是服务于全国高校师生的学术资源共享与知识传播平台，提供海量优质学习资源。",
    create_time: "2025-04-15 09:00:00",
    update_time: "2025-04-15 09:00:00",
  },
  {
    id: 2,
    page_name: "资源中心",
    page_title: "资源中心 - 学术研究资源共享平台",
    meta_keywords: "学术资源,资源中心,学习资料,课件,论文",
    meta_description: "资源中心提供各学科各专业的学术资源，包括课件、论文、学习笔记等内容。",
    create_time: "2025-04-15 09:00:00",
    update_time: "2025-04-15 09:00:00",
  },
  {
    id: 3,
    page_name: "学院专区",
    page_title: "学院专区 - 学术研究资源共享平台",
    meta_keywords: "学院专区,各学院资源,专业资源",
    meta_description: "学院专区提供各学院的专业学习资源，为不同学科背景的师生提供专业服务。",
    create_time: "2025-04-15 09:00:00",
    update_time: "2025-04-15 09:00:00",
  },
  {
    id: 4,
    page_name: "资讯动态",
    page_title: "资讯动态 - 学术研究资源共享平台",
    meta_keywords: "资讯动态,学术新闻,研究动态",
    meta_description: "资讯动态提供最新的学术新闻、研究动态及平台更新信息。",
    create_time: "2025-04-15 09:00:00",
    update_time: "2025-04-15 09:00:00",
  },
  {
    id: 5,
    page_name: "关于我们",
    page_title: "关于我们 - 学术研究资源共享平台",
    meta_keywords: "关于我们,平台介绍,联系方式",
    meta_description: "了解学术研究资源共享平台的发展历程、团队介绍及联系方式。",
    create_time: "2025-04-15 09:00:00",
    update_time: "2025-04-15 09:00:00",
  },
];

// 模拟上传配置数据
export const MOCK_UPLOAD_SETTING: UploadSetting = {
  id: 1,
  storage_type: "oss",
  bucket_name: "study-resources",
  region: "oss-cn-beijing",
  domain: "https://study-cdn-img.jobpi.cn",
  access_key: "LTAI5tABCDEFG1234567",
  secret_key: "******************************",
  max_size: 100,
  allowed_types: "jpg,jpeg,png,gif,doc,docx,ppt,pptx,xls,xlsx,pdf,zip,rar",
  create_time: "2025-04-15 09:00:00",
  update_time: "2025-04-30 15:20:40",
};

// 存储类型选项
export const STORAGE_TYPE_OPTIONS = [
  { label: "本地存储", value: "local" },
  { label: "阿里云OSS", value: "oss" },
  { label: "腾讯云COS", value: "cos" },
  { label: "七牛云存储", value: "qiniu" },
];

// 模拟站点数据
export const MOCK_SITE_DATA: SiteData[] = [
  {
    id: 1,
    name: "主站",
    code: "main",
    domain: "www.auditresearch.org",
    logo: "/logo/main.png",
    description: "审核研究中心主站",
    status: 1,
    sort: 1,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 2,
    name: "研究平台",
    code: "research",
    domain: "research.auditresearch.org",
    logo: "/logo/research.png",
    description: "研究数据平台",
    status: 1,
    sort: 2,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 3,
    name: "学术资源",
    code: "academic",
    domain: "academic.auditresearch.org",
    logo: "/logo/academic.png",
    description: "学术资源共享平台",
    status: 1,
    sort: 3,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 4,
    name: "培训中心",
    code: "training",
    domain: "training.auditresearch.org",
    logo: "/logo/training.png",
    description: "在线培训平台",
    status: 1,
    sort: 4,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 5,
    name: "测试站点",
    code: "test",
    domain: "test.auditresearch.org",
    logo: "/logo/test.png",
    description: "测试用途站点",
    status: 0,
    sort: 5,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
];
