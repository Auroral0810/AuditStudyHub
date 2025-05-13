<template>
  <Page title="资源管理" description="管理所有上传的学习资源">
    <!-- 表格 -->
    <Grid>
      <template #toolbar-tools>
        <!-- 添加新增按钮 -->
        <ElButton class="mr-2" type="primary" @click="showAddResourceDialog">
          <i class="el-icon-plus mr-1"></i> 新增资源
        </ElButton>
        <!-- 添加ES同步按钮 -->
        <ElButton class="mr-2" type="warning" @click="triggerEsSync">
          <i class="el-icon-refresh mr-1"></i> 同步ES
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

    <!-- 资源表单对话框 -->
    <ElDialog
      v-model="showResourceFormModal"
      :title="isEdit ? '编辑资源' : '新增资源'"
      width="700px"
      destroy-on-close
    >
      <ElForm
        ref="resourceFormRef"
        :model="resourceForm"
        :rules="resourceFormRules"
        label-width="100px"
      >
        <ElFormItem label="资源标题" prop="title">
          <ElInput v-model="resourceForm.title" placeholder="请输入资源标题" />
        </ElFormItem>
        <ElFormItem label="资源描述" prop="description">
          <ElInput
            v-model="resourceForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入资源描述"
          />
        </ElFormItem>
        <ElFormItem label="文件URL" prop="file_url">
          <ElInput v-model="resourceForm.file_url" placeholder="请输入文件URL" />
        </ElFormItem>
        <ElFormItem label="文件类型" prop="file_type">
          <ElSelect v-model="resourceForm.file_type" placeholder="请选择文件类型" style="width: 100%">
            <ElOption
              v-for="item in FILE_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="文件大小" prop="fileSize">
          <ElInputNumber v-model="resourceForm.fileSize" :min="0" placeholder="请输入文件大小(MB)" style="width: 100%" />
        </ElFormItem>
        <ElFormItem label="上传者ID" prop="uploader_id">
          <ElInputNumber v-model="resourceForm.uploader_id" :min="1" placeholder="请输入上传者ID" style="width: 100%" />
        </ElFormItem>
        <ElFormItem label="所属学院" prop="college_id">
          <ElSelect v-model="resourceForm.college_id" placeholder="请选择所属学院" style="width: 100%">
            <ElOption
              v-for="item in collegeData"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="所属分类" prop="category_id">
          <ElSelect v-model="resourceForm.category_id" placeholder="请选择所属分类" style="width: 100%">
            <ElOption
              v-for="item in categoryData"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="资源标签" prop="tags">
          <ElInput v-model="resourceForm.tags" placeholder="多个标签用逗号分隔" />
        </ElFormItem>
        <ElFormItem label="审核状态" prop="status">
          <ElSelect v-model="resourceForm.status" placeholder="请选择审核状态" style="width: 100%">
            <ElOption
              v-for="item in STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="showResourceFormModal = false">取 消</ElButton>
        <ElButton type="primary" @click="submitResourceForm">确 定</ElButton>
      </template>
    </ElDialog>
  </Page>
</template>

<script lang="ts" setup>
import { h, ref, reactive, onMounted } from "vue";

import { Page } from "@vben/common-ui";
import { ElButton, ElMessage, ElMessageBox, ElTag, ElDialog, ElForm, ElFormItem, ElInput, ElInputNumber, ElSelect, ElOption } from "element-plus";

import { useVbenVxeGrid } from "../../adapter/vxe-table";
import type { VbenFormProps } from "../../adapter/form";

import { MOCK_RESOURCE_DATA, STATUS_OPTIONS, FILE_TYPE_OPTIONS } from "./list-data";
import type { Resource } from "./list-data";
import { getCategoriesApi } from "../../api/core/category";
import { getCollegesApi } from "../../api/core/college";
import { MOCK_RESOURCE_REQUEST_DATA } from "./request-data";
import { getResourcesApi, searchResourcesApi, updateResourceStatusApi, addResourceApi, updateResourceApi, triggerSyncApi, type AdminResourceSearchRequest } from "../../api/core/resource";

