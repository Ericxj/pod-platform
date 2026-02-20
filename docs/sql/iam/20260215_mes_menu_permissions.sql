-- P1.4 MES 工单 菜单与权限：MES(950)、工单(951)
INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(950, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'mes:center', 'MES', 'MENU', '/mes', 'LAYOUT', 'cluster', NULL, NULL, NULL, 0, 32, 'ENABLED', 0, 1, 0),
(951, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'mes:work-order:list', '工单', 'MENU', '/mes/work-orders', 'mes/work-orders/index', NULL, NULL, NULL, NULL, 950, 1, 'ENABLED', 0, 1, 0);

INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(95101, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'mes:work-order:page', '工单列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 951, 1, 'ENABLED', 0, 1, 0),
(95102, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'mes:work-order:get', '工单详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 951, 2, 'ENABLED', 0, 1, 0),
(95103, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'mes:work-order:create', '按履约单创建工单', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 951, 3, 'ENABLED', 0, 1, 0),
(95104, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'mes:work-order:release', '释放', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 951, 4, 'ENABLED', 0, 1, 0),
(95105, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'mes:work-order:start', '开始', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 951, 5, 'ENABLED', 0, 1, 0),
(95106, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'mes:work-order:report', '报工', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 951, 6, 'ENABLED', 0, 1, 0),
(95107, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'mes:work-order:cancel', '取消', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 951, 7, 'ENABLED', 0, 1, 0);

-- 授权给角色 ADMIN(role_id=1)
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(260, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 950),
(261, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 951),
(262, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 95101),(263, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 95102),(264, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 95103),
(265, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 95104),(266, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 95105),(267, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 95106),(268, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 95107);

SELECT 'mes menu and permissions inserted' AS result;
