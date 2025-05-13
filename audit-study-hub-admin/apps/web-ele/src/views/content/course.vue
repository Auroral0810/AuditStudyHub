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

import type { Course } from "./course-data";
import { 
  getCoursesApi, 
  getCourseDetailApi, 
  searchCoursesApi, 
  addCourseApi, 
  updateCourseApi, 
  deleteCourseApi 
} from "../../api/core/course";

const sleep = (time = 500) => new Promise((resolve) => setTimeout(resolve, time));

// 搜索表单配置
const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入课程名称",
      },
      fieldName: "name",
      label: "课程名称",
    },
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入课程描述",
        type: "textarea",
        rows: 2,
      },
      fieldName: "description",
      label: "课程描述",
    },
    {
      component: "InputNumber",
      componentProps: {
        placeholder: "请输入学分",
        min: 0,
        precision: 2,
      },
      fieldName: "credit",
      label: "学分",
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

async function getCourseApi(params: {
  page: number;
  pageSize: number;
  name?: string;
  description?: string;
  credit?: number;
  is_deleted?: number;
}) {
  try {
    // 获取真实数据
    const allItems = await getCoursesApi();
    
    // 过滤数据
    const filteredItems = allItems.filter((item) => {
      if (params.is_deleted !== undefined && item.is_deleted !== params.is_deleted) return false;
      if (params.name && !item.name.includes(params.name)) return false;
      if (params.description && item.description && !item.description.includes(params.description)) return false;
      if (params.credit !== undefined && item.credit !== params.credit) return false;
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
  } catch (error) {
    console.error('获取课程数据失败:', error);
    ElMessage.error('获取课程数据失败');
    return {
      total: 0,
      items: [],
    };
  }
}

// 表单数据
const formData = reactive({
  id: 0,
  name: "",
  description: null as null | string,
  credit: 0,
  is_deleted: 0,
});

// 课程数据
const courseData = ref<Course[]>([]);

// 弹窗控制
const showModal = ref(false);
const modalTitle = ref("添加课程");
const isEdit = ref(false);

const pager = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});

const filteredData = computed(() =>
  courseData.value.filter((item) => item.is_deleted === 0)
);

const pagedData = computed(() => {
  const start = (pager.currentPage - 1) * pager.pageSize;
  const end = start + pager.pageSize;
  return filteredData.value.slice(start, end);
});

// 表格配置
const gridOptions = {
  border: true,
  stripe: false,
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
    pageSizes: [5, 10, 20, 50, 100, 200],
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
    { field: "name", title: "课程名称", minWidth: 220 },
    { field: "description", title: "课程描述", minWidth: 180 },
    {
      field: "credit",
      title: "学分",
      width: 80,
      formatter: ({ cellValue }: { cellValue: number }) => cellValue?.toFixed(2) || "0.00",
    },
    { field: "create_time", title: "创建时间", width: 180 },
    { field: "update_time", title: "更新时间", width: 180 },
    {
      field: "is_deleted",
      title: "状态",
      width: 80,
      slots: {
        default: ({ row }: { row: Course }) => 
          row.is_deleted === 0 ? "正常" : "已删除",
      },
    },
    {
      title: "操作",
      width: 160,
      slots: {
        default: ({ row }: { row: Course }) => [
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
        return await getCourseApi({
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

// 添加课程
const handleAdd = () => {
  resetForm();
  modalTitle.value = "添加课程";
  isEdit.value = false;
  showModal.value = true;
  afterDataChange();
};

// 编辑课程
const handleEdit = (row: Course) => {
  formData.id = row.id;
  formData.name = row.name;
  formData.description = row.description;
  formData.credit = row.credit;
  formData.is_deleted = row.is_deleted;
  modalTitle.value = "编辑课程";
  isEdit.value = true;
  showModal.value = true;
  afterDataChange();
};

// 删除课程
const handleDelete = (row: Course) => {
  ElMessageBox.confirm(`确认删除课程"${row.name}"吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        // 调用后端接口删除课程
        await deleteCourseApi(row.id);
        
        // 重新加载课程数据
        await loadCourseData();
        
        ElMessage.success("删除成功");
      } catch (error) {
        console.error("删除课程失败", error);
        ElMessage.error("删除课程失败");
      }
    })
    .catch(() => {});
};

// 提交表单
const handleSubmit = async () => {
  try {
    if (!formData.name) {
      ElMessage.warning("请输入课程名称");
      return;
    }

    if (formData.credit <= 0) {
      ElMessage.warning("请输入课程学分");
      return;
    }

    if (isEdit.value) {
      // 更新课程
      await updateCourseApi({
        id: formData.id,
        name: formData.name,
        description: formData.description,
        credit: formData.credit,
        is_deleted: formData.is_deleted
      });
      ElMessage.success("更新成功");
    } else {
      // 添加课程
      await addCourseApi({
        name: formData.name,
        description: formData.description,
        credit: formData.credit,
        is_deleted: formData.is_deleted
      });
      ElMessage.success("添加成功");
    }

    // 重新加载课程数据
    await loadCourseData();
    showModal.value = false;
  } catch (error) {
    console.error("提交表单失败", error);
    ElMessage.error("提交失败");
  }
};

// 重置表单
const resetForm = () => {
  formData.id = 0;
  formData.name = "";
  formData.description = null;
  formData.credit = 0;
  formData.is_deleted = 0;
};

// 加载课程数据
async function loadCourseData() {
  try {
    courseData.value = await getCoursesApi();
    afterDataChange();
  } catch (error) {
    console.error('加载课程数据失败:', error);
    ElMessage.error('加载课程数据失败');
  }
}

// 页面加载后获取课程数据
onMounted(async () => {
  await loadCourseData();
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
</script>

<template>
  <Page title="课程管理" description="进行课程管理">
    <template #extra>
      <ElButton type="primary" @click="handleAdd">添加课程</ElButton>
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
      width="500px"
      append-to-body
    >
      <ElForm label-width="100px">
        <ElFormItem label="课程名称">
          <ElInput v-model="formData.name" placeholder="请输入课程名称" />
        </ElFormItem>
        <ElFormItem label="课程描述">
          <ElInput 
            v-model="formData.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入课程描述" 
          />
        </ElFormItem>
        <ElFormItem label="课程学分">
          <ElInputNumber 
            v-model="formData.credit" 
            :min="0" 
            :max="10"
            :precision="2"
            :step="0.5" 
            style="width: 100%" 
          />
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
