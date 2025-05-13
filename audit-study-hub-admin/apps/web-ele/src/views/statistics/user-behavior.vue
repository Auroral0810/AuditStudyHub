<template>
  <Page title="用户行为分析" description="分析用户在系统中的各种行为数据">
    <!-- 表格 -->
    <Grid>
      <template #toolbar-tools>
        <ElButton class="mr-2" type="primary" @click="handleExport">
          导出数据
        </ElButton>
        <ElButton class="mr-2" type="success" @click="handleGenerateReport">
          生成报表
        </ElButton>
        <ElButton class="mr-2" type="primary" @click="changeBorder">
          {{ showBorder ? "隐藏" : "显示" }}边框
        </ElButton>
        <ElButton class="mr-2" type="primary" @click="changeLoading">
          显示loading
        </ElButton>
        <ElButton class="mr-2" type="primary" @click="changeStripe">
          {{ showStripe ? "隐藏" : "显示" }}斑马纹
        </ElButton>
        <ElButton class="mr-2" type="primary" @click="() => gridApi.query()">
          刷新当前页面
        </ElButton>
        <ElButton type="primary" @click="() => gridApi.reload()">
          刷新并返回第一页
        </ElButton>
      </template>
    </Grid>
  </Page>
</template>

<script lang="ts" setup>
import { h, ref, reactive } from "vue";

import { Page } from "@vben/common-ui";
import { ElButton, ElMessage, ElTag } from "element-plus";

import { useVbenVxeGrid } from "../../adapter/vxe-table";
import type { VbenFormProps } from "../../adapter/form";

import { 
  MOCK_USER_BEHAVIOR_DATA, 
  BEHAVIOR_TYPE_OPTIONS, 
  DEVICE_TYPE_OPTIONS 
} from "./user-behavior-data";
import type { UserBehavior } from "./user-behavior-data";

const sleep = (time = 500) => new Promise((resolve) => setTimeout(resolve, time));

// 搜索表单配置
const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入用户ID或用户名",
      },
      fieldName: "user_search",
      label: "用户",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: BEHAVIOR_TYPE_OPTIONS,
        placeholder: "请选择行为类型",
      },
      fieldName: "behavior_type",
      label: "行为类型",
    },
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入内容关键词",
      },
      fieldName: "content",
      label: "内容关键词",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: DEVICE_TYPE_OPTIONS,
        placeholder: "请选择设备类型",
      },
      fieldName: "device_type",
      label: "设备类型",
    },
    {
      component: "DatePicker",
      componentProps: {
        type: "datetimerange",
        valueFormat: "YYYY-MM-DD HH:mm:ss",
        placeholder: "请选择行为时间范围",
      },
      fieldName: "time_range",
      label: "行为时间",
    },
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入IP地址",
      },
      fieldName: "ip_address",
      label: "IP地址",
    },
  ],
  showCollapseButton: true,
  submitButtonOptions: {
    content: "查询",
  },
  submitOnChange: false,
  submitOnEnter: true,
};

// 获取行为类型标签类型
const getBehaviorTagType = (behaviorType: string): "success" | "info" | "warning" | "danger" => {
  switch (behaviorType) {
    case "search":
      return "info";
    case "download":
      return "success";
    case "view":
      return "info";
    case "login":
    case "register":
      return "success";
    case "comment":
    case "share":
    case "collect":
      return "warning";
    default:
      return "info";
  }
};

// 获取行为类型名称
const getBehaviorTypeName = (behaviorType: string): string => {
  const option = BEHAVIOR_TYPE_OPTIONS.find(item => item.value === behaviorType);
  return option ? option.label : behaviorType;
};

// 获取设备类型名称
const getDeviceTypeName = (deviceType: string): string => {
  const option = DEVICE_TYPE_OPTIONS.find(item => item.value === deviceType);
  return option ? option.label : deviceType;
};

// 格式化JSON数据
const formatExtraData = (extraData?: string): string => {
  if (!extraData) return "-";
  try {
    const data = JSON.parse(extraData);
    return Object.entries(data)
      .map(([key, value]) => `${key}: ${value}`)
      .join(", ");
  } catch (e) {
    return extraData;
  }
};

