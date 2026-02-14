package com.pod.iam.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

@TableName("sys_ai_diagnosis")
public class AiDiagnosisRecord extends BaseEntity {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";

    private String traceId;
    
    private String businessKey;
    
    // PENDING, PROCESSING, COMPLETED, FAILED
    private String status;
    
    // e.g., PERMISSION_CHECK, SYSTEM_HEALTH
    private String diagnosisType;
    
    private String resultJson;

    // --- Domain Behaviors ---

    public void init(String traceId, String diagnosisType, String businessKey) {
        this.traceId = traceId;
        this.diagnosisType = diagnosisType;
        this.businessKey = businessKey;
        this.status = STATUS_PENDING;
    }

    public void startProcessing() {
        if (!STATUS_PENDING.equals(this.status)) {
            throw new BusinessException("Cannot start processing from status: " + this.status);
        }
        this.status = STATUS_PROCESSING;
    }

    public void complete(String resultJson) {
        if (!STATUS_PROCESSING.equals(this.status)) {
             throw new BusinessException("Cannot complete from status: " + this.status);
        }
        this.resultJson = resultJson;
        this.status = STATUS_COMPLETED;
    }

    public void fail(String errorMessage) {
        // Can fail from PENDING or PROCESSING
        this.resultJson = "{\"error\": \"" + errorMessage + "\"}";
        this.status = STATUS_FAILED;
    }

    // --- Getters and Setters ---

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDiagnosisType() {
        return diagnosisType;
    }

    public void setDiagnosisType(String diagnosisType) {
        this.diagnosisType = diagnosisType;
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }
}
