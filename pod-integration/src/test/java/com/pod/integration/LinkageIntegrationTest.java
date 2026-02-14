package com.pod.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.art.domain.ArtJob;
import com.pod.art.domain.RenderTask;
import com.pod.art.mapper.ArtJobMapper;
import com.pod.art.mapper.RenderTaskMapper;
import com.pod.art.service.ArtJobService;
import com.pod.infra.outbox.domain.OutboxEvent;
import com.pod.infra.outbox.mapper.OutboxMapper;
import com.pod.integration.job.PodJobHandler;
import com.pod.mes.domain.WorkOrder;
import com.pod.mes.mapper.WorkOrderMapper;
import com.pod.mes.service.WorkOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LinkageIntegrationTest {

    @InjectMocks
    private PodJobHandler podJobHandler;

    @Mock
    private ArtJobService artJobService;

    @Mock
    private OutboxMapper outboxMapper;

    @Mock
    private WorkOrderService workOrderService;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void testOutboxDispatcher_Success() throws Exception {
        // Arrange
        OutboxEvent event = new OutboxEvent();
        event.setId(1L);
        event.setEventType("ArtJobCompleted");
        event.setPayloadJson("{\"jobId\":100, \"jobNo\":\"JOB-001\", \"productionFile\":\"/path/file.zip\"}");
        event.setStatus("NEW");

        when(outboxMapper.selectList(any())).thenReturn(List.of(event));
        
        ArtJobService.ArtJobCompletedEvent payloadObj = new ArtJobService.ArtJobCompletedEvent(100L, 200L, "JOB-001", "/path/file.zip");
        when(objectMapper.readValue(eq(event.getPayloadJson()), eq(ArtJobService.ArtJobCompletedEvent.class)))
                .thenReturn(payloadObj);

        // Act
        podJobHandler.outboxDispatcherJobHandler();

        // Assert
        verify(workOrderService).createWorkOrder(eq(200L), eq("JOB-001"));
        
        // Verify status update
        assertEquals("SENT", event.getStatus());
        verify(outboxMapper).updateById(event);
    }

    @Test
    public void testRenderRetryJob() {
        // Act
        podJobHandler.renderRetryJobHandler();

        // Assert
        verify(artJobService).retryFailedTasks();
    }
}
