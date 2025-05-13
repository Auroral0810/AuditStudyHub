import dayjs from "dayjs";

// 权限类型选项
export const PERMISSION_TYPE_OPTIONS = [
  { label: "菜单", value: 1 },
  { label: "按钮", value: 2 },
  { label: "接口", value: 3 },
];

// 状态选项
export const STATUS_OPTIONS = [
  { label: "启用", value: 1 },
  { label: "禁用", value: 0 },
];

// 显示状态选项
export const HIDDEN_OPTIONS = [
  { label: "显示", value: 0 },
  { label: "隐藏", value: 1 },
];

// 权限数据结构
export interface Permission {
  id: number;
  name: string;
  code: string;
  type: number; // 1-菜单；2-按钮；3-接口
  parent_id: number;
  path?: string;
  component?: string;
  redirect?: string;
  icon?: string;
  sort: number;
  status: number; // 1-启用；0-禁用
  hidden: number; // 0-显示；1-隐藏
  create_time?: string;
  update_time?: string;
  children?: Permission[];
}

// 角色数据结构
export interface Role {
  id: number;
  name: string;
  code: string;
  description?: string;
  status: number; // 1-启用；0-禁用
  permissions?: number[]; // 权限ID列表
  create_time?: string;
  update_time?: string;
}

// 权限树结构的转换方法
export const buildPermissionTree = (permissions: Permission[]): Permission[] => {
  // 创建映射表
  const map = new Map<number, Permission>();
  const result: Permission[] = [];

  // 创建映射
  permissions.forEach((permission) => {
    map.set(permission.id, { ...permission, children: [] });
  });

  // 构建树
  permissions.forEach((permission) => {
    const node = map.get(permission.id);
    if (node) {
      if (permission.parent_id === 0) {
        // 根节点
        result.push(node);
      } else {
        // 子节点
        const parent = map.get(permission.parent_id);
        if (parent) {
          if (!parent.children) {
            parent.children = [];
          }
          parent.children.push(node);
        }
      }
    }
  });

  return result;
};

// 模拟权限数据
export const MOCK_PERMISSION_DATA: Permission[] = [
  {
    id: 1,
    name: "系统管理",
    code: "system",
    type: 1,
    parent_id: 0,
    path: "/system",
    component: "LAYOUT",
    redirect: "/system/user",
    icon: "setting",
    sort: 1,
    status: 1,
    hidden: 0,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 2,
    name: "用户管理",
    code: "system:user",
    type: 1,
    parent_id: 1,
    path: "user",
    component: "/system/user",
    icon: "user",
    sort: 1,
    status: 1,
    hidden: 0,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 3,
    name: "角色管理",
    code: "system:role",
    type: 1,
    parent_id: 1,
    path: "role",
    component: "/system/role",
    icon: "role",
    sort: 2,
    status: 1,
    hidden: 0,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 4,
    name: "权限管理",
    code: "system:permission",
    type: 1,
    parent_id: 1,
    path: "permission",
    component: "/system/permission",
    icon: "permission",
    sort: 3,
    status: 1,
    hidden: 0,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 5,
    name: "站点管理",
    code: "system:site",
    type: 1,
    parent_id: 1,
    path: "site",
    component: "/system/site",
    icon: "site",
    sort: 4,
    status: 1,
    hidden: 0,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 101,
    name: "用户查询",
    code: "system:user:list",
    type: 2,
    parent_id: 2,
    sort: 1,
    status: 1,
    hidden: 0,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 102,
    name: "用户新增",
    code: "system:user:add",
    type: 2,
    parent_id: 2,
    sort: 2,
    status: 1,
    hidden: 0,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 103,
    name: "用户编辑",
    code: "system:user:edit",
    type: 2,
    parent_id: 2,
    sort: 3,
    status: 1,
    hidden: 0,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 104,
    name: "用户删除",
    code: "system:user:delete",
    type: 2,
    parent_id: 2,
    sort: 4,
    status: 1,
    hidden: 0,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 201,
    name: "角色查询",
    code: "system:role:list",
    type: 2,
    parent_id: 3,
    sort: 1,
    status: 1,
    hidden: 0,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 202,
    name: "角色新增",
    code: "system:role:add",
    type: 2,
    parent_id: 3,
    sort: 2,
    status: 1,
    hidden: 0,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 203,
    name: "角色编辑",
    code: "system:role:edit",
    type: 2,
    parent_id: 3,
    sort: 3,
    status: 1,
    hidden: 0,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 204,
    name: "角色删除",
    code: "system:role:delete",
    type: 2,
    parent_id: 3,
    sort: 4,
    status: 1,
    hidden: 0,
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
];

// 模拟角色数据
export const MOCK_ROLE_DATA: Role[] = [
  {
    id: 1,
    name: "超级管理员",
    code: "admin",
    description: "拥有所有权限",
    status: 1,
    permissions: [1, 2, 3, 4, 5, 101, 102, 103, 104, 201, 202, 203, 204],
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 2,
    name: "普通用户",
    code: "user",
    description: "只拥有基本权限",
    status: 1,
    permissions: [1, 2, 101],
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 3,
    name: "游客",
    code: "guest",
    description: "只拥有查看权限",
    status: 1,
    permissions: [1, 2, 3, 4, 5, 101, 201],
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
  {
    id: 4,
    name: "测试角色",
    code: "test",
    description: "测试用途",
    status: 0,
    permissions: [],
    create_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().format("YYYY-MM-DD HH:mm:ss"),
  },
];
