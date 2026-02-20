-- P2.2-A SRM 采购单主流程：供应商 + 采购单（可重复执行）
SET NAMES utf8mb4;

-- A. srm_supplier
CREATE TABLE IF NOT EXISTS `srm_supplier` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL DEFAULT '0',
  `factory_id` bigint NOT NULL DEFAULT '0',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted` tinyint NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '1',
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `trace_id` varchar(64) DEFAULT NULL,
  `supplier_code` varchar(64) NOT NULL COMMENT '供应商编码',
  `supplier_name` varchar(128) NOT NULL COMMENT '供应商名称',
  `contact_name` varchar(64) DEFAULT NULL,
  `phone` varchar(32) DEFAULT NULL,
  `email` varchar(64) DEFAULT NULL,
  `address` varchar(256) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
  `remark` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier` (`tenant_id`,`factory_id`,`supplier_code`),
  KEY `idx_tenant_factory_status` (`tenant_id`,`factory_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SRM供应商';

-- B. srm_purchase_order
CREATE TABLE IF NOT EXISTS `srm_purchase_order` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL DEFAULT '0',
  `factory_id` bigint NOT NULL DEFAULT '0',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted` tinyint NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '1',
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `trace_id` varchar(64) DEFAULT NULL,
  `po_no` varchar(64) NOT NULL COMMENT '采购单号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `currency` varchar(8) NOT NULL DEFAULT 'CNY',
  `status` varchar(24) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/SUBMITTED/APPROVED/CLOSED/CANCELED',
  `total_qty` decimal(18,4) NOT NULL DEFAULT '0',
  `total_amount` decimal(18,4) NOT NULL DEFAULT '0',
  `expected_arrive_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_po` (`tenant_id`,`factory_id`,`po_no`),
  KEY `idx_tenant_factory_status` (`tenant_id`,`factory_id`,`status`),
  KEY `idx_supplier` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SRM采购单';

-- C. srm_purchase_order_line
CREATE TABLE IF NOT EXISTS `srm_purchase_order_line` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL DEFAULT '0',
  `factory_id` bigint NOT NULL DEFAULT '0',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted` tinyint NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '1',
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `trace_id` varchar(64) DEFAULT NULL,
  `po_id` bigint NOT NULL COMMENT '采购单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NOT NULL,
  `sku_code` varchar(64) NOT NULL COMMENT '快照',
  `sku_name` varchar(128) DEFAULT NULL COMMENT '快照',
  `qty_ordered` decimal(18,4) NOT NULL COMMENT '订购数量',
  `unit_price` decimal(18,4) NOT NULL DEFAULT '0',
  `qty_received` decimal(18,4) NOT NULL DEFAULT '0' COMMENT '已收货数量',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_po_line` (`tenant_id`,`factory_id`,`po_id`,`line_no`),
  KEY `idx_po_id` (`po_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SRM采购单行';

SELECT 'P2.2-A SRM tables created or exist' AS result;
