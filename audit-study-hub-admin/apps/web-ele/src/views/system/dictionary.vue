<template>
  <Page title="数据字典管理" description="管理系统中的数据字典配置">
    <div class="dictionary-container">
      <div class="dictionary-content">
        <div class="dict-left">
          <ElCard class="dict-list-card" shadow="never">
            <template #header>
              <div class="dict-card-header">
                <span>字典列表</span>
                <div class="header-actions">
                  <ElInput
                    v-model="searchKeyword"
                    placeholder="搜索字典名称/编码"
                    clearable
                    @clear="handleSearchDictionary"
                    @keyup.enter="handleSearchDictionary"
                  >
                    <template #append>
                      <ElButton @click="handleSearchDictionary">
                        <ElIcon><Search /></ElIcon>
                      </ElButton>
                    </template>
                  </ElInput>
                  <ElButton type="primary" @click="handleAddDictionary">
                    <ElIcon><Plus /></ElIcon>新增字典
                  </ElButton>
                </div>
              </div>
            </template>
            
            <ElTable
              ref="dictTable"
              :data="filteredDictionaries"
              style="width: 100%"
              v-loading="loading"
              highlight-current-row
              @row-click="handleDictionarySelect"
              :row-class-name="rowClassName"
            >
              <ElTableColumn prop="name" label="字典名称" show-overflow-tooltip min-width="120" />
              <ElTableColumn prop="code" label="字典编码" show-overflow-tooltip min-width="120" />
              <ElTableColumn prop="status" label="字典状态" width="100">
                <template #default="{ row }">
                  <ElTag :type="getDictStatusTag(row.status).type" effect="plain">
                    {{ getDictStatusTag(row.status).label }}
                  </ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <ElButton 
                    type="primary" 
                    link 
                    @click.stop="handleEditDictionary(row)"
                  >
                    编辑
                  </ElButton>
                  <ElButton 
                    type="success" 
                    link 
                    @click.stop="handleDictionarySelect(row)"
                  >
                    字典项
                  </ElButton>
                  <ElButton 
                    type="danger" 
                    link 
                    :disabled="row.is_built_in === 1"
                    @click.stop="handleDeleteDictionary(row)"
                  >
                    删除
                  </ElButton>
                </template>
              </ElTableColumn>
            </ElTable>
          </ElCard>
        </div>
        
        <div class="dict-right">
          <ElCard shadow="never" class="dict-item-card">
            <template #header>
              <div class="dict-card-header">
                <span>
                  {{ currentDictionary ? `字典数据项 - ${currentDictionary.name}` : '字典数据项' }}
                </span>
                <div class="header-actions" v-if="currentDictionary">
                  <ElInput
                    v-model="searchItemKeyword"
                    placeholder="搜索标签/值"
                    clearable
                    @clear="handleSearchDictItem"
                    @keyup.enter="handleSearchDictItem"
                    style="width: 220px"
                  >
                    <template #append>
                      <ElButton @click="handleSearchDictItem">
                        <ElIcon><Search /></ElIcon>
                      </ElButton>
                    </template>
                  </ElInput>
                  <ElButton type="primary" @click="handleAddDictItem">
                    <ElIcon><Plus /></ElIcon>新增
                  </ElButton>
                  <ElButton type="success" @click="handleRefreshCache">
                    <ElIcon><Refresh /></ElIcon>刷新缓存
                  </ElButton>
                  <ElButton @click="handleExportDict">
                    <ElIcon><Download /></ElIcon>导出
                  </ElButton>
                </div>
              </div>
            </template>
            
            <div v-if="!currentDictionary" class="empty-dict-item">
              <ElEmpty description="请先选择左侧的字典" />
            </div>
            
            <ElTable
              v-else
              ref="dictItemTable"
              :data="filteredDictItems"
              style="width: 100%"
              v-loading="itemLoading"
            >
              <ElTableColumn type="index" width="50" label="#" />
              <ElTableColumn prop="label" label="字典标签" min-width="120" show-overflow-tooltip />
              <ElTableColumn prop="value" label="字典键值" min-width="120" show-overflow-tooltip />
              <ElTableColumn prop="tag_type" label="标签样式" width="120">
                <template #default="{ row }">
                  <ElTag :type="row.tag_type || 'info'" effect="plain">
                    {{ row.tag_type ? row.tag_type : '默认' }}
                  </ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <ElTag :type="row.status === 1 ? 'success' : 'danger'" effect="plain">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn prop="sort" label="排序" width="80" />
              <ElTableColumn prop="description" label="描述" min-width="120" show-overflow-tooltip />
              <ElTableColumn label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <ElButton 
                    type="primary" 
                    link 
                    @click="handleEditDictItem(row)"
                  >
                    编辑
                  </ElButton>
                  <ElButton 
                    type="danger" 
                    link 
                    @click="handleDeleteDictItem(row)"
                  >
                    删除
                  </ElButton>
                </template>
              </ElTableColumn>
            </ElTable>
          </ElCard>
        </div>
      </div>
    </div>
    
    <!-- 字典表单对话框 -->
    <ElDialog
      v-model="dictionaryDialogVisible"
      :title="isEdit ? '编辑字典' : '新增字典'"
      width="580px"
      append-to-body
      destroy-on-close
    >
      <ElForm
        ref="dictFormRef"
        :model="dictionaryForm"
        :rules="dictionaryRules"
        label-width="100px"
      >
        <ElFormItem label="字典名称" prop="name">
          <ElInput v-model="dictionaryForm.name" placeholder="请输入字典名称" />
        </ElFormItem>
        <ElFormItem label="字典编码" prop="code">
          <ElInput 
            v-model="dictionaryForm.code" 
            placeholder="请输入字典编码"
            :disabled="isEdit && dictionaryForm.is_built_in === 1"
          />
        </ElFormItem>
        <ElFormItem label="字典状态" prop="status">
          <ElRadioGroup v-model="dictionaryForm.status">
            <ElRadio :label="1">启用</ElRadio>
            <ElRadio :label="0">禁用</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="显示顺序" prop="sort">
          <ElInputNumber
            v-model="dictionaryForm.sort"
            :min="0"
            :max="999"
            style="width: 120px"
          />
        </ElFormItem>
        <ElFormItem label="字典描述" prop="description">
          <ElInput
            v-model="dictionaryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入字典描述"
          />
        </ElFormItem>
        <ElFormItem label="备注" prop="remark">
          <ElInput
            v-model="dictionaryForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="dictionaryDialogVisible = false">取 消</ElButton>
          <ElButton type="primary" @click="submitDictionaryForm">确 定</ElButton>
        </div>
      </template>
    </ElDialog>
    
    <!-- 字典项表单对话框 -->
    <ElDialog
      v-model="dictItemDialogVisible"
      :title="isItemEdit ? '编辑字典项' : '新增字典项'"
      width="580px"
      append-to-body
      destroy-on-close
    >
      <ElForm
        ref="dictItemFormRef"
        :model="dictItemForm"
        :rules="dictItemRules"
        label-width="100px"
      >
        <ElFormItem label="字典类型" prop="dict_id">
          <ElInput 
            :value="currentDictionary?.name" 
            disabled 
            placeholder="字典类型"
          />
        </ElFormItem>
        <ElFormItem label="数据标签" prop="label">
          <ElInput 
            v-model="dictItemForm.label" 
            placeholder="请输入数据标签"
          />
        </ElFormItem>
        <ElFormItem label="数据键值" prop="value">
          <ElInput 
            v-model="dictItemForm.value" 
            placeholder="请输入数据键值"
          />
        </ElFormItem>
        <ElFormItem label="标签样式" prop="tag_type">
          <ElSelect 
            v-model="dictItemForm.tag_type" 
            placeholder="请选择标签样式" 
            clearable
          >
            <ElOption
              v-for="item in TAG_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <ElTag :type="item.value" effect="plain">{{ item.label }}</ElTag>
            </ElOption>
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="显示顺序" prop="sort">
          <ElInputNumber 
            v-model="dictItemForm.sort" 
            :min="0" 
            :max="999" 
            style="width: 120px"
          />
        </ElFormItem>
        <ElFormItem label="状态" prop="status">
          <ElRadioGroup v-model="dictItemForm.status">
            <ElRadio :label="1">启用</ElRadio>
            <ElRadio :label="0">禁用</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="默认选中" prop="default_selected">
          <ElRadioGroup v-model="dictItemForm.default_selected">
            <ElRadio :label="1">是</ElRadio>
            <ElRadio :label="0">否</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="图标" prop="icon">
          <ElInput 
            v-model="dictItemForm.icon" 
            placeholder="请输入图标名称" 
            clearable
          >
            <template #append>
              <ElButton>选择</ElButton>
            </template>
          </ElInput>
        </ElFormItem>
        <ElFormItem label="附加样式" prop="css_class">
          <ElInput 
            v-model="dictItemForm.css_class" 
            placeholder="请输入附加样式" 
            clearable
          />
        </ElFormItem>
        <ElFormItem label="列表样式" prop="list_class">
          <ElInput 
            v-model="dictItemForm.list_class" 
            placeholder="请输入列表样式" 
            clearable
          />
        </ElFormItem>
        <ElFormItem label="描述" prop="description">
          <ElInput
            v-model="dictItemForm.description"
            type="textarea"
            :rows="2"
            placeholder="请输入描述内容"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="dictItemDialogVisible = false">取 消</ElButton>
          <ElButton type="primary" @click="submitDictItemForm">确 定</ElButton>
        </div>
      </template>
    </ElDialog>
  </Page>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from "vue";
