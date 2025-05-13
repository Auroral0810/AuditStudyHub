<template>
  <Page title="权限管理" description="管理系统权限和用户角色">
    <div class="permission-container">
      <ElTabs v-model="activeTab">
        <!-- 权限管理标签页 -->
        <ElTabPane label="权限管理" name="permission">
          <div class="toolbar">
            <ElButton type="primary" @click="handleAddPermission">添加权限</ElButton>
          </div>
          <ElTable
            :data="permissionTreeList"
            style="width: 100%;"
            border
            row-key="id"
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          >
            <ElTableColumn prop="name" label="权限名称" min-width="180" />
            <ElTableColumn prop="code" label="权限编码" min-width="180" />
            <ElTableColumn prop="type" label="权限类型" width="120">
              <template #default="{ row }">
                <ElTag
                  :type="row.type === 1 ? 'primary' : row.type === 2 ? 'success' : 'warning'"
                >
                  {{ getPermissionTypeName(row.type) }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="path" label="路径" min-width="180" />
            <ElTableColumn prop="icon" label="图标" width="100">
              <template #default="{ row }">
                <span v-if="row.icon">{{ row.icon }}</span>
                <span v-else>-</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="sort" label="排序" width="80" />
            <ElTableColumn prop="status" label="状态" width="80">
              <template #default="{ row }">
                <ElTag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ getStatusName(row.status) }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="hidden" label="显示" width="80">
              <template #default="{ row }">
                <ElTag :type="row.hidden === 0 ? 'success' : 'info'">
                  {{ getHiddenName(row.hidden) }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <ElButton
                  size="small"
                  type="primary"
                  @click="handleEditPermission(row)"
                >
                  编辑
                </ElButton>
                <ElButton
                  size="small"
                  type="danger"
                  @click="handleDeletePermission(row)"
                >
                  删除
                </ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElTabPane>
        
        <!-- 角色管理标签页 -->
        <ElTabPane label="角色管理" name="role">
          <div class="toolbar">
            <ElButton type="primary" @click="handleAddRole">添加角色</ElButton>
          </div>
          <ElTable :data="roleList" style="width: 100%;" border>
            <ElTableColumn prop="id" label="ID" width="80" />
            <ElTableColumn prop="name" label="角色名称" min-width="150" />
            <ElTableColumn prop="code" label="角色编码" min-width="150" />
            <ElTableColumn prop="description" label="描述" min-width="200" />
            <ElTableColumn prop="status" label="状态" width="100">
              <template #default="{ row }">
                <ElTag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ getStatusName(row.status) }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="create_time" label="创建时间" width="180" />
            <ElTableColumn label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <ElButton
                  size="small"
                  type="primary"
                  @click="handleEditRole(row)"
                >
                  编辑
                </ElButton>
                <ElButton
                  size="small"
                  type="danger"
                  @click="handleDeleteRole(row)"
                >
                  删除
                </ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElTabPane>
      </ElTabs>

      <!-- 权限表单对话框 -->
      <ElDialog
        v-model="permissionDialogVisible"
        :title="permissionDialogTitle"
        width="600px"
        destroy-on-close
      >
        <ElForm
          ref="permissionForm"
          :model="permissionFormData"
          label-width="120px"
          :rules="permissionRules"
        >
          <ElFormItem label="权限类型" prop="type">
            <ElSelect v-model="permissionFormData.type" style="width: 100%">
              <ElOption
                v-for="item in PERMISSION_TYPE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="父级权限" prop="parent_id">
            <ElSelect v-model="permissionFormData.parent_id" style="width: 100%">
              <ElOption :label="'顶级权限'" :value="0" />
              <ElOption
                v-for="item in MOCK_PERMISSION_DATA.filter(p => p.type === 1)"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="权限名称" prop="name">
            <ElInput v-model="permissionFormData.name" placeholder="请输入权限名称" />
          </ElFormItem>
          <ElFormItem label="权限编码" prop="code">
            <ElInput v-model="permissionFormData.code" placeholder="请输入权限编码" />
          </ElFormItem>

          <template v-if="permissionFormData.type === 1">
            <ElFormItem label="路径" prop="path">
              <ElInput v-model="permissionFormData.path" placeholder="请输入路径" />
            </ElFormItem>
            <ElFormItem label="组件" prop="component">
              <ElInput v-model="permissionFormData.component" placeholder="请输入组件路径" />
            </ElFormItem>
            <ElFormItem label="重定向" prop="redirect">
              <ElInput v-model="permissionFormData.redirect" placeholder="请输入重定向路径" />
            </ElFormItem>
            <ElFormItem label="图标" prop="icon">
              <ElInput v-model="permissionFormData.icon" placeholder="请输入图标名称" />
            </ElFormItem>
          </template>

          <ElFormItem label="排序" prop="sort">
            <ElInputNumber v-model="permissionFormData.sort" :min="0" style="width: 100%" />
          </ElFormItem>
          <ElFormItem label="状态" prop="status">
            <ElSelect v-model="permissionFormData.status" style="width: 100%">
              <ElOption
                v-for="item in STATUS_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="是否显示" prop="hidden" v-if="permissionFormData.type === 1">
            <ElSelect v-model="permissionFormData.hidden" style="width: 100%">
              <ElOption
                v-for="item in HIDDEN_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElForm>
        <template #footer>
          <div class="dialog-footer">
            <ElButton @click="permissionDialogVisible = false">取消</ElButton>
            <ElButton type="primary" @click="savePermission">确定</ElButton>
          </div>
        </template>
      </ElDialog>

      <!-- 角色表单对话框 -->
      <ElDialog
        v-model="roleDialogVisible"
        :title="roleDialogTitle"
        width="700px"
        destroy-on-close
      >
        <ElForm
          ref="roleForm"
          :model="roleFormData"
          label-width="120px"
          :rules="roleRules"
        >
          <ElFormItem label="角色名称" prop="name">
            <ElInput v-model="roleFormData.name" placeholder="请输入角色名称" />
          </ElFormItem>
          <ElFormItem label="角色编码" prop="code">
            <ElInput v-model="roleFormData.code" placeholder="请输入角色编码" />
          </ElFormItem>
          <ElFormItem label="角色描述" prop="description">
            <ElInput
              v-model="roleFormData.description"
              type="textarea"
              :rows="3"
              placeholder="请输入角色描述"
            />
          </ElFormItem>
          <ElFormItem label="状态" prop="status">
            <ElSelect v-model="roleFormData.status" style="width: 100%">
              <ElOption
                v-for="item in STATUS_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="权限" prop="permissions">
            <ElTree
              ref="permissionTree"
              :data="permissionTreeData"
              show-checkbox
              node-key="id"
              :default-checked-keys="defaultPermissionKeys"
              @check="permissionTreeChange"
            />
          </ElFormItem>
        </ElForm>
        <template #footer>
          <div class="dialog-footer">
            <ElButton @click="roleDialogVisible = false">取消</ElButton>
            <ElButton type="primary" @click="saveRole">确定</ElButton>
          </div>
        </template>
      </ElDialog>
    </div>
  </Page>
