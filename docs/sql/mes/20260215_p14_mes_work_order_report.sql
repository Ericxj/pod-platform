-- P1.4 MES 工单域：uk_source、工单行 produced_qty/scrap_qty、mes_operation、mes_report
SET NAMES utf8mb4;

-- 1) mes_work_order: 增加 source_type/source_no，uk_source 幂等
ALTER TABLE `mes_work_order`
  ADD COLUMN `source_type` varchar(32) NULL DEFAULT NULL COMMENT '来源类型(FULFILLMENT等)' AFTER `trace_id`,
  ADD COLUMN `source_no` varchar(64) NULL DEFAULT NULL COMMENT '来源单号' AFTER `source_type`;

UPDATE `mes_work_order` wo
  INNER JOIN `oms_fulfillment` f ON wo.fulfillment_id = f.id AND f.deleted = 0
  SET wo.source_type = 'FULFILLMENT', wo.source_no = f.fulfillment_no
  WHERE wo.deleted = 0;

ALTER TABLE `mes_work_order` ADD UNIQUE INDEX `uk_source` (`tenant_id`, `factory_id`, `source_type`(32), `source_no`(64));

-- 2) mes_work_order_item: 增加 produced_qty/scrap_qty（工单行报工累计）
ALTER TABLE `mes_work_order_item`
  ADD COLUMN `produced_qty` int NOT NULL DEFAULT 0 COMMENT '已产数量' AFTER `qty`,
  ADD COLUMN `scrap_qty` int NOT NULL DEFAULT 0 COMMENT '报废数量' AFTER `produced_qty`;

-- 3) mes_operation: 工序定义（默认工序模板）
CREATE TABLE IF NOT EXISTS `mes_operation` (
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
  `op_code` varchar(64) NOT NULL COMMENT '工序编码',
  `op_name` varchar(128) NOT NULL COMMENT '工序名称',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认模板(1=印花/质检/包装)',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_op_code` (`tenant_id`, `factory_id`, `op_code`(64)),
  INDEX `idx_status` (`tenant_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工序定义';

-- 4) mes_report: 报工记录
CREATE TABLE IF NOT EXISTS `mes_report` (
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
  `work_order_id` bigint NOT NULL,
  `work_order_line_id` bigint NOT NULL COMMENT 'mes_work_order_item.id',
  `op_code` varchar(64) NOT NULL,
  `work_order_op_id` bigint NULL COMMENT '工单工序实例ID',
  `good_qty` int NOT NULL DEFAULT 0,
  `scrap_qty` int NOT NULL DEFAULT 0,
  `workstation_id` bigint NULL,
  `remark` varchar(255) NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_wo` (`tenant_id`, `work_order_id`, `created_at`),
  INDEX `idx_wo_line` (`tenant_id`, `work_order_line_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报工记录';

SELECT 'mes p1.4 work_order + report migration applied' AS result;
