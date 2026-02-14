package com.pod.iam.service;

import com.pod.common.core.context.TenantContext;
import com.pod.infra.context.RequestIdContext;
import com.pod.iam.domain.AiDiagnosisRecord;
import com.pod.iam.mapper.AiDiagnosisMapper;
import com.pod.iam.mapper.IamUserMapper;
import com.pod.iam.mapper.IamPermissionMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "iam.menu.validate-on-startup=false"
})
public class AiDiagnosisServiceTest {

    @Autowired
    private AiDiagnosisService diagnosisService;

    @Autowired
    private AiDiagnosisMapper diagnosisMapper;
    
    @MockBean
    private IamPermissionMapper permissionMapper;
    
    @MockBean
    private IamUserMapper iamUserMapper;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(100001L);
        TenantContext.setFactoryId(100001L);
        TenantContext.setUserId(1L);
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put(RequestIdContext.MDC_KEY, UUID.randomUUID().toString());
        when(permissionMapper.selectByUserId(anyLong())).thenReturn(Collections.emptyList());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        MDC.clear();
    }

    @Test
    void testSubmitAndPollDiagnosis() throws InterruptedException {
        String businessKey = "1";
        Long id = diagnosisService.submitDiagnosis("PERMISSION_CHECK", businessKey);
        assertNotNull(id);

        // 2. Verify Initial State
        AiDiagnosisRecord record = diagnosisService.getDiagnosis(id);
        assertNotNull(record);
        assertEquals("PENDING", record.getStatus());

        // 3. Wait for Async Processing (Mock takes 2s)
        // Polling for up to 5 seconds
        boolean completed = false;
        for (int i = 0; i < 10; i++) {
            TimeUnit.MILLISECONDS.sleep(500);
            record = diagnosisService.getDiagnosis(id);
            if ("COMPLETED".equals(record.getStatus()) || "FAILED".equals(record.getStatus())) {
                completed = true;
                break;
            }
        }

        assertTrue(completed, "Diagnosis should complete within 5 seconds");
        assertEquals("COMPLETED", record.getStatus());
        assertNotNull(record.getResultJson());
        assertTrue(record.getResultJson().contains("score"));

        System.out.println("Diagnosis Result: " + record.getResultJson());
    }

    @Test
    void testIdempotency() {
        String businessKey = "1";
        diagnosisService.submitDiagnosis("PERMISSION_CHECK", businessKey);
        assertThrows(Exception.class, () -> {
            diagnosisService.submitDiagnosis("PERMISSION_CHECK", businessKey);
        });
    }
}
