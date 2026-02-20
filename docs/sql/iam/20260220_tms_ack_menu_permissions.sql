-- P1.6 TMS 回传任务 菜单+按钮+API 权限（UPSERT + ADMIN 授权）
-- 兼容 uk_perm_code；可复活 deleted=1
SET NAMES utf8mb4;

-- 菜单：tms:center（970 可能已存在）；回传任务子菜单 tms:ack:list（972）
INSERT INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(970, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:center', 'TMS', 'MENU', '/tms', 'LAYOUT', 'car', NULL, NULL, NULL, 0, 33, 'ENABLED', 0, 1, 0),
(972, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:ack:list', '回传任务', 'MENU', '/tms/acks', 'tms/acks/index', NULL, NULL, NULL, NULL, 970, 2, 'ENABLED', 0, 1, 0)
ON DUPLICATE KEY UPDATE `perm_code`=VALUES(`perm_code`), `perm_name`=VALUES(`perm_name`), `perm_type`=VALUES(`perm_type`), `menu_path`=VALUES(`menu_path`), `component`=VALUES(`component`), `parent_id`=VALUES(`parent_id`), `sort_no`=VALUES(`sort_no`), `deleted`=0, `updated_at`=NOW(3);

-- BUTTON
INSERT INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(97201, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:ack:page', '回传任务列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 972, 1, 'ENABLED', 0, 1, 0),
(97202, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:ack:get', '回传任务详情', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 972, 2, 'ENABLED', 0, 1, 0),
(97203, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:ack:retry', '手动重试', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 972, 3, 'ENABLED', 0, 1, 0),
(97204, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:ack:create', '从出库单创建', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 972, 4, 'ENABLED', 0, 1, 0)
ON DUPLICATE KEY UPDATE `perm_name`=VALUES(`perm_name`), `deleted`=0, `updated_at`=NOW(3);

-- API（perm_type='API' + api_method + api_path）
INSERT INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(97211, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:api:acks:page', 'GET回传列表', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/tms/acks', 972, 10, 'ENABLED', 0, 1, 0),
(97212, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:api:acks:get', 'GET回传详情', 'API', NULL, NULL, NULL, NULL, 'GET', '/api/tms/acks/*', 972, 11, 'ENABLED', 0, 1, 0),
(97213, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:api:acks:retry', 'POST回传重试', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/tms/acks/*/retry', 972, 12, 'ENABLED', 0, 1, 0),
(97214, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:api:acks:create', 'POST从出库单创建', 'API', NULL, NULL, NULL, NULL, 'POST', '/api/tms/acks/createFromOutbound', 972, 13, 'ENABLED', 0, 1, 0)
ON DUPLICATE KEY UPDATE `perm_name`=VALUES(`perm_name`), `api_method`=VALUES(`api_method`), `api_path`=VALUES(`api_path`), `deleted`=0, `updated_at`=NOW(3);

-- 授权 ADMIN(role_id=1)：按 perm_code 查 perm_id 后写入 iam_role_permission
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`)
SELECT 288, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, p.id FROM `iam_permission` p WHERE p.perm_code='tms:center' AND p.deleted=0 LIMIT 1;
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`)
SELECT 289, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, p.id FROM `iam_permission` p WHERE p.perm_code='tms:ack:list' AND p.deleted=0 LIMIT 1;
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`)
SELECT 290, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, p.id FROM `iam_permission` p WHERE p.perm_code='tms:ack:page' AND p.deleted=0 LIMIT 1;
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`)
SELECT 291, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, p.id FROM `iam_permission` p WHERE p.perm_code='tms:ack:get' AND p.deleted=0 LIMIT 1;
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`)
SELECT 292, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, p.id FROM `iam_permission` p WHERE p.perm_code='tms:ack:retry' AND p.deleted=0 LIMIT 1;
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`)
SELECT 293, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, p.id FROM `iam_permission` p WHERE p.perm_code='tms:ack:create' AND p.deleted=0 LIMIT 1;
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`)
SELECT 294, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, p.id FROM `iam_permission` p WHERE p.perm_code='tms:api:acks:page' AND p.deleted=0 LIMIT 1;
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`)
SELECT 295, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, p.id FROM `iam_permission` p WHERE p.perm_code='tms:api:acks:get' AND p.deleted=0 LIMIT 1;
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`)
SELECT 296, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, p.id FROM `iam_permission` p WHERE p.perm_code='tms:api:acks:retry' AND p.deleted=0 LIMIT 1;
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`)
SELECT 297, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, p.id FROM `iam_permission` p WHERE p.perm_code='tms:api:acks:create' AND p.deleted=0 LIMIT 1;

SELECT 'tms ack menu and permissions upserted' AS result;
