// 资源审核页面使用的数据和模型，基于资源表的数据结构
import type { Resource } from "./list-data";

// 审核结果选项
export const AUDIT_RESULT_OPTIONS = [
  { label: "待审核", value: 0 },
  { label: "已通过", value: 1 },
  { label: "已拒绝", value: 2 },
];

// 审核意见模板
export const AUDIT_OPINION_TEMPLATES = [
  { label: "资源内容符合规范，审核通过", value: "资源内容符合规范，审核通过" },
  { label: "资源质量优秀，审核通过", value: "资源质量优秀，审核通过" },
  {
    label: "资源内容丰富，对学习有帮助，审核通过",
    value: "资源内容丰富，对学习有帮助，审核通过",
  },
  {
    label: "资源内容不符合规范，请修改后重新提交",
    value: "资源内容不符合规范，请修改后重新提交",
  },
  {
    label: "资源存在敏感内容，审核不通过",
    value: "资源存在敏感内容，审核不通过",
  },
  {
    label: "资源内容与分类不符，请调整分类后重新提交",
    value: "资源内容与分类不符，请调整分类后重新提交",
  },
  {
    label: "资源可能侵犯版权，审核不通过",
    value: "资源可能侵犯版权，审核不通过",
  },
];

// 审核记录接口
export interface AuditRecord {
  id: number;
  resource_id: number;
  resource_title: string;
  audit_user_id: number;
  audit_time: string;
  audit_result: number; // 对应 AUDIT_RESULT_OPTIONS 的 value
  audit_opinion: string;
}

// 获取待审核资源
export function getPendingAuditResources(resources: Resource[]): Resource[] {
  return resources.filter((item) => item.status === 0 && item.is_deleted === 0);
}

// 模拟审核记录数据
export const MOCK_AUDIT_RECORDS: AuditRecord[] = [
  {
    id: 1,
    resource_id: 2,
    resource_title: "计算机网络原理PPT",
    audit_user_id: 1,
    audit_time: "2025-05-06 14:32:10",
    audit_result: 1,
    audit_opinion: "资源内容符合规范，审核通过",
  },
  {
    id: 2,
    resource_id: 3,
    resource_title: "审计学原理与实务教材",
    audit_user_id: 1,
    audit_time: "2025-05-07 08:24:53",
    audit_result: 1,
    audit_opinion: "资源质量优秀，审核通过",
  },
  {
    id: 3,
    resource_id: 21,
    resource_title: "哄哄就哦i见哦",
    audit_user_id: 1,
    audit_time: "2025-05-09 15:30:06",
    audit_result: 2,
    audit_opinion: "资源内容不符合规范，请修改后重新提交",
  },
];
