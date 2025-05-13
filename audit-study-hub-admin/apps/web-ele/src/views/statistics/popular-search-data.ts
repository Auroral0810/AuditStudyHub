// 流行搜索数据接口
export interface PopularSearch {
  id: number;
  keyword: string;
  search_count: number;
  user_id: number;
  user_name: string;
  device_type: string;
  ip_address: string;
  search_time: string;
  result_count: number;
  is_deleted: number;
}

// 设备类型选项
export const DEVICE_TYPE_OPTIONS = [
  { label: "PC", value: "pc" },
  { label: "移动端", value: "mobile" },
  { label: "平板", value: "tablet" },
  { label: "其他", value: "other" },
];

// 模拟流行搜索数据
// TODO: 之后需要从后端获取
export const MOCK_POPULAR_SEARCH_DATA: PopularSearch[] = [
  {
    id: 1,
    keyword: "高等数学期末复习",
    search_count: 245,
    user_id: 101,
    user_name: "张三",
    device_type: "pc",
    ip_address: "192.168.1.101",
    search_time: "2025-05-10 08:30:15",
    result_count: 12,
    is_deleted: 0,
  },
  {
    id: 2,
    keyword: "大学英语四级",
    search_count: 198,
    user_id: 102,
    user_name: "李四",
    device_type: "mobile",
    ip_address: "192.168.1.102",
    search_time: "2025-05-10 09:15:42",
    result_count: 8,
    is_deleted: 0,
  },
  {
    id: 3,
    keyword: "Java程序设计",
    search_count: 176,
    user_id: 103,
    user_name: "王五",
    device_type: "pc",
    ip_address: "192.168.1.103",
    search_time: "2025-05-10 10:25:38",
    result_count: 15,
    is_deleted: 0,
  },
  {
    id: 4,
    keyword: "数据结构与算法",
    search_count: 165,
    user_id: 104,
    user_name: "赵六",
    device_type: "tablet",
    ip_address: "192.168.1.104",
    search_time: "2025-05-10 11:05:22",
    result_count: 10,
    is_deleted: 0,
  },
  {
    id: 5,
    keyword: "计算机网络",
    search_count: 143,
    user_id: 105,
    user_name: "钱七",
    device_type: "pc",
    ip_address: "192.168.1.105",
    search_time: "2025-05-10 13:42:19",
    result_count: 9,
    is_deleted: 0,
  },
  {
    id: 6,
    keyword: "操作系统原理",
    search_count: 135,
    user_id: 106,
    user_name: "孙八",
    device_type: "mobile",
    ip_address: "192.168.1.106",
    search_time: "2025-05-10 14:36:55",
    result_count: 7,
    is_deleted: 0,
  },
  {
    id: 7,
    keyword: "数据库系统",
    search_count: 124,
    user_id: 107,
    user_name: "周九",
    device_type: "pc",
    ip_address: "192.168.1.107",
    search_time: "2025-05-10 15:18:33",
    result_count: 11,
    is_deleted: 0,
  },
  {
    id: 8,
    keyword: "软件工程",
    search_count: 118,
    user_id: 108,
    user_name: "吴十",
    device_type: "tablet",
    ip_address: "192.168.1.108",
    search_time: "2025-05-10 16:24:47",
    result_count: 6,
    is_deleted: 0,
  },
  {
    id: 9,
    keyword: "人工智能",
    search_count: 105,
    user_id: 109,
    user_name: "郑十一",
    device_type: "pc",
    ip_address: "192.168.1.109",
    search_time: "2025-05-10 17:09:30",
    result_count: 14,
    is_deleted: 0,
  },
  {
    id: 10,
    keyword: "机器学习",
    search_count: 98,
    user_id: 110,
    user_name: "王十二",
    device_type: "mobile",
    ip_address: "192.168.1.110",
    search_time: "2025-05-10 18:32:15",
    result_count: 8,
    is_deleted: 0,
  },
];
