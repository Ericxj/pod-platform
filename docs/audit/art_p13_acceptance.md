# P1.3 稿件域 + 自动生成生产图 验收脚本

## 前置

1. 执行 DB 迁移：`docs/sql/art/20260215_p13_art_job_production_file.sql`
2. 执行 IAM 菜单与权限：`docs/sql/iam/20260215_art_menu_permissions.sql`
3. 已有可用的 Fulfillment（状态 RESERVED 或 CREATED），且存在 `oms_fulfillment_item` 行（履约行）。

## 验收流程

### 1. 为履约单创建生产图任务（按行幂等）

- 记下一个已 RESERVED 的履约单 ID：`fulfillmentId`（如 1）。
- 调用 `POST /api/art/jobs/from-fulfillment/{fulfillmentId}`，Header：`X-Request-Id`、`X-Tenant-Id`、`X-Factory-Id`、`Authorization`。
- **预期**：200；返回任务 ID 列表（每个履约行一个任务）；`art_job` 表按 `uk_line`(tenant_id, factory_id, fulfillment_id, fulfillment_line_id) 幂等，重复调用返回相同数量、不重复插入。

### 2. 跑批生成生产图（XXL-JOB）

- 在 XXL-JOB 控制台执行任务 `artGenerateProductionFilesJobHandler`（无参数）。
- **预期**：任务成功；`art_job` 中原 `status=PENDING` 的记录变为 `status=GENERATING` 再变为 `status=READY`；`art_production_file` 表新增对应记录（art_job_id、file_url、file_hash 等）；若渲染失败则 job `status=FAILED`，`retry_count`+1，`last_error_code`/`last_error_msg` 有值。

### 3. 全部 line READY 后履约单状态推进

- 当该履约单下**所有** `art_job`（按 fulfillment_id 关联）的 `status` 均为 `READY` 时，跑批逻辑会将对应 `oms_fulfillment.status` 从 `RESERVED` 更新为 `ART_READY`。
- **验证**：跑批后查询该 fulfillment 的 `oms_fulfillment.status` 应为 `ART_READY`（若该单下仅有一个 line 且该 job 已 READY）。

### 4. API 与重试

- `GET /api/art/jobs?current=1&size=10&status=READY`：分页、多租户过滤，返回任务列表。
- `GET /api/art/jobs/{id}`：任务详情。
- `POST /api/art/jobs/{id}/retry`：对 `status=FAILED` 的任务重试（置为 PENDING，清空错误信息）；需有权限 `art:job:retry`。
- `GET /api/art/files?jobId={id}&current=1&size=10`：按任务 ID 分页查生产文件；多租户过滤。

### 5. 前端

- 菜单「稿件」下出现「生产图任务」「生产文件」；进入生产图任务列表可筛选状态、查看详情、重试失败任务；支持「按履约单创建」输入履约单 ID 创建任务；生产文件列表可按 jobId 筛选、查看详情与下载链接。

## HTTP 示例（替换 baseUrl、token、tenantId、factoryId）

```http
### 按履约单创建生产图任务（幂等）
POST {{baseUrl}}/api/art/jobs/from-fulfillment/1
X-Request-Id: art-create-{{$timestamp}}
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}

### 分页查询任务
GET {{baseUrl}}/api/art/jobs?current=1&size=10&status=PENDING
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}

### 任务详情
GET {{baseUrl}}/api/art/jobs/1
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}

### 重试失败任务
POST {{baseUrl}}/api/art/jobs/1/retry
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}

### 分页查询生产文件（按 jobId）
GET {{baseUrl}}/api/art/files?jobId=1&current=1&size=10
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}
```

## 简要检查清单

- [ ] 迁移执行无报错；`art_job` 有 `fulfillment_line_id/retry_count/last_error_*` 及 `uk_line`；`art_production_file` 有 `file_hash/format` 及 `uk_hash`、`idx_status`。
- [ ] 创建任务：按履约单创建得到 N 条任务（N=履约行数）；重复请求幂等。
- [ ] XXL-JOB `artGenerateProductionFilesJobHandler` 执行后 PENDING→READY，且生成 `art_production_file` 记录。
- [ ] 某履约单下全部 job READY 后，该履约单状态由 RESERVED→ART_READY。
- [ ] 前端稿件菜单、任务列表/详情/重试、文件列表/详情可用；权限控制生效。