</template>

<script lang="ts" setup>
import { ref, reactive, h, nextTick } from "vue";

import { Page } from "@vben/common-ui";
import {
  ElButton,
  ElTabs,
  ElTabPane,
  ElTable,
  ElTableColumn,
  ElTag,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElSelect,
  ElOption,
  ElInputNumber,
  ElSwitch,
  ElTree,
  ElMessage,
  ElMessageBox,
} from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import type { TreeNodeData } from "element-plus/es/components/tree/src/tree.type";

import {
  MOCK_PERMISSION_DATA,
  MOCK_ROLE_DATA,
  PERMISSION_TYPE_OPTIONS,
  STATUS_OPTIONS,
  HIDDEN_OPTIONS,
} from "./permission-data";
import type { Permission, Role } from "./permission-data";

// 当前选中的标签页
const activeTab = ref("permission");

// 权限相关
const permissionDialogVisible = ref(false);
const permissionDialogTitle = ref("添加权限");
const permissionForm = ref<FormInstance>();
const isEditPermission = ref(false);
const permissionFormData = reactive<Partial<Permission>>({
  name: "",
  code: "",
  type: 1,
  parent_id: 0,
  path: "",
  component: "",
  redirect: "",
  icon: "",
  sort: 1,
  status: 1,
  hidden: 0,
});