async function getUserBehaviorApi(params: {
  page: number;
  pageSize: number;
  user_search?: string;
  behavior_type?: string;
  content?: string;
  device_type?: string;
  time_range?: string[];
  ip_address?: string;
}) {
  const { 
    page, 
    pageSize, 
    user_search,
    behavior_type, 
    content,
    device_type,
    time_range,
    ip_address
  } = params;
  
  const all = MOCK_USER_BEHAVIOR_DATA.filter((item) => {
    // 用户ID或用户名匹配
    if (user_search) {
      const userIdMatch = item.user_id.toString().includes(user_search);
      const userNameMatch = item.user_name.includes(user_search);
      if (!userIdMatch && !userNameMatch) return false;
    }
    
    // 行为类型匹配
    if (behavior_type && item.behavior_type !== behavior_type) return false;
    
    // 内容关键词匹配
    if (content && !item.content.includes(content)) return false;
    
    // 设备类型匹配
    if (device_type && item.device_type !== device_type) return false;
    
    // IP地址匹配
    if (ip_address && !item.ip_address.includes(ip_address)) return false;
    
    // 时间范围匹配
    if (time_range && time_range.length === 2) {
      const start = time_range[0];
      const end = time_range[1];
      if (start && end) {
        if (item.happen_time < start || item.happen_time > end) return false;
      }
    }
    
    return true;
  });
  
  const items = all.slice((page - 1) * pageSize, page * pageSize);
  await sleep(300);
  return {
    total: all.length,
    items,
  };
}

// 表格配置
const gridOptions = reactive({
  border: true,
  showOverflow: true,
  stripe: true,
  rowConfig: {
    keyField: "id",
    isHover: true,
  },
  columnConfig: {
    resizable: true,
  },
  pagerConfig: {
    pageSize: 10,
    pageSizes: [5, 10, 20, 50, 100],
    layouts: [
      "PrevPage",
      "JumpNumber",
      "NextPage",
      "FullJump",
      "Sizes",
      "Total",
    ],
  },
  columns: [
    { field: "id", title: "ID", width: 80, fixed: "left" },
    { field: "user_id", title: "用户ID", width: 100 },
    { field: "user_name", title: "用户名", width: 120 },
    { 
      field: "behavior_type", 
      title: "行为类型", 
      width: 120,
      slots: {
        default: ({ row }: { row: UserBehavior }) => [
          h(
            ElTag,
            {
              type: getBehaviorTagType(row.behavior_type),
              effect: "light",
            },
            { default: () => getBehaviorTypeName(row.behavior_type) }
          ),
        ],
      },
    },
    { field: "content", title: "内容", minWidth: 250 },
    { 
      field: "device_type", 
      title: "设备类型", 
      width: 120,
      formatter: ({ row }: { row: UserBehavior }) => getDeviceTypeName(row.device_type),
    },
    { field: "ip_address", title: "IP地址", width: 150 },
    { field: "happen_time", title: "行为时间", width: 180 },
    { field: "session_id", title: "会话ID", width: 120 },
    { field: "page_url", title: "页面地址", width: 180 },
    { 
      field: "duration", 
      title: "停留时长(秒)", 
      width: 120,
      formatter: ({ row }: { row: UserBehavior }) => (row.duration !== undefined ? row.duration : "-"),
    },
    { 
      field: "extra_data", 
      title: "额外数据", 
      width: 200,
      formatter: ({ row }: { row: UserBehavior }) => formatExtraData(row.extra_data),
    },
    {
      title: "操作",
      width: 120,
      fixed: "right",
      slots: {
        default: ({ row }: { row: UserBehavior }) => [
          h(
            ElButton,
            {
              size: "small",
              type: "primary",
              onClick: () => handleViewDetail(row),
            },
            { default: () => "查看详情" }
          ),
        ],
      },
    },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page }: { page: { currentPage: number; pageSize: number } }, formValues: Record<string, any>) => {
        return await getUserBehaviorApi({
          page: page.currentPage,
          pageSize: page.pageSize,
          ...formValues,
        });
      },
    },
  },
  toolbarConfig: {
    custom: true,
    export: true,
    refresh: true,
    zoom: true,
    search: true,
  },
});

// 初始化Grid
const [Grid, gridApi] = useVbenVxeGrid({ 
  gridOptions: gridOptions as any, 
  formOptions 
});

// 获取边框和条纹状态
const showBorder = gridApi.useStore((state) => state.gridOptions?.border);
const showStripe = gridApi.useStore((state) => state.gridOptions?.stripe);

// 切换边框显示
function changeBorder() {
  gridApi.setGridOptions({
    border: !showBorder.value,
  });
}

// 切换条纹显示
function changeStripe() {
  gridApi.setGridOptions({
    stripe: !showStripe.value,
  });
}

// 显示加载状态
function changeLoading() {
  gridApi.setLoading(true);
  setTimeout(() => {
    gridApi.setLoading(false);
  }, 2000);
}

// 查看详情
const handleViewDetail = (row: UserBehavior) => {
  ElMessage.info(`查看用户"${row.user_name}"的行为详情功能待实现`);
};

// 导出数据
const handleExport = () => {
  ElMessage.info("导出用户行为数据功能待实现");
};

// 生成统计报表
const handleGenerateReport = () => {
  ElMessage.info("生成用户行为统计报表功能待实现");
};
</script>

<style scoped>
.mr-2 {
  margin-right: 8px;
}
</style>