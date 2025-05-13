import dayjs from "dayjs";

// 字典类型接口
export interface Dictionary {
  id: number;
  name: string;           // 字典名称
  code: string;           // 字典编码
  status: number;         // 状态：1-启用，0-禁用
  description?: string;   // 描述信息
  create_time: string;    // 创建时间
  update_time?: string;   // 更新时间
  create_by?: string;     // 创建人
  update_by?: string;     // 更新人
  remark?: string;        // 备注
  sort: number;           // 排序
  is_built_in: number;    // 是否内置：1-是，0-否（内置字典不可删除）
}

// 字典项接口
export interface DictionaryItem {
  id: number;
  dict_id: number;        // 字典ID
  dict_code?: string;     // 字典编码
  label: string;          // 标签
  value: string;          // 值
  status: number;         // 状态：1-启用，0-禁用
  css_class?: string;     // CSS类名
  list_class?: string;    // 列表样式类名
  icon?: string;          // 图标
  default_selected: number; // 是否默认：1-是，0-否
  sort: number;           // 排序
  description?: string;   // 描述
  create_time: string;    // 创建时间
  update_time?: string;   // 更新时间
  create_by?: string;     // 创建人
  update_by?: string;     // 更新人
  tag_type?: "primary" | "success" | "warning" | "danger" | "info"; // 标签类型
}

// 字典状态选项
export const DICT_STATUS_OPTIONS = [
  { label: "启用", value: 1 },
  { label: "禁用", value: 0 },
];

// 标签类型选项
export const TAG_TYPE_OPTIONS = [
  { label: "主要", value: "primary" },
  { label: "成功", value: "success" },
  { label: "警告", value: "warning" },
  { label: "危险", value: "danger" },
  { label: "信息", value: "info" },
];

