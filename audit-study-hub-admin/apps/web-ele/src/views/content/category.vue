<script lang="ts" setup>
import { h, onMounted, reactive, ref, computed, watch } from "vue";

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
  ElImage,
  ElIcon,
} from "element-plus";

import { useVbenVxeGrid } from "../../adapter/vxe-table";
import type { VbenFormProps } from "../../adapter/form";

import { ICON_OPTIONS, ICON_COMPONENT_MAP } from "./category-data";
import type { Category } from "./category-data";

import {
  Notebook,
  DataAnalysis,
  Document,
  Reading,
  VideoPlay,
  DocumentChecked,
  Folder,
  Picture,
  Upload,
  Download,
} from "@element-plus/icons-vue";

// 导入API函数
import { getCategoriesApi, addCategoryApi, updateCategoryApi, deleteCategoryApi } from "../../api/core/category";

const sleep = (time = 500) => new Promise((resolve) => setTimeout(resolve, time));

// 搜索表单配置
const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入分类名称',
      },
      fieldName: 'name',
      label: '分类名称',
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: ICON_OPTIONS,
        placeholder: '请选择图标',
      },
      fieldName: 'icon',
      label: '分类图标',
    },
    {
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入排序',
      },
      fieldName: 'sort',
      label: '排序',
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: "全部", value: undefined },
          { label: "正常", value: 0 },
          { label: "已删除", value: 1 },
        ],
        placeholder: '请选择状态',
      },
      fieldName: 'is_deleted',
      label: '状态',
    },
  ],
  showCollapseButton: true,
  submitButtonOptions: {
    content: '查询',
  },
  submitOnChange: false,
  submitOnEnter: true,
};

// 真实API调用替换模拟数据函数
async function getCategoryApi(params: { 
  page: number; 
  pageSize: number, 
  name?: string, 
  icon?: string, 
  sort?: number,
  is_deleted?: number
}) {
  try {
    // 获取真实数据
    const allItems = await getCategoriesApi();
    
    // 过滤数据
    const filteredItems = allItems.filter((item) => {
      if (params.is_deleted !== undefined && item.is_deleted !== params.is_deleted) return false;
      if (params.name && !item.name.includes(params.name)) return false;
      if (params.icon && item.icon !== params.icon) return false;
      if (params.sort !== undefined && item.sort !== params.sort) return false;
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
        icon: item.icon,
        sort: item.sort,
        create_time: item.createTime,
        update_time: item.updateTime,
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
    
    console.error('获取分类数据失败:', error);
    ElMessage.error('获取分类数据失败');
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
  icon: "",
  sort: 0,
  isDeleted: 0,
});

// 分类数据
const categoryData = ref<Category[]>([]);

// 弹窗控制
const showModal = ref(false);
const modalTitle = ref("添加分类");
const isEdit = ref(false);

const pager = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});

const filteredData = computed(() =>
  categoryData.value.filter((item) => item.is_deleted === 0)
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
    {
      field: "icon",
      title: "分类图标",
      width: 100,
      slots: {
        default: ({ row }) => h(
          ElIcon,
          { 
            size: 24,
            style: "color: var(--el-color-primary);"
          },
          {
            default: () => h(ICON_COMPONENT_MAP[row.icon])
          }
        )
      }
    },
    { field: "name", title: "分类名称", minWidth: 120 },
    { field: "sort", title: "排序", width: 80 },
    { field: "create_time", title: "创建时间", width: 180 },
    { field: "update_time", title: "更新时间", width: 180 },
    {
      field: "is_deleted",
      title: "状态",
      width: 100,
      slots: {
        default: ({ row }: { row: Category }) => (row.is_deleted === 0 ? "正常" : "已删除"),
      },
    },
    {
      title: "操作",
      width: 160,
      slots: {
        default: ({ row }: { row: Category }) => [
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
        return await getCategoryApi({
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

// 加载分类数据
async function loadCategoryData() {
  try {
    categoryData.value = await getCategoriesApi();
    afterDataChange();
  } catch (error) {
    console.error('加载分类数据失败:', error);
    ElMessage.error('加载分类数据失败');
  }
}

// 添加分类
const handleAdd = () => {
  resetForm();
  modalTitle.value = "添加分类";
  isEdit.value = false;
  showModal.value = true;
};

// 编辑分类
const handleEdit = (row: Category) => {
  formData.id = row.id;
  formData.name = row.name;
  formData.icon = row.icon;
  formData.sort = Number(row.sort);
  formData.isDeleted = row.is_deleted;
  modalTitle.value = "编辑分类";
  isEdit.value = true;
  showModal.value = true;
};

// 删除分类
const handleDelete = (row: Category) => {
  ElMessageBox.confirm(`确认删除分类"${row.name}"吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        // 调用删除API
        await deleteCategoryApi(row.id);
        
        // 重新加载数据
        await loadCategoryData();
        
        ElMessage.success("删除成功");
      } catch (error) {
        console.error("删除分类失败", error);
        ElMessage.error("删除分类失败");
      }
    })
    .catch(() => {});
};

// 提交表单
const handleSubmit = async () => {
  try {
    if (!formData.name) {
      ElMessage.warning("请输入分类名称");
      return;
    }

    if (isEdit.value) {
      // 更新分类
      await updateCategoryApi({
        id: formData.id,
        name: formData.name,
        icon: formData.icon,
        sort: Number(formData.sort),
        isDeleted: formData.isDeleted,
      });
      ElMessage.success("更新成功");
    } else {
      // 添加分类
      await addCategoryApi({
        name: formData.name,
        icon: formData.icon,
        sort: Number(formData.sort),
        isDeleted: formData.isDeleted,
      });
      ElMessage.success("添加成功");
    }

    // 重新加载数据
    await loadCategoryData();
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
  formData.icon = "";
  formData.sort = 0;
  formData.isDeleted = 0;
};

// 页面加载后获取数据
onMounted(async () => {
  await loadCategoryData();
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
  <Page title="分类管理" description="进行分类管理">
    <template #extra>
      <ElButton type="primary" @click="handleAdd">添加分类</ElButton>
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
        <ElFormItem label="分类名称">
          <ElInput v-model="formData.name" placeholder="请输入分类名称" />
        </ElFormItem>
        <ElFormItem label="图标">
          <ElSelect
            v-model="formData.icon"
            placeholder="请选择图标"
            style="width: 100%"
          >
            <ElOption
              v-for="item in ICON_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <ElIcon :size="20">
                <component :is="ICON_COMPONENT_MAP[item.value]" />
              </ElIcon>
              <span style="margin-left: 8px">{{ item.label }}</span>
            </ElOption>
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="排序">
          <ElInputNumber v-model="formData.sort" :min="0" :step="1" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="formData.isDeleted" style="width: 100%">
            <ElOption :label="'正常'" :value="0" />
            <ElOption :label="'已删除'" :value="1" />
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
