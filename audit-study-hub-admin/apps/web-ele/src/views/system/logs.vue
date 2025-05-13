<template>
  <Page title="日志管理" description="系统操作日志查询">
    <div class="logs-container">
      <div class="search-form">
        <ElForm :inline="true" :model="searchForm" class="form-inline">
          <ElFormItem label="日志类型">
            <ElSelect v-model="searchForm.type" placeholder="选择日志类型" clearable>
              <ElOption label="登录日志" value="login" />
              <ElOption label="操作日志" value="operation" />
              <ElOption label="系统日志" value="system" />
              <ElOption label="错误日志" value="error" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="操作人">
            <ElInput v-model="searchForm.username" placeholder="请输入操作人" />
          </ElFormItem>
          <ElFormItem label="IP地址">
            <ElInput v-model="searchForm.ip" placeholder="请输入IP地址" />
          </ElFormItem>
          <ElFormItem label="操作时间">
            <ElDatePicker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            />
          </ElFormItem>
          <ElFormItem>
            <ElButton type="primary" @click="handleSearch">查询</ElButton>
            <ElButton @click="resetSearch">重置</ElButton>
          </ElFormItem>
        </ElForm>
      </div>

      <div class="toolbar">
        <div class="toolbar-left">
          <ElRadioGroup v-model="currentLogType" @change="handleLogTypeChange">
            <ElRadioButton label="all">全部日志</ElRadioButton>
            <ElRadioButton label="login">登录日志</ElRadioButton>
            <ElRadioButton label="operation">操作日志</ElRadioButton>
            <ElRadioButton label="system">系统日志</ElRadioButton>
            <ElRadioButton label="error">错误日志</ElRadioButton>
          </ElRadioGroup>
        </div>
        <div class="toolbar-right">
          <ElButton type="danger" @click="handleClearLogs">清空日志</ElButton>
          <ElButton type="primary" @click="handleExportLogs">导出</ElButton>
          <ElButton @click="refreshList">刷新</ElButton>
        </div>
      </div>

      <div class="logs-table">
        <ElTable
          :data="logsList"
          style="width: 100%"
          border
          stripe
          v-loading="loading"
        >
          <ElTableColumn type="expand">
            <template #default="{ row }">
              <div class="log-expand">
                <div v-if="row.details" class="log-details">
                  <pre>{{ row.details }}</pre>
                </div>
                <ElEmpty v-else description="无详细信息" />
              </div>
            </template>
          </ElTableColumn>
          <ElTableColumn label="日志类型" width="120">
            <template #default="{ row }">
              <ElTag :type="getLogTypeTagType(row.type)" effect="plain">
                {{ getLogTypeName(row.type) }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作人" prop="username" min-width="120" />
          <ElTableColumn label="IP地址" prop="ip" width="140" />
          <ElTableColumn label="操作内容" prop="content" min-width="250" show-overflow-tooltip />
          <ElTableColumn label="操作时间" prop="create_time" width="180" sortable />
          <ElTableColumn label="状态" width="100">
            <template #default="{ row }">
              <ElTag :type="row.status === 1 ? 'success' : 'danger'" effect="plain">
                {{ row.status === 1 ? "成功" : "失败" }}
              </ElTag>
            </template>
          </ElTableColumn>
        </ElTable>
        <div class="pagination-container">
          <ElPagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="totalItems"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </Page>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue";
import { Page } from "@vben/common-ui";
import {
  ElButton,
  ElTable,
  ElTableColumn,
  ElTag,
  ElInput,
  ElSelect,
  ElOption,
  ElForm,
  ElFormItem,
  ElPagination,
  ElRadioGroup,
  ElRadioButton,
  ElDatePicker,
  ElEmpty,
  ElMessage,
  ElMessageBox,
} from "element-plus";

interface LogItem {
  id: number;
  type: string;
  username: string;
  ip: string;
  content: string;
  create_time: string;
  status: number;
  details: string | null;
}

interface SearchForm {
  type: string;
  username: string;
  ip: string;
  dateRange: string[];
}

// 搜索表单
const searchForm = reactive<SearchForm>({
  type: "",
  username: "",
  ip: "",
  dateRange: [],
});

// 数据相关
const logsList = ref<LogItem[]>([]);
const loading = ref(false);
const currentLogType = ref("all");
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);

