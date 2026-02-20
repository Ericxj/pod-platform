-- 系统管理增强：plat_site、fx_rate、plat_shop/plat_api_credential 增强
-- 幂等：判断列/索引存在再执行
SET NAMES utf8mb4;
SET @db = DATABASE();

-- ========== A) plat_site 站点管理 ==========
CREATE TABLE IF NOT EXISTS `plat_site` (
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
  `platform_code` varchar(32) NOT NULL COMMENT 'AMAZON/TEMU/SHEIN',
  `site_code` varchar(32) NOT NULL COMMENT 'US/DE/UK',
  `site_name` varchar(64) NOT NULL,
  `country_code` varchar(8) NULL,
  `currency` varchar(8) NULL,
  `timezone` varchar(64) NULL,
  `status` varchar(16) NOT NULL COMMENT 'ENABLED/DISABLED',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_site` (`tenant_id`, `platform_code`(32), `site_code`(32)),
  INDEX `idx_platform_status` (`tenant_id`, `platform_code`(32), `status`(16))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站点管理';

-- ========== B) fx_rate 汇率（系统管理用，与 fin_fx_rate 可并存） ==========
CREATE TABLE IF NOT EXISTS `fx_rate` (
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
  `base_currency` varchar(8) NOT NULL,
  `quote_currency` varchar(8) NOT NULL,
  `rate` decimal(18,8) NOT NULL,
  `effective_date` date NOT NULL,
  `source` varchar(32) NULL COMMENT 'MANUAL/IMPORT/API',
  `status` varchar(16) NOT NULL COMMENT 'ENABLED/DISABLED',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_rate` (`tenant_id`, `base_currency`(8), `quote_currency`(8), `effective_date`),
  INDEX `idx_pair_date` (`tenant_id`, `base_currency`(8), `quote_currency`(8), `effective_date` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='汇率';

-- ========== C) plat_shop 增强：status 统一 ENABLED/DISABLED，新增索引 ==========
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'plat_shop' AND COLUMN_NAME = 'status');
SET @s = IF(@c > 0, 'UPDATE `plat_shop` SET `status` = ''ENABLED'' WHERE `status` = ''ACTIVE'' AND deleted = 0; UPDATE `plat_shop` SET `status` = ''DISABLED'' WHERE `status` = ''INACTIVE'' AND deleted = 0', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @idx = (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'plat_shop' AND INDEX_NAME = 'idx_platform_site');
SET @s = IF(@idx = 0, 'ALTER TABLE `plat_shop` ADD INDEX `idx_platform_site` (`tenant_id`, `platform_code`(32), `site_code`(32), `status`(16))', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- ========== D) plat_api_credential 增强 ==========
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'plat_api_credential' AND COLUMN_NAME = 'credential_name');
SET @s = IF(@c = 0, 'ALTER TABLE `plat_api_credential` ADD COLUMN `credential_name` varchar(64) NULL COMMENT ''前端展示名'' AFTER `auth_type`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'plat_api_credential' AND COLUMN_NAME = 'refresh_expires_at');
SET @s = IF(@c = 0, 'ALTER TABLE `plat_api_credential` ADD COLUMN `refresh_expires_at` datetime(3) NULL COMMENT ''refresh token 过期时间'' AFTER `expires_at`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'plat_api_credential' AND COLUMN_NAME = 'last_refresh_at');
SET @s = IF(@c = 0, 'ALTER TABLE `plat_api_credential` ADD COLUMN `last_refresh_at` datetime(3) NULL COMMENT ''最近刷新时间'' AFTER `refresh_expires_at`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- plat_api_credential.status 统一为 ENABLED/DISABLED（原 ACTIVE/INACTIVE）
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'plat_api_credential' AND COLUMN_NAME = 'status');
SET @s = IF(@c > 0, 'UPDATE `plat_api_credential` SET `status` = ''ENABLED'' WHERE `status` = ''ACTIVE'' AND deleted = 0; UPDATE `plat_api_credential` SET `status` = ''DISABLED'' WHERE `status` = ''INACTIVE'' AND deleted = 0', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SELECT 'sys platform site shop fx migration applied' AS result;
