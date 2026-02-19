package com.pod.start;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;

/**
 * 仅当 iam.seed.enabled=true 时执行（默认 false）。生产环境禁止开启。
 * 会执行 pod_os_ddl.sql 及 pod_os_reset_and_seed_v4.sql，清空并重写 iam_* 等表。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "iam.seed.enabled", havingValue = "true")
public class FullResetRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FullResetRunner.class);
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public FullResetRunner(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info(">>> STARTING FULL DATABASE RESET...");
        
        String baseDir = System.getProperty("user.dir");
        File dbDir = new File(baseDir, "db");
        if (!dbDir.isDirectory()) {
            dbDir = new File(baseDir, "pod-platform/db");
        }
        File mainScript = new File(dbDir, "pod_os_ddl.sql");
        File v4Script = new File(dbDir, "pod_os_reset_and_seed_v4.sql");
        
        if (!mainScript.exists()) {
            log.error(">>> Main script not found: " + mainScript.getAbsolutePath());
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            // Disable FK checks just in case, though user said no FKs
            try {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            } catch (Exception e) {
                log.warn("Could not disable foreign keys", e);
            }

            log.info(">>> Executing pod_os_ddl.sql (DDL + Original Data)...");
            ScriptUtils.executeSqlScript(conn, new FileSystemResource(mainScript));
            log.info(">>> pod_os_ddl.sql executed.");

            if (v4Script.exists()) {
                log.info(">>> Executing pod_os_reset_and_seed_v4.sql (Clean & Seed)...");
                ScriptUtils.executeSqlScript(conn, new FileSystemResource(v4Script));
                log.info(">>> pod_os_reset_and_seed_v4.sql executed.");
            } else {
                log.warn(">>> pod_os_reset_and_seed_v4.sql not found at {}, skipping (DDL may already contain full seed).", v4Script.getAbsolutePath());
            }

            // Verify fixes
            Integer permCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iam_permission WHERE deleted = 0", Integer.class);
            log.info(">>> Validation: iam_permission count = {}", permCount);
            
            // Check Data Scopes for Admin
            Integer scopeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iam_data_scope WHERE subject_type='ROLE' AND subject_id=1", Integer.class);
            log.info(">>> Validation: Admin Role Scopes = {}", scopeCount);

            Integer rolePermCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iam_role_permission WHERE role_id = 1", Integer.class);
            log.info(">>> Validation: iam_role_permission count for ADMIN (1) = {}", rolePermCount);

            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iam_user WHERE username = 'admin'", Integer.class);
            log.info(">>> Validation: Admin user count = {}", userCount);

            log.info(">>> DATABASE RESET COMPLETED.");
        } catch (Exception e) {
            log.error(">>> DATABASE RESET FAILED - CRITICAL ERROR", e);
            throw e; // Fail hard
        }
    }
}
