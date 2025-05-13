/**
 * 分类基础信息（用户界面使用）
 */
export interface CategoryBaseDTO {
  /**
   * 分类ID
   */
  id: number;
  
  /**
   * 分类名称
   */
  name: string;
  
  /**
   * 分类图标url
   */
  icon?: string;
  
  /**
   * 排序
   */
  sort?: number;

  /**
   * 显示样式类型（可选）
   * 用于控制前端显示样式，可以是：success, warning, info, danger, primary
   */
  styleType?: string;
  
  /**
   * 图标样式类（可选）
   * 用于控制图标的显示样式
   */
  iconClass?: string;
}
