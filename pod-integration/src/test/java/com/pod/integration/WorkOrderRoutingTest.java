package com.pod.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.infra.context.RequestIdContext;
import com.pod.mes.domain.WorkOrder;
import org.slf4j.MDC;
import com.pod.mes.domain.WorkOrderOp;
import com.pod.mes.mapper.WorkOrderMapper;
import com.pod.mes.mapper.WorkOrderOpMapper;
import com.pod.mes.service.WorkOrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class WorkOrderRoutingTest {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private WorkOrderMapper workOrderMapper;
    
    @Autowired
    private WorkOrderOpMapper workOrderOpMapper;

    @BeforeEach
    public void setup() {
        TenantContext.setTenantId(1L);
        TenantContext.setFactoryId(1L);
        
        workOrderOpMapper.delete(null);
        workOrderMapper.delete(null);
    }

    private void withRequestId(String requestId, Runnable action) {
        MDC.put(RequestIdContext.MDC_KEY, requestId);
        try {
            action.run();
        } finally {
            MDC.remove(RequestIdContext.MDC_KEY);
        }
    }

    @Test
    public void testRoutingTemplateApplication() {
        withRequestId(UUID.randomUUID().toString(), () -> {
            Long woId1 = workOrderService.createWorkOrder(1001L, "JOB-DEFAULT");
            List<WorkOrderOp> ops1 = workOrderService.getOperations(woId1);
            Assertions.assertEquals(3, ops1.size(), "Default template should have 3 steps");
            Assertions.assertEquals("PRINT", ops1.get(0).getOpCode());
            Assertions.assertEquals("CUT", ops1.get(1).getOpCode());
            Assertions.assertEquals("PACK", ops1.get(2).getOpCode());
        });
        withRequestId(UUID.randomUUID().toString(), () -> {
            Long woId2 = workOrderService.createWorkOrder(1002L, "JOB-TSHIRT-001");
            List<WorkOrderOp> ops2 = workOrderService.getOperations(woId2);
            Assertions.assertEquals(4, ops2.size(), "T-Shirt template should have 4 steps");
            Assertions.assertEquals("PRINT", ops2.get(0).getOpCode());
            Assertions.assertEquals("DRY", ops2.get(1).getOpCode());
            Assertions.assertEquals("QC", ops2.get(2).getOpCode());
            Assertions.assertEquals("PACK", ops2.get(3).getOpCode());
            WorkOrder wo2 = workOrderMapper.selectById(woId2);
            Assertions.assertTrue(wo2.getRemark().contains("RoutingTemplate:POD_TSHIRT_V1"));
        });
    }

    @Test
    public void testSequentialValidation() {
        Long woId = withRequestIdAndReturn(UUID.randomUUID().toString(), () -> workOrderService.createWorkOrder(2001L, "JOB-SEQ-TEST"));
        List<WorkOrderOp> ops = workOrderService.getOperations(woId);
        WorkOrderOp op1 = ops.get(0);
        WorkOrderOp op2 = ops.get(1);

        Assertions.assertThrows(BusinessException.class, () -> {
            withRequestId(UUID.randomUUID().toString(), () -> workOrderService.startOperation(woId, op2.getId()));
        }, "Should fail to start Op 2 when Op 1 is not DONE");

        withRequestId(UUID.randomUUID().toString(), () -> workOrderService.startOperation(woId, op1.getId()));

        Assertions.assertThrows(BusinessException.class, () -> {
            withRequestId(UUID.randomUUID().toString(), () -> workOrderService.finishOperation(woId, op2.getId()));
        }, "Should fail to finish Op 2 when Op 1 is not DONE");

        withRequestId(UUID.randomUUID().toString(), () -> workOrderService.finishOperation(woId, op1.getId()));

        withRequestId(UUID.randomUUID().toString(), () -> workOrderService.startOperation(woId, op2.getId()));

        WorkOrderOp op2Updated = workOrderOpMapper.selectById(op2.getId());
        Assertions.assertEquals(WorkOrderOp.STATUS_RUNNING, op2Updated.getStatus());
    }

    private <T> T withRequestIdAndReturn(String requestId, java.util.function.Supplier<T> action) {
        MDC.put(RequestIdContext.MDC_KEY, requestId);
        try {
            return action.get();
        } finally {
            MDC.remove(RequestIdContext.MDC_KEY);
        }
    }

    @Test
    public void testConcurrentFinish() throws InterruptedException {
        Long woId = withRequestIdAndReturn(UUID.randomUUID().toString(), () -> workOrderService.createWorkOrder(3001L, "JOB-CONC-TEST"));
        List<WorkOrderOp> ops = workOrderService.getOperations(woId);
        WorkOrderOp op1 = ops.get(0);
        withRequestId(UUID.randomUUID().toString(), () -> workOrderService.startOperation(woId, op1.getId()));

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
                    withRequestId(UUID.randomUUID().toString(), () -> workOrderService.finishOperation(woId, op1.getId()));
                    successCount.incrementAndGet();
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

        // One should succeed, others fail (or be idempotent if we used same ID, but here different IDs -> only one wins optimistic lock)
        // Wait, if one succeeds, status becomes DONE. 
        // Subsequent threads:
        // - If they read "DONE" before update -> "Cannot finish Operation from status: DONE" (Validation Error)
        // - If they read "RUNNING" and try to update -> Version Mismatch (Optimistic Lock Error)
        
        // So successCount should be 1.
        Assertions.assertEquals(1, successCount.get());
        Assertions.assertEquals(4, failCount.get());
        
        WorkOrderOp finalOp = workOrderOpMapper.selectById(op1.getId());
        Assertions.assertEquals(WorkOrderOp.STATUS_DONE, finalOp.getStatus());
    }
}
