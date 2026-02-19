# IAM 启动初始化禁用说明

## 目标

彻底移除/禁用“应用启动后自动初始化 IAM 数据库（菜单/权限/角色/数据权限/租户/工厂）”的代码与脚本。生产环境禁止任何 seed 写入。

---

## 一、发现的初始化点清单

| 文件路径 | 类型 | 行为 | 处理 |
|----------|------|------|------|
| `pod-start/.../FullResetRunner.java` | CommandLineRunner | 启动时执行 `pod_os_ddl.sql`、`pod_os_reset_and_seed_v4.sql`，清空并重写 iam_* 等表 | **已禁用**：`@ConditionalOnProperty(iam.seed.enabled=true)`，默认不生效 |
| `pod-start/.../PasswordResetRunner.java` | CommandLineRunner | 启动时将 admin/operator 密码重置为 123456（写 iam_user） | **已禁用**：同上 |
| `pod-iam/.../MenuBootstrapValidatorRunner.java` | ApplicationRunner | 启动时**仅读取** iam_permission 做菜单校验，不写库 | **已默认关闭**：`iam.menu.validate-on-startup=false`，且代码默认 `validateOnStartup=false` |
| `pod-start/.../StartupCheckRunner.java` | CommandLineRunner | 启动时**仅读取** information_schema 与表存在性，不写库 | **保留**（只读） |

**未发现：**

- `@PostConstruct` / `InitializingBean` / `ApplicationReadyEvent` / `ContextRefreshedEvent` 中写 iam_* 的逻辑
- `resources` 下被 Spring 自动执行的 `schema.sql` / `data.sql` / `import.sql` / `init*.sql`（主工程无此类文件）
- Flyway / Liquibase 迁移（项目未使用）

**说明：**

- `pod-iam/src/test/resources/schema.sql` 含 iam_* 的 CREATE 与 INSERT，仅**测试**使用，主应用启动不会加载，无需修改。
- `db/*.sql`（如 `pod_iam_reset_and_init.sql`）仅在被脚本或 FullResetRunner 显式执行时生效；FullResetRunner 已默认关闭，故启动不会执行。

---

## 二、配置开关

在 `pod-start/src/main/resources/application.yml` 中新增（默认关闭）：

```yaml
iam:
  seed:
    enabled: false   # 生产必须 false；仅 dev 需手工初始化时可临时 true
  menu:
    validate-on-startup: false   # 启动时不跑菜单校验
```

- **iam.seed.enabled**：为 `true` 时才会创建并执行 `FullResetRunner`、`PasswordResetRunner`。生产环境必须为 `false`。
- **iam.menu.validate-on-startup**：为 `true` 时启动会执行 `MenuBootstrapValidatorRunner`（仅读校验）。默认 `false`，不写库。

---

## 三、修改 diff 摘要

### 1. FullResetRunner.java

- 增加 `@ConditionalOnProperty(name = "iam.seed.enabled", havingValue = "true")`。
- 增加类注释：说明仅 iam.seed.enabled=true 时执行，生产禁止开启。

### 2. PasswordResetRunner.java

- 增加 `@ConditionalOnProperty(name = "iam.seed.enabled", havingValue = "true")`。
- 增加类注释：说明仅 iam.seed.enabled=true 时执行，生产禁止开启。

### 3. application.yml（pod-start）

- 新增 `iam.seed.enabled: false`、`iam.menu.validate-on-startup: false` 及注释。

### 4. IamMenuProperties.java（pod-iam）

- `validateOnStartup` 默认值由 `true` 改为 `false`，并补充注释。

---

## 四、验证步骤（重启后 iam_* 表数据不变）

1. **备份当前 iam_* 行数（或关键表 updated_at）**
   ```sql
   SELECT 'iam_permission' t, COUNT(*) c FROM iam_permission WHERE deleted=0
   UNION ALL SELECT 'iam_role', COUNT(*) FROM iam_role WHERE deleted=0
   UNION ALL SELECT 'iam_user', COUNT(*) FROM iam_user WHERE deleted=0
   UNION ALL SELECT 'iam_role_permission', COUNT(*) FROM iam_role_permission WHERE deleted=0
   UNION ALL SELECT 'iam_data_scope', COUNT(*) FROM iam_data_scope WHERE deleted=0
   UNION ALL SELECT 'iam_tenant', COUNT(*) FROM iam_tenant WHERE deleted=0
   UNION ALL SELECT 'iam_factory', COUNT(*) FROM iam_factory WHERE deleted=0;
   ```
   可选：记录 `iam_permission.updated_at` 的最大值等。

2. **确认未开启 seed**
   - 配置中**不**设置 `iam.seed.enabled`，或显式设置 `iam.seed.enabled: false`。

3. **重启应用**
   - 启动完成后查看日志：应**无** “STARTING FULL DATABASE RESET”、“Executing pod_os_ddl.sql”、“PasswordResetRunner: Admin password reset” 等。

4. **再次执行步骤 1 的 SQL**
   - 各表行数应与重启前一致；若有记录 `updated_at`，最大值不应被刷新。

5. **（可选）验证开关生效**
   - 设置 `iam.seed.enabled: true` 后重启，应看到 FullResetRunner/PasswordResetRunner 的日志并发生写库；验证后改回 `false` 并再次确认重启后不再写库。

---

## 五、生产部署检查清单

- [ ] 未配置 `iam.seed.enabled`，或 `iam.seed.enabled=false`
- [ ] 未在启动参数或环境变量中设置 `iam.seed.enabled=true`
- [ ] 重启后 iam_* 表行数及关键数据未变化
