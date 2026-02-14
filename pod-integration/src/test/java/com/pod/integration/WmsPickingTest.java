package com.pod.integration;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.infra.context.RequestIdContext;
import com.pod.wms.domain.OutboundOrder;
import org.slf4j.MDC;
import com.pod.wms.domain.OutboundOrderLine;
import com.pod.wms.domain.PickTask;
import com.pod.wms.domain.PickTaskLine;
import com.pod.wms.mapper.OutboundOrderMapper;
import com.pod.wms.mapper.OutboundOrderLineMapper;
import com.pod.wms.mapper.PickTaskMapper;
import com.pod.wms.mapper.PickTaskLineMapper;
import com.pod.wms.service.OutboundApplicationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public class WmsPickingTest {

    @Autowired
    private OutboundApplicationService outboundService;
    @Autowired
    private OutboundOrderMapper outboundOrderMapper;
    @Autowired
    private OutboundOrderLineMapper outboundOrderLineMapper;
    @Autowired
    private PickTaskMapper pickTaskMapper;
    @Autowired
    private PickTaskLineMapper pickTaskLineMapper;

    @BeforeEach
    public void setup() {
        TenantContext.setTenantId(1001L);
        TenantContext.setFactoryId(2001L);
    }

    private void withRequestId(Runnable action) {
        MDC.put(RequestIdContext.MDC_KEY, UUID.randomUUID().toString());
        try {
            action.run();
        } finally {
            MDC.remove(RequestIdContext.MDC_KEY);
        }
    }

    @Test
    public void testCreatePickTaskAndVerifyLines() {
        String fulfillmentNo = "FUL_TEST_1_" + System.currentTimeMillis();
        List<OutboundOrderLine> lines = new ArrayList<>();
        OutboundOrderLine line1 = new OutboundOrderLine();
        line1.setLineNo(1);
        line1.setSkuId(101L);
        line1.setQty(10);
        lines.add(line1);
        OutboundOrderLine line2 = new OutboundOrderLine();
        line2.setLineNo(2);
        line2.setSkuId(102L);
        line2.setQty(5);
        lines.add(line2);

        final String[] outboundNoHolder = new String[1];
        withRequestId(() -> outboundNoHolder[0] = outboundService.createOutboundOrder(fulfillmentNo, 9001L, lines));
        String outboundNo = outboundNoHolder[0];

        final String[] pickTaskNoHolder = new String[1];
        withRequestId(() -> pickTaskNoHolder[0] = outboundService.createPickTask(outboundNo));
        String pickTaskNo = pickTaskNoHolder[0];
        
        PickTask pickTask = pickTaskMapper.selectOne(new QueryWrapper<PickTask>().eq("pick_task_no", pickTaskNo));
        Assertions.assertNotNull(pickTask);

        // 3. Verify Lines
        List<PickTaskLine> pickLines = pickTaskLineMapper.selectList(new QueryWrapper<PickTaskLine>().eq("pick_task_id", pickTask.getId()));
        Assertions.assertEquals(2, pickLines.size());
        
        PickTaskLine pl1 = pickLines.stream().filter(pl -> pl.getSkuId().equals(101L)).findFirst().orElse(null);
        Assertions.assertNotNull(pl1);
        Assertions.assertEquals(10, pl1.getQty());
        Assertions.assertEquals(0, pl1.getQtyActual());
        Assertions.assertEquals(PickTaskLine.STATUS_PENDING, pl1.getStatus());
    }

    @Test
    public void testBlindPickingPrevention() {
        String fulfillmentNo = "FUL_TEST_2_" + System.currentTimeMillis();
        List<OutboundOrderLine> lines = new ArrayList<>();
        OutboundOrderLine line1 = new OutboundOrderLine();
        line1.setLineNo(1);
        line1.setSkuId(101L);
        line1.setQty(10);
        lines.add(line1);

        final String[] outboundNoHolder = new String[1];
        withRequestId(() -> outboundNoHolder[0] = outboundService.createOutboundOrder(fulfillmentNo, 9001L, lines));
        final String[] pickTaskNoHolder = new String[1];
        withRequestId(() -> pickTaskNoHolder[0] = outboundService.createPickTask(outboundNoHolder[0]));
        PickTask pickTask = pickTaskMapper.selectOne(new QueryWrapper<PickTask>().eq("pick_task_no", pickTaskNoHolder[0]));

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> {
            withRequestId(() -> outboundService.confirmPick(pickTask.getId()));
        });
        Assertions.assertEquals("Cannot complete pick task: Not all lines are DONE.", exception.getMessage());
    }

    @Test
    public void testFullPickingFlow() {
        String fulfillmentNo = "FUL_TEST_3_" + System.currentTimeMillis();
        List<OutboundOrderLine> lines = new ArrayList<>();
        OutboundOrderLine line1 = new OutboundOrderLine();
        line1.setLineNo(1);
        line1.setSkuId(101L);
        line1.setQty(10);
        lines.add(line1);

        final String[] outboundNoHolder = new String[1];
        withRequestId(() -> outboundNoHolder[0] = outboundService.createOutboundOrder(fulfillmentNo, 9001L, lines));
        final String[] pickTaskNoHolder = new String[1];
        withRequestId(() -> pickTaskNoHolder[0] = outboundService.createPickTask(outboundNoHolder[0]));
        PickTask pickTask = pickTaskMapper.selectOne(new QueryWrapper<PickTask>().eq("pick_task_no", pickTaskNoHolder[0]));

        List<PickTaskLine> pickLines = pickTaskLineMapper.selectList(new QueryWrapper<PickTaskLine>().eq("pick_task_id", pickTask.getId()));
        PickTaskLine pl1 = pickLines.get(0);

        withRequestId(() -> outboundService.confirmPickLine(pl1.getId(), 10));

        PickTaskLine updatedPl1 = pickTaskLineMapper.selectById(pl1.getId());
        Assertions.assertEquals(PickTaskLine.STATUS_DONE, updatedPl1.getStatus());
        Assertions.assertEquals(10, updatedPl1.getQtyActual());

        withRequestId(() -> outboundService.confirmPick(pickTask.getId()));

        // 4. Verify Task Status
        PickTask updatedTask = pickTaskMapper.selectById(pickTask.getId());
        Assertions.assertEquals(PickTask.STATUS_DONE, updatedTask.getStatus());

        // 5. Verify Outbound Status
        OutboundOrder outbound = outboundOrderMapper.selectOne(new QueryWrapper<OutboundOrder>().eq("outbound_no", outboundNoHolder[0]));
        Assertions.assertEquals(OutboundOrder.STATUS_PICKED, outbound.getStatus());
        
        // 6. Verify Outbound Line QtyPicked
        OutboundOrderLine obLine = outboundOrderLineMapper.selectOne(new QueryWrapper<OutboundOrderLine>().eq("outbound_id", outbound.getId()));
        Assertions.assertEquals(10, obLine.getQtyPicked());
    }
}
