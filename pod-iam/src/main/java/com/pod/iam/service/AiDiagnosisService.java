package com.pod.iam.service;

import com.pod.iam.domain.AiDiagnosisRecord;

public interface AiDiagnosisService {
    
    /**
     * Submit a diagnosis request. X-Request-Id must be provided via request header (see RequestIdContext).
     * @param diagnosisType Type of diagnosis
     * @param businessKey Business key (e.g. userId)
     * @return The diagnosis record ID
     */
    Long submitDiagnosis(String diagnosisType, String businessKey);

    /**
     * Get diagnosis status and result.
     * @param id Record ID
     * @return Record
     */
    AiDiagnosisRecord getDiagnosis(Long id);
}
