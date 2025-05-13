<template>
  <Page title="资源评论" description="管理用户对资源的评论内容">
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
    
    <!-- 评论详情弹窗 -->
    <ElDialog
      v-model="showDetailModal"
      title="评论详情"
      width="700px"
      append-to-body
    >
      <div v-if="currentComment">
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="评论ID">
            {{ currentComment.id }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="资源标题">
            {{ currentComment.resourceTitle }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="用户昵称">
            {{ currentComment.userName }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="评论时间">
            {{ formatDate(currentComment.createTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="评论状态">
            <ElTag :type="currentComment.isDeleted === 0 ? 'success' : 'danger'">
              {{ currentComment.isDeleted === 0 ? '正常' : '已删除' }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="父评论" v-if="currentComment.parentId">
            {{ currentComment.parentContent ? `(${currentComment.parentId}) ${currentComment.parentContent.substring(0, 20)}...` : `评论#${currentComment.parentId}` }}
          </ElDescriptionsItem>
        </ElDescriptions>
        
        <div class="mt-4">
          <h4 class="mb-2">评论内容</h4>
          <div class="comment-content">
            {{ currentComment.content }}
          </div>
        </div>
        
        <div class="mt-4" v-if="currentComment.parentId && currentComment.parentContent">
          <h4 class="mb-2">父评论内容</h4>
          <div class="comment-content parent-comment">
            {{ currentComment.parentContent }}
          </div>
        </div>
        
        <div class="mt-4" v-if="childComments.length > 0">
          <h4 class="mb-2">回复列表</h4>
          <ElTable :data="childComments" style="width: 100%" border>
            <ElTableColumn prop="id" label="ID" width="70" />
            <ElTableColumn prop="userName" label="用户昵称" width="120" />
            <ElTableColumn prop="content" label="回复内容" show-overflow-tooltip />
            <ElTableColumn label="回复时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </ElTableColumn>
            <ElTableColumn label="状态" width="80">
              <template #default="{ row }">
                <ElTag :type="row.isDeleted === 0 ? 'success' : 'danger'">
                  {{ row.isDeleted === 0 ? '正常' : '已删除' }}
                </ElTag>
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
      </div>
      <template #footer>
        <ElButton @click="showDetailModal = false">关闭</ElButton>
        <ElButton 
          v-if="currentComment && currentComment.isDeleted === 0" 
          type="danger" 
          @click="handleDelete(currentComment)"
        >
          删除评论
        </ElButton>
        <ElButton 
          v-if="currentComment && currentComment.isDeleted === 1" 
          type="success" 
          @click="handleRestore(currentComment)"
        >
          恢复评论
        </ElButton>
      </template>
    </ElDialog>
  </Page>
</template>

<script lang="ts" setup>
import { h, ref, onMounted } from "vue";

import { Page } from "@vben/common-ui";
import {
  ElButton,
  ElTable,
  ElTableColumn,
  ElDescriptions,
  ElDescriptionsItem,
  ElTag,
  ElDialog,
  ElMessage,
  ElMessageBox,
  ElTooltip
} from "element-plus";

import { useVbenVxeGrid } from "../../adapter/vxe-table";
import type { VbenFormProps } from "../../adapter/form";

import { COMMENT_STATUS_OPTIONS } from "./comment-data";
import { 
  getCommentsApi, 
  getCommentDetailApi, 
  searchCommentsApi, 
  updateCommentStatusApi, 
  deleteCommentApi,
  formatDate,
  type AdminCommentDTO
} from "../../api/core/comment";

// 当前选中的评论
const currentComment = ref<AdminCommentDTO | null>(null);
// 子评论列表（显示在详情中）
const childComments = ref<AdminCommentDTO[]>([]);

// 弹窗控制
const showDetailModal = ref(false);

// 修改搜索表单配置 - 移除不需要的学院和资源类型搜索框
const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入评论内容",
      },
      fieldName: "content",
      label: "评论内容",
    },
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入资源标题",
      },
      fieldName: "resourceTitle",
      label: "资源标题",
    },
    {
      component: "Input",
      componentProps: {
        placeholder: "请输入用户昵称",
      },
      fieldName: "userName",
      label: "用户昵称",
    },
    {
      component: "Select",
      componentProps: {
        allowClear: true,
        options: COMMENT_STATUS_OPTIONS,
        placeholder: "请选择状态",
      },
      fieldName: "isDeleted",
      label: "评论状态",
    },
    {
      component: "DatePicker",
      componentProps: {
        type: "daterange",
        valueFormat: "YYYY-MM-DD",
        placeholder: "请选择评论时间",
      },
      fieldName: "dateRange",
      label: "评论时间",
    },
  ],
  showCollapseButton: true,
  submitButtonOptions: {
    content: "查询",
  },
  submitOnChange: false,
  submitOnEnter: true,
};

// 使用真实API获取评论数据
async function getCommentApi(params: {
  page: number;
  pageSize: number;
  content?: string;
  resourceTitle?: string;
  userName?: string;
  isDeleted?: number;
  dateRange?: string[];
}) {
  try {
    const { page, pageSize, content, resourceTitle, userName, isDeleted, dateRange } = params;
    
    // 构建搜索请求参数
    const searchParams = {
      page,
      size: pageSize,
      content,
      resourceTitle,
      userName,
      isDeleted,
      startDate: dateRange && dateRange.length > 0 ? dateRange[0] : undefined,
      endDate: dateRange && dateRange.length > 1 ? dateRange[1] : undefined
    };
    
    // 调用后端API
    const result = await searchCommentsApi(searchParams);
    
    return {
      total: result.total,
      items: result.list
    };
  } catch (error) {
    console.error('获取评论列表失败:', error);
    ElMessage.error('获取评论列表失败');
    
    // 查询失败时返回空数据
    return {
      total: 0,
      items: []
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
      field: "resourceTitle", 
      title: "资源标题", 
      minWidth: 200,
    },
    { field: "userName", title: "用户昵称", width: 120 },
    { 
      field: "content", 
      title: "评论内容", 
      minWidth: 250,
      showOverflow: true,
    },
    { 
      field: "parentId", 
      title: "父评论", 
      width: 120,
      slots: {
        default: ({ row }: { row: AdminCommentDTO }) => {
          if (!row.parentId) return '';
          
          return h(
            ElTooltip,
            {
              content: row.parentContent || '已删除的评论',
              placement: "top",
              effect: "light",
              enterable: true,
              popperClass: "parent-comment-tooltip",
              showAfter: 200
            },
            {
              default: () => h(
                'div',
                {
                  class: 'parent-comment-link',
                  style: 'cursor: pointer; color: #1890ff;',
                  onClick: (e) => {
                    e.stopPropagation();
                    if (row.parentId) viewParentComment(row.parentId);
                  }
                },
                `ID: ${row.parentId}`
              )
            }
          );
        }
      }
    },
    { 
      field: "createTime", 
      title: "评论时间", 
      width: 160,
      formatter: ({ row }: { row: AdminCommentDTO }) => formatDate(row.createTime),
    },
    { 
      field: "isDeleted", 
      title: "状态", 
      width: 80,
      slots: {
        default: ({ row }: { row: AdminCommentDTO }) => [
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
      width: 180,
      slots: {
        default: ({ row }: { row: AdminCommentDTO }) => [
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
      query: async ({ page }, formValues) => {
        return await getCommentApi({
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

// 获取子评论
async function fetchChildComments(parentId: number) {
  try {
    const params = {
      page: 1,
      size: 100,
      parentId
    };
    
    const result = await searchCommentsApi(params);
    return result.list || [];
  } catch (error) {
    console.error('获取子评论失败:', error);
    return [];
  }
}

// 查看评论详情
const handleView = async (row: AdminCommentDTO) => {
  try {
    // 获取完整的评论详情
    const commentDetail = await getCommentDetailApi(row.id);
    currentComment.value = commentDetail;
    
    // 获取子评论
    childComments.value = await fetchChildComments(row.id);
    
    showDetailModal.value = true;
  } catch (error) {
    console.error('获取评论详情失败:', error);
    ElMessage.error('获取评论详情失败');
  }
};

// 删除评论
const handleDelete = (row: AdminCommentDTO) => {
  ElMessageBox.confirm(`确认删除该评论吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        // 调用API进行逻辑删除
        const success = await updateCommentStatusApi(row.id, 1);

        if (success) {
          ElMessage.success("删除成功");
          
          // 如果是在详情弹窗中删除，则更新当前评论
          if (currentComment.value && currentComment.value.id === row.id) {
            currentComment.value.isDeleted = 1;
            currentComment.value.updateTime = new Date();
          }
          
          // 刷新表格数据
          gridApi.query();
        } else {
          ElMessage.error("删除失败");
        }
      } catch (error) {
        console.error("删除评论失败", error);
        ElMessage.error("删除评论失败");
      }
    })
    .catch(() => {});
};

// 恢复评论
const handleRestore = (row: AdminCommentDTO) => {
  ElMessageBox.confirm(`确认恢复该评论吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "success",
  })
    .then(async () => {
      try {
        // 调用API恢复评论
        const success = await updateCommentStatusApi(row.id, 0);

        if (success) {
          ElMessage.success("恢复成功");
          
          // 如果是在详情弹窗中恢复，则更新当前评论
          if (currentComment.value && currentComment.value.id === row.id) {
            currentComment.value.isDeleted = 0;
            currentComment.value.updateTime = new Date();
          }
          
          // 刷新表格数据
          gridApi.query();
        } else {
          ElMessage.error("恢复失败");
        }
      } catch (error) {
        console.error("恢复评论失败", error);
        ElMessage.error("恢复评论失败");
      }
    })
    .catch(() => {});
};

// 添加一个方法，用于点击父评论ID查看父评论详情
const viewParentComment = async (parentId: number | null) => {
  if (!parentId) return;
  
  try {
    // 获取父评论详情
    const parentComment = await getCommentDetailApi(parentId);
    
    // 打开详情弹窗显示父评论
    currentComment.value = parentComment;
    
    // 获取父评论的子评论
    childComments.value = await fetchChildComments(parentId);
    
    showDetailModal.value = true;
  } catch (error) {
    console.error('获取父评论详情失败:', error);
    ElMessage.error('获取父评论详情失败');
  }
};

// 在组件挂载时加载数据
onMounted(() => {
  // 刷新表格数据
  gridApi.query();
});
</script>

<style scoped>
.comment-content {
  padding: 16px;
  /* 移除背景颜色，使用系统默认背景色 */
  /* background-color: #f5f7fa; */
  border-radius: 4px;
  margin-bottom: 16px;
  white-space: pre-wrap;
  word-break: break-all;
}

.parent-comment {
  /* 仅为父评论保留一个轻微的视觉区分 */
  border-left: 4px solid #909399;
  padding-left: 16px;
  /* 移除父评论的背景色 */
  /* background-color: #ebeef5; */
}

.parent-comment-link:hover {
  text-decoration: underline;
}
</style>

<style>
.parent-comment-tooltip {
  max-width: 400px;
}

.parent-comment-tooltip .el-tooltip__content {
  padding: 12px !important;
}

.tooltip-content {
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;

  border-left: 3px solid #909399;
  padding: 8px;
  border-radius: 4px;
  font-size: 13px;
}
</style>
