package com.pod.iam.runner;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.application.MenuValidator;
import com.pod.iam.config.IamMenuProperties;
import com.pod.iam.domain.IamDataScope;
import com.pod.iam.domain.IamPermission;
import com.pod.iam.dto.MenuValidationResultDto;
import com.pod.iam.mapper.IamDataScopeMapper;
import com.pod.iam.mapper.IamPermissionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import com.pod.common.core.context.TenantContext;

@Component
public class MenuBootstrapValidatorRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MenuBootstrapValidatorRunner.class);

    private final IamMenuProperties properties;
    private final MenuValidator menuValidator;
    private final IamPermissionMapper permissionMapper;

    public MenuBootstrapValidatorRunner(IamMenuProperties properties, MenuValidator menuValidator, IamPermissionMapper permissionMapper) {
        this.properties = properties;
        this.menuValidator = menuValidator;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!properties.isValidateOnStartup()) {
            log.info(">>> IAM Menu Startup Validation is DISABLED.");
            return;
        }

        // Setup Trace ID for logs
        String traceId = "BOOT-" + UUID.randomUUID().toString().substring(0, 8);
        MDC.put("traceId", traceId);

        log.info(">>> IAM Menu Startup Validation STARTED. Fail-Fast: {}", properties.isFailFast());

        try {
            List<TenantFactoryPair> targets = determineValidationTargets();
            
            int failureCount = 0;
            List<String> errorSummaries = new ArrayList<>();

            for (TenantFactoryPair target : targets) {
                // Set Tenant Context for Validator (which calls MP queries)
                TenantContext.setTenantId(target.tenantId);
                TenantContext.setFactoryId(target.factoryId);
                
                try {
                    log.info("Checking Tenant: {}, Factory: {} ...", target.tenantId, target.factoryId);
                    
                    MenuValidationResultDto result = menuValidator.validateAll(target.tenantId, target.factoryId);
                    
                    if (!result.isOk()) {
                        failureCount++;
                        int errCount = result.getErrors().size();
                        log.error("[FAIL] Tenant: {}, Factory: {} has {} errors.", target.tenantId, target.factoryId, errCount);
                        
                        // Log first N errors
                        int limit = Math.min(errCount, 5);
                        for (int i = 0; i < limit; i++) {
                            MenuValidationResultDto.ValidationError err = result.getErrors().get(i);
                            String msg = String.format("  - [%s] %s (Path: %s, Comp: %s)", 
                                    err.getCode(), err.getMessage(), err.getPath(), err.getComponent());
                            log.error(msg);
                            if (errorSummaries.size() < 20) {
                                errorSummaries.add(msg);
                            }
                        }
                    } else {
                        log.info("[PASS] Tenant: {}, Factory: {}", target.tenantId, target.factoryId);
                    }
                } finally {
                    TenantContext.clear();
                }
            }

            if (failureCount > 0) {
                String msg = String.format("IAM Menu Validation FAILED for %d scopes. Please fix DB data or check /api/iam/menus/validate.", failureCount);
                log.error(msg);
                if (properties.isFailFast()) {
                    throw new IllegalStateException(msg + "\nErrors:\n" + String.join("\n", errorSummaries));
                }
            } else {
                log.info(">>> IAM Menu Startup Validation PASSED.");
            }

        } finally {
            MDC.remove("traceId");
        }
    }

    private List<TenantFactoryPair> determineValidationTargets() {
        List<TenantFactoryPair> targets = new ArrayList<>();

        // 1. Explicit configuration
        if (CollUtil.isNotEmpty(properties.getValidateTenants()) && CollUtil.isNotEmpty(properties.getValidateFactories())) {
            for (Long t : properties.getValidateTenants()) {
                for (Long f : properties.getValidateFactories()) {
                    targets.add(new TenantFactoryPair(t, f));
                }
            }
            return targets;
        }

        // 2. Scan DB (Find all distinct tenant_id, factory_id from iam_permission)
        // Use custom method with @InterceptorIgnore to avoid tenant filtering
        List<IamPermission> distinctPairs = permissionMapper.selectDistinctTenantFactory();
        
        if (CollUtil.isNotEmpty(distinctPairs)) {
            for (IamPermission p : distinctPairs) {
                if (p.getTenantId() != null && p.getFactoryId() != null) {
                     targets.add(new TenantFactoryPair(p.getTenantId(), p.getFactoryId()));
                }
            }
        }

        // 3. Fallback default
        if (targets.isEmpty()) {
            targets.add(new TenantFactoryPair(1L, 1L));
        }

        return targets;
    }

    private static class TenantFactoryPair {
        Long tenantId;
        Long factoryId;

        public TenantFactoryPair(Long tenantId, Long factoryId) {
            this.tenantId = tenantId;
            this.factoryId = factoryId;
        }
    }
}
