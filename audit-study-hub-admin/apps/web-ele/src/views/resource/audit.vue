<script lang="ts" setup>
import { h, reactive, ref, onMounted } from "vue";

import { Page } from "@vben/common-ui";

import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElSelect,
  ElOption,
  ElMessage,
  ElTag,
  ElImage,
  ElDescriptions,
  ElDescriptionsItem,
  ElLink,
  ElMessageBox,
} from "element-plus";

import { useVbenVxeGrid } from "../../adapter/vxe-table";
import type { VbenFormProps } from "../../adapter/form";

import { MOCK_RESOURCE_DATA, STATUS_OPTIONS, FILE_TYPE_OPTIONS } from "./list-data";
import type { Resource } from "./list-data";
import { getCategoriesApi } from "../../api/core/category";
import { getCollegesApi } from "../../api/core/college";
import { 
  getPendingAuditResources, 
  AUDIT_RESULT_OPTIONS, 
  AUDIT_OPINION_TEMPLATES,
  MOCK_AUDIT_RECORDS
} from "./audit-data";
import type { AuditRecord } from "./audit-data";
import { processCategoryData, processCollegeData } from "../../utils/api-helpers";
import { requestClient } from '#/api/request';
import { 
  searchAuditResourcesApi, 
  batchAuditResourcesApi, 
  formatDate,
  type AuditResourceSearchRequest
} from '../../api/core/audit';

const sleep = (time = 500) => new Promise((resolve) => setTimeout(resolve, time));

// 资源数据
const resourceData = ref<Resource[]>([...MOCK_RESOURCE_DATA]);
const auditRecords = ref<AuditRecord[]>([...MOCK_AUDIT_RECORDS]);

// 当前选中的资源
const currentResource = ref<Resource | null>(null);

// 弹窗控制
const showDetailModal = ref(false);
const showAuditModal = ref(false);

// 审核操作：1-通过，2-拒绝
const auditAction = ref(0);

// 审核表单
const auditForm = reactive({
  // 已移除opinion字段
});

// 分类数据
const categoryData = ref<{label: string, value: number}[]>([]);
// 学院数据
const collegeData = ref<{label: string, value: number}[]>([]);

// 加载分类数据
async function loadCategoryData() {
  try {
    console.log('开始加载审核页面分类数据...');
    const categories = await getCategoriesApi();
    console.log('分类API返回结果:', categories);
    
    // 正确格式化分类数据为下拉框所需格式
    categoryData.value = categories
      .filter(category => category.is_deleted === 0)
      .map(category => ({
        label: category.name,
        value: category.id
      }));
    
    console.log('审核页面分类数据已加载:', categoryData.value);
  } catch (error) {
    console.error('加载分类数据失败:', error);
    ElMessage.error('加载分类数据失败');
  }
}

// 加载学院数据
async function loadCollegeData() {
  try {
    console.log('开始加载审核页面学院数据...');
    const colleges = await getCollegesApi();
    console.log('学院API返回结果:', colleges);
    
    // 正确格式化学院数据为下拉框所需格式
    collegeData.value = colleges
      .filter(college => college.is_deleted === 0)
      .map(college => ({
        label: college.name,
        value: college.id
      }));
    
    console.log('审核页面学院数据已加载:', collegeData.value);
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
        placeholder: "请输入资源标题",
      },
      fieldName: "title",
      label: "资源标题",
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
        options: FILE_TYPE_OPTIONS,
        placeholder: "请选择文件类型",
      },
      fieldName: "file_type",
      label: "文件类型",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: STATUS_OPTIONS,
        placeholder: "请选择状态",
      },
      fieldName: "status",
      label: "审核状态",
    },
  ],
  showCollapseButton: true,
  submitButtonOptions: {
    content: "查询",
  },
  submitOnChange: false,
  submitOnEnter: true,
};

// 获取格式化后的文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
};

// 获取格式化后的标签
const formatTags = (tags: string | null): string[] => {
  if (!tags) return [];
  // 处理JSON格式标签
  if (tags.startsWith("[")) {
    try {
      return JSON.parse(tags);
    } catch (e) {
      return [];
    }
  }
  // 处理逗号分隔的标签
  return tags.split(",");
};

// 获取状态名称
const getStatusName = (status: number): string => {
  const option = STATUS_OPTIONS.find(item => item.value === status);
  return option ? option.label : "未知";
};

// 获取文件类型名称
const getFileTypeName = (fileType: string): string => {
  const option = FILE_TYPE_OPTIONS.find(item => item.value === fileType);
  return option ? option.label : fileType;
};

