# pod-prd 商品域

P0 可商用：SPU/SKU、条码、BOM、工艺路线、平台 SKU 映射（Amazon/Temu/Shein），接入 IAM 与多租户/多工厂。

## 技术栈

- **后端**：Java 21 + Spring Boot 3.x，MyBatis-Plus，MySQL 8（utf8mb4_0900_ai_ci）；无 Lombok；无外键；表含 id, tenant_id, factory_id, created_at, updated_at, deleted, version, created_by, updated_by, trace_id
- **前端**：Vben Admin Antd（Vue3 + TypeScript），动态路由来自 IAM 菜单，权限码控制按钮可见性

## 领域模型（富模型）

- **Spu**：update()
- **Sku**：activate(条码/BOM/Routing 校验)、deactivate()、update()
- **Bom**：publish(items 非空、用量>0)、unpublish()
- **Routing**：publish(step 连续、op_code 非空)、unpublish()
- **SkuMapping**：bind/unbind/update；externalSku 在 (tenant,factory,channel,shopId) 唯一；绑定时 SKU 需 ACTIVE

## API

- SPU：`GET /api/prd/spu/page`，`GET /api/prd/spu/{id}`，`POST /api/prd/spu`，`PUT /api/prd/spu/{id}`
- SKU：`GET /api/prd/sku/page`，`GET /api/prd/sku/{id}`，`POST /api/prd/sku`，`PUT /api/prd/sku/{id}`，`POST /api/prd/sku/{id}/activate`，`POST /api/prd/sku/{id}/deactivate`
- Barcode：`GET /api/prd/barcode/list?skuId=`，`POST /api/prd/barcode/batchAdd`，`DELETE /api/prd/barcode/{id}`
- BOM：`GET /api/prd/bom/{id}`，`GET /api/prd/bom/{id}/items`，`POST /api/prd/bom/save`，`POST /api/prd/bom/{id}/publish`，`POST /api/prd/bom/{id}/unpublish`
- Routing：`GET /api/prd/routing/{id}`，`GET /api/prd/routing/{id}/steps`，`POST /api/prd/routing/save`，`POST /api/prd/routing/{id}/publish`，`POST /api/prd/routing/{id}/unpublish`
- Mapping：`GET /api/prd/mapping/page`，`POST /api/prd/mapping`，`PUT /api/prd/mapping/{id}`，`DELETE /api/prd/mapping/{id}`

## 权限码

- prd:spu:page, prd:spu:get, prd:spu:create, prd:spu:update
- prd:sku:page, prd:sku:get, prd:sku:create, prd:sku:update, prd:sku:activate, prd:sku:deactivate
- prd:barcode:list, prd:barcode:batchAdd, prd:barcode:delete
- prd:bom:get, prd:bom:save, prd:bom:publish, prd:bom:unpublish
- prd:routing:get, prd:routing:save, prd:routing:publish, prd:routing:unpublish
- prd:mapping:page, prd:mapping:create, prd:mapping:update, prd:mapping:delete

## 数据库

- 表：prd_spu, prd_sku, prd_sku_barcode, prd_bom, prd_bom_item, prd_routing, prd_routing_step, prd_sku_mapping（见 DDL 与 `docs/sql/prd/20260220_prd_sku_mapping.sql`）
- 迁移：执行 `docs/sql/prd/20260220_prd_sku_mapping.sql` 创建 prd_sku_mapping
- 菜单/权限：执行 `docs/sql/iam/20260220_prd_menu_permissions.sql`

## 幂等

- 写接口依赖请求头 `X-Request-Id`，由 pod-infra IdempotentService 做幂等（同一 Request-Id 重复请求返回 409 或视为已处理）。

## 验收

见 `docs/audit/prd_p0_acceptance.md`：创建 SPU -> 创建 SKU -> 加条码 -> 配 BOM(2 行) -> 配 Routing(3 步) -> 发布 BOM/Routing -> 激活 SKU -> 绑定平台映射。
