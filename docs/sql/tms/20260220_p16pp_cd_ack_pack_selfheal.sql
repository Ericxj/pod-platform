-- P1.6++ C+D: 多包裹 + 自愈可观测字段
SET NAMES utf8mb4;
SET @db = DATABASE();

-- channel_shipment_ack: 多包裹 pack 关联 + 自愈
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'channel_shipment_ack' AND COLUMN_NAME = 'wms_pack_id');
SET @s = IF(@c = 0, 'ALTER TABLE `channel_shipment_ack` ADD COLUMN `wms_pack_id` bigint NULL COMMENT ''WMS pack_order.id(多包裹时)'' AFTER `unified_order_id`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'channel_shipment_ack' AND COLUMN_NAME = 'order_items_json');
SET @s = IF(@c = 0, 'ALTER TABLE `channel_shipment_ack` ADD COLUMN `order_items_json` text NULL COMMENT ''本包裹 orderItemId+quantity 快照(多包裹时)'' AFTER `wms_pack_id`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'channel_shipment_ack' AND COLUMN_NAME = 'self_heal_attempted');
SET @s = IF(@c = 0, 'ALTER TABLE `channel_shipment_ack` ADD COLUMN `self_heal_attempted` tinyint NOT NULL DEFAULT 0 COMMENT ''是否已尝试自愈'' AFTER `last_attempt_at`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'channel_shipment_ack' AND COLUMN_NAME = 'self_heal_action');
SET @s = IF(@c = 0, 'ALTER TABLE `channel_shipment_ack` ADD COLUMN `self_heal_action` varchar(128) NULL COMMENT ''自愈动作(shipDate/carrier/orderItemId等)'' AFTER `self_heal_attempted`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'channel_shipment_ack' AND COLUMN_NAME = 'self_heal_at');
SET @s = IF(@c = 0, 'ALTER TABLE `channel_shipment_ack` ADD COLUMN `self_heal_at` datetime(3) NULL COMMENT ''自愈时间'' AFTER `self_heal_action`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'channel_shipment_ack' AND COLUMN_NAME = 'retry_404_count');
SET @s = IF(@c = 0, 'ALTER TABLE `channel_shipment_ack` ADD COLUMN `retry_404_count` int NOT NULL DEFAULT 0 COMMENT ''404 重试次数(最多3次)'' AFTER `self_heal_at`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SELECT 'p16++ cd ack pack + self_heal fields applied' AS result;