const sleep = (time = 500) => new Promise((resolve) => setTimeout(resolve, time));

// 添加分类数据状态
const categoryData = ref<{ label: string, value: number }[]>([]);
// 添加学院数据状态
const collegeData = ref<{ label: string, value: number }[]>([]);

// 添加加载分类数据的函数
async function loadCategoryData() {
  try {
    console.log('开始加载分类数据...');
    const categories = await getCategoriesApi();
    console.log('分类API返回结果:', categories);
    
    // 正确格式化分类数据为下拉框所需格式
    categoryData.value = categories
      .filter(category => category.is_deleted === 0)
      .map(category => ({
        label: category.name,
        value: category.id
      }));
    
    console.log('分类数据已加载:', categoryData.value);
    return categories;
  } catch (error) {
    console.error('加载分类数据失败:', error);
    ElMessage.error('加载分类数据失败');
    return [];
  }
}

// 加载学院数据
async function loadCollegeData() {
  try {
    console.log('开始加载学院数据...');
    const colleges = await getCollegesApi();
    console.log('学院API返回结果:', colleges);
    
    // 正确格式化学院数据为下拉框所需格式
    collegeData.value = colleges
      .filter(college => college.is_deleted === 0)
      .map(college => ({
        label: college.name,
        value: college.id
      }));
    
    console.log('学院数据已加载:', collegeData.value);
    return colleges;
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
        placeholder: "请输入资源标题",
      },
      fieldName: "title",
      label: "资源标题",
    },
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入上传者姓名",
      },
      fieldName: "uploaderName",
      label: "上传者",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: collegeData,
        placeholder: "请选择学院",
      },
      fieldName: "collegeId",
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
      fieldName: "categoryId",
      label: "资源分类",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: FILE_TYPE_OPTIONS,
        placeholder: "请选择文件类型",
      },
      fieldName: "fileType",
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
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: [
          { label: "全部", value: null },
          { label: "正常", value: 0 },
          { label: "已删除", value: 1 },
        ],
        placeholder: "请选择是否删除",
      },
      fieldName: "isDeleted",
      label: "是否删除",
    },
    {
      component: "DatePicker",
      componentProps: {
        type: "date",
        placeholder: "开始日期",
        format: "YYYY-MM-DD",
      },
      fieldName: "startDate",
      label: "开始日期",
    },
    {
      component: "DatePicker",
      componentProps: {
        type: "date",
        placeholder: "结束日期",
        format: "YYYY-MM-DD",
      },
      fieldName: "endDate",
      label: "结束日期",
    },
    {
      component: "InputNumber",
      componentProps: {
        placeholder: "最小文件大小(MB)",
        min: 0,
      },
      fieldName: "minSize",
      label: "最小大小(MB)",
    },
    {
      component: "InputNumber",
      componentProps: {
        placeholder: "最大文件大小(MB)",
        min: 0,
      },
      fieldName: "maxSize",
      label: "最大大小(MB)",
    },
    {
      component: "Select",
      componentProps: {
        options: [
          { label: "最新上传", value: "newest" },
          { label: "下载量", value: "downloads" },
          { label: "浏览量", value: "views" },
          { label: "点赞量", value: "likes" },
        ],
        placeholder: "排序方式",
      },
      fieldName: "sort",
      label: "排序方式",
      defaultValue: "newest",
    },
  ],
  showCollapseButton: true,
  submitButtonOptions: {
    content: "查询",
  },
  submitOnChange: false,
  submitOnEnter: true,
};

