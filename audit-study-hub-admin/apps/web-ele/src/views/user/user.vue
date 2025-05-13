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
  ElSelect,
  ElOption,
  ElImage,
  ElTag,
  ElAvatar
} from "element-plus";
import type { FormInstance, FormRules } from "element-plus";

import { useVbenForm } from "#/adapter/form";
import { 
  MOCK_USERS, 
  MOCK_ROLES,
  MOCK_COLLEGE_DATA, 
  USER_STATUS_OPTIONS, 
  IS_ADMIN_OPTIONS,
  getUserRoleNames
} from "./user-data";
import type { User } from "./user-data";

// 用户数据
const tableData = ref(MOCK_USERS);

// 搜索表单
const searchForm = reactive({
  username: "",
  nickname: "",
  status: "",
  college_id: "",
  is_admin: "",
});

// 用户表单
const userForm = reactive({
  id: 0,
  username: "",
  nickname: "",
  student_id: "",
  password: "",
  mobile: "",
  email: "",
  avatar: "",
  college_id: 0,
  major_id: 0,
  is_admin: 0,
  status: 1,
  roles: [] as number[],
});

const dialogVisible = ref(false);
const dialogTitle = ref("添加用户");
const isEdit = ref(false);
const userFormRef = ref<FormInstance>();

// 用户表单验证规则
const userFormRules = reactive<FormRules>({
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 2, max: 20, message: "长度在2到20个字符之间", trigger: "blur" },
  ],
  nickname: [
    { required: true, message: "请输入昵称", trigger: "blur" },
  ],
  student_id: [
    { required: true, message: "请输入学号", trigger: "blur" },
  ],
  password: [
    { required: !isEdit.value, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在6到20个字符之间", trigger: "blur" },
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
  ],
  mobile: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号格式", trigger: "blur" },
  ],
  college_id: [
    { required: true, message: "请选择学院", trigger: "change" },
  ],
});

// 搜索用户
function handleSearch() {
  // 过滤条件
  const filters: Record<string, any> = {};
  
  if (searchForm.username) {
    filters.username = searchForm.username;
  }
  
  if (searchForm.nickname) {
    filters.nickname = searchForm.nickname;
  }
  
  if (searchForm.status !== "") {
    filters.status = Number(searchForm.status);
  }
  
  if (searchForm.college_id) {
    filters.college_id = Number(searchForm.college_id);
  }
  
  if (searchForm.is_admin !== "") {
    filters.is_admin = Number(searchForm.is_admin);
  }
  
  // 过滤用户数据
  tableData.value = MOCK_USERS.filter((user) => {
    let match = true;
    
    for (const key in filters) {
      if (user[key as keyof User] !== filters[key]) {
        match = false;
        break;
      }
    }
    
    return match;
  });
  
  ElMessage.success("搜索完成");
}

// 重置搜索
function handleReset() {
  searchForm.username = "";
  searchForm.nickname = "";
  searchForm.status = "";
  searchForm.college_id = "";
  searchForm.is_admin = "";
  tableData.value = MOCK_USERS;
}

// 添加用户
function handleAdd() {
  dialogTitle.value = "添加用户";
  isEdit.value = false;
  userForm.id = 0;
  userForm.username = "";
  userForm.nickname = "";
  userForm.student_id = "";
  userForm.password = "";
  userForm.email = "";
  userForm.mobile = "";
  userForm.avatar = "https://big-event20040810.oss-cn-beijing.aliyuncs.com/avatar/default.png";
  userForm.college_id = 0;
  userForm.major_id = 0;
  userForm.is_admin = 0;
  userForm.status = 1;
  userForm.roles = [];
  dialogVisible.value = true;
}

// 编辑用户
function handleEdit(row: User) {
  dialogTitle.value = "编辑用户";
  isEdit.value = true;
  userForm.id = row.id;
  userForm.username = row.username;
  userForm.nickname = row.nickname;
  userForm.student_id = row.student_id;
  userForm.password = "";
  userForm.email = row.email;
  userForm.mobile = row.mobile;
  userForm.avatar = row.avatar;
  userForm.college_id = row.college_id;
  userForm.major_id = row.major_id;
  userForm.is_admin = row.is_admin;
  userForm.status = row.status;
  userForm.roles = [];
  dialogVisible.value = true;
}

