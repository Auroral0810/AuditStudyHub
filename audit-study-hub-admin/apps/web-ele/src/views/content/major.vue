<script lang="ts" setup>
import { computed, h, onMounted, reactive, ref, watch } from "vue";

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
  ElOption,
  ElSelect,
} from "element-plus";

import { useVbenVxeGrid } from "../../adapter/vxe-table";
import type { VbenFormProps } from "../../adapter/form";

import { 
  XL_OPTIONS,

  XZ_OPTIONS,
} from "./major-data";
import type { Major } from "./major-data";
import { getCollegesApi } from "../../api/core/college";

import { 
  getMajorsApi, 
  getMajorDetailApi, 
  getMajorsByCollegeApi, 
  addMajorApi, 
  updateMajorApi, 
  deleteMajorApi 
} from "../../api/core/major";

const sleep = (time = 500) => new Promise((resolve) => setTimeout(resolve, time));

// 添加学院选项状态
const collegeOptions = ref<{id: number, name: string}[]>([]);

// 获取学院数据的函数
async function loadCollegeOptions() {
  try {
    const colleges = await getCollegesApi();
    // 只获取未删除的学院
    collegeOptions.value = colleges
      .filter(college => college.is_deleted === 0)
      .map(college => ({
        id: college.id,
        name: college.name
      }));
  } catch (error) {
    console.error('加载学院数据失败:', error);
    ElMessage.error('加载学院数据失败');
  }
}

// 搜索表单配置
const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入专业名称",
      },
      fieldName: "name",
      label: "专业名称",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: computed(() => collegeOptions.value.map(item => ({ label: item.name, value: item.id }))),
        placeholder: "请选择学院",
      },
      fieldName: "college_id",
      label: "所属学院",
    },
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入学位类型",
      },
      fieldName: "degree",
      label: "学位类型",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: XL_OPTIONS,
        placeholder: "请选择学历等级",
      },
      fieldName: "xl",
      label: "学历等级",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: XZ_OPTIONS,
        placeholder: "请选择学制",
      },
      fieldName: "xz",
      label: "学制",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: [
          { label: "全部", value: undefined },
          { label: "正常", value: 0 },
          { label: "已删除", value: 1 },
        ],
        placeholder: "请选择状态",
      },
      fieldName: "is_deleted",
      label: "状态",
    },
  ],
  showCollapseButton: true,
  submitButtonOptions: {
    content: "查询",
  },
  submitOnChange: false,
  submitOnEnter: true,
};

async function getMajorApi(params: {
  page: number;
  pageSize: number;
  name?: string;
  college_id?: number;
  degree?: string;
  xl?: number;
  xz?: string;
  is_deleted?: number;
}) {
  try {
    // 获取真实数据
    const allItems = await getMajorsApi();
    
    // 过滤数据
    const filteredItems = allItems.filter((item) => {
      if (params.is_deleted !== undefined && item.is_deleted !== params.is_deleted) return false;
      if (params.name && !item.name.includes(params.name)) return false;
      if (params.college_id !== undefined && item.college_id !== params.college_id) return false;
      if (params.degree && item.degree && !item.degree.includes(params.degree)) return false;
      if (params.xl !== undefined && item.xl !== params.xl) return false;
      if (params.xz && item.xz && !item.xz.includes(params.xz)) return false;
      return true;
    });
    
    // 分页
    const items = filteredItems.slice(
      (params.page - 1) * params.pageSize,
      params.page * params.pageSize
    );
    
    return {
      total: filteredItems.length,
      items,
    };
  } catch (error: any) {
    // 检查是否是预期的成功响应，但格式不匹配
    if (error.response?.data?.code === 20000 && error.response?.data?.data) {
      const items = error.response.data.data.map(item => ({
        id: item.id,
        name: item.name,
        college_id: item.collegeId,
        college_name: item.collegeName,
        degree: item.degree,
        xl: item.xl,
        xz: item.xz,
        create_time: formatDate(item.createTime),
        update_time: formatDate(item.updateTime),
        is_deleted: item.isDeleted
      }));
      return {
        total: items.length,
        items: items.slice(
          (params.page - 1) * params.pageSize,
          params.page * params.pageSize
        )
      };
    }
    
    console.error('获取专业数据失败:', error);
    ElMessage.error('获取专业数据失败');
    return {
      total: 0,
      items: [],
    };
  }
}

