import dayjs from "dayjs";

// 用户基本信息接口
export interface User {
  id: number;
  username: string; // 用户名
  nickname: string; // 昵称
  student_id: string; // 学号
  password?: string; // 密码（接口中通常不返回）
  mobile: string; // 手机号
  email: string; // 邮箱
  avatar: string; // 头像URL
  college_id: number; // 学院ID
  college_name: string; // 学院名称 
  major_id: number; // 专业ID
  major_name: string; // 专业名称
  is_admin: number; // 是否管理员 1-是 0-否
  status: number; // 状态 1-启用 0-禁用
  create_time: string; // 创建时间
  update_time: string; // 更新时间
  is_deleted: number; // 是否删除 0-未删除 1-已删除
  roles?: Role[]; // 关联角色
}

// 角色信息接口
export interface Role {
  id: number;
  name: string;
  code: string;
  description?: string;
  status: number; // 1-启用 0-禁用
  create_time?: string;
  update_time?: string;
  permissions?: number[]; // 权限ID列表
}

// 用户-角色关联接口
export interface UserRole {
  id: number;
  user_id: number;
  role_id: number;
  create_time: string;
}

// 资源简要信息接口
export interface ResourceBrief {
  id: number;
  title: string;
  cover_url?: string;
  file_size?: number;
  download_count?: number;
  create_time?: string;
}

// 用户下载记录接口
export interface UserDownload {
  id: number;
  user_id: number;
  resource_id: number;
  create_time: string;
  resource?: ResourceBrief;
}

// 用户点赞记录接口
export interface UserLike {
  id: number;
  user_id: number;
  resource_id: number;
  create_time: string;
  is_deleted: number; // 0-未删除 1-已删除
  resource?: ResourceBrief;
}

