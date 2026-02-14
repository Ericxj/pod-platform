CREATE TABLE IF NOT EXISTS sys_ai_diagnosis (
  id bigint NOT NULL,
  trace_id varchar(64) NOT NULL,
  business_key varchar(128) DEFAULT NULL,
  status varchar(32) NOT NULL DEFAULT 'PENDING',
  diagnosis_type varchar(64) NOT NULL,
  result_json text,
  tenant_id bigint DEFAULT NULL,
  factory_id bigint DEFAULT NULL,
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime DEFAULT CURRENT_TIMESTAMP,
  created_by bigint DEFAULT NULL,
  updated_by bigint DEFAULT NULL,
  deleted tinyint DEFAULT '0',
  version int DEFAULT '1',
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS sys_idempotent (
  id bigint NOT NULL AUTO_INCREMENT,
  key_id varchar(128) NOT NULL,
  record_value varchar(255) DEFAULT NULL,
  expire_at datetime NOT NULL,
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime DEFAULT CURRENT_TIMESTAMP,
  tenant_id bigint DEFAULT NULL,
  factory_id bigint DEFAULT NULL,
  created_by bigint DEFAULT NULL,
  updated_by bigint DEFAULT NULL,
  deleted tinyint DEFAULT '0',
  version int DEFAULT '1',
  trace_id varchar(64) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_key_id (key_id)
);

-- For IamRoleApplicationService / RoleController integration tests
CREATE TABLE IF NOT EXISTS iam_role (
  id bigint NOT NULL AUTO_INCREMENT,
  tenant_id bigint NOT NULL,
  factory_id bigint NOT NULL,
  created_at datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  updated_at datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  deleted tinyint NOT NULL DEFAULT 0,
  version int NOT NULL DEFAULT 0,
  created_by bigint DEFAULT NULL,
  updated_by bigint DEFAULT NULL,
  trace_id varchar(64) DEFAULT NULL,
  role_code varchar(64) NOT NULL,
  role_name varchar(128) NOT NULL,
  role_type varchar(16) NOT NULL,
  status varchar(16) NOT NULL,
  remark varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (tenant_id, factory_id, role_code, deleted)
);

CREATE TABLE IF NOT EXISTS iam_role_permission (
  id bigint NOT NULL AUTO_INCREMENT,
  tenant_id bigint NOT NULL,
  factory_id bigint NOT NULL,
  created_at datetime(3) DEFAULT NULL,
  updated_at datetime(3) DEFAULT NULL,
  deleted tinyint NOT NULL DEFAULT 0,
  version int NOT NULL DEFAULT 0,
  created_by bigint DEFAULT NULL,
  updated_by bigint DEFAULT NULL,
  trace_id varchar(64) DEFAULT NULL,
  role_id bigint NOT NULL,
  perm_id bigint NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_perm (tenant_id, factory_id, role_id, perm_id, deleted)
);

CREATE TABLE IF NOT EXISTS iam_permission (
  id bigint NOT NULL,
  tenant_id bigint NOT NULL,
  factory_id bigint NOT NULL,
  created_at datetime(3) DEFAULT NULL,
  updated_at datetime(3) DEFAULT NULL,
  deleted tinyint NOT NULL DEFAULT 0,
  version int NOT NULL DEFAULT 0,
  created_by bigint DEFAULT NULL,
  updated_by bigint DEFAULT NULL,
  trace_id varchar(64) DEFAULT NULL,
  perm_code varchar(128) NOT NULL,
  perm_name varchar(128) NOT NULL,
  perm_type varchar(16) NOT NULL,
  menu_path varchar(255) DEFAULT NULL,
  component varchar(255) DEFAULT NULL,
  icon varchar(64) DEFAULT NULL,
  redirect varchar(255) DEFAULT NULL,
  api_method varchar(16) DEFAULT NULL,
  api_path varchar(255) DEFAULT NULL,
  parent_id bigint DEFAULT NULL,
  sort_no int NOT NULL DEFAULT 0,
  status varchar(16) NOT NULL,
  hidden tinyint(1) DEFAULT 0,
  keep_alive tinyint(1) DEFAULT 1,
  always_show tinyint(1) DEFAULT 0,
  PRIMARY KEY (id)
);

INSERT INTO iam_permission (id, tenant_id, factory_id, deleted, version, perm_code, perm_name, perm_type, parent_id, sort_no, status) VALUES
(900, 1, 1, 0, 1, 'sys:manage', 'System', 'MENU', 0, 90, 'ENABLED'),
(901, 1, 1, 0, 1, 'sys:user:list', 'User', 'MENU', 900, 1, 'ENABLED'),
(902, 1, 1, 0, 1, 'sys:role:list', 'Role', 'MENU', 900, 2, 'ENABLED'),
(90201, 1, 1, 0, 1, 'iam:role:page', 'Role Page', 'BUTTON', 902, 1, 'ENABLED'),
(90202, 1, 1, 0, 1, 'iam:role:create', 'Role Create', 'BUTTON', 902, 2, 'ENABLED');

-- For DataScopeService / switchFactory tests
CREATE TABLE IF NOT EXISTS iam_user (
  id bigint NOT NULL AUTO_INCREMENT,
  tenant_id bigint NOT NULL,
  factory_id bigint NOT NULL,
  created_at datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  updated_at datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  deleted tinyint NOT NULL DEFAULT 0,
  version int NOT NULL DEFAULT 0,
  created_by bigint DEFAULT NULL,
  updated_by bigint DEFAULT NULL,
  trace_id varchar(64) DEFAULT NULL,
  username varchar(64) NOT NULL,
  password_hash varchar(128) NOT NULL,
  real_name varchar(64) DEFAULT NULL,
  email varchar(128) DEFAULT NULL,
  phone varchar(32) DEFAULT NULL,
  status varchar(16) NOT NULL,
  last_login_at datetime(3) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS iam_user_role (
  id bigint NOT NULL AUTO_INCREMENT,
  tenant_id bigint NOT NULL,
  factory_id bigint NOT NULL,
  created_at datetime(3) DEFAULT NULL,
  updated_at datetime(3) DEFAULT NULL,
  deleted tinyint NOT NULL DEFAULT 0,
  version int NOT NULL DEFAULT 0,
  created_by bigint DEFAULT NULL,
  updated_by bigint DEFAULT NULL,
  trace_id varchar(64) DEFAULT NULL,
  user_id bigint NOT NULL,
  role_id bigint NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS iam_data_scope (
  id bigint NOT NULL AUTO_INCREMENT,
  tenant_id bigint NOT NULL,
  factory_id bigint NOT NULL,
  created_at datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  updated_at datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  deleted tinyint NOT NULL DEFAULT 0,
  version int NOT NULL DEFAULT 0,
  created_by bigint DEFAULT NULL,
  updated_by bigint DEFAULT NULL,
  trace_id varchar(64) DEFAULT NULL,
  subject_type varchar(32) NOT NULL,
  subject_id bigint NOT NULL,
  scope_type varchar(32) NOT NULL,
  scope_id bigint NOT NULL,
  status varchar(16) NOT NULL,
  PRIMARY KEY (id)
);
