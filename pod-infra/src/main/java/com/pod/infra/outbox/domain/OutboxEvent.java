package com.pod.infra.outbox.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("sys_outbox_event")
public class OutboxEvent extends BaseEntity {
    private String eventId;
    private String eventType;
    private String aggregateType;
    private Long aggregateId;
    private String bizNo;
    private String payloadJson;
    private String status; // NEW, SENT, FAILED
    private Integer attempts;
    private java.time.LocalDateTime nextRetryAt;

    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_FAILED = "FAILED";

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }

    public Long getAggregateId() { return aggregateId; }
    public void setAggregateId(Long aggregateId) { this.aggregateId = aggregateId; }

    public String getBizNo() { return bizNo; }
    public void setBizNo(String bizNo) { this.bizNo = bizNo; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
    
    public java.time.LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(java.time.LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }
}
