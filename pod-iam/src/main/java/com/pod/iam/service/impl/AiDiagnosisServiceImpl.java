package com.pod.iam.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.pod.common.core.context.TenantContext;
import com.pod.common.utils.TraceIdUtils;
import com.pod.iam.domain.AiDiagnosisRecord;
import com.pod.iam.mapper.AiDiagnosisMapper;
import com.pod.iam.mapper.IamPermissionMapper;
import com.pod.iam.service.AiDiagnosisService;
import com.pod.infra.context.RequestIdContext;
import com.pod.infra.idempotent.service.IdempotentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class AiDiagnosisServiceImpl implements AiDiagnosisService {

    private static final Logger log = LoggerFactory.getLogger(AiDiagnosisServiceImpl.class);

    private final AiDiagnosisMapper diagnosisMapper;
    private final IdempotentService idempotentService;
    private final IamPermissionMapper permissionMapper;

    public AiDiagnosisServiceImpl(AiDiagnosisMapper diagnosisMapper, IdempotentService idempotentService, IamPermissionMapper permissionMapper) {
        this.diagnosisMapper = diagnosisMapper;
        this.idempotentService = idempotentService;
        this.permissionMapper = permissionMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitDiagnosis(String diagnosisType, String businessKey) {
        String requestId = RequestIdContext.getRequired();
        idempotentService.lock(requestId, "AI_DIAGNOSIS:" + diagnosisType + ":" + businessKey);

        AiDiagnosisRecord record = new AiDiagnosisRecord();
        String traceId = MDC.get(TraceIdUtils.TRACE_ID);
        if (traceId == null) {
            traceId = requestId;
        }
        record.init(traceId, diagnosisType, businessKey);
        
        diagnosisMapper.insert(record);

        // 3. Trigger Async Processing (Mock AI Analysis) - Execute AFTER Commit to ensure record visibility
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                processDiagnosisAsync(record.getId(), businessKey);
            }
        });

        return record.getId();
    }

    @Override
    public AiDiagnosisRecord getDiagnosis(Long id) {
        return diagnosisMapper.selectById(id);
    }

    // Mock AI Processing - Async
    private void processDiagnosisAsync(Long recordId, String businessKey) {
        // Capture Context from Main Thread
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        Long userId = TenantContext.getUserId();
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        CompletableFuture.runAsync(() -> {
            try {
                // Restore Context in Async Thread
                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                }
                TenantContext.setTenantId(tenantId);
                TenantContext.setFactoryId(factoryId);
                TenantContext.setUserId(userId);

                // Fetch Record for DDD behavior
                AiDiagnosisRecord record = diagnosisMapper.selectById(recordId);
                if (record == null) return;

                // Start Processing
                record.startProcessing();
                diagnosisMapper.updateById(record);

                // Reload record to get updated version (Optimistic Lock)
                record = diagnosisMapper.selectById(recordId);

                // Simulate AI Thinking Time
                TimeUnit.SECONDS.sleep(2);

                // Analyze Permissions (Mock Logic using real data count)
                long permCount = 0;
                try {
                    Long targetUserId = Long.valueOf(businessKey);
                    permCount = permissionMapper.selectByUserId(targetUserId).size();
                } catch (NumberFormatException e) {
                    log.warn("Business Key is not a valid User ID: {}", businessKey);
                    // Treat as 0 or handle differently
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("ai_model", "Gemini-Pro-Mock");
                result.put("timestamp", LocalDateTime.now());

                if (permCount == 0) {
                    result.put("score", 60);
                    result.put("issues", new String[]{"NO_PERMISSIONS"});
                    result.put("suggestions", "警告：检测到该用户没有任何权限，请检查角色配置。");
                } else {
                    result.put("score", 95);
                    result.put("issues", new String[]{});
                    result.put("suggestions", "权限配置正常。检测到 " + permCount + " 个权限点。");
                }

                record.complete(JSONUtil.toJsonStr(result));
                diagnosisMapper.updateById(record);

            } catch (Exception e) {
                log.error("Async AI Diagnosis failed", e);
                // Handle failure state update if needed
                try {
                    AiDiagnosisRecord record = diagnosisMapper.selectById(recordId);
                    if (record != null) {
                        record.fail("Internal Error: " + e.getMessage());
                        diagnosisMapper.updateById(record);
                    }
                } catch (Exception ex) {
                    log.error("Failed to update failure status", ex);
                }
            } finally {
                TenantContext.clear();
                MDC.clear();
            }
        });
    }
}
