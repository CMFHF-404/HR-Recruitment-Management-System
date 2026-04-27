# 数据库设计说明

## 表结构

| 表名 | 说明 |
| --- | --- |
| `admin` | 管理员账号，密码使用 BCrypt 加密 |
| `position` | 招聘岗位信息 |
| `candidate` | 候选人基本信息，并关联应聘岗位 |
| `resume_screening` | 简历筛选状态和意见 |
| `interview` | 面试安排、状态和评价 |
| `offer_result` | 最终录用状态和备注 |

## 关系说明

- 一个岗位可以关联多个候选人。
- 一个候选人对应一条简历筛选记录、一条面试记录和一条录用结果记录。
- 删除岗位时，如果岗位已有关联候选人，则后端拒绝删除。
- 删除候选人时，后端会同步删除该候选人的筛选、面试和录用记录。

## 状态枚举

- 岗位状态：`OPEN`、`CLOSED`
- 简历筛选：`PENDING`、`PASSED`、`REJECTED`
- 面试状态：`NOT_SCHEDULED`、`SCHEDULED`、`COMPLETED`、`CANCELED`
- 录用状态：`PENDING`、`OFFERED`、`REJECTED`、`ABANDONED`
