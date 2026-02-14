package com.pod.infra.filter;

import com.pod.common.core.context.TenantContext;
import com.pod.common.utils.TraceIdUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class TraceIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String traceId = req.getHeader(TraceIdUtils.TRACE_ID);
        if (traceId == null || traceId.isEmpty()) {
            traceId = TraceIdUtils.generateTraceId();
        }
        TraceIdUtils.setTraceId(traceId);

        String tenantId = req.getHeader("X-Tenant-Id");
        if (tenantId != null) {
            try {
                TenantContext.setTenantId(Long.parseLong(tenantId));
            } catch (NumberFormatException ignored) {}
        }
        
        String factoryId = req.getHeader("X-Factory-Id");
        if (factoryId != null) {
            try {
                TenantContext.setFactoryId(Long.parseLong(factoryId));
            } catch (NumberFormatException ignored) {}
        }

        try {
            chain.doFilter(request, response);
            if (response instanceof HttpServletResponse) {
                ((HttpServletResponse) response).setHeader(TraceIdUtils.TRACE_ID, traceId);
            }
        } finally {
            TraceIdUtils.remove();
            TenantContext.clear();
        }
    }
}
