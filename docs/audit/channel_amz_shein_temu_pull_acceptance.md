# 渠道对接（Amazon / Shein / Temu）拉单验收

## 1. 范围

- **Amazon 拉单重构**：从 `plat_shop` + `plat_api_credential` 驱动，多店铺/多站点/多租户；token 自动刷新并写回；401/403 写入 OMS hold（CHANNEL_DATA / AMZ_AUTH_FAILED）；429/503 指数退避重试。
- **Shein / Temu**：授权以 `plat_api_credential` 承载；拉单客户端（Mock + Real 占位）；XXL-JOB 拉单闭环与 Amazon 对齐（upsertFromChannel -> SKU 映射 -> 全映射创建履约）。
- **系统管理**：平台/站点/店铺/授权（Prompt A 产物）作为数据来源；testConnection 当前为解密校验，可扩展为真实 API 调用。

## 2. 前置条件

- 已执行 `docs/sql/sys/20260220_sys_platform_site_shop_fx.sql`（plat_site、fx_rate、plat_shop/credential 增强）。
- 已执行 `docs/sql/iam/20260220_sys_platform_site_shop_fx_perm.sql`（菜单与权限）。
- 应用已引入 `pod-iam`、`pod-oms`，且 XXL-JOB 已注册三个 handler：`omsPullAmazonOrdersJobHandler`、`omsPullSheinOrdersJobHandler`、`omsPullTemuOrdersJobHandler`。

## 3. 配置 3 个 shop + credential

### 3.1 平台 / 站点 / 店铺

- **AMAZON**：在系统管理 -> 平台管理中新增平台 AMAZON；站点管理中新增站点（如 US、UK）；店铺管理中新增店铺，platform_code=AMAZON，site_code=US（或对应站点），shop_code 唯一。
- **SHEIN**：新增平台 SHEIN；新增店铺，platform_code=SHEIN。
- **TEMU**：新增平台 TEMU；新增店铺，platform_code=TEMU。

记录各店铺 ID（shopId），供 job 参数与授权使用。

### 3.2 平台授权

- **Amazon**：在平台授权中新增授权，选择平台 AMAZON、店铺（上述 shopId）、认证类型 OAUTH。payloadPlainJson 示例（明文，提交后后端加密）：
  ```json
  {
    "refresh_token": "xxx",
    "client_id": "yyy",
    "client_secret": "zzz",
    "aws_access_key": "AKIA...",
    "aws_secret_key": "...",
    "selling_region": "NA"
  }
  ```
  可选：`endpoint`、`region`、`access_token`、`expires_at`（刷新后由拉单写回）。
- **Shein / Temu**：新增授权，认证类型 TOKEN 或 KEY_PAIR，payloadPlainJson 按渠道要求（如 appKey、appSecret、token 等）。当前 Real 客户端为占位，仅校验凭证存在并解密成功。

## 4. 执行 XXL-JOB

### 4.1 Amazon

- Job：`omsPullAmazonOrdersJobHandler`
- 参数示例：`shopId=1` 或 `shopId=1,lastUpdatedAfter=2026-02-01 00:00:00,lastUpdatedBefore=2026-02-14 23:59:59`
- 使用 **real** 客户端时需配置：`oms.amazon.client=real`，并确保对应 shop 的 credential 存在且 ENABLED。
- **预期**：
  - 成功：订单落库 oms_unified_order / oms_unified_order_item；若存在 plat_sku_mapping（channel=AMAZON, shopId, externalSku）则 sku_id 回填；全行映射则创建履约。
  - 401/403：该 shop 写入 oms_order_hold（hold_type=CHANNEL_DATA, reason_code=AMZ_AUTH_FAILED），job 返回失败信息。
  - 无订单时：拉取 0 条，job 成功。

### 4.2 Shein

- Job：`omsPullSheinOrdersJobHandler`
- 参数：`shopId=<SHEIN 店铺 ID>`
- Mock：`oms.shein.client=mock`（默认）返回 1 条模拟订单；Real：`oms.shein.client=real` 当前返回空列表（占位）。
- **预期**：Mock 下订单落库；SKU 未映射时产生 hold（SKU_MAPPING_NOT_FOUND）；全映射则创建履约。

### 4.3 Temu

- Job：`omsPullTemuOrdersJobHandler`
- 参数：`shopId=<TEMU 店铺 ID>`
- Mock：`oms.temu.client=mock`（默认）返回 1 条模拟订单；Real：`oms.temu.client=real` 占位返回空列表。
- **预期**：同 Shein。

## 5. 订单入库与 SKU 映射

- 统一走 `UnifiedOrderApplicationService.upsertFromChannel(requestId, channel, shopId, dto)`。
- `SkuMappingResolver.resolveOrHold(channel, shopId, externalOrderId, externalSku, ...)`：有映射则回填 skuId/skuCode；无映射则写入 oms_order_hold（hold_type=SKU_MAPPING, reason_code=SKU_MAPPING_NOT_FOUND）。
- 全行已映射时，job 调用 `FulfillmentApplicationService.createFromUnifiedOrder(unifiedOrderId)` 创建履约。

## 6. Amazon token 自动刷新与写回

- RealAmazonOrdersClient 在拉单时：若 credential 中无有效 access_token 或已过期，则调用 LWA refresh_token 接口获取新 access_token，并将新 payload（含 access_token、expires_at）加密后通过 `CredentialApplicationService.updateCredentialPayloadAfterRefresh` 写回 plat_api_credential。
- 内存缓存：按 shopId 缓存 access_token，在过期前 60 秒内复用，减少解密与刷新次数。

## 7. testConnection

- 系统管理 -> 平台授权 -> 测试连接：当前调用 `POST /api/sys/credentials/{id}/test`，后端仅做**解密校验**（解密成功即返回 true）。
- 可观测：解密失败或凭证不存在时返回 false，前端可提示“连接失败”。后续可扩展为 Amazon 调用 getMarketplaceParticipations、Shein/Temu 调用轻量 API 做真实连通性校验。

## 8. 权限

- 拉单为 XXL-JOB 后台执行，使用当前任务上下文的 tenantId/factoryId；接口权限见 `docs/sql/iam/20260220_sys_platform_site_shop_fx_perm.sql`。
- 若提供手动触发拉单 API（如 POST /api/oms/pull/amazon、/shein、/temu），需为对应 path 补齐 API 权限并授权 ADMIN。

## 9. 验收检查表

| 项 | 说明 | 结果 |
|----|------|------|
| 平台/站点/店铺/授权 | 系统管理下可配置 AMAZON/SHEIN/TEMU 店铺与凭证 | |
| Amazon 拉单 real | shopId + credential 驱动，getOrders + getOrderItems，订单与行落库 | |
| Amazon 401/403 | 写入 hold AMZ_AUTH_FAILED，job 失败可观测 | |
| Amazon token 刷新 | 过期后自动 refresh 并写回 credential | |
| Shein job | Mock 拉单 -> 落库 -> 映射/hold -> 全映射创建履约 | |
| Temu job | 同 Shein | |
| SKU 映射 | channel + shopId + externalSku 对应 prd_sku_mapping，未映射 hold | |
| testConnection | 解密成功返回 true，失败可观测 | |
