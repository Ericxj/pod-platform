# P1.6 TMS 最小版 — 验收说明

## 前置条件

1. 执行 TMS 迁移：`docs/sql/tms/20260215_p16_tms_shipment_carrier.sql`
2. 执行 IAM 权限：`docs/sql/iam/20260215_tms_menu_permissions.sql`
3. 存在一条 **SHIPPED** 的 WMS 出库单（且已有 wms_shipment 记录，可选）

## 验收步骤

### 1) WMS 出库单 SHIPPED → 创建 tms_shipment（幂等）

- 调用：`POST /api/tms/shipments/from-outbound/{outboundId}`（outboundId 为已 SHIPPED 的出库单 ID）
- 期望：返回 tms_shipment.id；`tms_shipment` 新增一条，`source_type='WMS_OUTBOUND'`，`source_no=outbound_no`，`status='CREATED'`
- 再次用同一 outboundId 调用
- 期望：返回同一 shipment.id（幂等）；表条数不增加

### 2) createLabel → trackingNo/labelUrl 回写

- 调用：`POST /api/tms/shipments/{id}/label/create`
- 期望：承运商网关（当前 Mock）返回运单号与面单 URL；`tms_shipment` 更新 `tracking_no`、`label_url`，`status='LABEL_CREATED'`
- 失败时：`status='FAILED'`，`fail_reason` 有值，便于 XXL-JOB 重试

### 3) syncToChannel(Amazon) → TRACKING_SYNCED

- 调用：`POST /api/tms/shipments/{id}/sync/channel`（shipment 状态为 LABEL_CREATED 或 HANDED_OVER）
- 期望：渠道网关（当前 Mock Amazon）返回成功；`tms_shipment.status='TRACKING_SYNCED'`

### 4) XXL-JOB 重试

- **tmsCreateLabelJob**：扫描 `status IN (CREATED, FAILED)` 的 tms_shipment，逐个调用 createLabel；失败单可再次被扫描重试
- **tmsSyncToChannelJob**：扫描 `status IN (LABEL_CREATED, HANDED_OVER)` 的 tms_shipment，逐个调用 syncToChannel
- 在 XXL-JOB 控制台配置并执行上述两个任务，确认可跑通且失败可重试

### 5) 前端

- 菜单：TMS → 发货单（Shipment）
- 列表：分页、状态筛选；支持「从出库单创建」输入 outboundId 创建发货单
- 详情：展示 carrier/service/tracking/labelUrl/failReason；按钮「生成面单」「回传平台」按状态展示
- 承运商：GET /api/tms/carriers 用于配置页（可选展示）

## 检查点汇总

| 项 | 检查内容 |
|----|----------|
| 幂等创建 | 同一 outboundId 多次创建只产生一条 tms_shipment（uk_source） |
| createLabel | Mock 承运商返回 trackingNo/labelUrl，状态 LABEL_CREATED |
| syncToChannel | Mock Amazon 成功，状态 TRACKING_SYNCED |
| XXL-JOB | tmsCreateLabelJob、tmsSyncToChannelJob 可执行且失败可重试 |
| 权限 | tms:center、tms:shipment:*、tms:carrier:list 已授权 ADMIN(role_id=1) |