// 初始化
onMounted(() => {
  refreshList();
});

// 刷新列表
const refreshList = () => {
  loading.value = true;
  // 模拟加载数据
  setTimeout(() => {
    // 这里添加实际的API调用
    logsList.value = getMockLogs(20);
    totalItems.value = 200; // 假设总共有200条数据
    loading.value = false;
  }, 500);
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  refreshList();
};

// 重置搜索
const resetSearch = () => {
  searchForm.type = "";
  searchForm.username = "";
  searchForm.ip = "";
  searchForm.dateRange = [];
  handleSearch();
};

// 处理日志类型切换
const handleLogTypeChange = () => {
  currentPage.value = 1;
  refreshList();
};

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  refreshList();
};

// 处理当前页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  refreshList();
};

// 处理清空日志
const handleClearLogs = () => {
  ElMessageBox.confirm(
    "确定要清空所有日志吗？此操作不可恢复！",
    "警告",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  )
    .then(() => {
      ElMessage({
        type: "success",
        message: "日志已清空",
      });
      refreshList();
    })
    .catch(() => {
      // 用户取消操作
    });
};

// 处理导出日志
const handleExportLogs = () => {
  ElMessage.info("导出功能待实现");
};

// 获取日志类型名称
const getLogTypeName = (type: string): string => {
  const typeMap: Record<string, string> = {
    login: "登录日志",
    operation: "操作日志",
    system: "系统日志",
    error: "错误日志",
  };
  return typeMap[type] || "未知类型";
};

// 获取日志类型标签类型
const getLogTypeTagType = (type: string): string => {
  const typeMap: Record<string, string> = {
    login: "info",
    operation: "success",
    system: "primary",
    error: "danger",
  };
  return typeMap[type] || "info";
};

// 生成模拟日志数据
const getMockLogs = (count: number): LogItem[] => {
  const types = ["login", "operation", "system", "error"];
  const usernames = ["admin", "user1", "user2", "system"];
  const ips = ["192.168.1.1", "127.0.0.1", "10.0.0.1", "172.16.0.1"];
  const contents = [
    "用户登录系统",
    "添加新用户",
    "修改系统配置",
    "删除记录",
    "导出数据",
    "系统初始化",
    "定时任务执行",
    "数据库备份",
    "空间清理",
  ];

  return Array.from({ length: count }, (_, index) => {
    const type = types[Math.floor(Math.random() * types.length)];
    const status = Math.random() > 0.1 ? 1 : 0; // 90%成功率
    
    return {
      id: index + 1,
      type,
      username: usernames[Math.floor(Math.random() * usernames.length)],
      ip: ips[Math.floor(Math.random() * ips.length)],
      content: contents[Math.floor(Math.random() * contents.length)],
      create_time: new Date(Date.now() - Math.floor(Math.random() * 30) * 86400000).toLocaleString(),
      status,
      details: status === 0 ? "Error: 执行失败，原因：连接超时" : null,
    };
  });
};
</script>

<style scoped>
.logs-container {
  width: 100%;
}

.search-form {
  margin-bottom: 16px;
  padding: 16px;
  border-radius: 4px;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-left {
  display: flex;
  gap: 8px;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.logs-table {
  border: 1px solid #eee;
  border-radius: 4px;
}

.log-expand {
  padding: 16px;
}

.log-details {
  padding: 10px;
  border-radius: 4px;
  font-family: monospace;
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
}

.pagination-container {
  margin-top: 16px;
  padding: 10px;
  display: flex;
  justify-content: flex-end;
}
</style> 