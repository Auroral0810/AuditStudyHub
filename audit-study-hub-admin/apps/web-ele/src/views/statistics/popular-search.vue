<template>
  <Page title="流行搜索记录" description="查看用户的热门搜索记录和统计">
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

import { MOCK_POPULAR_SEARCH_DATA, DEVICE_TYPE_OPTIONS } from "./popular-search-data";
import type { PopularSearch } from "./popular-search-data";

const sleep = (time = 500) => new Promise((resolve) => setTimeout(resolve, time));

// 搜索表单配置
const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入搜索关键词",
      },
      fieldName: "keyword",
      label: "搜索关键词",
    },
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入用户名",
      },
      fieldName: "user_name",
      label: "用户名",
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
        type: "daterange",
        valueFormat: "YYYY-MM-DD",
        placeholder: "请选择搜索时间范围",
      },
      fieldName: "date_range",
      label: "搜索时间",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: [
          { label: "正常", value: 0 },
          { label: "已删除", value: 1 },
        ],
        placeholder: "请选择是否删除",
      },
      fieldName: "is_deleted",
      label: "是否删除",
    },
  ],
  showCollapseButton: true,
  submitButtonOptions: {
    content: "查询",
  },
  submitOnChange: false,
  submitOnEnter: true,
};

async function getPopularSearchApi(params: {
  page: number;
  pageSize: number;
  keyword?: string;
  user_name?: string;
  device_type?: string;
  date_range?: string[];
  is_deleted?: number;
}) {
  const { 
    page, 
    pageSize, 
    keyword, 
    user_name, 
    device_type, 
    date_range, 
    is_deleted 
  } = params;
  
  const all = MOCK_POPULAR_SEARCH_DATA.filter((item) => {
    if (keyword && !item.keyword.includes(keyword)) return false;
    if (user_name && !item.user_name.includes(user_name)) return false;
    if (device_type && item.device_type !== device_type) return false;
    if (is_deleted !== undefined && item.is_deleted !== is_deleted) return false;
    
    // 处理日期范围
    if (date_range && date_range.length === 2) {
      const start = date_range[0];
      const end = date_range[1];
      if (start && end) {
        const itemDate = item.search_time.split(" ")[0];
        if (itemDate < start || itemDate > end) return false;
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

// 流行搜索数据
const popularSearchData = ref<PopularSearch[]>([...MOCK_POPULAR_SEARCH_DATA]);

// 获取设备类型名称
const getDeviceTypeName = (deviceType: string): string => {
  const option = DEVICE_TYPE_OPTIONS.find(item => item.value === deviceType);
  return option ? option.label : deviceType;
};

// 表格配置
const gridOptions = reactive({
  border: true,
  showOverflow: true,
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
    { field: "keyword", title: "搜索关键词", minWidth: 200 },
    { field: "search_count", title: "搜索次数", width: 100 },
    { field: "user_id", title: "用户ID", width: 100 },
    { field: "user_name", title: "用户名称", width: 120 },
    { 
      field: "device_type", 
      title: "设备类型", 
      width: 100,
      formatter: ({ row }: { row: PopularSearch }) => getDeviceTypeName(row.device_type),
    },
    { field: "ip_address", title: "IP地址", width: 150 },
    { field: "search_time", title: "搜索时间", width: 180 },
    { field: "result_count", title: "结果数量", width: 100 },
    {
      field: "is_deleted",
      title: "是否删除",
      width: 100,
      formatter: ({ row }: { row: PopularSearch }) => (row.is_deleted === 0 ? "正常" : "已删除"),
    },
    {
      title: "操作",
      width: 180,
      fixed: "right",
      slots: {
        default: ({ row }: { row: PopularSearch }) => [
          h(
            ElButton,
            {
              size: "small",
              type: "primary",
              onClick: () => handleView(row),
            },
            { default: () => "查看详情" }
          ),
          h(
            ElButton,
            {
              size: "small",
              type: "danger",
              onClick: () => handleDelete(row),
              disabled: row.is_deleted === 1,
            },
            { default: () => "删除" }
          ),
        ],
      },
    },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page }: { page: { currentPage: number; pageSize: number } }, formValues: Record<string, any>) => {
        return await getPopularSearchApi({
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
const handleView = (row: PopularSearch) => {
  ElMessage.info(`查看用户"${row.user_name}"的"${row.keyword}"搜索详情功能待实现`);
};

// 删除搜索记录
const handleDelete = (row: PopularSearch) => {
  ElMessage.info("删除搜索记录功能待实现");
};

// 导出搜索数据
const handleExport = () => {
  ElMessage.info("导出搜索数据功能待实现");
};

// 生成统计报表
const handleGenerateReport = () => {
  ElMessage.info("生成统计报表功能待实现");
};
</script>

<style scoped>
.mr-2 {
  margin-right: 8px;
}
</style>