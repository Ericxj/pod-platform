package com.pod.infra.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensure this runs very first to set up MDC
public class RequestIdFilter implements Filter {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String requestId = req.getHeader(HEADER_REQUEST_ID);
        final boolean clientSentRequestId = (requestId != null && !requestId.isEmpty() && !requestId.isBlank());

        if (clientSentRequestId) {
            MDC.put("requestId", requestId);
        } else {
            requestId = UUID.randomUUID().toString();
        }

        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).setHeader(HEADER_REQUEST_ID, requestId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            if (clientSentRequestId) {
                MDC.remove("requestId");
            }
        }
    }
}
