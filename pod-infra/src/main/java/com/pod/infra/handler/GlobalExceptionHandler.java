package com.pod.infra.handler;

import com.pod.common.core.domain.Result;
import com.pod.common.core.exception.BusinessException;
import com.pod.common.utils.TraceIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException e) {
        log.error("Business Exception: {}", e.getMessage());
        Result<?> result = Result.error(e.getCode(), e.getMessage());
        result.setTraceId(TraceIdUtils.getTraceId());
        HttpStatus status = (e.getCode() != null && e.getCode() == 400) ? HttpStatus.BAD_REQUEST
                : (e.getCode() != null && e.getCode() == 401) ? HttpStatus.UNAUTHORIZED
                : (e.getCode() != null && e.getCode() == 403) ? HttpStatus.FORBIDDEN
                : (e.getCode() != null && e.getCode() == 404) ? HttpStatus.NOT_FOUND
                : (e.getCode() != null && e.getCode() == 409) ? HttpStatus.CONFLICT
                : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e) {
        log.error("System Exception", e);
        if (e instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.error(404, "Not Found: " + e.getMessage()));
        }
        Result<?> result = Result.error("System Error: " + e.getMessage());
        result.setTraceId(TraceIdUtils.getTraceId());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}
