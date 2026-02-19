-- P1.3 稿件/生产图 菜单与权限：稿件中心(940)、生产图任务(941)、生产文件(942)
INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(940, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'art:center', '稿件', 'MENU', '/art', 'LAYOUT', 'file', NULL, NULL, NULL, 0, 33, 'ENABLED', 0, 1, 0),
(941, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'art:job:list', '生产图任务', 'MENU', '/art/jobs', 'art/jobs/index', NULL, NULL, NULL, NULL, 940, 1, 'ENABLED', 0, 1, 0),
(942, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'art:file:list', '生产文件', 'MENU', '/art/files', 'art/files/index', NULL, NULL, NULL, NULL, 940, 2, 'ENABLED', 0, 1, 0);

INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(94101, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'art:job:page', '任务列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 941, 1, 'ENABLED', 0, 1, 0),
(94102, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'art:job:get', '任务详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 941, 2, 'ENABLED', 0, 1, 0),
(94103, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'art:job:retry', '重试任务', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 941, 3, 'ENABLED', 0, 1, 0),
(94104, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'art:job:create', '按履约单创建', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 941, 4, 'ENABLED', 0, 1, 0),
(94201, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'art:file:page', '文件列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 942, 1, 'ENABLED', 0, 1, 0),
(94202, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'art:file:get', '文件详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 942, 2, 'ENABLED', 0, 1, 0);

-- 授权给角色 ADMIN(role_id=1)
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(250, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 940),
(251, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 941),
(252, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 942),
(253, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 94101),(254, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 94102),(255, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 94103),(256, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 94104),
(257, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 94201),(258, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 94202);

SELECT 'art menu and permissions inserted' AS result;