import { Page } from "@vben/common-ui";
import {
  ElAlert,
  ElButton,
  ElCard,
  ElDialog,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElRadio,
  ElRadioGroup,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag,
} from "element-plus";
import { Plus, Search, Refresh, Download } from "@element-plus/icons-vue";
import type { FormInstance, FormRules } from "element-plus";
import type { Dictionary, DictionaryItem } from "./dictionary-data";
import {
  MOCK_DICTIONARIES,
  MOCK_DICTIONARY_ITEMS,
  TAG_TYPE_OPTIONS,
  getDictItemsById,
  getDictStatusTag,
} from "./dictionary-data";

// 字典管理相关
const loading = ref(false);
const searchKeyword = ref("");
const dictionaries = ref<Dictionary[]>([...MOCK_DICTIONARIES]);
const filteredDictionaries = ref<Dictionary[]>([...MOCK_DICTIONARIES]);
const currentDictionary = ref<Dictionary | null>(null);
const dictionaryDialogVisible = ref(false);
const isEdit = ref(false);
const dictFormRef = ref<FormInstance>();

// 字典项管理相关
const itemLoading = ref(false);
const searchItemKeyword = ref("");
const dictItems = ref<DictionaryItem[]>([]);
const filteredDictItems = ref<DictionaryItem[]>([]);
const dictItemDialogVisible = ref(false);
const isItemEdit = ref(false);
const dictItemFormRef = ref<FormInstance>();

