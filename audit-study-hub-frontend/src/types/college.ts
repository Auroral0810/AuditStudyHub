/**
 * 学院基础信息（用户界面使用）
 */
export interface CollegeBaseDTO {
  /**
   * 学院ID
   */
  id: number;
  
  /**
   * 学院名称
   */
  name: string;
  
  /**
   * 学院封面图片URL
   */
  coverUrl?: string;
  
  /**
   * 学院logo图片URL
   */
  logoUrl?: string;
} 