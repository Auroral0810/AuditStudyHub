// 用户行为数据接口
export interface UserBehavior {
  id: number;
  user_id: number;
  user_name: string;
  behavior_type: string;
  content: string;
  device_type: string;
  ip_address: string;
  happen_time: string;
  session_id: string;
  page_url: string;
  duration?: number;
  extra_data?: string;
  is_deleted: number;
}

// 行为类型选项
export const BEHAVIOR_TYPE_OPTIONS = [
  { label: "搜索", value: "search" },
  { label: "点击", value: "click" },
  { label: "下载", value: "download" },
  { label: "分享", value: "share" },
  { label: "收藏", value: "collect" },
  { label: "评论", value: "comment" },
  { label: "浏览", value: "view" },
  { label: "登录", value: "login" },
  { label: "注册", value: "register" },
  { label: "其他", value: "other" },
];

// 设备类型选项
export const DEVICE_TYPE_OPTIONS = [
  { label: "PC", value: "pc" },
  { label: "移动端", value: "mobile" },
  { label: "平板", value: "tablet" },
  { label: "其他", value: "other" },
];

// 模拟用户行为数据
// TODO: 之后需要从后端获取
export const MOCK_USER_BEHAVIOR_DATA: UserBehavior[] = [
  {
    id: 1,
    user_id: 101,
    user_name: "张三",
    behavior_type: "search",
    content: "高等数学期末复习",
    device_type: "pc",
    ip_address: "192.168.1.101",
    happen_time: "2025-05-10 08:30:15",
    session_id: "s10001",
    page_url: "/search",
    duration: 120,
    extra_data: JSON.stringify({ result_count: 12 }),
    is_deleted: 0,
  },
  {
    id: 2,
    user_id: 102,
    user_name: "李四",
    behavior_type: "click",
    content: "点击了《高等数学习题集》",
    device_type: "mobile",
    ip_address: "192.168.1.102",
    happen_time: "2025-05-10 09:15:42",
    session_id: "s10002",
    page_url: "/resource/12345",
    is_deleted: 0,
  },
  {
    id: 3,
    user_id: 103,
    user_name: "王五",
    behavior_type: "download",
    content: "下载了《Java程序设计》PDF",
    device_type: "pc",
    ip_address: "192.168.1.103",
    happen_time: "2025-05-10 10:25:38",
    session_id: "s10003",
    page_url: "/download/45678",
    extra_data: JSON.stringify({ file_size: "15MB", file_type: "pdf" }),
    is_deleted: 0,
  },
  {
    id: 4,
    user_id: 104,
    user_name: "赵六",
    behavior_type: "share",
    content: "分享了《数据结构与算法》到微信",
    device_type: "tablet",
    ip_address: "192.168.1.104",
    happen_time: "2025-05-10 11:05:22",
    session_id: "s10004",
    page_url: "/resource/23456",
    extra_data: JSON.stringify({ platform: "wechat" }),
    is_deleted: 0,
  },
  {
    id: 5,
    user_id: 105,
    user_name: "钱七",
    behavior_type: "collect",
    content: "收藏了《计算机网络》",
    device_type: "pc",
    ip_address: "192.168.1.105",
    happen_time: "2025-05-10 13:42:19",
    session_id: "s10005",
    page_url: "/resource/34567",
    is_deleted: 0,
  },
  {
    id: 6,
    user_id: 106,
    user_name: "孙八",
    behavior_type: "comment",
    content: "评论了《操作系统原理》: 这本书讲解得很清晰",
    device_type: "mobile",
    ip_address: "192.168.1.106",
    happen_time: "2025-05-10 14:36:55",
    session_id: "s10006",
    page_url: "/resource/56789",
    is_deleted: 0,
  },
  {
    id: 7,
    user_id: 107,
    user_name: "周九",
    behavior_type: "view",
    content: "浏览了《数据库系统》",
    device_type: "pc",
    ip_address: "192.168.1.107",
    happen_time: "2025-05-10 15:18:33",
    session_id: "s10007",
    page_url: "/resource/67890",
    duration: 450,
    is_deleted: 0,
  },
  {
    id: 8,
    user_id: 108,
    user_name: "吴十",
    behavior_type: "login",
    content: "用户登录",
    device_type: "tablet",
    ip_address: "192.168.1.108",
    happen_time: "2025-05-10 16:24:47",
    session_id: "s10008",
    page_url: "/login",
    extra_data: JSON.stringify({ method: "password" }),
    is_deleted: 0,
  },
  {
    id: 9,
    user_id: 109,
    user_name: "郑十一",
    behavior_type: "search",
    content: "人工智能",
    device_type: "pc",
    ip_address: "192.168.1.109",
    happen_time: "2025-05-10 17:09:30",
    session_id: "s10009",
    page_url: "/search",
    extra_data: JSON.stringify({ result_count: 14 }),
    is_deleted: 0,
  },
  {
    id: 10,
    user_id: 110,
    user_name: "王十二",
    behavior_type: "register",
    content: "用户注册",
    device_type: "mobile",
    ip_address: "192.168.1.110",
    happen_time: "2025-05-10 18:32:15",
    session_id: "s10010",
    page_url: "/register",
    extra_data: JSON.stringify({ invite_code: "INV12345" }),
    is_deleted: 0,
  },
];