// 权限表单验证规则
const permissionRules = reactive<FormRules>({
  name: [
    { required: true, message: "请输入权限名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在2到50个字符之间", trigger: "blur" },
  ],
  code: [
    { required: true, message: "请输入权限编码", trigger: "blur" },
    { pattern: /^[a-z0-9:-]+$/, message: "只能包含小写字母、数字、冒号和连字符", trigger: "blur" },
  ],
  type: [
    { required: true, message: "请选择权限类型", trigger: "change" },
  ],
});

// 角色相关
const roleDialogVisible = ref(false);
const roleDialogTitle = ref("添加角色");
const roleForm = ref<FormInstance>();
const isEditRole = ref(false);
const roleFormData = reactive<Partial<Role>>({
  name: "",
  code: "",
  description: "",
  status: 1,
  permissions: [],
});
const permissionTreeData = ref<TreeNodeData[]>([]);
const defaultPermissionKeys = ref<number[]>([]);

// 角色表单验证规则
const roleRules = reactive<FormRules>({
  name: [
    { required: true, message: "请输入角色名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在2到50个字符之间", trigger: "blur" },
  ],
  code: [
    { required: true, message: "请输入角色编码", trigger: "blur" },
    { pattern: /^[a-z0-9_]+$/, message: "只能包含小写字母、数字和下划线", trigger: "blur" },
  ],
});

// 权限数据（树形结构）
const permissionTreeList = ref<Permission[]>([]);

// 角色数据
const roleList = ref<Role[]>(MOCK_ROLE_DATA);

// 初始加载数据
const loadData = () => {
  // 转换权限数据为树形结构
  permissionTreeList.value = transformPermissionTree(MOCK_PERMISSION_DATA);
  // 转换权限数据为权限树选择器数据
  permissionTreeData.value = generatePermissionTreeData(MOCK_PERMISSION_DATA);
  // 加载角色数据
  roleList.value = MOCK_ROLE_DATA;
};

// 将权限列表转换为树形结构
const transformPermissionTree = (permissions: Permission[]): Permission[] => {
  const map = new Map<number, Permission>();
  const result: Permission[] = [];

  // 创建映射
  permissions.forEach(permission => {
    map.set(permission.id, { ...permission, children: [] });
  });

  // 构建树
  permissions.forEach(permission => {
    const node = map.get(permission.id);
    if (node) {
      if (permission.parent_id === 0) {
        // 根节点
        result.push(node);
      } else {
        // 子节点
        const parent = map.get(permission.parent_id);
        if (parent && !parent.children) {
          parent.children = [];
        }
        if (parent) {
          parent.children?.push(node);
        }
      }
    }
  });

  return result;
};

// 生成权限树选择器数据
const generatePermissionTreeData = (permissions: Permission[]): TreeNodeData[] => {
  // 按parent_id分组
  const groups: Record<number, Permission[]> = {};
  permissions.forEach(permission => {
    if (!groups[permission.parent_id]) {
      groups[permission.parent_id] = [];
    }
    groups[permission.parent_id].push(permission);
  });

  // 递归构建树
  const buildTree = (parentId: number): TreeNodeData[] => {
    const children = groups[parentId] || [];
    return children.map(item => {
      const hasChildren = groups[item.id] && groups[item.id].length > 0;
      return {
        id: item.id,
        label: item.name,
        children: hasChildren ? buildTree(item.id) : [],
        disabled: item.status === 0,
      };
    });
  };

  return buildTree(0);
};

// 获取权限类型名称
const getPermissionTypeName = (type: number): string => {
  const option = PERMISSION_TYPE_OPTIONS.find(item => item.value === type);
  return option ? option.label : String(type);
};

// 获取状态名称
const getStatusName = (status: number): string => {
  const option = STATUS_OPTIONS.find(item => item.value === status);
  return option ? option.label : String(status);
};

// 获取显示状态名称
const getHiddenName = (hidden: number): string => {
  const option = HIDDEN_OPTIONS.find(item => item.value === hidden);
  return option ? option.label : String(hidden);
};

