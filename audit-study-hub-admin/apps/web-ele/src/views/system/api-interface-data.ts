// API资源接口状态类型定义
export enum ApiStatus {
  Disabled = 0,
  Enabled = 1,
}

// API方法类型定义
export const ApiMethods = [
  "GET",
  "POST",
  "PUT",
  "DELETE",
  "PATCH",
  "OPTIONS",
  "HEAD",
] as const;
export type ApiMethod = (typeof ApiMethods)[number];

// API资源接口数据类型
export interface ApiResource {
  id: number;
  name: string;
  url: string;
  method: ApiMethod;
  description: string;
  permission_id: null | number;
  permission_name?: string;
  status: ApiStatus;
  create_time: string;
  update_time: string;
  is_deleted: number;
  calls_count?: number;
  avg_response_time?: number;
}

// API资源查询参数
export interface ApiQueryParams {
  name: string;
  url: string;
  method: string;
  status: "" | ApiStatus;
  permission_id: null | number;
  page: number;
  pageSize: number;
}

// API资源表单
export interface ApiResourceForm {
  id?: number;
  name: string;
  url: string;
  method: ApiMethod;
  description: string;
  permission_id: null | number;
  status: ApiStatus;
}

// API测试表单
export interface ApiTestForm {
  url: string;
  method: ApiMethod;
  headers: Array<{ key: string; value: string }>;
  params: Array<{ key: string; value: string }>;
  body: string;
}

// API测试结果
export interface ApiTestResult {
  status: number;
  statusText: string;
  headers: Record<string, string>;
  data: any;
  time: number;
}

// API监控数据类型定义
export interface ApiMonitorData {
  id: number;
  url: string;
  method: string;
  calls_count: number;
  avg_response_time: number;
  success_rate: number;
  last_called: string;
}

// 表格列定义
export const apiTableColumns = [
  { prop: "id", label: "ID", width: "80px" },
  { prop: "name", label: "接口名称", minWidth: "150px" },
  { prop: "url", label: "接口路径", minWidth: "220px" },
  { prop: "method", label: "请求方法", width: "100px" },
  { prop: "permission_name", label: "关联权限", width: "150px" },
  { prop: "description", label: "描述", minWidth: "200px" },
  { prop: "status", label: "状态", width: "100px" },
  { prop: "create_time", label: "创建时间", width: "160px" },
  { prop: "update_time", label: "更新时间", width: "160px" },
  { prop: "action", label: "操作", width: "220px", fixed: "right" },
];

// 监控表格列定义
export const monitorTableColumns = [
  { prop: "id", label: "ID", width: "80px" },
  { prop: "url", label: "接口路径", minWidth: "220px" },
  { prop: "method", label: "请求方法", width: "100px" },
  { prop: "calls_count", label: "调用次数", width: "100px" },
  { prop: "avg_response_time", label: "平均响应时间(ms)", width: "150px" },
  { prop: "success_rate", label: "成功率", width: "100px" },
  { prop: "last_called", label: "最后调用时间", width: "160px" },
  { prop: "action", label: "操作", width: "120px", fixed: "right" },
];

// 默认API查询参数
export const defaultApiQueryParams: ApiQueryParams = {
  name: "",
  url: "",
  method: "",
  status: "",
  permission_id: null,
  page: 1,
  pageSize: 10,
};

// 默认API表单数据
export const defaultApiForm: ApiResourceForm = {
  name: "",
  url: "",
  method: "GET",
  description: "",
  permission_id: null,
  status: ApiStatus.Enabled,
};

// 默认API测试表单
export const defaultApiTestForm: ApiTestForm = {
  url: "",
  method: "GET",
  headers: [{ key: "", value: "" }],
  params: [{ key: "", value: "" }],
  body: "",
};

// API表单校验规则
export const apiFormRules = {
  name: [
    { required: true, message: "请输入接口名称", trigger: "blur" },
    { min: 2, max: 100, message: "长度在 2 到 100 个字符", trigger: "blur" },
  ],
  url: [
    { required: true, message: "请输入接口路径", trigger: "blur" },
    { min: 1, max: 255, message: "长度在 1 到 255 个字符", trigger: "blur" },
    {
      pattern: /^\/[a-zA-Z0-9\-._~%!$&'()*+,;=:@/]*$/,
      message: "接口路径必须以/开头，且只能包含有效字符",
      trigger: "blur",
    },
  ],
  method: [
    { required: true, message: "请选择请求方法", trigger: "change" },
  ],
  status: [
    { required: true, message: "请选择状态", trigger: "change" },
  ],
};

// 生成模拟数据函数
export function generateMockApiData(count = 15): ApiResource[] {
  const mockData: ApiResource[] = [];
  const methods = ["GET", "POST", "PUT", "DELETE"];
  const permissions = [
    { id: 10101, name: "查看用户列表" },
    { id: 10102, name: "添加用户" },
    { id: 10103, name: "编辑用户" },
    { id: 10104, name: "删除用户" },
    { id: 10201, name: "查看角色列表" },
    { id: 10202, name: "添加角色" },
    { id: null, name: null },
  ];

  for (let i = 1; i <= count; i++) {
    const permissionIndex = Math.floor(Math.random() * permissions.length);
    const permission = permissions[permissionIndex];

    mockData.push({
      id: i,
      name: `API接口${i}`,
      url: `/api/v1/resource${i}`,
      method: methods[i % methods.length] as ApiMethod,
      description: `这是API接口${i}的描述信息`,
      permission_id: permission.id,
      permission_name: permission.name || "-",
      status: i % 5 === 0 ? ApiStatus.Disabled : ApiStatus.Enabled,
      create_time: new Date().toISOString().slice(0, 19).replace("T", " "),
      update_time: new Date().toISOString().slice(0, 19).replace("T", " "),
      is_deleted: 0,
      calls_count: Math.floor(Math.random() * 1000),
      avg_response_time: Math.floor(Math.random() * 500)
    });
  }

  return mockData;
}

// 生成模拟监控数据
export function generateMockMonitorData(count = 10): ApiMonitorData[] {
  const mockData: ApiMonitorData[] = [];
  const methods = ['GET', 'POST', 'PUT', 'DELETE'];

  for (let i = 1; i <= count; i++) {
    mockData.push({
      id: i,
      url: `/api/v1/resource${i}`,
      method: methods[i % methods.length],
      calls_count: Math.floor(Math.random() * 10000),
      avg_response_time: Math.floor(Math.random() * 500),
      success_rate: Math.floor(80 + Math.random() * 20),
      last_called: new Date().toISOString().slice(0, 19).replace('T', ' ')
    });
  }

  return mockData;
}
