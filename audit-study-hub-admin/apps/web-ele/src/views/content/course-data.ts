// 课程数据接口
export interface Course {
  id: number;
  name: string;
  description: string | null;
  create_time: string;
  update_time: string;
  is_deleted: number;
  credit: number;
}
