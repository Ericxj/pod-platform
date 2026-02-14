package com.pod.infra.outbox.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pod.common.core.exception.BusinessException;
import com.pod.infra.outbox.domain.OutboxEvent;
import com.pod.infra.outbox.mapper.OutboxMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {

    @Autowired
    private OutboxMapper outboxMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Publish an event to the outbox.
     * Must be called within a transaction.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void publish(String eventType, String aggregateType, Long aggregateId, String bizNo, Object payload) {
        OutboxEvent event = new OutboxEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setBizNo(bizNo);
        try {
            event.setPayloadJson(objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new BusinessException("Failed to serialize event payload");
        }
        event.setStatus(OutboxEvent.STATUS_NEW);
        event.setAttempts(0);
        outboxMapper.insert(event);
    }
}
