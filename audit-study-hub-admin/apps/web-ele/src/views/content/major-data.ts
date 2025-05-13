// 专业数据接口
export interface Major {
  id: number;
  name: string;
  college_id: number;
  college_name?: string;
  degree: string | null;
  xl: number;
  xz: string | null;
  create_time: string;
  update_time: string;
  is_deleted: number;
}

// 学院简单数据接口（用于关联）
export interface CollegeOption {
  id: number;
  name: string;
}


// 学历等级选项
export const XL_OPTIONS = [
  { label: "专科", value: 0 },
  { label: "本科", value: 1 },
  { label: "硕士", value: 2 },
  { label: "博士", value: 3 },
];

// 学制选项
export const XZ_OPTIONS = [
  { label: "四年制", value: "四年制" },
  { label: "二年制", value: "二年制" },
  { label: "三年制", value: "三年制" },
  { label: "五年制", value: "五年制" },
];
