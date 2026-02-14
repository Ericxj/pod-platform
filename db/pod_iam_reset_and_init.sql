-- IAM 全表清空并重新初始化
-- 执行前请确认数据库为测试/开发环境；执行后 admin/operator 密码均为 123456（BCrypt）

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 清空所有 iam 表（先删关联表，再删主表）
DELETE FROM `iam_user_role`;
DELETE FROM `iam_role_permission`;
DELETE FROM `iam_data_scope`;
DELETE FROM `iam_user`;
DELETE FROM `iam_role`;
DELETE FROM `iam_permission`;
DELETE FROM `iam_factory`;
DELETE FROM `iam_tenant`;

SET FOREIGN_KEY_CHECKS = 1;

-- 2. 租户
INSERT INTO `iam_tenant` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`tenant_code`,`tenant_name`,`status`,`plan_type`,`plan_expire_at`) VALUES
(1, 0, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'default', 'Default Tenant', 'ENABLED', NULL, NULL);

-- 3. 工厂
INSERT INTO `iam_factory` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`factory_code`,`factory_name`,`country_code`,`province`,`city`,`address`,`contact_name`,`contact_phone`,`status`) VALUES
(1, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'F1', 'Factory One', NULL, NULL, NULL, NULL, NULL, NULL, 'ENABLED'),
(2, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'F2', 'Factory Two', NULL, NULL, NULL, NULL, NULL, NULL, 'ENABLED');

-- 4. 权限（系统管理 + 用户/角色/权限菜单及按钮 + 数据权限/租户/工厂菜单及按钮）
INSERT INTO `iam_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`perm_code`,`perm_name`,`perm_type`,`menu_path`,`component`,`icon`,`redirect`,`api_method`,`api_path`,`parent_id`,`sort_no`,`status`,`hidden`,`keep_alive`,`always_show`) VALUES
(900, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:manage', '系统管理', 'MENU', '/system', 'LAYOUT', NULL, NULL, NULL, NULL, 0, 90, 'ENABLED', 0, 1, 0),
(901, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:user:list', '用户管理', 'MENU', '/system/user', '/system/user/index', NULL, NULL, NULL, NULL, 900, 1, 'ENABLED', 0, 1, 0),
(902, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:role:list', '角色管理', 'MENU', '/system/role', '/system/role/index', NULL, NULL, NULL, NULL, 900, 2, 'ENABLED', 0, 1, 0),
(903, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:perm:list', '权限管理', 'MENU', '/system/permission', '/system/permission/index', NULL, NULL, NULL, NULL, 900, 4, 'ENABLED', 0, 1, 0),
(904, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:scope:list', '数据权限', 'MENU', '/system/data-scope', 'system/data-scope/index', NULL, NULL, NULL, NULL, 900, 3, 'ENABLED', 0, 1, 0),
(905, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:tenant:list', '租户', 'MENU', '/system/tenant', 'system/tenant/index', NULL, NULL, NULL, NULL, 900, 5, 'ENABLED', 0, 1, 0),
(906, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'sys:factory:list', '工厂', 'MENU', '/system/factory', 'system/factory/index', NULL, NULL, NULL, NULL, 900, 6, 'ENABLED', 0, 1, 0),
(90101, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:user:list', '用户列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 901, 1, 'ENABLED', 0, 1, 0),
(90102, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:user:create', '新增用户', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 901, 2, 'ENABLED', 0, 1, 0),
(90103, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:user:update', '编辑用户', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 901, 3, 'ENABLED', 0, 1, 0),
(90104, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:user:delete', '删除用户', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 901, 4, 'ENABLED', 0, 1, 0),
(90105, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:user:query', '查询用户', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 901, 5, 'ENABLED', 0, 1, 0),
(90106, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:user:reset_pwd', '重置密码', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 901, 6, 'ENABLED', 0, 1, 0),
(90201, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:role:page', '角色列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 902, 1, 'ENABLED', 0, 1, 0),
(90202, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:role:create', '新增角色', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 902, 2, 'ENABLED', 0, 1, 0),
(90203, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:role:update', '编辑角色', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 902, 3, 'ENABLED', 0, 1, 0),
(90204, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:role:delete', '删除角色', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 902, 4, 'ENABLED', 0, 1, 0),
(90205, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:role:grant', '分配权限', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 902, 5, 'ENABLED', 0, 1, 0),
(90301, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:perm:page', '权限列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 903, 1, 'ENABLED', 0, 1, 0),
(90302, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:perm:create', '新增权限', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 903, 2, 'ENABLED', 0, 1, 0),
(90303, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:perm:update', '编辑权限', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 903, 3, 'ENABLED', 0, 1, 0),
(90304, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'iam:perm:delete', '删除权限', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 903, 4, 'ENABLED', 0, 1, 0),
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

-- 5. 角色（ADMIN / OPERATOR，role_type 与 DDL 一致：SYSTEM / BUSINESS）
INSERT INTO `iam_role` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_code`,`role_name`,`role_type`,`status`,`remark`) VALUES
(1, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'ADMIN', 'Administrator', 'SYSTEM', 'ENABLED', NULL),
(2, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'OPERATOR', 'Operator', 'BUSINESS', 'ENABLED', NULL);

-- 6. 角色-权限（ADMIN 拥有全部菜单+按钮，OPERATOR 拥有部分）
INSERT INTO `iam_role_permission` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`role_id`,`perm_id`) VALUES
(1, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 900),
(2, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 901),
(3, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90101),
(4, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90102),
(5, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90103),
(6, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90104),
(7, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90105),
(8, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90106),
(9, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 2, 900),
(10, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 2, 901),
(11, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 2, 90101),
(12, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 2, 90105),
(13, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 902),
(14, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90201),
(15, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90202),
(16, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90203),
(17, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90204),
(18, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90205),
(19, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 903),
(20, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90301),
(21, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90302),
(22, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90303),
(23, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 90304),
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

-- 7. 用户（admin / operator，密码均为 123456 的 BCrypt）
INSERT INTO `iam_user` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`username`,`password_hash`,`real_name`,`email`,`phone`,`status`,`last_login_at`) VALUES
(1, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'admin', '$2a$10$GzCERpucEK3zfFAHJKOBFeliWWhbaeXI9n3.HtKTk8XkHJxcGVRSG', 'Administrator', NULL, NULL, 'ENABLED', NULL),
(2, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'operator', '$2a$10$GzCERpucEK3zfFAHJKOBFeliWWhbaeXI9n3.HtKTk8XkHJxcGVRSG', 'Operator User', NULL, NULL, 'ENABLED', NULL);

-- 8. 用户-角色
INSERT INTO `iam_user_role` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`user_id`,`role_id`) VALUES
(1, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 1, 1),
(2, 1, 1, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 2, 2);

-- 9. 数据权限（ADMIN 可访问工厂 1、2，OPERATOR 可访问工厂 1）
INSERT INTO `iam_data_scope` (`id`,`tenant_id`,`factory_id`,`created_at`,`updated_at`,`deleted`,`version`,`created_by`,`updated_by`,`trace_id`,`subject_type`,`subject_id`,`scope_type`,`scope_id`,`status`) VALUES
(1, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'ROLE', 1, 'FACTORY', 1, 'ENABLED'),
(2, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'ROLE', 1, 'FACTORY', 2, 'ENABLED'),
(3, 1, 0, NOW(3), NOW(3), 0, 1, NULL, NULL, NULL, 'ROLE', 2, 'FACTORY', 1, 'ENABLED');
