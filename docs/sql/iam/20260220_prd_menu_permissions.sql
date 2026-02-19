-- 商品中心菜单与权限：商品中心(910)、SPU管理(911)、SKU管理(912)、平台SKU映射(913)
-- 执行前确保 iam_permission / iam_role_permission 存在。建议 INSERT IGNORE 或先查后插。

INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(910, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:center', '商品中心', 'MENU', '/prd', 'LAYOUT', 'product', NULL, NULL, NULL, 50, 'ENABLED', 0, 1, 0),
(911, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:spu:list', 'SPU管理', 'MENU', '/prd/spu', 'prd/spu/index', NULL, NULL, NULL, NULL, 910, 1, 'ENABLED', 0, 1, 0),
(912, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:sku:list', 'SKU管理', 'MENU', '/prd/sku', 'prd/sku/index', NULL, NULL, NULL, NULL, 910, 2, 'ENABLED', 0, 1, 0),
(913, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:mapping:list', '平台SKU映射', 'MENU', '/prd/mapping', 'prd/mapping/index', NULL, NULL, NULL, NULL, 910, 3, 'ENABLED', 0, 1, 0);

INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(91101, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:spu:page', 'SPU列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 911, 1, 'ENABLED', 0, 1, 0),
(91102, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:spu:get', 'SPU详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 911, 2, 'ENABLED', 0, 1, 0),
(91103, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:spu:create', '新增SPU', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 911, 3, 'ENABLED', 0, 1, 0),
(91104, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:spu:update', '编辑SPU', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 911, 4, 'ENABLED', 0, 1, 0),
(91201, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:sku:page', 'SKU列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 1, 'ENABLED', 0, 1, 0),
(91202, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:sku:get', 'SKU详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 2, 'ENABLED', 0, 1, 0),
(91203, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:sku:create', '新增SKU', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 3, 'ENABLED', 0, 1, 0),
(91204, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:sku:update', '编辑SKU', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 4, 'ENABLED', 0, 1, 0),
(91205, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:sku:activate', '激活SKU', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 5, 'ENABLED', 0, 1, 0),
(91206, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:sku:deactivate', '停用SKU', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 6, 'ENABLED', 0, 1, 0),
(91207, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:barcode:list', '条码列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 7, 'ENABLED', 0, 1, 0),
(91208, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:barcode:batchAdd', '批量添加条码', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 8, 'ENABLED', 0, 1, 0),
(91209, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:barcode:delete', '删除条码', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 9, 'ENABLED', 0, 1, 0),
(91210, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:bom:get', 'BOM详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 10, 'ENABLED', 0, 1, 0),
(91211, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:bom:save', '保存BOM', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 11, 'ENABLED', 0, 1, 0),
(91212, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:bom:publish', '发布BOM', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 12, 'ENABLED', 0, 1, 0),
(91213, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:bom:unpublish', '取消发布BOM', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 13, 'ENABLED', 0, 1, 0),
(91214, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:routing:get', '工艺路线详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 14, 'ENABLED', 0, 1, 0),
(91215, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:routing:save', '保存工艺路线', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 15, 'ENABLED', 0, 1, 0),
(91216, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:routing:publish', '发布工艺路线', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 16, 'ENABLED', 0, 1, 0),
(91217, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:routing:unpublish', '取消发布工艺路线', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 912, 17, 'ENABLED', 0, 1, 0),
(91301, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:mapping:page', '映射列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 913, 1, 'ENABLED', 0, 1, 0),
(91302, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:mapping:create', '新增映射', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 913, 2, 'ENABLED', 0, 1, 0),
(91303, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:mapping:update', '编辑映射', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 913, 3, 'ENABLED', 0, 1, 0),
(91304, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'prd:mapping:delete', '删除映射', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 913, 4, 'ENABLED', 0, 1, 0);

-- 授权给角色 ADMIN(role_id=1)
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(200, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 910),
(201, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 911),
(202, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91101),(203, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91102),(204, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91103),(205, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91104),
(206, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 912),
(207, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91201),(208, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91202),(209, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91203),(210, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91204),(211, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91205),(212, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91206),(213, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91207),(214, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91208),(215, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91209),(216, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91210),(217, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91211),(218, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91212),(219, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91213),(220, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91214),(221, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91215),(222, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91216),(223, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91217),
(224, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 913),
(225, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91301),(226, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91302),(227, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91303),(228, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91304);

SELECT 'prd menu and permissions inserted' AS result;
