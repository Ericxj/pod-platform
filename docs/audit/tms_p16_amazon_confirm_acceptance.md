# P1.6 Amazon confirmShipment 回传 — 验收脚本

## 前置条件

1. 执行迁移：`docs/sql/tms/20260220_p16_channel_shipment_ack.sql`
2. 执行 IAM：`docs/sql/iam/20260220_tms_ack_menu_permissions.sql`
3. 存在一条 **SHIPPED** 的 WMS 出库单，且有关联 **wms_shipment**（tracking_no、carrier_code、shipped_at）、**fulfillment**、**oms_unified_order**（external_order_id，可选 marketplace_id、order_purchase_date_utc）、**oms_unified_order_item**（可选 external_order_item_id）
4. Mock 网关：`tms.amazon.gateway=mock`，`tms.amazon.confirm.mock.response=204`（或 429/503/400 用于步骤 3/4）

---

## 1) 出库 SHIPPED → createAckFromOutbound（幂等）

- **请求**：`POST /api/tms/acks/createFromOutbound`  
  Body: `{"outboundId": <已 SHIPPED 的出库单 ID>}`  
  Header: `X-Request-Id: req-ack-1`、租户/工厂/Authorization
- **预期**：200，返回 `channel_shipment_ack.id`；表 `channel_shipment_ack` 新增一条，`channel=AMAZON`，`amazon_order_id=订单 external_order_id`，`package_reference_id=1`，`status=CREATED`，`uk_ack` 唯一
- **幂等**：同一 outboundId 再次请求（同一或不同 X-Request-Id），返回同一 ack id，不新增记录

---

## 2) 手动重试 → confirmShipment（Mock 204）

- **请求**：`POST /api/tms/acks/{ackId}/retry`
- **预期**：200；ack 状态由 CREATED → SENDING → **SUCCESS**；`response_code=204`；`request_payload_json` 有 marketplaceId、packageDetail（trackingNumber、shipDate、orderItems）
- **shipDate 纠偏**：payload 中 shipDate 为 UTC Instant，且 ≤ nowUtc-2s，且 ≥ order 的 order_purchase_date_utc（若有）

---

## 3) 模拟 429/503 → FAILED_RETRYABLE + next_retry_at

- 配置：`tms.amazon.confirm.mock.response=429`
- **请求**：对一条新的 ack（或新建一条）执行 `POST /api/tms/acks/{id}/retry`
- **预期**：200；ack 状态 → **FAILED_RETRYABLE**；`next_retry_at` 已设置（约 1 分钟后）；`retry_count` 递增；`error_code`/`error_message` 有值
- 配置改回 `tms.amazon.confirm.mock.response=204`，等待 next_retry_at 或再次手动 retry，应变为 SUCCESS

---

## 4) 模拟 400 InvalidInput → FAILED_MANUAL + OMS Hold

- 配置：`tms.amazon.confirm.mock.response=400`
- **请求**：`POST /api/tms/acks/{id}/retry`
- **预期**：200；ack 状态 → **FAILED_MANUAL**；表 **oms_order_hold** 新增一条：`hold_type=CHANNEL_ACK`，`reason_code=AMZ_CONFIRM_SHIPMENT_FAILED`，`external_order_id=amazon_order_id`，`status=OPEN`

---

## 5) XXL-JOB 重试

- 在 XXL-JOB 控制台配置并执行 **tmsAckAmazonShipmentJobHandler**（参数可选：tenantId=1,factoryId=1）
- **预期**：扫描 `status IN (CREATED, FAILED_RETRYABLE)` 且 `next_retry_at <= now` 的 ack，逐条 sendAck；成功条数/失败条数在日志或 handleSuccess 中可见
- 将一条 ack 设为 FAILED_RETRYABLE、next_retry_at 设为过去，再次执行 Job，该条应被重试

---

## 6) 权限

- **无权限用户**：调用 `GET /api/tms/acks` 或 `POST /api/tms/acks/{id}/retry` 未带对应权限 → 403（或项目统一未授权响应）
- **ADMIN（role_id=1）**：拥有 tms:ack:page / get / retry / create 及对应 API 权限，可正常访问列表、详情、重试、创建

---

## 7) 前端可观测

- 菜单 **TMS → 回传任务**：列表展示订单号、tracking、carrier、shipDate、status、retryCount、nextRetryAt、lastError
- **详情**：请求 payload（折叠）、响应码/响应体、错误码/错误信息、手动重试按钮
- **从出库单创建**：输入 outboundId，创建回传任务

---

## 检查点汇总

| 项 | 检查内容 |
|----|----------|
| 幂等创建 | 同一 outboundId 仅一条 ack（uk_ack） |
| confirmShipment | Mock 204 → SUCCESS；request_payload 含 marketplaceId、packageDetail、shipDate、orderItems |
| shipDate | ≤ nowUtc-2s，≥ orderPurchaseDateUtc |
| 429/503 | FAILED_RETRYABLE + next_retry_at + 指数退避 |
| 400 | FAILED_MANUAL + OMS hold（CHANNEL_ACK, AMZ_CONFIRM_SHIPMENT_FAILED） |
| XXL-JOB | tmsAckAmazonShipmentJobHandler 扫描并重试 |
| 权限 | API + BUTTON 校验；ADMIN 可访问 |
