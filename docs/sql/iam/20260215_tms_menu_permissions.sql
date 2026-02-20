-- P1.6 TMS 发货单 菜单与权限：TMS(970)、发货单(971)
INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(970, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:center', 'TMS', 'MENU', '/tms', 'LAYOUT', 'car', NULL, NULL, NULL, 0, 33, 'ENABLED', 0, 1, 0),
(971, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:shipment:list', '发货单', 'MENU', '/tms/shipments', 'tms/shipments/index', NULL, NULL, NULL, NULL, 970, 1, 'ENABLED', 0, 1, 0);

INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(97101, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:shipment:page', '发货单列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 971, 1, 'ENABLED', 0, 1, 0),
(97102, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:shipment:get', '发货单详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 971, 2, 'ENABLED', 0, 1, 0),
(97103, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:shipment:create', '从出库单创建', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 971, 3, 'ENABLED', 0, 1, 0),
(97104, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:shipment:label', '生成面单', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 971, 4, 'ENABLED', 0, 1, 0),
(97105, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:shipment:sync', '回传平台', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 971, 5, 'ENABLED', 0, 1, 0),
(97106, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:carrier:list', '承运商列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 971, 6, 'ENABLED', 0, 1, 0);

-- 授权给角色 ADMIN(role_id=1)
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(280, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 970),
(281, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 971),
(282, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 97101),(283, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 97102),(284, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 97103),
(285, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 97104),(286, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 97105),(287, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 97106);

SELECT 'tms menu and permissions inserted' AS result;