// 用户收藏记录接口
export interface UserCollection {
  id: number;
  user_id: number;
  resource_id: number;
  create_time: string;
  is_deleted: number; // 0-未删除 1-已删除
  resource?: ResourceBrief;
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

// 状态选项
export const USER_STATUS_OPTIONS = [
  { label: "启用", value: 1 },
  { label: "禁用", value: 0 },
];

// 管理员选项
export const IS_ADMIN_OPTIONS = [
  { label: "是", value: 1 },
  { label: "否", value: 0 },
];

// 模拟用户数据
export const MOCK_USERS: User[] = [
  {
    id: 1,
    username: "admin",
    nickname: "管理员",
    student_id: "admin",
    mobile: "13800000000",
    email: "1957689514@qq.com",
    avatar: "https://big-event20040810.oss-cn-beijing.aliyuncs.com/avatars/admin/20250507/dc1eab8999c644d3ad2fa793b31fbb98.jpg",
    college_id: 1744,
    college_name: "计算机学院/智能审计学院",
    major_id: 5420,
    major_name: "软件工程（区块链审计方向）",
    is_admin: 1,
    status: 1,
    create_time: "2025-05-05 22:45:21",
    update_time: "2025-05-08 19:47:09",
    is_deleted: 0
  },
  {
    id: 2,
    username: "student",
    nickname: "测试学生",
    student_id: "2023001",
    mobile: "13900000000",
    email: "student@example.com",
    avatar: "https://big-event20040810.oss-cn-beijing.aliyuncs.com/avatar/default.png",
    college_id: 1744,
    college_name: "计算机学院/智能审计学院",
    major_id: 5420,
    major_name: "软件工程（区块链审计方向）",
    is_admin: 0,
    status: 1,
    create_time: "2025-05-05 22:45:22",
    update_time: "2025-05-05 22:45:22",
    is_deleted: 0
  },
  {
    id: 3,
    username: "testadmin",
    nickname: "测试管理员",
    student_id: "testadmin",
    mobile: "13811111111",
    email: "testadmin@example.com",
    avatar: "https://big-event20040810.oss-cn-beijing.aliyuncs.com/avatar/default.png",
    college_id: 1744,
    college_name: "计算机学院/智能审计学院",
    major_id: 5420,
    major_name: "软件工程（区块链审计方向）",
    is_admin: 1,
    status: 1,
    create_time: "2025-05-06 19:43:26",
    update_time: "2025-05-06 19:43:26",
    is_deleted: 0
  },
  {
    id: 4,
    username: "auroral",
    nickname: "俞云烽",
    student_id: "222090140",
    mobile: "15968588744",
    email: "15968581744@163.com",
    avatar: "https://big-event20040810.oss-cn-beijing.aliyuncs.com/avatar/default.png",
    college_id: 1744,
    college_name: "计算机学院/智能审计学院",
    major_id: 5420,
    major_name: "软件工程（区块链审计方向）",
    is_admin: 0,
    status: 1,
    create_time: "2025-05-06 20:58:17",
    update_time: "2025-05-06 20:58:17",
    is_deleted: 0
  }
];

// 模拟角色数据
export const MOCK_ROLES: Role[] = [
  {
    id: 1,
    name: "超级管理员",
    code: "super_admin",
    description: "拥有所有权限",
    status: 1,
    create_time: "2025-05-05 22:45:22",
    update_time: "2025-05-05 22:45:22"
  },
  {
    id: 2,
    name: "管理员",
    code: "admin",
    description: "拥有大部分管理权限",
    status: 1,
    create_time: "2025-05-05 22:45:22",
    update_time: "2025-05-05 22:45:22"
  },
  {
    id: 5,
    name: "普通用户",
    code: "user",
    description: "普通用户权限",
    status: 1,
    create_time: "2025-05-05 22:45:22",
    update_time: "2025-05-05 22:45:22"
  }
];

// 模拟用户-角色关联数据
export const MOCK_USER_ROLES: UserRole[] = [
  {
    id: 1,
    user_id: 1,
    role_id: 1,
    create_time: "2025-05-09 18:13:28"
  },
  {
    id: 2,
    user_id: 3,
    role_id: 2,
    create_time: "2025-05-09 18:13:28"
  },
  {
    id: 3,
    user_id: 2,
    role_id: 5,
    create_time: "2025-05-09 18:13:28"
  },
  {
    id: 4,
    user_id: 4,
    role_id: 5,
    create_time: "2025-05-09 18:13:28"
  }
];

// 模拟资源简要信息
export const MOCK_RESOURCES: ResourceBrief[] = [
  { id: 1, title: "区块链审计技术概论", cover_url: "/covers/blockchain.jpg", file_size: 15240, download_count: 156, create_time: "2025-04-15 10:00:00" },
  { id: 2, title: "信息系统审计实践指南", cover_url: "/covers/is-audit.jpg", file_size: 8520, download_count: 89, create_time: "2025-04-16 14:30:00" },
  { id: 3, title: "数据分析在审计中的应用", cover_url: "/covers/data-analysis.jpg", file_size: 12580, download_count: 210, create_time: "2025-04-18 09:15:00" },
  { id: 4, title: "财务审计案例分析", cover_url: "/covers/finance.jpg", file_size: 6420, download_count: 178, create_time: "2025-04-20 16:45:00" },
  { id: 5, title: "内部控制评估方法", cover_url: "/covers/internal-control.jpg", file_size: 5280, download_count: 65, create_time: "2025-04-22 11:20:00" }
];

// 模拟用户下载记录
export const MOCK_USER_DOWNLOADS: UserDownload[] = [
  { id: 1, user_id: 1, resource_id: 1, create_time: "2025-05-08 12:32:24" },
  { id: 4, user_id: 1, resource_id: 4, create_time: "2025-05-08 12:37:20" },
  { id: 14, user_id: 1, resource_id: 3, create_time: "2025-05-08 17:49:55" },
  { id: 35, user_id: 1, resource_id: 13, create_time: "2025-05-09 00:11:23" },
  { id: 36, user_id: 1, resource_id: 18, create_time: "2025-05-09 00:11:26" },
  { id: 55, user_id: 1, resource_id: 5, create_time: "2025-05-09 19:47:47" }
];

// 模拟用户点赞记录
export const MOCK_USER_LIKES: UserLike[] = [
  { id: 1, user_id: 1, resource_id: 1, create_time: "2025-05-08 12:58:12", is_deleted: 0 },
  { id: 23, user_id: 1, resource_id: 2, create_time: "2025-05-08 17:49:50", is_deleted: 0 },
  { id: 24, user_id: 1, resource_id: 3, create_time: "2025-05-08 17:49:52", is_deleted: 0 },
  { id: 25, user_id: 1, resource_id: 4, create_time: "2025-05-08 18:39:55", is_deleted: 1 }
];

// 模拟用户收藏记录
export const MOCK_USER_COLLECTIONS: UserCollection[] = [
  { id: 1, user_id: 1, resource_id: 1, create_time: "2025-05-08 12:58:00", is_deleted: 1 },
  { id: 7, user_id: 1, resource_id: 3, create_time: "2025-05-08 18:59:36", is_deleted: 0 },
  { id: 9, user_id: 1, resource_id: 5, create_time: "2025-05-08 19:24:06", is_deleted: 0 },
  { id: 10, user_id: 1, resource_id: 20, create_time: "2025-05-09 01:13:32", is_deleted: 0 }
];

// 模拟学院数据，从原有文件保留
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
    id: 1744,
    name: "计算机学院/智能审计学院",
    cover_url: "https://study-cdn-img.jobpi.cn/upload/2024-03-08/17098832827106.jpeg",
    logo_url: "https://study-cdn-img.jobpi.cn/upload/2025-03-13/17418493648596.png",
    create_time: "2025-05-07 13:25:10",
    update_time: "2025-05-07 13:25:10",
    is_deleted: 0,
  }
];

