// 定义API响应数据类型
export interface CategoryItem {
  id: number;
  name: string;
  icon: string;
  sort: number;
  createTime: string;
  updateTime: string;
  isDeleted: number;
}

export interface CollegeItem {
  id: number;
  name: string;
  coverUrl: string;
  logoUrl: string;
  createTime: string;
  updateTime: string;
  isDeleted: number;
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T[];
}

// 处理API返回的分类数据 - 修改为接受直接数组
export function processCategoryData(apiData: CategoryItem[]) {
  if (!apiData || !Array.isArray(apiData)) return [];
  
  return apiData.map(item => ({
    id: item.id,
    name: item.name,
    icon: item.icon,
    sort: item.sort,
    create_time: formatDate(item.createTime),
    update_time: formatDate(item.updateTime),
    is_deleted: item.isDeleted
  }));
}

// 处理API返回的学院数据 - 修改为接受直接数组
export function processCollegeData(apiData: CollegeItem[]) {
  if (!apiData || !Array.isArray(apiData)) return [];
  
  return apiData.map(item => ({
    id: item.id,
    name: item.name,
    cover_url: item.coverUrl,
    logo_url: item.logoUrl,
    create_time: formatDate(item.createTime),
    update_time: formatDate(item.updateTime),
    is_deleted: item.isDeleted
  }));
}

// 格式化日期辅助函数
function formatDate(dateStr: string): string {
  if (!dateStr) return '';
  try {
    const date = new Date(dateStr);
    return date.toISOString().replace('T', ' ').slice(0, 19);
  } catch (e) {
    return dateStr;
  }
} 