-- prd_sku_mapping: 平台 SKU 映射（Amazon/Temu/Shein）
-- 唯一索引 uk_mapping(tenant_id, factory_id, channel, shop_id, external_sku, deleted)
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `prd_sku_mapping` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint NULL DEFAULT NULL,
  `updated_by` bigint NULL DEFAULT NULL,
  `trace_id` varchar(64) NULL DEFAULT NULL,
  `sku_id` bigint NULL DEFAULT NULL COMMENT '内部SKU ID',
  `sku_code` varchar(64) NULL DEFAULT NULL COMMENT '内部SKU编码',
  `channel` varchar(32) NOT NULL COMMENT 'AMAZON/TEMU/SHEIN等',
  `shop_id` varchar(64) NOT NULL DEFAULT '' COMMENT '店铺ID',
  `external_sku` varchar(128) NOT NULL COMMENT '平台SKU',
  `external_name` varchar(255) NULL DEFAULT NULL,
  `remark` varchar(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_mapping` (`tenant_id`, `factory_id`, `channel`, `shop_id`, `external_sku`),
  INDEX `idx_tenant_factory_deleted` (`tenant_id`, `factory_id`, `deleted`),
  INDEX `idx_mapping_sku` (`tenant_id`, `sku_id`),
  INDEX `idx_mapping_channel_shop` (`tenant_id`, `channel`, `shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台SKU映射';
