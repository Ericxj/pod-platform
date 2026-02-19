-- sys_idempotent
CREATE TABLE IF NOT EXISTS `sys_idempotent` (
  `id` bigint NOT NULL,
  `key_id` varchar(128) NOT NULL,
  `record_value` text,
  `expire_at` datetime NOT NULL,
  `tenant_id` bigint,
  `factory_id` bigint,
  `created_at` datetime,
  `updated_at` datetime,
  `deleted` tinyint DEFAULT 0,
  `version` int DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_key`(`key_id`)
);

-- ai_task
CREATE TABLE IF NOT EXISTS `ai_task` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `task_no` varchar(64) NOT NULL,
  `task_type` varchar(32) NOT NULL,
  `biz_type` varchar(64),
  `biz_no` varchar(64),
  `status` varchar(32) NOT NULL,
  `payload_json` json,
  `result_json` json,
  `error_msg` text,
  `attempts` int NOT NULL DEFAULT 0,
  `max_attempts` int NOT NULL DEFAULT 3,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_ai_task_no`(`task_no`)
);



-- tms_shipment
CREATE TABLE IF NOT EXISTS `tms_shipment` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `shipment_no` varchar(64) NOT NULL,
  `fulfillment_id` bigint,
  `outbound_id` bigint,
  `carrier_id` bigint NOT NULL,
  `method_id` bigint,
  `tracking_no` varchar(128),
  `label_url` varchar(512),
  `label_format` varchar(32),
  `status` varchar(32) NOT NULL,
  `shipped_at` datetime(3),
  `delivered_at` datetime(3),
  `cost_amount` decimal(18, 2),
  `estimated_cost_amount` decimal(18, 2),
  `billed_cost_amount` decimal(18, 2),
  `cost_currency` varchar(8),
  `currency` varchar(8),
  `ship_to_address_json` json,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_shipment_no`(`tenant_id`, `shipment_no`),
  UNIQUE INDEX `uk_tracking`(`tenant_id`, `tracking_no`)
);

-- tms_shipment_package
CREATE TABLE IF NOT EXISTS `tms_shipment_package` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `shipment_id` bigint NOT NULL,
  `package_no` int NOT NULL,
  `weight_g` int,
  `length_mm` int,
  `width_mm` int,
  `height_mm` int,
  `content_json` json,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_ship_pkg`(`tenant_id`, `shipment_id`, `package_no`)
);

-- tms_platform_ack
CREATE TABLE IF NOT EXISTS `tms_platform_ack` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `platform_code` varchar(32) NOT NULL,
  `shop_id` bigint NOT NULL,
  `platform_order_id` varchar(128) NOT NULL,
  `action_type` varchar(32) NOT NULL,
  `status` varchar(16) NOT NULL,
  `attempts` int NOT NULL DEFAULT 0,
  `last_error` varchar(1024),
  `payload_json` json,
  `response_json` json,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_ack`(`tenant_id`, `platform_code`, `shop_id`, `platform_order_id`, `action_type`)
);

-- wms_outbound_order
CREATE TABLE IF NOT EXISTS `wms_outbound_order` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `outbound_no` varchar(64) NOT NULL,
  `outbound_type` varchar(32) NOT NULL,
  `source_no` varchar(64),
  `fulfillment_id` bigint,
  `warehouse_id` bigint NOT NULL,
  `ship_to_address_json` json,
  `pack_strategy` varchar(32),
  `status` varchar(32) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_outbound_no`(`tenant_id`, `outbound_no`)
);

-- wms_outbound_order_line
CREATE TABLE IF NOT EXISTS `wms_outbound_order_line` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `outbound_id` bigint NOT NULL,
  `line_no` int NOT NULL,
  `sku_id` bigint NOT NULL,
  `qty` int NOT NULL,
  `qty_picked` int NOT NULL DEFAULT 0,
  `qty_shipped` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_outbound_line`(`tenant_id`, `outbound_id`, `line_no`)
);

-- wms_pack_order
CREATE TABLE IF NOT EXISTS `wms_pack_order` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `pack_no` varchar(64) NOT NULL,
  `outbound_id` bigint NOT NULL,
  `status` varchar(32) NOT NULL,
  `package_count` int NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_pack_no`(`tenant_id`, `pack_no`)
);

