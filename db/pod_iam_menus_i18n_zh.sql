-- 将系统管理菜单与按钮名称改为中文（用于已有库的更新）
-- 执行后请重新登录或刷新菜单以生效

UPDATE `iam_permission` SET `perm_name` = '系统管理' WHERE `id` = 900;
UPDATE `iam_permission` SET `perm_name` = '用户管理' WHERE `id` = 901;
UPDATE `iam_permission` SET `perm_name` = '角色管理' WHERE `id` = 902;
UPDATE `iam_permission` SET `perm_name` = '权限管理' WHERE `id` = 903;
UPDATE `iam_permission` SET `perm_name` = '数据权限' WHERE `id` = 904;
UPDATE `iam_permission` SET `perm_name` = '租户' WHERE `id` = 905;
UPDATE `iam_permission` SET `perm_name` = '工厂' WHERE `id` = 906;

UPDATE `iam_permission` SET `perm_name` = '用户列表' WHERE `id` = 90101;
UPDATE `iam_permission` SET `perm_name` = '新增用户' WHERE `id` = 90102;
UPDATE `iam_permission` SET `perm_name` = '编辑用户' WHERE `id` = 90103;
UPDATE `iam_permission` SET `perm_name` = '删除用户' WHERE `id` = 90104;
UPDATE `iam_permission` SET `perm_name` = '查询用户' WHERE `id` = 90105;
UPDATE `iam_permission` SET `perm_name` = '重置密码' WHERE `id` = 90106;

UPDATE `iam_permission` SET `perm_name` = '角色列表' WHERE `id` = 90201;
UPDATE `iam_permission` SET `perm_name` = '新增角色' WHERE `id` = 90202;
UPDATE `iam_permission` SET `perm_name` = '编辑角色' WHERE `id` = 90203;
UPDATE `iam_permission` SET `perm_name` = '删除角色' WHERE `id` = 90204;
UPDATE `iam_permission` SET `perm_name` = '分配权限' WHERE `id` = 90205;

UPDATE `iam_permission` SET `perm_name` = '权限列表' WHERE `id` = 90301;
UPDATE `iam_permission` SET `perm_name` = '新增权限' WHERE `id` = 90302;
UPDATE `iam_permission` SET `perm_name` = '编辑权限' WHERE `id` = 90303;
UPDATE `iam_permission` SET `perm_name` = '删除权限' WHERE `id` = 90304;

UPDATE `iam_permission` SET `perm_name` = '查询范围' WHERE `id` = 90401;
UPDATE `iam_permission` SET `perm_name` = '保存范围' WHERE `id` = 90402;

UPDATE `iam_permission` SET `perm_name` = '租户列表' WHERE `id` = 90501;
UPDATE `iam_permission` SET `perm_name` = '新增租户' WHERE `id` = 90502;
UPDATE `iam_permission` SET `perm_name` = '编辑租户' WHERE `id` = 90503;
UPDATE `iam_permission` SET `perm_name` = '删除租户' WHERE `id` = 90504;

UPDATE `iam_permission` SET `perm_name` = '工厂列表' WHERE `id` = 90601;
UPDATE `iam_permission` SET `perm_name` = '新增工厂' WHERE `id` = 90602;
UPDATE `iam_permission` SET `perm_name` = '编辑工厂' WHERE `id` = 90603;
UPDATE `iam_permission` SET `perm_name` = '删除工厂' WHERE `id` = 90604;
