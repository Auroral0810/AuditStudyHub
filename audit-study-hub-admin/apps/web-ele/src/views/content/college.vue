<script lang="ts" setup>
import { computed, h, onMounted, reactive, ref, watch } from "vue";

import { Page } from "@vben/common-ui";

import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElImage,
  ElInput,
  ElMessageBox,
  ElMessage,
  ElOption,
  ElSelect,
} from "element-plus";

import { useVbenVxeGrid } from "../../adapter/vxe-table";
import type { VbenFormProps } from "../../adapter/form";

import { getCollegesApi, getCollegeDetailApi, addCollegeApi, updateCollegeApi, deleteCollegeApi } from "../../api/core/college";
import type { College } from "./college-data";

const sleep = (time = 500) => new Promise((resolve) => setTimeout(resolve, time));

// 搜索表单配置
const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入学院名称",
      },
      fieldName: "name",
      label: "学院名称",
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

async function getCollegeApi(params: {
  page: number;
  pageSize: number;
  name?: string;
  is_deleted?: number;
}) {
  try {
    // 获取真实数据
    const allItems = await getCollegesApi();
    
    // 过滤数据
    const filteredItems = allItems.filter((item) => {
      if (params.is_deleted !== undefined && item.is_deleted !== params.is_deleted) return false;
      if (params.name && !item.name.includes(params.name)) return false;
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
        cover_url: item.coverUrl,
        logo_url: item.logoUrl,
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
    
    console.error('获取学院数据失败:', error);
    ElMessage.error('获取学院数据失败');
    return {
      total: 0,
      items: [],
    };
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

// 加载学院数据函数
async function loadCollegeData() {
  try {
    collegeData.value = await getCollegesApi();
    afterDataChange();
  } catch (error) {
    console.error('加载学院数据失败:', error);
    ElMessage.error('加载学院数据失败');
  }
}

// 表单数据
const formData = reactive({
  id: 0,
  name: "",
  cover_url: "",
  logo_url: "",
  is_deleted: 0,
});

// 学院数据
const collegeData = ref<College[]>([]);

// 弹窗控制
const showModal = ref(false);
const modalTitle = ref("添加学院");
const isEdit = ref(false);

const pager = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});

const filteredData = computed(() =>
  collegeData.value.filter((item) => item.is_deleted === 0)
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
    { field: "name", title: "学院名称", minWidth: 200 },
    {
      field: "logo_url",
      title: "学院Logo",
      width: 100,
      slots: {
        default: ({ row }: { row: College }) =>
          h(ElImage, {
            src: row.logo_url,
            style: "width: 40px; height: 40px;",
            previewSrcList: [row.logo_url],
          }),
      },
    },
    {
      field: "cover_url",
      title: "学院封面",
      width: 120,
      slots: {
        default: ({ row }: { row: College }) =>
          h(ElImage, {
            src: row.cover_url,
            style: "width: 80px; height: 45px;",
            previewSrcList: [row.cover_url],
          }),
      },
    },
    { field: "create_time", title: "创建时间", width: 180 },
    { field: "update_time", title: "更新时间", width: 180 },
    {
      field: "is_deleted",
      title: "状态",
      width: 100,
      slots: {
        default: ({ row }: { row: College }) => 
          row.is_deleted === 0 ? "正常" : "已删除",
      },
    },
    {
      title: "操作",
      width: 160,
      slots: {
        default: ({ row }: { row: College }) => [
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
        return await getCollegeApi({
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

// 添加学院
const handleAdd = () => {
  resetForm();
  modalTitle.value = "添加学院";
  isEdit.value = false;
  showModal.value = true;
  afterDataChange();
};

// 编辑学院
const handleEdit = (row: College) => {
  formData.id = row.id;
  formData.name = row.name;
  formData.cover_url = row.cover_url;
  formData.logo_url = row.logo_url;
  formData.is_deleted = row.is_deleted;
  modalTitle.value = "编辑学院";
  isEdit.value = true;
  showModal.value = true;
  afterDataChange();
};

// 删除学院
const handleDelete = (row: College) => {
  ElMessageBox.confirm(`确认删除学院"${row.name}"吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        // 调用删除API
        await deleteCollegeApi(row.id);
        
        // 重新加载数据
        await loadCollegeData();
        
        ElMessage.success("删除成功");
      } catch (error) {
        console.error("删除学院失败", error);
        ElMessage.error("删除学院失败");
      }
    })
    .catch(() => {});
};

// 提交表单
const handleSubmit = async () => {
  try {
    if (!formData.name) {
      ElMessage.warning("请输入学院名称");
      return;
    }

    if (!formData.logo_url) {
      ElMessage.warning("请输入学院Logo地址");
      return;
    }

    if (!formData.cover_url) {
      ElMessage.warning("请输入学院封面地址");
      return;
    }

    if (isEdit.value) {
      // 更新学院
      await updateCollegeApi({
        id: formData.id,
        name: formData.name,
        cover_url: formData.cover_url,
        logo_url: formData.logo_url,
        is_deleted: formData.is_deleted,
      });
      ElMessage.success("更新成功");
    } else {
      // 添加学院
      await addCollegeApi({
        name: formData.name,
        cover_url: formData.cover_url,
        logo_url: formData.logo_url,
        is_deleted: formData.is_deleted,
      });
      ElMessage.success("添加成功");
    }

    // 重新加载数据
    await loadCollegeData();
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
  formData.logo_url = "";
  formData.cover_url = "";
  formData.is_deleted = 0;
};

// 页面加载后确保数据正确显示
onMounted(async () => {
  await loadCollegeData();
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
  <Page title="学院管理" description="进行学院管理">
    <template #extra>
      <ElButton type="primary" @click="handleAdd">添加学院</ElButton>
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
        <ElFormItem label="学院名称">
          <ElInput v-model="formData.name" placeholder="请输入学院名称" />
        </ElFormItem>
        <ElFormItem label="Logo地址">
          <ElInput v-model="formData.logo_url" placeholder="请输入Logo图片地址" />
        </ElFormItem>
        <ElFormItem label="封面地址">
          <ElInput v-model="formData.cover_url" placeholder="请输入封面图片地址" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="formData.is_deleted" style="width: 100%">
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