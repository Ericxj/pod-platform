package com.pod.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.art.domain.ArtJob;
import com.pod.art.domain.ProductionFile;
import com.pod.art.domain.RenderTask;
import com.pod.art.mapper.ArtJobMapper;
import com.pod.art.mapper.ProductionFileMapper;
import com.pod.art.mapper.RenderTaskMapper;
import com.pod.art.service.ArtJobService;
import com.pod.common.core.context.TenantContext;
import com.pod.infra.outbox.domain.OutboxEvent;
import com.pod.infra.outbox.mapper.OutboxMapper;
import com.pod.integration.job.PodJobHandler;
import com.pod.mes.domain.WorkOrder;
import com.pod.mes.mapper.WorkOrderMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public class ArtMesIntegrationTest {

    @Autowired
    private ArtJobService artJobService;

    @Autowired
    private ArtJobMapper artJobMapper;

    @Autowired
    private RenderTaskMapper renderTaskMapper;

    @Autowired
    private ProductionFileMapper productionFileMapper;

    @Autowired
    private OutboxMapper outboxMapper;

    @Autowired
    private PodJobHandler podJobHandler;

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @BeforeEach
    public void setup() {
        TenantContext.setTenantId(1L);
        TenantContext.setFactoryId(1L);
        
        // Clean up
        workOrderMapper.delete(null);
        outboxMapper.delete(null);
        productionFileMapper.delete(null);
        renderTaskMapper.delete(null);
        artJobMapper.delete(null);
    }

    @Test
    public void testArtToMesFlow() {
        // 1. Create ArtJob from Fulfillment
        Long fulfillmentId = 1001L;
        String jobNo = "JOB-TEST-1";
        
        Long jobId = artJobService.createJobFromFulfillment(fulfillmentId, jobNo, "req-1001");
        Assertions.assertNotNull(jobId);
        
        ArtJob job = artJobMapper.selectById(jobId);
        Assertions.assertEquals(ArtJob.STATUS_CREATED, job.getStatus());
        
        // Verify RenderTask created
        List<RenderTask> tasks = renderTaskMapper.selectList(new LambdaQueryWrapper<RenderTask>().eq(RenderTask::getArtJobId, jobId));
        Assertions.assertEquals(1, tasks.size());
        RenderTask task = tasks.get(0);
        Assertions.assertEquals(RenderTask.STATUS_PENDING, task.getStatus());

        // 2. Simulate Worker picking and executing task
        // Loop a few times to ensure state transition
        boolean picked = artJobService.pickAndExecuteTask();
        Assertions.assertTrue(picked, "Task should be picked");
        
        // Verify Task Status -> SUCCESS (since we mock sync execution in service)
        task = renderTaskMapper.selectById(task.getId());
        Assertions.assertEquals(RenderTask.STATUS_SUCCESS, task.getStatus());
        Assertions.assertNotNull(task.getOutputUrl());

        // Verify ArtJob Status -> SUCCESS
        job = artJobMapper.selectById(jobId);
        Assertions.assertEquals(ArtJob.STATUS_SUCCESS, job.getStatus());
        
        // Verify ProductionFile created
        Long pfCount = productionFileMapper.selectCount(new LambdaQueryWrapper<ProductionFile>().eq(ProductionFile::getArtJobId, jobId));
        Assertions.assertEquals(1, pfCount);

        // 3. Verify Outbox Event created
        List<OutboxEvent> events = outboxMapper.selectList(new LambdaQueryWrapper<OutboxEvent>().eq(OutboxEvent::getAggregateId, jobId));
        Assertions.assertEquals(1, events.size());
        OutboxEvent event = events.get(0);
        Assertions.assertEquals("ArtJobCompleted", event.getEventType());
        Assertions.assertEquals(OutboxEvent.STATUS_NEW, event.getStatus());

        // 4. Simulate Outbox Dispatcher
        podJobHandler.outboxDispatcherJobHandler();
        
        // Verify Event Status -> SENT
        event = outboxMapper.selectById(event.getId());
        Assertions.assertEquals(OutboxEvent.STATUS_SENT, event.getStatus());

        // 5. Verify WorkOrder created in MES
        List<WorkOrder> wos = workOrderMapper.selectList(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getFulfillmentId, fulfillmentId));
        Assertions.assertEquals(1, wos.size());
        WorkOrder wo = wos.get(0);
        Assertions.assertEquals("WO-" + jobNo, wo.getWorkOrderNo());
        Assertions.assertEquals(WorkOrder.STATUS_CREATED, wo.getStatus());
        
        System.out.println("Test ArtToMesFlow Passed!");
    }
}