async function getResourceApi(params: {
  page: number;
  pageSize: number;
  title?: string;
  collegeId?: number;
  categoryId?: number;
  fileType?: string;
  status?: number;
  isDeleted?: number;
  uploaderName?: string;
  startDate?: string;
  endDate?: string;
  minSize?: number;
  maxSize?: number;
  sort?: string;
}) {
  try {
    const searchParams: AdminResourceSearchRequest = {
      page: params.page,
      size: params.pageSize,
      title: params.title,
      collegeId: params.collegeId,
      categoryId: params.categoryId,
      fileType: params.fileType,
      status: params.status,
      isDeleted: params.isDeleted,
      uploaderName: params.uploaderName,
      startDate: params.startDate,
      endDate: params.endDate,
      minSize: params.minSize,
      maxSize: params.maxSize,
      sort: params.sort || "newest",
    };

    const result = await searchResourcesApi(searchParams);
    console.log('API返回结果格式:', result);
    
    // 将后端返回的驼峰命名转换为前端的下划线命名的辅助函数
    const convertItem = (item: any) => {
      return {
        id: item.id,
        title: item.title,
        description: item.description,
        file_url: item.fileUrl,
        file_type: item.fileType,
        file_size: item.fileSize,
        tags: item.tags || "",
        download_count: item.downloadCount,
        view_count: item.viewCount,
        like_count: item.likeCount,
        uploader_id: item.uploaderId,
        uploaderName: item.uploaderName || "",
        college_id: item.collegeId,
        college_name: item.collegeName,
        major_id: item.majorId,
        major_name: item.majorName,
        course_id: item.courseId,
        course_name: item.courseName,
        category_id: item.categoryId,
        category_name: item.categoryName,
        status: item.status,
        create_time: item.createTime,
        update_time: item.updateTime,
        is_deleted: item.isDeleted === null ? 0 : item.isDeleted
      };
    };
    
    // 处理不同的返回格式
    if (result && result.code && result.data && result.data.list && Array.isArray(result.data.list)) {
      // 标准API响应格式：{code, message, data: {list, total}}
      return {
        total: result.data.total,
        items: result.data.list.map(convertItem)
      };
    } else if (result && result.list && Array.isArray(result.list)) {
      // 处理 {list: [...], total: ...} 格式
      return {
        total: result.total || result.list.length,
        items: result.list.map(convertItem)
      };
    } else if (result && result.data && Array.isArray(result.data)) {
      // 处理 {data: [...]} 格式
      return {
        total: result.data.length,
        items: result.data.map(convertItem)
      };
    } else if (result && Array.isArray(result)) {
      // 直接返回数组的格式
      return {
        total: result.length,
        items: result.map(convertItem)
      };
    } else {
      console.error('API返回格式不正确:', result);
      throw new Error('API返回格式不正确');
    }
  } catch (error) {
    console.error('获取资源数据失败:', error);
    ElMessage.error('获取资源数据失败');
    
    // 如果API调用失败，使用本地数据作为后备方案
    const { 
      page, 
      pageSize, 
      title, 
      collegeId, 
      categoryId, 
      fileType, 
      status, 
      isDeleted, 
      uploaderName, 
      startDate, 
      endDate, 
      minSize, 
      maxSize, 
      sort 
    } = params;
    
    const all = MOCK_RESOURCE_DATA.filter((item) => {
      if (title && !item.title.includes(title)) return false;
      if (collegeId !== undefined && item.college_id !== collegeId) return false;
      if (categoryId !== undefined && item.category_id !== categoryId) return false;
      if (fileType && item.file_type !== fileType) return false;
      if (status !== undefined && item.status !== status) return false;
      if (isDeleted !== undefined && item.is_deleted !== isDeleted) return false;
      if (uploaderName && !item.uploader_name.includes(uploaderName)) return false;
      if (startDate && new Date(item.create_time) < new Date(startDate)) return false;
      if (endDate && new Date(item.create_time) > new Date(endDate)) return false;
      if (minSize && item.file_size < minSize * 1024 * 1024) return false;
      if (maxSize && item.file_size > maxSize * 1024 * 1024) return false;
      return true;
    });
    
    const items = all.slice((page - 1) * pageSize, page * pageSize);
    await sleep(300);
    return {
      total: all.length,
      items,
    };
  }
}

// 资源数据
const resourceData = ref<Resource[]>([...MOCK_RESOURCE_DATA]);

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
};