-- wms_pack_order_line
CREATE TABLE IF NOT EXISTS `wms_pack_order_line` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `pack_id` bigint NOT NULL,
  `line_no` int NOT NULL,
  `sku_id` bigint NOT NULL,
  `qty` int NOT NULL,
  `package_no` int NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_pack_line`(`tenant_id`, `pack_id`, `line_no`)
);

-- wms_pick_task
CREATE TABLE IF NOT EXISTS `wms_pick_task` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `pick_task_no` varchar(64) NOT NULL,
  `wave_id` bigint NOT NULL,
  `outbound_id` bigint NOT NULL,
  `status` varchar(32) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_pick_task_no`(`tenant_id`, `pick_task_no`)
);

-- wms_pick_wave
CREATE TABLE IF NOT EXISTS `wms_pick_wave` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `wave_no` varchar(64) NOT NULL,
  `warehouse_id` bigint NOT NULL,
  `status` varchar(32) NOT NULL,
  `strategy_json` json,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_wave_no`(`tenant_id`, `wave_no`)
);

-- sys_outbox_event
CREATE TABLE IF NOT EXISTS `sys_outbox_event` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `event_id` varchar(64) NOT NULL,
  `event_type` varchar(64) NOT NULL,
  `aggregate_type` varchar(64) NOT NULL,
  `aggregate_id` bigint NOT NULL,
  `biz_no` varchar(64),
  `payload_json` text,
  `status` varchar(32) NOT NULL,
  `attempts` int NOT NULL DEFAULT 0,
  `next_retry_at` datetime(3),
  PRIMARY KEY (`id`)
);


-- oms_unified_order
CREATE TABLE IF NOT EXISTS `oms_unified_order`  (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `unified_order_no` varchar(64) NOT NULL,
  `platform_code` varchar(32) NOT NULL,
  `shop_id` bigint NOT NULL,
  `platform_order_id` varchar(128) NOT NULL,
  `platform_order_no` varchar(128),
  `service_level` varchar(64),
  `order_created_at` datetime(3),
  `buyer_name` varchar(128),
  `buyer_email` varchar(128),
  `buyer_note` varchar(512),
  `currency` varchar(8),
  `total_amount` decimal(18, 2),
  `shipping_amount` decimal(18, 2),
  `tax_amount` decimal(18, 2),
  `discount_amount` decimal(18, 2),
  `order_status` varchar(32) NOT NULL,
  `payment_status` varchar(32),
  `risk_flag` tinyint NOT NULL DEFAULT 0,
  `source_raw_order_id` bigint,
  `extra_json` json,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_unified`(`tenant_id`, `unified_order_no`)
);

-- oms_unified_order_item
CREATE TABLE IF NOT EXISTS `oms_unified_order_item`  (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `unified_order_id` bigint NOT NULL,
  `line_no` int NOT NULL,
  `sku_id` bigint NOT NULL,
  `sku_code` varchar(64),
  `platform_sku` varchar(128),
  `item_title` varchar(255),
  `qty` int NOT NULL,
  `unit_price` decimal(18, 2),
  `item_status` varchar(32) NOT NULL,
  `personalization_json` json,
  `extra_json` json,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_order_line`(`tenant_id`, `unified_order_id`, `line_no`)
);

-- oms_fulfillment
CREATE TABLE IF NOT EXISTS `oms_fulfillment` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `fulfillment_no` varchar(64) NOT NULL,
  `unified_order_id` bigint NOT NULL,
  `fulfillment_type` varchar(32) NOT NULL,
  `status` varchar(32) NOT NULL,
  `priority` int NOT NULL DEFAULT 0,
  `warehouse_id` bigint,
  `expected_ship_at` datetime(3),
  `remark` varchar(512),
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_fulfillment_no`(`tenant_id`, `fulfillment_no`)
);

