<template>
  <Page title="API接口管理" description="系统API接口资源管理">
    <div class="api-interface-container">
      <div class="toolbar">
        <div class="toolbar-left">
          <ElButton type="primary" @click="handleAddApi">添加接口</ElButton>
          <ElButton @click="refreshList">刷新</ElButton>
          <ElButton type="success" @click="handleTestApi">接口测试</ElButton>
          <ElButton type="info" @click="showMonitor">接口监控</ElButton>
        </div>
        <div class="toolbar-right">
          <ElInput
            v-model="queryParams.name"
            placeholder="接口名称"
            style="width: 180px"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <ElInput
            v-model="queryParams.url"
            placeholder="接口路径"
            style="width: 180px; margin-left: 10px"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <ElSelect
            v-model="queryParams.method"
            placeholder="请求方法"
            clearable
            style="width: 120px; margin-left: 10px"
            @change="handleSearch"
          >
            <ElOption v-for="method in ApiMethods" :key="method" :label="method" :value="method" />
          </ElSelect>
          <ElSelect
            v-model="queryParams.status"
            placeholder="状态"
            clearable
            style="width: 120px; margin-left: 10px"
            @change="handleSearch"
          >
            <ElOption label="启用" :value="ApiStatus.Enabled" />
            <ElOption label="禁用" :value="ApiStatus.Disabled" />
          </ElSelect>
          <ElButton type="primary" style="margin-left: 10px" @click="handleSearch">
            <ElIcon><Search /></ElIcon>
            搜索
          </ElButton>
        </div>
      </div>

      <ElTable
        :data="apiList"
        border
        style="width: 100%"
        v-loading="loading"
      >
        <ElTableColumn v-for="col in apiTableColumns" 
          :key="col.prop" 
          :prop="col.prop" 
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          :fixed="col.fixed" 
          show-overflow-tooltip
        >
          <template #default="{ row }" v-if="col.prop === 'method'">
            <ElTag :type="getMethodTagType(row.method)">{{ row.method }}</ElTag>
          </template>
          <template #default="{ row }" v-if="col.prop === 'status'">
            <ElTag :type="row.status === ApiStatus.Enabled ? 'success' : 'info'">
              {{ row.status === ApiStatus.Enabled ? '启用' : '禁用' }}
            </ElTag>
          </template>
          <template #default="{ row }" v-if="col.prop === 'action'">
            <ElButton type="primary" size="small" @click.stop="handleEditApi(row)">编辑</ElButton>
            <ElButton type="success" size="small" @click.stop="handleViewApi(row)">查看</ElButton>
            <ElButton 
              :type="row.status === ApiStatus.Enabled ? 'warning' : 'success'" 
              size="small" 
              @click.stop="handleToggleStatus(row)"
            >
              {{ row.status === ApiStatus.Enabled ? '禁用' : '启用' }}
            </ElButton>
            <ElButton type="danger" size="small" @click.stop="handleDeleteApi(row)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <div class="pagination-container">
        <ElPagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- API表单对话框 -->
      <ElDialog
        v-model="apiDialogVisible"
        :title="isEdit ? '编辑API接口' : '添加API接口'"
        width="700px"
        destroy-on-close
      >
        <ElForm
          ref="apiForm"
          :model="apiFormData"
          :rules="apiFormRules"
          label-width="120px"
        >
          <ElFormItem label="接口名称" prop="name">
            <ElInput v-model="apiFormData.name" placeholder="请输入接口名称" />
          </ElFormItem>
          <ElFormItem label="接口路径" prop="url">
            <ElInput v-model="apiFormData.url" placeholder="请输入接口路径，以/开头" />
          </ElFormItem>
          <ElFormItem label="请求方法" prop="method">
            <ElSelect v-model="apiFormData.method" placeholder="请选择请求方法">
              <ElOption v-for="method in ApiMethods" :key="method" :label="method" :value="method" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="关联权限" prop="permission_id">
            <ElSelect v-model="apiFormData.permission_id" placeholder="请选择关联权限" clearable>
              <ElOption 
                v-for="item in permissionOptions" 
                :key="item.id" 
                :label="item.name" 
                :value="item.id" 
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="描述" prop="description">
            <ElInput 
              v-model="apiFormData.description" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入接口描述" 
            />
          </ElFormItem>
          <ElFormItem label="状态" prop="status">
            <ElRadioGroup v-model="apiFormData.status">
              <ElRadio :label="ApiStatus.Enabled">启用</ElRadio>
              <ElRadio :label="ApiStatus.Disabled">禁用</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElForm>
        <template #footer>
          <ElButton @click="apiDialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="handleSaveApi">确定</ElButton>
        </template>
      </ElDialog>

      <!-- API接口测试对话框 -->
      <ElDialog
        v-model="testDialogVisible"
        title="API接口测试"
        width="800px"
        destroy-on-close
      >
        <ElForm :model="testFormData" label-width="100px">
          <ElFormItem label="请求地址" required>
            <ElInput v-model="testFormData.url" placeholder="请输入请求地址" />
          </ElFormItem>
          <ElFormItem label="请求方法" required>
            <ElSelect v-model="testFormData.method" style="width: 120px">
              <ElOption v-for="method in ApiMethods" :key="method" :label="method" :value="method" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="请求头">
            <div v-for="(header, index) in testFormData.headers" :key="index" class="param-item">
              <ElInput v-model="header.key" placeholder="参数名" style="width: 200px" />
              <ElInput v-model="header.value" placeholder="参数值" style="width: 200px; margin-left: 10px" />
              <ElButton 
                v-if="index === testFormData.headers.length - 1" 
                type="primary" 
                icon="Plus" 
                circle 
                style="margin-left: 10px"
                @click="addHeader"
              />
              <ElButton 
                v-if="testFormData.headers.length > 1" 
                type="danger" 
                icon="Delete" 
                circle 
                style="margin-left: 10px"
                @click="removeHeader(index)"
              />
            </div>
          </ElFormItem>
          <ElFormItem label="请求参数">
            <div v-for="(param, index) in testFormData.params" :key="index" class="param-item">
              <ElInput v-model="param.key" placeholder="参数名" style="width: 200px" />
              <ElInput v-model="param.value" placeholder="参数值" style="width: 200px; margin-left: 10px" />
              <ElButton 
                v-if="index === testFormData.params.length - 1" 
                type="primary" 
                icon="Plus" 
                circle 
                style="margin-left: 10px"
                @click="addParam"
              />
              <ElButton 
                v-if="testFormData.params.length > 1" 
                type="danger" 
                icon="Delete" 
                circle 
                style="margin-left: 10px"
                @click="removeParam(index)"
              />
            </div>
          </ElFormItem>
          <ElFormItem label="请求体" v-if="['POST', 'PUT', 'PATCH'].includes(testFormData.method)">
            <ElInput 
              v-model="testFormData.body" 
              type="textarea" 
              :rows="5" 
              placeholder="请输入JSON格式的请求体" 
            />
          </ElFormItem>
        </ElForm>
        <div class="test-button-container">
          <ElButton type="primary" @click="executeApiTest">执行测试</ElButton>
        </div>
        
        <div v-if="testResult" class="test-result">
          <h3>测试结果</h3>
          <div class="result-item">
            <div class="result-label">状态码:</div>
            <div class="result-value">
              <ElTag :type="testResult.status >= 200 && testResult.status < 300 ? 'success' : 'danger'">
                {{ testResult.status }} {{ testResult.statusText }}
              </ElTag>
            </div>
          </div>
          <div class="result-item">
            <div class="result-label">响应时间:</div>
            <div class="result-value">{{ testResult.time }}ms</div>
          </div>
          <div class="result-item">
            <div class="result-label">响应头:</div>
            <div class="result-value headers">
              <div v-for="(value, key) in testResult.headers" :key="key" class="header-item">
                {{ key }}: {{ value }}
              </div>
            </div>
          </div>
          <div class="result-item">
            <div class="result-label">响应体:</div>
            <div class="result-value">
              <div class="response-body">
                <pre>{{ JSON.stringify(testResult.data, null, 2) }}</pre>
              </div>
            </div>
          </div>
        </div>
      </ElDialog>

      <!-- 监控数据对话框 -->
      <ElDialog
        v-model="monitorDialogVisible"
        title="API接口监控"
        width="900px"
        destroy-on-close
      >
        <ElTable :data="monitorData" border style="width: 100%">
          <ElTableColumn 
            v-for="col in monitorTableColumns" 
            :key="col.prop" 
            :prop="col.prop" 
            :label="col.label"
            :width="col.width"
            :min-width="col.minWidth" 
            show-overflow-tooltip
          >
            <template #default="{ row }" v-if="col.prop === 'method'">
              <ElTag :type="getMethodTagType(row.method)">{{ row.method }}</ElTag>
            </template>
            <template #default="{ row }" v-if="col.prop === 'success_rate'">
              <ElProgress 
                :percentage="row.success_rate" 
                :status="getSuccessRateStatus(row.success_rate)" 
              />
            </template>
          </ElTableColumn>
        </ElTable>
      </ElDialog>
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
  ElDialog,
  ElRadioGroup,
  ElRadio,
  ElIcon,
  ElMessage,
  ElProgress,
} from "element-plus";
import { Search, Plus, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import {
  ApiStatus,
  ApiMethods,
  type ApiMethod,
  type ApiResource,
  type ApiQueryParams,
  type ApiResourceForm,
  type ApiTestForm,
  type ApiTestResult,
  type ApiMonitorData,
  apiTableColumns,
  monitorTableColumns,
  defaultApiQueryParams,
  defaultApiForm,
  defaultApiTestForm,
  apiFormRules,
  generateMockApiData,
  generateMockMonitorData,
} from "./api-interface-data";

// 查询参数
const queryParams = reactive<ApiQueryParams>({...defaultApiQueryParams});
const loading = ref(false);
const total = ref(0);
const apiList = ref<ApiResource[]>([]);

// API表单相关
const apiDialogVisible = ref(false);
const isEdit = ref(false);
const apiFormData = reactive<ApiResourceForm>({...defaultApiForm});
const apiForm = ref<FormInstance>();

// 测试相关
const testDialogVisible = ref(false);
const testFormData = reactive<ApiTestForm>({...defaultApiTestForm});
const testResult = ref<ApiTestResult | null>(null);

// 监控相关
const monitorDialogVisible = ref(false);
const monitorData = ref<ApiMonitorData[]>([]);

// 权限选项
const permissionOptions = ref([
  { id: 10101, name: "查看用户列表" },
  { id: 10102, name: "添加用户" },
  { id: 10103, name: "编辑用户" },
  { id: 10104, name: "删除用户" },
  { id: 10201, name: "查看角色列表" },
  { id: 10202, name: "添加角色" },
]);

// 初始化
onMounted(() => {
  refreshList();
});

// 刷新列表
const refreshList = () => {
  loading.value = true;
  // 这里模拟API调用，实际应用需替换为真实API调用
  setTimeout(() => {
    const mockData = generateMockApiData(20);
    apiList.value = mockData;
    total.value = mockData.length;
    loading.value = false;
  }, 500);
};

// 处理搜索
const handleSearch = () => {
  queryParams.page = 1;
  refreshList();
};

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  queryParams.pageSize = size;
  refreshList();
};

