/**
 * 专业基础信息（用户界面使用）
 */
export interface MajorBaseDTO {
  /**
   * 专业ID
   */
  id: number;
  
  /**
   * 专业名称
   */
  name: string;
  
  /**
   * 所属学院ID
   */
  collegeId: number;
  
  /**
   * 所属学院名称
   */
  collegeName: string;
  
  /**
   * 学位类型
   */
  degree?: string;
  
  /**
   * 学历等级
   */
  xl?: number;
  
  /**
   * 学制
   */
  xz?: string;
} 