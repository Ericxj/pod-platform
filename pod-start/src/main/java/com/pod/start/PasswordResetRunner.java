package com.pod.start;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.context.TenantIgnoreContext;
import com.pod.iam.domain.IamUser;
import com.pod.iam.mapper.IamUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class PasswordResetRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetRunner.class);

    private final IamUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public PasswordResetRunner(IamUserMapper userMapper, PasswordEncoder passwordEncoder, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Password Reset Runner...");
        
        try {
            // Debug: Check all users
            List<Map<String, Object>> allUsers = jdbcTemplate.queryForList("SELECT id, username, deleted, tenant_id FROM iam_user");
            log.info("DEBUG: Found {} users in DB: {}", allUsers.size(), allUsers);

            TenantIgnoreContext.setIgnore(true);
            
            String newHash = passwordEncoder.encode("123456");

            // Reset Admin
            userMapper.update(null, new LambdaUpdateWrapper<IamUser>()
                    .eq(IamUser::getUsername, "admin")
                    .set(IamUser::getPasswordHash, newHash));
            log.info(">>> PasswordResetRunner: Admin password reset DONE.");

            // Reset Operator
            userMapper.update(null, new LambdaUpdateWrapper<IamUser>()
                    .eq(IamUser::getUsername, "operator")
                    .set(IamUser::getPasswordHash, newHash));
            log.info(">>> PasswordResetRunner: Operator password reset DONE.");

            // Verify Fixes (V3 Data: Role 1, User 1)
            Integer rolePermCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iam_role_permission WHERE role_id = 1", Integer.class);
            log.info(">>> VERIFY: Role 1 Permission Count = {}", rolePermCount);
            
            try {
                // Check Data Scope
                List<Map<String, Object>> scopes = jdbcTemplate.queryForList("SELECT * FROM iam_data_scope WHERE subject_type='ROLE' AND subject_id=1");
                log.info(">>> VERIFY: Role 1 Data Scopes: {}", scopes);
            } catch (Exception e) {
                 log.info(">>> VERIFY: Failed to check scopes: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("Password reset failed", e);
        } finally {
            TenantIgnoreContext.clear();
            TenantContext.clear();
        }
    }
}