// 处理当前页变化
const handleCurrentChange = (page: number) => {
  queryParams.page = page;
  refreshList();
};

// 获取请求方法标签类型
const getMethodTagType = (method: string): string => {
  const typeMap: Record<string, string> = {
    GET: "",
    POST: "success",
    PUT: "warning",
    DELETE: "danger",
    PATCH: "info",
    OPTIONS: "",
    HEAD: "",
  };
  return typeMap[method] || "";
};

// 添加API
const handleAddApi = () => {
  isEdit.value = false;
  Object.assign(apiFormData, defaultApiForm);
  apiDialogVisible.value = true;
};

// 编辑API
const handleEditApi = (api: ApiResource) => {
  isEdit.value = true;
  Object.assign(apiFormData, {
    id: api.id,
    name: api.name,
    url: api.url,
    method: api.method,
    description: api.description,
    permission_id: api.permission_id,
    status: api.status,
  });
  apiDialogVisible.value = true;
};

// 查看API
const handleViewApi = (api: ApiResource) => {
  // 设置测试表单数据
  testFormData.url = api.url;
  testFormData.method = api.method;
  testDialogVisible.value = true;
};

// 保存API
const handleSaveApi = async () => {
  if (!apiForm.value) return;
  
  await apiForm.value.validate((valid) => {
    if (valid) {
      if (isEdit.value) {
        // 更新API
        ElMessage.success("API接口更新成功");
      } else {
        // 添加API
        ElMessage.success("API接口添加成功");
      }
      apiDialogVisible.value = false;
      refreshList();
    }
  });
};