// 删除用户
function handleDelete(row: User) {
  ElMessage.success(`删除用户：${row.username}`);
  tableData.value = tableData.value.filter((item) => item.id !== row.id);
}

// 保存用户
function handleSave() {
  if (!userFormRef.value) return;

  userFormRef.value.validate((valid) => {
    if (valid) {
      if (isEdit.value) {
        // 编辑模式
        const index = tableData.value.findIndex((item) => item.id === userForm.id);
        if (index !== -1) {
          // 获取学院名称
          const college = MOCK_COLLEGE_DATA.find(c => c.id === userForm.college_id);
          tableData.value[index] = {
            ...tableData.value[index],
            username: userForm.username,
            nickname: userForm.nickname,
            student_id: userForm.student_id,
            email: userForm.email,
            mobile: userForm.mobile,
            avatar: userForm.avatar,
            college_id: userForm.college_id,
            college_name: college?.name || tableData.value[index].college_name,
            major_id: userForm.major_id,
            is_admin: userForm.is_admin,
            status: userForm.status,
            update_time: new Date().toISOString().replace('T', ' ').substring(0, 19)
          };
          ElMessage.success("更新成功");
        }
      } else {
        // 添加用户
        const newId = Math.max(...tableData.value.map((item) => item.id)) + 1;
        const college = MOCK_COLLEGE_DATA.find(c => c.id === userForm.college_id);
        const now = new Date().toISOString().replace('T', ' ').substring(0, 19);
        tableData.value.push({
          id: newId,
          username: userForm.username,
          nickname: userForm.nickname,
          student_id: userForm.student_id,
          mobile: userForm.mobile,
          email: userForm.email,
          avatar: userForm.avatar,
          college_id: userForm.college_id,
          college_name: college?.name || "未知学院",
          major_id: userForm.major_id,
          major_name: "未知专业",
          is_admin: userForm.is_admin,
          status: userForm.status,
          create_time: now,
          update_time: now,
          is_deleted: 0
        });
        ElMessage.success("添加成功");
      }
      dialogVisible.value = false;
    }
  });
}

// 使用 useVbenForm 创建搜索表单
const [SearchForm] = useVbenForm({
  layout: "inline",
  schema: [
    {
      component: "Input",
      fieldName: "username",
      label: "用户名",
      componentProps: {
        placeholder: "请输入用户名",
      },
    },
    {
      component: "Input",
      fieldName: "nickname",
      label: "昵称",
      componentProps: {
        placeholder: "请输入昵称",
      },
    },
    {
      component: "Select",
      fieldName: "status",
      label: "状态",
      componentProps: {
        placeholder: "请选择状态",
        options: USER_STATUS_OPTIONS,
      },
    },
    {
      component: "Select",
      fieldName: "is_admin",
      label: "管理员",
      componentProps: {
        placeholder: "请选择类型",
        options: IS_ADMIN_OPTIONS,
      },
    },
    {
      component: "Select",
      fieldName: "college_id",
      label: "学院",
      componentProps: {
        placeholder: "请选择学院",
        options: MOCK_COLLEGE_DATA.map(college => ({
          label: college.name,
          value: college.id
        })),
      },
    },
  ],
});

// 格式化状态
function formatStatus(status: number): string {
  return USER_STATUS_OPTIONS.find(item => item.value === status)?.label || String(status);
}

// 格式化管理员
function formatIsAdmin(isAdmin: number): string {
  return IS_ADMIN_OPTIONS.find(item => item.value === isAdmin)?.label || String(isAdmin);
}

// 获取用户角色
function getUserRoles(userId: number): string {
  return getUserRoleNames(userId).join(', ') || '无角色';
}
</script>

