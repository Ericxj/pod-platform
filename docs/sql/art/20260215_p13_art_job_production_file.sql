-- P1.3 稿件域：art_job 按 line 幂等 + 重试/错误字段；art_production_file 补 file_hash/uk_hash/idx_status
SET NAMES utf8mb4;

-- 1) art_job: 增加 fulfillment_line_id（履约行ID）、重试与错误信息；uk_line 幂等
ALTER TABLE `art_job`
  ADD COLUMN `fulfillment_line_id` bigint NULL DEFAULT NULL COMMENT '履约行ID(oms_fulfillment_item.id)' AFTER `fulfillment_id`,
  ADD COLUMN `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数' AFTER `remark`,
  ADD COLUMN `last_error_code` varchar(64) NULL DEFAULT NULL COMMENT '最近错误码' AFTER `retry_count`,
  ADD COLUMN `last_error_msg` varchar(512) NULL DEFAULT NULL COMMENT '最近错误信息' AFTER `last_error_code`;

ALTER TABLE `art_job` ADD UNIQUE INDEX `uk_line` (`tenant_id`, `factory_id`, `fulfillment_id`, `fulfillment_line_id`);

-- template_id 改为可空（P1.3 最小可用可无模板）
ALTER TABLE `art_job` MODIFY COLUMN `template_id` bigint NULL DEFAULT NULL COMMENT '模板ID(可选)';

-- 2) art_production_file: 增加 file_hash、format；uk_hash 防重复；idx_status
ALTER TABLE `art_production_file`
  ADD COLUMN `file_hash` varchar(64) NULL DEFAULT NULL COMMENT '文件哈希(防重复)' AFTER `file_no`,
  ADD COLUMN `format` varchar(16) NULL DEFAULT NULL COMMENT '格式(如 PDF/TIFF)' AFTER `file_type`;

ALTER TABLE `art_production_file` ADD UNIQUE INDEX `uk_hash` (`tenant_id`, `factory_id`, `file_hash`(64));
ALTER TABLE `art_production_file` ADD INDEX `idx_status` (`tenant_id`, `status`, `created_at`);

SELECT 'art p1.3 art_job + production_file migration applied' AS result;