// 获取审核状态类型
const getStatusType = (status: number): string => {
  switch (status) {
    case 0: return "warning"; // 待审核
    case 1: return "success"; // 已通过
    case 2: return "danger";  // 已拒绝
    default: return "info";
  }
};

// 获取资源API，用于表格数据加载
async function getResourceApi(params: {
  page: number;
  pageSize: number;
  title?: string;
  college_id?: number;
  category_id?: number;
  file_type?: string;
  status?: number;
}) {
  try {
    // 使用后端API获取数据
    const searchParams: AuditResourceSearchRequest = {
      page: params.page,
      size: params.pageSize,
      title: params.title,
      collegeId: params.college_id,
      categoryId: params.category_id,
      fileType: params.file_type,
      status: params.status,
    };
    
    const result = await searchAuditResourcesApi(searchParams);
    
    // 转换字段格式
    const items = result.list.map(item => ({
      id: item.id,
      title: item.title,
      category_name: item.categoryName,
      file_type: item.fileType,
      college_name: item.collegeName,
      status: item.status,
      create_time: formatDate(item.createTime)
    }));
    
    return {
      total: result.total,
      items
    };
  } catch (error) {
    console.error('获取审核资源失败:', error);
    ElMessage.error('获取审核资源失败');
    
    // 如果API调用失败，使用本地数据作为后备方案
    // 筛选未删除的资源
    const all = MOCK_RESOURCE_DATA.filter((item) => {
      if (item.is_deleted === 1) return false;
      if (params.title && !item.title.includes(params.title)) return false;
      if (params.college_id !== undefined && item.college_id !== params.college_id) return false;
      if (params.category_id !== undefined && item.category_id !== params.category_id) return false;
      if (params.file_type && item.file_type !== params.file_type) return false;
      if (params.status !== undefined && item.status !== params.status) return false;
      return true;
    });
    
    const items = all.slice((params.page - 1) * params.pageSize, params.page * params.pageSize);
    return {
      total: all.length,
      items,
    };
  }
}

