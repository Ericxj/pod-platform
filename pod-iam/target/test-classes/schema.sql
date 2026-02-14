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
