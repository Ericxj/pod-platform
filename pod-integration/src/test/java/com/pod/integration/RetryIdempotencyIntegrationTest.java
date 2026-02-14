package com.pod.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.art.domain.ArtJob;
import com.pod.art.domain.RenderTask;
import com.pod.art.mapper.ArtJobMapper;
import com.pod.art.mapper.RenderTaskMapper;
import com.pod.art.service.ArtJobService;
import com.pod.common.core.context.TenantContext;
import com.pod.mes.domain.WorkOrder;
import com.pod.mes.mapper.WorkOrderMapper;
import com.pod.infra.context.RequestIdContext;
import com.pod.mes.service.WorkOrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public class RetryIdempotencyIntegrationTest {

    @Autowired
    private ArtJobService artJobService;

    @Autowired
    private ArtJobMapper artJobMapper;

    @Autowired
    private RenderTaskMapper renderTaskMapper;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @BeforeEach
    public void setup() {
        TenantContext.setTenantId(1L);
        TenantContext.setFactoryId(1L);
        
        workOrderMapper.delete(null);
        renderTaskMapper.delete(null);
        artJobMapper.delete(null);
    }

    /**
     * Test Idempotency of WorkOrder Creation.
     * Multiple concurrent requests with same RequestId should result in only ONE WorkOrder.
     */
    @Test
    public void testWorkOrderCreationIdempotency() throws InterruptedException {
        Long fulfillmentId = 9999L;
        String jobNo = "JOB-IDEM-TEST";
        String requestId = UUID.randomUUID().toString();
        
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    TenantContext.setTenantId(1L);
                    TenantContext.setFactoryId(1L);
                    MDC.put(RequestIdContext.MDC_KEY, requestId);
                    try {
                        workOrderService.createWorkOrder(fulfillmentId, jobNo);
                        successCount.incrementAndGet();
                    } finally {
                        MDC.remove(RequestIdContext.MDC_KEY);
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    TenantContext.clear();
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Verify only 1 WorkOrder created
        Long count = workOrderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>()
                .eq(WorkOrder::getFulfillmentId, fulfillmentId));
        
        Assertions.assertEquals(1, count, "Should have exactly 1 WorkOrder");
    }

    /**
     * Test RenderTask Retry Mechanism.
     * Task fails -> Retry -> Attempts increment -> Max attempts reached -> Stop retrying.
     */
    @Test
    public void testRenderTaskRetryLogic() {
        // 1. Setup Failed Task
        ArtJob job = new ArtJob();
        job.setFulfillmentId(8888L);
        job.setArtJobNo("JOB-RETRY-TEST");
        job.setStatus(ArtJob.STATUS_RENDERING);
        job.setFactoryId(1L);
        job.setTenantId(1L);
        artJobMapper.insert(job);

        RenderTask task = new RenderTask();
        task.setArtJobId(job.getId());
        task.setTaskNo("RT-12345");
        task.setStatus(RenderTask.STATUS_FAILED);
        task.setAttempts(1);
        task.setFactoryId(1L);
        task.setTenantId(1L);
        renderTaskMapper.insert(task);

        // 2. Trigger Retry
        artJobService.retryFailedTasks();

        // 3. Verify Status -> PENDING (so it can be picked again)
        task = renderTaskMapper.selectById(task.getId());
        Assertions.assertEquals(RenderTask.STATUS_PENDING, task.getStatus());
        // Retry logic resets status to PENDING but does not increment attempts until it runs and fails again
        Assertions.assertEquals(1, task.getAttempts());

        // 4. Fail it again (manually set to FAILED)
        task.setStatus(RenderTask.STATUS_FAILED);
        task.setAttempts(task.getAttempts() + 1); // Simulate that the retry executed and failed
        renderTaskMapper.updateById(task);
        
        // 5. Retry again
        artJobService.retryFailedTasks();
        
        task = renderTaskMapper.selectById(task.getId());
        Assertions.assertEquals(RenderTask.STATUS_PENDING, task.getStatus());
        Assertions.assertEquals(2, task.getAttempts());
        
        // 6. Fail again -> Max attempts reached
        task.setStatus(RenderTask.STATUS_FAILED);
        task.setAttempts(3); // Max attempts
        renderTaskMapper.updateById(task);
        
        artJobService.retryFailedTasks();
        
        task = renderTaskMapper.selectById(task.getId());
        Assertions.assertEquals(RenderTask.STATUS_FAILED, task.getStatus()); // Should NOT be PENDING
    }
}
