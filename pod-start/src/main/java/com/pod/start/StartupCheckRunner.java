package com.pod.start;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class StartupCheckRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupCheckRunner.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public StartupCheckRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info(">>> Starting Self-Check...");

        try (Connection connection = dataSource.getConnection()) {
            log.info("[PASS] Database Connection: {}", connection.getMetaData().getURL());
        } catch (Exception e) {
            log.error("[FAIL] Database Connection failed: {}", e.getMessage());
            throw e;
        }

        checkTables();
        checkCharset();

        log.info(">>> Self-Check Completed.");
    }

    private void checkTables() {
        List<String> requiredTables = Arrays.asList("iam_user", "iam_role", "iam_permission", "iam_data_scope");
        for (String table : requiredTables) {
            try {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT count(1) FROM information_schema.tables WHERE table_schema = (SELECT DATABASE()) AND table_name = ?",
                        Integer.class, table);
                if (count != null && count > 0) {
                    log.info("[PASS] Table exists: {}", table);
                    checkIndexes(table);
                } else {
                    log.error("[FAIL] Table missing: {}", table);
                    log.error("SUGGESTION: Run DDL to create table {}", table);
                    throw new RuntimeException("Missing critical table: " + table);
                }
            } catch (Exception e) {
                if (e instanceof RuntimeException) throw e;
                log.warn("[WARN] Could not check table {}: {}", table, e.getMessage());
            }
        }
    }

    private void checkIndexes(String table) {
        // Check for common indexes like tenant_id, factory_id if columns exist
        // This is a basic check.
        if ("iam_user".equals(table)) {
            checkIndexExists(table, "username");
        }
        checkIndexExists(table, "tenant_id");
    }

    private void checkIndexExists(String table, String column) {
        try {
            // Check if column exists first
            Integer colCount = jdbcTemplate.queryForObject(
                    "SELECT count(1) FROM information_schema.columns WHERE table_schema = (SELECT DATABASE()) AND table_name = ? AND column_name = ?",
                    Integer.class, table, column);
            if (colCount == null || colCount == 0) return;

            // Check index
            Integer idxCount = jdbcTemplate.queryForObject(
                    "SELECT count(1) FROM information_schema.statistics WHERE table_schema = (SELECT DATABASE()) AND table_name = ? AND column_name = ?",
                    Integer.class, table, column);
            
            if (idxCount != null && idxCount > 0) {
                log.info("[PASS] Index exists on {}.{}", table, column);
            } else {
                log.warn("[WARN] Missing index on {}.{} (Performance risk)", table, column);
                log.warn("SUGGESTION: CREATE INDEX idx_{}_{} ON {} ({});", table, column, table, column);
            }
        } catch (Exception e) {
            // Ignore H2 or other DB specific issues
        }
    }

    private void checkCharset() {
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                    "SELECT default_collation_name FROM information_schema.schemata WHERE schema_name = (SELECT DATABASE())");
            if (!result.isEmpty()) {
                String collation = (String) result.get(0).get("DEFAULT_COLLATION_NAME"); // H2 returns uppercase
                if (collation != null && !collation.toLowerCase().contains("utf8mb4")) {
                    log.warn("[WARN] Database collation is {}, expected utf8mb4_0900_ai_ci or similar.", collation);
                    log.warn("SUGGESTION: ALTER DATABASE <dbname> CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;");
                } else {
                    log.info("[PASS] Database collation: {}", collation);
                }
            }
        } catch (Exception e) {
            log.warn("[WARN] Could not check charset: {}", e.getMessage());
        }
    }
}
