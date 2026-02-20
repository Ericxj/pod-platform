-- P1.6 Amazon confirmShipment 回传：channel_shipment_ack 表；OMS 补 marketplace_id / order_purchase_date_utc / external_order_item_id
SET NAMES utf8mb4;

-- 1) channel_shipment_ack：渠道发货回传任务/记录
CREATE TABLE IF NOT EXISTS `channel_shipment_ack` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint NULL,
  `updated_by` bigint NULL,
  `trace_id` varchar(64) NULL,
  `channel` varchar(32) NOT NULL COMMENT 'AMAZON/TEMU/SHEIN',
  `marketplace_id` varchar(32) NULL,
  `shop_id` bigint NULL,
  `amazon_order_id` varchar(128) NOT NULL COMMENT '平台订单ID',
  `external_order_id` varchar(128) NULL COMMENT '同 amazon_order_id 或扩展',
  `package_reference_id` varchar(32) NOT NULL DEFAULT '1' COMMENT '包裹序号(正数数值字符串)',
  `carrier_code` varchar(64) NULL,
  `carrier_name` varchar(128) NULL,
  `shipping_method` varchar(64) NULL,
  `tracking_no` varchar(128) NULL,
  `ship_date_utc` datetime(3) NULL COMMENT '发货时间UTC',
  `request_payload_json` text NULL COMMENT '请求体(脱敏可追溯)',
  `response_code` int NULL,
  `response_body` text NULL,
  `error_code` varchar(64) NULL,
  `error_message` varchar(1024) NULL,
  `status` varchar(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/SENDING/SUCCESS/FAILED_RETRYABLE/FAILED_MANUAL',
  `retry_count` int NOT NULL DEFAULT 0,
  `next_retry_at` datetime(3) NULL,
  `last_attempt_at` datetime(3) NULL,
  `business_idempotency_key` varchar(256) NULL COMMENT '幂等键',
  `wms_shipment_id` bigint NULL,
  `outbound_id` bigint NULL,
  `fulfillment_id` bigint NULL,
  `unified_order_id` bigint NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_ack` (`tenant_id`, `factory_id`, `channel`(32), `amazon_order_id`(128), `package_reference_id`(32)),
  INDEX `idx_status_next_retry` (`tenant_id`, `factory_id`, `status`(32), `next_retry_at`),
  INDEX `idx_order_id` (`tenant_id`, `factory_id`, `amazon_order_id`(128)),
  INDEX `idx_created_at` (`tenant_id`, `factory_id`, `created_at`),
  INDEX `idx_tenant_factory_deleted` (`tenant_id`, `factory_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='渠道发货回传任务';

-- 2) oms_unified_order：补 marketplace_id、order_purchase_date_utc（Amazon 回传与 shipDate 校验用）
SET @db = DATABASE();
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'oms_unified_order' AND COLUMN_NAME = 'marketplace_id');
SET @s = IF(@c = 0, 'ALTER TABLE `oms_unified_order` ADD COLUMN `marketplace_id` varchar(32) NULL COMMENT ''Amazon marketplaceId'' AFTER `extra_json`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'oms_unified_order' AND COLUMN_NAME = 'order_purchase_date_utc');
SET @s = IF(@c = 0, 'ALTER TABLE `oms_unified_order` ADD COLUMN `order_purchase_date_utc` datetime(3) NULL COMMENT ''订单下单时间UTC'' AFTER `marketplace_id`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 3) oms_unified_order_item：补 external_order_item_id（Amazon orderItemId）
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'oms_unified_order_item' AND COLUMN_NAME = 'external_order_item_id');
SET @s = IF(@c = 0, 'ALTER TABLE `oms_unified_order_item` ADD COLUMN `external_order_item_id` varchar(64) NULL COMMENT ''平台订单行ID(Amazon orderItemId)'' AFTER `extra_json`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 4) tms_shipment：补 ship_date_utc（若不存在）
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'tms_shipment' AND COLUMN_NAME = 'ship_date_utc');
SET @s = IF(@c = 0, 'ALTER TABLE `tms_shipment` ADD COLUMN `ship_date_utc` datetime(3) NULL COMMENT ''发货时间UTC'' AFTER `label_url`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SELECT 'p16 channel_shipment_ack + oms/tms fields applied' AS result;
