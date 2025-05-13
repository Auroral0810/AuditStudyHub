<template>
  <Page title="资源请求回复" description="管理用户资源请求的回复内容">
    <!-- 表格 -->
    <Grid>
      <template #toolbar-tools>
        <ElButton class="mr-2" type="primary" @click="handleAdd">
          新增回复
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
    
    <!-- 回复详情弹窗 -->
    <ElDialog
      v-model="showDetailModal"
      title="回复详情"
      width="700px"
      append-to-body
    >
      <div v-if="currentReply">
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="回复ID">
            {{ currentReply.id }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="请求内容">
            {{ currentReply.content }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="用户昵称">
            {{ currentReply.userName }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="回复时间">
            {{ formatDate(currentReply.replyTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="状态">
            <ElTag :type="currentReply.isDeleted === 0 ? 'success' : 'danger'">
              {{ currentReply.isDeleted === 0 ? "正常" : "已删除" }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="关联资源" v-if="currentReply.resourceId">
            {{ currentReply.resourceName || '未知资源' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="父回复" v-if="currentReply.parentId">
            <ElTooltip
              placement="top"
              :content="currentReply.parentContent || '无内容'"
              :effect="'light'"
            >
              <span class="parent-link">回复ID: {{ currentReply.parentId }}</span>
            </ElTooltip>
          </ElDescriptionsItem>
        </ElDescriptions>
        
        <div class="mt-4">
          <h4 class="mb-2">回复内容</h4>
          <div class="reply-content">
            {{ currentReply.replyContent }}
          </div>
        </div>
        
        <div class="mt-4" v-if="currentReply.parentId && currentReply.parentContent">
          <h4 class="mb-2">父回复内容</h4>
          <div class="reply-content parent-reply">
            {{ currentReply.parentContent }}
          </div>
        </div>
      </div>
      <template #footer>
        <ElButton @click="showDetailModal = false">关闭</ElButton>
        <ElButton
          v-if="currentReply && currentReply.isDeleted === 0"
          type="primary" 
          @click="handleEdit(currentReply)"
        >
          编辑回复
        </ElButton>
        <ElButton
          v-if="currentReply && currentReply.isDeleted === 0"
          type="danger"
          @click="handleDelete(currentReply)"
        >
          删除回复
        </ElButton>
        <ElButton
          v-if="currentReply && currentReply.isDeleted === 1"
          type="success"
          @click="handleRestore(currentReply)"
        >
          恢复回复
        </ElButton>
      </template>
    </ElDialog>
    
    <!-- 新增/编辑回复表单弹窗 -->
    <ElDialog
      v-model="showFormModal"
      :title="isEdit ? '编辑回复' : '新增回复'"
      width="600px"
      append-to-body
    >
      <ElForm
        :model="replyForm"
        ref="replyFormRef"
        :rules="replyFormRules"
        label-width="100px"
      >
        <ElFormItem label="请求ID" prop="requestId" v-if="!isEdit">
          <ElInput 
            v-model.number="replyForm.requestId"
            placeholder="请输入请求ID"
          />
        </ElFormItem>
        <ElFormItem label="请求内容" prop="content">
          <ElInput 
            v-model="replyForm.content"
            type="textarea"
            :rows="3"
            placeholder="请输入请求内容"
          />
        </ElFormItem>
        <ElFormItem label="用户ID" prop="userId">
          <ElInput 
            v-model.number="replyForm.userId"
            placeholder="请输入用户ID"
          />
        </ElFormItem>
        <ElFormItem label="回复内容" prop="replyContent">
          <ElInput 
            v-model="replyForm.replyContent"
            type="textarea"
            :rows="3"
            placeholder="请输入回复内容"
          />
        </ElFormItem>
        <ElFormItem label="关联资源ID" prop="resourceId">
          <ElInput 
            v-model.number="replyForm.resourceId"
            placeholder="请输入关联资源ID，非必填"
          />
        </ElFormItem>
        <ElFormItem label="父回复ID" prop="parentId">
          <ElInput 
            v-model.number="replyForm.parentId"
            placeholder="请输入父回复ID，非必填"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="showFormModal = false">取消</ElButton>
        <ElButton type="primary" @click="submitReplyForm">保存</ElButton>
      </template>
    </ElDialog>
  </Page>
</template>

<script lang="ts" setup>
import { h, ref, reactive, onMounted, nextTick } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { Page } from "@vben/common-ui";
import {
  ElButton,
  ElDescriptions,
  ElDescriptionsItem,
  ElTag,
  ElDialog,
  ElMessage,
  ElMessageBox,
  ElForm,
  ElFormItem,
  ElInput,
  ElTooltip
} from "element-plus";

import { useVbenVxeGrid } from "../../adapter/vxe-table";
import type { VbenFormProps } from "../../adapter/form";

import type { AdminResourceRequestReplyDTO } from '../../api/core/resource-request-reply';
import { 
  getReplyDetailApi,
  searchRepliesApi,
  addReplyApi,
  updateReplyContentApi,
  updateReplyStatusApi,
  formatDate
} from '../../api/core/resource-request-reply';

// 当前选中的回复
const currentReply = ref<AdminResourceRequestReplyDTO | null>(null);

// 弹窗控制
const showDetailModal = ref(false);
const showFormModal = ref(false);
const isEdit = ref(false);

// 表单相关
const replyFormRef = ref<FormInstance>();
const replyForm = reactive<Partial<AdminResourceRequestReplyDTO>>({
  requestId: undefined,
  content: '',
  userId: undefined,
  replyContent: '',
  resourceId: undefined,
  parentId: undefined,
  isDeleted: 0
});

// 表单验证规则
const replyFormRules = reactive<FormRules>({
  requestId: [
    { required: true, message: '请输入请求ID', trigger: 'blur' },
    { type: 'number', message: '请求ID必须为数字', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入请求内容', trigger: 'blur' }
  ],
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur' },
    { type: 'number', message: '用户ID必须为数字', trigger: 'blur' }
  ],
  replyContent: [
    { required: true, message: '请输入回复内容', trigger: 'blur' }
  ],
  resourceId: [
    { type: 'number', message: '资源ID必须为数字', trigger: 'blur' }
  ],
  parentId: [
    { type: 'number', message: '父回复ID必须为数字', trigger: 'blur' }
  ]
});

// 搜索表单配置
const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入请求内容",
      },
      fieldName: "content",
      label: "请求内容",
    },
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入回复内容",
      },
      fieldName: "replyContent",
      label: "回复内容",
    },
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入资源名称",
      },
      fieldName: "resourceName",
      label: "资源名称",
    },
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入用户名称",
      },
      fieldName: "userName",
      label: "用户名称",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: [
          { label: "正常", value: 0 },
          { label: "已删除", value: 1 },
        ],
        placeholder: "请选择状态",
      },
      fieldName: "isDeleted",
      label: "状态",
    },
    {
      component: "DatePicker",
      componentProps: {
        type: "daterange",
        valueFormat: "YYYY-MM-DD",
        placeholder: "请选择回复时间",
      },
      fieldName: "dateRange",
      label: "回复时间",
    },
  ],
  showCollapseButton: true,
  submitButtonOptions: {
    content: "查询",
  },
  submitOnChange: false,
  submitOnEnter: true,
};

