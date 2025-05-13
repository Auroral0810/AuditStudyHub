import {
  Notebook,
  DataAnalysis,
  Document,
  Reading,
  VideoPlay,
  DocumentChecked,
  Folder,
  Picture,
  Upload,
  Download,
} from "@element-plus/icons-vue";

// 分类数据接口
export interface Category {
  id: number;
  name: string;
  icon: string;
  sort: number;
  create_time: string;
  update_time: string;
  is_deleted: number;
}


// 图标选项
export const ICON_OPTIONS = [
  { label: "笔记本", value: "Notebook" },
  { label: "数据分析", value: "DataAnalysis" },
  { label: "文档", value: "Document" },
  { label: "阅读", value: "Reading" },
  { label: "视频播放", value: "VideoPlay" },
  { label: "已审核文档", value: "DocumentChecked" },
  { label: "文件夹", value: "Folder" },
  { label: "图片", value: "Picture" },
  { label: "上传", value: "Upload" },
  { label: "下载", value: "Download" },
];

// 图标映射
export const ICON_COMPONENT_MAP = {
  Notebook,
  DataAnalysis,
  Document,
  Reading,
  VideoPlay,
  DocumentChecked,
  Folder,
  Picture,
  Upload,
  Download,
};