// 字典表单数据
const dictionaryForm = ref<Partial<Dictionary>>({
  name: "",
  code: "",
  status: 1,
  description: "",
  sort: 0,
  remark: "",
  is_built_in: 0,
});

// 字典项表单数据
const dictItemForm = ref<Partial<DictionaryItem>>({
  dict_id: 0,
  label: "",
  value: "",
  status: 1,
  tag_type: "",
  sort: 0,
  default_selected: 0,
  icon: "",
  css_class: "",
  list_class: "",
  description: "",
});

// 校验字典编码是否重复 - 移到表单规则定义之前
const checkDictCodeUnique = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error("请输入字典编码"));
    return;
  }
  
  // 判断编码是否重复
  const isEdit = dictionaryForm.value.id !== undefined;
  const isDuplicate = dictionaries.value.some(
    dict => dict.code === value && (!isEdit || dict.id !== dictionaryForm.value.id)
  );
  
  if (isDuplicate) {
    callback(new Error("字典编码已存在"));
  } else {
    callback();
  }
};

// 校验字典项键值是否重复 - 移到表单规则定义之前
const checkDictItemValueUnique = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error("请输入字典键值"));
    return;
  }
  
  // 判断键值是否重复
  const isEdit = dictItemForm.value.id !== undefined;
  const isDuplicate = dictItems.value.some(
    item => item.value === value && item.dict_id === dictItemForm.value.dict_id && (!isEdit || item.id !== dictItemForm.value.id)
  );
  
  if (isDuplicate) {
    callback(new Error("字典键值已存在"));
  } else {
    callback();
  }
};

