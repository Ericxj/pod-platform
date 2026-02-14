/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80300 (8.3.0)
 Source Host           : localhost:3306
 Source Schema         : pod_system

 Target Server Type    : MySQL
 Target Server Version : 80300 (8.3.0)
 File Encoding         : 65001

 Date: 14/02/2026 13:55:20
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_task
-- ----------------------------
DROP TABLE IF EXISTS `ai_task`;
CREATE TABLE `ai_task`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `task_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务单号',
  `task_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务类型',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型',
  `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务单号',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'CREATED' COMMENT '状态',
  `payload_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '输入参数',
  `result_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '输出结果',
  `error_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `attempts` int NOT NULL DEFAULT 0 COMMENT '尝试次数',
  `max_attempts` int NOT NULL DEFAULT 3 COMMENT '最大尝试次数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_no`(`tenant_id` ASC, `factory_id` ASC, `task_no` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_biz`(`tenant_id` ASC, `factory_id` ASC, `biz_type` ASC, `biz_no` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_status`(`tenant_id` ASC, `factory_id` ASC, `status` ASC, `updated_at` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI任务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_task
-- ----------------------------

-- ----------------------------
-- Table structure for art_asset
-- ----------------------------
DROP TABLE IF EXISTS `art_asset`;
CREATE TABLE `art_asset`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `asset_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'USER_IMAGE/FONT/ELEMENT',
  `asset_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '素材名称',
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件URL(OSS/S3)',
  `file_sha256` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件哈希',
  `meta_json` json NULL COMMENT '元数据(宽高/格式/ICC等)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_asset_type`(`tenant_id` ASC, `asset_type` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_asset_sha`(`tenant_id` ASC, `file_sha256` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '素材资产' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of art_asset
-- ----------------------------

