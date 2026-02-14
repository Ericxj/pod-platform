package com.pod.iam.controller;

import cn.hutool.core.util.StrUtil;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.domain.Result;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.application.MenuValidator;
import com.pod.iam.dto.MenuValidationResultDto;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iam/menus")
public class MenuValidationController {

    private final MenuValidator menuValidator;

    public MenuValidationController(MenuValidator menuValidator) {
        this.menuValidator = menuValidator;
    }

    @GetMapping("/validate")
    public Result<MenuValidationResultDto> validate(@RequestParam(required = false) Long factoryId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context missing");
        }

        // Determine Factory ID: Parameter > Context
        Long targetFactoryId = factoryId;
        if (targetFactoryId == null) {
            targetFactoryId = TenantContext.getFactoryId();
        }
        
        if (targetFactoryId == null) {
            throw new BusinessException("Factory ID required (via query param or header)");
        }

        MenuValidationResultDto validationResult = menuValidator.validateAll(tenantId, targetFactoryId);
        
        Result<MenuValidationResultDto> result = Result.success(validationResult);
        
        // Populate Trace ID
        String traceId = MDC.get("traceId");
        if (StrUtil.isBlank(traceId)) {
            traceId = MDC.get("requestId"); // Fallback
        }
        result.setTraceId(traceId);
        
        return result;
    }
}