// 修改获取学院名称的方法
const getCollegeName = (row: Major): string => {
  return row.college_name || collegeOptions.value.find(item => item.id === row.college_id)?.name || "未知学院";
};

// 学历等级名称映射
const getXlName = (xl: number): string => {
  const xlOption = XL_OPTIONS.find((item) => item.value === xl);
  return xlOption ? xlOption.label : "未设置";
};

// 表单数据
const formData = reactive({
  id: 0,
  name: "",
  college_id: 0,
  degree: null,
  xl: 0,
  xz: null,
  is_deleted: 0,
});

// 专业数据
const majorData = ref<Major[]>([]);

// 弹窗控制
const showModal = ref(false);
const modalTitle = ref("添加专业");
const isEdit = ref(false);

const pager = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});

const filteredData = computed(() =>
  majorData.value.filter((item) => item.is_deleted === 0)
);

const pagedData = computed(() => {
  const start = (pager.currentPage - 1) * pager.pageSize;
  const end = start + pager.pageSize;
  return filteredData.value.slice(start, end);
});

// 表格配置
const gridOptions = {
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
    pageSizes: [5, 10, 15, 50, 100, 200],
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
    { field: "name", title: "专业名称", minWidth: 220 },
    { 
      field: "college_id", 
      title: "所属学院", 
      width: 180,
      formatter: ({ row }) => getCollegeName(row),
    },
    { field: "degree", title: "学位类型", width: 120 },
    { 
      field: "xl", 
      title: "学历等级", 
      width: 100,
      formatter: ({ row }: { row: Major }) => getXlName(row.xl),
    },
    { field: "xz", title: "学制", width: 100 },
    { field: "create_time", title: "创建时间", width: 180 },
    { field: "update_time", title: "更新时间", width: 180 },
    {
      field: "is_deleted",
      title: "状态",
      width: 80,
      slots: {
        default: ({ row }: { row: Major }) => 
          row.is_deleted === 0 ? "正常" : "已删除",
      },
    },
    {
      title: "操作",
      width: 160,
      slots: {
        default: ({ row }: { row: Major }) => [
          h(
            ElButton,
            {
              size: "small",
              type: "primary",
              onClick: () => handleEdit(row),
            },
            { default: () => "编辑" }
          ),
          h(
            ElButton,
            {
              size: "small",
              type: "danger",
              onClick: () => handleDelete(row),
            },
            { default: () => "删除" }
          ),
        ],
      },
    },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page }, formValues) => {
        return await getMajorApi({
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
};

// 初始化Grid
const [Grid, gridApi] = useVbenVxeGrid({ gridOptions, formOptions });

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

// 添加专业
const handleAdd = () => {
  resetForm();
  modalTitle.value = "添加专业";
  isEdit.value = false;
  showModal.value = true;
  afterDataChange();
};

// 编辑专业
const handleEdit = (row: Major) => {
  formData.id = row.id;
  formData.name = row.name;
  formData.college_id = row.college_id;
  formData.degree = row.degree;
  formData.xl = row.xl;
  formData.xz = row.xz;
  formData.is_deleted = row.is_deleted;
  modalTitle.value = "编辑专业";
  isEdit.value = true;
  showModal.value = true;
  afterDataChange();
};

// 删除专业
const handleDelete = (row: Major) => {
  ElMessageBox.confirm(`确认删除专业"${row.name}"吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        // 调用删除API
        await deleteMajorApi(row.id);
        
        // 重新加载数据
        await loadMajorData();
        
        ElMessage.success("删除成功");
      } catch (error) {
        console.error("删除专业失败", error);
        ElMessage.error("删除专业失败");
      }
    })
    .catch(() => {});
};

// 提交表单
const handleSubmit = async () => {
  try {
    if (!formData.name) {
      ElMessage.warning("请输入专业名称");
      return;
    }

    if (!formData.college_id) {
      ElMessage.warning("请选择所属学院");
      return;
    }

    if (isEdit.value) {
      // 更新专业
      await updateMajorApi({
        id: formData.id,
        name: formData.name,
        college_id: formData.college_id,
        degree: formData.degree,
        xl: formData.xl,
        xz: formData.xz,
        is_deleted: formData.is_deleted,
      });
      ElMessage.success("更新成功");
    } else {
      // 添加专业
      await addMajorApi({
        name: formData.name,
        college_id: formData.college_id,
        degree: formData.degree,
        xl: formData.xl,
        xz: formData.xz,
        is_deleted: formData.is_deleted,
      });
      ElMessage.success("添加成功");
    }

    // 重新加载数据
    await loadMajorData();
    showModal.value = false;
  } catch (error: any) {
    // 处理特定的错误消息
    const errorMsg = error.response?.data?.message || "提交失败";
    console.error("提交表单失败", error);
    ElMessage.error(errorMsg);
  }
};

// 重置表单
const resetForm = () => {
  formData.id = 0;
  formData.name = "";
  formData.college_id = 0;
  formData.degree = null;
  formData.xl = 0;
  formData.xz = null;
  formData.is_deleted = 0;
};

// 页面加载后确保数据正确显示
onMounted(async () => {
  await loadCollegeOptions(); // 先加载学院数据
  await loadMajorData();
  pager.total = filteredData.value.length;
  gridApi.setGridOptions({ data: pagedData.value });
});

watch(
  [() => filteredData.value.length, () => pager.currentPage, () => pager.pageSize],
  () => {
    pager.total = filteredData.value.length;
    gridApi.setGridOptions({ data: pagedData.value });
  }
);

const handlePageChange = ({ currentPage, pageSize }) => {
  pager.currentPage = currentPage;
  pager.pageSize = pageSize;
  gridApi.setGridOptions({ data: pagedData.value });
};

// 数据变动后刷新
function afterDataChange() {
  gridApi.query();
}

// 加载专业数据函数
async function loadMajorData() {
  try {
    majorData.value = await getMajorsApi();
    afterDataChange();
  } catch (error) {
    console.error('加载专业数据失败:', error);
    ElMessage.error('加载专业数据失败');
  }
}

// 格式化日期辅助函数
function formatDate(dateStr: string): string {
  if (!dateStr) return '';
  try {
    const date = new Date(dateStr);
    return date.toISOString().replace('T', ' ').slice(0, 19);
  } catch (e) {
    return dateStr;
  }
}
</script>

<template>
  <Page title="专业管理" description="进行专业管理">
    <template #extra>
      <ElButton type="primary" @click="handleAdd">添加专业</ElButton>
    </template>
    <!-- 表格 -->
    <Grid @page-change="handlePageChange">
      <template #toolbar-tools>
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
    <!-- 表单弹窗 -->
    <ElDialog
      v-model="showModal"
      :title="modalTitle"
      width="600px"
      append-to-body
    >
      <ElForm label-width="100px">
        <ElFormItem label="专业名称">
          <ElInput v-model="formData.name" placeholder="请输入专业名称" />
        </ElFormItem>
        <ElFormItem label="所属学院">
          <ElSelect v-model="formData.college_id" placeholder="请选择所属学院" style="width: 100%">
            <ElOption
              v-for="item in collegeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="学位类型">
          <ElInput v-model="formData.degree" placeholder="请输入学位类型" />
        </ElFormItem>
        <ElFormItem label="学历等级">
          <ElSelect v-model="formData.xl" placeholder="请选择学历等级" style="width: 100%">
            <ElOption
              v-for="item in XL_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="学制">
          <ElSelect v-model="formData.xz" placeholder="请选择学制" style="width: 100%">
            <ElOption
              v-for="item in XZ_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="formData.is_deleted" style="width: 100%">
            <ElOption label="正常" :value="0" />
            <ElOption label="已删除" :value="1" />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="showModal = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>
  </Page>
</template>
