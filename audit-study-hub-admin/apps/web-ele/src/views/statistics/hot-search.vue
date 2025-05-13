<template>
  <Page title="热门搜索管理" description="管理系统中的热门搜索词条，支持手动添加和自动生成">
    <!-- 表格 -->
    <Grid>
      <template #toolbar-tools>
        <ElButton class="mr-2" type="primary" @click="handleAddHotSearch">
          添加热搜
        </ElButton>
        <ElButton class="mr-2" type="success" @click="handleAutoGenerate">
          自动生成热搜
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

    <!-- 添加/编辑热搜对话框 -->
    <ElDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑热搜' : '添加热搜'"
      width="500px"
      destroy-on-close
    >
      <ElForm
        ref="formRef"
        :model="formData"
        label-width="100px"
        :rules="formRules"
      >
        <ElFormItem label="搜索关键词" prop="keyword">
          <ElInput v-model="formData.keyword" placeholder="请输入搜索关键词" />
        </ElFormItem>
        <ElFormItem label="权重" prop="weight">
          <ElInputNumber
            v-model="formData.weight"
            :min="1"
            :max="100"
            style="width: 100%"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="dialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="handleSaveHotSearch">
            确定
          </ElButton>
        </div>
      </template>
    </ElDialog>
  </Page>
</template>

<script lang="ts" setup>
import { h, ref, reactive } from "vue";

import { Page } from "@vben/common-ui";
import { 
  ElButton, 
  ElDialog, 
  ElForm, 
  ElFormItem, 
  ElInput, 
  ElInputNumber, 
  ElMessage,
  ElMessageBox,
  ElTag 
} from "element-plus";
import type { FormRules, FormInstance } from "element-plus";

import { useVbenVxeGrid } from "../../adapter/vxe-table";
import type { VbenFormProps } from "../../adapter/form";

import { MOCK_HOT_SEARCH_DATA } from "./hot-search-data";
import type { HotSearch } from "./hot-search-data";

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
      component: "Select",
      componentProps: {
        allowClear: true,
        options: [
          { label: "系统生成", value: 0 },
          { label: "手动添加", value: 1 },
        ],
        placeholder: "请选择添加方式",
      },
      fieldName: "is_manual",
      label: "添加方式",
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
    {
      component: "DatePicker",
      componentProps: {
        type: "daterange",
        valueFormat: "YYYY-MM-DD",
        placeholder: "请选择创建时间范围",
      },
      fieldName: "date_range",
      label: "创建时间",
    },
  ],
  showCollapseButton: true,
  submitButtonOptions: {
    content: "查询",
  },
  submitOnChange: false,
  submitOnEnter: true,
};

async function getHotSearchApi(params: {
  page: number;
  pageSize: number;
  keyword?: string;
  is_manual?: number;
  is_deleted?: number;
  date_range?: string[];
}) {
  const { 
    page, 
    pageSize, 
    keyword, 
    is_manual, 
    is_deleted, 
    date_range 
  } = params;
  
  const all = MOCK_HOT_SEARCH_DATA.filter((item) => {
    if (keyword && !item.keyword.includes(keyword)) return false;
    if (is_manual !== undefined && item.is_manual !== is_manual) return false;
    if (is_deleted !== undefined && item.is_deleted !== is_deleted) return false;
    
    // 处理日期范围
    if (date_range && date_range.length === 2) {
      const start = date_range[0];
      const end = date_range[1];
      if (start && end) {
        const itemDate = item.create_time.split(" ")[0];
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
    { field: "weight", title: "权重", width: 100 },
    { field: "daily_count", title: "日搜索量", width: 120 },
    { field: "weekly_count", title: "周搜索量", width: 120 },
    { field: "monthly_count", title: "月搜索量", width: 120 },
    { 
      field: "is_manual", 
      title: "添加方式", 
      width: 120,
      formatter: ({ row }: { row: HotSearch }) => (
        row.is_manual === 0 ? "系统生成" : "手动添加"
      ),
    },
    { 
      field: "creator_name", 
      title: "创建人", 
      width: 120,
      formatter: ({ row }: { row: HotSearch }) => (
        row.creator_name || (row.is_manual === 0 ? "系统" : "未知")
      ),
    },
    { field: "create_time", title: "创建时间", width: 180 },
    { field: "update_time", title: "更新时间", width: 180 },
    {
      field: "is_deleted",
      title: "状态",
      width: 100,
      formatter: ({ row }: { row: HotSearch }) => (row.is_deleted === 0 ? "正常" : "已删除"),
    },
    {
      title: "操作",
      width: 220,
      fixed: "right",
      slots: {
        default: ({ row }: { row: HotSearch }) => [
          h(
            ElButton,
            {
              size: "small",
              type: "primary",
              onClick: () => handleEdit(row),
              disabled: row.is_deleted === 1,
            },
            { default: () => "编辑" }
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
        return await getHotSearchApi({
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

// 对话框相关
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref<FormInstance>();
const formData = reactive<{
  id?: number;
  keyword: string;
  weight: number;
}>({
  keyword: "",
  weight: 50,
});

// 表单验证规则
const formRules = reactive<FormRules>({
  keyword: [
    { required: true, message: "请输入搜索关键词", trigger: "blur" },
    { min: 2, max: 50, message: "长度在2到50个字符之间", trigger: "blur" },
  ],
  weight: [
    { required: true, message: "请输入权重", trigger: "blur" },
    { type: "number", min: 1, max: 100, message: "权重在1到100之间", trigger: "blur" },
  ],
});

// 添加热搜
const handleAddHotSearch = () => {
  isEdit.value = false;
  formData.id = undefined;
  formData.keyword = "";
  formData.weight = 50;
  dialogVisible.value = true;
};

// 编辑热搜
const handleEdit = (row: HotSearch) => {
  isEdit.value = true;
  formData.id = row.id;
  formData.keyword = row.keyword;
  formData.weight = row.weight;
  dialogVisible.value = true;
};

// 保存热搜
const handleSaveHotSearch = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate((valid) => {
    if (valid) {
      if (isEdit.value) {
        ElMessage.success(`已成功更新热搜"${formData.keyword}"`);
      } else {
        ElMessage.success(`已成功添加热搜"${formData.keyword}"`);
      }
      dialogVisible.value = false;
      gridApi.reload(); // 重新加载数据
    }
  });
};

// 删除热搜
const handleDelete = (row: HotSearch) => {
  ElMessageBox.confirm(`确定要删除热搜"${row.keyword}"吗?`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    ElMessage.success(`已成功删除热搜"${row.keyword}"`);
    gridApi.reload(); // 重新加载数据
  }).catch(() => {
    // 取消删除
  });
};

// 自动生成热搜
const handleAutoGenerate = () => {
  ElMessage.success("系统正在自动生成热搜，请稍后查看");
  setTimeout(() => {
    gridApi.reload();
  }, 1000);
};
</script>

<style scoped>
.mr-2 {
  margin-right: 8px;
}
</style>