// 字典表单校验规则
const dictionaryRules = reactive<FormRules>({
  name: [
    { required: true, message: "请输入字典名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在2到50个字符之间", trigger: "blur" },
  ],
  code: [
    { required: true, message: "请输入字典编码", trigger: "blur" },
    { pattern: /^[a-z][a-z0-9_]*$/, message: "编码必须以小写字母开头，只能包含小写字母、数字和下划线", trigger: "blur" },
    { validator: checkDictCodeUnique, trigger: "blur" },
  ],
  status: [
    { required: true, message: "请选择字典状态", trigger: "change" },
  ],
  sort: [
    { required: true, message: "请输入显示顺序", trigger: "blur" },
  ],
});

// 字典项表单校验规则
const dictItemRules = reactive<FormRules>({
  label: [
    { required: true, message: "请输入数据标签", trigger: "blur" },
    { min: 1, max: 50, message: "长度在1到50个字符之间", trigger: "blur" },
  ],
  value: [
    { required: true, message: "请输入数据键值", trigger: "blur" },
    { min: 1, max: 100, message: "长度在1到100个字符之间", trigger: "blur" },
    { validator: checkDictItemValueUnique, trigger: "blur" },
  ],
  sort: [
    { required: true, message: "请输入显示顺序", trigger: "blur" },
  ],
  status: [
    { required: true, message: "请选择状态", trigger: "change" },
  ],
});

// 初始化数据
onMounted(() => {
  loadDictionaries();
});

// 加载字典列表
const loadDictionaries = () => {
  loading.value = true;
  // 模拟异步加载
  setTimeout(() => {
    dictionaries.value = [...MOCK_DICTIONARIES];
    filterDictionaries();
    loading.value = false;
  }, 500);
};

// 过滤字典列表
const filterDictionaries = () => {
  if (!searchKeyword.value) {
    filteredDictionaries.value = [...dictionaries.value];
    return;
  }
  
  const keyword = searchKeyword.value.toLowerCase();
  filteredDictionaries.value = dictionaries.value.filter(
    dict => 
      dict.name.toLowerCase().includes(keyword) ||
      dict.code.toLowerCase().includes(keyword) ||
      (dict.description && dict.description.toLowerCase().includes(keyword))
  );
};

// 加载字典项列表
const loadDictItems = (dictId: number) => {
  itemLoading.value = true;
  // 模拟异步加载
  setTimeout(() => {
    dictItems.value = getDictItemsById(dictId);
    filterDictItems();
    itemLoading.value = false;
  }, 300);
};

// 过滤字典项列表
const filterDictItems = () => {
  if (!searchItemKeyword.value) {
    filteredDictItems.value = [...dictItems.value];
    return;
  }
  
  const keyword = searchItemKeyword.value.toLowerCase();
  filteredDictItems.value = dictItems.value.filter(
    item => 
      item.label.toLowerCase().includes(keyword) ||
      item.value.toLowerCase().includes(keyword) ||
      (item.description && item.description.toLowerCase().includes(keyword))
  );
};

