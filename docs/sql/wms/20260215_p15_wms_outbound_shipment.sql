-- P1.5 WMS 出库：uk_src、line packed_qty、wms_shipment
SET NAMES utf8mb4;

-- 1) wms_outbound_order: 增加 source_type，uk_src 幂等
ALTER TABLE `wms_outbound_order`
  ADD COLUMN `source_type` varchar(32) NULL DEFAULT NULL COMMENT '来源类型(FULFILLMENT等)' AFTER `trace_id`;

UPDATE `wms_outbound_order` SET `source_type` = COALESCE(`outbound_type`, 'FULFILLMENT') WHERE `deleted` = 0;

ALTER TABLE `wms_outbound_order` ADD UNIQUE INDEX `uk_src` (`tenant_id`, `factory_id`, `source_type`(32), `source_no`(64));

-- 2) wms_outbound_order_line: 增加 packed_qty（已打包数量）
ALTER TABLE `wms_outbound_order_line`
  ADD COLUMN `packed_qty` int NOT NULL DEFAULT 0 COMMENT '已打包数量' AFTER `qty_picked`;

-- 3) wms_shipment: 发货记录（最小版）
CREATE TABLE IF NOT EXISTS `wms_shipment` (
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
  `outbound_id` bigint NOT NULL COMMENT '出库单ID',
  `outbound_no` varchar(64) NOT NULL COMMENT '出库单号',
  `carrier_code` varchar(64) NULL COMMENT '承运商编码',
  `tracking_no` varchar(128) NULL COMMENT '运单号',
  `shipped_at` datetime(3) NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_shipment_outbound` (`tenant_id`, `outbound_id`),
  INDEX `idx_status` (`tenant_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发货记录';

SELECT 'wms p1.5 outbound + shipment migration applied' AS result;
