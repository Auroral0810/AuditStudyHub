<template>
  <Page title="定时任务" description="系统定时任务管理">
    <div class="scheduler-container">
      <div class="toolbar">
        <div class="toolbar-left">
          <ElButton type="primary" @click="handleAddTask">添加任务</ElButton>
          <ElButton @click="refreshList">刷新</ElButton>
        </div>
        <div class="toolbar-right">
          <ElInput
            v-model="searchKeyword"
            placeholder="搜索任务名称/标识"
            style="width: 220px"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <ElButton @click="handleSearch">
                <ElIcon><Search /></ElIcon>
              </ElButton>
            </template>
          </ElInput>
          <ElSelect
            v-model="searchStatus"
            placeholder="任务状态"
            clearable
            style="width: 120px; margin-left: 10px"
            @change="handleSearch"
          >
            <ElOption label="正常" :value="1" />
            <ElOption label="暂停" :value="0" />
          </ElSelect>
        </div>
      </div>

      <ElTable
        :data="taskList"
        style="width: 100%"
        border
        v-loading="loading"
      >
        <ElTableColumn type="expand">
          <template #default="{ row }">
            <div class="task-expand">
              <div class="task-item">
                <div class="task-item-label">描述</div>
                <div class="task-item-value">{{ row.description || '无' }}</div>
              </div>
              <div class="task-item">
                <div class="task-item-label">执行参数</div>
                <div class="task-item-value">{{ row.params || '无' }}</div>
              </div>
              <div class="task-item">
                <div class="task-item-label">cron表达式</div>
                <div class="task-item-value">{{ row.cron_expression }}</div>
              </div>
              <div class="task-item">
                <div class="task-item-label">创建时间</div>
                <div class="task-item-value">{{ row.create_time }}</div>
              </div>
              <div class="task-item">
                <div class="task-item-label">最近执行</div>
                <div class="task-item-value">{{ row.last_execute_time || '未执行' }}</div>
              </div>
              <div class="task-item">
                <div class="task-item-label">下次执行</div>
                <div class="task-item-value">{{ row.next_execute_time }}</div>
              </div>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn label="任务名称" prop="name" min-width="180" show-overflow-tooltip />
        <ElTableColumn label="标识" prop="key" min-width="180" show-overflow-tooltip />
        <ElTableColumn label="执行策略" prop="strategy" width="120">
          <template #default="{ row }">
            {{ getExecuteStrategy(row.strategy) }}
          </template>
        </ElTableColumn>
        <ElTableColumn label="状态" width="100">
          <template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'info'" effect="plain" size="small">
              {{ row.status === 1 ? '正常' : '暂停' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="最近执行结果" width="140">
          <template #default="{ row }">
            <ElTag v-if="row.last_execute_status !== undefined" :type="row.last_execute_status ? 'success' : 'danger'" effect="plain">
              {{ row.last_execute_status ? '成功' : '失败' }}
            </ElTag>
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <ElButton
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click.stop="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '暂停' : '恢复' }}
            </ElButton>
            <ElButton
              type="primary"
              size="small"
              @click.stop="handleEditTask(row)"
            >
              编辑
            </ElButton>
            <ElButton
              type="success"
              size="small"
              @click.stop="handleExecuteTask(row)"
            >
              执行
            </ElButton>
            <ElButton
              type="danger"
              size="small"
              @click.stop="handleDeleteTask(row)"
            >
              删除
            </ElButton>
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

      <!-- 任务表单对话框 -->
      <ElDialog
        v-model="taskDialogVisible"
        :title="isEdit ? '编辑任务' : '添加任务'"
        width="600px"
        append-to-body
        destroy-on-close
      >
        <ElForm
          ref="taskForm"
          :model="taskFormData"
          label-width="120px"
          :rules="taskFormRules"
        >
          <ElFormItem label="任务名称" prop="name">
            <ElInput v-model="taskFormData.name" placeholder="请输入任务名称" />
          </ElFormItem>
          <ElFormItem label="任务标识" prop="key">
            <ElInput v-model="taskFormData.key" placeholder="请输入任务标识" :disabled="isEdit" />
          </ElFormItem>
          <ElFormItem label="执行策略" prop="strategy">
            <ElSelect v-model="taskFormData.strategy" placeholder="请选择执行策略">
              <ElOption label="立即执行" value="immediate" />
              <ElOption label="串行执行" value="serial" />
              <ElOption label="并行执行" value="parallel" />
              <ElOption label="单例执行" value="singleton" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="cron表达式" prop="cron_expression">
            <ElInput v-model="taskFormData.cron_expression" placeholder="例如: 0 0 12 * * ?" />
          </ElFormItem>
          <ElFormItem label="执行参数" prop="params">
            <ElInput 
              v-model="taskFormData.params" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入JSON格式的执行参数" 
            />
          </ElFormItem>
          <ElFormItem label="任务描述" prop="description">
            <ElInput 
              v-model="taskFormData.description" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入任务描述" 
            />
          </ElFormItem>
          <ElFormItem label="状态" prop="status">
            <ElRadioGroup v-model="taskFormData.status">
              <ElRadio :label="1">正常</ElRadio>
              <ElRadio :label="0">暂停</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElForm>
        <template #footer>
          <ElButton @click="taskDialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="handleSaveTask">确定</ElButton>
        </template>
      </ElDialog>

      <!-- 日志抽屉 -->
      <ElDrawer
        v-model="logDrawerVisible"
        title="任务执行日志"
        size="60%"
        destroy-on-close
      >
        <div class="log-content" v-loading="logLoading">
          <ElTable
            :data="taskLogs"
            stripe
            border
            style="width: 100%"
          >
            <ElTableColumn label="任务名称" prop="task_name" min-width="150" show-overflow-tooltip />
            <ElTableColumn label="开始时间" prop="start_time" width="180" />
            <ElTableColumn label="结束时间" prop="end_time" width="180" />
            <ElTableColumn label="执行时长(秒)" prop="duration" width="120" sortable />
            <ElTableColumn label="结果" width="100">
              <template #default="{ row }">
                <ElTag :type="row.status ? 'success' : 'danger'" effect="dark">
                  {{ row.status ? '成功' : '失败' }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn label="详情" min-width="250" show-overflow-tooltip>
              <template #default="{ row }">
                <span>{{ row.message || '-' }}</span>
              </template>
            </ElTableColumn>
          </ElTable>
          <div class="pagination-container">
            <ElPagination
              v-model:current-page="logCurrentPage"
              v-model:page-size="logPageSize"
              :page-sizes="[10, 20, 50]"
              :total="logTotalItems"
              layout="total, sizes, prev, pager, next"
              @size-change="handleLogSizeChange"
              @current-change="handleLogCurrentChange"
            />
          </div>
        </div>
      </ElDrawer>
    </div>
  </Page>
</template>

<script lang="ts" setup>
import { ref, onMounted } from "vue";
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
  ElDialog,
  ElDrawer,
  ElRadioGroup,
  ElRadio,
  ElIcon,
  ElMessage,
} from "element-plus";
import { Search } from "@element-plus/icons-vue";
import type { FormRules, FormInstance } from "element-plus";

interface Task {
  id: number;
  name: string;
  key: string;
  strategy: string;
  cron_expression: string;
  params: string;
  description: string;
  status: number;
  create_time: string;
  last_execute_time: string | null;
  last_execute_status?: boolean;
  next_execute_time: string;
}

interface TaskLog {
  id: number;
  task_id: number;
  task_name: string;
  start_time: string;
  end_time: string;
  duration: number;
  status: boolean;
  message: string;
}

// 搜索相关
const searchKeyword = ref("");
const searchStatus = ref<string | number>("");
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);
const loading = ref(false);

// 任务列表
const taskList = ref<Task[]>([]);

// 表单相关
const taskDialogVisible = ref(false);
const isEdit = ref(false);
const taskFormData = ref({
  id: 0,
  name: "",
  key: "",
  strategy: "serial",
  cron_expression: "",
  params: "",
  description: "",
  status: 1,
});
const taskForm = ref<FormInstance>();
const taskFormRules: FormRules = {
  name: [
    { required: true, message: "请输入任务名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" }
  ],
  key: [
    { required: true, message: "请输入任务标识", trigger: "blur" },
    { pattern: /^[a-zA-Z0-9_]+$/, message: "只能包含字母、数字和下划线", trigger: "blur" }
  ],
  strategy: [
    { required: true, message: "请选择执行策略", trigger: "change" }
  ],
  cron_expression: [
    { required: true, message: "请输入cron表达式", trigger: "blur" }
  ],
};

// 日志相关
const logDrawerVisible = ref(false);
const logLoading = ref(false);
const taskLogs = ref<TaskLog[]>([]);
const logCurrentPage = ref(1);
const logPageSize = ref(10);
const logTotalItems = ref(0);
const currentTaskId = ref(0);

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
    taskList.value = getMockTasks(20);
    totalItems.value = 20;
    loading.value = false;
  }, 500);
};

