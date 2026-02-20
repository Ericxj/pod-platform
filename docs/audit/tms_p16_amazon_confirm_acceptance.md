# P1.6 Amazon confirmShipment 回传 — 验收脚本

## 自测方式（最小可执行）

- **Mock 验收**：保持 `tms.amazon.gateway=mock`，按步骤 1–9 执行；确认幂等创建、204 成功、429/503 可重试、400 → FAILED_MANUAL + OMS Hold、XXL-JOB、权限与前端可观测；P1.6+ Case 8（缺 orderItemId 自动回填成功）、Case 9（无法匹配 → FAILED_MANUAL + AMZ_ORDER_ITEM_NOT_MATCHED hold）。
- **Real 模式**：切到 `tms.amazon.gateway=real` 并配置 `tms.amazon.spapi.*`（无真实密钥可填占位如 `"..."`）。对一条 CREATED ack 调用 `POST /api/tms/acks/{id}/retry`：无密钥时预期 401/403，ack 变为 **FAILED_MANUAL**，`error_message` 可查；有密钥时预期 204 成功或按 Amazon 返回落 FAILED_MANUAL/FAILED_RETRYABLE。用于验证组包、SigV4 签名与 HTTP 请求正确，且 `ConfirmShipmentResult` 解析与落库正常。

---

## 前置条件

1. 执行迁移：`docs/sql/tms/20260220_p16_channel_shipment_ack.sql`、`docs/sql/oms/20260220_p16_plus_unified_order_item_amazon.sql`、`docs/sql/tms/20260220_p16pp_amazon_order_items_cache.sql`、`docs/sql/tms/20260220_p16pp_cd_ack_pack_selfheal.sql`、`docs/sql/wms/20260220_p16pp_wms_shipment_pack_id.sql`（可选，多包裹时）
2. 执行 IAM：`docs/sql/iam/20260220_tms_ack_menu_permissions.sql`；P1.6++ C+D 可选增量：`docs/sql/iam/20260220_tms_ack_p16pp_cd_permissions.sql`（`tms:ack:self-heal:retry`、`tms:ack:pack:view`）
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

- 菜单 **TMS → 回传任务**：列表展示订单号、tracking、carrier、shipDate、status、retryCount、nextRetryAt、lastError；P1.6++ 增加 **packageReferenceId**、**selfHealAttempted**/ **selfHealAction**
- **详情**：请求 payload（折叠）、响应码/响应体、错误码/错误信息、手动重试按钮；P1.6++ 增加包裹内 **item 分摊明细**（orderItems quantity）、selfHealAt
- **从出库单创建**：输入 outboundId，创建回传任务

---

## 8) P1.6+ 缺 orderItemId 自动回填 → confirmShipment 成功

- **准备**：一条 ack 对应的 **oms_unified_order_item** 的 `external_order_item_id` 为空；Mock 网关下 getOrderItems 返回的 SellerSKU 与订单行可匹配（如 `sku_code='MOCK-SKU'` 或 `amazon_seller_sku='MOCK-SKU'`）。
- **请求**：`POST /api/tms/acks/{ackId}/retry`
- **预期**：200；sendAck 内自动调用 getOrderItems，按优先级（amazon_seller_sku → sku_code → amazon_asin）匹配并回填 `external_order_item_id`（及 amazon_seller_sku/amazon_asin/amazon_quantity_ordered）；随后 confirmShipment 成功；ack 状态 → **SUCCESS**；日志可见 getOrderItems 耗时、匹配规则、回填前后。

---

## 9) P1.6+ 无法匹配 → FAILED_MANUAL + OMS Hold

- **准备**：订单行缺 `external_order_item_id`，且 getOrderItems 返回的 OrderItems 无法与本地行匹配（如 SKU/ASIN 均不一致），或 Mock 可配置为返回空列表/不匹配 SKU。
- **请求**：`POST /api/tms/acks/{ackId}/retry`
- **预期**：200；ack 状态 → **FAILED_MANUAL**；`error_code=OrderItemNotMatched`，`error_message` 含 "Amazon orderItemId not matched"；表 **oms_order_hold** 新增（或已有则跳过）一条：`hold_type=CHANNEL_ACK`，`reason_code=AMZ_ORDER_ITEM_NOT_MATCHED`，`external_order_id=amazon_order_id`，`status=OPEN`。

