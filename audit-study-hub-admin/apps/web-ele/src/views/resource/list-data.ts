// 资源状态选项
export const STATUS_OPTIONS = [
  { label: "待审核", value: 0 },
  { label: "已通过", value: 1 },
  { label: "已拒绝", value: 2 },
];

// 文件类型选项
export const FILE_TYPE_OPTIONS = [
  { label: "文档", value: "document" },
  { label: "图片", value: "image" },
  { label: "视频", value: "video" },
  { label: "音频", value: "audio" },
  { label: "压缩包", value: "zip" },
  { label: "PDF", value: "pdf" },
  { label: "PPT", value: "ppt" },
  { label: "Excel", value: "excel" },
  { label: "其他", value: "other" },
];

// 资源类型接口
export interface Resource {
  id: number;
  title: string;
  description: string | null;
  file_url: string;
  file_type: string;
  file_size: number;
  tags: string | null;
  download_count: number;
  view_count: number;
  like_count: number;
  uploader_id: number;
  uploaderName: string;
  college_id: number;
  college_name: string;
  major_id: number;
  major_name: string;
  course_id: number | null;
  course_name: string | null;
  category_id: number;
  category_name: string;
  status: number;
  create_time: string;
  update_time: string;
  is_deleted: number;
}

// 模拟资源数据
// TODO: 之后需要从后端获取
export const MOCK_RESOURCE_DATA = [
  {
    id: 1,
    title: "高等数学期末复习笔记",
    description: "南审数学学院2025年高等数学综合复习资料，包含重点公式和例题",
    file_url: "https://big-event20040810.oss-cn-beijing.aliyuncs.com/2509754.pdf",
    file_type: "pdf",
    file_size: 1245600,
    download_count: 104,
    view_count: 305,
    like_count: 13,
    uploader_id: 1,
    college_id: 1745,
    college_name: "数学学院",
    major_id: 5423,
    major_name: "数学与应用数学",
    course_id: null,
    course_name: null,
    category_id: 1,
    category_name: "学习笔记",
    tags: "高数,数学,期末复习",
    status: 1,
    create_time: "2025-05-03 10:15:28",
    update_time: "2025-05-06 15:32:10",
    is_deleted: 0,
  },
  {
    id: 2,
    title: "计算机网络原理PPT",
    description: "计算机学院网络原理课程精华PPT，涵盖TCP/IP和OSI模型",
    file_url: "https://big-event20040810.oss-cn-beijing.aliyuncs.com/Web%E6%8A%80%E6%9C%AF%E5%BA%94%E7%94%A8%E5%9F%BA%E7%A1%80_d08.ppt",
    file_type: "pptx",
    file_size: 3569200,
    download_count: 58,
    view_count: 208,
    like_count: 29,
    uploader_id: 2,
    college_id: 1744,
    college_name: "计算机学院/智能审计学院",
    major_id: 5419,
    major_name: "计算机科学与技术（计算机审计方向）",
    course_id: null,
    course_name: null,
    category_id: 2,
    category_name: "课件PPT",
    tags: "计算机网络,TCP/IP,OSI",
    status: 1,
    create_time: "2025-05-02 09:23:45",
    update_time: "2025-05-06 16:41:33",
    is_deleted: 0,
  },
  {
    id: 3,
    title: "审计学原理与实务教材",
    description: "社会审计学院推荐教材，含课后习题答案",
    file_url: "https://big-event20040810.oss-cn-beijing.aliyuncs.com/%E5%AE%9E%E4%B9%A0%E9%97%AE%E9%A2%98%EF%BC%88%E5%AE%A1%E8%AE%A1%EF%BC%89.pdf",
    file_type: "pdf",
    file_size: 8642500,
    download_count: 91,
    view_count: 344,
    like_count: 43,
    uploader_id: 3,
    college_id: 1738,
    college_name: "社会审计学院",
    major_id: 5406,
    major_name: "审计学（创新基地班）",
    course_id: null,
    course_name: null,
    category_id: 4,
    category_name: "电子书籍",
    tags: "审计学,教材,习题答案",
    status: 1,
    create_time: "2025-04-29 13:42:18",
    update_time: "2025-05-07 08:24:53",
    is_deleted: 0,
  },
  {
    id: 4,
    title: "金融衍生品分析实验报告",
    description: "金融工程专业期末实验报告模板与范例",
    file_url: "https://big-event20040810.oss-cn-beijing.aliyuncs.com/%E8%AE%A1%E7%AE%97%E6%9C%BA%E5%AD%A6%E9%99%A2%E5%AD%A6%E7%94%9F%E4%BC%9A%E7%BB%8F%E9%AA%8C%E5%88%86%E4%BA%AB%E4%BA%A4%E6%B5%81%E4%BC%9A%E5%8F%91%E8%A8%80%E7%A8%BF.docx",
    file_type: "docx",
    file_size: 762800,
    download_count: 26,
    view_count: 100,
    like_count: 8,
    uploader_id: 4,
    college_id: 1733,
    college_name: "金融学院",
    major_id: 5388,
    major_name: "金融工程",
    course_id: null,
    course_name: null,
    category_id: 6,
    category_name: "实验报告",
    tags: "金融衍生品,实验报告,模板",
    status: 1,
    create_time: "2025-05-05 16:31:45",
    update_time: "2025-05-07 10:12:36",
    is_deleted: 0,
  },
  {
    id: 5,
    title: "法学考研真题解析",
    description: "法学专业历年考研真题及详细解析",
    file_url: "https://big-event20040810.oss-cn-beijing.aliyuncs.com/%E8%AF%BE%E7%A8%8B%E8%A1%A8.xlsx",
    file_type: "pdf",
    file_size: 5467200,
    download_count: 122,
    view_count: 565,
    like_count: 73,
    uploader_id: 1,
    college_id: 1729,
    college_name: "法学院",
    major_id: 5369,
    major_name: "法学",
    course_id: null,
    course_name: null,
    category_id: 3,
    category_name: "资料文件",
    tags: "法学,考研,真题解析",
    status: 1,
    create_time: "2025-04-20 14:28:32",
    update_time: "2025-05-07 09:45:27",
    is_deleted: 0,
  },
  {
    id: 21,
    title: "哄哄就哦i见哦",
    description: "哄哄就哦i见哦哄哄就哦i见哦",
    file_url: "https://github.com/lin-xin/vue-manage-system",
    file_type: "repository",
    file_size: 0,
    download_count: 0,
    view_count: 0,
    like_count: 0,
    uploader_id: 1,
    college_id: 1729,
    college_name: "法学院",
    major_id: 5369,
    major_name: "法学",
    course_id: null,
    course_name: "Internal Auditing(IAEP)",
    category_id: 2,
    category_name: "课件PPT",
    tags: "[\"哄哄就哦i见哦\",\"哄哄就哦i见哦\"]",
    status: 0,
    create_time: "2025-05-09 13:45:10",
    update_time: "2025-05-09 15:30:06",
    is_deleted: 1,
  },
  {
    id: 22,
    title: "afeaeafga",
    description: "afeaeafgaafeaeafgaafeaeafgaafeaeafga",
    file_url: "https://big-event20040810.oss-cn-beijing.aliyuncs.com/resources/admin/20250509/1e33adc4d33d487ea63028bbb81f2e0e.png",
    file_type: "image",
    file_size: 1259669,
    download_count: 0,
    view_count: 1,
    like_count: 0,
    uploader_id: 1,
    college_id: 1730,
    college_name: "外国语学院",
    major_id: 5373,
    major_name: "英语（涉外审计）",
    course_id: null,
    course_name: "Internal Auditing(IAEP)",
    category_id: 2,
    category_name: "课件PPT",
    tags: "[\"afeaeafga\",\"afeaeafga\"]",
    status: 0,
    create_time: "2025-05-09 17:00:11",
    update_time: "2025-05-09 17:00:11",
    is_deleted: 0,
  },
];
