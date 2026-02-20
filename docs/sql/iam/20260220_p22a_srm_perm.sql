-- P2.2-A SRM 菜单与权限（供应商 + 采购单）
SET NAMES utf8mb4;

-- 父菜单 SRM（parent_id=0 表示顶级）
INSERT INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(920, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:center', 'SRM', 'MENU', '/srm', NULL, NULL, NULL, NULL, NULL, 0, 20, 'ENABLED', 0, 1, 1),
(921, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:supplier:list', '供应商管理', 'MENU', '/srm/supplier', 'srm/supplier/index', NULL, NULL, NULL, NULL, 920, 1, 'ENABLED', 0, 1, 0),
(922, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:po:list', '采购单管理', 'MENU', '/srm/purchase-order', 'srm/purchase-order/index', NULL, NULL, NULL, NULL, 920, 2, 'ENABLED', 0, 1, 0)
ON DUPLICATE KEY UPDATE `perm_code`=VALUES(`perm_code`), `perm_name`=VALUES(`perm_name`), `menu_path`=VALUES(`menu_path`), `component`=VALUES(`component`), `parent_id`=VALUES(`parent_id`), `sort_no`=VALUES(`sort_no`), `deleted`=0, `updated_at`=NOW(3);

-- BUTTON 供应商
INSERT INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(92101, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:supplier:page', '列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 921, 1, 'ENABLED', 0, 1, 0),
(92102, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:supplier:get', '详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 921, 2, 'ENABLED', 0, 1, 0),
(92103, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:supplier:create', '新增', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 921, 3, 'ENABLED', 0, 1, 0),
(92104, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:supplier:update', '编辑', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 921, 4, 'ENABLED', 0, 1, 0),
(92105, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:supplier:enable', '启用', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 921, 5, 'ENABLED', 0, 1, 0),
(92106, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:supplier:disable', '禁用', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 921, 6, 'ENABLED', 0, 1, 0)
ON DUPLICATE KEY UPDATE `perm_name`=VALUES(`perm_name`), `deleted`=0, `updated_at`=NOW(3);

-- BUTTON 采购单
INSERT INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(92201, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:po:page', '列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 922, 1, 'ENABLED', 0, 1, 0),
(92202, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:po:get', '详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 922, 2, 'ENABLED', 0, 1, 0),
(92203, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:po:create', '新增', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 922, 3, 'ENABLED', 0, 1, 0),
(92204, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:po:addLine', '添加行', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 922, 4, 'ENABLED', 0, 1, 0),
(92205, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:po:updateLine', '编辑行', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 922, 5, 'ENABLED', 0, 1, 0),
(92206, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:po:submit', '提交', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 922, 6, 'ENABLED', 0, 1, 0),
(92207, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:po:approve', '审批', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 922, 7, 'ENABLED', 0, 1, 0),
(92208, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:po:cancel', '取消', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 922, 8, 'ENABLED', 0, 1, 0),
(92209, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:po:close', '关闭', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 922, 9, 'ENABLED', 0, 1, 0)
ON DUPLICATE KEY UPDATE `perm_name`=VALUES(`perm_name`), `deleted`=0, `updated_at`=NOW(3);

-- API 权限
INSERT INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(92111, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:suppliers', 'GET/POST供应商', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/srm/suppliers', 921, 10, 'ENABLED', 0, 1, 0),
(92112, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:suppliers:id', 'GET供应商详情', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/srm/suppliers/*', 921, 11, 'ENABLED', 0, 1, 0),
(92115, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:suppliers:put', 'PUT供应商', 'API', NULL, NULL, NULL, NULL, 'PUT', '/api/srm/suppliers/*', 921, 12, 'ENABLED', 0, 1, 0),
(92113, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:suppliers:enable', 'POST启用', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/srm/suppliers/*/enable', 921, 13, 'ENABLED', 0, 1, 0),
(92114, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:suppliers:disable', 'POST禁用', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/srm/suppliers/*/disable', 921, 14, 'ENABLED', 0, 1, 0),
(92211, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:po:page', 'GET采购单', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/srm/purchase-orders', 922, 10, 'ENABLED', 0, 1, 0),
(92212, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:po:get', 'GET采购单详情', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/srm/purchase-orders/*', 922, 11, 'ENABLED', 0, 1, 0),
(92213, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:po:create', 'POST采购单', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/srm/purchase-orders', 922, 12, 'ENABLED', 0, 1, 0),
(92214, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:po:lines', 'POST采购单行', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/srm/purchase-orders/*/lines', 922, 13, 'ENABLED', 0, 1, 0),
(92219, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:po:line:put', 'PUT采购单行', 'API', NULL, NULL, NULL, NULL, 'PUT', '/api/srm/purchase-orders/*/lines/*', 922, 14, 'ENABLED', 0, 1, 0),
(92215, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:po:submit', 'POST提交', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/srm/purchase-orders/*/submit', 922, 15, 'ENABLED', 0, 1, 0),
(92216, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:po:approve', 'POST审批', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/srm/purchase-orders/*/approve', 922, 16, 'ENABLED', 0, 1, 0),
(92217, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:po:cancel', 'POST取消', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/srm/purchase-orders/*/cancel', 922, 17, 'ENABLED', 0, 1, 0),
(92218, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'srm:api:po:close', 'POST关闭', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/srm/purchase-orders/*/close', 922, 18, 'ENABLED', 0, 1, 0)
ON DUPLICATE KEY UPDATE `perm_name`=VALUES(`perm_name`), `api_method`=VALUES(`api_method`), `api_path`=VALUES(`api_path`), `deleted`=0, `updated_at`=NOW(3);

-- 授权 ADMIN(role_id=1)，使用 920xxx 避免冲突
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(920400, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 920),(920401, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 921),(920402, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 922);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(920403, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92101),(920404, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92102),(920405, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92103),(920406, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92104),(920407, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92105),(920408, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92106);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(920409, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92201),(920410, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92202),(920411, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92203),(920412, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92204),(920413, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92205),(920414, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92206),(920415, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92207),(920416, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92208),(920417, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92209);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(920418, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92111),(920419, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92112),(920420, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92115),(920421, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92113),(920431, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92114);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(920422, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92211),(920423, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92212),(920424, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92213),(920425, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92214),(920432, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92219),(920426, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92215),(920427, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92216),(920428, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92217),(920429, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92218);

SELECT 'P2.2-A SRM permissions upserted' AS result;
