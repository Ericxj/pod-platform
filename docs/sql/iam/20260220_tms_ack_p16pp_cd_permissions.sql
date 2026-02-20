-- P1.6++ C+D 回传任务 权限增量（可选）：自愈重试、包裹查看
SET NAMES utf8mb4;

-- BUTTON（可选）
INSERT INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(97205, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:ack:self-heal:retry', '自愈后重试', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 972, 5, 'ENABLED', 0, 1, 0),
(97206, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'tms:ack:pack:view', '包裹明细', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 972, 6, 'ENABLED', 0, 1, 0)
ON DUPLICATE KEY UPDATE `perm_name`=VALUES(`perm_name`), `deleted`=0, `updated_at`=NOW(3);

-- 授权 ADMIN(role_id=1)
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`)
SELECT 298, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, p.id FROM `iam_permission` p WHERE p.perm_code='tms:ack:self-heal:retry' AND p.deleted=0 LIMIT 1;
INSERT IGNORE INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`)
SELECT 299, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, p.id FROM `iam_permission` p WHERE p.perm_code='tms:ack:pack:view' AND p.deleted=0 LIMIT 1;

SELECT 'tms ack p16++ cd permissions upserted' AS result;
