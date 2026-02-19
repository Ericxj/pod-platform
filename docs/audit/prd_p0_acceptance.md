# 商品域 P0 验收脚本

前置：执行 `docs/sql/prd/20260220_prd_sku_mapping.sql` 创建表；执行 `docs/sql/iam/20260220_prd_menu_permissions.sql` 初始化菜单与权限。  
请求头：`Authorization: Bearer <token>`，`X-Tenant-Id: 1`，`X-Factory-Id: 1`，`X-Request-Id: <uuid>`（写操作必填）。  
Base URL: `http://localhost:8080`（或实际后端地址）。

---

## 步骤 1：创建 SPU

```http
POST /api/prd/spu HTTP/1.1
Host: localhost:8080
Content-Type: application/json
X-Request-Id: req-prd-001
X-Tenant-Id: 1
X-Factory-Id: 1
Authorization: Bearer <token>

{"spuCode":"SPU-TSHIRT","spuName":"POD T-Shirt","categoryCode":"APPAREL","brand":"DEMO"}
```

**预期**：`code: 200`，`data.id` 有值，`data.spuCode: "SPU-TSHIRT"`，`data.status: "DRAFT"`。

---

## 步骤 2：创建 SKU（sku_code=TSHIRT-001-WHITE-M）

假设 SPU id=3001（或上一步返回的 id）。

```http
POST /api/prd/sku HTTP/1.1
Content-Type: application/json
X-Request-Id: req-prd-002
X-Tenant-Id: 1
X-Factory-Id: 1
Authorization: Bearer <token>

{"spuId":3001,"skuCode":"TSHIRT-001-WHITE-M","skuName":"T-Shirt White M","price":19.99,"weightG":200,"attributesJson":"{\"size\":\"M\",\"color\":\"white\"}"}
```

**预期**：`code: 200`，`data.skuCode: "TSHIRT-001-WHITE-M"`，`data.status: "DRAFT"`。记下 `data.id` 为 skuId。

---

## 步骤 3：加条码 BC1234567890

```http
POST /api/prd/barcode/batchAdd HTTP/1.1
Content-Type: application/json
X-Request-Id: req-prd-003
X-Tenant-Id: 1
X-Factory-Id: 1
Authorization: Bearer <token>

{"skuId":<skuId>,"barcodes":["BC1234567890"],"barcodeType":"EAN","isPrimary":1}
```

**预期**：`code: 200`。重复相同 X-Request-Id 不重复插入（幂等）。

---

## 步骤 4：配 BOM（2 行）

需物料 ID（如 5001、5002）。若无物料可先插入 prd_material 或使用已有 material_id。

```http
POST /api/prd/bom/save HTTP/1.1
Content-Type: application/json
X-Request-Id: req-prd-004
X-Tenant-Id: 1
X-Factory-Id: 1
Authorization: Bearer <token>

{"skuId":<skuId>,"versionNo":1,"remark":"P0 BOM","items":[{"materialId":5001,"qty":1.5,"uom":"PCS","lossRate":0,"sortNo":1},{"materialId":5002,"qty":0.2,"uom":"M","lossRate":0.01,"sortNo":2}]}
```

**预期**：`code: 200`，`data.status: "DRAFT"`。记下 `data.id` 为 bomId。

---

## 步骤 5：发布 BOM

```http
POST /api/prd/bom/<bomId>/publish HTTP/1.1
X-Request-Id: req-prd-005
X-Tenant-Id: 1
X-Factory-Id: 1
Authorization: Bearer <token>
```

**预期**：`code: 200`。再 GET `/api/prd/bom/<bomId>` 得 `status: "PUBLISHED"`。

---

## 步骤 6：配工艺路线（3 步）

