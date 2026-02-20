# P1.5 WMS 出库最小版 — 验收脚本

## 前置条件

1. 执行 WMS 迁移：`docs/sql/wms/20260215_p15_wms_outbound_shipment.sql`
2. 执行 IAM 权限：`docs/sql/iam/20260215_wms_menu_permissions.sql`
3. 存在一条 **READY_TO_SHIP** 的 Fulfillment，且该履约单已做 **FULFILLMENT** 预占（inv_reservation 有对应记录，biz_type='FULFILLMENT', biz_no=fulfillment_no）

## 验收步骤

### 1) 从 Fulfillment 创建出库单（幂等）

- 调用：`POST /api/wms/outbounds/from-fulfillment/{fulfillmentId}`（fulfillmentId 为上述履约单 ID）
- 期望：返回出库单 ID；`wms_outbound_order` 新增一条，`source_type='FULFILLMENT'`，`source_no=fulfillment_no`，`status='CREATED'`
- 再次用同一 fulfillmentId 调用
- 期望：返回同一出库单 ID（幂等）；表条数不增加

### 2) 开始拣货 → 确认拣货 → 打包 → 发货

- **开始拣货**：`POST /api/wms/outbounds/{outboundId}/picking/start`  
  期望：出库单 `status` 变为 `PICKING`（仅当原状态为 CREATED 时允许）

- **确认拣货**：`POST /api/wms/outbounds/{outboundId}/picking/confirm`，body：`[{"lineId":<lineId>,"pickedQty":<qty>}, ...]`  
  期望：对应 `wms_outbound_order_line` 的 `qty_picked` 累加正确

- **打包**：`POST /api/wms/outbounds/{outboundId}/pack`  
  期望：生成 `wms_pack` / `wms_pack_line`（若有）；出库单行 `packed_qty` 回写；出库单 `status='PACKED'`

- **发货**：`POST /api/wms/outbounds/{outboundId}/ship`，body：`{"carrierCode":"SF","trackingNo":"SF123456"}`  
  期望：  
  - INV：`inventory_balance` 扣减（on_hand、allocated）；对应 `inventory_reservation`  consume；`inventory_ledger` 有 WMS_SHIP 记录（bizType=FULFILLMENT, bizNo=fulfillment_no）  
  - 再次调用 ship（同一 outboundId）：扣减只发生一次（幂等）  
  - `wms_shipment` 新增一条，carrier_code/tracking_no/shipped_at 正确  
  - 出库单 `status='SHIPPED'`  
  - Fulfillment 状态更新为 `SHIPPED`（若有关联且原为 READY_TO_SHIP）

### 3) 取消（可选）

- 对一条未发货的出库单（如新创建的 CREATED 状态）：`POST /api/wms/outbounds/{outboundId}/cancel`
- 期望：出库单 `status='CANCELLED'`；INV 调用 `releaseByBiz("FULFILLMENT", sourceNo)`，预占释放正确

### 4) 前端

- 菜单：WMS → 出库单，可打开列表页
- 列表：分页、按状态筛选；支持「按履约单创建」输入 fulfillmentId 创建出库单
- 详情：可查看出库单及行信息；根据状态展示「开始拣货 / 确认拣货 / 打包 / 发货 / 取消」
- 拣货弹窗：按行填写本次拣货数量并提交
- 发货弹窗：填写承运商、运单号后提交

## 检查点汇总

| 项 | 检查内容 |
|----|----------|
| 幂等创建 | 同一 fulfillmentId 多次创建只产生一张出库单（uk_src） |
| INV 扣减 | ship 时按 FULFILLMENT+fulfillmentNo 扣减且只扣一次 |
| 预占 | 发货时预占 consume/结转；取消时释放 |
| 状态回写 | 出库单 CREATED→PICKING→PACKED→SHIPPED；Fulfillment→SHIPPED |
| 权限 | wms:center、wms:outbound:* 已授权 ADMIN(role_id=1) |
