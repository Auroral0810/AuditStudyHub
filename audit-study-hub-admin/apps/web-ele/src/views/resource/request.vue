<template>
  <Page title="资源求助" description="发布和管理资源求助">
    <template #extra>
      <ElButton type="primary" @click="handleAdd">发布求助</ElButton>
    </template>
    
    <!-- 表格 -->
    <Grid>
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
    
    <!-- 发布求助弹窗 -->
    <ElDialog
      v-model="showModal"
      :title="isEditMode ? '编辑资源求助' : '发布资源求助'"
      width="600px"
      append-to-body
    >
      <ElForm label-width="100px">
        <ElFormItem label="求助标题" required>
          <ElInput v-model="formData.title" placeholder="请输入求助标题" />
        </ElFormItem>
        <ElFormItem label="求助内容" required>
          <ElInput 
            v-model="formData.content" 
            type="textarea"
            :rows="4"
            placeholder="请输入求助内容，描述您需要的资源" 
          />
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
        <ElFormItem label="资源分类">
          <ElSelect v-model="formData.category_id" placeholder="请选择资源分类" style="width: 100%">
            <ElOption
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="showModal = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">发布</ElButton>
      </template>
    </ElDialog>
  </Page>
</template>

<script lang="ts" setup>
import { h, onMounted, reactive, ref } from "vue";

import { Page } from "@vben/common-ui";

import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElSelect,
  ElMessage,
  ElMessageBox,
  ElOption,
} from "element-plus";

import { useVbenVxeGrid } from "../../adapter/vxe-table";
import type { VbenFormProps } from "../../adapter/form";

import { MOCK_RESOURCE_REQUEST_DATA, STATUS_OPTIONS } from "./request-data";
import type { ResourceRequest } from "./request-data";
import { getCategoriesApi } from "../../api/core/category";
import { getCollegesApi } from "../../api/core/college";
import { processCategoryData, processCollegeData } from "../../utils/api-helpers";
import {
  getRequestsApi,
  getRequestDetailApi,
  searchRequestsApi,
  updateRequestStatusApi,
  updateRequestDeleteStatusApi,
  deleteRequestApi,
  formatDate,
  type AdminRequestDTO,
  createRequestApi,
  updateRequestApi,
  restoreRequestApi
} from "../../api/core/resource-request";

const sleep = (time = 500) => new Promise((resolve) => setTimeout(resolve, time));

// 存储原始数据以供表单使用
const categoryOptions = ref<{id: number, name: string}[]>([]);
const collegeOptions = ref<{id: number, name: string}[]>([]);

// 修改分类和学院数据状态定义
const categoryData = ref<{ label: string, value: number }[]>([]);
const collegeData = ref<{ label: string, value: number }[]>([]);

// 添加以下状态变量到 setup 顶部
const isEditMode = ref(false);
const editingId = ref<number | null>(null);

// 修改加载分类数据函数
async function loadCategoryData() {
  try {
    console.log('开始加载请求页面分类数据...');
    const categories = await getCategoriesApi();
    console.log('分类API返回结果:', categories);
    
    // 为下拉框使用的格式化数据
    const formattedCategories = categories
      .filter(category => category.is_deleted === 0)
      .map(category => ({
        label: category.name,
        value: category.id
      }));
    
    // 为表单使用的格式化数据
    const formCategories = categories
      .filter(category => category.is_deleted === 0)
      .map(category => ({
        id: category.id,
        name: category.name
      }));
    
    categoryData.value = formattedCategories;
    console.log('请求页面分类数据已加载:', categoryData.value);
    return formCategories;
  } catch (error) {
    console.error('加载分类数据失败:', error);
    ElMessage.error('加载分类数据失败');
    return [];
  }
}

// 修改加载学院数据函数
async function loadCollegeData() {
  try {
    console.log('开始加载请求页面学院数据...');
    const colleges = await getCollegesApi();
    console.log('学院API返回结果:', colleges);
    
    // 为下拉框使用的格式化数据
    const formattedColleges = colleges
      .filter(college => college.is_deleted === 0)
      .map(college => ({
        label: college.name,
        value: college.id
      }));
    
    // 为表单使用的格式化数据
    const formColleges = colleges
      .filter(college => college.is_deleted === 0)
      .map(college => ({
        id: college.id,
        name: college.name
      }));
    
    collegeData.value = formattedColleges;
    console.log('请求页面学院数据已加载:', collegeData.value);
    return formColleges;
  } catch (error) {
    console.error('加载学院数据失败:', error);
    ElMessage.error('加载学院数据失败');
    return [];
  }
}

