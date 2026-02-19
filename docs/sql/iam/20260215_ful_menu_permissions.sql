-- P1.2 Fulfillment 菜单与权限：Fulfillment(930)、履约单(931)
INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(930, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'ful:center', 'Fulfillment', 'MENU', '/ful', 'LAYOUT', 'delivered', NULL, NULL, NULL, 0, 35, 'ENABLED', 0, 1, 0),
(931, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'ful:fulfillment:list', '履约单', 'MENU', '/ful/fulfillments', 'ful/fulfillments/index', NULL, NULL, NULL, NULL, 930, 1, 'ENABLED', 0, 1, 0);

INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(93101, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'ful:fulfillment:page', '履约单列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 931, 1, 'ENABLED', 0, 1, 0),
(93102, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'ful:fulfillment:get', '履约单详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 931, 2, 'ENABLED', 0, 1, 0),
(93103, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'ful:fulfillment:reserve-retry', '重试预占', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 931, 3, 'ENABLED', 0, 1, 0),
(93104, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'ful:fulfillment:cancel', '取消履约', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 931, 4, 'ENABLED', 0, 1, 0);

-- 授权给角色 ADMIN(role_id=1)
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(240, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 930),
(241, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 931),
(242, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 93101),(243, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 93102),(244, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 93103),(245, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 93104);

SELECT 'ful menu and permissions inserted' AS result;
