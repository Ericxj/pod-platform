package com.pod.integration.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pod.art.service.ArtJobService;
import com.pod.infra.context.RequestIdContext;
import com.pod.infra.outbox.domain.OutboxEvent;
import com.pod.infra.outbox.mapper.OutboxMapper;
import com.pod.mes.service.WorkOrderService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PodJobHandler {

    @Autowired
    private ArtJobService artJobService;
    
    @Autowired
    private OutboxMapper outboxMapper;
    
    @Autowired
    private WorkOrderService workOrderService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @XxlJob("helloJob")
    public void helloJob() {
        System.out.println("XXL-JOB, Hello World.");
    }

    /**
     * Retry failed render tasks.
     * Schedule: Every 5 minutes.
     */
    @XxlJob("renderRetryJobHandler")
    public void renderRetryJobHandler() {
        artJobService.retryFailedTasks();
    }

    /**
     * Dispatch Outbox Events.
     * In real production, this might be a dedicated process or CDC.
     * Schedule: Every 1 second (or use @Scheduled).
     */
    @XxlJob("outboxDispatcherJobHandler")
    public void outboxDispatcherJobHandler() {
        // Find NEW events
        LambdaQueryWrapper<OutboxEvent> query = new LambdaQueryWrapper<>();
        query.eq(OutboxEvent::getStatus, OutboxEvent.STATUS_NEW);
        query.last("LIMIT 100"); // Batch size
        
        List<OutboxEvent> events = outboxMapper.selectList(query);
        
        for (OutboxEvent event : events) {
            try {
                processEvent(event);
                event.setStatus(OutboxEvent.STATUS_SENT);
            } catch (Exception e) {
                event.setStatus(OutboxEvent.STATUS_FAILED);
                // Log error
            }
            outboxMapper.updateById(event);
        }
    }

    private void processEvent(OutboxEvent event) throws Exception {
        if ("ArtJobCompleted".equals(event.getEventType())) {
            ArtJobService.ArtJobCompletedEvent payload = objectMapper.readValue(event.getPayloadJson(), ArtJobService.ArtJobCompletedEvent.class);
            MDC.put(RequestIdContext.MDC_KEY, event.getEventId());
            try {
                workOrderService.createWorkOrder(payload.fulfillmentId, payload.artJobNo);
            } finally {
                MDC.remove(RequestIdContext.MDC_KEY);
            }
        }
    }
}
