-- P1.1 OMS 菜单与权限：OMS(920)、统一订单(921)、异常队列(922)
INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(920, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'oms:center', 'OMS', 'MENU', '/oms', 'LAYOUT', 'order', NULL, NULL, NULL, 40, 'ENABLED', 0, 1, 0),
(921, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'oms:unified-order:list', '统一订单', 'MENU', '/oms/unified-orders', 'oms/unified-orders/index', NULL, NULL, NULL, NULL, 920, 1, 'ENABLED', 0, 1, 0),
(922, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'oms:hold:list', '异常队列', 'MENU', '/oms/holds', 'oms/holds/index', NULL, NULL, NULL, NULL, 920, 2, 'ENABLED', 0, 1, 0);

INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(92101, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'oms:unified-order:page', '统一订单列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 921, 1, 'ENABLED', 0, 1, 0),
(92102, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'oms:unified-order:get', '统一订单详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 921, 2, 'ENABLED', 0, 1, 0),
(92201, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'oms:hold:page', '异常队列列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 922, 1, 'ENABLED', 0, 1, 0),
(92202, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'oms:hold:get', '异常队列详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 922, 2, 'ENABLED', 0, 1, 0),
(92203, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'oms:hold:resolve', '处理异常(绑定SKU)', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 922, 3, 'ENABLED', 0, 1, 0);

-- 授权给角色 ADMIN(role_id=1)
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(230, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 920),
(231, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 921),
(232, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92101),(233, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92102),
(234, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 922),
(235, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92201),(236, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92202),(237, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 92203);

SELECT 'oms menu and permissions inserted' AS result;
