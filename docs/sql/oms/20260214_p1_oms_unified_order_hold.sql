-- P1.1 OMS: 补齐 oms_unified_order 字段与索引，订单行 sku_id 可空，新增异常队列表 oms_order_hold
-- 执行一次即可；若列/索引已存在请注释对应 ALTER。
SET NAMES utf8mb4;

-- 1) oms_unified_order: 增加 channel / external_order_id（与 platform_code / platform_order_id 对齐）
-- 若列已存在请注释下面一行
ALTER TABLE `oms_unified_order`
  ADD COLUMN `channel` varchar(32) NULL DEFAULT NULL COMMENT '渠道 AMAZON/TEMU/SHEIN' AFTER `trace_id`,
  ADD COLUMN `external_order_id` varchar(128) NULL DEFAULT NULL COMMENT '平台订单ID' AFTER `channel`;

UPDATE `oms_unified_order` SET `channel` = COALESCE(`channel`, `platform_code`), `external_order_id` = COALESCE(`external_order_id`, `platform_order_id`) WHERE 1=1;

-- 唯一索引 uk_order（与 uk_platform_order 等价；若存在 uk_platform_order 请先执行: DROP INDEX uk_platform_order ON oms_unified_order;）
ALTER TABLE `oms_unified_order` ADD UNIQUE INDEX `uk_order` (`tenant_id`, `factory_id`, `channel`(32), `shop_id`, `external_order_id`(128));

-- 索引：按状态、按最后更新时间
ALTER TABLE `oms_unified_order` ADD INDEX `idx_order_status` (`tenant_id`, `factory_id`, `order_status`);
ALTER TABLE `oms_unified_order` ADD INDEX `idx_last_updated_at` (`tenant_id`, `factory_id`, `updated_at`);

-- 2) oms_unified_order_item: sku_id 允许 NULL（映射失败时先落行后补）
ALTER TABLE `oms_unified_order_item` MODIFY COLUMN `sku_id` bigint NULL DEFAULT NULL COMMENT 'SKU ID(映射失败时可空)';

-- 3) 通用异常队列表 oms_order_hold
CREATE TABLE IF NOT EXISTS `oms_order_hold` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint NULL DEFAULT NULL,
  `updated_by` bigint NULL DEFAULT NULL,
  `trace_id` varchar(64) NULL DEFAULT NULL,
  `hold_type` varchar(32) NOT NULL COMMENT 'SKU_MAPPING等',
  `status` varchar(16) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN/RESOLVED',
  `reason_code` varchar(64) NULL DEFAULT NULL,
  `reason_msg` varchar(512) NULL DEFAULT NULL,
  `channel` varchar(32) NOT NULL,
  `shop_id` varchar(64) NOT NULL DEFAULT '',
  `external_order_id` varchar(128) NOT NULL,
  `external_sku` varchar(128) NOT NULL,
  `unified_order_id` bigint NULL DEFAULT NULL COMMENT '关联统一订单ID',
  `unified_order_item_id` bigint NULL DEFAULT NULL COMMENT '关联订单行ID',
  `resolve_sku_id` bigint NULL DEFAULT NULL,
  `resolve_sku_code` varchar(64) NULL DEFAULT NULL,
  `resolved_at` datetime(3) NULL DEFAULT NULL,
  `resolved_by` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_hold` (`tenant_id`, `factory_id`, `hold_type`, `channel`, `shop_id`, `external_order_id`, `external_sku`),
  INDEX `idx_status_created_at` (`tenant_id`, `factory_id`, `status`, `created_at`),
  INDEX `idx_channel_shop` (`tenant_id`, `factory_id`, `channel`, `shop_id`),
  INDEX `idx_tenant_factory_deleted` (`tenant_id`, `factory_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OMS异常队列表(SKU映射失败等)';

SELECT 'oms p1.1 migration applied' AS result;