// 格式化标签
const formatTags = (tags: string | null | undefined): string[] => {
  if (!tags) return [];
  
  try {
    // 处理JSON格式标签
    if (typeof tags === 'string' && tags.startsWith("[")) {
      const parsed = JSON.parse(tags);
      return Array.isArray(parsed) ? parsed : [];
    }
    // 处理逗号分隔的标签
    return typeof tags === 'string' ? tags.split(",") : [];
  } catch (e) {
    console.error('标签解析错误:', e, tags);
    return [];
  }
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

// 获取用户名称
const getUserName = (userId: number): string => {
  // 从请求数据中查找匹配用户ID的请求，返回用户名
  const request = MOCK_RESOURCE_REQUEST_DATA.find(item => item.user_id === userId);
  if (request) {
    return `${request.user_name} (ID: ${userId})`;
  }
  
  // 如果在请求数据中找不到，显示用户ID
  return `用户#${userId}`;
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
    { field: "title", title: "资源标题", minWidth: 200 },
    { 
      field: "description", 
      title: "资源描述", 
      minWidth: 250,
      showOverflow: true,
    },
    { 
      field: "uploader_id", 
      title: "上传用户",
      width: 150,
      formatter: ({ row }: { row: Resource }) => getUserName(row.uploader_id),
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
      field: "course_name", 
      title: "所属课程",
      width: 180,
      formatter: ({ row }: { row: Resource }) => row.course_name || '无',
    },
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
      field: "file_url", 
      title: "资源URL",
      minWidth: 250,
      showOverflow: true,
      formatter: ({ row }: { row: Resource }) => {
        // 确保 url 存在
        const url = row?.file_url || '';
        return url.length > 50 ? url.substring(0, 50) + '...' : url;
      },
    },
    { 
      field: "file_size", 
      title: "文件大小",
      width: 100,
      formatter: ({ row }: { row: Resource }) => formatFileSize(row.file_size),
    },
    { 
      field: "tags", 
      title: "标签",
      width: 200,
      slots: {
        default: ({ row }: { row: Resource }) => {
          // 确保 row 和 row.tags 存在
          if (!row || typeof row.tags === 'undefined') {
            return [];
          }
          // 安全地格式化标签
          try {
            const tags = formatTags(row.tags);
            return tags.map(tag => 
              h(ElTag, { size: "small", effect: "plain" }, { default: () => tag })
            );
          } catch (e) {
            console.error('标签格式化错误:', e);
            return [];
          }
        },
      },
    },
    { 
      field: "download_count", 
      title: "下载量",
      width: 80,
    },
    { 
      field: "view_count", 
      title: "浏览量",
      width: 80,
    },
    { 
      field: "like_count", 
      title: "点赞量",
      width: 80,
    },
    { 
      field: "status", 
      title: "状态",
      width: 100,
      formatter: ({ row }: { row: Resource }) => getStatusName(row.status),
    },
    { field: "create_time", title: "创建时间", width: 160 },
    { field: "update_time", title: "更新时间", width: 160 },
    {
      field: "is_deleted",
      title: "是否删除",
      width: 100,
      formatter: ({ row }: { row: Resource }) => (row.is_deleted === 0 ? "正常" : "已删除"),
    },
    {
      title: "操作",
      width: 320,
      fixed: "right",
      slots: {
        default: ({ row }: { row: Resource }) => [
          h(
            ElButton,
            {
              size: "small",
              type: "primary",
              onClick: () => handlePreview(row),
            },
            { default: () => "预览" }
          ),
          h(
            ElButton,
            {
              size: "small",
              type: "warning",
              onClick: () => handleEdit(row),
            },
            { default: () => "编辑" }
          ),
          h(
            ElButton,
            {
              size: "small",
              type: "success",
              onClick: () => handleDownload(row),
            },
            { default: () => "下载" }
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
          h(
            ElButton,
            {
              size: "small",
              type: "info",
              onClick: () => handleRestore(row),
              disabled: row.is_deleted === 0,
            },
            { default: () => "恢复" }
          ),
        ],
      },
    },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page }: { page: { currentPage: number; pageSize: number } }, formValues: Record<string, any>) => {
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

// 预览资源
const handlePreview = (row: Resource) => {
  // 增加浏览计数（真实环境中应当调用API）
  resourceData.value = resourceData.value.map((item) =>
    item.id === row.id
      ? {
          ...item,
          view_count: item.view_count + 1,
        }
      : item
  );
  
  // 打开新窗口预览文件
  window.open(row.file_url, "_blank");
  
  // 刷新表格数据
  gridApi.query();
};

// 下载资源
const handleDownload = (row: Resource) => {
  // 增加下载计数（真实环境中应当调用API）
  resourceData.value = resourceData.value.map((item) =>
    item.id === row.id
      ? {
          ...item,
          download_count: item.download_count + 1,
        }
      : item
  );
  
  // 创建下载链接并点击
  const link = document.createElement("a");
  link.href = row.file_url;
  link.download = row.title;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  
  ElMessage.success("下载成功");
  
  // 刷新表格数据
  gridApi.query();
};

// 删除资源
const handleDelete = (row: Resource) => {
  ElMessageBox.confirm(`确认删除资源"${row.title}"吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        gridApi.setLoading(true);
        
        // 调用API进行逻辑删除（设置is_deleted=1）
        await updateResourceStatusApi(row.id, 1);
        
        ElMessage.success("删除成功");
        
        // 刷新表格数据
        gridApi.query();
        
        // 如果当前正在查看该资源的详情，更新详情中的状态
        if (currentResource.value && currentResource.value.id === row.id) {
          currentResource.value.is_deleted = 1;
        }
      } catch (error) {
        console.error("删除资源失败", error);
        ElMessage.error("删除资源失败");
      } finally {
        gridApi.setLoading(false);
      }
    })
    .catch(() => {});
};

// 恢复资源
const handleRestore = (row: Resource) => {
  ElMessageBox.confirm(`确认恢复资源"${row.title}"吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "success",
  })
    .then(async () => {
      try {
        gridApi.setLoading(true);
        
        // 调用API进行状态恢复（设置is_deleted=0）
        await updateResourceStatusApi(row.id, 0);
        
        ElMessage.success("恢复成功");
        
        // 刷新表格数据
        gridApi.query();
        
        // 如果当前正在查看该资源的详情，更新详情中的状态
        if (currentResource.value && currentResource.value.id === row.id) {
          currentResource.value.is_deleted = 0;
        }
      } catch (error) {
        console.error("恢复资源失败", error);
        ElMessage.error("恢复资源失败");
      } finally {
        gridApi.setLoading(false);
      }
    })
    .catch(() => {});
};

