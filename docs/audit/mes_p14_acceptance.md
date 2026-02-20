# P1.4 MES 工单域 验收脚本

## 前置

1. 执行 DB 迁移：`docs/sql/mes/20260215_p14_mes_work_order_report.sql`
2. 执行 IAM 菜单与权限：`docs/sql/iam/20260215_mes_menu_permissions.sql`
3. 已有 Fulfillment 状态为 **ART_READY**（P1.3 生产图全部 READY 后推进）。
4. INV：目标仓库/库位存在对应 SKU 的 inv_balance 记录（完工入库会增加 on_hand_qty）。

## 验收流程

### 1) 从 Fulfillment(ART_READY) 创建工单（幂等 uk_source）

- 记下一个 ART_READY 的履约单 ID：`fulfillmentId`。
- 调用 `POST /api/mes/work-orders/from-fulfillment/{fulfillmentId}`，Header：`X-Request-Id`、`X-Tenant-Id`、`X-Factory-Id`、`Authorization`。
- **预期**：200；返回工单 ID；`mes_work_order` 新增一条，`source_type=FULFILLMENT`、`source_no=履约单号`；`mes_work_order_item` 按履约行生成（qty 取自 reserved_qty 或 qty）；`mes_work_order_op` 按默认工序（如 PRINT/QC/PACK）生成。
- 再次用同一 `fulfillmentId` 调用创建接口。
- **预期**：幂等，返回同一工单 ID，不重复插入。

### 2) 释放 / 开始 / 报工 → produced_qty 累加

- `POST /api/mes/work-orders/{id}/release`：工单状态 CREATED → RELEASED（where status=CREATED 乐观更新）。
- `POST /api/mes/work-orders/{id}/start`：工单状态 RELEASED → IN_PROGRESS。
- `POST /api/mes/work-orders/{id}/report`，body：`{"lineId": <mes_work_order_item.id>, "goodQty": 2, "scrapQty": 0, "opCode": "REPORT"}`。
- **预期**：200；`mes_report` 新增一条；对应 `mes_work_order_item.produced_qty` 累加 2。
- 继续报工直至该行 `produced_qty` ≥ 该行 `qty`（若工单多行则每行都需报满）。

### 3) 工单 DONE → INV 入库 → Fulfillment READY_TO_SHIP

- 当工单下**所有行**的 `produced_qty` ≥ 各行 `qty` 时，下一次报工会触发 `finishIfCompleted`：工单状态 → DONE。
- **预期**：
  - `mes_work_order.status` = DONE。
  - 调用 INV：`InventoryApplicationService.produceIn("MES_PRODUCE_IN", workOrderNo, warehouseId, skuId, producedQty)` 按行入库；`inv_balance.on_hand_qty` 增加，`inv_ledger` 有 PRODUCE_IN 记录（幂等：同一 bizType+bizNo+skuId 仅入一次）。
  - 对应 Fulfillment 状态由 ART_READY → **READY_TO_SHIP**（`oms_fulfillment.status` 更新，乐观锁）。

### 4) 取消工单

- `POST /api/mes/work-orders/{id}/cancel`：工单状态为 CREATED/RELEASED/IN_PROGRESS 时可取消 → CANCELED。

### 5) API 与前端

- `GET /api/mes/work-orders?current=1&size=10&status=CREATED`：分页、多租户过滤。
- `GET /api/mes/work-orders/{id}`：工单详情。
- `GET /api/mes/work-orders/{id}/lines`、`/ops`、`/reports`：工单行、工序、报工记录。
- 前端：菜单 MES/工单，列表、详情（工序/报工/报工记录）、按钮释放/开始/报工/取消、按履约单创建。

## HTTP 示例（替换 baseUrl、token、tenantId、factoryId）

```http
### 按履约单创建工单（幂等）
POST {{baseUrl}}/api/mes/work-orders/from-fulfillment/1
X-Request-Id: mes-wo-{{$timestamp}}
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}

### 分页查询工单
GET {{baseUrl}}/api/mes/work-orders?current=1&size=10&status=CREATED
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}

### 工单详情
GET {{baseUrl}}/api/mes/work-orders/1
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}

### 释放
POST {{baseUrl}}/api/mes/work-orders/1/release
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}

### 开始
POST {{baseUrl}}/api/mes/work-orders/1/start
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}

### 报工
POST {{baseUrl}}/api/mes/work-orders/1/report
Content-Type: application/json
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}

{"lineId": 1, "goodQty": 2, "scrapQty": 0, "opCode": "REPORT"}

### 取消
POST {{baseUrl}}/api/mes/work-orders/1/cancel
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}
```

## 简要检查清单

- [ ] 迁移执行无报错；mes_work_order 有 source_type/source_no、uk_source；mes_work_order_item 有 produced_qty/scrap_qty；mes_operation、mes_report 表存在。
- [ ] 创建工单：Fulfillment ART_READY → 创建工单；重复创建幂等。
- [ ] 释放/开始：状态 CREATED→RELEASED→IN_PROGRESS。
- [ ] 报工：mes_report 写入，工单行 produced_qty 累加；全部行报满后工单 DONE。
- [ ] 工单 DONE 后：INV 完工入库成功（on_hand 增加、ledger PRODUCE_IN）；Fulfillment ART_READY→READY_TO_SHIP。
- [ ] 取消：CREATED/RELEASED/IN_PROGRESS → CANCELED。
- [ ] 前端 MES/工单 列表、详情、报工、按钮与权限正常。
