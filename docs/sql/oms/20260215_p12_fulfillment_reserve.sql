-- P1.2 Fulfillment: oms_fulfillment 增加 channel/shop_id/external_order_id + uk_src_order；行表增加 reserved_qty/reserve_status 与索引
SET NAMES utf8mb4;

-- 1) oms_fulfillment: 增加来源订单唯一键防重复创建
ALTER TABLE `oms_fulfillment`
  ADD COLUMN `channel` varchar(32) NULL DEFAULT NULL COMMENT '渠道' AFTER `trace_id`,
  ADD COLUMN `shop_id` bigint NULL DEFAULT NULL COMMENT '店铺ID' AFTER `channel`,
  ADD COLUMN `external_order_id` varchar(128) NULL DEFAULT NULL COMMENT '平台订单ID' AFTER `shop_id`;

UPDATE `oms_fulfillment` o
  INNER JOIN `oms_unified_order` u ON o.unified_order_id = u.id AND u.deleted = 0
  SET o.channel = u.channel, o.shop_id = u.shop_id, o.external_order_id = u.external_order_id
  WHERE o.deleted = 0;

ALTER TABLE `oms_fulfillment` ADD UNIQUE INDEX `uk_src_order` (`tenant_id`, `factory_id`, `channel`(32), `shop_id`, `external_order_id`(128));

-- 2) oms_fulfillment_item: 预占数量与行状态
ALTER TABLE `oms_fulfillment_item`
  ADD COLUMN `reserved_qty` int NOT NULL DEFAULT 0 COMMENT '已预占数量' AFTER `qty`,
  ADD COLUMN `reserve_status` varchar(32) NULL DEFAULT NULL COMMENT 'RESERVED/SHORTAGE/RELEASED' AFTER `reserved_qty`;

ALTER TABLE `oms_fulfillment_item` ADD INDEX `idx_ff_item_sku` (`tenant_id`, `sku_id`);

SELECT 'oms p1.2 fulfillment reserve migration applied' AS result;
