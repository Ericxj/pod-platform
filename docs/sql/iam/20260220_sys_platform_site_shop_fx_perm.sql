-- 系统管理增强：平台/站点/店铺/授权/汇率 菜单与权限（UPSERT + ADMIN 授权）
SET NAMES utf8mb4;

-- 菜单（parent_id=900 系统管理）
INSERT INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(907, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:platform:list', '平台管理', 'MENU', '/system/platform', 'system/platform/index', NULL, NULL, NULL, NULL, 900, 7, 'ENABLED', 0, 1, 0),
(908, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:site:list', '站点管理', 'MENU', '/system/site', 'system/site/index', NULL, NULL, NULL, NULL, 900, 8, 'ENABLED', 0, 1, 0),
(909, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:shop:list', '店铺管理', 'MENU', '/system/shop', 'system/shop/index', NULL, NULL, NULL, NULL, 900, 9, 'ENABLED', 0, 1, 0),
(910, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:credential:list', '平台授权', 'MENU', '/system/credential', 'system/credential/index', NULL, NULL, NULL, NULL, 900, 10, 'ENABLED', 0, 1, 0),
(911, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:fx:list', '汇率管理', 'MENU', '/system/fx-rate', 'system/fx-rate/index', NULL, NULL, NULL, NULL, 900, 11, 'ENABLED', 0, 1, 0)
ON DUPLICATE KEY UPDATE `perm_code`=VALUES(`perm_code`), `perm_name`=VALUES(`perm_name`), `menu_path`=VALUES(`menu_path`), `component`=VALUES(`component`), `parent_id`=VALUES(`parent_id`), `sort_no`=VALUES(`sort_no`), `deleted`=0, `updated_at`=NOW(3);

-- BUTTON
INSERT INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(90701, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:platform:page', '列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 907, 1, 'ENABLED', 0, 1, 0),
(90702, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:platform:get', '详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 907, 2, 'ENABLED', 0, 1, 0),
(90703, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:platform:create', '新增', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 907, 3, 'ENABLED', 0, 1, 0),
(90704, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:platform:update', '编辑', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 907, 4, 'ENABLED', 0, 1, 0),
(90705, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:platform:enable', '启用', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 907, 5, 'ENABLED', 0, 1, 0),
(90706, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:platform:disable', '禁用', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 907, 6, 'ENABLED', 0, 1, 0),
(90801, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:site:page', '列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 908, 1, 'ENABLED', 0, 1, 0),
(90802, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:site:get', '详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 908, 2, 'ENABLED', 0, 1, 0),
(90803, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:site:create', '新增', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 908, 3, 'ENABLED', 0, 1, 0),
(90804, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:site:update', '编辑', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 908, 4, 'ENABLED', 0, 1, 0),
(90805, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:site:enable', '启用', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 908, 5, 'ENABLED', 0, 1, 0),
(90806, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:site:disable', '禁用', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 908, 6, 'ENABLED', 0, 1, 0),
(90901, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:shop:page', '列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 909, 1, 'ENABLED', 0, 1, 0),
(90902, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:shop:get', '详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 909, 2, 'ENABLED', 0, 1, 0),
(90903, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:shop:create', '新增', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 909, 3, 'ENABLED', 0, 1, 0),
(90904, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:shop:update', '编辑', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 909, 4, 'ENABLED', 0, 1, 0),
(90905, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:shop:enable', '启用', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 909, 5, 'ENABLED', 0, 1, 0),
(90906, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:shop:disable', '禁用', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 909, 6, 'ENABLED', 0, 1, 0),
(91001, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:credential:page', '列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 910, 1, 'ENABLED', 0, 1, 0),
(91002, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:credential:get', '详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 910, 2, 'ENABLED', 0, 1, 0),
(91003, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:credential:create', '新增', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 910, 3, 'ENABLED', 0, 1, 0),
(91004, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:credential:update', '编辑', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 910, 4, 'ENABLED', 0, 1, 0),
(91005, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:credential:enable', '启用', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 910, 5, 'ENABLED', 0, 1, 0),
(91006, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:credential:disable', '禁用', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 910, 6, 'ENABLED', 0, 1, 0),
(91007, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:credential:test', '测试连接', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 910, 7, 'ENABLED', 0, 1, 0),
(91101, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:fx:page', '列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 911, 1, 'ENABLED', 0, 1, 0),
(91102, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:fx:get', '详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 911, 2, 'ENABLED', 0, 1, 0),
(91103, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:fx:create', '新增', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 911, 3, 'ENABLED', 0, 1, 0),
(91104, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:fx:update', '编辑', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 911, 4, 'ENABLED', 0, 1, 0),
(91105, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:fx:enable', '启用', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 911, 5, 'ENABLED', 0, 1, 0),
(91106, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:fx:disable', '禁用', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 911, 6, 'ENABLED', 0, 1, 0)
ON DUPLICATE KEY UPDATE `perm_name`=VALUES(`perm_name`), `deleted`=0, `updated_at`=NOW(3);

-- API（perm_type='API' + api_method + api_path）
INSERT INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(90711, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:platforms:page', 'GET平台列表', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/sys/platforms', 907, 10, 'ENABLED', 0, 1, 0),
(90712, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:platforms:get', 'GET平台详情', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/sys/platforms/*', 907, 11, 'ENABLED', 0, 1, 0),
(90713, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:platforms:create', 'POST平台', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/platforms', 907, 12, 'ENABLED', 0, 1, 0),
(90714, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:platforms:update', 'PUT平台', 'API', NULL, NULL, NULL, NULL, 'PUT', '/api/sys/platforms/*', 907, 13, 'ENABLED', 0, 1, 0),
(90715, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:platforms:enable', 'POST平台启用', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/platforms/*/enable', 907, 14, 'ENABLED', 0, 1, 0),
(90716, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:platforms:disable', 'POST平台禁用', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/platforms/*/disable', 907, 15, 'ENABLED', 0, 1, 0),
(90811, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:sites:page', 'GET站点列表', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/sys/sites', 908, 10, 'ENABLED', 0, 1, 0),
(90812, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:sites:get', 'GET站点详情', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/sys/sites/*', 908, 11, 'ENABLED', 0, 1, 0),
(90813, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:sites:create', 'POST站点', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/sites', 908, 12, 'ENABLED', 0, 1, 0),
(90814, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:sites:update', 'PUT站点', 'API', NULL, NULL, NULL, NULL, 'PUT', '/api/sys/sites/*', 908, 13, 'ENABLED', 0, 1, 0),
(90815, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:sites:enable', 'POST站点启用', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/sites/*/enable', 908, 14, 'ENABLED', 0, 1, 0),
(90816, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:sites:disable', 'POST站点禁用', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/sites/*/disable', 908, 15, 'ENABLED', 0, 1, 0),
(90911, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:shops:page', 'GET店铺列表', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/sys/shops', 909, 10, 'ENABLED', 0, 1, 0),
(90912, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:shops:get', 'GET店铺详情', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/sys/shops/*', 909, 11, 'ENABLED', 0, 1, 0),
(90913, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:shops:create', 'POST店铺', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/shops', 909, 12, 'ENABLED', 0, 1, 0),
(90914, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:shops:update', 'PUT店铺', 'API', NULL, NULL, NULL, NULL, 'PUT', '/api/sys/shops/*', 909, 13, 'ENABLED', 0, 1, 0),
(90915, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:shops:enable', 'POST店铺启用', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/shops/*/enable', 909, 14, 'ENABLED', 0, 1, 0),
(90916, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:shops:disable', 'POST店铺禁用', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/shops/*/disable', 909, 15, 'ENABLED', 0, 1, 0),
(91011, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:credentials:page', 'GET授权列表', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/sys/credentials', 910, 10, 'ENABLED', 0, 1, 0),
(91012, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:credentials:get', 'GET授权详情', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/sys/credentials/*', 910, 11, 'ENABLED', 0, 1, 0),
(91013, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:credentials:create', 'POST授权', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/credentials', 910, 12, 'ENABLED', 0, 1, 0),
(91014, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:credentials:update', 'PUT授权', 'API', NULL, NULL, NULL, NULL, 'PUT', '/api/sys/credentials/*', 910, 13, 'ENABLED', 0, 1, 0),
(91015, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:credentials:enable', 'POST授权启用', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/credentials/*/enable', 910, 14, 'ENABLED', 0, 1, 0),
(91016, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:credentials:disable', 'POST授权禁用', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/credentials/*/disable', 910, 15, 'ENABLED', 0, 1, 0),
(91017, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:credentials:test', 'POST授权测试', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/credentials/*/test', 910, 16, 'ENABLED', 0, 1, 0),
(91111, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:fx-rates:page', 'GET汇率列表', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/sys/fx-rates', 911, 10, 'ENABLED', 0, 1, 0),
(91112, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:fx-rates:get', 'GET汇率详情', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/sys/fx-rates/*', 911, 11, 'ENABLED', 0, 1, 0),
(91113, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:fx-rates:create', 'POST汇率', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/fx-rates', 911, 12, 'ENABLED', 0, 1, 0),
(91114, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:fx-rates:update', 'PUT汇率', 'API', NULL, NULL, NULL, NULL, 'PUT', '/api/sys/fx-rates/*', 911, 13, 'ENABLED', 0, 1, 0),
(91115, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:fx-rates:enable', 'POST汇率启用', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/fx-rates/*/enable', 911, 14, 'ENABLED', 0, 1, 0),
(91116, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:fx-rates:disable', 'POST汇率禁用', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/sys/fx-rates/*/disable', 911, 15, 'ENABLED', 0, 1, 0),
(91117, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:api:fx-rates:quote', 'GET汇率查询', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/sys/fx-rates/quote', 911, 16, 'ENABLED', 0, 1, 0)
ON DUPLICATE KEY UPDATE `perm_name`=VALUES(`perm_name`), `api_method`=VALUES(`api_method`), `api_path`=VALUES(`api_path`), `deleted`=0, `updated_at`=NOW(3);

-- 授权 ADMIN(role_id=1)：使用 id 区间 900400+ 避免与已有数据冲突，确保 INSERT 成功
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900400, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 907);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900401, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 908);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900402, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 909);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900403, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 910);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900404, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 911);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900405, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90701);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900406, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90702);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900407, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90703);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900408, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90704);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900409, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90705);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900410, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90706);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900411, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90801);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900412, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90802);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900413, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90803);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900414, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90804);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900415, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90805);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900416, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90806);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900417, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90901);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900418, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90902);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900419, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90903);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900420, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90904);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900421, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90905);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900422, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90906);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900423, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91001);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900424, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91002);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900425, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91003);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900426, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91004);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900427, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91005);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900428, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91006);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900429, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91007);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900430, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91101);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900431, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91102);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900432, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91103);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900433, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91104);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900434, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91105);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900435, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91106);
-- API 权限授权 ADMIN
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900436, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90711),(900437, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90712),(900438, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90713),(900439, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90714),(900440, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90715),(900441, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90716);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900442, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90811),(900443, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90812),(900444, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90813),(900445, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90814),(900446, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90815),(900447, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90816);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900448, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90911),(900449, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90912),(900450, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90913),(900451, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90914),(900452, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90915),(900453, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90916);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900454, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91011),(900455, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91012),(900456, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91013),(900457, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91014),(900458, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91015),(900459, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91016),(900460, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91017);
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES (900461, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91111),(900462, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91112),(900463, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91113),(900464, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91114),(900465, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91115),(900466, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91116),(900467, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 91117);

SELECT 'sys platform site shop fx permissions upserted' AS result;
