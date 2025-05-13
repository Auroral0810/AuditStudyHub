<script lang="ts" setup>
import { reactive, ref } from "vue";

import { Page } from "@vben/common-ui";

import {
  ElButton,
  ElCard,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElPopconfirm,
  ElTable,
  ElTableColumn,
  ElSwitch,
  ElSelect,
  ElOption,
  ElCheckbox,
  ElCheckboxGroup,
} from "element-plus";

// 模拟角色数据
const tableData = ref([
  {
    id: "1",
    name: "管理员",
    code: "admin",
    description: "系统管理员，拥有所有权限",
    status: true,
    createTime: "2021-08-01 10:00:00",
    permissions: ["user", "resource", "content", "statistics", "system"],
  },
  {
    id: "2",
    name: "普通用户",
    code: "user",
    description: "普通用户，只有基本权限",
    status: true,
    createTime: "2021-08-02 11:00:00",
    permissions: ["resource"],
  },
]);

// 角色表单
const roleForm = reactive({
  id: "",
  name: "",
  code: "",
  description: "",
  status: true,
  permissions: [] as string[],
});

const dialogVisible = ref(false);
const dialogTitle = ref("添加角色");
const isEdit = ref(false);

// 模拟权限列表
const permissionOptions = [
  { label: "用户管理", value: "user" },
  { label: "资源管理", value: "resource" },
  { label: "内容管理", value: "content" },
  { label: "统计分析", value: "statistics" },
  { label: "系统设置", value: "system" },
];

// 添加角色
function handleAdd() {
  dialogTitle.value = "添加角色";
  isEdit.value = false;
  roleForm.id = "";
  roleForm.name = "";
  roleForm.code = "";
  roleForm.description = "";
  roleForm.status = true;
  roleForm.permissions = [];
  dialogVisible.value = true;
}

// 编辑角色
function handleEdit(row: any) {
  dialogTitle.value = "编辑角色";
  isEdit.value = true;
  roleForm.id = row.id;
  roleForm.name = row.name;
  roleForm.code = row.code;
  roleForm.description = row.description;
  roleForm.status = row.status;
  roleForm.permissions = [...row.permissions];
  dialogVisible.value = true;
}

// 删除角色
function handleDelete(row: any) {
  ElMessage.success(`删除角色：${row.name}`);
  tableData.value = tableData.value.filter((item) => item.id !== row.id);
}

// 保存角色
function handleSave() {
  ElMessage.success(`保存角色：${JSON.stringify(roleForm)}`);
  dialogVisible.value = false;
  
  // 模拟数据更新
  if (isEdit.value) {
    const index = tableData.value.findIndex((item) => item.id === roleForm.id);
    if (index !== -1) {
      tableData.value[index] = {
        ...tableData.value[index],
        name: roleForm.name,
        code: roleForm.code,
        description: roleForm.description,
        status: roleForm.status,
        permissions: [...roleForm.permissions],
      };
    }
  } else {
    // 添加角色
    tableData.value.push({
      id: String(tableData.value.length + 1),
      name: roleForm.name,
      code: roleForm.code,
      description: roleForm.description,
      status: roleForm.status,
      permissions: [...roleForm.permissions],
      createTime: new Date().toLocaleString(),
    });
  }
}
</script>

<template>
  <Page description="管理系统角色及权限" title="用户角色">
    <ElCard>
      <template #header>
        <div class="flex items-center">
          <span class="flex-auto">角色列表</span>
          <ElButton type="primary" @click="handleAdd">添加角色</ElButton>
        </div>
      </template>
      <ElTable :data="tableData" stripe style="width: 100%">
        <ElTableColumn prop="name" label="角色名称" />
        <ElTableColumn prop="code" label="角色标识" />
        <ElTableColumn prop="description" label="描述" />
        <ElTableColumn label="状态">
          <template #default="{ row }">
            <ElSwitch v-model="row.status" disabled />
          </template>
        </ElTableColumn>
        <ElTableColumn label="权限">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-1">
              <ElButton 
                v-for="perm in row.permissions" 
                :key="perm" 
                size="small" 
                type="info"
                plain
              >
                {{ permissionOptions.find(p => p.value === perm)?.label }}
              </ElButton>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createTime" label="创建时间" />
        <ElTableColumn label="操作" width="200">
          <template #default="{ row }">
            <ElButton 
              type="primary" 
              size="small" 
              @click="handleEdit(row)"
            >
              编辑
            </ElButton>
            <ElPopconfirm
              title="确认删除该角色吗？"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <ElButton type="danger" size="small">删除</ElButton>
              </template>
            </ElPopconfirm>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <ElDialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <ElForm label-width="80px">
        <ElFormItem label="角色名称">
          <ElInput v-model="roleForm.name" placeholder="请输入角色名称" />
        </ElFormItem>
        <ElFormItem label="角色标识">
          <ElInput v-model="roleForm.code" placeholder="请输入角色标识" />
        </ElFormItem>
        <ElFormItem label="描述">
          <ElInput 
            v-model="roleForm.description" 
            type="textarea" 
            placeholder="请输入角色描述"
          />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSwitch v-model="roleForm.status" />
        </ElFormItem>
        <ElFormItem label="权限">
          <ElCheckboxGroup v-model="roleForm.permissions">
            <ElCheckbox 
              v-for="option in permissionOptions" 
              :key="option.value" 
              :label="option.value"
            >
              {{ option.label }}
            </ElCheckbox>
          </ElCheckboxGroup>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSave">确认</ElButton>
      </template>
    </ElDialog>
  </Page>
</template> 