// 添加权限
const handleAddPermission = () => {
  isEditPermission.value = false;
  permissionDialogTitle.value = "添加权限";
  permissionFormData.name = "";
  permissionFormData.code = "";
  permissionFormData.type = 1;
  permissionFormData.parent_id = 0;
  permissionFormData.path = "";
  permissionFormData.component = "";
  permissionFormData.redirect = "";
  permissionFormData.icon = "";
  permissionFormData.sort = 1;
  permissionFormData.status = 1;
  permissionFormData.hidden = 0;
  permissionDialogVisible.value = true;
};

// 编辑权限
const handleEditPermission = (row: Permission) => {
  isEditPermission.value = true;
  permissionDialogTitle.value = "编辑权限";
  permissionFormData.id = row.id;
  permissionFormData.name = row.name;
  permissionFormData.code = row.code;
  permissionFormData.type = row.type;
  permissionFormData.parent_id = row.parent_id;
  permissionFormData.path = row.path;
  permissionFormData.component = row.component;
  permissionFormData.redirect = row.redirect;
  permissionFormData.icon = row.icon;
  permissionFormData.sort = row.sort;
  permissionFormData.status = row.status;
  permissionFormData.hidden = row.hidden;
  permissionDialogVisible.value = true;
};

// 删除权限
const handleDeletePermission = (row: Permission) => {
  ElMessageBox.confirm(`确定要删除权限"${row.name}"吗？此操作将同时删除其所有子权限！`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    // 这里模拟删除操作
    ElMessage.success(`已成功删除权限"${row.name}"`);
    // 重新加载数据
    loadData();
  }).catch(() => {});
};

// 保存权限
const savePermission = async () => {
  if (!permissionForm.value) return;
  
  await permissionForm.value.validate(valid => {
    if (valid) {
      // 这里模拟保存操作
      if (isEditPermission.value) {
        ElMessage.success(`已成功更新权限"${permissionFormData.name}"`);
      } else {
        ElMessage.success(`已成功添加权限"${permissionFormData.name}"`);
      }
      
      // 关闭对话框
      permissionDialogVisible.value = false;
      
      // 重新加载数据
      loadData();
    }
  });
};

// 添加角色
const handleAddRole = () => {
  isEditRole.value = false;
  roleDialogTitle.value = "添加角色";
  roleFormData.name = "";
  roleFormData.code = "";
  roleFormData.description = "";
  roleFormData.status = 1;
  roleFormData.permissions = [];
  defaultPermissionKeys.value = [];
  roleDialogVisible.value = true;
};

// 编辑角色
const handleEditRole = (row: Role) => {
  isEditRole.value = true;
  roleDialogTitle.value = "编辑角色";
  roleFormData.id = row.id;
  roleFormData.name = row.name;
  roleFormData.code = row.code;
  roleFormData.description = row.description;
  roleFormData.status = row.status;
  roleFormData.permissions = row.permissions || [];
  defaultPermissionKeys.value = row.permissions || [];
  roleDialogVisible.value = true;
};

// 删除角色
const handleDeleteRole = (row: Role) => {
  ElMessageBox.confirm(`确定要删除角色"${row.name}"吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    // 这里模拟删除操作
    ElMessage.success(`已成功删除角色"${row.name}"`);
    // 重新加载数据
    loadData();
  }).catch(() => {});
};

// 保存角色
const saveRole = async () => {
  if (!roleForm.value) return;
  
  await roleForm.value.validate(valid => {
    if (valid) {
      // 这里模拟保存操作
      if (isEditRole.value) {
        ElMessage.success(`已成功更新角色"${roleFormData.name}"`);
      } else {
        ElMessage.success(`已成功添加角色"${roleFormData.name}"`);
      }
      
      // 关闭对话框
      roleDialogVisible.value = false;
      
      // 重新加载数据
      loadData();
    }
  });
};

// 权限树选择变化
const permissionTreeChange = (keys: number[]) => {
  roleFormData.permissions = keys;
};

// 初始化
loadData();
</script>

<style scoped>
.permission-container {
  padding: 10px;
  border-radius: 4px;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
:deep(.el-tree) {
  max-height: 400px;
  overflow-y: auto;
}
</style>
