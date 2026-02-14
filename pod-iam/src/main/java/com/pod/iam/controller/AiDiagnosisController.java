package com.pod.iam.controller;

import com.pod.common.core.domain.Result;
import com.pod.iam.domain.AiDiagnosisRecord;
import com.pod.iam.service.AiDiagnosisService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/iam/ai")
public class AiDiagnosisController {

    private final AiDiagnosisService diagnosisService;

    public AiDiagnosisController(AiDiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping("/diagnose")
    @PreAuthorize("hasAuthority('SysAiDiagnosis')")
    public Result<Map<String, Object>> submitDiagnosis(@RequestBody Map<String, String> payload) {
        String type = payload.get("type");
        String businessKey = payload.get("businessKey");
        Long id = diagnosisService.submitDiagnosis(type, businessKey);
        return Result.success(Map.of("id", id, "status", "PENDING"));
    }

    @GetMapping("/diagnose/{id}")
    @PreAuthorize("hasAuthority('SysAiDiagnosis')")
    public Result<AiDiagnosisRecord> getDiagnosis(@PathVariable Long id) {
        AiDiagnosisRecord record = diagnosisService.getDiagnosis(id);
        return Result.success(record);
    }
}
