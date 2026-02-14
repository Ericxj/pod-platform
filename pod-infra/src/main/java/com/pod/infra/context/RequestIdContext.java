package com.pod.infra.context;

import com.pod.common.core.exception.BusinessException;
import org.slf4j.MDC;

/**
 * Thread-local context for X-Request-Id. Read from MDC("requestId") set by {@link com.pod.infra.filter.RequestIdFilter}.
 * For write APIs, use getRequired() so that missing header results in HTTP 400.
 */
public final class RequestIdContext {

    public static final String MDC_KEY = "requestId";

    private RequestIdContext() {
    }

    /**
     * Get current request id if present (may be null when client did not send X-Request-Id).
     */
    public static String get() {
        String value = MDC.get(MDC_KEY);
        return (value != null && !value.isBlank()) ? value : null;
    }

    /**
     * Get current request id or throw BusinessException(400). Use in write APIs to enforce mandatory X-Request-Id.
     */
    public static String getRequired() {
        String value = get();
        if (value == null) {
            throw new BusinessException(400, "X-Request-Id is required for this operation");
        }
        return value;
    }
}
