// 资源请求回复数据接口
export interface ResourceRequestReply {
  id: number;
  request_id: number;
  user_id: number;
  content: string;
  resource_id: null | number;
  parent_id: null | number;
  create_time: string;
  update_time: string;
  is_deleted: number;
}

// 模拟资源请求回复数据
export const MOCK_RESOURCE_REQUEST_REPLY_DATA: ResourceRequestReply[] = [
  {
    id: 1,
    request_id: 1,
    user_id: 101,
    content: "我有这个资料，可以私聊我",
    resource_id: null,
    parent_id: null,
    create_time: "2025-04-20 15:32:46",
    update_time: "2025-04-20 15:32:46",
    is_deleted: 0,
  },
  {
    id: 2,
    request_id: 1,
    user_id: 102,
    content: "我找到了一份不太完整的，但应该可以参考",
    resource_id: 3,
    parent_id: null,
    create_time: "2025-04-20 16:45:13",
    update_time: "2025-04-20 16:45:13",
    is_deleted: 0,
  },
  {
    id: 3,
    request_id: 1,
    user_id: 103,
    content: "谢谢分享，非常有帮助",
    resource_id: null,
    parent_id: 2,
    create_time: "2025-04-20 17:23:41",
    update_time: "2025-04-20 17:23:41",
    is_deleted: 0,
  },
  {
    id: 4,
    request_id: 2,
    user_id: 104,
    content: "我有一份去年的题库，不知道今年的变化大不大",
    resource_id: 4,
    parent_id: null,
    create_time: "2025-04-21 09:12:35",
    update_time: "2025-04-21 09:12:35",
    is_deleted: 0,
  },
  {
    id: 5,
    request_id: 2,
    user_id: 105,
    content: "今年的题型有变化，但知识点差不多",
    resource_id: null,
    parent_id: 4,
    create_time: "2025-04-21 10:31:56",
    update_time: "2025-04-21 10:31:56",
    is_deleted: 0,
  },
  {
    id: 6,
    request_id: 3,
    user_id: 106,
    content: "我手上有一份教程，比较全面",
    resource_id: 5,
    parent_id: null,
    create_time: "2025-04-22 14:28:37",
    update_time: "2025-04-22 14:28:37",
    is_deleted: 0,
  },
  {
    id: 7,
    request_id: 4,
    user_id: 107,
    content: "这门课程的资料比较难找，我只有一份部分讲义",
    resource_id: null,
    parent_id: null,
    create_time: "2025-04-23 16:52:14",
    update_time: "2025-04-23 16:52:14",
    is_deleted: 1,
  },
  {
    id: 8,
    request_id: 5,
    user_id: 108,
    content: "我刚好做过这个实验，资料已分享",
    resource_id: 6,
    parent_id: null,
    create_time: "2025-04-24 11:34:27",
    update_time: "2025-04-24 11:34:27",
    is_deleted: 0,
  },
  {
    id: 9,
    request_id: 6,
    user_id: 109,
    content: "我有一份比较新的PPT，希望对你有帮助",
    resource_id: 7,
    parent_id: null,
    create_time: "2025-04-25 13:45:59",
    update_time: "2025-04-25 13:45:59",
    is_deleted: 0,
  },
  {
    id: 10,
    request_id: 6,
    user_id: 110,
    content: "谢谢分享！正是我需要的",
    resource_id: null,
    parent_id: 9,
    create_time: "2025-04-25 14:23:17",
    update_time: "2025-04-25 14:23:17",
    is_deleted: 0,
  },
];
