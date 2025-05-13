/**
 * 用户信息类型，与后端UserDTO结构对应
 */
export interface UserInfo {
  id: number;
  username: string;
  realName: string;
  studentId: string;
  email: string;
  phone: string;
  avatar: string;
  collegeId: number;
  collegeName: string;
  majorId: number;
  majorName: string;
  role: number;
  status: number;
} 