<template>
  <Page title="用户管理" description="管理系统用户信息">
    <ElCard>
      <template #header>
        <div class="flex items-center">
          <span class="flex-auto">用户搜索</span>
          <ElButton type="primary" @click="handleSearch">搜索</ElButton>
          <ElButton @click="handleReset">重置</ElButton>
        </div>
      </template>
      <SearchForm v-model:formData="searchForm" />
    </ElCard>
    
    <ElCard class="mt-4">
      <template #header>
        <div class="flex items-center">
          <span class="flex-auto">用户列表</span>
          <ElButton type="primary" @click="handleAdd">添加用户</ElButton>
        </div>
      </template>
      <ElTable :data="tableData" stripe style="width: 100%">
        <ElTableColumn prop="id" label="ID" width="60" />
        <ElTableColumn label="头像" width="80">
          <template #default="{ row }">
            <ElAvatar :src="row.avatar" :size="40" />
          </template>
        </ElTableColumn>
        <ElTableColumn prop="username" label="用户名" min-width="100" />
        <ElTableColumn prop="nickname" label="昵称" min-width="100" />
        <ElTableColumn prop="student_id" label="学号" min-width="100" />
        <ElTableColumn prop="email" label="邮箱" min-width="160" />
        <ElTableColumn prop="mobile" label="手机号" min-width="120" />
        <ElTableColumn prop="college_name" label="学院" min-width="180" />
        <ElTableColumn label="角色">
          <template #default="{ row }">
            <span>{{ getUserRoles(row.id) }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn label="管理员" width="80">
          <template #default="{ row }">
            <ElTag :type="row.is_admin === 1 ? 'danger' : 'info'">
              {{ formatIsAdmin(row.is_admin) }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="状态" width="80">
          <template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'danger'">
              {{ formatStatus(row.status) }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="create_time" label="创建时间" min-width="160" />
        <ElTableColumn label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <ElButton
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </ElButton>
            <ElPopconfirm
              title="确认删除该用户吗？"
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
      width="600px"
      destroy-on-close
    >
      <ElForm
        ref="userFormRef"
        :model="userForm"
        label-width="100px"
        :rules="userFormRules"
      >
        <ElFormItem label="用户名" prop="username">
          <ElInput v-model="userForm.username" placeholder="请输入用户名" />
        </ElFormItem>
        <ElFormItem label="昵称" prop="nickname">
          <ElInput v-model="userForm.nickname" placeholder="请输入昵称" />
        </ElFormItem>
        <ElFormItem label="学号" prop="student_id">
          <ElInput v-model="userForm.student_id" placeholder="请输入学号" />
        </ElFormItem>
        <ElFormItem label="密码" prop="password" v-if="!isEdit">
          <ElInput
            v-model="userForm.password"
            type="password"
            placeholder="请输入密码"
          />
        </ElFormItem>
        <ElFormItem label="头像">
          <ElInput v-model="userForm.avatar" placeholder="请输入头像URL" />
          <div v-if="userForm.avatar" class="mt-2">
            <ElImage :src="userForm.avatar" style="width: 100px; height: 100px" />
          </div>
        </ElFormItem>
        <ElFormItem label="邮箱" prop="email">
          <ElInput v-model="userForm.email" placeholder="请输入邮箱" />
        </ElFormItem>
        <ElFormItem label="手机号" prop="mobile">
          <ElInput v-model="userForm.mobile" placeholder="请输入手机号" />
        </ElFormItem>
        <ElFormItem label="学院" prop="college_id">
          <ElSelect v-model="userForm.college_id" placeholder="请选择学院" style="width: 100%">
            <ElOption
              v-for="item in MOCK_COLLEGE_DATA"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="管理员">
          <ElSelect v-model="userForm.is_admin" placeholder="请选择是否管理员" style="width: 100%">
            <ElOption
              v-for="item in IS_ADMIN_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="userForm.status" placeholder="请选择状态" style="width: 100%">
            <ElOption
              v-for="item in USER_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSave">确认</ElButton>
      </template>
    </ElDialog>
  </Page>
</template>

<style scoped>
.mt-2 {
  margin-top: 8px;
}

.mt-4 {
  margin-top: 16px;
}
</style> 