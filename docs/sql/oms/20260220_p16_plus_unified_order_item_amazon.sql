-- P1.6+ getOrderItems 回填：oms_unified_order_item 补 Amazon 匹配字段（若不存在）
SET NAMES utf8mb4;
SET @db = DATABASE();

-- amazon_seller_sku, amazon_asin, amazon_quantity_ordered
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'oms_unified_order_item' AND COLUMN_NAME = 'amazon_seller_sku');
SET @s = IF(@c = 0, 'ALTER TABLE `oms_unified_order_item` ADD COLUMN `amazon_seller_sku` varchar(128) NULL COMMENT ''Amazon SellerSKU(回填用)'' AFTER `external_order_item_id`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'oms_unified_order_item' AND COLUMN_NAME = 'amazon_asin');
SET @s = IF(@c = 0, 'ALTER TABLE `oms_unified_order_item` ADD COLUMN `amazon_asin` varchar(32) NULL COMMENT ''Amazon ASIN(回填用)'' AFTER `amazon_seller_sku`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'oms_unified_order_item' AND COLUMN_NAME = 'amazon_quantity_ordered');
SET @s = IF(@c = 0, 'ALTER TABLE `oms_unified_order_item` ADD COLUMN `amazon_quantity_ordered` int NULL COMMENT ''Amazon QuantityOrdered(回填用)'' AFTER `amazon_asin`', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 索引：按 SellerSKU 查询
SET @idx = (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'oms_unified_order_item' AND INDEX_NAME = 'idx_amz_seller_sku');
SET @s = IF(@idx = 0, 'ALTER TABLE `oms_unified_order_item` ADD INDEX `idx_amz_seller_sku` (`amazon_seller_sku`(64))', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SELECT 'p16+ oms_unified_order_item amazon fields applied' AS result;