-- oms_fulfillment_item
CREATE TABLE IF NOT EXISTS `oms_fulfillment_item` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `fulfillment_id` bigint NOT NULL,
  `unified_order_item_id` bigint,
  `line_no` int NOT NULL,
  `sku_id` bigint NOT NULL,
  `qty` int NOT NULL,
  `status` varchar(32) NOT NULL,
  `personalization_json` json,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_fulfillment_line`(`tenant_id`, `fulfillment_id`, `line_no`)
);

-- art_job (P1.3: fulfillment_line_id, retry_count, last_error_*, uk_line)
CREATE TABLE IF NOT EXISTS `art_job` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `art_job_no` varchar(64) NOT NULL,
  `fulfillment_id` bigint NOT NULL,
  `fulfillment_line_id` bigint,
  `template_id` bigint,
  `status` varchar(32) NOT NULL,
  `priority` int NOT NULL DEFAULT 100,
  `retry_count` int NOT NULL DEFAULT 0,
  `last_error_code` varchar(64),
  `last_error_msg` varchar(512),
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_art_job_no`(`tenant_id`, `art_job_no`),
  UNIQUE INDEX `uk_line`(`tenant_id`, `factory_id`, `fulfillment_id`, `fulfillment_line_id`)
);

-- art_render_task
CREATE TABLE IF NOT EXISTS `art_render_task` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `art_job_id` bigint NOT NULL,
  `task_no` varchar(64) NOT NULL,
  `status` varchar(32) NOT NULL,
  `attempts` int NOT NULL DEFAULT 0,
  `last_error` varchar(1024),
  `output_url` varchar(512),
  PRIMARY KEY (`id`)
);

-- art_production_file (P1.3: file_hash, format, uk_hash)
CREATE TABLE IF NOT EXISTS `art_production_file` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `art_job_id` bigint NOT NULL,
  `file_no` varchar(64) NOT NULL,
  `file_hash` varchar(64),
  `file_type` varchar(32) NOT NULL,
  `format` varchar(16),
  `file_url` varchar(512) NOT NULL,
  `width_px` int,
  `height_px` int,
  `dpi` int,
  `status` varchar(32) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_hash`(`tenant_id`, `factory_id`, `file_hash`(64))
);

-- mes_work_order
CREATE TABLE IF NOT EXISTS `mes_work_order` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `work_order_no` varchar(64) NOT NULL,
  `fulfillment_id` bigint NOT NULL,
  `routing_id` bigint,
  `status` varchar(32) NOT NULL,
  `priority` int NOT NULL DEFAULT 100,
  `planned_start_at` datetime(3),
  `planned_end_at` datetime(3),
  `remark` varchar(255),
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_mes_wo_no`(`tenant_id`, `work_order_no`)
);

-- mes_work_order_op
CREATE TABLE IF NOT EXISTS `mes_work_order_op` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `work_order_id` bigint NOT NULL,
  `step_no` int NOT NULL,
  `op_code` varchar(64) NOT NULL,
  `status` varchar(32) NOT NULL,
  `workstation_id` bigint,
  `equipment_id` bigint,
  `start_at` datetime(3),
  `end_at` datetime(3),
  `result_json` json,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_wo_op`(`tenant_id`, `work_order_id`, `step_no`)
);

-- mes_work_order_item
CREATE TABLE IF NOT EXISTS `mes_work_order_item` (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `factory_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_by` bigint,
  `updated_by` bigint,
  `trace_id` varchar(64),
  `work_order_id` bigint NOT NULL,
  `line_no` int NOT NULL,
  `sku_id` bigint NOT NULL,
  `qty` int NOT NULL,
  `surface_code` varchar(32),
  `production_file_id` bigint,
  `status` varchar(32) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_wo_line`(`tenant_id`, `work_order_id`, `line_no`)
);