// 切换API状态
const handleToggleStatus = (api: ApiResource) => {
  const newStatus = api.status === ApiStatus.Enabled ? ApiStatus.Disabled : ApiStatus.Enabled;
  // 这里应该调用实际的API来更新状态
  // 模拟API调用
  ElMessage.success(`API接口状态已${newStatus === ApiStatus.Enabled ? '启用' : '禁用'}`);
  refreshList();
};

// 删除API
const handleDeleteApi = (api: ApiResource) => {
  // 这里应该添加删除确认对话框
  // 模拟API调用
  ElMessage.success("API接口删除成功");
  refreshList();
};

// 打开接口测试对话框
const handleTestApi = () => {
  Object.assign(testFormData, defaultApiTestForm);
  testResult.value = null;
  testDialogVisible.value = true;
};

// 添加请求头
const addHeader = () => {
  testFormData.headers.push({ key: "", value: "" });
};

// 移除请求头
const removeHeader = (index: number) => {
  testFormData.headers.splice(index, 1);
};

// 添加请求参数
const addParam = () => {
  testFormData.params.push({ key: "", value: "" });
};

// 移除请求参数
const removeParam = (index: number) => {
  testFormData.params.splice(index, 1);
};

// 执行API测试
const executeApiTest = () => {
  // 验证测试表单
  if (!testFormData.url) {
    ElMessage.warning("请输入请求地址");
    return;
  }
  
  if (!testFormData.method) {
    ElMessage.warning("请选择请求方法");
    return;
  }
  
  // 模拟API调用
  const startTime = Date.now();
  
  // 随机状态码
  const statusCodes = [200, 201, 400, 401, 403, 404, 500];
  const status = statusCodes[Math.floor(Math.random() * statusCodes.length)];
  
  const statusTexts: Record<number, string> = {
    200: "OK",
    201: "Created",
    400: "Bad Request",
    401: "Unauthorized",
    403: "Forbidden",
    404: "Not Found",
    500: "Internal Server Error",
  };
  
  // 模拟测试结果
  setTimeout(() => {
    testResult.value = {
      status,
      statusText: statusTexts[status] || "",
      headers: {
        "Content-Type": "application/json",
        "X-Request-ID": `req-${Math.random().toString(36).substring(2, 10)}`,
        "Date": new Date().toUTCString(),
        "Server": "Nginx/1.20.1",
      },
      data: status >= 200 && status < 300 
        ? { code: 0, data: { id: 1, name: "测试数据" }, message: "操作成功" }
        : { code: status, message: `请求失败: ${statusTexts[status]}` },
      time: Date.now() - startTime,
    };
  }, 500 + Math.random() * 1000);
};

// 显示监控信息
const showMonitor = () => {
  monitorDialogVisible.value = true;
  // 加载监控数据
  monitorData.value = generateMockMonitorData(10);
};

// 获取成功率状态
const getSuccessRateStatus = (rate: number): "" | "success" | "exception" | "warning" => {
  if (rate >= 90) return "success";
  if (rate >= 70) return "warning";
  return "exception";
};
</script>

<style scoped>
.api-interface-container {
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

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.param-item {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.test-button-container {
  margin: 16px 0;
  display: flex;
  justify-content: center;
}

.test-result {
  margin-top: 20px;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.result-item {
  margin-bottom: 12px;
  display: flex;
}

.result-label {
  width: 100px;
  font-weight: bold;
}

.result-value {
  flex: 1;
}

.headers {
  max-height: 150px;
  overflow-y: auto;
}

.response-body {
  background-color: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}

pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>