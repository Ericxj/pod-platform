-- P1.6++ getOrderItems 缓存表，降低调用量、防 429
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `amazon_order_items_cache` (
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
  `amazon_order_id` varchar(128) NOT NULL COMMENT '平台订单ID',
  `marketplace_id` varchar(32) NULL DEFAULT NULL COMMENT 'marketplaceId',
  `payload_json` text NULL COMMENT 'orderItems payload(脱敏可追溯)',
  `fetched_at` datetime(3) NOT NULL COMMENT '拉取时间',
  `expire_at` datetime(3) NOT NULL COMMENT '过期时间',
  `status` varchar(16) NOT NULL DEFAULT 'VALID' COMMENT 'VALID/EXPIRED',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_marketplace` (`tenant_id`, `factory_id`, `amazon_order_id`(128), `marketplace_id`(32)),
  INDEX `idx_expire_at` (`tenant_id`, `factory_id`, `expire_at`),
  INDEX `idx_status` (`tenant_id`, `factory_id`, `status`(16)),
  INDEX `idx_tenant_factory_deleted` (`tenant_id`, `factory_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Amazon getOrderItems 缓存';

SELECT 'p16++ amazon_order_items_cache created' AS result;