-- ----------------------------
-- Table structure for art_job
-- ----------------------------
DROP TABLE IF EXISTS `art_job`;
CREATE TABLE `art_job`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `art_job_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '稿件任务号',
  `fulfillment_id` bigint NOT NULL COMMENT '履约单ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/RENDERING/SUCCESS/FAILED',
  `priority` int NOT NULL DEFAULT 100 COMMENT '优先级',
  `input_personalization_json` json NULL COMMENT '输入个性化数据(合并后)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_art_job_no`(`tenant_id` ASC, `art_job_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_art_job_ff`(`tenant_id` ASC, `fulfillment_id` ASC) USING BTREE,
  INDEX `idx_art_job_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '稿件任务(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of art_job
-- ----------------------------

-- ----------------------------
-- Table structure for art_job_item
-- ----------------------------
DROP TABLE IF EXISTS `art_job_item`;
CREATE TABLE `art_job_item`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `art_job_id` bigint NOT NULL COMMENT '稿件任务ID',
  `surface_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '面编码',
  `area_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域编码',
  `editable_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'TEXT/IMAGE/MIXED',
  `input_payload_json` json NOT NULL COMMENT '输入数据(文字/图片URL等)',
  `render_payload_json` json NULL COMMENT '渲染中间数据(可选)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_art_job_item`(`tenant_id` ASC, `art_job_id` ASC, `surface_code` ASC, `area_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_art_job_item_job`(`tenant_id` ASC, `art_job_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '稿件任务(行/面/区域)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of art_job_item
-- ----------------------------

-- ----------------------------
-- Table structure for art_production_file
-- ----------------------------
DROP TABLE IF EXISTS `art_production_file`;
CREATE TABLE `art_production_file`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `art_job_id` bigint NOT NULL COMMENT '稿件任务ID',
  `file_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件编号',
  `file_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'TIFF/PSD/PNG/PDF',
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件URL',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小(bytes)',
  `width_px` int NULL DEFAULT NULL COMMENT '宽(px)',
  `height_px` int NULL DEFAULT NULL COMMENT '高(px)',
  `dpi` int NULL DEFAULT NULL COMMENT 'DPI',
  `icc_profile` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ICC配置名/标识',
  `white_ink_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '白墨模式/通道规则',
  `meta_json` json NULL COMMENT '更多元数据(通道/色域/percent等)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_prod_file_no`(`tenant_id` ASC, `file_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_prod_file_job`(`tenant_id` ASC, `art_job_id` ASC) USING BTREE,
  INDEX `idx_prod_file_type`(`tenant_id` ASC, `file_type` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产图文件' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of art_production_file
-- ----------------------------

-- ----------------------------
-- Table structure for art_production_file_version
-- ----------------------------
DROP TABLE IF EXISTS `art_production_file_version`;
CREATE TABLE `art_production_file_version`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `file_id` bigint NOT NULL COMMENT '生产图文件ID',
  `version_no` int NOT NULL COMMENT '版本号',
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '版本文件URL',
  `change_note` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更说明',
  `meta_json` json NULL COMMENT '元数据',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_file_ver`(`tenant_id` ASC, `file_id` ASC, `version_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_file_ver_file`(`tenant_id` ASC, `file_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产图文件版本' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of art_production_file_version
-- ----------------------------

-- ----------------------------
-- Table structure for art_render_task
-- ----------------------------
DROP TABLE IF EXISTS `art_render_task`;
CREATE TABLE `art_render_task`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `art_job_id` bigint NOT NULL COMMENT '稿件任务ID',
  `task_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '渲染任务号',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'PENDING/RUNNING/SUCCESS/FAILED',
  `attempts` int NOT NULL DEFAULT 0 COMMENT '尝试次数',
  `max_attempts` int NOT NULL DEFAULT 5 COMMENT '最大重试',
  `last_error` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后错误',
  `payload_json` json NULL COMMENT '渲染参数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_render_task_no`(`tenant_id` ASC, `task_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_render_task_job`(`tenant_id` ASC, `art_job_id` ASC) USING BTREE,
  INDEX `idx_render_task_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '渲染任务(异步/可重试)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of art_render_task
-- ----------------------------

-- ----------------------------
-- Table structure for bi_dashboard_def
-- ----------------------------
DROP TABLE IF EXISTS `bi_dashboard_def`;
CREATE TABLE `bi_dashboard_def`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `dashboard_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '看板编码',
  `dashboard_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '看板名称',
  `dashboard_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型(OPERATIONS/FINANCE/PRODUCTION/LOGISTICS)',
  `layout_json` json NULL COMMENT '布局(组件/筛选/图表配置)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dashboard_code`(`tenant_id` ASC, `dashboard_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '看板定义(数据中台配置化)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bi_dashboard_def
-- ----------------------------

-- ----------------------------
-- Table structure for bi_dashboard_metric
-- ----------------------------
DROP TABLE IF EXISTS `bi_dashboard_metric`;
CREATE TABLE `bi_dashboard_metric`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `dashboard_id` bigint NOT NULL COMMENT '看板ID',
  `metric_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '指标编码',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `config_json` json NULL COMMENT '组件配置(维度/过滤/展示)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dashboard_metric`(`tenant_id` ASC, `dashboard_id` ASC, `metric_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_dash_metric_dash`(`tenant_id` ASC, `dashboard_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '看板-指标绑定' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bi_dashboard_metric
-- ----------------------------

-- ----------------------------
-- Table structure for bi_metric_def
-- ----------------------------
DROP TABLE IF EXISTS `bi_metric_def`;
CREATE TABLE `bi_metric_def`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `metric_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '指标编码',
  `metric_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '指标名称',
  `metric_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型(COUNT/AMOUNT/RATIO/TIME)',
  `default_currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认币种(金额类)',
  `grain` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '粒度(DAY/WEEK/MONTH)',
  `owner_domain` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '归属域(OMS/WMS/MES/TMS/FIN)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '口径说明',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_metric_def`(`tenant_id` ASC, `metric_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_metric_def_domain`(`tenant_id` ASC, `owner_domain` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '指标定义(口径/归属域/币种)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bi_metric_def
-- ----------------------------

-- ----------------------------
-- Table structure for bi_metric_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `bi_metric_snapshot`;
CREATE TABLE `bi_metric_snapshot`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `snapshot_date` date NOT NULL COMMENT '快照日期',
  `metric_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '指标编码(如 GMV/ORDER_CNT/SHIP_COST)',
  `dim_json` json NULL COMMENT '维度(店铺/平台/工厂/仓库/渠道等)',
  `metric_value` decimal(20, 6) NOT NULL COMMENT '指标值',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '币种(金额类)',
  `meta_json` json NULL COMMENT '扩展',
  `dim_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维度哈希(用于去重/唯一约束)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_metric`(`tenant_id` ASC, `snapshot_date` ASC, `metric_code` ASC, `dim_hash` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_metric_date`(`tenant_id` ASC, `snapshot_date` ASC, `metric_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'BI指标快照(用于数据中台看板，轻量可用)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bi_metric_snapshot
-- ----------------------------

-- ----------------------------
-- Table structure for cs_ticket
-- ----------------------------
DROP TABLE IF EXISTS `cs_ticket`;
CREATE TABLE `cs_ticket`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `ticket_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单号',
  `ticket_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型(RETURN/REFUND/REPRINT/ADDRESS_CHANGE/OTHER)',
  `channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源(PLATFORM/EMAIL/MANUAL)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OPEN/PROCESSING/RESOLVED/CLOSED',
  `priority` int NOT NULL DEFAULT 100 COMMENT '优先级',
  `subject` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联业务类型',
  `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联业务单号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ticket_no`(`tenant_id` ASC, `ticket_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ticket_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_ticket_biz`(`tenant_id` ASC, `biz_type` ASC, `biz_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '客服/售后工单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cs_ticket
-- ----------------------------

-- ----------------------------
-- Table structure for fin_ap_bill
-- ----------------------------
DROP TABLE IF EXISTS `fin_ap_bill`;
CREATE TABLE `fin_ap_bill`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `ap_bill_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '应付单号',
  `payee_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收款方类型(SUPPLIER/CARRIER/OTHER)',
  `payee_id` bigint NULL DEFAULT NULL COMMENT '收款方ID(供应商/承运商等)',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源业务类型(PURCHASE/SHIPMENT/OTHER)',
  `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源业务单号',
  `amount` decimal(18, 2) NOT NULL COMMENT '应付金额',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/CONFIRMED/PAID/CANCELLED',
  `due_at` datetime(3) NULL DEFAULT NULL COMMENT '到期时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `meta_json` json NULL COMMENT '扩展',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ap_bill_no`(`tenant_id` ASC, `ap_bill_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ap_payee`(`tenant_id` ASC, `payee_type` ASC, `payee_id` ASC) USING BTREE,
  INDEX `idx_ap_biz`(`tenant_id` ASC, `biz_type` ASC, `biz_no` ASC) USING BTREE,
  INDEX `idx_ap_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应付单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_ap_bill
-- ----------------------------

-- ----------------------------
-- Table structure for fin_ar_invoice
-- ----------------------------
DROP TABLE IF EXISTS `fin_ar_invoice`;
CREATE TABLE `fin_ar_invoice`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `ar_invoice_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '应收单号',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源业务类型(ORDER/SHIPMENT/OTHER)',
  `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源业务单号',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台编码(可选)',
  `shop_id` bigint NULL DEFAULT NULL COMMENT '店铺ID(可选)',
  `amount` decimal(18, 2) NOT NULL COMMENT '应收金额',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `tax_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '税额(可选)',
  `fee_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '平台手续费(可选)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/CONFIRMED/SETTLED/CANCELLED',
  `occurred_at` datetime(3) NULL DEFAULT NULL COMMENT '发生时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `meta_json` json NULL COMMENT '扩展(明细/拆分/来源字段)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ar_invoice_no`(`tenant_id` ASC, `ar_invoice_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ar_biz`(`tenant_id` ASC, `biz_type` ASC, `biz_no` ASC) USING BTREE,
  INDEX `idx_ar_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_ar_shop`(`tenant_id` ASC, `platform_code` ASC, `shop_id` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应收单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_ar_invoice
-- ----------------------------

-- ----------------------------
-- Table structure for fin_ar_settlement
-- ----------------------------
DROP TABLE IF EXISTS `fin_ar_settlement`;
CREATE TABLE `fin_ar_settlement`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `settlement_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '应收核销单号',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台编码',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `total_ar_amount` decimal(18, 2) NOT NULL COMMENT '核销应收合计(我方口径)',
  `total_payout_amount` decimal(18, 2) NOT NULL COMMENT '匹配到账合计(平台口径)',
  `diff_amount` decimal(18, 2) NOT NULL COMMENT '差异(到账-应收)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/MATCHED/PART_MATCHED/CLOSED/CANCELLED',
  `period_start` date NULL DEFAULT NULL COMMENT '核销周期开始(可选)',
  `period_end` date NULL DEFAULT NULL COMMENT '核销周期结束(可选)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `meta_json` json NULL COMMENT '扩展(匹配规则/证据)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ar_settlement_no`(`tenant_id` ASC, `settlement_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ar_settlement_shop`(`tenant_id` ASC, `platform_code` ASC, `shop_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_ar_settlement_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应收核销(平台结算对账闭环)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_ar_settlement
-- ----------------------------

-- ----------------------------
-- Table structure for fin_ar_settlement_line
-- ----------------------------
DROP TABLE IF EXISTS `fin_ar_settlement_line`;
CREATE TABLE `fin_ar_settlement_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `settlement_id` bigint NOT NULL COMMENT '核销单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `ar_invoice_id` bigint NOT NULL COMMENT '应收单ID(fin_ar_invoice.id)',
  `ar_amount` decimal(18, 2) NOT NULL COMMENT '应收金额',
  `matched_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '已匹配到账金额',
  `match_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'UNMATCHED/PART_MATCHED/MATCHED',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ar_settlement_line`(`tenant_id` ASC, `settlement_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ar_settlement_line_settlement`(`tenant_id` ASC, `settlement_id` ASC) USING BTREE,
  INDEX `idx_ar_settlement_line_ar`(`tenant_id` ASC, `ar_invoice_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应收核销明细(按应收单核销)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_ar_settlement_line
-- ----------------------------

-- ----------------------------
-- Table structure for fin_cost_item
-- ----------------------------
DROP TABLE IF EXISTS `fin_cost_item`;
CREATE TABLE `fin_cost_item`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `cost_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '成本项编码(MATERIAL/LABOR/SHIP/OVERHEAD)',
  `cost_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '成本项名称',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_cost_code`(`tenant_id` ASC, `cost_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成本项' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_cost_item
-- ----------------------------

-- ----------------------------
-- Table structure for fin_cost_ledger
-- ----------------------------
DROP TABLE IF EXISTS `fin_cost_ledger`;
CREATE TABLE `fin_cost_ledger`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `cost_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '成本流水号',
  `cost_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '成本项编码',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型(ORDER/FULFILLMENT/SHIPMENT/WORK_ORDER)',
  `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务单号',
  `amount` decimal(18, 2) NOT NULL COMMENT '金额',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '币种',
  `occurred_at` datetime(3) NULL DEFAULT NULL COMMENT '发生时间',
  `meta_json` json NULL COMMENT '扩展(分摊规则/来源明细)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_cost_no`(`tenant_id` ASC, `cost_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_cost_biz`(`tenant_id` ASC, `biz_type` ASC, `biz_no` ASC) USING BTREE,
  INDEX `idx_cost_code_time`(`tenant_id` ASC, `cost_code` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成本台账(轻量版，可商用对账)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_cost_ledger
-- ----------------------------

-- ----------------------------
-- Table structure for fin_currency
-- ----------------------------
DROP TABLE IF EXISTS `fin_currency`;
CREATE TABLE `fin_currency`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种代码(ISO 4217)',
  `currency_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种名称',
  `symbol` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '符号',
  `precision_scale` int NOT NULL DEFAULT 2 COMMENT '小数位数',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_currency`(`tenant_id` ASC, `currency` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '币种' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_currency
-- ----------------------------

-- ----------------------------
-- Table structure for fin_fee_code
-- ----------------------------
DROP TABLE IF EXISTS `fin_fee_code`;
CREATE TABLE `fin_fee_code`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `fee_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '费用项编码(如 AMAZON_COMMISSION, CARRIER_FUEL)',
  `fee_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '费用项名称',
  `fee_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '费用类型(PLATFORM_FEE/CARRIER_FEE/TAX/DUTY/OTHER)',
  `direction` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '方向(AR+/AP-): INCOME/EXPENSE',
  `default_currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认币种(可选)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_fee_code`(`tenant_id` ASC, `fee_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_fee_type`(`tenant_id` ASC, `fee_type` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '费用项字典(平台/物流/税费统一口径)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_fee_code
-- ----------------------------

-- ----------------------------
-- Table structure for fin_fee_map
-- ----------------------------
DROP TABLE IF EXISTS `fin_fee_map`;
CREATE TABLE `fin_fee_map`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `map_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '映射类型(PLATFORM/CARRIER)',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台编码(当map_type=PLATFORM)',
  `carrier_id` bigint NULL DEFAULT NULL COMMENT '承运商ID(当map_type=CARRIER)',
  `external_fee_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '外部费用项编码/名称',
  `fee_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内部费用项编码(fin_fee_code.fee_code)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_fee_map`(`tenant_id` ASC, `map_type` ASC, `platform_code` ASC, `carrier_id` ASC, `external_fee_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_fee_map_internal`(`tenant_id` ASC, `fee_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '外部费用项→内部费用项 映射' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_fee_map
-- ----------------------------

-- ----------------------------
-- Table structure for fin_fix_action
-- ----------------------------
DROP TABLE IF EXISTS `fin_fix_action`;
CREATE TABLE `fin_fix_action`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `queue_id` bigint NOT NULL COMMENT '修复队列ID',
  `action_no` int NOT NULL COMMENT '动作序号',
  `action_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作类型(SET_REF/SET_FEE_CODE/SET_AMOUNT/IGNORE/REPARSE/REPOST)',
  `before_json` json NULL COMMENT '修改前快照',
  `after_json` json NULL COMMENT '修改后快照',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人',
  `note` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_fix_action`(`tenant_id` ASC, `queue_id` ASC, `action_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_fix_action_queue`(`tenant_id` ASC, `queue_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '人工修复动作(审计留痕，可回放)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_fix_action
-- ----------------------------

-- ----------------------------
-- Table structure for fin_fix_queue
-- ----------------------------
DROP TABLE IF EXISTS `fin_fix_queue`;
CREATE TABLE `fin_fix_queue`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `queue_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '修复单号',
  `batch_id` bigint NOT NULL COMMENT '导入批次ID',
  `issue_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题类型(UNMATCHED_REF/UNKNOWN_FEE_CODE/INVALID_AMOUNT/PARSE_ERROR/OTHER)',
  `severity` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '严重级别(WARN/ERROR)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OPEN/PROCESSING/RESOLVED/CLOSED',
  `ref_line_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联行类型',
  `ref_line_id` bigint NOT NULL COMMENT '关联行ID',
  `summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摘要',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详情(可包含原始行)',
  `assigned_to` bigint NULL DEFAULT NULL COMMENT '指派人',
  `resolved_at` datetime(3) NULL DEFAULT NULL COMMENT '解决时间',
  `resolution` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '解决方案摘要',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_fix_queue_no`(`tenant_id` ASC, `queue_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_fix_queue_batch_status`(`tenant_id` ASC, `batch_id` ASC, `status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_fix_queue_assignee`(`tenant_id` ASC, `assigned_to` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '人工修复队列(账单异常处理入口)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_fix_queue
-- ----------------------------

-- ----------------------------
-- Table structure for fin_fx_rate
-- ----------------------------
DROP TABLE IF EXISTS `fin_fx_rate`;
CREATE TABLE `fin_fx_rate`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `base_currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '基准币种(如 USD)',
  `quote_currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计价币种(如 CNY)',
  `rate_date` date NOT NULL COMMENT '汇率日期',
  `rate` decimal(18, 8) NOT NULL COMMENT '汇率(1 base = rate quote)',
  `source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源(ECB/OPENEX/Manual)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_fx`(`tenant_id` ASC, `base_currency` ASC, `quote_currency` ASC, `rate_date` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_fx_date`(`tenant_id` ASC, `rate_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '汇率' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_fx_rate
-- ----------------------------

-- ----------------------------
-- Table structure for fin_import_batch
-- ----------------------------
DROP TABLE IF EXISTS `fin_import_batch`;
CREATE TABLE `fin_import_batch`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '导入批次号',
  `batch_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型(PLATFORM_STATEMENT/CARRIER_BILL/SUPPLIER_STATEMENT/OTHER)',
  `source_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源名称(平台/承运商/供应商)',
  `source_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源编码(AMAZON/SHEIN/TEMU/UPS/XXX)',
  `shop_id` bigint NULL DEFAULT NULL COMMENT '店铺ID(平台账单可选)',
  `carrier_id` bigint NULL DEFAULT NULL COMMENT '承运商ID(物流账单可选)',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID(供应商对账可选)',
  `period_start` date NULL DEFAULT NULL COMMENT '账期开始',
  `period_end` date NULL DEFAULT NULL COMMENT '账期结束',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '币种(可选)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/UPLOADED/PARSING/PARSED/POSTED/FAILED/CANCELLED',
  `error_count` int NOT NULL DEFAULT 0 COMMENT '错误行数',
  `total_lines` int NOT NULL DEFAULT 0 COMMENT '总行数',
  `success_lines` int NOT NULL DEFAULT 0 COMMENT '成功行数',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `meta_json` json NULL COMMENT '扩展(文件模板/解析规则版本)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_import_batch_no`(`tenant_id` ASC, `batch_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_import_batch_type_status`(`tenant_id` ASC, `batch_type` ASC, `status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_import_batch_source`(`tenant_id` ASC, `source_code` ASC, `shop_id` ASC, `carrier_id` ASC, `supplier_id` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账单导入批次(统一入口)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_import_batch
-- ----------------------------

-- ----------------------------
-- Table structure for fin_import_error
-- ----------------------------
DROP TABLE IF EXISTS `fin_import_error`;
CREATE TABLE `fin_import_error`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `batch_id` bigint NOT NULL COMMENT '导入批次ID',
  `file_id` bigint NULL DEFAULT NULL COMMENT '文件ID(可选)',
  `line_no` int NULL DEFAULT NULL COMMENT '行号(可选)',
  `error_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '错误码',
  `error_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '错误信息',
  `raw_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '原始行文本(可选)',
  `raw_json` json NULL COMMENT '原始行JSON(可选)',
  `severity` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '严重级别(WARN/ERROR)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_import_err_batch`(`tenant_id` ASC, `batch_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_import_err_sev`(`tenant_id` ASC, `severity` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '导入/解析错误明细(可定位/可修复)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_import_error
-- ----------------------------

-- ----------------------------
-- Table structure for fin_import_file
-- ----------------------------
DROP TABLE IF EXISTS `fin_import_file`;
CREATE TABLE `fin_import_file`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `batch_id` bigint NOT NULL COMMENT '导入批次ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名',
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件URL',
  `file_sha256` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件哈希',
  `file_size` bigint NULL DEFAULT NULL COMMENT '大小(bytes)',
  `content_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内容类型(text/csv, application/vnd.ms-excel...)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_import_file_batch`(`tenant_id` ASC, `batch_id` ASC) USING BTREE,
  INDEX `idx_import_file_sha`(`tenant_id` ASC, `file_sha256` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账单导入文件(批次附件)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_import_file
-- ----------------------------

-- ----------------------------
-- Table structure for fin_match_evidence
-- ----------------------------
DROP TABLE IF EXISTS `fin_match_evidence`;
CREATE TABLE `fin_match_evidence`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `batch_id` bigint NOT NULL COMMENT '导入批次ID',
  `line_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '行类型(PLATFORM_LINE/SUPPLIER_LINE/CARRIER_LINE)',
  `line_id` bigint NOT NULL COMMENT '行ID(对应各自line表)',
  `rule_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '使用的规则编码',
  `match_target` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '匹配目标',
  `matched_ref_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '匹配到的ref_type',
  `matched_ref_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '匹配到的ref_no',
  `match_score` decimal(10, 6) NULL DEFAULT NULL COMMENT '匹配得分(可选)',
  `match_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'MATCHED/MISMATCHED/UNMATCHED',
  `note` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `meta_json` json NULL COMMENT '扩展(候选集/证据字段)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_match_evidence_batch`(`tenant_id` ASC, `batch_id` ASC, `match_status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_match_evidence_ref`(`tenant_id` ASC, `matched_ref_type` ASC, `matched_ref_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '匹配证据(可解释、可回溯、便于人工修复)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_match_evidence
-- ----------------------------

-- ----------------------------
-- Table structure for fin_match_rule
-- ----------------------------
DROP TABLE IF EXISTS `fin_match_rule`;
CREATE TABLE `fin_match_rule`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `rule_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '匹配规则编码',
  `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则名称',
  `batch_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '适用批次类型',
  `priority` int NOT NULL DEFAULT 100 COMMENT '优先级(小优先)',
  `match_target` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '匹配目标(ORDER/SHIPMENT/PURCHASE/WORK_ORDER)',
  `match_expr` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '匹配表达式(如 platform_order_id -> oms_unified_order.platform_order_id)',
  `result_ref_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产出ref_type',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `meta_json` json NULL COMMENT '扩展(索引提示/容错)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_match_rule_code`(`tenant_id` ASC, `rule_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_match_rule_scope`(`tenant_id` ASC, `batch_type` ASC, `match_target` ASC, `status` ASC, `priority` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '自动匹配规则(把外部行匹配到内部ref_no)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_match_rule
-- ----------------------------

-- ----------------------------
-- Table structure for fin_parse_run
-- ----------------------------
DROP TABLE IF EXISTS `fin_parse_run`;
CREATE TABLE `fin_parse_run`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `batch_id` bigint NOT NULL COMMENT '导入批次ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '解析任务ID(fin_parse_task.id)',
  `parser_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '解析器编码',
  `template_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板编号',
  `rule_version` int NOT NULL DEFAULT 1 COMMENT '规则版本',
  `input_file_sha256` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '输入文件哈希(用于幂等)',
  `run_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'RUNNING/SUCCESS/FAILED',
  `started_at` datetime(3) NULL DEFAULT NULL COMMENT '开始时间',
  `finished_at` datetime(3) NULL DEFAULT NULL COMMENT '结束时间',
  `error_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误摘要',
  `meta_json` json NULL COMMENT '扩展(统计/环境)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_parse_run_idem`(`tenant_id` ASC, `batch_id` ASC, `parser_code` ASC, `rule_version` ASC, `input_file_sha256` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_parse_run_status`(`tenant_id` ASC, `run_status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '解析运行快照(幂等+版本化)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_parse_run
-- ----------------------------

-- ----------------------------
-- Table structure for fin_parse_task
-- ----------------------------
DROP TABLE IF EXISTS `fin_parse_task`;
CREATE TABLE `fin_parse_task`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `task_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '解析任务号',
  `batch_id` bigint NOT NULL COMMENT '导入批次ID',
  `parser_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '解析器编码(AMAZON_SETTLEMENT_V2/SUPPLIER_XLS_V1...)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/RUNNING/SUCCESS/FAILED/CANCELLED',
  `attempts` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `max_attempts` int NOT NULL DEFAULT 3 COMMENT '最大重试',
  `started_at` datetime(3) NULL DEFAULT NULL COMMENT '开始时间',
  `finished_at` datetime(3) NULL DEFAULT NULL COMMENT '结束时间',
  `error_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误摘要',
  `meta_json` json NULL COMMENT '扩展(配置/参数)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_parse_task_no`(`tenant_id` ASC, `task_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_parse_task_batch`(`tenant_id` ASC, `batch_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_parse_task_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账单解析任务(支持重跑/审计)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_parse_task
-- ----------------------------

-- ----------------------------
-- Table structure for fin_parser_def
-- ----------------------------
DROP TABLE IF EXISTS `fin_parser_def`;
CREATE TABLE `fin_parser_def`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `parser_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '解析器编码(AMAZON_SETTLEMENT_2026/SHEIN_STATEMENT_V1...)',
  `parser_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '解析器名称',
  `batch_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '适用批次类型(PLATFORM_STATEMENT/CARRIER_BILL/SUPPLIER_STATEMENT)',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台编码(可选)',
  `carrier_id` bigint NULL DEFAULT NULL COMMENT '承运商ID(可选)',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID(可选)',
  `input_format` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '输入格式(CSV/XLSX/JSON)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `version_no` int NOT NULL DEFAULT 1 COMMENT '版本号(用于升级兼容)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_parser_code`(`tenant_id` ASC, `parser_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_parser_scope`(`tenant_id` ASC, `batch_type` ASC, `platform_code` ASC, `carrier_id` ASC, `supplier_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '解析器定义(注册表)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_parser_def
-- ----------------------------

-- ----------------------------
-- Table structure for fin_parser_field_def
-- ----------------------------
DROP TABLE IF EXISTS `fin_parser_field_def`;
CREATE TABLE `fin_parser_field_def`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `field_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段编码(内部标准字段)',
  `field_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段名称',
  `field_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型(STRING/DECIMAL/INT/DATE/DATETIME/JSON)',
  `required` tinyint NOT NULL DEFAULT 0 COMMENT '是否必填',
  `biz_domain` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '归属域(PLATFORM/CARRIER/SUPPLIER/COMMON)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_field_code`(`tenant_id` ASC, `field_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_field_domain`(`tenant_id` ASC, `biz_domain` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '解析字段字典(标准字段集)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_parser_field_def
-- ----------------------------

-- ----------------------------
-- Table structure for fin_parser_field_map
-- ----------------------------
DROP TABLE IF EXISTS `fin_parser_field_map`;
CREATE TABLE `fin_parser_field_map`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `template_id` bigint NOT NULL COMMENT '模板ID(fin_parser_template.id)',
  `line_no` int NOT NULL COMMENT '行号',
  `source_field` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源字段名(表头/JSONPath)',
  `field_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标准字段编码(fin_parser_field_def.field_code)',
  `transform_expr` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '转换表达式(轻量DSL/函数名)',
  `default_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认值',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_field_map_line`(`tenant_id` ASC, `template_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_field_map_template`(`tenant_id` ASC, `template_id` ASC) USING BTREE,
  INDEX `idx_field_map_field`(`tenant_id` ASC, `field_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字段映射(来源字段→标准字段, 支持转换)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_parser_field_map
-- ----------------------------

-- ----------------------------
-- Table structure for fin_parser_rule
-- ----------------------------
DROP TABLE IF EXISTS `fin_parser_rule`;
CREATE TABLE `fin_parser_rule`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `rule_no` int NOT NULL COMMENT '规则序号',
  `rule_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则类型(FILTER/DERIVE/VALIDATE/ROUTE)',
  `rule_expr` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则表达式(轻量DSL)',
  `priority` int NOT NULL DEFAULT 100 COMMENT '优先级(小优先)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_parser_rule`(`tenant_id` ASC, `template_id` ASC, `rule_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_parser_rule_priority`(`tenant_id` ASC, `template_id` ASC, `status` ASC, `priority` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '解析规则(过滤/派生/校验/路由, 可配置化)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_parser_rule
-- ----------------------------

-- ----------------------------
-- Table structure for fin_parser_template
-- ----------------------------
DROP TABLE IF EXISTS `fin_parser_template`;
CREATE TABLE `fin_parser_template`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `parser_id` bigint NOT NULL COMMENT '解析器ID(fin_parser_def.id)',
  `template_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板编号',
  `template_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `file_encoding` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件编码(UTF-8/GBK等)',
  `delimiter_char` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分隔符(CSV)',
  `sheet_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工作表名(XLSX)',
  `header_row_no` int NULL DEFAULT NULL COMMENT '表头行号(从1开始)',
  `data_start_row_no` int NULL DEFAULT NULL COMMENT '数据开始行号(从1开始)',
  `date_format` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日期格式(如 yyyy-MM-dd)',
  `timezone` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '时区(如 UTC/Asia/Shanghai)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `rule_version` int NOT NULL DEFAULT 1 COMMENT '规则版本(字段映射版本)',
  `meta_json` json NULL COMMENT '扩展(预处理/过滤规则)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_parser_template`(`tenant_id` ASC, `parser_id` ASC, `template_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_template_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '解析模板(文件结构配置)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_parser_template
-- ----------------------------

-- ----------------------------
-- Table structure for fin_payment
-- ----------------------------
DROP TABLE IF EXISTS `fin_payment`;
CREATE TABLE `fin_payment`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `payment_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '付款单号',
  `ap_bill_id` bigint NOT NULL COMMENT '应付单ID',
  `pay_method` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '付款方式(BANK/ALIPAY/PAYPAL/OTHER)',
  `paid_amount` decimal(18, 2) NOT NULL COMMENT '付款金额',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `paid_at` datetime(3) NULL DEFAULT NULL COMMENT '付款时间',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/PROCESSING/SUCCESS/FAILED/CANCELLED',
  `bank_ref_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '银行流水号/第三方单号',
  `response_json` json NULL COMMENT '回执',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_payment_no`(`tenant_id` ASC, `payment_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_payment_ap`(`tenant_id` ASC, `ap_bill_id` ASC) USING BTREE,
  INDEX `idx_payment_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '付款单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_payment
-- ----------------------------

-- ----------------------------
-- Table structure for fin_payment_allocation
-- ----------------------------
DROP TABLE IF EXISTS `fin_payment_allocation`;
CREATE TABLE `fin_payment_allocation`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `payment_id` bigint NOT NULL COMMENT '付款单ID(fin_payment.id)',
  `ap_bill_id` bigint NOT NULL COMMENT '应付单ID(fin_ap_bill.id)',
  `allocated_amount` decimal(18, 2) NOT NULL COMMENT '分摊/核销金额',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_payment_alloc`(`tenant_id` ASC, `payment_id` ASC, `ap_bill_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_payment_alloc_ap`(`tenant_id` ASC, `ap_bill_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '付款分摊(付款单↔应付单核销)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_payment_allocation
-- ----------------------------

-- ----------------------------
-- Table structure for fin_payment_apply
-- ----------------------------
DROP TABLE IF EXISTS `fin_payment_apply`;
CREATE TABLE `fin_payment_apply`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `apply_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '付款申请单号',
  `payee_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收款方类型(SUPPLIER/CARRIER/OTHER)',
  `payee_id` bigint NULL DEFAULT NULL COMMENT '收款方ID',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `apply_amount` decimal(18, 2) NOT NULL COMMENT '申请金额',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/APPROVED/REJECTED/PAID/CANCELLED',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `meta_json` json NULL COMMENT '扩展(审批流/附件等)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_apply_no`(`tenant_id` ASC, `apply_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_apply_payee`(`tenant_id` ASC, `payee_type` ASC, `payee_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_apply_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '付款申请(可用于审批/出纳)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_payment_apply
-- ----------------------------

-- ----------------------------
-- Table structure for fin_payment_apply_line
-- ----------------------------
DROP TABLE IF EXISTS `fin_payment_apply_line`;
CREATE TABLE `fin_payment_apply_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `apply_id` bigint NOT NULL COMMENT '付款申请ID',
  `line_no` int NOT NULL COMMENT '行号',
  `ap_bill_id` bigint NOT NULL COMMENT '应付单ID(fin_ap_bill.id)',
  `ap_amount` decimal(18, 2) NOT NULL COMMENT '应付金额',
  `proposed_pay_amount` decimal(18, 2) NOT NULL COMMENT '拟支付金额',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_apply_line`(`tenant_id` ASC, `apply_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_apply_line_apply`(`tenant_id` ASC, `apply_id` ASC) USING BTREE,
  INDEX `idx_apply_line_ap`(`tenant_id` ASC, `ap_bill_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '付款申请明细(关联应付单)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_payment_apply_line
-- ----------------------------

-- ----------------------------
-- Table structure for fin_payout
-- ----------------------------
DROP TABLE IF EXISTS `fin_payout`;
CREATE TABLE `fin_payout`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `payout_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收款/打款单号(平台结算打款)',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台编码',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `settlement_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台结算ID(可选)',
  `amount` decimal(18, 2) NOT NULL COMMENT '到账金额',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `paid_at` datetime(3) NULL DEFAULT NULL COMMENT '到账时间',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/MATCHED/UNMATCHED/CLOSED',
  `bank_ref_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '银行流水号',
  `meta_json` json NULL COMMENT '扩展',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_payout_no`(`tenant_id` ASC, `payout_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_payout_shop`(`tenant_id` ASC, `platform_code` ASC, `shop_id` ASC, `paid_at` ASC) USING BTREE,
  INDEX `idx_payout_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台打款/到账记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_payout
-- ----------------------------

-- ----------------------------
-- Table structure for fin_payout_match
-- ----------------------------
DROP TABLE IF EXISTS `fin_payout_match`;
CREATE TABLE `fin_payout_match`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `settlement_id` bigint NOT NULL COMMENT '核销单ID',
  `payout_id` bigint NOT NULL COMMENT '到账记录ID(fin_payout.id)',
  `matched_amount` decimal(18, 2) NOT NULL COMMENT '匹配金额',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `match_note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '匹配说明',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_payout_match`(`tenant_id` ASC, `settlement_id` ASC, `payout_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_payout_match_payout`(`tenant_id` ASC, `payout_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '到账匹配(核销单↔到账记录)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_payout_match
-- ----------------------------

-- ----------------------------
-- Table structure for fin_platform_payout_record
-- ----------------------------
DROP TABLE IF EXISTS `fin_platform_payout_record`;
CREATE TABLE `fin_platform_payout_record`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `platform_statement_id` bigint NOT NULL COMMENT '平台账单ID',
  `payout_id` bigint NULL DEFAULT NULL COMMENT '到账记录ID(fin_payout.id)',
  `platform_payout_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台打款ID(原始)',
  `amount` decimal(18, 2) NOT NULL COMMENT '打款金额',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `paid_at` datetime(3) NULL DEFAULT NULL COMMENT '到账时间(平台)',
  `bank_ref_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '银行流水',
  `match_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'UNMATCHED/MATCHED/PART_MATCHED',
  `raw_json` json NULL COMMENT '原始',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_platform_payout_stmt`(`tenant_id` ASC, `platform_statement_id` ASC) USING BTREE,
  INDEX `idx_platform_payout_match`(`tenant_id` ASC, `match_status` ASC, `paid_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台打款记录(来自账单，用于匹配 fin_payout)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_platform_payout_record
-- ----------------------------

-- ----------------------------
-- Table structure for fin_platform_statement
-- ----------------------------
DROP TABLE IF EXISTS `fin_platform_statement`;
CREATE TABLE `fin_platform_statement`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `statement_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台账单号(内部)',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台编码(AMAZON/SHEIN/TEMU)',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `platform_statement_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台账单ID(原始)',
  `period_start` date NOT NULL COMMENT '账期开始',
  `period_end` date NOT NULL COMMENT '账期结束',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `total_amount` decimal(18, 2) NOT NULL COMMENT '平台口径总额',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/IMPORTED/PARSING/PARSED/RECONCILING/FINISHED/CLOSED',
  `import_batch_id` bigint NULL DEFAULT NULL COMMENT '导入批次ID(fin_import_batch.id)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `meta_json` json NULL COMMENT '扩展(报告类型/版本)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_platform_statement_no`(`tenant_id` ASC, `statement_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_platform_stmt_shop_period`(`tenant_id` ASC, `platform_code` ASC, `shop_id` ASC, `period_start` ASC, `period_end` ASC) USING BTREE,
  INDEX `idx_platform_stmt_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台账单(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_platform_statement
-- ----------------------------

-- ----------------------------
-- Table structure for fin_platform_statement_line
-- ----------------------------
DROP TABLE IF EXISTS `fin_platform_statement_line`;
CREATE TABLE `fin_platform_statement_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `platform_statement_id` bigint NOT NULL COMMENT '平台账单ID',
  `batch_id` bigint NULL DEFAULT NULL COMMENT '导入批次ID(fin_import_batch.id)',
  `line_no` int NOT NULL COMMENT '行号',
  `event_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件类型(ORDER/REFUND/ADJUSTMENT/FEE/PAYOUT...)',
  `platform_order_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台订单ID',
  `platform_ref_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台引用ID(退款/调整等)',
  `ref_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '引用类型(ORDER/SHIPMENT/FEE/OTHER)',
  `ref_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '引用单号(内部)',
  `fee_external_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外部费用项(原始)',
  `fee_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内部费用项(fin_fee_code.fee_code)',
  `amount` decimal(18, 2) NOT NULL COMMENT '金额(平台口径)',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `quantity` int NULL DEFAULT NULL COMMENT '数量(可选)',
  `occurred_at` datetime(3) NULL DEFAULT NULL COMMENT '发生时间',
  `raw_json` json NULL COMMENT '原始行数据',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_platform_stmt_line`(`tenant_id` ASC, `platform_statement_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_platform_stmt_line_order`(`tenant_id` ASC, `platform_order_id` ASC) USING BTREE,
  INDEX `idx_platform_stmt_line_ref`(`tenant_id` ASC, `ref_type` ASC, `ref_no` ASC) USING BTREE,
  INDEX `idx_platform_stmt_line_fee`(`tenant_id` ASC, `fee_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台账单行(明细导入/解析)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_platform_statement_line
-- ----------------------------

-- ----------------------------
-- Table structure for fin_post_rule
-- ----------------------------
DROP TABLE IF EXISTS `fin_post_rule`;
CREATE TABLE `fin_post_rule`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `rule_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '过账规则编码',
  `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '过账规则名称',
  `batch_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '适用批次类型',
  `posting_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '过账类型(TO_STATEMENT/TO_AR/TO_AP/TO_PAYOUT)',
  `priority` int NOT NULL DEFAULT 100 COMMENT '优先级',
  `condition_expr` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '条件表达式(可选)',
  `action_expr` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作表达式(生成单据/字段映射)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_rule_code`(`tenant_id` ASC, `rule_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_post_rule_scope`(`tenant_id` ASC, `batch_type` ASC, `posting_type` ASC, `status` ASC, `priority` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '过账规则(导入行→对账/AR/AP/到账, 配置化)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_post_rule
-- ----------------------------

-- ----------------------------
-- Table structure for fin_posting_log
-- ----------------------------
DROP TABLE IF EXISTS `fin_posting_log`;
CREATE TABLE `fin_posting_log`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `batch_id` bigint NOT NULL COMMENT '导入批次ID',
  `posting_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '过账类型(TO_STATEMENT/TO_AR/TO_AP/TO_PAYOUT)',
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标类型(FIN_STATEMENT/AR_INVOICE/AP_BILL/PAYOUT)',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID(可选)',
  `target_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标单号(可选)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/RUNNING/SUCCESS/FAILED',
  `error_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误摘要',
  `meta_json` json NULL COMMENT '扩展(生成规则/映射结果)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_posting_batch`(`tenant_id` ASC, `batch_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_posting_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_posting_target`(`tenant_id` ASC, `target_type` ASC, `target_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '过账日志(批次→对账/AR/AP/Payout) 用于幂等与追溯' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_posting_log
-- ----------------------------

-- ----------------------------
-- Table structure for fin_recon_result
-- ----------------------------
DROP TABLE IF EXISTS `fin_recon_result`;
CREATE TABLE `fin_recon_result`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `statement_id` bigint NOT NULL COMMENT '对账单ID',
  `ref_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '引用类型',
  `ref_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '引用单号',
  `expected_amount` decimal(18, 2) NOT NULL COMMENT '系统金额(我方口径)',
  `statement_amount` decimal(18, 2) NOT NULL COMMENT '对账方金额',
  `diff_amount` decimal(18, 2) NOT NULL COMMENT '差异金额(statement-expected)',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `match_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'MATCHED/MISMATCHED/MISSING_IN_SYS/MISSING_IN_STMT',
  `resolve_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OPEN/RESOLVED/CLOSED',
  `resolve_note` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理说明',
  `meta_json` json NULL COMMENT '扩展(匹配规则/证据)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_recon_ref`(`tenant_id` ASC, `statement_id` ASC, `ref_type` ASC, `ref_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_recon_match`(`tenant_id` ASC, `match_status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_recon_resolve`(`tenant_id` ASC, `resolve_status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '对账结果(差异/缺失)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_recon_result
-- ----------------------------

-- ----------------------------
-- Table structure for fin_ref_mapping
-- ----------------------------
DROP TABLE IF EXISTS `fin_ref_mapping`;
CREATE TABLE `fin_ref_mapping`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `mapping_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '映射类型(PLATFORM_ORDER/PLATFORM_REF/TRACKING_NO/SUPPLIER_REF)',
  `external_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '外部键(如 platform_order_id, tracking_no)',
  `ref_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内部引用类型(ORDER/SHIPMENT/PURCHASE/WORK_ORDER)',
  `ref_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内部引用号',
  `confidence` decimal(10, 6) NOT NULL DEFAULT 1.000000 COMMENT '置信度(人工修复可设)',
  `source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源(AUTO/MANUAL/IMPORT)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ref_mapping`(`tenant_id` ASC, `mapping_type` ASC, `external_key` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ref_mapping_ref`(`tenant_id` ASC, `ref_type` ASC, `ref_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '外部键→内部业务号 映射辅助(加速匹配/人工修复沉淀)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_ref_mapping
-- ----------------------------

-- ----------------------------
-- Table structure for fin_statement
-- ----------------------------
DROP TABLE IF EXISTS `fin_statement`;
CREATE TABLE `fin_statement`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `statement_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对账单号',
  `statement_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型(PLATFORM/CARRIER/SUPPLIER)',
  `counterparty_id` bigint NULL DEFAULT NULL COMMENT '对账方ID(店铺/承运商/供应商等，按type解释)',
  `period_start` date NOT NULL COMMENT '开始日期',
  `period_end` date NOT NULL COMMENT '结束日期',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `total_amount` decimal(18, 2) NOT NULL COMMENT '对账总额(对账方口径)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/IMPORTING/RECONCILING/FINISHED/CLOSED',
  `import_batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '导入批次(附件/账单)',
  `import_batch_id` bigint NULL DEFAULT NULL COMMENT '导入批次ID(fin_import_batch.id)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_statement_no`(`tenant_id` ASC, `statement_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_statement_type_period`(`tenant_id` ASC, `statement_type` ASC, `period_start` ASC, `period_end` ASC) USING BTREE,
  INDEX `idx_statement_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '对账单(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_statement
-- ----------------------------

-- ----------------------------
-- Table structure for fin_statement_file
-- ----------------------------
DROP TABLE IF EXISTS `fin_statement_file`;
CREATE TABLE `fin_statement_file`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `statement_id` bigint NOT NULL COMMENT '对账单ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名',
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件URL',
  `file_sha256` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件哈希',
  `file_size` bigint NULL DEFAULT NULL COMMENT '大小(bytes)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_stmt_file_stmt`(`tenant_id` ASC, `statement_id` ASC) USING BTREE,
  INDEX `idx_stmt_file_sha`(`tenant_id` ASC, `file_sha256` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '对账附件(平台/物流账单)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_statement_file
-- ----------------------------

-- ----------------------------
-- Table structure for fin_statement_line
-- ----------------------------
DROP TABLE IF EXISTS `fin_statement_line`;
CREATE TABLE `fin_statement_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `statement_id` bigint NOT NULL COMMENT '对账单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `ref_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '引用类型(ORDER/SHIPMENT/FEE/OTHER)',
  `ref_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '引用单号',
  `amount` decimal(18, 2) NOT NULL COMMENT '金额(对账方口径)',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `fee_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '费用项编码(可选)',
  `occurred_at` datetime(3) NULL DEFAULT NULL COMMENT '发生时间',
  `raw_json` json NULL COMMENT '原始明细(导入行/平台字段)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_statement_line`(`tenant_id` ASC, `statement_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_statement_ref`(`tenant_id` ASC, `ref_type` ASC, `ref_no` ASC) USING BTREE,
  INDEX `idx_statement_line_stmt`(`tenant_id` ASC, `statement_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '对账单(行)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_statement_line
-- ----------------------------

-- ----------------------------
-- Table structure for fin_supplier_statement
-- ----------------------------
DROP TABLE IF EXISTS `fin_supplier_statement`;
CREATE TABLE `fin_supplier_statement`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `statement_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商对账单号(内部)',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `period_start` date NOT NULL COMMENT '账期开始',
  `period_end` date NOT NULL COMMENT '账期结束',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `total_amount` decimal(18, 2) NOT NULL COMMENT '对账总额(供应商口径)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/IMPORTED/PARSING/PARSED/RECONCILING/FINISHED/CLOSED',
  `import_batch_id` bigint NULL DEFAULT NULL COMMENT '导入批次ID',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `meta_json` json NULL COMMENT '扩展(计价规则/合同版本)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_supplier_stmt_no`(`tenant_id` ASC, `statement_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_supplier_stmt_supplier_period`(`tenant_id` ASC, `supplier_id` ASC, `period_start` ASC, `period_end` ASC) USING BTREE,
  INDEX `idx_supplier_stmt_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '供应商对账单(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_supplier_statement
-- ----------------------------

-- ----------------------------
-- Table structure for fin_supplier_statement_line
-- ----------------------------
DROP TABLE IF EXISTS `fin_supplier_statement_line`;
CREATE TABLE `fin_supplier_statement_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `supplier_statement_id` bigint NOT NULL COMMENT '供应商对账单ID',
  `batch_id` bigint NULL DEFAULT NULL COMMENT '导入批次ID(fin_import_batch.id)',
  `line_no` int NOT NULL COMMENT '行号',
  `ref_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '引用类型(PURCHASE_ORDER/RECEIPT/WORK_ORDER/SHIPMENT/OTHER)',
  `ref_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '引用单号(内部)',
  `external_ref_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外部单号(供应商)',
  `fee_external_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外部费用项/计价项(原始)',
  `fee_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内部费用项(fin_fee_code)',
  `amount` decimal(18, 2) NOT NULL COMMENT '金额(供应商口径)',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `quantity` int NULL DEFAULT NULL COMMENT '数量(可选)',
  `occurred_at` datetime(3) NULL DEFAULT NULL COMMENT '发生时间',
  `raw_json` json NULL COMMENT '原始行数据',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_supplier_stmt_line`(`tenant_id` ASC, `supplier_statement_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_supplier_stmt_line_ref`(`tenant_id` ASC, `ref_type` ASC, `ref_no` ASC) USING BTREE,
  INDEX `idx_supplier_stmt_line_fee`(`tenant_id` ASC, `fee_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '供应商对账单行' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_supplier_statement_line
-- ----------------------------

-- ----------------------------
-- Table structure for fin_writeoff_log
-- ----------------------------
DROP TABLE IF EXISTS `fin_writeoff_log`;
CREATE TABLE `fin_writeoff_log`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `writeoff_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '核销类型(AR/AP)',
  `doc_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单据类型(AR_INVOICE/AP_BILL/PAYMENT/PAYOUT)',
  `doc_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单据号',
  `action` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作(MATCH/CLOSE/CANCEL/ADJUST)',
  `amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '金额(可选)',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '币种(可选)',
  `operator_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SYSTEM/USER',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人',
  `note` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `meta_json` json NULL COMMENT '扩展',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_writeoff_doc`(`tenant_id` ASC, `writeoff_type` ASC, `doc_type` ASC, `doc_no` ASC) USING BTREE,
  INDEX `idx_writeoff_time`(`tenant_id` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '核销审计日志(财务可追溯)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_writeoff_log
-- ----------------------------

-- ----------------------------
-- Table structure for iam_data_scope
-- ----------------------------
DROP TABLE IF EXISTS `iam_data_scope`;
CREATE TABLE `iam_data_scope`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `subject_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主体类型(USER/ROLE)',
  `subject_id` bigint NOT NULL COMMENT '主体ID(user_id/role_id)',
  `scope_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '范围类型(FACTORY/WAREHOUSE/SUPPLIER/SHOP)',
  `scope_id` bigint NOT NULL COMMENT '范围对象ID',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态(ENABLED/DISABLED)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_scope_subject`(`tenant_id` ASC, `subject_type` ASC, `subject_id` ASC) USING BTREE,
  INDEX `idx_scope_type`(`tenant_id` ASC, `scope_type` ASC, `scope_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据权限范围' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of iam_data_scope
-- ----------------------------
INSERT INTO `iam_data_scope` VALUES (1, 1, 0, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'ROLE', 1, 'FACTORY', 1, 'ENABLED');
INSERT INTO `iam_data_scope` VALUES (2, 1, 0, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'ROLE', 1, 'FACTORY', 2, 'ENABLED');
INSERT INTO `iam_data_scope` VALUES (3, 1, 0, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'ROLE', 2, 'FACTORY', 1, 'ENABLED');

-- ----------------------------
-- Table structure for iam_factory
-- ----------------------------
DROP TABLE IF EXISTS `iam_factory`;
CREATE TABLE `iam_factory`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `factory_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工厂编码(租户内唯一)',
  `factory_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工厂名称',
  `country_code` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国家代码',
  `province` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省/州',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '城市',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态(ACTIVE/INACTIVE)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_factory_code`(`tenant_id` ASC, `factory_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_factory_status`(`tenant_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工厂/履约主体' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of iam_factory
-- ----------------------------
INSERT INTO `iam_factory` VALUES (1, 1, 0, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'F1', 'Factory One', NULL, NULL, NULL, NULL, NULL, NULL, 'ENABLED');
INSERT INTO `iam_factory` VALUES (2, 1, 0, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'F2', 'Factory Two', NULL, NULL, NULL, NULL, NULL, NULL, 'ENABLED');

-- ----------------------------
-- Table structure for iam_permission
-- ----------------------------
DROP TABLE IF EXISTS `iam_permission`;
CREATE TABLE `iam_permission`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NULL DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NULL DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `perm_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限码(全局唯一)',
  `perm_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
  `perm_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'MENU/BUTTON/API',
  `menu_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端路由/菜单路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端组件路径（Vben）',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单重定向路径',
  `api_method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP方法(GET/POST/PUT/DELETE)',
  `api_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'API路径',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父权限ID(菜单树)',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态(ENABLED/DISABLED)',
  `hidden` tinyint(1) NULL DEFAULT 0 COMMENT '是否隐藏 (0:显示, 1:隐藏)',
  `keep_alive` tinyint(1) NULL DEFAULT 1 COMMENT '是否缓存 (0:否, 1:是)',
  `always_show` tinyint(1) NULL DEFAULT 0 COMMENT '是否总是显示根菜单',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_perm_code`(`perm_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_perm_parent`(`tenant_id` ASC, `parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限点(菜单/按钮/API)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of iam_permission
-- ----------------------------
INSERT INTO `iam_permission` VALUES (900, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'sys:manage', '系统管理', 'MENU', '/system', 'LAYOUT', NULL, NULL, NULL, NULL, 0, 90, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (901, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'sys:user:list', '用户管理', 'MENU', '/system/user', '/system/user/index', NULL, NULL, NULL, NULL, 900, 1, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (902, 1, 1, '2026-02-14 13:50:14.486', '2026-02-14 13:50:14.486', 0, 1, NULL, NULL, NULL, 'sys:role:list', '角色管理', 'MENU', '/system/role', '/system/role/index', NULL, NULL, NULL, NULL, 900, 2, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (903, 1, 1, '2026-02-14 13:50:14.488', '2026-02-14 13:50:14.488', 0, 1, NULL, NULL, NULL, 'sys:perm:list', '权限管理', 'MENU', '/system/permission', '/system/permission/index', NULL, NULL, NULL, NULL, 900, 4, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90101, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'iam:user:list', '用户列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 901, 1, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90102, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'iam:user:create', '新增用户', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 901, 2, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90103, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'iam:user:update', '编辑用户', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 901, 3, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90104, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'iam:user:delete', '删除用户', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 901, 4, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90105, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'iam:user:query', '查询用户', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 901, 5, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90106, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'iam:user:reset_pwd', '重置密码', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 901, 6, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90201, 1, 1, '2026-02-14 13:50:14.486', '2026-02-14 13:50:14.486', 0, 1, NULL, NULL, NULL, 'iam:role:page', '角色列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 902, 1, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90202, 1, 1, '2026-02-14 13:50:14.486', '2026-02-14 13:50:14.486', 0, 1, NULL, NULL, NULL, 'iam:role:create', '新增角色', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 902, 2, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90203, 1, 1, '2026-02-14 13:50:14.486', '2026-02-14 13:50:14.486', 0, 1, NULL, NULL, NULL, 'iam:role:update', '编辑角色', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 902, 3, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90204, 1, 1, '2026-02-14 13:50:14.486', '2026-02-14 13:50:14.486', 0, 1, NULL, NULL, NULL, 'iam:role:delete', '删除角色', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 902, 4, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90205, 1, 1, '2026-02-14 13:50:14.486', '2026-02-14 13:50:14.486', 0, 1, NULL, NULL, NULL, 'iam:role:grant', '分配权限', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 902, 5, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90301, 1, 1, '2026-02-14 13:50:14.488', '2026-02-14 13:50:14.488', 0, 1, NULL, NULL, NULL, 'iam:perm:page', '权限列表', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 903, 1, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90302, 1, 1, '2026-02-14 13:50:14.488', '2026-02-14 13:50:14.488', 0, 1, NULL, NULL, NULL, 'iam:perm:create', '新增权限', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 903, 2, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90303, 1, 1, '2026-02-14 13:50:14.488', '2026-02-14 13:50:14.488', 0, 1, NULL, NULL, NULL, 'iam:perm:update', '编辑权限', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 903, 3, 'ENABLED', 0, 1, 0);
INSERT INTO `iam_permission` VALUES (90304, 1, 1, '2026-02-14 13:50:14.488', '2026-02-14 13:50:14.488', 0, 1, NULL, NULL, NULL, 'iam:perm:delete', '删除权限', 'BUTTON', NULL, NULL, NULL, NULL, NULL, NULL, 903, 4, 'ENABLED', 0, 1, 0);

-- ----------------------------
-- Table structure for iam_role
-- ----------------------------
DROP TABLE IF EXISTS `iam_role`;
CREATE TABLE `iam_role`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `role_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码(租户内唯一)',
  `role_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SYSTEM/TENANT',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态(ENABLED/DISABLED)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`tenant_id` ASC, `role_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_role_status`(`tenant_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of iam_role
-- ----------------------------
INSERT INTO `iam_role` VALUES (1, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'ADMIN', 'Administrator', 'SYSTEM', 'ENABLED', NULL);
INSERT INTO `iam_role` VALUES (2, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'OPERATOR', 'Operator', 'BUSINESS', 'ENABLED', NULL);

-- ----------------------------
-- Table structure for iam_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `iam_role_permission`;
CREATE TABLE `iam_role_permission`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NULL DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NULL DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `perm_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`tenant_id` ASC, `role_id` ASC, `perm_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_role_perm_role`(`tenant_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_role_perm_perm`(`tenant_id` ASC, `perm_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色-权限关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of iam_role_permission
-- ----------------------------
INSERT INTO `iam_role_permission` VALUES (1, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 1, 900);
INSERT INTO `iam_role_permission` VALUES (2, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 1, 901);
INSERT INTO `iam_role_permission` VALUES (3, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 1, 90101);
INSERT INTO `iam_role_permission` VALUES (4, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 1, 90102);
INSERT INTO `iam_role_permission` VALUES (5, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 1, 90103);
INSERT INTO `iam_role_permission` VALUES (6, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 1, 90104);
INSERT INTO `iam_role_permission` VALUES (7, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 1, 90105);
INSERT INTO `iam_role_permission` VALUES (8, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 1, 90106);
INSERT INTO `iam_role_permission` VALUES (9, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 2, 900);
INSERT INTO `iam_role_permission` VALUES (10, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 2, 901);
INSERT INTO `iam_role_permission` VALUES (11, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 2, 90101);
INSERT INTO `iam_role_permission` VALUES (12, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 2, 90105);
INSERT INTO `iam_role_permission` VALUES (13, 1, 1, '2026-02-14 13:50:14.489', '2026-02-14 13:50:14.489', 0, 1, NULL, NULL, NULL, 1, 902);
INSERT INTO `iam_role_permission` VALUES (14, 1, 1, '2026-02-14 13:50:14.489', '2026-02-14 13:50:14.489', 0, 1, NULL, NULL, NULL, 1, 90201);
INSERT INTO `iam_role_permission` VALUES (15, 1, 1, '2026-02-14 13:50:14.489', '2026-02-14 13:50:14.489', 0, 1, NULL, NULL, NULL, 1, 90202);
INSERT INTO `iam_role_permission` VALUES (16, 1, 1, '2026-02-14 13:50:14.489', '2026-02-14 13:50:14.489', 0, 1, NULL, NULL, NULL, 1, 90203);
INSERT INTO `iam_role_permission` VALUES (17, 1, 1, '2026-02-14 13:50:14.489', '2026-02-14 13:50:14.489', 0, 1, NULL, NULL, NULL, 1, 90204);
INSERT INTO `iam_role_permission` VALUES (18, 1, 1, '2026-02-14 13:50:14.489', '2026-02-14 13:50:14.489', 0, 1, NULL, NULL, NULL, 1, 90205);
INSERT INTO `iam_role_permission` VALUES (19, 1, 1, '2026-02-14 13:50:14.489', '2026-02-14 13:50:14.489', 0, 1, NULL, NULL, NULL, 1, 903);
INSERT INTO `iam_role_permission` VALUES (20, 1, 1, '2026-02-14 13:50:14.489', '2026-02-14 13:50:14.489', 0, 1, NULL, NULL, NULL, 1, 90301);
INSERT INTO `iam_role_permission` VALUES (21, 1, 1, '2026-02-14 13:50:14.489', '2026-02-14 13:50:14.489', 0, 1, NULL, NULL, NULL, 1, 90302);
INSERT INTO `iam_role_permission` VALUES (22, 1, 1, '2026-02-14 13:50:14.489', '2026-02-14 13:50:14.489', 0, 1, NULL, NULL, NULL, 1, 90303);
INSERT INTO `iam_role_permission` VALUES (23, 1, 1, '2026-02-14 13:50:14.489', '2026-02-14 13:50:14.489', 0, 1, NULL, NULL, NULL, 1, 90304);

-- ----------------------------
-- Table structure for iam_tenant
-- ----------------------------
DROP TABLE IF EXISTS `iam_tenant`;
CREATE TABLE `iam_tenant`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `tenant_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '租户编码(业务唯一)',
  `tenant_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '租户名称',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态(ACTIVE/LOCKED)',
  `plan_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '套餐类型(BASIC/PRO/ENTERPRISE)',
  `plan_expire_at` datetime(3) NULL DEFAULT NULL COMMENT '套餐到期时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_code`(`tenant_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '租户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of iam_tenant
-- ----------------------------
INSERT INTO `iam_tenant` VALUES (1, 0, 0, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'default', 'Default Tenant', 'ENABLED', NULL, NULL);

-- ----------------------------
-- Table structure for iam_user
-- ----------------------------
DROP TABLE IF EXISTS `iam_user`;
CREATE TABLE `iam_user`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录名(租户内唯一)',
  `password_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码Hash',
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态(ENABLED/DISABLED)',
  `last_login_at` datetime(3) NULL DEFAULT NULL COMMENT '最近登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_username`(`tenant_id` ASC, `username` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_user_status`(`tenant_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of iam_user
-- ----------------------------
INSERT INTO `iam_user` VALUES (1, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'admin', '$2a$10$GzCERpucEK3zfFAHJKOBFeliWWhbaeXI9n3.HtKTk8XkHJxcGVRSG', 'Administrator', NULL, NULL, 'ENABLED', NULL);
INSERT INTO `iam_user` VALUES (2, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 'operator', '$2a$10$GzCERpucEK3zfFAHJKOBFeliWWhbaeXI9n3.HtKTk8XkHJxcGVRSG', 'Operator User', NULL, NULL, 'ENABLED', NULL);

-- ----------------------------
-- Table structure for iam_user_role
-- ----------------------------
DROP TABLE IF EXISTS `iam_user_role`;
CREATE TABLE `iam_user_role`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`tenant_id` ASC, `user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_user_role_user`(`tenant_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_role_role`(`tenant_id` ASC, `role_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-角色关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of iam_user_role
-- ----------------------------
INSERT INTO `iam_user_role` VALUES (1, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 1, 1);
INSERT INTO `iam_user_role` VALUES (2, 1, 1, '2026-02-14 09:42:01.000', '2026-02-14 09:42:01.000', 0, 1, NULL, NULL, NULL, 2, 2);

-- ----------------------------
-- Table structure for int_raw_order
-- ----------------------------
DROP TABLE IF EXISTS `int_raw_order`;
CREATE TABLE `int_raw_order`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台编码',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `platform_order_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台订单ID',
  `platform_order_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台订单号(可空)',
  `order_created_at` datetime(3) NULL DEFAULT NULL COMMENT '平台下单时间',
  `order_payload_json` json NOT NULL COMMENT '原始订单JSON',
  `hash_sig` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'payload哈希(用于变更检测)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'RAW/INGESTED/IGNORED',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_raw_order`(`tenant_id` ASC, `platform_code` ASC, `shop_id` ASC, `platform_order_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_raw_order_created`(`tenant_id` ASC, `platform_code` ASC, `order_created_at` ASC) USING BTREE,
  INDEX `idx_raw_order_status`(`tenant_id` ASC, `platform_code` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '原始订单(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of int_raw_order
-- ----------------------------

-- ----------------------------
-- Table structure for int_raw_order_item
-- ----------------------------
DROP TABLE IF EXISTS `int_raw_order_item`;
CREATE TABLE `int_raw_order_item`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `raw_order_id` bigint NOT NULL COMMENT '原始订单ID',
  `platform_order_item_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台订单行ID',
  `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台SKU/商家SKU',
  `qty` int NOT NULL COMMENT '数量',
  `item_payload_json` json NULL COMMENT '原始订单行JSON',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_raw_item`(`tenant_id` ASC, `raw_order_id` ASC, `platform_order_item_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_raw_item_order`(`tenant_id` ASC, `raw_order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '原始订单(行)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of int_raw_order_item
-- ----------------------------

-- ----------------------------
-- Table structure for int_sync_job
-- ----------------------------
DROP TABLE IF EXISTS `int_sync_job`;
CREATE TABLE `int_sync_job`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `job_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务编码(如 AMAZON_ORDER_PULL)',
  `job_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台(可空表示通用)',
  `shop_id` bigint NULL DEFAULT NULL COMMENT '店铺(可空表示全部)',
  `xxl_job_id` bigint NULL DEFAULT NULL COMMENT 'XXL-JOB任务ID',
  `cron_expr` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'cron表达式(配置冗余保存)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ENABLED/DISABLED',
  `config_json` json NULL COMMENT '任务配置(窗口/分页/速率等)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sync_job`(`tenant_id` ASC, `job_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_sync_job_status`(`tenant_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '对接同步任务配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of int_sync_job
-- ----------------------------

-- ----------------------------
-- Table structure for int_sync_run
-- ----------------------------
DROP TABLE IF EXISTS `int_sync_run`;
CREATE TABLE `int_sync_run`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `job_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务编码',
  `run_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '运行ID(用于串联日志)',
  `start_at` datetime(3) NOT NULL COMMENT '开始时间',
  `end_at` datetime(3) NULL DEFAULT NULL COMMENT '结束时间',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'RUNNING/SUCCESS/FAILED',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '成功条数',
  `fail_count` int NOT NULL DEFAULT 0 COMMENT '失败条数',
  `error_message` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误摘要',
  `detail_json` json NULL COMMENT '明细(分页/游标等)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sync_run`(`tenant_id` ASC, `job_code` ASC, `run_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_sync_run_time`(`tenant_id` ASC, `job_code` ASC, `start_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '对接同步运行记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of int_sync_run
-- ----------------------------

-- ----------------------------
-- Table structure for inv_balance
-- ----------------------------
DROP TABLE IF EXISTS `inv_balance`;
CREATE TABLE `inv_balance`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `location_id` bigint NOT NULL COMMENT '库位ID(可用0表示虚拟库位)',
  `sku_id` bigint NOT NULL COMMENT 'SKU/物料ID',
  `on_hand_qty` int NOT NULL DEFAULT 0 COMMENT '现存数量',
  `allocated_qty` int NOT NULL DEFAULT 0 COMMENT '预占数量',
  `available_qty` int NOT NULL DEFAULT 0 COMMENT '可用数量(on_hand-allocated)',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次号(可选)',
  `lot_json` json NULL COMMENT '批次/效期等',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_balance`(`tenant_id` ASC, `factory_id` ASC, `warehouse_id` ASC, `location_id` ASC, `sku_id` ASC, `batch_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_balance_sku`(`tenant_id` ASC, `warehouse_id` ASC, `sku_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存余额(读模型)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of inv_balance
-- ----------------------------
INSERT INTO `inv_balance` VALUES (9001, 1, 1, '2026-02-12 08:10:00.000', '2026-02-12 08:10:00.000', 0, 0, 0, 0, 'seed', 8001, 8003, 3101, 100, 1, 99, NULL, NULL);

-- ----------------------------
-- Table structure for inv_doc
-- ----------------------------
DROP TABLE IF EXISTS `inv_doc`;
CREATE TABLE `inv_doc`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `doc_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库存单据号',
  `doc_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'IN/OUT/TRANSFER/ADJUST/COUNT',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源业务类型',
  `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源业务单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/POSTED/CANCELLED',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inv_doc_no`(`tenant_id` ASC, `doc_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_inv_doc_biz`(`tenant_id` ASC, `biz_type` ASC, `biz_no` ASC) USING BTREE,
  INDEX `idx_inv_doc_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存单据(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of inv_doc
-- ----------------------------

-- ----------------------------
-- Table structure for inv_doc_line
-- ----------------------------
DROP TABLE IF EXISTS `inv_doc_line`;
CREATE TABLE `inv_doc_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `doc_id` bigint NOT NULL COMMENT '库存单据ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NOT NULL COMMENT 'SKU/物料ID',
  `from_location_id` bigint NULL DEFAULT NULL COMMENT '来源库位(可空)',
  `to_location_id` bigint NULL DEFAULT NULL COMMENT '目标库位(可空)',
  `qty` int NOT NULL COMMENT '数量',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inv_doc_line`(`tenant_id` ASC, `doc_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_inv_doc_line_doc`(`tenant_id` ASC, `doc_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存单据(行)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of inv_doc_line
-- ----------------------------

-- ----------------------------
-- Table structure for inv_ledger
-- ----------------------------
DROP TABLE IF EXISTS `inv_ledger`;
CREATE TABLE `inv_ledger`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `txn_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库存流水号',
  `txn_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变更类型(RESERVE/RELEASE/IN/OUT/ADJUST...)',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型',
  `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU/物料ID',
  `delta_qty` int NOT NULL COMMENT '变更数量(正入负出)',
  `before_on_hand` int NOT NULL COMMENT '变更前现存',
  `after_on_hand` int NOT NULL COMMENT '变更后现存',
  `before_allocated` int NOT NULL COMMENT '变更前预占',
  `after_allocated` int NOT NULL COMMENT '变更后预占',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `extra_json` json NULL COMMENT '扩展',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ledger_txn_no`(`tenant_id` ASC, `txn_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ledger_biz`(`tenant_id` ASC, `biz_type` ASC, `biz_no` ASC) USING BTREE,
  INDEX `idx_ledger_sku_time`(`tenant_id` ASC, `warehouse_id` ASC, `sku_id` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存台账(可审计真相)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of inv_ledger
-- ----------------------------

-- ----------------------------
-- Table structure for inv_reservation
-- ----------------------------
DROP TABLE IF EXISTS `inv_reservation`;
CREATE TABLE `inv_reservation`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型(FULFILLMENT/OUTBOUND/WORK_ORDER...)',
  `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU/物料ID',
  `qty` int NOT NULL COMMENT '预占数量',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'RESERVED/RELEASED/CONSUMED',
  `expire_at` datetime(3) NULL DEFAULT NULL COMMENT '预占过期时间(可选)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_reserve`(`tenant_id` ASC, `biz_type` ASC, `biz_no` ASC, `warehouse_id` ASC, `sku_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_reserve_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_reserve_biz`(`tenant_id` ASC, `biz_type` ASC, `biz_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存预占' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of inv_reservation
-- ----------------------------

-- ----------------------------
-- Table structure for mes_batch
-- ----------------------------
DROP TABLE IF EXISTS `mes_batch`;
CREATE TABLE `mes_batch`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '生产批次号',
  `batch_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型(POD/REWORK/SAMPLE)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/RELEASED/RUNNING/DONE/CANCELLED',
  `workstation_id` bigint NULL DEFAULT NULL COMMENT '产线(可空)',
  `plan_id` bigint NULL DEFAULT NULL COMMENT '排产计划ID(可空)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_batch_no`(`tenant_id` ASC, `batch_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_batch_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产批次(用于合单生产/集批)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_batch
-- ----------------------------

-- ----------------------------
-- Table structure for mes_batch_item
-- ----------------------------
DROP TABLE IF EXISTS `mes_batch_item`;
CREATE TABLE `mes_batch_item`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `line_no` int NOT NULL COMMENT '行号',
  `work_order_id` bigint NOT NULL COMMENT '工单ID',
  `qty` int NOT NULL COMMENT '数量(工单内汇总)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/RUNNING/DONE/CANCELLED',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_batch_line`(`tenant_id` ASC, `batch_id` ASC, `line_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_batch_wo`(`tenant_id` ASC, `batch_id` ASC, `work_order_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_batch_item_batch`(`tenant_id` ASC, `batch_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产批次明细' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_batch_item
-- ----------------------------

-- ----------------------------
-- Table structure for mes_calendar
-- ----------------------------
DROP TABLE IF EXISTS `mes_calendar`;
CREATE TABLE `mes_calendar`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `workstation_id` bigint NOT NULL COMMENT '工位/产线ID',
  `work_date` date NOT NULL COMMENT '日期',
  `is_workday` tinyint NOT NULL DEFAULT 1 COMMENT '是否工作日',
  `shift_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '班次编码(可空表示默认)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_calendar`(`tenant_id` ASC, `workstation_id` ASC, `work_date` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_calendar_date`(`tenant_id` ASC, `work_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '产线日历(按工位)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_calendar
-- ----------------------------

-- ----------------------------
-- Table structure for mes_capacity
-- ----------------------------
DROP TABLE IF EXISTS `mes_capacity`;
CREATE TABLE `mes_capacity`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `workstation_id` bigint NOT NULL COMMENT '工位/产线ID',
  `capacity_date` date NOT NULL COMMENT '日期',
  `capacity_minutes` int NOT NULL COMMENT '可用产能(分钟)',
  `reserved_minutes` int NOT NULL DEFAULT 0 COMMENT '已占用产能(分钟)',
  `available_minutes` int NOT NULL DEFAULT 0 COMMENT '剩余产能(分钟)',
  `capacity_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模式(STD/MANUAL/OVERTIME)',
  `meta_json` json NULL COMMENT '扩展(人数/设备/效率系数)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_capacity`(`tenant_id` ASC, `workstation_id` ASC, `capacity_date` ASC, `capacity_mode` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_capacity_date`(`tenant_id` ASC, `capacity_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '产能池(按工位/日期)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for mes_equipment
-- ----------------------------
DROP TABLE IF EXISTS `mes_equipment`;
CREATE TABLE `mes_equipment`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `equipment_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备编码',
  `equipment_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备名称',
  `equipment_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备类型(如 EPSON_F2100)',
  `workstation_id` bigint NULL DEFAULT NULL COMMENT '归属工位ID',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE/MAINTENANCE',
  `meta_json` json NULL COMMENT '设备参数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_equipment`(`tenant_id` ASC, `equipment_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_equipment_type`(`tenant_id` ASC, `equipment_type` ASC) USING BTREE,
  INDEX `idx_equipment_ws`(`tenant_id` ASC, `workstation_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_equipment
-- ----------------------------

-- ----------------------------
-- Table structure for mes_material_issue
-- ----------------------------
DROP TABLE IF EXISTS `mes_material_issue`;
CREATE TABLE `mes_material_issue`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `issue_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '领料单号',
  `work_order_id` bigint NOT NULL COMMENT '工单ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/ISSUED/RETURNED/CANCELLED',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_issue_no`(`tenant_id` ASC, `issue_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_issue_wo`(`tenant_id` ASC, `work_order_id` ASC) USING BTREE,
  INDEX `idx_issue_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产领料单(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_material_issue
-- ----------------------------

-- ----------------------------
-- Table structure for mes_material_issue_line
-- ----------------------------
DROP TABLE IF EXISTS `mes_material_issue_line`;
CREATE TABLE `mes_material_issue_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `issue_id` bigint NOT NULL COMMENT '领料单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `material_id` bigint NOT NULL COMMENT '物料ID(prd_material.id)',
  `qty` decimal(18, 6) NOT NULL COMMENT '领料数量',
  `uom` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单位',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次号',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_issue_line`(`tenant_id` ASC, `issue_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_issue_line_issue`(`tenant_id` ASC, `issue_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产领料单(行)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_material_issue_line
-- ----------------------------

-- ----------------------------
-- Table structure for mes_production_report
-- ----------------------------
DROP TABLE IF EXISTS `mes_production_report`;
CREATE TABLE `mes_production_report`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `report_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报工单号',
  `work_order_id` bigint NOT NULL COMMENT '工单ID',
  `step_no` int NULL DEFAULT NULL COMMENT '工序序号(可空表示整单报工)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/POSTED/CANCELLED',
  `good_qty` int NOT NULL DEFAULT 0 COMMENT '良品数',
  `bad_qty` int NOT NULL DEFAULT 0 COMMENT '不良数',
  `scrap_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报废原因',
  `extra_json` json NULL COMMENT '扩展(工时/机台参数)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_report_no`(`tenant_id` ASC, `report_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_report_wo`(`tenant_id` ASC, `work_order_id` ASC) USING BTREE,
  INDEX `idx_report_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产报工(用于产量/工时/良率统计)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_production_report
-- ----------------------------

-- ----------------------------
-- Table structure for mes_qc_order
-- ----------------------------
DROP TABLE IF EXISTS `mes_qc_order`;
CREATE TABLE `mes_qc_order`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `qc_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '质检单号',
  `work_order_id` bigint NOT NULL COMMENT '工单ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/INSPECTING/PASSED/FAILED',
  `result` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'PASS/FAIL',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_qc_no`(`tenant_id` ASC, `qc_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_qc_wo`(`tenant_id` ASC, `work_order_id` ASC) USING BTREE,
  INDEX `idx_qc_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '质检单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_qc_order
-- ----------------------------

-- ----------------------------
-- Table structure for mes_qc_order_item
-- ----------------------------
DROP TABLE IF EXISTS `mes_qc_order_item`;
CREATE TABLE `mes_qc_order_item`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `qc_order_id` bigint NOT NULL COMMENT '质检单ID',
  `item_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '检验项编码',
  `item_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '检验项名称',
  `standard` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标准',
  `result` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'PASS/FAIL',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_qc_item`(`tenant_id` ASC, `qc_order_id` ASC, `item_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_qc_item_qc`(`tenant_id` ASC, `qc_order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '质检项' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_qc_order_item
-- ----------------------------

-- ----------------------------
-- Table structure for mes_schedule_audit
-- ----------------------------
DROP TABLE IF EXISTS `mes_schedule_audit`;
CREATE TABLE `mes_schedule_audit`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `plan_id` bigint NOT NULL COMMENT '排产计划ID',
  `work_order_id` bigint NOT NULL COMMENT '工单ID',
  `workstation_id` bigint NOT NULL COMMENT '工位ID',
  `action` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作(CREATE/MOVE/RESIZE/SWAP/CANCEL/LOCK)',
  `from_start_at` datetime(3) NULL DEFAULT NULL COMMENT '调整前开始',
  `from_end_at` datetime(3) NULL DEFAULT NULL COMMENT '调整前结束',
  `to_start_at` datetime(3) NULL DEFAULT NULL COMMENT '调整后开始',
  `to_end_at` datetime(3) NULL DEFAULT NULL COMMENT '调整后结束',
  `operator_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SYSTEM/USER',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人',
  `note` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `meta_json` json NULL COMMENT '扩展(拖拽来源/冲突信息)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_schedule_audit_plan`(`tenant_id` ASC, `plan_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_schedule_audit_wo`(`tenant_id` ASC, `work_order_id` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '排产审计(支持甘特图拖拽调整留痕)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_schedule_audit
-- ----------------------------

-- ----------------------------
-- Table structure for mes_schedule_item
-- ----------------------------
DROP TABLE IF EXISTS `mes_schedule_item`;
CREATE TABLE `mes_schedule_item`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `plan_id` bigint NOT NULL COMMENT '排产计划ID',
  `work_order_id` bigint NOT NULL COMMENT '工单ID',
  `workstation_id` bigint NOT NULL COMMENT '工位/产线ID',
  `scheduled_start_at` datetime(3) NOT NULL COMMENT '排产开始时间',
  `scheduled_end_at` datetime(3) NOT NULL COMMENT '排产结束时间',
  `sequence_no` int NOT NULL COMMENT '序号(同工位排序)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SCHEDULED/STARTED/DONE/CANCELLED',
  `is_locked` tinyint NOT NULL DEFAULT 0 COMMENT '是否锁定(1锁定不自动重排)',
  `source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源(AUTO/MANUAL/IMPORT)',
  `meta_json` json NULL COMMENT '扩展(换线/工时分解)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_plan_item`(`tenant_id` ASC, `plan_id` ASC, `workstation_id` ASC, `sequence_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_plan_wo`(`tenant_id` ASC, `plan_id` ASC, `work_order_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_plan_item_ws_time`(`tenant_id` ASC, `workstation_id` ASC, `scheduled_start_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '排产明细(工单→工位时间窗)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_schedule_item
-- ----------------------------

-- ----------------------------
-- Table structure for mes_schedule_lock
-- ----------------------------
DROP TABLE IF EXISTS `mes_schedule_lock`;
CREATE TABLE `mes_schedule_lock`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `lock_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '锁Key(PLAN/WORKSTATION/WORK_ORDER)',
  `lock_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '锁类型(PLAN/WORKSTATION/WORK_ORDER)',
  `lock_ref_id` bigint NOT NULL COMMENT '引用ID(按type解释)',
  `locked_by` bigint NULL DEFAULT NULL COMMENT '锁定人(用户ID)',
  `lock_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '锁定原因',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'LOCKED/RELEASED',
  `expire_at` datetime(3) NULL DEFAULT NULL COMMENT '过期时间(可选)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_schedule_lock`(`tenant_id` ASC, `lock_key` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_schedule_lock_ref`(`tenant_id` ASC, `lock_type` ASC, `lock_ref_id` ASC) USING BTREE,
  INDEX `idx_schedule_lock_status`(`tenant_id` ASC, `status` ASC, `expire_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '排产锁(防止并发修改/支持锁定区间)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_schedule_lock
-- ----------------------------

-- ----------------------------
-- Table structure for mes_schedule_plan
-- ----------------------------
DROP TABLE IF EXISTS `mes_schedule_plan`;
CREATE TABLE `mes_schedule_plan`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `plan_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '排产计划号',
  `plan_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型(AUTO/MANUAL/REPLAN)',
  `horizon_start` date NOT NULL COMMENT '计划开始日期',
  `horizon_end` date NOT NULL COMMENT '计划结束日期',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/RUNNING/PUBLISHED/CANCELLED',
  `strategy_json` json NULL COMMENT '排产策略(优先级/交期/设备约束等)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_plan_no`(`tenant_id` ASC, `plan_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_plan_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '排产计划(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_schedule_plan
-- ----------------------------

-- ----------------------------
-- Table structure for mes_shift
-- ----------------------------
DROP TABLE IF EXISTS `mes_shift`;
CREATE TABLE `mes_shift`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `shift_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '班次编码',
  `shift_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '班次名称',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `break_minutes` int NOT NULL DEFAULT 0 COMMENT '休息分钟',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shift_code`(`tenant_id` ASC, `shift_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '班次定义' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_shift
-- ----------------------------

-- ----------------------------
-- Table structure for mes_work_order
-- ----------------------------
DROP TABLE IF EXISTS `mes_work_order`;
CREATE TABLE `mes_work_order`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `work_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单号',
  `fulfillment_id` bigint NOT NULL COMMENT '履约单ID',
  `routing_id` bigint NULL DEFAULT NULL COMMENT '工艺路线ID(冗余保存)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/SCHEDULED/RUNNING/QC/FINISHED/FAILED',
  `priority` int NOT NULL DEFAULT 100 COMMENT '优先级',
  `planned_start_at` datetime(3) NULL DEFAULT NULL COMMENT '计划开始',
  `planned_end_at` datetime(3) NULL DEFAULT NULL COMMENT '计划结束',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wo_no`(`tenant_id` ASC, `work_order_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_wo_ff`(`tenant_id` ASC, `fulfillment_id` ASC) USING BTREE,
  INDEX `idx_wo_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产工单(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_work_order
-- ----------------------------

-- ----------------------------
-- Table structure for mes_work_order_item
-- ----------------------------
DROP TABLE IF EXISTS `mes_work_order_item`;
CREATE TABLE `mes_work_order_item`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `work_order_id` bigint NOT NULL COMMENT '工单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `qty` int NOT NULL COMMENT '数量',
  `surface_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '面编码(可选)',
  `production_file_id` bigint NULL DEFAULT NULL COMMENT '生产图文件ID(可选)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/RUNNING/FINISHED/FAILED',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wo_line`(`tenant_id` ASC, `work_order_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_wo_item_wo`(`tenant_id` ASC, `work_order_id` ASC) USING BTREE,
  INDEX `idx_wo_item_sku`(`tenant_id` ASC, `sku_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产工单(行)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_work_order_item
-- ----------------------------

-- ----------------------------
-- Table structure for mes_work_order_op
-- ----------------------------
DROP TABLE IF EXISTS `mes_work_order_op`;
CREATE TABLE `mes_work_order_op`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `work_order_id` bigint NOT NULL COMMENT '工单ID',
  `step_no` int NOT NULL COMMENT '工序序号',
  `op_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工序编码',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'PENDING/RUNNING/DONE/FAILED',
  `workstation_id` bigint NULL DEFAULT NULL COMMENT '工位ID',
  `equipment_id` bigint NULL DEFAULT NULL COMMENT '设备ID',
  `start_at` datetime(3) NULL DEFAULT NULL COMMENT '开始时间',
  `end_at` datetime(3) NULL DEFAULT NULL COMMENT '结束时间',
  `result_json` json NULL COMMENT '结果(如产出/损耗/报废原因)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wo_op`(`tenant_id` ASC, `work_order_id` ASC, `step_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_wo_op_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工单工序实例' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_work_order_op
-- ----------------------------

-- ----------------------------
-- Table structure for mes_work_order_op_log
-- ----------------------------
DROP TABLE IF EXISTS `mes_work_order_op_log`;
CREATE TABLE `mes_work_order_op_log`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `work_order_id` bigint NOT NULL COMMENT '工单ID',
  `step_no` int NOT NULL COMMENT '工序序号',
  `event_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'START/PAUSE/RESUME/DONE/FAIL',
  `message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `extra_json` json NULL COMMENT '扩展',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_wo_op_log`(`tenant_id` ASC, `work_order_id` ASC, `step_no` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工单工序日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_work_order_op_log
-- ----------------------------

-- ----------------------------
-- Table structure for mes_workstation
-- ----------------------------
DROP TABLE IF EXISTS `mes_workstation`;
CREATE TABLE `mes_workstation`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `workstation_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工位/产线编码',
  `workstation_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工位/产线名称',
  `workstation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'PRINT/CUT/SEW/PACK/QC',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `config_json` json NULL COMMENT '配置',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_workstation`(`tenant_id` ASC, `workstation_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_workstation_type`(`tenant_id` ASC, `workstation_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工位/产线' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mes_workstation
-- ----------------------------

-- ----------------------------
-- Table structure for oms_exception
-- ----------------------------
DROP TABLE IF EXISTS `oms_exception`;
CREATE TABLE `oms_exception`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型(ORDER/FULFILLMENT/ART/MES/WMS/TMS)',
  `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务单号',
  `severity` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'LOW/MEDIUM/HIGH',
  `exception_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '异常编码',
  `exception_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '异常描述',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OPEN/RESOLVED/CLOSED',
  `resolve_note` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理说明',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ex_biz`(`tenant_id` ASC, `biz_type` ASC, `biz_no` ASC) USING BTREE,
  INDEX `idx_ex_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '异常单/告警' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_exception
-- ----------------------------

-- ----------------------------
-- Table structure for oms_fulfillment
-- ----------------------------
DROP TABLE IF EXISTS `oms_fulfillment`;
CREATE TABLE `oms_fulfillment`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `fulfillment_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '履约单号',
  `unified_order_id` bigint NOT NULL COMMENT '统一订单ID',
  `fulfillment_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'POD/RESELL/OTHER',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态(CREATED/RELEASED/IN_PRODUCTION/...)',
  `priority` int NOT NULL DEFAULT 100 COMMENT '优先级(数值越小越高)',
  `warehouse_id` bigint NULL DEFAULT NULL COMMENT '出库仓ID(可空，按策略分配)',
  `expected_ship_at` datetime(3) NULL DEFAULT NULL COMMENT '期望发货时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_fulfillment_no`(`tenant_id` ASC, `fulfillment_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ff_order`(`tenant_id` ASC, `unified_order_id` ASC) USING BTREE,
  INDEX `idx_ff_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '履约单(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_fulfillment
-- ----------------------------

-- ----------------------------
-- Table structure for oms_fulfillment_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_fulfillment_item`;
CREATE TABLE `oms_fulfillment_item`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `fulfillment_id` bigint NOT NULL COMMENT '履约单ID',
  `unified_order_item_id` bigint NOT NULL COMMENT '订单行ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `qty` int NOT NULL COMMENT '数量',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
  `personalization_json` json NULL COMMENT '个性化字段冗余',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ff_line`(`tenant_id` ASC, `fulfillment_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ff_item_ff`(`tenant_id` ASC, `fulfillment_id` ASC) USING BTREE,
  INDEX `idx_ff_item_order_item`(`tenant_id` ASC, `unified_order_item_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '履约单(行)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_fulfillment_item
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order_address
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_address`;
CREATE TABLE `oms_order_address`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `unified_order_id` bigint NOT NULL COMMENT '统一订单ID',
  `address_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SHIP_TO/BILL_TO',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收件人',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `country_code` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国家',
  `state` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '州/省',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '城市',
  `zip` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮编',
  `address1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址1',
  `address2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址2',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_addr`(`tenant_id` ASC, `unified_order_id` ASC, `address_type` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_order_addr_order`(`tenant_id` ASC, `unified_order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单地址' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_order_address
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order_status_log
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_status_log`;
CREATE TABLE `oms_order_status_log`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `unified_order_id` bigint NOT NULL COMMENT '统一订单ID',
  `from_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原状态',
  `to_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '新状态',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原因',
  `operator_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SYSTEM/USER',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `extra_json` json NULL COMMENT '扩展',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_status_log_order`(`tenant_id` ASC, `unified_order_id` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单状态流水' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_order_status_log
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order_tag
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_tag`;
CREATE TABLE `oms_order_tag`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `unified_order_id` bigint NOT NULL COMMENT '统一订单ID',
  `tag_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签编码',
  `tag_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签名称',
  `tag_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签类型(风险/运营/渠道...)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_tag`(`tenant_id` ASC, `unified_order_id` ASC, `tag_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tag_code`(`tenant_id` ASC, `tag_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单标签' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_order_tag
-- ----------------------------

-- ----------------------------
-- Table structure for oms_refund
-- ----------------------------
DROP TABLE IF EXISTS `oms_refund`;
CREATE TABLE `oms_refund`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '退款单号',
  `unified_order_id` bigint NOT NULL COMMENT '统一订单ID',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台编码(可选)',
  `shop_id` bigint NULL DEFAULT NULL COMMENT '店铺ID(可选)',
  `platform_refund_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台退款ID(可选)',
  `refund_amount` decimal(18, 2) NOT NULL COMMENT '退款金额',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '币种',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/PROCESSING/SUCCESS/FAILED/CLOSED',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原因',
  `response_json` json NULL COMMENT '平台响应',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_refund_no`(`tenant_id` ASC, `refund_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_refund_order`(`tenant_id` ASC, `unified_order_id` ASC) USING BTREE,
  INDEX `idx_refund_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退款单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_refund
-- ----------------------------

-- ----------------------------
-- Table structure for oms_return_request
-- ----------------------------
DROP TABLE IF EXISTS `oms_return_request`;
CREATE TABLE `oms_return_request`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `return_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '退货申请号',
  `unified_order_id` bigint NOT NULL COMMENT '统一订单ID',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台编码(可选)',
  `shop_id` bigint NULL DEFAULT NULL COMMENT '店铺ID(可选)',
  `platform_return_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台退货单ID(可选)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/APPROVED/REJECTED/RECEIVING/RECEIVED/CLOSED',
  `reason_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原因码',
  `reason_detail` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原因说明',
  `return_address_json` json NULL COMMENT '退回地址',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_return_no`(`tenant_id` ASC, `return_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_return_order`(`tenant_id` ASC, `unified_order_id` ASC) USING BTREE,
  INDEX `idx_return_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退货申请(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_return_request
-- ----------------------------

-- ----------------------------
-- Table structure for oms_return_request_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_return_request_item`;
CREATE TABLE `oms_return_request_item`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `return_id` bigint NOT NULL COMMENT '退货申请ID',
  `line_no` int NOT NULL COMMENT '行号',
  `unified_order_item_id` bigint NOT NULL COMMENT '订单行ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `qty` int NOT NULL COMMENT '退货数量',
  `condition_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '货品状态(UNOPENED/OPENED/DAMAGED)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_return_line`(`tenant_id` ASC, `return_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_return_item_return`(`tenant_id` ASC, `return_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退货申请(行)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_return_request_item
-- ----------------------------

-- ----------------------------
-- Table structure for oms_unified_order
-- ----------------------------
DROP TABLE IF EXISTS `oms_unified_order`;
CREATE TABLE `oms_unified_order`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `unified_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '统一订单号(内部)',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台编码',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `platform_order_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台订单ID',
  `platform_order_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台订单号',
  `service_level` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台物流服务等级/配送方式',
  `order_created_at` datetime(3) NULL DEFAULT NULL COMMENT '平台下单时间',
  `buyer_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '买家姓名',
  `buyer_email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '买家邮箱',
  `buyer_note` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '买家留言',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '币种',
  `total_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '订单总金额',
  `shipping_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '运费',
  `tax_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '税费',
  `discount_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '折扣',
  `order_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单状态(NEW/VALIDATED/SPLIT/...)',
  `payment_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付状态(UNPAID/PAID/REFUNDED/PART_REFUNDED)',
  `risk_flag` tinyint NOT NULL DEFAULT 0 COMMENT '风控标记(可选)',
  `source_raw_order_id` bigint NULL DEFAULT NULL COMMENT '来源原始订单ID',
  `extra_json` json NULL COMMENT '扩展字段',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_unified`(`tenant_id` ASC, `unified_order_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_platform_order`(`tenant_id` ASC, `platform_code` ASC, `shop_id` ASC, `platform_order_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_oms_status`(`tenant_id` ASC, `order_status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_oms_shop_time`(`tenant_id` ASC, `shop_id` ASC, `order_created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一订单(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_unified_order
-- ----------------------------

-- ----------------------------
-- Table structure for oms_unified_order_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_unified_order_item`;
CREATE TABLE `oms_unified_order_item`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `unified_order_id` bigint NOT NULL COMMENT '统一订单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SKU编码冗余',
  `platform_sku` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台SKU/刊登SKU',
  `item_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台商品标题',
  `qty` int NOT NULL COMMENT '数量',
  `unit_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '单价',
  `item_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '行状态',
  `personalization_json` json NULL COMMENT '个性化字段(JSON)',
  `extra_json` json NULL COMMENT '扩展',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_line`(`tenant_id` ASC, `unified_order_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_order_item_order`(`tenant_id` ASC, `unified_order_id` ASC) USING BTREE,
  INDEX `idx_order_item_sku`(`tenant_id` ASC, `sku_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一订单(行)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_unified_order_item
-- ----------------------------

-- ----------------------------
-- Table structure for plat_api_credential
-- ----------------------------
DROP TABLE IF EXISTS `plat_api_credential`;
CREATE TABLE `plat_api_credential`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台编码',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `auth_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '授权类型(OAUTH/KEY_PAIR/TOKEN)',
  `encrypted_payload` json NOT NULL COMMENT '加密后的凭证载荷(access/refresh/keys等)',
  `expires_at` datetime(3) NULL DEFAULT NULL COMMENT 'access_token过期时间',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_credential`(`tenant_id` ASC, `platform_code` ASC, `shop_id` ASC, `auth_type` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_credential_expire`(`tenant_id` ASC, `platform_code` ASC, `expires_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台API凭证(加密存储)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of plat_api_credential
-- ----------------------------

-- ----------------------------
-- Table structure for plat_listing
-- ----------------------------
DROP TABLE IF EXISTS `plat_listing`;
CREATE TABLE `plat_listing`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台编码',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `listing_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台刊登ID(ASIN/SPU等)',
  `listing_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '刊登标题',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE/DELISTED',
  `payload_json` json NULL COMMENT '刊登原始信息/属性',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_listing`(`tenant_id` ASC, `platform_code` ASC, `shop_id` ASC, `listing_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_listing_status`(`tenant_id` ASC, `platform_code` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台刊登(用于对账/刊登管理)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of plat_listing
-- ----------------------------

-- ----------------------------
-- Table structure for plat_platform
-- ----------------------------
DROP TABLE IF EXISTS `plat_platform`;
CREATE TABLE `plat_platform`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台编码(AMAZON/TEMU/SHEIN)',
  `platform_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台名称',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ENABLED/DISABLED',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_platform_code`(`platform_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台定义' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of plat_platform
-- ----------------------------

-- ----------------------------
-- Table structure for plat_shop
-- ----------------------------
DROP TABLE IF EXISTS `plat_shop`;
CREATE TABLE `plat_shop`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台编码',
  `shop_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '店铺编码(租户内唯一)',
  `shop_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '店铺名称',
  `site_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '站点/国家(如 US/DE)',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认币种',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shop`(`tenant_id` ASC, `platform_code` ASC, `shop_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_shop_status`(`tenant_id` ASC, `platform_code` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '店铺' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of plat_shop
-- ----------------------------
INSERT INTO `plat_shop` VALUES (2001, 1, 1, '2026-02-12 08:10:00.000', '2026-02-12 08:10:00.000', 0, 0, 0, 0, 'seed', 'AMAZON', 'AMZ_US_1', 'Amazon US Shop 1', 'US', 'USD', 'ENABLED');

-- ----------------------------
-- Table structure for plat_sku_mapping
-- ----------------------------
DROP TABLE IF EXISTS `plat_sku_mapping`;
CREATE TABLE `plat_sku_mapping`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台编码',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `platform_sku` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台SKU/卖家SKU',
  `sku_id` bigint NOT NULL COMMENT '内部SKU ID',
  `listing_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联刊登ID(可选)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_platform_sku`(`tenant_id` ASC, `platform_code` ASC, `shop_id` ASC, `platform_sku` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_mapping_sku`(`tenant_id` ASC, `sku_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台SKU映射(平台SKU→内部SKU)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of plat_sku_mapping
-- ----------------------------

-- ----------------------------
-- Table structure for pod_template
-- ----------------------------
DROP TABLE IF EXISTS `pod_template`;
CREATE TABLE `pod_template`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板编码',
  `template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `version_no` int NOT NULL DEFAULT 1 COMMENT '模板版本号',
  `canvas_width_mm` decimal(10, 2) NOT NULL COMMENT '画布宽(mm)',
  `canvas_height_mm` decimal(10, 2) NOT NULL COMMENT '画布高(mm)',
  `finished_width_mm` decimal(10, 2) NOT NULL COMMENT '成品宽(mm)',
  `finished_height_mm` decimal(10, 2) NOT NULL COMMENT '成品高(mm)',
  `bleed_mm` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '默认出血(mm)',
  `safe_mm` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '默认安全边距(mm)',
  `template_meta_json` json NULL COMMENT '模板元数据(印刷方式/默认字体等)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'DRAFT/ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tpl_code_ver`(`tenant_id` ASC, `template_code` ASC, `version_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tpl_status`(`tenant_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'POD模板(版本化)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pod_template
-- ----------------------------

-- ----------------------------
-- Table structure for pod_template_area
-- ----------------------------
DROP TABLE IF EXISTS `pod_template_area`;
CREATE TABLE `pod_template_area`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `surface_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '面编码',
  `area_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域编码(面内唯一)',
  `area_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区域名称',
  `shape_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'RECT/PATH/CIRCLE',
  `area_json` json NOT NULL COMMENT '区域定义(JSON:坐标/轮廓点/变换矩阵)',
  `editable_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'TEXT/IMAGE/MIXED',
  `constraints_json` json NULL COMMENT '约束(最小/最大缩放/旋转等)',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tpl_area`(`tenant_id` ASC, `template_id` ASC, `surface_code` ASC, `area_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_area_tpl`(`tenant_id` ASC, `template_id` ASC, `surface_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模板-可定制区域(含异形轮廓)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pod_template_area
-- ----------------------------

-- ----------------------------
-- Table structure for pod_template_binding
-- ----------------------------
DROP TABLE IF EXISTS `pod_template_binding`;
CREATE TABLE `pod_template_binding`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `binding_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `default_personalization_json` json NULL COMMENT '默认个性化字段(可选)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tpl_bind`(`tenant_id` ASC, `sku_id` ASC, `template_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_bind_tpl`(`tenant_id` ASC, `template_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SKU-模板绑定' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pod_template_binding
-- ----------------------------

-- ----------------------------
-- Table structure for pod_template_layer_rule
-- ----------------------------
DROP TABLE IF EXISTS `pod_template_layer_rule`;
CREATE TABLE `pod_template_layer_rule`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `surface_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '面编码',
  `rule_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则编码',
  `rule_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'TEXT_LAYER/IMAGE_LAYER/WHITE_INK/EXPORT_PROFILE',
  `rule_json` json NOT NULL COMMENT '规则定义(层名映射/通道/ICC/导出参数等)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_layer_rule`(`tenant_id` ASC, `template_id` ASC, `surface_code` ASC, `rule_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_layer_rule_tpl`(`tenant_id` ASC, `template_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模板图层规则(文字/图片/白墨/导出)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pod_template_layer_rule
-- ----------------------------

-- ----------------------------
-- Table structure for pod_template_surface
-- ----------------------------
DROP TABLE IF EXISTS `pod_template_surface`;
CREATE TABLE `pod_template_surface`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `surface_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '面编码(FRONT/BACK/LEFT_SLEEVE...)',
  `surface_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '面名称',
  `print_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '打印模式(DTG/DTF/UV/SUB)',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tpl_surface`(`tenant_id` ASC, `template_id` ASC, `surface_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_surface_tpl`(`tenant_id` ASC, `template_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模板-面' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pod_template_surface
-- ----------------------------

-- ----------------------------
-- Table structure for prd_bom
-- ----------------------------
DROP TABLE IF EXISTS `prd_bom`;
CREATE TABLE `prd_bom`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `version_no` int NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'DRAFT/ACTIVE/INACTIVE',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_bom_sku_ver`(`tenant_id` ASC, `sku_id` ASC, `version_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_bom_status`(`tenant_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'BOM(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of prd_bom
-- ----------------------------

-- ----------------------------
-- Table structure for prd_bom_item
-- ----------------------------
DROP TABLE IF EXISTS `prd_bom_item`;
CREATE TABLE `prd_bom_item`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `bom_id` bigint NOT NULL COMMENT 'BOM ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `qty` decimal(18, 6) NOT NULL COMMENT '用量',
  `uom` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单位',
  `loss_rate` decimal(10, 6) NOT NULL DEFAULT 0.000000 COMMENT '损耗率(0-1)',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_bom_item`(`tenant_id` ASC, `bom_id` ASC, `material_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_bom_item_bom`(`tenant_id` ASC, `bom_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'BOM(明细)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of prd_bom_item
-- ----------------------------

-- ----------------------------
-- Table structure for prd_material
-- ----------------------------
DROP TABLE IF EXISTS `prd_material`;
CREATE TABLE `prd_material`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `material_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物料编码(租户内唯一)',
  `material_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物料名称',
  `material_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'RAW/SEMI/FINISHED/PACK',
  `uom` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计量单位(PCS/M/ROLL...)',
  `spec_json` json NULL COMMENT '规格JSON',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_material_code`(`tenant_id` ASC, `material_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_material_type`(`tenant_id` ASC, `material_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物料主数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of prd_material
-- ----------------------------

-- ----------------------------
-- Table structure for prd_routing
-- ----------------------------
DROP TABLE IF EXISTS `prd_routing`;
CREATE TABLE `prd_routing`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `version_no` int NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'DRAFT/ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_routing_sku_ver`(`tenant_id` ASC, `sku_id` ASC, `version_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_routing_status`(`tenant_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工艺路线(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of prd_routing
-- ----------------------------

-- ----------------------------
-- Table structure for prd_routing_step
-- ----------------------------
DROP TABLE IF EXISTS `prd_routing_step`;
CREATE TABLE `prd_routing_step`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `routing_id` bigint NOT NULL COMMENT '工艺路线ID',
  `step_no` int NOT NULL COMMENT '工序序号',
  `op_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工序编码(如 PRINT/CUT/SEW/PACK)',
  `op_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工序名称',
  `equipment_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备类型(可空)',
  `std_cycle_seconds` int NULL DEFAULT NULL COMMENT '标准节拍(秒)',
  `qc_required` tinyint NOT NULL DEFAULT 0 COMMENT '是否需要质检',
  `config_json` json NULL COMMENT '工序配置',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_routing_step`(`tenant_id` ASC, `routing_id` ASC, `step_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_routing_step_op`(`tenant_id` ASC, `op_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工艺路线(步骤)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of prd_routing_step
-- ----------------------------

-- ----------------------------
-- Table structure for prd_sku
-- ----------------------------
DROP TABLE IF EXISTS `prd_sku`;
CREATE TABLE `prd_sku`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `spu_id` bigint NOT NULL COMMENT 'SPU ID',
  `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SKU编码(租户内唯一)',
  `sku_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SKU名称',
  `price` decimal(18, 2) NULL DEFAULT NULL COMMENT '售价(可选)',
  `weight_g` int NULL DEFAULT NULL COMMENT '重量(g)',
  `attributes_json` json NULL COMMENT '属性JSON(尺码/颜色等)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'DRAFT/ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sku_code`(`tenant_id` ASC, `sku_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_sku_spu`(`tenant_id` ASC, `spu_id` ASC) USING BTREE,
  INDEX `idx_sku_status`(`tenant_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品SKU' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of prd_sku
-- ----------------------------
INSERT INTO `prd_sku` VALUES (3101, 1, 1, '2026-02-12 08:10:00.000', '2026-02-12 08:10:00.000', 0, 0, 0, 0, 'seed', 3001, 'SKU-TSHIRT-BLACK-M', 'T-Shirt Black M', 19.99, 200, '{\"size\": \"M\", \"color\": \"black\"}', 'ENABLED');

-- ----------------------------
-- Table structure for prd_sku_barcode
-- ----------------------------
DROP TABLE IF EXISTS `prd_sku_barcode`;
CREATE TABLE `prd_sku_barcode`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `barcode` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '条码(UPC/EAN/自编码)',
  `barcode_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'UPC/EAN/CODE128/OTHER',
  `is_primary` tinyint NOT NULL DEFAULT 0 COMMENT '是否主条码',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_barcode`(`tenant_id` ASC, `barcode` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_barcode_sku`(`tenant_id` ASC, `sku_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SKU条码' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of prd_sku_barcode
-- ----------------------------

-- ----------------------------
-- Table structure for prd_spu
-- ----------------------------
DROP TABLE IF EXISTS `prd_spu`;
CREATE TABLE `prd_spu`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `spu_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SPU编码(租户内唯一)',
  `spu_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SPU名称',
  `category_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类目编码',
  `brand` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'DRAFT/ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_spu_code`(`tenant_id` ASC, `spu_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_spu_status`(`tenant_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品SPU' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of prd_spu
-- ----------------------------
INSERT INTO `prd_spu` VALUES (3001, 1, 1, '2026-02-12 08:10:00.000', '2026-02-12 08:10:00.000', 0, 0, 0, 0, 'seed', 'SPU-TSHIRT', 'POD T-Shirt', 'APPAREL', 'DEMO', 'ENABLED');

-- ----------------------------
-- Table structure for pur_price_contract
-- ----------------------------
DROP TABLE IF EXISTS `pur_price_contract`;
CREATE TABLE `pur_price_contract`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `contract_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '合同号/协议号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `effective_from` date NOT NULL COMMENT '生效日期',
  `effective_to` date NULL DEFAULT NULL COMMENT '失效日期',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'DRAFT/ACTIVE/INACTIVE',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `meta_json` json NULL COMMENT '扩展(结算规则版本)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_contract_no`(`tenant_id` ASC, `contract_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_contract_supplier`(`tenant_id` ASC, `supplier_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '供应商计价合同(用于对账解释)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pur_price_contract
-- ----------------------------

-- ----------------------------
-- Table structure for pur_price_contract_item
-- ----------------------------
DROP TABLE IF EXISTS `pur_price_contract_item`;
CREATE TABLE `pur_price_contract_item`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `contract_id` bigint NOT NULL COMMENT '合同ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NULL DEFAULT NULL COMMENT 'SKU(成品/物料)',
  `service_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务编码(印刷/包装/打样/加急等)',
  `fee_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '费用项(fin_fee_code)',
  `price` decimal(18, 6) NOT NULL COMMENT '单价',
  `uom` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单位',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_contract_item`(`tenant_id` ASC, `contract_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_contract_item_sku`(`tenant_id` ASC, `sku_id` ASC) USING BTREE,
  INDEX `idx_contract_item_fee`(`tenant_id` ASC, `fee_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '供应商合同计价明细' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pur_price_contract_item
-- ----------------------------

-- ----------------------------
-- Table structure for pur_purchase_order
-- ----------------------------
DROP TABLE IF EXISTS `pur_purchase_order`;
CREATE TABLE `pur_purchase_order`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `po_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '采购单号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/CONFIRMED/RECEIVING/RECEIVED/CANCELLED',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '币种',
  `total_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '总金额',
  `expected_arrive_at` datetime(3) NULL DEFAULT NULL COMMENT '预计到货',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_po_no`(`tenant_id` ASC, `po_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_po_supplier`(`tenant_id` ASC, `supplier_id` ASC) USING BTREE,
  INDEX `idx_po_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '采购单(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pur_purchase_order
-- ----------------------------

-- ----------------------------
-- Table structure for pur_purchase_order_line
-- ----------------------------
DROP TABLE IF EXISTS `pur_purchase_order_line`;
CREATE TABLE `pur_purchase_order_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `po_id` bigint NOT NULL COMMENT '采购单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NOT NULL COMMENT 'SKU/物料ID',
  `qty` int NOT NULL COMMENT '数量',
  `unit_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '单价',
  `amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '金额',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_po_line`(`tenant_id` ASC, `po_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_po_line_po`(`tenant_id` ASC, `po_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '采购单行' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pur_purchase_order_line
-- ----------------------------

-- ----------------------------
-- Table structure for pur_purchase_receipt
-- ----------------------------
DROP TABLE IF EXISTS `pur_purchase_receipt`;
CREATE TABLE `pur_purchase_receipt`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `receipt_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '采购收货单号',
  `po_id` bigint NOT NULL COMMENT '采购单ID',
  `warehouse_id` bigint NOT NULL COMMENT '入库仓ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/RECEIVING/RECEIVED/CLOSED',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_receipt_no`(`tenant_id` ASC, `receipt_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_receipt_po`(`tenant_id` ASC, `po_id` ASC) USING BTREE,
  INDEX `idx_receipt_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '采购收货单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pur_purchase_receipt
-- ----------------------------

-- ----------------------------
-- Table structure for pur_purchase_receipt_line
-- ----------------------------
DROP TABLE IF EXISTS `pur_purchase_receipt_line`;
CREATE TABLE `pur_purchase_receipt_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `receipt_id` bigint NOT NULL COMMENT '收货单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NOT NULL COMMENT 'SKU/物料ID',
  `qty_expected` int NOT NULL COMMENT '应收',
  `qty_received` int NOT NULL DEFAULT 0 COMMENT '实收',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_receipt_line`(`tenant_id` ASC, `receipt_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_receipt_line_receipt`(`tenant_id` ASC, `receipt_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '采购收货单行' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pur_purchase_receipt_line
-- ----------------------------

-- ----------------------------
-- Table structure for pur_purchase_request
-- ----------------------------
DROP TABLE IF EXISTS `pur_purchase_request`;
CREATE TABLE `pur_purchase_request`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `pr_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请购单号',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/APPROVED/REJECTED/CLOSED',
  `requester_id` bigint NULL DEFAULT NULL COMMENT '请购人',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pr_no`(`tenant_id` ASC, `pr_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_pr_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '请购单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pur_purchase_request
-- ----------------------------

-- ----------------------------
-- Table structure for pur_purchase_request_line
-- ----------------------------
DROP TABLE IF EXISTS `pur_purchase_request_line`;
CREATE TABLE `pur_purchase_request_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `pr_id` bigint NOT NULL COMMENT '请购单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NOT NULL COMMENT 'SKU/物料ID',
  `qty` int NOT NULL COMMENT '数量',
  `need_by_at` datetime(3) NULL DEFAULT NULL COMMENT '需求日期',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pr_line`(`tenant_id` ASC, `pr_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_pr_line_pr`(`tenant_id` ASC, `pr_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '请购单行' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pur_purchase_request_line
-- ----------------------------

-- ----------------------------
-- Table structure for pur_supplier
-- ----------------------------
DROP TABLE IF EXISTS `pur_supplier`;
CREATE TABLE `pur_supplier`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `supplier_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商编码(租户内唯一)',
  `supplier_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商名称',
  `country_code` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国家',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_supplier_code`(`tenant_id` ASC, `supplier_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_supplier_status`(`tenant_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '供应商' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pur_supplier
-- ----------------------------

-- ----------------------------
-- Table structure for sys_ai_diagnosis
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_diagnosis`;
CREATE TABLE `sys_ai_diagnosis`  (
  `id` bigint NOT NULL,
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Trace ID',
  `business_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Business Key (e.g. userId, orderId)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, PROCESSING, COMPLETED, FAILED',
  `diagnosis_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Type of diagnosis',
  `result_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Diagnosis Result JSON',
  `tenant_id` bigint NULL DEFAULT NULL,
  `factory_id` bigint NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint NULL DEFAULT NULL,
  `updated_by` bigint NULL DEFAULT NULL,
  `deleted` tinyint(1) NULL DEFAULT 0,
  `version` int NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_id`(`trace_id` ASC) USING BTREE,
  INDEX `idx_business_key`(`business_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI Diagnosis Records' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_ai_diagnosis
-- ----------------------------

-- ----------------------------
-- Table structure for sys_audit_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_audit_log`;
CREATE TABLE `sys_audit_log`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作(如 CREATE_ORDER/UPDATE_ROLE)',
  `resource_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资源类型',
  `resource_id` bigint NULL DEFAULT NULL COMMENT '资源ID',
  `success` tinyint NOT NULL DEFAULT 1 COMMENT '是否成功(1是0否)',
  `error_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败原因',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'UA',
  `extra_json` json NULL COMMENT '扩展信息',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_audit_actor`(`tenant_id` ASC, `created_by` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_audit_action`(`tenant_id` ASC, `action` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审计日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_audit_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `dict_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典编码',
  `dict_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ENABLED/DISABLED',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_code`(`tenant_id` ASC, `dict_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据字典(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `dict_id` bigint NOT NULL COMMENT '字典ID',
  `item_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典项编码',
  `item_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典项名称',
  `item_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '值(可选)',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ENABLED/DISABLED',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_item`(`tenant_id` ASC, `dict_id` ASC, `item_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_dict_item_dict`(`tenant_id` ASC, `dict_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据字典(项)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------

-- ----------------------------
-- Table structure for sys_idempotency_record
-- ----------------------------
DROP TABLE IF EXISTS `sys_idempotency_record`;
CREATE TABLE `sys_idempotency_record`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `biz_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型(如 CREATE_FULFILLMENT)',
  `biz_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务Key(如 fulfillment_no / order_no)',
  `request_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求幂等ID(X-Request-Id)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'PROCESSING/SUCCESS/FAILED',
  `result_snapshot` json NULL COMMENT '结果快照(可选)',
  `expire_at` datetime(3) NULL DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_idem`(`tenant_id` ASC, `biz_type` ASC, `biz_key` ASC, `request_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_idem_biz`(`tenant_id` ASC, `biz_type` ASC, `biz_key` ASC) USING BTREE,
  INDEX `idx_idem_expire`(`tenant_id` ASC, `expire_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '幂等记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_idempotency_record
-- ----------------------------

-- ----------------------------
-- Table structure for sys_idempotent
-- ----------------------------
DROP TABLE IF EXISTS `sys_idempotent`;
CREATE TABLE `sys_idempotent`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `key_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Composite Key: requestId + :: + bizKey',
  `record_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Business Value or Status',
  `expire_at` datetime NOT NULL COMMENT 'Expiration Time',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `tenant_id` bigint NULL DEFAULT NULL,
  `factory_id` bigint NULL DEFAULT NULL,
  `created_by` bigint NULL DEFAULT NULL,
  `updated_by` bigint NULL DEFAULT NULL,
  `deleted` tinyint(1) NULL DEFAULT 0,
  `version` int NULL DEFAULT 1,
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Trace ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_key_id`(`key_id` ASC) USING BTREE,
  INDEX `idx_expire_at`(`expire_at` ASC) USING BTREE,
  INDEX `idx_trace_id`(`trace_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Idempotent Request Log' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_idempotent
-- ----------------------------

-- ----------------------------
-- Table structure for sys_inbox_event
-- ----------------------------
DROP TABLE IF EXISTS `sys_inbox_event`;
CREATE TABLE `sys_inbox_event`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `event_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件ID(来自outbox)',
  `consumer` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消费者标识(服务/handler)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'RECEIVED/PROCESSED/FAILED',
  `last_error` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误',
  `payload_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '载荷哈希(可选)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inbox`(`tenant_id` ASC, `consumer` ASC, `event_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_inbox_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Inbox去重表(防重复消费)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_inbox_event
-- ----------------------------

-- ----------------------------
-- Table structure for sys_job_lock
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_lock`;
CREATE TABLE `sys_job_lock`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `lock_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '锁Key(如 JOB:AMAZON_PULL:SHOP:1)',
  `owner` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '持有者(节点/实例)',
  `lock_at` datetime(3) NOT NULL COMMENT '加锁时间',
  `expire_at` datetime(3) NOT NULL COMMENT '过期时间',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'LOCKED/RELEASED',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_job_lock`(`tenant_id` ASC, `lock_key` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_job_lock_expire`(`tenant_id` ASC, `expire_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务互斥锁(防并发重复拉单/回传)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_job_lock
-- ----------------------------

-- ----------------------------
-- Table structure for sys_kv_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_kv_config`;
CREATE TABLE `sys_kv_config`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `config_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置Key(全局唯一)',
  `config_value` json NOT NULL COMMENT '配置Value(JSON)',
  `scope_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '范围类型(GLOBAL/TENANT/FACTORY/SHOP)',
  `scope_id` bigint NULL DEFAULT NULL COMMENT '范围ID(可空)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ENABLED/DISABLED',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key_scope`(`tenant_id` ASC, `config_key` ASC, `scope_type` ASC, `scope_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_config_scope`(`tenant_id` ASC, `scope_type` ASC, `scope_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置中心(KV)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_kv_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint NOT NULL,
  `tenant_id` bigint NULL DEFAULT NULL,
  `factory_id` bigint NULL DEFAULT NULL,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `login_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------
INSERT INTO `sys_login_log` VALUES (2022486498957631490, 1, 1, 'operator', NULL, 'SUCCESS', 'Login successful', '2026-02-14 09:42:12.000', '2026-02-14 09:42:12.000', 0, 0, NULL, NULL, '67b3947f5c60492c8e226aedf824141a');
INSERT INTO `sys_login_log` VALUES (2022486501805563906, 1, 1, 'admin', NULL, 'SUCCESS', 'Login successful', '2026-02-14 09:42:13.000', '2026-02-14 09:42:13.000', 0, 0, NULL, NULL, '5a93da49f48e47e9b2af9684365e6471');
INSERT INTO `sys_login_log` VALUES (2022549347671269378, 1, 1, 'admin', NULL, 'SUCCESS', 'Login successful', '2026-02-14 13:51:56.286', '2026-02-14 13:51:56.295', 0, 0, NULL, NULL, '93be15bc-f73f-49f0-b391-0a99dc023806');

-- ----------------------------
-- Table structure for sys_outbox_event
-- ----------------------------
DROP TABLE IF EXISTS `sys_outbox_event`;
CREATE TABLE `sys_outbox_event`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `event_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件ID(全局唯一)',
  `event_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件类型(如 ORDER_CREATED)',
  `aggregate_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '聚合类型(ORDER/FULFILLMENT...)',
  `aggregate_id` bigint NOT NULL COMMENT '聚合ID',
  `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务单号(可选)',
  `payload_json` json NOT NULL COMMENT '事件载荷',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'NEW/SENT/FAILED',
  `attempts` int NOT NULL DEFAULT 0 COMMENT '尝试次数',
  `next_retry_at` datetime(3) NULL DEFAULT NULL COMMENT '下次重试时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_outbox_event_id`(`tenant_id` ASC, `event_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_outbox_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_outbox_agg`(`tenant_id` ASC, `aggregate_type` ASC, `aggregate_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Outbox事件表(事务内落库，异步投递)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_outbox_event
-- ----------------------------

-- ----------------------------
-- Table structure for sys_rate_limit
-- ----------------------------
DROP TABLE IF EXISTS `sys_rate_limit`;
CREATE TABLE `sys_rate_limit`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `limit_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '限流Key(如 PLATFORM:AMAZON:SHOP:xxx)',
  `window_seconds` int NOT NULL COMMENT '窗口秒数',
  `max_requests` int NOT NULL COMMENT '最大请求数',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ENABLED/DISABLED',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rate_limit`(`tenant_id` ASC, `limit_key` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'API限流策略(配置)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_rate_limit
-- ----------------------------

-- ----------------------------
-- Table structure for sys_sequence
-- ----------------------------
DROP TABLE IF EXISTS `sys_sequence`;
CREATE TABLE `sys_sequence`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `seq_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '号段编码(如 UNIFIED_ORDER_NO)',
  `prefix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前缀(可选)',
  `date_pattern` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日期模式(yyyyMMdd等,可选)',
  `current_value` bigint NOT NULL DEFAULT 0 COMMENT '当前值',
  `step` int NOT NULL DEFAULT 1 COMMENT '步长',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ENABLED/DISABLED',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_seq_code`(`tenant_id` ASC, `seq_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '业务单号生成器(号段)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_sequence
-- ----------------------------

-- ----------------------------
-- Table structure for tms_billing_invoice
-- ----------------------------
DROP TABLE IF EXISTS `tms_billing_invoice`;
CREATE TABLE `tms_billing_invoice`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `billing_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物流账单号',
  `carrier_id` bigint NOT NULL COMMENT '承运商ID',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `period_start` date NOT NULL COMMENT '开始日期',
  `period_end` date NOT NULL COMMENT '结束日期',
  `total_amount` decimal(18, 2) NOT NULL COMMENT '账单总额(结算价)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/IMPORTING/RECONCILING/FINISHED/CLOSED',
  `import_batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '导入批次',
  `import_batch_id` bigint NULL DEFAULT NULL COMMENT '导入批次ID(fin_import_batch.id)',
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账单文件URL',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `meta_json` json NULL COMMENT '扩展',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_billing_no`(`tenant_id` ASC, `billing_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_billing_carrier_period`(`tenant_id` ASC, `carrier_id` ASC, `period_start` ASC, `period_end` ASC) USING BTREE,
  INDEX `idx_billing_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物流账单(头，结算价口径)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_billing_invoice
-- ----------------------------

-- ----------------------------
-- Table structure for tms_billing_invoice_line
-- ----------------------------
DROP TABLE IF EXISTS `tms_billing_invoice_line`;
CREATE TABLE `tms_billing_invoice_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `billing_id` bigint NOT NULL COMMENT '物流账单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `tracking_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '运单号(外部)',
  `shipment_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内部运单号',
  `shipment_id` bigint NULL DEFAULT NULL COMMENT '运单ID(可空)',
  `fee_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '费用项编码(fin_fee_code)',
  `amount` decimal(18, 2) NOT NULL COMMENT '结算金额',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `weight_g` int NULL DEFAULT NULL COMMENT '计费重量(g)',
  `zone_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分区(结算)',
  `occurred_at` datetime(3) NULL DEFAULT NULL COMMENT '发生时间',
  `raw_json` json NULL COMMENT '原始导入行',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_billing_line`(`tenant_id` ASC, `billing_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_billing_line_tracking`(`tenant_id` ASC, `tracking_no` ASC) USING BTREE,
  INDEX `idx_billing_line_shipment`(`tenant_id` ASC, `shipment_id` ASC) USING BTREE,
  INDEX `idx_billing_line_fee`(`tenant_id` ASC, `fee_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物流账单行(可与运单/报价对账)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_billing_invoice_line
-- ----------------------------

-- ----------------------------
-- Table structure for tms_carrier
-- ----------------------------
DROP TABLE IF EXISTS `tms_carrier`;
CREATE TABLE `tms_carrier`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `carrier_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '承运商编码',
  `carrier_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '承运商名称',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `config_json` json NULL COMMENT '接口配置(可选)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_carrier_code`(`tenant_id` ASC, `carrier_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '承运商' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_carrier
-- ----------------------------

-- ----------------------------
-- Table structure for tms_cost_estimate
-- ----------------------------
DROP TABLE IF EXISTS `tms_cost_estimate`;
CREATE TABLE `tms_cost_estimate`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `shipment_id` bigint NOT NULL COMMENT '运单ID(tms_shipment.id)',
  `quote_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报价单号(tms_quote_cache.quote_no)',
  `estimated_base_amount` decimal(18, 2) NOT NULL COMMENT '预估基础运费',
  `estimated_surcharge_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '预估附加费',
  `estimated_duty_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '预估关税',
  `estimated_tax_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '预估税费',
  `estimated_total_amount` decimal(18, 2) NOT NULL COMMENT '预估总额',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ESTIMATED/LOCKED/REVISED',
  `meta_json` json NULL COMMENT '扩展(计算证据/规则版本)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ship_est`(`tenant_id` ASC, `shipment_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ship_est_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '运单成本预估(下单/打单时锁定口径)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_cost_estimate
-- ----------------------------

-- ----------------------------
-- Table structure for tms_cost_recon
-- ----------------------------
DROP TABLE IF EXISTS `tms_cost_recon`;
CREATE TABLE `tms_cost_recon`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `billing_id` bigint NOT NULL COMMENT '物流账单ID',
  `shipment_id` bigint NOT NULL COMMENT '运单ID',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `estimated_total_amount` decimal(18, 2) NOT NULL COMMENT '预估总额',
  `billed_total_amount` decimal(18, 2) NOT NULL COMMENT '结算总额',
  `diff_amount` decimal(18, 2) NOT NULL COMMENT '差异(结算-预估)',
  `match_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'MATCHED/MISMATCHED/MISSING_EST/MISSING_BILL',
  `resolve_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OPEN/RESOLVED/CLOSED',
  `resolve_note` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理说明',
  `meta_json` json NULL COMMENT '扩展(匹配规则/证据/费用拆分)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_cost_recon`(`tenant_id` ASC, `billing_id` ASC, `shipment_id` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_cost_recon_match`(`tenant_id` ASC, `match_status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_cost_recon_resolve`(`tenant_id` ASC, `resolve_status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物流成本对账结果(预估价 vs 结算价)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_cost_recon
-- ----------------------------

-- ----------------------------
-- Table structure for tms_duty_tax_rule
-- ----------------------------
DROP TABLE IF EXISTS `tms_duty_tax_rule`;
CREATE TABLE `tms_duty_tax_rule`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `rule_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则编码',
  `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则名称',
  `country_code` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '国家',
  `hs_code_prefix` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HS编码前缀(可选)',
  `duty_percent` decimal(10, 6) NOT NULL DEFAULT 0.000000 COMMENT '关税比例',
  `tax_percent` decimal(10, 6) NOT NULL DEFAULT 0.000000 COMMENT '税费比例',
  `min_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '最低税费(可选)',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '币种',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `meta_json` json NULL COMMENT '扩展',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_duty_rule`(`tenant_id` ASC, `rule_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_duty_country`(`tenant_id` ASC, `country_code` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '关税/税费规则(估算)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_duty_tax_rule
-- ----------------------------

-- ----------------------------
-- Table structure for tms_logistics_method
-- ----------------------------
DROP TABLE IF EXISTS `tms_logistics_method`;
CREATE TABLE `tms_logistics_method`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `method_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物流方式编码',
  `method_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物流方式名称',
  `carrier_id` bigint NOT NULL COMMENT '承运商ID',
  `service_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务等级(经济/标准/加急)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `rules_json` json NULL COMMENT '可达国家/计费规则等',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_method_code`(`tenant_id` ASC, `method_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_method_carrier`(`tenant_id` ASC, `carrier_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物流方式' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_logistics_method
-- ----------------------------

-- ----------------------------
-- Table structure for tms_platform_ack
-- ----------------------------
DROP TABLE IF EXISTS `tms_platform_ack`;
CREATE TABLE `tms_platform_ack`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `platform_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台编码',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `platform_order_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台订单ID',
  `action_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作(SHIP_CONFIRM/LABEL_UPLOAD/TRACKING_UPDATE)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'PENDING/SUCCESS/FAILED',
  `attempts` int NOT NULL DEFAULT 0 COMMENT '尝试次数',
  `last_error` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后错误',
  `payload_json` json NULL COMMENT '回传载荷',
  `response_json` json NULL COMMENT '平台响应',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ack`(`tenant_id` ASC, `platform_code` ASC, `shop_id` ASC, `platform_order_id` ASC, `action_type` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ack_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台回传记录(幂等/可重试)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_platform_ack
-- ----------------------------

-- ----------------------------
-- Table structure for tms_quote_cache
-- ----------------------------
DROP TABLE IF EXISTS `tms_quote_cache`;
CREATE TABLE `tms_quote_cache`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `quote_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报价单号(内部)',
  `carrier_id` bigint NOT NULL COMMENT '承运商ID',
  `method_id` bigint NULL DEFAULT NULL COMMENT '物流方式ID',
  `ship_from_country` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发货国',
  `ship_to_country` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货国',
  `ship_to_zip` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货邮编',
  `weight_g` int NOT NULL COMMENT '重量(g)',
  `length_mm` int NULL DEFAULT NULL COMMENT '长(mm)',
  `width_mm` int NULL DEFAULT NULL COMMENT '宽(mm)',
  `height_mm` int NULL DEFAULT NULL COMMENT '高(mm)',
  `zone_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分区(计算后)',
  `base_amount` decimal(18, 2) NOT NULL COMMENT '基础运费',
  `surcharge_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '附加费合计',
  `duty_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '关税(估算)',
  `tax_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '税费(估算)',
  `total_amount` decimal(18, 2) NOT NULL COMMENT '总报价',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `expires_at` datetime(3) NULL DEFAULT NULL COMMENT '缓存过期',
  `meta_json` json NULL COMMENT '扩展',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_quote_no`(`tenant_id` ASC, `quote_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_quote_key`(`tenant_id` ASC, `carrier_id` ASC, `method_id` ASC, `ship_to_country` ASC, `weight_g` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_quote_expire`(`tenant_id` ASC, `expires_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '运费报价缓存(试算/下单前)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_quote_cache
-- ----------------------------

-- ----------------------------
-- Table structure for tms_rate_card
-- ----------------------------
DROP TABLE IF EXISTS `tms_rate_card`;
CREATE TABLE `tms_rate_card`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `rate_card_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报价卡编码',
  `rate_card_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报价卡名称',
  `carrier_id` bigint NOT NULL COMMENT '承运商ID',
  `method_id` bigint NULL DEFAULT NULL COMMENT '物流方式ID(可空)',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '币种',
  `effective_from` date NOT NULL COMMENT '生效日期',
  `effective_to` date NULL DEFAULT NULL COMMENT '失效日期',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `meta_json` json NULL COMMENT '扩展(燃油/计费规则版本等)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rate_card_code`(`tenant_id` ASC, `rate_card_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_rate_card_carrier`(`tenant_id` ASC, `carrier_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_rate_card_effective`(`tenant_id` ASC, `effective_from` ASC, `effective_to` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '运费报价卡(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_rate_card
-- ----------------------------

-- ----------------------------
-- Table structure for tms_rate_card_surcharge
-- ----------------------------
DROP TABLE IF EXISTS `tms_rate_card_surcharge`;
CREATE TABLE `tms_rate_card_surcharge`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `rate_card_id` bigint NOT NULL COMMENT '报价卡ID',
  `surcharge_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '附加费编码',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_card_surcharge`(`tenant_id` ASC, `rate_card_id` ASC, `surcharge_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_card_surcharge_card`(`tenant_id` ASC, `rate_card_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报价卡-附加费绑定' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_rate_card_surcharge
-- ----------------------------

-- ----------------------------
-- Table structure for tms_rate_rule
-- ----------------------------
DROP TABLE IF EXISTS `tms_rate_rule`;
CREATE TABLE `tms_rate_rule`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `rate_card_id` bigint NOT NULL COMMENT '报价卡ID',
  `zone_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区编码',
  `weight_from_g` int NOT NULL COMMENT '重量起(g)',
  `weight_to_g` int NOT NULL COMMENT '重量止(g)',
  `price` decimal(18, 2) NOT NULL COMMENT '价格',
  `billing_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计费类型(BASE/STEP/FLAT)',
  `step_g` int NULL DEFAULT NULL COMMENT '阶梯步长(g,可选)',
  `extra_json` json NULL COMMENT '扩展(体积重/首重续重等)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rate_rule`(`tenant_id` ASC, `rate_card_id` ASC, `zone_code` ASC, `weight_from_g` ASC, `weight_to_g` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_rate_rule_card`(`tenant_id` ASC, `rate_card_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '运费报价规则(Zone+重量区间)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_rate_rule
-- ----------------------------

-- ----------------------------
-- Table structure for tms_shipment
-- ----------------------------
DROP TABLE IF EXISTS `tms_shipment`;
CREATE TABLE `tms_shipment`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `shipment_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发货单号/运单号(内部)',
  `fulfillment_id` bigint NULL DEFAULT NULL COMMENT '履约单ID(可空)',
  `outbound_id` bigint NULL DEFAULT NULL COMMENT '出库单ID(可空)',
  `carrier_id` bigint NOT NULL COMMENT '承运商ID',
  `method_id` bigint NULL DEFAULT NULL COMMENT '物流方式ID',
  `tracking_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '运单号',
  `label_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '面单URL',
  `label_format` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '面单格式(PDF/ZPL/PNG)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/LABELED/SHIPPED/DELIVERED/EXCEPTION',
  `shipped_at` datetime(3) NULL DEFAULT NULL COMMENT '发货时间',
  `delivered_at` datetime(3) NULL DEFAULT NULL COMMENT '签收时间',
  `cost_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '运费成本(可选)',
  `estimated_cost_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '预估运费成本(锁定口径)',
  `billed_cost_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '结算运费成本(账单口径)',
  `cost_currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '成本币种',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '币种',
  `ship_to_address_json` json NULL COMMENT '收货地址快照',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shipment_no`(`tenant_id` ASC, `shipment_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_tracking`(`tenant_id` ASC, `tracking_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ship_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_ship_ff`(`tenant_id` ASC, `fulfillment_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '发货单/运单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_shipment
-- ----------------------------

-- ----------------------------
-- Table structure for tms_shipment_package
-- ----------------------------
DROP TABLE IF EXISTS `tms_shipment_package`;
CREATE TABLE `tms_shipment_package`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `shipment_id` bigint NOT NULL COMMENT '发货单ID',
  `package_no` int NOT NULL COMMENT '包裹序号',
  `weight_g` int NULL DEFAULT NULL COMMENT '重量(g)',
  `length_mm` int NULL DEFAULT NULL COMMENT '长(mm)',
  `width_mm` int NULL DEFAULT NULL COMMENT '宽(mm)',
  `height_mm` int NULL DEFAULT NULL COMMENT '高(mm)',
  `content_json` json NULL COMMENT '包裹内容(可选)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ship_pkg`(`tenant_id` ASC, `shipment_id` ASC, `package_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ship_pkg_ship`(`tenant_id` ASC, `shipment_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '包裹' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_shipment_package
-- ----------------------------

-- ----------------------------
-- Table structure for tms_surcharge
-- ----------------------------
DROP TABLE IF EXISTS `tms_surcharge`;
CREATE TABLE `tms_surcharge`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `surcharge_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '附加费编码(FUEL/REMOTE/PEAK/DUTY...)',
  `surcharge_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '附加费名称',
  `calc_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计算方式(FIXED/PERCENT/BY_WEIGHT/BY_PIECE)',
  `calc_value` decimal(18, 6) NOT NULL COMMENT '计算值',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '币种(固定费用时)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  `meta_json` json NULL COMMENT '扩展(适用条件)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_surcharge_code`(`tenant_id` ASC, `surcharge_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '附加费定义' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_surcharge
-- ----------------------------

-- ----------------------------
-- Table structure for tms_tracking_event
-- ----------------------------
DROP TABLE IF EXISTS `tms_tracking_event`;
CREATE TABLE `tms_tracking_event`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `shipment_id` bigint NOT NULL COMMENT '发货单ID',
  `event_time` datetime(3) NOT NULL COMMENT '事件时间',
  `location` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地点',
  `status` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点状态(原文/归一化)',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `raw_json` json NULL COMMENT '原始事件',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_track_ship_time`(`tenant_id` ASC, `shipment_id` ASC, `event_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物流轨迹事件' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_tracking_event
-- ----------------------------

-- ----------------------------
-- Table structure for tms_zone
-- ----------------------------
DROP TABLE IF EXISTS `tms_zone`;
CREATE TABLE `tms_zone`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `zone_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区编码',
  `zone_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区名称',
  `country_code` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国家(可空，表示多国)',
  `state_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '州/省(可空)',
  `zip_prefix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮编前缀(可空)',
  `rule_json` json NULL COMMENT '更复杂的分区规则',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_zone_code`(`tenant_id` ASC, `zone_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_zone_country`(`tenant_id` ASC, `country_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物流分区(Zone)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tms_zone
-- ----------------------------

-- ----------------------------
-- Table structure for wms_area
-- ----------------------------
DROP TABLE IF EXISTS `wms_area`;
CREATE TABLE `wms_area`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `area_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库区编码',
  `area_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库区名称',
  `area_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'STORAGE/PICKING/RECEIVING/SHIPPING',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_area`(`tenant_id` ASC, `warehouse_id` ASC, `area_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_area_wh`(`tenant_id` ASC, `warehouse_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库区' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_area
-- ----------------------------
INSERT INTO `wms_area` VALUES (8002, 1, 1, '2026-02-12 08:10:00.000', '2026-02-12 08:10:00.000', 0, 0, 0, 0, 'seed', 8001, 'A-01', 'Pick Area', 'PICK', 'ENABLED');

-- ----------------------------
-- Table structure for wms_inbound_receipt
-- ----------------------------
DROP TABLE IF EXISTS `wms_inbound_receipt`;
CREATE TABLE `wms_inbound_receipt`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `inbound_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '入库单号',
  `inbound_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'PURCHASE/RETURN/PRODUCTION',
  `source_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源单号(采购单/工单等)',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/RECEIVING/RECEIVED/CLOSED',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inbound_no`(`tenant_id` ASC, `inbound_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_inbound_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_inbound_wh`(`tenant_id` ASC, `warehouse_id` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '入库单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_inbound_receipt
-- ----------------------------

-- ----------------------------
-- Table structure for wms_inbound_receipt_line
-- ----------------------------
DROP TABLE IF EXISTS `wms_inbound_receipt_line`;
CREATE TABLE `wms_inbound_receipt_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `inbound_id` bigint NOT NULL COMMENT '入库单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NOT NULL COMMENT 'SKU/物料ID(统一用sku_id承载物料)',
  `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码冗余',
  `qty_expected` int NOT NULL COMMENT '应收数量',
  `qty_received` int NOT NULL DEFAULT 0 COMMENT '实收数量',
  `location_id` bigint NULL DEFAULT NULL COMMENT '上架库位(可空)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inbound_line`(`tenant_id` ASC, `inbound_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_inbound_line_inbound`(`tenant_id` ASC, `inbound_id` ASC) USING BTREE,
  INDEX `idx_inbound_line_sku`(`tenant_id` ASC, `sku_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '入库单行' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_inbound_receipt_line
-- ----------------------------

-- ----------------------------
-- Table structure for wms_location
-- ----------------------------
DROP TABLE IF EXISTS `wms_location`;
CREATE TABLE `wms_location`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `area_id` bigint NULL DEFAULT NULL COMMENT '库区ID',
  `location_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库位编码',
  `location_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'BIN/SHELF/PALLET/VIRTUAL',
  `capacity_json` json NULL COMMENT '容量/限制',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_location_code`(`tenant_id` ASC, `warehouse_id` ASC, `location_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_loc_wh`(`tenant_id` ASC, `warehouse_id` ASC) USING BTREE,
  INDEX `idx_loc_area`(`tenant_id` ASC, `area_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库位' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_location
-- ----------------------------
INSERT INTO `wms_location` VALUES (8003, 1, 1, '2026-02-12 08:10:00.000', '2026-02-12 08:10:00.000', 0, 0, 0, 0, 'seed', 8001, 8002, 'L-01-01', 'SHELF', '{\"maxQty\": 1000}', 'ENABLED');

-- ----------------------------
-- Table structure for wms_outbound_order
-- ----------------------------
DROP TABLE IF EXISTS `wms_outbound_order`;
CREATE TABLE `wms_outbound_order`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `outbound_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出库单号',
  `outbound_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'FULFILLMENT/TRANSFER/RETURN',
  `source_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源单号(履约单等)',
  `fulfillment_id` bigint NULL DEFAULT NULL COMMENT '履约单ID(可空)',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `ship_to_address_json` json NULL COMMENT '收货地址快照(用于仓内作业)',
  `pack_strategy` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '包装策略(AUTO/MANUAL)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/ALLOCATED/PICKING/PACKED/SHIPPED/CANCELLED',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_outbound_no`(`tenant_id` ASC, `outbound_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_outbound_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_outbound_ff`(`tenant_id` ASC, `fulfillment_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '出库单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_outbound_order
-- ----------------------------

-- ----------------------------
-- Table structure for wms_outbound_order_line
-- ----------------------------
DROP TABLE IF EXISTS `wms_outbound_order_line`;
CREATE TABLE `wms_outbound_order_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `outbound_id` bigint NOT NULL COMMENT '出库单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NOT NULL COMMENT 'SKU/物料ID',
  `qty` int NOT NULL COMMENT '数量',
  `qty_picked` int NOT NULL DEFAULT 0 COMMENT '已拣数量',
  `qty_shipped` int NOT NULL DEFAULT 0 COMMENT '已发数量',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_outbound_line`(`tenant_id` ASC, `outbound_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_outbound_line_outbound`(`tenant_id` ASC, `outbound_id` ASC) USING BTREE,
  INDEX `idx_outbound_line_sku`(`tenant_id` ASC, `sku_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '出库单行' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_outbound_order_line
-- ----------------------------

-- ----------------------------
-- Table structure for wms_pack_order
-- ----------------------------
DROP TABLE IF EXISTS `wms_pack_order`;
CREATE TABLE `wms_pack_order`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `pack_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '包装单号',
  `outbound_id` bigint NOT NULL COMMENT '出库单ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/PACKING/PACKED/CANCELLED',
  `package_count` int NOT NULL DEFAULT 1 COMMENT '包裹数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pack_no`(`tenant_id` ASC, `pack_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_pack_outbound`(`tenant_id` ASC, `outbound_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '包装单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_pack_order
-- ----------------------------

-- ----------------------------
-- Table structure for wms_pack_order_line
-- ----------------------------
DROP TABLE IF EXISTS `wms_pack_order_line`;
CREATE TABLE `wms_pack_order_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `pack_id` bigint NOT NULL COMMENT '包装单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NOT NULL COMMENT 'SKU/物料ID',
  `qty` int NOT NULL COMMENT '数量',
  `package_no` int NOT NULL DEFAULT 1 COMMENT '第几个包裹',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pack_line`(`tenant_id` ASC, `pack_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_pack_line_pack`(`tenant_id` ASC, `pack_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '包装单行' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_pack_order_line
-- ----------------------------

-- ----------------------------
-- Table structure for wms_pick_task
-- ----------------------------
DROP TABLE IF EXISTS `wms_pick_task`;
CREATE TABLE `wms_pick_task`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `pick_task_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '拣货任务号',
  `wave_id` bigint NOT NULL COMMENT '波次ID',
  `outbound_id` bigint NOT NULL COMMENT '出库单ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/PICKING/DONE/CANCELLED',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pick_task_no`(`tenant_id` ASC, `pick_task_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_pick_task_wave`(`tenant_id` ASC, `wave_id` ASC) USING BTREE,
  INDEX `idx_pick_task_outbound`(`tenant_id` ASC, `outbound_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拣货任务' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_pick_task
-- ----------------------------

-- ----------------------------
-- Table structure for wms_pick_task_line
-- ----------------------------
DROP TABLE IF EXISTS `wms_pick_task_line`;
CREATE TABLE `wms_pick_task_line`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `pick_task_id` bigint NOT NULL COMMENT '拣货任务ID',
  `outbound_order_id` bigint NOT NULL COMMENT '出库单ID',
  `outbound_line_id` bigint NOT NULL COMMENT '出库单行ID',
  `line_no` int NOT NULL COMMENT '拣货任务行号（任务内序号）',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `qty` int NOT NULL COMMENT '应拣数量',
  `qty_actual` int NOT NULL DEFAULT 0 COMMENT '实拣数量',
  `from_location_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拣货库位（占位，可空）',
  `lot_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次号（占位，可空）',
  `serial_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '序列号（占位，可空）',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态：PENDING/PICKING/DONE/CANCELLED',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0=否,1=是)',
  `version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人',
  `updated_by` bigint NOT NULL DEFAULT 0 COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '链路追踪ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pick_task_line_no`(`tenant_id` ASC, `factory_id` ASC, `pick_task_id` ASC, `line_no` ASC, `deleted` ASC) USING BTREE,
  UNIQUE INDEX `uk_pick_task_outbound_line`(`tenant_id` ASC, `factory_id` ASC, `pick_task_id` ASC, `outbound_line_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_pick_task`(`tenant_id` ASC, `factory_id` ASC, `pick_task_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_outbound`(`tenant_id` ASC, `factory_id` ASC, `outbound_order_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_sku`(`tenant_id` ASC, `factory_id` ASC, `sku_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拣货任务行（按出库行实例化）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_pick_task_line
-- ----------------------------

-- ----------------------------
-- Table structure for wms_pick_wave
-- ----------------------------
DROP TABLE IF EXISTS `wms_pick_wave`;
CREATE TABLE `wms_pick_wave`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `wave_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '波次号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/PICKING/DONE/CANCELLED',
  `strategy_json` json NULL COMMENT '波次策略',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wave_no`(`tenant_id` ASC, `wave_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_wave_wh_status`(`tenant_id` ASC, `warehouse_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拣货波次' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_pick_wave
-- ----------------------------

-- ----------------------------
-- Table structure for wms_putaway_task
-- ----------------------------
DROP TABLE IF EXISTS `wms_putaway_task`;
CREATE TABLE `wms_putaway_task`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `putaway_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '上架任务号',
  `inbound_id` bigint NOT NULL COMMENT '入库单ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/PROCESSING/DONE/CANCELLED',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_putaway_no`(`tenant_id` ASC, `putaway_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_putaway_inbound`(`tenant_id` ASC, `inbound_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '上架任务' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_putaway_task
-- ----------------------------

-- ----------------------------
-- Table structure for wms_putaway_task_line
-- ----------------------------
DROP TABLE IF EXISTS `wms_putaway_task_line`;
CREATE TABLE `wms_putaway_task_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `putaway_id` bigint NOT NULL COMMENT '上架任务ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NOT NULL COMMENT 'SKU/物料ID',
  `qty` int NOT NULL COMMENT '数量',
  `from_location_id` bigint NULL DEFAULT NULL COMMENT '来源库位(可空)',
  `to_location_id` bigint NOT NULL COMMENT '目标库位',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_putaway_line`(`tenant_id` ASC, `putaway_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_putaway_line_putaway`(`tenant_id` ASC, `putaway_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '上架任务行' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_putaway_task_line
-- ----------------------------

-- ----------------------------
-- Table structure for wms_stocktake_order
-- ----------------------------
DROP TABLE IF EXISTS `wms_stocktake_order`;
CREATE TABLE `wms_stocktake_order`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `stocktake_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '盘点单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/COUNTING/DIFF_POSTED/CLOSED/CANCELLED',
  `scope_json` json NULL COMMENT '盘点范围(库区/库位/品类等)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stocktake_no`(`tenant_id` ASC, `stocktake_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_stocktake_wh_status`(`tenant_id` ASC, `warehouse_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '盘点单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_stocktake_order
-- ----------------------------

-- ----------------------------
-- Table structure for wms_stocktake_order_line
-- ----------------------------
DROP TABLE IF EXISTS `wms_stocktake_order_line`;
CREATE TABLE `wms_stocktake_order_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `stocktake_id` bigint NOT NULL COMMENT '盘点单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU/物料ID',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次号',
  `system_qty` int NOT NULL COMMENT '系统数量',
  `counted_qty` int NULL DEFAULT NULL COMMENT '盘点数量(录入)',
  `diff_qty` int NULL DEFAULT NULL COMMENT '差异数量(counted-system)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stocktake_line`(`tenant_id` ASC, `stocktake_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_stocktake_line_stocktake`(`tenant_id` ASC, `stocktake_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '盘点单行' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_stocktake_order_line
-- ----------------------------

-- ----------------------------
-- Table structure for wms_transfer_order
-- ----------------------------
DROP TABLE IF EXISTS `wms_transfer_order`;
CREATE TABLE `wms_transfer_order`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `transfer_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调拨单号',
  `from_warehouse_id` bigint NOT NULL COMMENT '来源仓库ID',
  `to_warehouse_id` bigint NOT NULL COMMENT '目标仓库ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CREATED/OUTBOUND/INBOUND/DONE/CANCELLED',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_transfer_no`(`tenant_id` ASC, `transfer_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_transfer_status`(`tenant_id` ASC, `status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库调拨单(头)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_transfer_order
-- ----------------------------

-- ----------------------------
-- Table structure for wms_transfer_order_line
-- ----------------------------
DROP TABLE IF EXISTS `wms_transfer_order_line`;
CREATE TABLE `wms_transfer_order_line`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `transfer_id` bigint NOT NULL COMMENT '调拨单ID',
  `line_no` int NOT NULL COMMENT '行号',
  `sku_id` bigint NOT NULL COMMENT 'SKU/物料ID',
  `qty` int NOT NULL COMMENT '数量',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_transfer_line`(`tenant_id` ASC, `transfer_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_transfer_line_transfer`(`tenant_id` ASC, `transfer_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库调拨单(行)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_transfer_order_line
-- ----------------------------

-- ----------------------------
-- Table structure for wms_warehouse
-- ----------------------------
DROP TABLE IF EXISTS `wms_warehouse`;
CREATE TABLE `wms_warehouse`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花/号段)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `created_at` datetime(3) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除(0否1是)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
  `warehouse_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库名称',
  `warehouse_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'RAW/FINISHED/3PL',
  `country_code` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国家',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ACTIVE/INACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_warehouse`(`tenant_id` ASC, `warehouse_code` ASC) USING BTREE,
  INDEX `idx_tenant_factory_created`(`tenant_id` ASC, `factory_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_updated`(`tenant_id` ASC, `factory_id` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_tenant_factory_deleted`(`tenant_id` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_wh_type`(`tenant_id` ASC, `warehouse_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wms_warehouse
-- ----------------------------
INSERT INTO `wms_warehouse` VALUES (8001, 1, 1, '2026-02-12 08:10:00.000', '2026-02-12 08:10:00.000', 0, 0, 0, 0, 'seed', 'WH-LA-01', 'LA Warehouse', 'OWN', 'US', 'Los Angeles', 'ENABLED');

SET FOREIGN_KEY_CHECKS = 1;
