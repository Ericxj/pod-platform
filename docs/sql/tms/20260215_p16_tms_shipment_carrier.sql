-- P1.6 TMS 最小版：tms_carrier / tms_carrier_service / tms_channel_carrier_mapping；tms_shipment uk_source + 字段
SET NAMES utf8mb4;

-- 1) tms_carrier（若主库已有则跳过；此处仅保证存在）
CREATE TABLE IF NOT EXISTS `tms_carrier` (
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
  `carrier_code` varchar(64) NOT NULL COMMENT '承运商编码',
  `carrier_name` varchar(128) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `config_json` json NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_carrier_code` (`tenant_id`, `carrier_code`(64)),
  INDEX `idx_tenant_factory_deleted` (`tenant_id`, `factory_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='承运商';

-- 2) tms_carrier_service
CREATE TABLE IF NOT EXISTS `tms_carrier_service` (
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
  `carrier_id` bigint NOT NULL,
  `service_code` varchar(64) NOT NULL COMMENT '服务编码',
  `service_name` varchar(128) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_carrier_service` (`tenant_id`, `carrier_id`, `service_code`(64)),
  INDEX `idx_tenant_factory_deleted` (`tenant_id`, `factory_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='承运商服务';

-- 3) tms_channel_carrier_mapping（渠道-承运商映射）
CREATE TABLE IF NOT EXISTS `tms_channel_carrier_mapping` (
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
  `channel_code` varchar(32) NOT NULL COMMENT '渠道(AMAZON等)',
  `carrier_code` varchar(64) NOT NULL,
  `service_code` varchar(64) NULL COMMENT '可选',
  `carrier_id` bigint NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_channel_carrier` (`tenant_id`, `factory_id`, `channel_code`(32), `carrier_code`(64)),
  INDEX `idx_tenant_factory_deleted` (`tenant_id`, `factory_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='渠道-承运商映射';

-- 4) tms_shipment：增加 source_type/source_no/carrier_code/service_code/fail_reason，uk_source，idx_status（可重复执行）
SET @db = DATABASE();
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'tms_shipment' AND COLUMN_NAME = 'source_type');
SET @sql = IF(@exist = 0, 'ALTER TABLE `tms_shipment` ADD COLUMN `source_type` varchar(32) NULL COMMENT ''来源类型'' AFTER `trace_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'tms_shipment' AND COLUMN_NAME = 'source_no');
SET @sql = IF(@exist = 0, 'ALTER TABLE `tms_shipment` ADD COLUMN `source_no` varchar(64) NULL COMMENT ''来源单号'' AFTER `source_type`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'tms_shipment' AND COLUMN_NAME = 'carrier_code');
SET @sql = IF(@exist = 0, 'ALTER TABLE `tms_shipment` ADD COLUMN `carrier_code` varchar(64) NULL COMMENT ''承运商编码'' AFTER `method_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'tms_shipment' AND COLUMN_NAME = 'service_code');
SET @sql = IF(@exist = 0, 'ALTER TABLE `tms_shipment` ADD COLUMN `service_code` varchar(64) NULL COMMENT ''服务编码'' AFTER `carrier_code`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'tms_shipment' AND COLUMN_NAME = 'fail_reason');
SET @sql = IF(@exist = 0, 'ALTER TABLE `tms_shipment` ADD COLUMN `fail_reason` varchar(512) NULL COMMENT ''失败原因'' AFTER `status`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- uk_source（先检查再添加）
SET @idx := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'tms_shipment' AND INDEX_NAME = 'uk_source');
SET @sql = IF(@idx = 0, 'ALTER TABLE `tms_shipment` ADD UNIQUE INDEX `uk_source` (`tenant_id`, `factory_id`, `source_type`(32), `source_no`(64))', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_status（若已有 idx_ship_status 可复用，这里加 idx_status 名）
SET @idx := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'tms_shipment' AND INDEX_NAME = 'idx_status');
SET @sql = IF(@idx = 0, 'ALTER TABLE `tms_shipment` ADD INDEX `idx_status` (`tenant_id`, `status`(32), `created_at`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- carrier_id 允许 NULL（从 WMS 创建时可能尚未选承运商）
ALTER TABLE `tms_shipment` MODIFY COLUMN `carrier_id` bigint NULL COMMENT '承运商ID';

SELECT 'p16 tms carrier + shipment uk_source migration applied' AS result;