---

## 10) P1.6++ OMS 拉单阶段预回填 external_order_item_id

- **准备**：执行 **omsPullAmazonOrdersJobHandler**（shopId 必填），拉取的订单中 **oms_unified_order_item** 存在 `external_order_item_id` 为空的行；Mock 或 Real 下 getOrderItems 可返回并与 sku_code/platform_sku/asin 匹配。
- **预期**：拉单落库后，同一 amazonOrderId 仅调用一次 getOrderItems（去重），匹配并回填 `external_order_item_id`、amazon_seller_sku、amazon_asin、amazon_quantity_ordered；日志可见 matchRule、回填行数；后续 TMS sendAck 时不再触发补齐（因已填好）。
- **失败**：401/403 → 写 OMS hold（`hold_type=CHANNEL_DATA`，`reason_code=AMZ_AUTH_FAILED`）；匹配失败 → hold（`reason_code=AMZ_ORDER_ITEM_NOT_MATCHED`，reason_msg 含未匹配 sku/asin）。429/503 不写 hold。

---

## 11) P1.6++ 缓存命中（同一订单不重复调用 getOrderItems）

- **准备**：同一 amazon_order_id 在短时间（缓存 TTL 内）多次需要 orderItems（例如拉单预回填 + 同订单多条 ack 重试，或同一订单重复 retry）。
- **预期**：首次调用 getOrderItems 请求 Amazon 并写入 **amazon_order_items_cache**；后续请求命中缓存（`cacheHit=true`），不再请求 Amazon；日志可见 `cacheHit=true`、durationMs。限流：每 marketplace 并发可配置（如 2~5），超时或限流时可选返回旧缓存（stale-while-revalidate 最多 6h）。

---

## 12) P1.6++ C 多包裹：一单多 pack → 多条 ack，packageReferenceId=1/2，quantity 分摊正确

- **准备**：同一 outbound 存在多条 **wms_pack_order**（如 2 个 pack），每条 pack 有 **wms_pack_order_line**（sku_id, qty）；可选 **wms_shipment** 带 `pack_id` 与 pack 一一对应（多包裹时每包一条 shipment）；若仅一条 shipment 则两包共用同一 tracking。
- **请求**：`POST /api/tms/acks/createFromOutbound` Body: `{"outboundId": <该 outboundId>}`
- **预期**：200，返回第一个 ack 的 id；表 **channel_shipment_ack** 新增 **2 条**，同一 `amazon_order_id`，`package_reference_id` 分别为 `"1"`、`"2"`，`wms_pack_id` 分别对应两个 pack 的 id，`order_items_json` 为各包内 orderItemId+quantity 快照（按 pack_line 分摊到 fulfillment 行）。
- **发送**：对每条 ack 分别 `POST /api/tms/acks/{id}/retry`；每条 confirmShipment 请求的 packageDetail.packageReferenceId 与 orderItems 数量与对应包裹一致；两包可并行重试，乐观锁防重复。

---

## 13) P1.6++ D 自愈：carrier invalid / shipDate after now / orderItemId invalid → 自动修复一次后重试

- **carrier invalid**：Mock 配置返回 400，response body 或 message 含 "carrier"+"invalid"（或 "not supported"）；**请求**：`POST /api/tms/acks/{id}/retry`。**预期**：第一次 400 后自愈将 carrierCode 改为 "Other"、carrierName=原 carrier，重试一次；若 Mock 改为 204 则 SUCCESS；否则 FAILED_MANUAL；ack 表 `self_heal_attempted=1`，`self_heal_action=carrier_other`，`self_heal_at` 有值。
- **shipDate after now**：Mock 返回 400，message 含 "shipdate"+"after"（或 "future"）；**预期**：自愈将 shipDate 调整为 nowUtc-2s，重试一次；同上可观测 `self_heal_*`。
- **orderItemId invalid**：Mock 返回 400，message 含 "orderItemId"+"invalid"；**预期**：自愈触发 getOrderItemsAndBackfillForce（忽略 TTL），回填后若为多包裹则重新分配 order_items_json，重试一次；成功或进入 FAILED_MANUAL，`self_heal_action=order_item_refresh`。
- 自愈**仅一次**：同一 ack 已 `self_heal_attempted=1` 时再次 400 不再自愈，直接 FAILED_MANUAL。

