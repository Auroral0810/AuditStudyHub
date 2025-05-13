<template>
  <Page title="站点管理" description="管理系统站点相关设置">
    <div class="site-container">
      <ElTabs v-model="activeTab">
        <ElTabPane label="站点列表" name="site-list">
          <div class="toolbar">
            <ElButton type="primary" @click="handleAddSite">添加站点</ElButton>
          </div>

          <ElTable 
            :data="siteList" 
            style="width: 100%" 
            border
            row-key="id"
          >
            <ElTableColumn prop="id" label="ID" width="80" />
            <ElTableColumn prop="name" label="站点名称" min-width="120" />
            <ElTableColumn prop="code" label="站点编码" min-width="120" />
            <ElTableColumn prop="domain" label="域名" min-width="180" />
            <ElTableColumn prop="description" label="描述" min-width="200" />
            <ElTableColumn prop="status" label="状态" width="100">
              <template #default="{ row }">
                <ElTag :type="getSiteStatusTag(row.status).type">
                  {{ getSiteStatusTag(row.status).label }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="sort" label="排序" width="80" />
            <ElTableColumn prop="create_time" label="创建时间" width="180" />
            <ElTableColumn label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <ElButton type="primary" size="small" @click="handleEditSite(row)">
                  编辑
                </ElButton>
                <ElButton type="danger" size="small" @click="handleDeleteSite(row)">
                  删除
                </ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElTabPane>

        <ElTabPane label="系统设置" name="system-settings">
          <ElCard>
            <template #header>
              <div class="card-header">
                <span>基本设置</span>
              </div>
            </template>

            <ElForm
              ref="siteSettingForm"
              :model="siteSettings"
              label-width="120px"
              :rules="settingsFormRules"
            >
              <ElFormItem label="系统名称" prop="siteName">
                <ElInput v-model="siteSettings.siteName" placeholder="请输入系统名称" />
              </ElFormItem>
              <ElFormItem label="系统描述" prop="siteDescription">
                <ElInput
                  v-model="siteSettings.siteDescription"
                  placeholder="请输入系统描述"
                  type="textarea"
                  :rows="2"
                />
              </ElFormItem>

              <ElDivider>界面设置</ElDivider>

              <ElFormItem label="系统Logo" prop="logo">
                <ElInput v-model="siteSettings.logo" placeholder="请输入Logo URL" />
                <div class="form-tip">建议尺寸: 240×60px, 支持PNG, JPG格式</div>
              </ElFormItem>
              <ElFormItem label="Favicon" prop="favicon">
                <ElInput v-model="siteSettings.favicon" placeholder="请输入Favicon URL" />
                <div class="form-tip">网站图标，建议尺寸: 32×32px, 支持ICO, PNG格式</div>
              </ElFormItem>

              <ElDivider>页脚设置</ElDivider>

              <ElFormItem label="备案号" prop="recordNumber">
                <ElInput v-model="siteSettings.recordNumber" placeholder="请输入备案号" />
              </ElFormItem>
              <ElFormItem label="版权信息" prop="copyright">
                <ElInput v-model="siteSettings.copyright" placeholder="请输入版权信息" />
              </ElFormItem>

              <ElFormItem>
                <ElButton type="primary" @click="saveSettings">保存设置</ElButton>
              </ElFormItem>
            </ElForm>
          </ElCard>
        </ElTabPane>
      </ElTabs>

      <!-- 站点表单对话框 -->
      <ElDialog
        v-model="siteDialogVisible"
        :title="siteDialogTitle"
        width="600px"
        destroy-on-close
      >
        <ElForm
          ref="siteFormRef"
          :model="currentSite"
          label-width="100px"
          :rules="siteFormRules"
        >
          <ElFormItem label="站点名称" prop="name">
            <ElInput v-model="currentSite.name" placeholder="请输入站点名称" />
          </ElFormItem>
          <ElFormItem label="站点编码" prop="code">
            <ElInput v-model="currentSite.code" placeholder="请输入站点编码" />
            <div class="form-tip">唯一标识符，仅支持小写字母、数字和下划线</div>
          </ElFormItem>
          <ElFormItem label="域名" prop="domain">
            <ElInput v-model="currentSite.domain" placeholder="请输入站点域名" />
          </ElFormItem>
          <ElFormItem label="Logo" prop="logo">
            <ElInput v-model="currentSite.logo" placeholder="请输入Logo URL" />
          </ElFormItem>
          <ElFormItem label="描述" prop="description">
            <ElInput
              v-model="currentSite.description"
              type="textarea"
              :rows="3"
              placeholder="请输入站点描述"
            />
          </ElFormItem>
          <ElFormItem label="状态" prop="status">
            <ElSelect v-model="currentSite.status" style="width: 100%">
              <ElOption
                v-for="item in SITE_STATUS_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="排序" prop="sort">
            <ElInputNumber
              v-model="currentSite.sort"
              :min="1"
              :max="999"
              style="width: 100%"
            />
          </ElFormItem>
        </ElForm>
        <template #footer>
          <div class="dialog-footer">
            <ElButton @click="siteDialogVisible = false">取消</ElButton>
            <ElButton type="primary" @click="saveSite">确定</ElButton>
          </div>
        </template>
      </ElDialog>
    </div>
  </Page>
</template>

<script lang="ts" setup>
import { ref, reactive, nextTick } from "vue";
import { Page } from "@vben/common-ui";
import {
  ElTable,
  ElTableColumn,
  ElButton,
  ElTag,
  ElMessage,
  ElMessageBox,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElSelect,
  ElOption,
  ElDialog,
  ElDivider,
  ElAlert,
  ElCard,
  ElTabs,
  ElTabPane,
} from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { MOCK_SITE_DATA, SITE_STATUS_OPTIONS } from "./site-data";
import type { SiteData } from "./site-data";

// 站点数据
const siteList = ref<SiteData[]>(MOCK_SITE_DATA);

// 当前编辑的站点
const currentSite = ref<Partial<SiteData>>({});

// 站点对话框
const siteDialogVisible = ref(false);
const siteDialogTitle = ref("添加站点");
const siteFormRef = ref<FormInstance>();
const isEditMode = ref(false);

// 站点表单验证规则
const siteFormRules = reactive<FormRules>({
  name: [
    { required: true, message: "请输入站点名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在2到50个字符之间", trigger: "blur" },
  ],
  code: [
    { required: true, message: "请输入站点编码", trigger: "blur" },
    { pattern: /^[a-z0-9_]+$/, message: "只能包含小写字母、数字和下划线", trigger: "blur" },
  ],
  domain: [
    { required: true, message: "请输入站点域名", trigger: "blur" },
    { pattern: /^[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$/, message: "请输入正确的域名格式", trigger: "blur" },
  ],
});

// 当前选中的标签页
const activeTab = ref("site-list");

// 系统设置表单
const siteSettingForm = ref<FormInstance>();
const siteSettings = reactive({
  siteName: "审核研究中心",
  siteDescription: "审核研究中心管理平台",
  recordNumber: "京ICP备XXXXXXXX号-1",
  copyright: "© 2023 审核研究中心. All Rights Reserved.",
  logo: "/logo.png",
  favicon: "/favicon.ico",
});

// 系统设置表单验证规则
const settingsFormRules = reactive<FormRules>({
  siteName: [
    { required: true, message: "请输入系统名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在2到50个字符之间", trigger: "blur" },
  ],
});

// 添加站点
const handleAddSite = () => {
  isEditMode.value = false;
  siteDialogTitle.value = "添加站点";
  currentSite.value = {
    name: "",
    code: "",
    domain: "",
    logo: "",
    description: "",
    status: 1,
    sort: 1,
  };
  nextTick(() => {
    siteFormRef.value?.resetFields();
  });
  siteDialogVisible.value = true;
};

// 编辑站点
const handleEditSite = (row: SiteData) => {
  isEditMode.value = true;
  siteDialogTitle.value = "编辑站点";
  currentSite.value = { ...row };
  nextTick(() => {
    siteFormRef.value?.resetFields();
  });
  siteDialogVisible.value = true;
};

// 删除站点
const handleDeleteSite = (row: SiteData) => {
  ElMessageBox.confirm(`确认删除站点 "${row.name}" 吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      // 实际应用中这里会调用API删除站点
      // 这里仅做模拟
      const index = siteList.value.findIndex((item) => item.id === row.id);
      if (index !== -1) {
        siteList.value.splice(index, 1);
        ElMessage.success("删除成功");
      }
    })
    .catch(() => {});
};

// 保存站点
const saveSite = async () => {
  if (!siteFormRef.value) return;

  await siteFormRef.value.validate((valid) => {
    if (valid) {
      // 实际应用中这里会调用API保存站点
      // 这里仅做模拟
      if (isEditMode.value && currentSite.value.id) {
        // 编辑模式
        const index = siteList.value.findIndex(
          (item) => item.id === currentSite.value.id
        );
        if (index !== -1) {
          siteList.value[index] = { ...currentSite.value } as SiteData;
          ElMessage.success("更新成功");
        }
      } else {
        // 新增模式
        const newId = Math.max(...siteList.value.map((item) => item.id)) + 1;
        siteList.value.push({
          ...currentSite.value,
          id: newId,
          create_time: new Date().toISOString(),
        } as SiteData);
        ElMessage.success("添加成功");
      }
      siteDialogVisible.value = false;
    }
  });
};

// 保存系统设置
const saveSettings = async () => {
  if (!siteSettingForm.value) return;

  await siteSettingForm.value.validate((valid) => {
    if (valid) {
      // 实际应用中这里会调用API保存系统设置
      // 这里仅做模拟
      ElMessage.success("保存成功");
    }
  });
};

// 获取站点状态显示
const getSiteStatusTag = (status: number) => {
  const option = SITE_STATUS_OPTIONS.find((item) => item.value === status);
  return {
    label: option ? option.label : String(status),
    type: status === 1 ? "success" : "danger",
  };
};
</script>

<style scoped>
.site-container {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