// 处理搜索
const handleSearch = () => {
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

// 添加任务
const handleAddTask = () => {
  isEdit.value = false;
  taskFormData.value = {
    id: 0,
    name: "",
    key: "",
    strategy: "serial",
    cron_expression: "",
    params: "",
    description: "",
    status: 1,
  };
  taskDialogVisible.value = true;
};

// 编辑任务
const handleEditTask = (task: Task) => {
  isEdit.value = true;
  taskFormData.value = { ...task };
  taskDialogVisible.value = true;
};

// 保存任务
const handleSaveTask = async () => {
  if (!taskForm.value) return;
  
  await taskForm.value.validate((valid) => {
    if (valid) {
      if (isEdit.value) {
        // 更新任务
        ElMessage.success("任务更新成功");
      } else {
        // 添加任务
        ElMessage.success("任务添加成功");
      }
      taskDialogVisible.value = false;
      refreshList();
    }
  });
};

// 删除任务
const handleDeleteTask = (_task: Task) => {
  ElMessage.success("任务删除成功");
  refreshList();
};

// 执行任务
const handleExecuteTask = (task: Task) => {
  ElMessage.success(`任务 "${task.name}" 已触发执行`);
  refreshList();
};

// 切换任务状态
const handleToggleStatus = (task: Task) => {
  task.status = task.status === 1 ? 0 : 1;
  ElMessage.success(`任务状态已${task.status === 1 ? '启用' : '暂停'}`);
  refreshList();
};

// 获取策略名称
const getExecuteStrategy = (strategy: string): string => {
  const strategyMap: Record<string, string> = {
    immediate: "立即执行",
    serial: "串行执行",
    parallel: "并行执行",
    singleton: "单例执行"
  };
  return strategyMap[strategy] || "未知";
};

// 处理日志分页大小变化
const handleLogSizeChange = (size: number) => {
  logPageSize.value = size;
  loadTaskLogs();
};

// 处理日志当前页变化
const handleLogCurrentChange = (page: number) => {
  logCurrentPage.value = page;
  loadTaskLogs();
};

// 加载任务日志
const loadTaskLogs = () => {
  logLoading.value = true;
  // 模拟加载数据
  setTimeout(() => {
    // 这里添加实际的API调用
    taskLogs.value = getMockLogs(currentTaskId.value, 10);
    logTotalItems.value = 50;
    logLoading.value = false;
  }, 500);
};

// 生成模拟任务数据
const getMockTasks = (count: number): Task[] => {
  const strategies = ["immediate", "serial", "parallel", "singleton"];
  const now = new Date();
  
  return Array.from({ length: count }, (_, index) => {
    const status = Math.random() > 0.3 ? 1 : 0;
    const hasExecuted = Math.random() > 0.2;
    const lastExecuteStatus = hasExecuted ? Math.random() > 0.1 : undefined;
    
    // 创建日期
    const createTime = new Date(now.getTime() - Math.floor(Math.random() * 30) * 86400000);
    
    // 最近执行时间
    let lastExecuteTime = null;
    if (hasExecuted) {
      lastExecuteTime = new Date(now.getTime() - Math.floor(Math.random() * 24) * 3600000);
    }
    
    // 下次执行时间
    const nextExecuteTime = new Date(now.getTime() + Math.floor(Math.random() * 24) * 3600000);
    
    return {
      id: index + 1,
      name: `测试任务 ${index + 1}`,
      key: `task_${index + 1}`,
      strategy: strategies[Math.floor(Math.random() * strategies.length)],
      cron_expression: "0 0/30 * * * ?",
      params: Math.random() > 0.5 ? JSON.stringify({ param1: "value1", param2: "value2" }) : "",
      description: Math.random() > 0.3 ? `这是测试任务 ${index + 1} 的描述信息` : "",
      status,
      create_time: createTime.toLocaleString(),
      last_execute_time: lastExecuteTime ? lastExecuteTime.toLocaleString() : null,
      last_execute_status: lastExecuteStatus,
      next_execute_time: nextExecuteTime.toLocaleString(),
    };
  });
};

// 生成模拟日志数据
const getMockLogs = (taskId: number, count: number): TaskLog[] => {
  const now = new Date();
  
  return Array.from({ length: count }, (_, index) => {
    const startTime = new Date(now.getTime() - Math.floor(Math.random() * 7) * 86400000);
    const duration = Math.floor(Math.random() * 10) + 1;
    const endTime = new Date(startTime.getTime() + duration * 1000);
    const status = Math.random() > 0.2;
    
    return {
      id: index + 1,
      task_id: taskId,
      task_name: `测试任务 ${taskId}`,
      start_time: startTime.toLocaleString(),
      end_time: endTime.toLocaleString(),
      duration,
      status,
      message: status ? "执行成功" : `执行失败: ${Math.random() > 0.5 ? "连接超时" : "内部错误"}`,
    };
  });
};
</script>

<style scoped>
.scheduler-container {
  width: 100%;
  height: 100%;
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
  align-items: center;
}

.task-expand {
  padding: 10px;
}

.task-item {
  margin-bottom: 8px;
  display: flex;
}

.task-item-label {
  width: 100px;
  color: #606266;
  font-weight: bold;
}

.task-item-value {
  flex: 1;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.log-content {
  width: 100%;
  height: 100%;
}
</style> 