// 资源表单相关
const showResourceFormModal = ref(false);
const isEdit = ref(false);
const resourceFormRef = ref<any>(null);
const currentResource = ref<any>(null); // 当前操作的资源

// 资源表单数据
const resourceForm = reactive({
  id: null as number | null,
  title: "",
  description: "",
  file_url: "",
  file_type: "",
  fileSize: 0,
  tags: "",
  uploader_id: null as number | null,
  college_id: null as number | null,
  college_name: "",
  category_id: null as number | null,
  category_name: "",
  status: 0,
  is_deleted: 0
});

// 资源表单验证规则
const resourceFormRules = {
  title: [{ required: true, message: "请输入资源标题", trigger: "blur" }],
  description: [{ required: true, message: "请输入资源描述", trigger: "blur" }],
  file_url: [{ required: true, message: "请输入文件URL", trigger: "blur" }],
  file_type: [{ required: true, message: "请选择文件类型", trigger: "change" }],
  fileSize: [{ required: true, message: "请输入文件大小", trigger: "blur" }],
  uploader_id: [{ required: true, message: "请输入上传者ID", trigger: "blur" }],
  college_id: [{ required: true, message: "请选择所属学院", trigger: "change" }],
  category_id: [{ required: true, message: "请选择所属分类", trigger: "change" }]
};

// 显示添加资源对话框
const showAddResourceDialog = () => {
  // 重置表单
  Object.keys(resourceForm).forEach(key => {
    if (key !== "status" && key !== "is_deleted") {
      // @ts-ignore
      resourceForm[key] = key === "fileSize" ? 0 : null;
    }
  });
  resourceForm.title = "";
  resourceForm.description = "";
  resourceForm.file_url = "";
  resourceForm.file_type = "";
  resourceForm.tags = "";
  resourceForm.status = 0;
  resourceForm.is_deleted = 0;
  
  isEdit.value = false;
  showResourceFormModal.value = true;
};

