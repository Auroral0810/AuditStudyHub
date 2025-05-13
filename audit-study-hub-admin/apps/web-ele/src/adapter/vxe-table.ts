import { h, ref } from "vue";

import { setupVbenVxeTable, useVbenVxeGrid } from "@vben/plugins/vxe-table";

import { ElButton, ElImage, ElIcon } from "element-plus";

import { useVbenForm } from "./form";
import { ICON_COMPONENT_MAP } from "../views/content/category-data";

setupVbenVxeTable({
  configVxeTable: (vxeUI) => {
    vxeUI.setConfig({
      grid: {
        align: "center",
        border: false,
        columnConfig: {
          resizable: true,
        },
        minHeight: 180,
        formConfig: {
          // 全局禁用vxe-table的表单配置，使用formOptions
          enabled: false,
        },
        proxyConfig: {
          autoLoad: true,
          response: {
            result: "items",
            total: "total",
            list: "items",
          },
          showActiveMsg: true,
          showResponseMsg: false,
        },
        round: true,
        showOverflow: true,
        size: "small",
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellImage' },
    vxeUI.renderer.add("CellImage", {
      renderTableDefault(_renderOpts, params) {
        const { column, row } = params;
        const src = row[column.field];
        return h(ElImage, { src, previewSrcList: [src] });
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellLink' },
    vxeUI.renderer.add("CellLink", {
      renderTableDefault(renderOpts) {
        const { props } = renderOpts;
        return h(
          ElButton,
          { size: "small", link: true },
          { default: () => props?.text },
        );
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellIcon' },
    vxeUI.renderer.add("CellIcon", {
      renderTableDefault(_renderOpts, params) {
        const { column, row } = params;
        const IconComponent = ICON_COMPONENT_MAP[row[column.field]];
        return h(
          ElIcon,
          { 
            size: 24,
            style: "color: var(--el-color-primary);"
          },
          {
            default: () => h(IconComponent)
          }
        );
      },
    });

    // 这里可以自行扩展 vxe-table 的全局配置，比如自定义格式化
    // vxeUI.formats.add
  },
  useVbenForm,
});

export { useVbenVxeGrid };
// 为了解决导入问题，将useVbenVxeGrid别名为useVxeGrid导出
// 并调整返回值结构以适配现有组件的使用方式
export function useVxeGrid(options = {}) {
  const [Grid, api] = useVbenVxeGrid(options);

  // 创建gridRef引用
  const gridRef = ref(null);

  // 创建gridOptions引用
  const gridOptions = ref({});

  // 扩展api，添加setData方法
  const extendedApi = {
    ...api,
    // 添加setData方法，内部调用setGridOptions更新数据
    setData: (data) => {
      api.setGridOptions({
        data,
      });
    },
  };

  // 返回符合现有组件期望的对象结构
  return {
    register: () => {}, // 提供空注册函数以兼容接口
    gridRef,
    gridOptions,
    Grid,
    api: extendedApi,
  };
}

export type * from "@vben/plugins/vxe-table";
