# OMS P1.1 验收脚本

## 前置

1. 执行 DB 迁移：`docs/sql/oms/20260214_p1_oms_unified_order_hold.sql`
2. 执行 IAM 菜单与权限：`docs/sql/iam/20260214_oms_menu_permissions.sql`
3. 确保 PRD 已有 SKU 及平台映射（无 mapping 时拉单会产生 hold）

## 验收流程

### 1. 无 mapping -> 拉单 -> 生成 hold

- 在 PRD 中**不**为 `AMZ-SELLER-SKU-001` 配置映射（或使用 mock 返回的其他 externalSku）。
- 在 XXL-JOB 控制台执行任务 `omsPullAmazonOrdersJobHandler`，参数：`shopId=1`（或 `shopId=1,lastUpdatedAfter=2026-02-01 00:00:00`）。
- **预期**：任务成功；`oms_unified_order` 新增一条订单；`oms_unified_order_item` 中对应行 `sku_id` 为空；`oms_order_hold` 新增一条 `hold_type=SKU_MAPPING`、`status=OPEN` 的记录。

### 2. 绑定 SKU -> hold 关闭 -> 订单行回写

- 调用 `GET /api/oms/holds?type=SKU_MAPPING&status=OPEN` 获取 OPEN 的 hold 列表，记下一条 `id`。
- 调用 `POST /api/oms/holds/{id}/resolve`，body：`{"skuId": <已激活的 SKU id>}`（Header：X-Request-Id、X-Tenant-Id、X-Factory-Id、Authorization）。
- **预期**：200；该 hold 的 `status=RESOLVED`，`resolve_sku_id`、`resolve_sku_code`、`resolved_at`、`resolved_by` 已填；对应 `oms_unified_order_item` 的 `sku_id`、`sku_code` 已回写。

### 3. 重跑拉单幂等

- 再次执行 XXL-JOB `omsPullAmazonOrdersJobHandler`，参数与第一次相同（同一时间范围，mock 会返回相同 external_order_id）。
- **预期**：任务成功；**不会**重复插入同一 `external_order_id` 的订单（幂等）；订单条数不变。

### 4. 唯一约束与租户过滤

- **唯一约束**：同一 tenant_id、factory_id、channel、shop_id、external_order_id 仅能有一条订单；同一 (tenant, factory, hold_type, channel, shop_id, external_order_id, external_sku) 仅能有一条 hold。
- **租户/工厂过滤**：切换租户或工厂后，`GET /api/oms/unified-orders` 与 `GET /api/oms/holds` 仅返回当前 tenant_id/factory_id 且 deleted=0 的数据。

## HTTP 示例（替换 baseUrl、token、tenantId、factoryId）

```http
### 分页查询统一订单
GET {{baseUrl}}/api/oms/unified-orders?current=1&size=10
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}

### 分页查询异常队列（OPEN）
GET {{baseUrl}}/api/oms/holds?current=1&size=10&type=SKU_MAPPING&status=OPEN
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}

### 处理异常：绑定 SKU
POST {{baseUrl}}/api/oms/holds/1/resolve
Content-Type: application/json
X-Request-Id: resolve-{{$timestamp}}
X-Tenant-Id: {{tenantId}}
X-Factory-Id: {{factoryId}}
Authorization: Bearer {{token}}

{"skuId": 4001}
```

## 前端验收

- 登录后可见 **OMS -> 统一订单**、**OMS -> 异常队列**。
- 统一订单：列表筛选渠道/店铺ID/平台订单ID/状态，分页正常。
- 异常队列：列表筛选类型/状态/渠道/店铺；点击「绑定SKU」打开弹窗，搜索 SKU、选择后提交，列表刷新且该条状态变为 RESOLVED。

---

# P1.2 Fulfillment 验收

## 前置

1. 执行 `docs/sql/oms/20260215_p12_fulfillment_reserve.sql`
2. 执行 `docs/sql/iam/20260215_ful_menu_permissions.sql`
3. INV 已有对应 warehouse/sku 的 inv_balance 且 available_qty ≥ 订单行数量

## 流程

### 1) 创建履约单（幂等）

- 对已有统一订单（且所有行均有 sku_id）调用创建履约：`POST` 无直接接口，由拉单任务在「全部行已映射」时自动创建，或通过内部/脚本调用 `FulfillmentApplicationService.createFromUnifiedOrder(unifiedOrderId)`。
- 再次对同一订单创建：**预期** 返回已存在的履约单 id，不重复插入（uk_src_order 幂等）。

### 2) 预占成功 -> 状态 RESERVED

- 对状态为 CREATED 的履约单，前端点击「预占」或调用 `POST /api/ful/fulfillments/{id}/reserve/retry`。
- **预期**：200；履约单状态变为 RESERVED；行表 reserved_qty、reserve_status 已更新；inv_reservation 有对应记录。

### 3) 预占失败 -> HOLD_INVENTORY

- 将对应 inv_balance 的 available_qty 置为 0 或不足，再对 CREATED 履约单调用预占。
- **预期**：业务报错或 4xx；履约单状态变为 HOLD_INVENTORY；行 reserve_status 为 SHORTAGE。

### 4) 补库存后重试预占 -> RESERVED

- 恢复 inv_balance 可用量后，对 HOLD_INVENTORY 履约单调用 `POST /api/ful/fulfillments/{id}/reserve/retry`。
- **预期**：200；状态变为 RESERVED。

### 5) 取消履约

- 对 CREATED/RESERVED/HOLD_INVENTORY 履约单调用 `POST /api/ful/fulfillments/{id}/cancel`。
- **预期**：200；状态变为 CANCELLED；若原为 RESERVED，inv 预占已释放（releaseByBiz）。

## HTTP 示例

```http
GET {{baseUrl}}/api/ful/fulfillments?current=1&size=10&status=CREATED
POST {{baseUrl}}/api/ful/fulfillments/1/reserve/retry
POST {{baseUrl}}/api/ful/fulfillments/1/cancel
```