// 显示编辑资源对话框
const handleEdit = (row: Resource) => {
  currentResource.value = row;
  
  // 填充表单数据
  resourceForm.id = row.id;
  resourceForm.title = row.title;
  resourceForm.description = row.description || "";
  resourceForm.file_url = row.file_url;
  resourceForm.file_type = row.file_type;
  resourceForm.fileSize = row.file_size / (1024 * 1024); // 将字节转为MB
  resourceForm.tags = row.tags || "";
  resourceForm.uploader_id = row.uploader_id;
  resourceForm.college_id = row.college_id;
  resourceForm.college_name = row.college_name;
  resourceForm.category_id = row.category_id;
  resourceForm.category_name = row.category_name;
  resourceForm.status = row.status;
  resourceForm.is_deleted = row.is_deleted;
  
  isEdit.value = true;
  showResourceFormModal.value = true;
};

// 提交资源表单
const submitResourceForm = async () => {
  if (!resourceFormRef.value) return;
  
  await resourceFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        gridApi.setLoading(true);
        
        // 准备提交的数据
        const submitData = {
          id: resourceForm.id,
          title: resourceForm.title,
          description: resourceForm.description,
          fileUrl: resourceForm.file_url,
          fileType: resourceForm.file_type,
          // 将MB转回字节
          fileSize: (resourceForm.fileSize || 0) * 1024 * 1024,
          tags: resourceForm.tags,
          uploaderId: resourceForm.uploader_id,
          collegeId: resourceForm.college_id,
          collegeName: resourceForm.college_name,
          categoryId: resourceForm.category_id,
          categoryName: resourceForm.category_name,
          status: resourceForm.status,
          isDeleted: resourceForm.is_deleted
        };
        
        if (isEdit.value) {
          // 编辑现有资源
          await updateResourceApi(submitData);
          ElMessage.success('资源更新成功');
        } else {
          // 创建新资源
          await addResourceApi(submitData);
          ElMessage.success('资源创建成功');
        }
        
        // 关闭表单弹窗并刷新表格
        showResourceFormModal.value = false;
        gridApi.query();
        
      } catch (error) {
        console.error(isEdit.value ? '更新资源失败:' : '创建资源失败:', error);
        ElMessage.error(isEdit.value ? '更新资源失败' : '创建资源失败');
      } finally {
        gridApi.setLoading(false);
      }
    } else {
      console.log('表单验证失败:', fields);
      ElMessage.warning('请正确填写表单！');
    }
  });
};

// 触发ES同步
const triggerEsSync = async () => {
  try {
    gridApi.setLoading(true);
    
    // 直接处理成功情况
    await triggerSyncApi();
    
    // 如果没有抛出异常，就是成功了
    ElMessage.success('ES同步成功');
  } catch (error: any) {
    console.log('捕获到ES同步响应:', error);
    
    // 检查错误对象的各种可能属性，查找成功标志
    if (
      (error?.message && typeof error.message === 'string' && error.message.includes('成功')) || 
      (error?.response?.data && typeof error.response.data === 'string' && error.response.data.includes('成功')) ||
      (error?.data && typeof error.data === 'string' && error.data.includes('成功')) ||
      (error && typeof error === 'string' && error.includes('成功')) ||
      (error?.status === 200)
    ) {
      console.log('从错误对象中检测到成功标志');
      ElMessage.success('ES同步成功');
    } else {
      console.error('ES同步失败:', error);
      ElMessage.error('ES同步失败');
    }
  } finally {
    gridApi.setLoading(false);
  }
};

// 在组件挂载时加载数据
onMounted(async () => {
  console.log('开始加载分类和学院数据');
  try {
    await Promise.all([
      loadCategoryData(),
      loadCollegeData()
    ]);
    console.log('分类和学院数据加载完成');
    
    // 刷新表格数据以反映新加载的选项
    gridApi.query();
  } catch (error) {
    console.error('加载数据失败:', error);
  }
});
</script>