// 模拟字典数据
export const MOCK_DICTIONARIES: Dictionary[] = [
  {
    id: 1,
    name: "性别",
    code: "sys_gender",
    status: 1,
    description: "系统性别字典",
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().subtract(15, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    sort: 1,
    is_built_in: 1,
  },
  {
    id: 2,
    name: "用户状态",
    code: "sys_user_status",
    status: 1,
    description: "用户状态字典",
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    update_time: dayjs().subtract(10, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    sort: 2,
    is_built_in: 1,
  },
  {
    id: 3,
    name: "是否选项",
    code: "sys_yes_no",
    status: 1,
    description: "通用是否字典",
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    sort: 3,
    is_built_in: 1,
  },
  {
    id: 4,
    name: "显示状态",
    code: "sys_show_hide",
    status: 1,
    description: "显示/隐藏状态",
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    sort: 4,
    is_built_in: 1,
  },
  {
    id: 5,
    name: "系统开关",
    code: "sys_normal_disable",
    status: 1,
    description: "系统开关状态",
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    sort: 5,
    is_built_in: 1,
  },
  {
    id: 6,
    name: "任务状态",
    code: "sys_job_status",
    status: 1,
    description: "任务状态",
    create_time: dayjs().subtract(20, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    sort: 6,
    is_built_in: 0,
  },
  {
    id: 7,
    name: "通知类型",
    code: "sys_notice_type",
    status: 1,
    description: "通知类型",
    create_time: dayjs().subtract(20, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    sort: 7,
    is_built_in: 0,
  },
  {
    id: 8,
    name: "通知状态",
    code: "sys_notice_status",
    status: 1,
    description: "通知状态",
    create_time: dayjs().subtract(20, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    sort: 8,
    is_built_in: 0,
  },
  {
    id: 9,
    name: "操作类型",
    code: "sys_oper_type",
    status: 1,
    description: "操作日志类型",
    create_time: dayjs().subtract(15, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    sort: 9,
    is_built_in: 0,
  },
  {
    id: 10,
    name: "文件类型",
    code: "sys_file_type",
    status: 0,
    description: "文件类型",
    create_time: dayjs().subtract(10, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    sort: 10,
    is_built_in: 0,
  },
];

// 模拟字典项数据
export const MOCK_DICTIONARY_ITEMS: DictionaryItem[] = [
  // 性别字典项
  {
    id: 1,
    dict_id: 1,
    dict_code: "sys_gender",
    label: "男",
    value: "1",
    status: 1,
    css_class: "",
    list_class: "",
    icon: "male",
    default_selected: 1,
    sort: 1,
    description: "男性",
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "primary",
  },
  {
    id: 2,
    dict_id: 1,
    dict_code: "sys_gender",
    label: "女",
    value: "2",
    status: 1,
    css_class: "",
    list_class: "",
    icon: "female", 
    default_selected: 0,
    sort: 2,
    description: "女性",
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "danger",
  },
  {
    id: 3,
    dict_id: 1,
    dict_code: "sys_gender",
    label: "未知",
    value: "0",
    status: 1,
    css_class: "",
    list_class: "",
    icon: "question",
    default_selected: 0,
    sort: 3,
    description: "未知性别",
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "info",
  },
  
  // 用户状态字典项
  {
    id: 4,
    dict_id: 2,
    dict_code: "sys_user_status",
    label: "正常",
    value: "1",
    status: 1,
    css_class: "",
    list_class: "success",
    default_selected: 1,
    sort: 1,
    description: "正常状态",
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "success",
  },
  {
    id: 5,
    dict_id: 2,
    dict_code: "sys_user_status",
    label: "停用",
    value: "0",
    status: 1,
    css_class: "",
    list_class: "danger",
    default_selected: 0,
    sort: 2,
    description: "停用状态",
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "danger",
  },
  {
    id: 6,
    dict_id: 2,
    dict_code: "sys_user_status",
    label: "锁定",
    value: "2",
    status: 1,
    css_class: "",
    list_class: "warning",
    default_selected: 0,
    sort: 3,
    description: "锁定状态",
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "warning",
  },
  
  // 是否选项字典项
  {
    id: 7,
    dict_id: 3,
    dict_code: "sys_yes_no",
    label: "是",
    value: "Y",
    status: 1,
    default_selected: 1,
    sort: 1,
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "success",
  },
  {
    id: 8,
    dict_id: 3,
    dict_code: "sys_yes_no",
    label: "否",
    value: "N",
    status: 1,
    default_selected: 0,
    sort: 2,
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "danger",
  },
  
  // 显示状态字典项
  {
    id: 9,
    dict_id: 4,
    dict_code: "sys_show_hide",
    label: "显示",
    value: "1",
    status: 1,
    default_selected: 1,
    sort: 1,
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "primary",
  },
  {
    id: 10,
    dict_id: 4,
    dict_code: "sys_show_hide",
    label: "隐藏",
    value: "0",
    status: 1,
    default_selected: 0,
    sort: 2,
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "info",
  },
  
  // 系统开关字典项
  {
    id: 11,
    dict_id: 5,
    dict_code: "sys_normal_disable",
    label: "正常",
    value: "1",
    status: 1,
    default_selected: 1,
    sort: 1,
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "success",
  },
  {
    id: 12,
    dict_id: 5,
    dict_code: "sys_normal_disable",
    label: "停用",
    value: "0",
    status: 1,
    default_selected: 0,
    sort: 2,
    create_time: dayjs().subtract(30, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "danger",
  },
  
  // 任务状态字典项
  {
    id: 13,
    dict_id: 6,
    dict_code: "sys_job_status",
    label: "运行中",
    value: "1",
    status: 1,
    default_selected: 1,
    sort: 1,
    create_time: dayjs().subtract(20, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "success",
  },
  {
    id: 14,
    dict_id: 6,
    dict_code: "sys_job_status",
    label: "已停止",
    value: "2",
    status: 1,
    default_selected: 0,
    sort: 2,
    create_time: dayjs().subtract(20, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "danger",
  },
  
  // 通知类型字典项
  {
    id: 15,
    dict_id: 7,
    dict_code: "sys_notice_type",
    label: "通知",
    value: "1",
    status: 1,
    default_selected: 1,
    sort: 1,
    create_time: dayjs().subtract(20, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "primary",
  },
  {
    id: 16,
    dict_id: 7,
    dict_code: "sys_notice_type",
    label: "公告",
    value: "2",
    status: 1,
    default_selected: 0,
    sort: 2,
    create_time: dayjs().subtract(20, "day").format("YYYY-MM-DD HH:mm:ss"),
    create_by: "admin",
    tag_type: "warning",
  },
];

// 根据字典代码获取字典项列表
export const getDictItemsByCode = (code: string): DictionaryItem[] => {
  return MOCK_DICTIONARY_ITEMS.filter(item => item.dict_code === code && item.status === 1)
    .sort((a, b) => a.sort - b.sort);
};

// 根据字典ID获取字典项列表
export const getDictItemsById = (id: number): DictionaryItem[] => {
  return MOCK_DICTIONARY_ITEMS.filter(item => item.dict_id === id)
    .sort((a, b) => a.sort - b.sort);
};

// 获取字典状态标签类型
export const getDictStatusTag = (status: number): { label: string; type: "success" | "danger" } => {
  return status === 1 
    ? { label: "启用", type: "success" } 
    : { label: "禁用", type: "danger" };
};
