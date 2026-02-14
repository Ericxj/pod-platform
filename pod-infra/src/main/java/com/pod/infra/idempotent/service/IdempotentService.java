package com.pod.infra.idempotent.service;

import com.pod.common.core.exception.BusinessException;
import com.pod.infra.idempotent.domain.IdempotentRecord;
import com.pod.infra.idempotent.mapper.IdempotentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class IdempotentService {

    @Autowired
    private IdempotentMapper idempotentMapper;

    /**
     * Tries to lock the request ID.
     * Must be called within a transaction.
     * @param requestId The X-Request-Id
     * @param value Metadata (e.g., operation name)
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void lock(String requestId, String value) {
        if (requestId == null || requestId.isBlank()) {
            throw new BusinessException(400, "X-Request-Id is required for idempotency");
        }

        // Use composite key to allow multiple idempotent steps in one request (e.g. Service A calls Service B)
        // Ensure key length does not exceed varchar(128)
        String compositeKey = requestId + "::" + value;
        if (compositeKey.length() > 128) {
             // Fallback to hash if too long, or truncate
             compositeKey = requestId + "::" + value.hashCode();
        }
        
        IdempotentRecord record = new IdempotentRecord();
        record.setKeyId(compositeKey);
        record.setRecordValue(value);
        record.setExpireAt(LocalDateTime.now().plusHours(24)); // Default 24h retention
        
        try {
            idempotentMapper.insert(record);
        } catch (DuplicateKeyException e) {
            // Throw specific exception for duplicate request
            throw new BusinessException(409, "Duplicate request: " + requestId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public <T> T execute(String requestId, String value, java.util.function.Supplier<T> action) {
        lock(requestId, value);
        return action.get();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void execute(String requestId, String value, Runnable action) {
        lock(requestId, value);
        action.run();
    }
}