---

## 14) P1.6++ D 404 延迟：第一次 404 → retryable，三次后 manual + hold

- **准备**：Mock 或 Real 下 confirmShipment 返回 **404**（order not found）。
- **请求**：对一条 CREATED ack 执行 `POST /api/tms/acks/{id}/retry`。
- **预期（第 1~2 次）**：ack 状态 → **FAILED_RETRYABLE**；`next_retry_at = now + 30min`；`retry_404_count` 递增（1、2）；不写 OMS hold。
- **预期（第 3 次及以后）**：ack 状态 → **FAILED_MANUAL**；`retry_404_count=3`；表 **oms_order_hold** 新增：`reason_code=AMZ_ORDER_NOT_FOUND_404`，`external_order_id=amazon_order_id`，`status=OPEN`。
- 通过 XXL-JOB 或手动在 next_retry_at 后再次 retry，重复 404 三次后确认 manual + hold。

---

## 15) Real 模式 Smoke Check（可选）

在 **gateway=real** 且已配置 `tms.amazon.spapi.*`（LWA + AWS 密钥）时：

- **有真实密钥**：对一条 CREATED ack 执行 `POST /api/tms/acks/{id}/retry`，预期 204 → SUCCESS；或根据 Amazon 返回得到 FAILED_MANUAL/FAILED_RETRYABLE，`error_message` 可观测。
- **无真实密钥（占位）**：配置 `gateway: real` 但密钥为占位（如 `"..."`），执行 retry 后预期 401/403；ack 状态为 **FAILED_MANUAL**，`response_code`/`error_code`/`error_message` 有值，便于确认组包、签名与请求已正确发出，且结果已正确解析并落库。

验收时 **mock 步骤 1–7 不变**，仍以 `tms.amazon.gateway=mock` 跑通即可。

---

## 检查点汇总

| 项 | 检查内容 |
|----|----------|
| 幂等创建 | 同一 outboundId 仅一条 ack（uk_ack） |
| confirmShipment | Mock 204 → SUCCESS；request_payload 含 marketplaceId、packageDetail、shipDate、orderItems |
| shipDate | ≤ nowUtc-2s，≥ orderPurchaseDateUtc（全 UTC，无 systemDefault） |
| 429/503 | FAILED_RETRYABLE + next_retry_at + 指数退避 |
| 400 | FAILED_MANUAL + OMS hold（CHANNEL_ACK, AMZ_CONFIRM_SHIPMENT_FAILED） |
| XXL-JOB | tmsAckAmazonShipmentJobHandler 扫描并重试 |
| 权限 | API + BUTTON 校验；ADMIN 可访问 |
| Real 模式 | 可选：real 下组包/签名/请求正确；无密钥时 401/403 → FAILED_MANUAL，error_message 可观测 |
| P1.6+ 回填 | 缺 external_order_item_id 时自动 getOrderItems，匹配回填后 confirmShipment；匹配规则：amazon_seller_sku → sku_code → amazon_asin，数量校验 |
| P1.6+ 不匹配 | 无法匹配 → FAILED_MANUAL + OMS hold（reason_code=AMZ_ORDER_ITEM_NOT_MATCHED） |
| P1.6++ 拉单预回填 | OMS 拉单后缺 external_order_item_id 时 getOrderItems 回填；401/403 → CHANNEL_DATA/AMZ_AUTH_FAILED；不匹配 → CHANNEL_DATA/AMZ_ORDER_ITEM_NOT_MATCHED |
| P1.6++ 缓存 | amazon_order_items_cache；命中 cacheHit=true；限流 per marketplace；429/503 不更新缓存 |
| P1.6++ C 多包裹 | 按 pack 生成多条 ack；packageReferenceId=1,2,...；order_items_json 按包分摊；confirmShipment 每包一条 |
| P1.6++ D 自愈 | 400 且可识别：shipDate/carrier/orderItemId 自动修复一次后重试；self_heal_* 可观测 |
| P1.6++ D 404 | 404 → FAILED_RETRYABLE，next_retry_at=now+30min，retry_404_count 最多 3 次；超 3 次 → FAILED_MANUAL + AMZ_ORDER_NOT_FOUND_404 hold |