// 使用真实API获取数据
async function getReplyApi(params: {
  page: number;
  pageSize: number;
  content?: string;
  replyContent?: string;
  userName?: string;
  resourceName?: string;
  isDeleted?: number;
  dateRange?: string[];
}) {
  try {
    const { page, pageSize, dateRange, ...otherParams } = params;
    
    // 构建搜索请求
    const searchParams: any = {
      page,
      size: pageSize,
      ...otherParams
    };
    
    if (dateRange && dateRange.length === 2) {
      searchParams.startDate = dateRange[0];
      searchParams.endDate = dateRange[1];
    }
    
    const result = await searchRepliesApi(searchParams);
    return {
      total: result.total,
      items: result.list
    };
  } catch (error) {
    console.error('获取回复数据失败', error);
    ElMessage.error('获取回复数据失败');
    return {
      total: 0,
      items: []
    };
  }
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
    { field: "id", title: "ID", width: 60, fixed: "left" },
    { 
      field: "content", 
      title: "请求内容", 
      minWidth: 200,
      showOverflow: true,
    },
    { 
      field: "userName", 
      title: "用户", 
      width: 150,
      showOverflow: true,
    },
    { 
      field: "replyContent", 
      title: "回复内容", 
      minWidth: 250,
      showOverflow: true,
    },
    { 
      field: "resourceName", 
      title: "关联资源", 
      width: 180,
      showOverflow: true,
    },
    { 
      field: "parentId", 
      title: "父回复", 
      width: 120,
      slots: {
        default: ({ row }: { row: AdminResourceRequestReplyDTO }) => [
          row.parentId ? h(
            ElTooltip,
            {
              content: row.parentContent || '无内容',
              placement: 'top',
              effect: 'light'
            },
            {
              default: () => h(
                'span',
                {
                  class: 'parent-link'
                },
                `ID: ${row.parentId}`
              )
            }
          ) : h('span', {}, '无')
        ]
      },
    },
    { 
      field: "createTime", 
      title: "创建时间", 
      width: 160,
      formatter: ({ row }: { row: AdminResourceRequestReplyDTO }) => 
        formatDate(row.createTime),
    },
    { 
      field: "replyTime", 
      title: "回复时间", 
      width: 160,
      formatter: ({ row }: { row: AdminResourceRequestReplyDTO }) => 
        formatDate(row.replyTime),
    },
    { 
      field: "isDeleted", 
      title: "状态", 
      width: 80,
      slots: {
        default: ({ row }: { row: AdminResourceRequestReplyDTO }) => [
          h(
            ElTag,
            {
              type: row.isDeleted === 0 ? "success" : "danger",
              effect: "light",
            },
            { default: () => row.isDeleted === 0 ? "正常" : "已删除" }
          ),
        ],
      },
    },
    {
      title: "操作",
      width: 220,
      fixed: "right",
      slots: {
        default: ({ row }: { row: AdminResourceRequestReplyDTO }) => [
          h(
            ElButton,
            {
              size: "small",
              type: "primary",
              onClick: () => handleView(row),
            },
            { default: () => "查看" }
          ),
          row.isDeleted === 0 ? h(
            ElButton,
            {
              size: "small",
              type: "warning",
              onClick: () => handleEdit(row),
            },
            { default: () => "编辑" }
          ) : null,
          row.isDeleted === 0 ? h(
            ElButton,
            {
              size: "small",
              type: "danger",
              onClick: () => handleDelete(row),
            },
            { default: () => "删除" }
          ) : h(
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
      query: async ({ page }: { page: { currentPage: number; pageSize: number } }, formValues: any) => {
        return await getReplyApi({
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

// 查看回复详情
const handleView = async (row: AdminResourceRequestReplyDTO) => {
  try {
    const response = await getReplyDetailApi(row.id);
    currentReply.value = response;
    showDetailModal.value = true;
  } catch (error) {
    console.error('获取回复详情失败', error);
    ElMessage.error('获取回复详情失败');
  }
};

// 新增回复
const handleAdd = () => {
  isEdit.value = false;
  // 重置表单
  Object.assign(replyForm, {
    requestId: undefined,
    content: '',
    userId: undefined,
    replyContent: '',
    resourceId: undefined,
    parentId: undefined,
    isDeleted: 0
  });
  
  showFormModal.value = true;
  
  // 等DOM更新后再获取表单实例
  nextTick(() => {
    if (replyFormRef.value) {
      replyFormRef.value.resetFields();
    }
  });
};

// 编辑回复
const handleEdit = (row: AdminResourceRequestReplyDTO) => {
  isEdit.value = true;
  
  // 填充表单数据
  Object.assign(replyForm, {
    id: row.id,
    requestId: row.requestId,
    content: row.content,
    userId: row.userId,
    replyContent: row.replyContent,
    resourceId: row.resourceId,
    parentId: row.parentId
  });
  
  showFormModal.value = true;
};

// 提交表单
const submitReplyForm = async () => {
  if (!replyFormRef.value) return;
  
  await replyFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        if (isEdit.value) {
          // 编辑模式 - 只更新回复内容
          if (replyForm.id && replyForm.replyContent) {
            await updateReplyContentApi(replyForm.id, replyForm.replyContent);
            ElMessage.success('编辑回复成功');
          }
        } else {
          // 新增模式
          await addReplyApi(replyForm);
          ElMessage.success('新增回复成功');
        }
        
        // 关闭表单弹窗并刷新表格
        showFormModal.value = false;
        gridApi.query();
        
      } catch (error) {
        console.error(isEdit.value ? '编辑回复失败' : '新增回复失败', error);
        ElMessage.error(isEdit.value ? '编辑回复失败' : '新增回复失败');
      }
    } else {
      console.log('表单验证失败', fields);
      ElMessage.warning('请正确填写表单！');
    }
  });
};

// 删除回复
const handleDelete = async (row: AdminResourceRequestReplyDTO) => {
  ElMessageBox.confirm(`确认删除该回复吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        // 调用逻辑删除API
        await updateReplyStatusApi(row.id, 1);
        ElMessage.success("删除成功");
        
        // 如果是在详情弹窗中删除，则更新当前回复
        if (currentReply.value && currentReply.value.id === row.id) {
          currentReply.value.isDeleted = 1;
        }
        
        // 刷新表格数据
        gridApi.query();
      } catch (error) {
        console.error("删除回复失败", error);
        ElMessage.error("删除回复失败");
      }
    })
    .catch(() => {});
};

// 恢复回复
const handleRestore = async (row: AdminResourceRequestReplyDTO) => {
  ElMessageBox.confirm(`确认恢复该回复吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "success",
  })
    .then(async () => {
      try {
        // 调用恢复API
        await updateReplyStatusApi(row.id, 0);
        ElMessage.success("恢复成功");
        
        // 如果是在详情弹窗中恢复，则更新当前回复
        if (currentReply.value && currentReply.value.id === row.id) {
          currentReply.value.isDeleted = 0;
        }
        
        // 刷新表格数据
        gridApi.query();
      } catch (error) {
        console.error("恢复回复失败", error);
        ElMessage.error("恢复回复失败");
      }
    })
    .catch(() => {});
};

// 页面加载时初始化数据
onMounted(() => {
  // 加载第一页数据
  gridApi.query();
});
</script>

<style scoped>
.reply-content {
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
  white-space: pre-wrap;
  word-break: break-all;
}

.parent-reply {
  background-color: #ebeef5;
  border-left: 4px solid #909399;
  padding-left: 16px;
}

.parent-link {
  color: #409eff;
  cursor: pointer;
  text-decoration: underline;
}
</style>
