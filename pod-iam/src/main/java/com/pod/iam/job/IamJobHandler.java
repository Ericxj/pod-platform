package com.pod.iam.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.common.core.context.TenantIgnoreContext;
import com.pod.common.utils.TraceIdUtils;
import com.pod.iam.application.MenuValidator;
import com.pod.iam.domain.IamPermission;
import com.pod.iam.dto.MenuValidationResultDto;
import com.pod.iam.mapper.IamPermissionMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class IamJobHandler {

    private static final Logger log = LoggerFactory.getLogger(IamJobHandler.class);

    private final MenuValidator menuValidator;
    private final IamPermissionMapper permissionMapper;

    public IamJobHandler(MenuValidator menuValidator, IamPermissionMapper permissionMapper) {
        this.menuValidator = menuValidator;
        this.permissionMapper = permissionMapper;
    }

    /**
     * Validate Menu Configuration Integrity for all tenants/factories.
     * Logs errors if found.
     */
    @XxlJob("iamMenuValidationJob")
    public void iamMenuValidationJob() {
        String traceId = TraceIdUtils.generateTraceId();
        TraceIdUtils.setTraceId(traceId);
        log.info("Job Start: iamMenuValidationJob");
        
        try {
            // Ignore Tenant Interceptor to scan all data
            TenantIgnoreContext.setIgnore(true);

            // 1. Find all distinct Tenant/Factory pairs
            // Note: Efficient way is using GROUP BY query.
            List<IamPermission> distinctPairs = permissionMapper.selectList(new LambdaQueryWrapper<IamPermission>()
                    .select(IamPermission::getTenantId, IamPermission::getFactoryId)
                    .eq(IamPermission::getDeleted, 0)
                    .groupBy(IamPermission::getTenantId, IamPermission::getFactoryId));

            if (CollUtil.isEmpty(distinctPairs)) {
                XxlJobHelper.handleSuccess("No data to validate");
                return;
            }

            int totalIssues = 0;
            StringBuilder report = new StringBuilder();

            for (IamPermission pair : distinctPairs) {
                Long tenantId = pair.getTenantId();
                Long factoryId = pair.getFactoryId();

                if (tenantId == null || factoryId == null) continue;

                try {
                    MenuValidationResultDto result = menuValidator.validateAll(tenantId, factoryId);
                    if (!result.isOk()) {
                        totalIssues += result.getErrors().size();
                        String msg = String.format("Issues found for Tenant %d / Factory %d: %s", 
                                tenantId, factoryId, JSONUtil.toJsonStr(result.getErrors()));
                        log.warn(msg);
                        report.append(msg).append("\n");
                    }
                } catch (Exception e) {
                    log.error("Validation failed for T:{} F:{}", tenantId, factoryId, e);
                }
            }

            if (totalIssues > 0) {
                XxlJobHelper.handleSuccess("Validation Completed with " + totalIssues + " issues. Check logs.");
                XxlJobHelper.log(report.toString());
            } else {
                XxlJobHelper.handleSuccess("Validation Passed. No issues found.");
            }

        } catch (Exception e) {
            log.error("Job Failed", e);
            XxlJobHelper.handleFail(e.getMessage());
        } finally {
            TenantIgnoreContext.clear();
            TraceIdUtils.remove();
            log.info("Job End: iamMenuValidationJob");
        }
    }
}
