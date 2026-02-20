# 系统管理增强 — 验收脚本（平台/站点/店铺/授权/汇率）

## 前置条件

1. 执行 DDL：`docs/sql/sys/20260220_sys_platform_site_shop_fx.sql`
2. 执行 IAM：`docs/sql/iam/20260220_sys_platform_site_shop_fx_perm.sql`
3. 登录用户具备 ADMIN 角色（role_id=1），或已授予对应 sys:platform / sys:site / sys:shop / sys:credential / sys:fx 权限

---

## 1) 平台管理 CRUD

- **列表**：`GET /api/sys/platforms?current=1&size=10`，Header：租户/工厂/Authorization。预期：200，分页数据。
- **详情**：`GET /api/sys/platforms/{id}`。预期：200，单条平台。
- **新增**：`POST /api/sys/platforms`，Body：`{"platformCode":"TEMU","platformName":"Temu","status":"ENABLED"}`。预期：200；再次同 platformCode 报错「platform_code already exists」。
- **编辑**：`PUT /api/sys/platforms/{id}`，Body：`{"platformName":"Temu Global"}`。预期：200。
- **启用/禁用**：`POST /api/sys/platforms/{id}/enable`、`POST /api/sys/platforms/{id}/disable`。预期：200，status 变更。

---

## 2) 站点管理 CRUD

- **列表**：`GET /api/sys/sites?platformCode=AMAZON`。预期：200，可筛选平台。
- **新增**：`POST /api/sys/sites`，Body：`{"platformCode":"AMAZON","siteCode":"DE","siteName":"Germany","countryCode":"DE","currency":"EUR","status":"ENABLED"}`。预期：200。
- **禁用**：对有关联 ENABLED 店铺的站点调用 `POST /api/sys/sites/{id}/disable`。预期：业务错误「Cannot disable site: N active shop(s) reference this site」。无关联或店铺已禁用后可禁用站点。

---

## 3) 店铺管理 CRUD

- **列表**：`GET /api/sys/shops?platformCode=AMAZON&siteCode=US`。预期：200。
- **新增**：先确保平台 ENABLED、站点（若填 siteCode）存在且 ENABLED。`POST /api/sys/shops`，Body：`{"platformCode":"AMAZON","shopCode":"AMZ_US_2","shopName":"US Shop 2","siteCode":"US","currency":"USD","status":"ENABLED"}`。预期：200。同 tenant+platform+shop_code 再次新增报错。
- **编辑/启用/禁用**：与平台、站点类似，调用 PUT、enable、disable。预期：200。

---

## 4) 平台授权 CRUD + 测试连接

- **列表**：`GET /api/sys/credentials?platformCode=AMAZON&shopId=2001`。预期：200；返回体中无明文 access_token/secret，仅有 payloadMasked（如 ***abcd）。
- **新增**：`POST /api/sys/credentials`，Body：`{"platformCode":"AMAZON","shopId":2001,"authType":"OAUTH","credentialName":"US OAuth","payloadPlainJson":"{\"refresh_token\":\"xxx\"}","status":"ENABLED"}`。预期：200；库中 encrypted_payload 为加密后内容，非明文。
- **详情**：`GET /api/sys/credentials/{id}`。预期：200；仅返回 payloadMasked，不返回明文。
- **测试连接**：`POST /api/sys/credentials/{id}/test`。预期：200，data 为 true/false（当前实现为解密成功即 true）。

---

## 5) 汇率管理 CRUD + 查询最近有效

- **列表**：`GET /api/sys/fx-rates?baseCurrency=USD&quoteCurrency=CNY`。预期：200。
- **新增**：`POST /api/sys/fx-rates`，Body：`{"baseCurrency":"USD","quoteCurrency":"CNY","rate":7.25,"effectiveDate":"2026-02-20","source":"MANUAL","status":"ENABLED"}`。预期：200。同 tenant+base+quote+effective_date 再次新增报错。
- **汇率查询**：`GET /api/sys/fx-rates/quote?base=USD&quote=CNY&date=2026-02-20`。预期：200，data 为 BigDecimal；若无当日数据则取 effective_date <= date 的最近一条。若无任何有效数据：业务错误「No fx rate for USD/CNY on or before ...」。

---

## 6) 权限

- **无权限用户**：调用 `GET /api/sys/platforms` 等未带对应权限 → 403（Permission Denied）。
- **ADMIN**：拥有 sys:platform:* / sys:site:* / sys:shop:* / sys:credential:* / sys:fx:* 及对应 API 权限，可访问所有菜单与接口。

---

## 检查点汇总

| 项 | 检查内容 |
|----|----------|
| 平台 | CRUD + enable/disable；platform_code 唯一 |
| 站点 | CRUD + enable/disable；禁用时若有 ENABLED 店铺引用则阻止 |
| 店铺 | CRUD + enable/disable；platform/site 存在且 ENABLED；shop_code 在 tenant+platform 唯一 |
| 授权 | CRUD + test；明文仅 payloadPlainJson 入参，落库加密；GET 仅返回 payloadMasked |
| 汇率 | CRUD + enable/disable；quote 接口当日无则取最近 effective_date |
| IAM | 菜单/按钮/API 权限；ADMIN 全量授权 |
