-- 仅补充「数据权限 / 租户 / 工厂」三个菜单及 ADMIN 授权（执行后重新登录生效）
-- 用于：执行过仅含 900-903 的初始化后，看不到这三个菜单时执行本脚本
-- 使用 INSERT IGNORE，重复执行不会报错

-- 菜单：数据权限(904)、租户(905)、工厂(906)，factory_id=0 表示任意工厂均展示
INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(904, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:scope:list', '数据权限', 'MENU', '/system/data-scope', 'system/data-scope/index', NULL, NULL, NULL, NULL, 900, 3, 'ENABLED', 0, 1, 0),
(905, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:tenant:list', '租户', 'MENU', '/system/tenant', 'system/tenant/index', NULL, NULL, NULL, NULL, 900, 5, 'ENABLED', 0, 1, 0),
(906, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:factory:list', '工厂', 'MENU', '/system/factory', 'system/factory/index', NULL, NULL, NULL, NULL, 900, 6, 'ENABLED', 0, 1, 0);

-- 数据权限/租户/工厂 按钮权限（factory_id=0）
INSERT IGNORE INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(90401, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:scope:query', '查询范围', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 904, 1, 'ENABLED', 0, 1, 0),
(90402, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:scope:update', '保存范围', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 904, 2, 'ENABLED', 0, 1, 0),
(90501, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:tenant:page', '租户列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 905, 1, 'ENABLED', 0, 1, 0),
(90502, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:tenant:create', '新增租户', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 905, 2, 'ENABLED', 0, 1, 0),
(90503, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:tenant:update', '编辑租户', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 905, 3, 'ENABLED', 0, 1, 0),
(90504, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:tenant:delete', '删除租户', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 905, 4, 'ENABLED', 0, 1, 0),
(90601, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:factory:page', '工厂列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 906, 1, 'ENABLED', 0, 1, 0),
(90602, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:factory:create', '新增工厂', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 906, 2, 'ENABLED', 0, 1, 0),
(90603, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:factory:update', '编辑工厂', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 906, 3, 'ENABLED', 0, 1, 0),
(90604, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:factory:delete', '删除工厂', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 906, 4, 'ENABLED', 0, 1, 0);

-- 为角色 ADMIN(role_id=1) 授权上述菜单与按钮（若 id 冲突可改为更大 id 或先查最大 id）
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(100, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 904),
(101, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90401),
(102, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90402),
(103, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 905),
(104, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90501),
(105, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90502),
(106, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90503),
(107, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90504),
(108, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 906),
(109, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90601),
(110, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90602),
(111, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90603),
(112, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90604);