// 处理字典搜索
const handleSearchDictionary = () => {
  filterDictionaries();
};

// 处理字典项搜索
const handleSearchDictItem = () => {
  filterDictItems();
};

// 处理选择字典
const handleDictionarySelect = (row: Dictionary) => {
  currentDictionary.value = row;
  loadDictItems(row.id);
};

// 行样式处理
const rowClassName = ({ row }: { row: Dictionary }) => {
  return currentDictionary.value && row.id === currentDictionary.value.id
    ? "current-row"
    : "";
};

// 处理添加字典
const handleAddDictionary = () => {
  isEdit.value = false;
  dictionaryForm.value = {
    name: "",
    code: "",
    status: 1,
    description: "",
    sort: 0,
    remark: "",
    is_built_in: 0,
  };
  dictionaryDialogVisible.value = true;
};

// 处理编辑字典
const handleEditDictionary = (row: Dictionary) => {
  isEdit.value = true;
  dictionaryForm.value = {
    ...row,
  };
  dictionaryDialogVisible.value = true;
};

// 处理删除字典
const handleDeleteDictionary = (row: Dictionary) => {
  if (row.is_built_in === 1) {
    ElMessage.warning("内置字典不能删除");
    return;
  }
  
  ElMessageBox.confirm(
    `确定要删除字典"${row.name}"吗？`,
    "警告",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  )
    .then(() => {
      // 模拟删除操作
      const index = dictionaries.value.findIndex(item => item.id === row.id);
      if (index !== -1) {
        dictionaries.value.splice(index, 1);
        filterDictionaries();
        
        // 如果删除的是当前选中的字典，则清空选中状态
        if (currentDictionary.value && currentDictionary.value.id === row.id) {
          currentDictionary.value = null;
          dictItems.value = [];
          filteredDictItems.value = [];
        }
        
        ElMessage.success("删除成功");
      }
    })
    .catch(() => {
      // 取消删除
    });
};

// 提交字典表单
const submitDictionaryForm = async () => {
  if (!dictFormRef.value) return;
  
  try {
    await dictFormRef.value.validate();
    
    if (isEdit.value) {
      // 编辑字典
      const index = dictionaries.value.findIndex(item => item.id === dictionaryForm.value.id);
      if (index !== -1) {
        dictionaries.value[index] = {
          ...dictionaries.value[index],
          ...dictionaryForm.value,
          update_time: new Date().toLocaleString(),
          update_by: "当前用户",
        } as Dictionary;
        
        // 如果修改的是当前选中的字典，则更新当前选中的字典
        if (currentDictionary.value && currentDictionary.value.id === dictionaryForm.value.id) {
          currentDictionary.value = dictionaries.value[index];
        }
        
        ElMessage.success("修改成功");
      }
    } else {
      // 新增字典
      const maxId = dictionaries.value.length > 0 
        ? Math.max(...dictionaries.value.map(item => item.id)) 
        : 0;
      
      const newDict: Dictionary = {
        ...dictionaryForm.value,
        id: maxId + 1,
        create_time: new Date().toLocaleString(),
        create_by: "当前用户",
        is_built_in: dictionaryForm.value.is_built_in || 0,
      } as Dictionary;
      
      dictionaries.value.push(newDict);
      ElMessage.success("添加成功");
    }
    
    filterDictionaries();
    dictionaryDialogVisible.value = false;
  } catch (error) {
    // 表单验证失败
    console.error("表单验证失败", error);
  }
};