// 搜索表单配置
const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入求助标题",
      },
      fieldName: "title",
      label: "求助标题",
    },
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入求助内容",
        type: "textarea",
        rows: 2,
      },
      fieldName: "content",
      label: "求助内容",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: collegeData,
        placeholder: "请选择学院",
      },
      fieldName: "college_id",
      label: "所属学院",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: categoryData,
        placeholder: "请选择分类",
        filterable: true,
      },
      fieldName: "category_id",
      label: "资源分类",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: STATUS_OPTIONS,
        placeholder: "请选择状态",
      },
      fieldName: "status",
      label: "解决状态",
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

async function getRequestApi(params: {
  page: number;
  pageSize: number;
  title?: string;
  content?: string;
  college_id?: number;
  category_id?: number;
  status?: number;
  is_deleted?: number;
}) {
  try {
    const { 
      page, 
      pageSize, 
      title, 
      content, 
      college_id, 
      category_id, 
      status, 
      is_deleted 
    } = params;
    
    // 构建搜索请求参数
    const searchParams = {
      page,
      size: pageSize,
      title,
      content,
      collegeId: college_id,
      categoryId: category_id,
      status,
      isDeleted: is_deleted
    };
    
    // 调用后端API
    const result = await searchRequestsApi(searchParams);
    
    // 转换数据格式以匹配前端期望的格式
    const items = result.list.map(item => ({
      id: item.id,
      title: item.title,
      content: item.content,
      user_id: item.userId,
      user_name: item.userName,
      college_id: item.collegeId,
      college_name: item.collegeName,
      major_id: item.majorId || 0,
      major_name: item.majorName || '',
      category_id: item.categoryId,
      category_name: item.categoryName,
      reply_count: item.replyCount,
      view_count: item.viewCount,
      status: item.status,
      create_time: formatDate(item.createTime),
      update_time: formatDate(item.updateTime),
      is_deleted: item.isDeleted
    }));
    
    return {
      total: result.total,
      items
    };
  } catch (error) {
    console.error('获取资源请求列表失败:', error);
    ElMessage.error('获取资源请求列表失败');
    
    // 查询失败时返回空数据
    return {
      total: 0,
      items: []
    };
  }
}

// 资源求助数据
const requestData = ref<ResourceRequest[]>([...MOCK_RESOURCE_REQUEST_DATA]);

// 获取状态名称
const getStatusName = (status: number): string => {
  const option = STATUS_OPTIONS.find(item => item.value === status);
  return option ? option.label : "未知";
};

