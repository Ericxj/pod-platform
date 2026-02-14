package com.pod.integration;

import com.pod.ai.domain.AiTask;
import com.pod.ai.service.AiApplicationService;
import com.pod.art.service.ArtJobService;
import com.pod.infra.context.RequestIdContext;
import com.pod.oms.job.OrderPullJob;
import com.pod.art.job.RenderRetryJob;
import com.pod.common.core.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public class AiSchedulingIntegrationTest {

    @Autowired
    private OrderPullJob orderPullJob;

    @Autowired
    private RenderRetryJob renderRetryJob;

    @Autowired
    private AiApplicationService aiApplicationService;
    
    @Autowired
    private com.pod.ai.mapper.AiTaskMapper aiTaskMapper;

    @BeforeEach
    public void setup() {
        TenantContext.setTenantId(1L);
        TenantContext.setFactoryId(100L);
        TenantContext.setUserId(999L);
    }

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    @Test
    @Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testAiTaskFlow() throws InterruptedException {
        // 1. Submit Diagnostic Task
        String bizType = "TEST_IMG";
        String bizNo = "IMG_001_" + System.currentTimeMillis();
        String payload = "{\"imageUrl\": \"http://mock/1.jpg\"}";
        
        // Mock Trace ID and Request ID (required by createDiagnoseTask)
        com.pod.common.utils.TraceIdUtils.setTraceId("test-trace");
        String taskNo;
        try {
            org.slf4j.MDC.put(RequestIdContext.MDC_KEY, "req-001");
            taskNo = aiApplicationService.createDiagnoseTask(bizType, bizNo, payload);
        } finally {
            org.slf4j.MDC.remove(RequestIdContext.MDC_KEY);
        }
        com.pod.common.utils.TraceIdUtils.remove();

        Assertions.assertNotNull(taskNo);
        
        // Verify Pending
        AiTask task = aiApplicationService.getTask(taskNo);
        Assertions.assertNotNull(task, "Task should be found");
        Assertions.assertEquals(AiTask.STATUS_CREATED, task.getStatus());
        
        // 2. Trigger Scheduler (Manually simulate XXL-JOB call)
        aiApplicationService.scanAndProcessTasks(null, 0, 1);
        
        // 3. Verify Done
        task = aiApplicationService.getTask(taskNo);
        Assertions.assertEquals(AiTask.STATUS_SUCCESS, task.getStatus());
        Assertions.assertNotNull(task.getResultJson());
        System.out.println("AI Result: " + task.getResultJson());
    }
    
    @Test
    @Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testOrderPullJob() {
        // Just verify it runs without exception
        orderPullJob.pullOrders();
    }
    
    @Test
    @Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testRenderRetryJob() {
        // Just verify it runs without exception
        renderRetryJob.retryFailedTasks();
    }
}