// 处理添加字典项
const handleAddDictItem = () => {
  if (!currentDictionary.value) {
    ElMessage.warning("请先选择字典");
    return;
  }
  
  isItemEdit.value = false;
  dictItemForm.value = {
    dict_id: currentDictionary.value.id,
    dict_code: currentDictionary.value.code,
    label: "",
    value: "",
    status: 1,
    tag_type: "",
    sort: dictItems.value.length > 0 ? Math.max(...dictItems.value.map(item => item.sort)) + 1 : 1,
    default_selected: 0,
    css_class: "",
    list_class: "",
    icon: "",
    description: "",
  };
  
  dictItemDialogVisible.value = true;
};

// 处理编辑字典项
const handleEditDictItem = (row: DictionaryItem) => {
  isItemEdit.value = true;
  dictItemForm.value = { ...row };
  dictItemDialogVisible.value = true;
};

// 处理删除字典项
const handleDeleteDictItem = (row: DictionaryItem) => {
  ElMessageBox.confirm(
    `确定要删除字典项"${row.label}"吗？`,
    "警告",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  )
    .then(() => {
      // 模拟删除操作
      const index = MOCK_DICTIONARY_ITEMS.findIndex(item => item.id === row.id);
      if (index !== -1) {
        MOCK_DICTIONARY_ITEMS.splice(index, 1);
        
        // 更新当前字典项列表
        if (currentDictionary.value) {
          loadDictItems(currentDictionary.value.id);
        }
        
        ElMessage.success("删除成功");
      }
    })
    .catch(() => {
      // 取消删除
    });
};

// 提交字典项表单
const submitDictItemForm = async () => {
  if (!dictItemFormRef.value) return;
  
  try {
    await dictItemFormRef.value.validate();
    
    if (isItemEdit.value) {
      // 编辑字典项
      const index = MOCK_DICTIONARY_ITEMS.findIndex(item => item.id === dictItemForm.value.id);
      if (index !== -1) {
        MOCK_DICTIONARY_ITEMS[index] = {
          ...MOCK_DICTIONARY_ITEMS[index],
          ...dictItemForm.value,
          update_time: new Date().toLocaleString(),
          update_by: "当前用户",
        } as DictionaryItem;
        
        ElMessage.success("修改成功");
      }
    } else {
      // 新增字典项
      const maxId = MOCK_DICTIONARY_ITEMS.length > 0 
        ? Math.max(...MOCK_DICTIONARY_ITEMS.map(item => item.id)) 
        : 0;
      
      const newDictItem: DictionaryItem = {
        ...dictItemForm.value,
        id: maxId + 1,
        create_time: new Date().toLocaleString(),
        create_by: "当前用户",
      } as DictionaryItem;
      
      MOCK_DICTIONARY_ITEMS.push(newDictItem);
      ElMessage.success("添加成功");
    }
    
    // 更新字典项列表
    if (currentDictionary.value) {
      loadDictItems(currentDictionary.value.id);
    }
    
    dictItemDialogVisible.value = false;
  } catch (error) {
    // 表单验证失败
    console.error("表单验证失败", error);
  }
};

// 处理刷新缓存
const handleRefreshCache = () => {
  ElMessage.success({
    message: "数据字典缓存刷新成功",
    type: "success",
    duration: 1500,
  });
};

// 处理导出字典
const handleExportDict = () => {
  if (!currentDictionary.value) {
    ElMessage.warning("请先选择需要导出的字典");
    return;
  }
  
  // 模拟导出操作
  ElMessage.success({
    message: `字典 ${currentDictionary.value.name} 导出成功`,
    type: "success",
    duration: 1500,
  });
};
</script>

<style scoped>
.dictionary-container {
  height: 100%;
  padding: 0;
}

.dictionary-content {
  display: flex;
  height: calc(100vh - 200px);
}

.dict-left {
  width: 450px;
  padding-right: 16px;
  height: 100%;
  overflow-y: auto;
}

.dict-right {
  flex: 1;
  height: 100%;
  overflow-y: auto;
}

.dict-list-card, .dict-item-card {
  height: 100%;
}

.dict-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.empty-dict-item {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.current-row {
  background-color: #f0f9eb;
}

.dialog-footer {
  text-align: right;
}
</style>