// 表格配置
const gridOptions = {
  border: true,
  showOverflow: true,
  autoResize: true,
  showHeaderOverflow: true,
  scrollX: true,
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
    { field: "title", title: "求助标题", minWidth: 200 },
    { 
      field: "content", 
      title: "求助内容",
      minWidth: 200,
      showOverflow: true,
      formatter: ({ cellValue }) => {
        if (cellValue && cellValue.length > 50) {
          return cellValue.substring(0, 50) + "...";
        }
        return cellValue;
      },
    },
    { 
      field: "college_name", 
      title: "所属学院",
      width: 180,
    },
    { 
      field: "major_name", 
      title: "所属专业",
      width: 180,
    },
    { 
      field: "category_name", 
      title: "资源分类",
      width: 120,
    },
    { 
      field: "reply_count", 
      title: "回复数量",
      width: 100,
    },
    { 
      field: "view_count", 
      title: "浏览量",
      width: 100,
    },
    { 
      field: "status", 
      title: "状态",
      width: 100,
      formatter: ({ row }: { row: ResourceRequest }) => getStatusName(row.status),
    },
    { field: "create_time", title: "创建时间", width: 160 },
    {
      field: "is_deleted",
      title: "是否删除",
      width: 100,
      formatter: ({ row }: { row: ResourceRequest }) => (row.is_deleted === 0 ? "正常" : "已删除"),
    },
    {
      title: "操作",
      width: 280,
      slots: {
        default: ({ row }: { row: ResourceRequest }) => [
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
              type: "warning",
              onClick: () => handleEdit(row),
              disabled: row.is_deleted === 1,
            },
            { default: () => "编辑" }
          ),
          row.is_deleted === 0 ?
          h(
            ElButton,
            {
              size: "small",
              type: "danger",
              onClick: () => handleDelete(row),
            },
            { default: () => "删除" }
          ) :
          h(
            ElButton,
            {
              size: "small",
              type: "success",
              onClick: () => handleRestore(row),
            },
            { default: () => "恢复" }
          ),
        ],
      },
    },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page }, formValues) => {
        return await getRequestApi({
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
const [Grid, gridApi] = useVbenVxeGrid({ 
  gridOptions: {
    ...gridOptions,
  } as any, 
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
const handleView = (row: ResourceRequest) => {
  // 增加浏览计数（真实环境中应当调用API）
  requestData.value = requestData.value.map((item) =>
    item.id === row.id
      ? {
          ...item,
          view_count: item.view_count + 1,
        }
      : item
  );
  
  // 模拟跳转到详情页面
  ElMessage.info(`暂时展示求助ID:${row.id} - ${row.title} 的详情页面`);
  
  // 在真实环境应该使用路由导航
  // router.push({ name: 'request-detail', params: { id: row.id } });
  
  // 刷新表格数据
  gridApi.query();
};

// 删除资源求助
const handleDelete = (row: ResourceRequest) => {
  ElMessageBox.confirm(`确认删除求助"${row.title}"吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        // 调用API进行逻辑删除
        const success = await updateRequestDeleteStatusApi(row.id, 1);

        if (success) {
          ElMessage.success("删除成功");
          
          // 刷新表格数据
          gridApi.query();
        } else {
          ElMessage.error("删除失败");
        }
      } catch (error) {
        console.error("删除求助失败", error);
        ElMessage.error("删除求助失败");
      }
    })
    .catch(() => {});
};

// 表单数据
const formData = reactive({
  title: "",
  content: "",
  college_id: undefined as undefined | number,
  major_id: null as number | null,
  course_id: null as number | null,
  category_id: undefined as undefined | number,
});

// 弹窗控制
const showModal = ref(false);

// 修改 handleAdd 方法
const handleAdd = () => {
  resetForm();
  isEditMode.value = false;
  editingId.value = null;
  showModal.value = true;
};

// 重置表单
const resetForm = () => {
  formData.title = "";
  formData.content = "";
  formData.college_id = undefined;
  formData.major_id = null;
  formData.course_id = null;
  formData.category_id = undefined;
};

// 修改 handleSubmit 方法
const handleSubmit = async () => {
  if (!formData.title) {
    ElMessage.warning("请输入求助标题");
    return;
  }
  
  if (!formData.content) {
    ElMessage.warning("请输入求助内容");
    return;
  }
  
  try {
    if (isEditMode.value && editingId.value) {
      // 编辑现有请求
      const requestData = {
        id: editingId.value,
        title: formData.title,
        content: formData.content,
        collegeId: formData.college_id,
        collegeName: collegeOptions.value.find(c => c.id === formData.college_id)?.name || '',
        categoryId: formData.category_id,
        categoryName: categoryOptions.value.find(c => c.id === formData.category_id)?.name || '',
        // 保留其他字段未变
        userId: 0, // 这里应该从当前编辑的记录中获取，但简化处理
        status: 0
      };
      
      const result = await updateRequestApi(requestData as any);
      ElMessage.success("求助更新成功");
    } else {
      // 创建新请求
      const requestData = {
        title: formData.title,
        content: formData.content,
        collegeId: formData.college_id,
        collegeName: collegeOptions.value.find(c => c.id === formData.college_id)?.name || '',
        categoryId: formData.category_id,
        categoryName: categoryOptions.value.find(c => c.id === formData.category_id)?.name || '',
        userId: 1, // 这里应该获取当前登录用户ID
        userName: '管理员' // 这里应该获取当前登录用户名
      };
      
      const result = await createRequestApi(requestData as any);
      ElMessage.success("求助发布成功");
    }
    
    // 关闭弹窗并刷新数据
    showModal.value = false;
    gridApi.query();
  } catch (error) {
    console.error("提交求助失败:", error);
    ElMessage.error("提交失败，请稍后重试");
  }
};

// 编辑资源请求
const handleEdit = (row: ResourceRequest) => {
  // 复制资源请求数据到表单
  formData.title = row.title;
  formData.content = row.content;
  formData.college_id = row.college_id;
  formData.category_id = row.category_id;
  
  // 设置编辑模式
  isEditMode.value = true;
  editingId.value = row.id;
  
  // 显示弹窗
  showModal.value = true;
};

// 恢复已删除的资源请求
const handleRestore = (row: ResourceRequest) => {
  ElMessageBox.confirm(`确认恢复求助"${row.title}"吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "info",
  })
    .then(async () => {
      try {
        // 调用API恢复请求
        const success = await restoreRequestApi(row.id);

        if (success) {
          ElMessage.success("恢复成功");
          
          // 刷新表格数据
          gridApi.query();
        } else {
          ElMessage.error("恢复失败");
        }
      } catch (error) {
        console.error("恢复求助失败", error);
        ElMessage.error("恢复求助失败");
      }
    })
    .catch(() => {});
};

// 在组件挂载时加载数据
onMounted(async () => {
  console.log('开始加载请求页面分类和学院数据');
  try {
    const [categoryResult, collegeResult] = await Promise.all([
      loadCategoryData(),
      loadCollegeData()
    ]);
    
    categoryOptions.value = categoryResult;
    collegeOptions.value = collegeResult;
    console.log('请求页面分类和学院数据加载完成');
    console.log('分类选项:', categoryOptions.value);
    console.log('学院选项:', collegeOptions.value);
    
    // 刷新表格数据以反映新加载的选项
    gridApi.query();
  } catch (error) {
    console.error('加载数据失败:', error);
  }
});
</script>
