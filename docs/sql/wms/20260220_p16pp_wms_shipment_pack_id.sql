-- P1.6++ 多包裹：wms_shipment 支持 pack_id（多包裹时每包一条）
SET NAMES utf8mb4;
SET @db = DATABASE();

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'wms_shipment' AND COLUMN_NAME = 'pack_id');
SET @s = IF(@c = 0, 'ALTER TABLE `wms_shipment` ADD COLUMN `pack_id` bigint NULL COMMENT ''wms_pack_order.id(多包裹时)'' AFTER `shipped_at`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SELECT 'p16++ wms_shipment pack_id applied' AS result;
