package com.pod.iam.filter;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to manage Trace ID for logging and request tracking.
 * Ensures every request has a trace_id in MDC and response header.
 */
@Component("iamTraceIdFilter")
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensure it runs before auth filter
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Request-Id"; // Standard header used by frontend
    private static final String MDC_TRACE_ID_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = request.getHeader(TRACE_ID_HEADER);

        // If missing, generate one
        if (StrUtil.isBlank(traceId)) {
            traceId = IdUtil.fastSimpleUUID();
        }

        try {
            // Put in MDC for logging
            MDC.put(MDC_TRACE_ID_KEY, traceId);
            
            // Add to response header
            response.setHeader(TRACE_ID_HEADER, traceId);
            
            // Also add standard 'trace_id' for some monitoring tools
            response.setHeader("trace_id", traceId);

            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC
            MDC.remove(MDC_TRACE_ID_KEY);
        }
    }
}
