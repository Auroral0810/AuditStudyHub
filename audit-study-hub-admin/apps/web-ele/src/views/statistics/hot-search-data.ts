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

// 模拟学院数据
// TODO: 之后需要从后端获取
export const MOCK_COLLEGE_DATA: College[] = [
{
    id: 1729,
    name: "法学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2023-11-13/16998694487204.png",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418493459303.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1730,
    name: "外国语学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2024-03-08/17098833300445.jpeg",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/1741849392578.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1731,
    name: "文学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2023-11-13/16998693057441.png",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418493825033.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1732,
    name: "经济学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2024-03-08/17098829887454.jpeg",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418493158634.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1733,
    name: "金融学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2024-03-08/17098830258518.jpeg",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418493244163.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1734,
    name: "商学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2024-03-08/17098829258799.jpeg",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418493065625.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1735,
    name: "会计学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2023-11-13/16998698689887.png",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418492966123.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1736,
    name: "政府审计学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2024-03-08/1709883583859.jpeg",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418492381115.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1737,
    name: "内部审计学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2023-11-13/16998702050972.png",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418492467777.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1738,
    name: "社会审计学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2024-03-08/17098828168824.jpeg",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418492560426.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1739,
    name: "中审学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2024-03-08/17098828789278.jpeg",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418492801786.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1740,
    name: "工程审计学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2023-11-13/1699870688591.png",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418492880473.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1741,
    name: "公共管理学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2023-11-13/16998709047157.png",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2023-11-13/16998688536404.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1742,
    name: "金融学院（经济与金融研究院）",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2023-11-13/16998711119542.png",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418493338104.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1743,
    name: "统计与数据科学学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2023-11-13/16998712282212.png",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418493553287.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1744,
    name: "计算机学院/智能审计学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2024-03-08/17098832827106.jpeg",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418493648596.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1745,
    name: "数学学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2023-11-13/16998717456491.png",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418493732635.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
{
    id: 1746,
    name: "国际联合审计学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2024-03-08/17098833828341.jpeg",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418494017267.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
},
];

// 热门搜索数据接口
export interface HotSearch {
  id: number;
  keyword: string;
  weight: number;
  daily_count: number;
  weekly_count: number;
  monthly_count: number;
  is_manual: number;
  create_time: string;
  update_time: string;
  is_deleted: number;
  creator_id?: number;
  creator_name?: string;
}

// 状态选项
export const STATUS_OPTIONS = [
  { label: "下线", value: 0 },
  { label: "上线", value: 1 },
];

// 是否手动添加选项
export const MANUAL_OPTIONS = [
  { label: "自动统计", value: 0 },
  { label: "手动添加", value: 1 },
];

// 模拟热门搜索数据
// TODO: 之后需要从后端获取
export const MOCK_HOT_SEARCH_DATA: HotSearch[] = [
  {
    id: 1,
    keyword: "高等数学",
    weight: 100,
    daily_count: 245,
    weekly_count: 1540,
    monthly_count: 5800,
    is_manual: 0,
    create_time: "2025-05-01 08:30:15",
    update_time: "2025-05-10 08:30:15",
    is_deleted: 0,
  },
  {
    id: 2,
    keyword: "大学英语四级",
    weight: 95,
    daily_count: 198,
    weekly_count: 1320,
    monthly_count: 4900,
    is_manual: 0,
    create_time: "2025-05-01 09:15:42",
    update_time: "2025-05-10 09:15:42",
    is_deleted: 0,
  },
  {
    id: 3,
    keyword: "Java程序设计",
    weight: 90,
    daily_count: 176,
    weekly_count: 1250,
    monthly_count: 4500,
    is_manual: 0,
    create_time: "2025-05-01 10:25:38",
    update_time: "2025-05-10 10:25:38",
    is_deleted: 0,
  },
  {
    id: 4,
    keyword: "数据结构与算法",
    weight: 88,
    daily_count: 165,
    weekly_count: 1150,
    monthly_count: 4200,
    is_manual: 1,
    create_time: "2025-05-01 11:05:22",
    update_time: "2025-05-10 11:05:22",
    is_deleted: 0,
    creator_id: 1001,
    creator_name: "管理员A",
  },
  {
    id: 5,
    keyword: "计算机网络",
    weight: 85,
    daily_count: 143,
    weekly_count: 980,
    monthly_count: 3800,
    is_manual: 0,
    create_time: "2025-05-01 13:42:19",
    update_time: "2025-05-10 13:42:19",
    is_deleted: 0,
  },
  {
    id: 6,
    keyword: "操作系统原理",
    weight: 82,
    daily_count: 135,
    weekly_count: 920,
    monthly_count: 3500,
    is_manual: 1,
    create_time: "2025-05-01 14:36:55",
    update_time: "2025-05-10 14:36:55",
    is_deleted: 0,
    creator_id: 1002,
    creator_name: "管理员B",
  },
  {
    id: 7,
    keyword: "数据库系统",
    weight: 80,
    daily_count: 124,
    weekly_count: 880,
    monthly_count: 3300,
    is_manual: 0,
    create_time: "2025-05-01 15:18:33",
    update_time: "2025-05-10 15:18:33",
    is_deleted: 0,
  },
  {
    id: 8,
    keyword: "软件工程",
    weight: 78,
    daily_count: 118,
    weekly_count: 850,
    monthly_count: 3100,
    is_manual: 0,
    create_time: "2025-05-01 16:24:47",
    update_time: "2025-05-10 16:24:47",
    is_deleted: 0,
  },
  {
    id: 9,
    keyword: "人工智能",
    weight: 75,
    daily_count: 105,
    weekly_count: 780,
    monthly_count: 2800,
    is_manual: 1,
    create_time: "2025-05-01 17:09:30",
    update_time: "2025-05-10 17:09:30",
    is_deleted: 0,
    creator_id: 1001,
    creator_name: "管理员A",
  },
  {
    id: 10,
    keyword: "机器学习",
    weight: 72,
    daily_count: 98,
    weekly_count: 720,
    monthly_count: 2600,
    is_manual: 0,
    create_time: "2025-05-01 18:32:15",
    update_time: "2025-05-10 18:32:15",
    is_deleted: 0,
  },
];