// 获取用户的角色名称列表
export function getUserRoleNames(userId: number): string[] {
  // 获取用户的角色ID列表
  const userRoleIds = MOCK_USER_ROLES
    .filter(ur => ur.user_id === userId)
    .map(ur => ur.role_id);
  
  // 根据角色ID获取角色名称
  return MOCK_ROLES
    .filter(role => userRoleIds.includes(role.id))
    .map(role => role.name);
}

// 为下载记录附加资源信息
export function getDownloadsWithResources(downloads: UserDownload[]): UserDownload[] {
  return downloads.map(download => {
    const resource = MOCK_RESOURCES.find(r => r.id === download.resource_id);
    return {
      ...download,
      resource: resource || { id: download.resource_id, title: `资源${download.resource_id}` }
    };
  });
}

// 获取指定用户的下载记录
export function getUserDownloads(userId: number): UserDownload[] {
  const downloads = MOCK_USER_DOWNLOADS.filter(d => d.user_id === userId);
  return getDownloadsWithResources(downloads);
}

// 为点赞记录附加资源信息
export function getLikesWithResources(likes: UserLike[]): UserLike[] {
  return likes.map(like => {
    const resource = MOCK_RESOURCES.find(r => r.id === like.resource_id);
    return {
      ...like,
      resource: resource || { id: like.resource_id, title: `资源${like.resource_id}` }
    };
  });
}

// 获取指定用户的有效点赞记录
export function getUserActiveLikes(userId: number): UserLike[] {
  const likes = MOCK_USER_LIKES.filter(l => l.user_id === userId && l.is_deleted === 0);
  return getLikesWithResources(likes);
}

// 为收藏记录附加资源信息
export function getCollectionsWithResources(collections: UserCollection[]): UserCollection[] {
  return collections.map(collection => {
    const resource = MOCK_RESOURCES.find(r => r.id === collection.resource_id);
    return {
      ...collection,
      resource: resource || { id: collection.resource_id, title: `资源${collection.resource_id}` }
    };
  });
}

// 获取指定用户的有效收藏记录
export function getUserActiveCollections(userId: number): UserCollection[] {
  const collections = MOCK_USER_COLLECTIONS.filter(c => c.user_id === userId && c.is_deleted === 0);
  return getCollectionsWithResources(collections);
}
