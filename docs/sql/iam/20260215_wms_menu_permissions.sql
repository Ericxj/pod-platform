-- P1.5 WMS 出库 菜单与权限：WMS(960)、出库单(961)
INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(960, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'wms:center', 'WMS', 'MENU', '/wms', 'LAYOUT', 'shop', NULL, NULL, NULL, 0, 31, 'ENABLED', 0, 1, 0),
(961, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'wms:outbound:list', '出库单', 'MENU', '/wms/outbounds', 'wms/outbounds/index', NULL, NULL, NULL, NULL, 960, 1, 'ENABLED', 0, 1, 0);

INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(96101, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'wms:outbound:page', '出库单列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 961, 1, 'ENABLED', 0, 1, 0),
(96102, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'wms:outbound:get', '出库单详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 961, 2, 'ENABLED', 0, 1, 0),
(96103, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'wms:outbound:create', '按履约单创建', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 961, 3, 'ENABLED', 0, 1, 0),
(96104, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'wms:outbound:picking:start', '开始拣货', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 961, 4, 'ENABLED', 0, 1, 0),
(96105, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'wms:outbound:picking:confirm', '确认拣货', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 961, 5, 'ENABLED', 0, 1, 0),
(96106, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'wms:outbound:pack', '打包', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 961, 6, 'ENABLED', 0, 1, 0),
(96107, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'wms:outbound:ship', '发货', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 961, 7, 'ENABLED', 0, 1, 0),
(96108, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'wms:outbound:cancel', '取消', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 961, 8, 'ENABLED', 0, 1, 0);

-- 授权给角色 ADMIN(role_id=1)
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(270, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 960),
(271, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 961),
(272, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 96101),(273, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 96102),(274, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 96103),
(275, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 96104),(276, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 96105),(277, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 96106),(278, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 96107),(279, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 96108);

SELECT 'wms menu and permissions inserted' AS result;