// 表格配置
const gridOptions = {
  border: true,
  showOverflow: true,
  rowConfig: {
    keyField: "id",
    isHover: true,
    isCurrent: true,
    isSelected: true
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
    { type: 'checkbox', width: 50 },
    { field: "id", title: "ID", width: 80, fixed: "left" },
    { field: "title", title: "资源标题", minWidth: 200 },
    { 
      field: "category_name", 
      title: "资源分类",
      width: 120,
    },
    { 
      field: "file_type", 
      title: "文件类型",
      width: 100,
      formatter: ({ row }: { row: Resource }) => getFileTypeName(row.file_type),
    },
    { 
      field: "college_name", 
      title: "所属学院",
      width: 180,
    },
    { 
      field: "status", 
      title: "状态",
      width: 100,
      slots: {
        default: ({ row }: { row: Resource }) => [
          h(
            ElTag,
            {
              type: getStatusType(row.status),
              effect: "light",
            },
            { default: () => getStatusName(row.status) }
          ),
        ],
      },
    },
    { field: "create_time", title: "上传时间", width: 160 },
    {
      title: "操作",
      width: 280,
      slots: {
        default: ({ row }: { row: Resource }) => [
          h(
            ElButton,
            {
              size: "small",
              type: "primary",
              onClick: () => handleView(row),
            },
            { default: () => "查看详情" }
          ),
          row.status === 0 ? h(
            ElButton,
            {
              size: "small",
              type: "success",
              onClick: () => handleAudit(row, 1),
            },
            { default: () => "通过审核" }
          ) : null,
          row.status === 0 ? h(
            ElButton,
            {
              size: "small",
              type: "danger",
              onClick: () => handleAudit(row, 2),
            },
            { default: () => "拒绝审核" }
          ) : null,
        ],
      },
    },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page }, formValues) => {
        return await getResourceApi({
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

// 查看资源详情
const handleView = (row: Resource) => {
  currentResource.value = row;
  showDetailModal.value = true;
};

// 审核资源
const handleAudit = (row: Resource, action: number) => {
  currentResource.value = row;
  auditAction.value = action;
  
  showDetailModal.value = false;
  showAuditModal.value = true;
};

// 提交审核结果
const submitAudit = async () => {
  if (!currentResource.value) return;
  
  try {
    // 调用批量审核API
    const request = {
      resourceIds: [currentResource.value.id],
      auditResult: auditAction.value
    };
    
    const success = await batchAuditResourcesApi(request);
    
    if (success) {
      ElMessage.success(auditAction.value === 1 ? "审核通过成功" : "审核拒绝成功");
      showAuditModal.value = false;
      
      // 刷新表格数据
      gridApi.query();
    } else {
      ElMessage.error("审核操作失败");
    }
  } catch (error) {
    console.error("审核失败", error);
    ElMessage.error("审核操作失败");
  }
};

// 批量审核
const batchAudit = async (auditResult: number) => {
  // 获取选中行
  const $grid = gridApi.getVmGrid?.() || gridApi.$grid;
  const selectedRows = $grid?.getCheckboxRecords?.() || [];
  
  if (!selectedRows || selectedRows.length === 0) {
    ElMessage.warning("请先选择资源");
    return;
  }
  
  try {
    // 批量操作确认
    await ElMessageBox.confirm(
      `确定${auditResult === 1 ? '通过' : '拒绝'}选中的${selectedRows.length}个资源吗？`, 
      '批量审核', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    // 收集所有选中资源的ID
    const resourceIds = selectedRows.map((row: any) => row.id);
    
    // 调用批量审核API
    const request = {
      resourceIds,
      auditResult
    };
    
    const success = await batchAuditResourcesApi(request);
    
    if (success) {
      ElMessage.success(`批量${auditResult === 1 ? '通过' : '拒绝'}成功`);
      // 刷新表格数据
      gridApi.query();
    } else {
      ElMessage.error("批量审核操作失败");
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error("批量审核失败", error);
      ElMessage.error("批量审核操作失败");
    }
  }
};

// 在组件挂载时加载数据
onMounted(async () => {
  console.log('开始加载审核页面分类和学院数据');
  try {
    await Promise.all([
      loadCategoryData(),
      loadCollegeData()
    ]);
    console.log('审核页面分类和学院数据加载完成');
    
    // 刷新表格数据以反映新加载的选项
    gridApi.query();
  } catch (error) {
    console.error('加载数据失败:', error);
  }
});

// 确保这两个函数可用
const getVmGrid = () => {
  return gridApi.getVmGrid();
};
</script>


<template>
  <Page title="资源审核" description="审核用户上传的资源内容">
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
        <ElButton class="mr-2" type="success" @click="batchAudit(1)">
          批量通过
        </ElButton>
        <ElButton class="mr-2" type="danger" @click="batchAudit(2)">
          批量拒绝
        </ElButton>
      </template>
    </Grid>
    
    <!-- 审核详情弹窗 -->
    <ElDialog
      v-model="showDetailModal"
      title="资源详情"
      width="800px"
      append-to-body
    >
      <div v-if="currentResource">
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="资源标题">
            {{ currentResource.title }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="资源ID">
            {{ currentResource.id }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="所属分类">
            {{ currentResource.category_name }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="文件类型">
            {{ getFileTypeName(currentResource.file_type) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="文件大小">
            {{ formatFileSize(currentResource.file_size) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="上传时间">
            {{ currentResource.create_time }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="所属学院">
            {{ currentResource.college_name }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="所属专业">
            {{ currentResource.major_name }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="标签" :span="2">
            <ElTag 
              v-for="tag in formatTags(currentResource.tags)" 
              :key="tag" 
              class="mr-1" 
              size="small"
            >
              {{ tag }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="资源描述" :span="2">
            {{ currentResource.description || '无描述' }}
          </ElDescriptionsItem>
        </ElDescriptions>
        
        <div class="mt-4">
          <h4 class="mb-2">资源预览</h4>
          <div class="resource-preview">
            <template v-if="currentResource.file_type === 'image'">
              <ElImage
                :src="currentResource.file_url"
                fit="contain"
                style="max-width: 100%; max-height: 300px;"
              />
            </template>
            <template v-else>
              <ElLink :href="currentResource.file_url" target="_blank" type="primary">
                点击查看文件
              </ElLink>
            </template>
          </div>
        </div>
      </div>
      <template #footer>
        <ElButton @click="showDetailModal = false">关闭</ElButton>
        <ElButton 
          v-if="currentResource && currentResource.status === 0"
          type="success" 
          @click="handleAudit(currentResource, 1)"
        >
          通过审核
        </ElButton>
        <ElButton 
          v-if="currentResource && currentResource.status === 0"
          type="danger" 
          @click="handleAudit(currentResource, 2)"
        >
          拒绝通过
        </ElButton>
      </template>
    </ElDialog>
    
    <!-- 审核确认弹窗 -->
    <ElDialog
      v-model="showAuditModal"
      :title="auditAction === 1 ? '审核通过' : '审核拒绝'"
      width="500px"
      append-to-body
    >
      <p>确定要{{ auditAction === 1 ? '通过' : '拒绝' }}该资源吗？</p>
      <template #footer>
        <ElButton @click="showAuditModal = false">取消</ElButton>
        <ElButton type="primary" @click="submitAudit">确认</ElButton>
      </template>
    </ElDialog>
  </Page>
</template>

<style scoped>
.resource-preview {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
  min-height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>