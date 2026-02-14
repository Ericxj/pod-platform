-- Migration: Add common fields and index to sys_login_log (MySQL 8)
-- Ensures sys_login_log complies with "all tables must have common fields" rule.

-- 1) Add missing common columns (types/defaults aligned with other tables)
ALTER TABLE `sys_login_log`
  ADD COLUMN `updated_at` datetime(3) NOT NULL DEFAULT (CURRENT_TIMESTAMP(3)) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间' AFTER `created_at`,
  ADD COLUMN `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)' AFTER `updated_at`,
  ADD COLUMN `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER `deleted`,
  ADD COLUMN `created_by` bigint NULL DEFAULT NULL COMMENT '创建人' AFTER `version`,
  ADD COLUMN `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人' AFTER `created_by`;

-- 2) Add common index for tenant/factory/created/deleted
CREATE INDEX `idx_tenant_factory_created` ON `sys_login_log` (`tenant_id`, `factory_id`, `created_at`, `deleted`) USING BTREE;