```http
POST /api/prd/routing/save HTTP/1.1
Content-Type: application/json
X-Request-Id: req-prd-006
X-Tenant-Id: 1
X-Factory-Id: 1
Authorization: Bearer <token>

{"skuId":<skuId>,"versionNo":1,"steps":[{"stepNo":1,"opCode":"PRINT","opName":"印花","equipmentType":null,"stdCycleSeconds":60,"qcRequired":0},{"stepNo":2,"opCode":"CUT","opName":"裁剪","equipmentType":null,"stdCycleSeconds":30,"qcRequired":0},{"stepNo":3,"opCode":"PACK","opName":"包装","equipmentType":null,"stdCycleSeconds":15,"qcRequired":1}]}
```

**预期**：`code: 200`，`data.status: "DRAFT"`。记下 `data.id` 为 routingId。

---

## 步骤 7：发布工艺路线

```http
POST /api/prd/routing/<routingId>/publish HTTP/1.1
X-Request-Id: req-prd-007
X-Tenant-Id: 1
X-Factory-Id: 1
Authorization: Bearer <token>
```

**预期**：`code: 200`。

---

## 步骤 8：绑定 Amazon 映射

```http
POST /api/prd/mapping HTTP/1.1
Content-Type: application/json
X-Request-Id: req-prd-008
X-Tenant-Id: 1
X-Factory-Id: 1
Authorization: Bearer <token>

{"channel":"AMAZON","shopId":"xxx","externalSku":"AMZ-SELLER-001","externalName":"Amazon listing","skuCode":"TSHIRT-001-WHITE-M","remark":"P0 mapping"}
```

**注意**：SKU 需先激活（步骤 9），否则会报 "SKU must be ACTIVE to bind mapping"。若先激活再绑映射，则上述 body 中 `skuCode` 为已存在的 ACTIVE SKU。

---

## 步骤 9：激活 SKU

```http
POST /api/prd/sku/<skuId>/activate HTTP/1.1
X-Request-Id: req-prd-009
X-Tenant-Id: 1
X-Factory-Id: 1
Authorization: Bearer <token>
```

**预期**：`code: 200`。再 GET `/api/prd/sku/<skuId>` 得 `status: "ACTIVE"`。

---

## 步骤 10：绑定平台映射（在 SKU 激活后）

同步骤 8，再发一次。**预期**：`code: 200`，`data.channel: "AMAZON"`，`data.externalSku: "AMZ-SELLER-001"`。

---

## 验收要点

| 项 | 说明 |
|----|------|
| 唯一约束 | 同一 tenant_id+factory_id 下 spu_code/sku_code/barcode/(channel,shop_id,external_sku) 唯一；重复创建返回 409 或业务提示。 |
| tenant/factory 过滤 | 查询与更新均带 tenant_id、factory_id，且 deleted=0。 |
| 幂等 | 写接口带 X-Request-Id，重复相同 Request-Id 返回 409 或成功（不重复落库）。 |
| BOM 发布 | items 非空、用量>0 方可 publish。 |
| Routing 发布 | step_no 连续、op_code 非空方可 publish。 |
| SKU 激活 | 条码>=1、BOM/Routing 可配置校验；默认条码>=1 即可激活。 |

---

## 核对 SQL（示例）

```sql
SELECT id, spu_code, status FROM prd_spu WHERE deleted=0 AND tenant_id=1 AND factory_id=1;
SELECT id, sku_code, status FROM prd_sku WHERE deleted=0 AND tenant_id=1 AND factory_id=1;
SELECT id, sku_id, barcode FROM prd_sku_barcode WHERE deleted=0 AND tenant_id=1 AND factory_id=1;
SELECT id, sku_id, status FROM prd_bom WHERE deleted=0 AND tenant_id=1 AND factory_id=1;
SELECT id, sku_id, status FROM prd_routing WHERE deleted=0 AND tenant_id=1 AND factory_id=1;
SELECT id, sku_id, channel, shop_id, external_sku FROM prd_sku_mapping WHERE deleted=0 AND tenant_id=1 AND factory_id=1;
```
