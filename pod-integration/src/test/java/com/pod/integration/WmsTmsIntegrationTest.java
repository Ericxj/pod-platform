package com.pod.integration;

import com.pod.common.core.context.TenantContext;
import com.pod.infra.context.RequestIdContext;
import com.pod.tms.domain.PlatformAck;
import org.slf4j.MDC;
import com.pod.tms.domain.Shipment;
import com.pod.tms.mapper.PlatformAckMapper;
import com.pod.tms.mapper.ShipmentMapper;
import com.pod.tms.service.ShipmentApplicationService;
import com.pod.wms.domain.OutboundOrder;
import com.pod.wms.domain.OutboundOrderLine;
import com.pod.wms.domain.PackOrder;
import com.pod.wms.domain.PickTask;
import com.pod.wms.mapper.OutboundOrderMapper;
import com.pod.wms.mapper.PackOrderMapper;
import com.pod.wms.mapper.PickTaskMapper;
import com.pod.wms.domain.PickTaskLine;
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
public class WmsTmsIntegrationTest {

    @Autowired
    private OutboundApplicationService outboundService;
    @Autowired
    private ShipmentApplicationService shipmentService;
    @Autowired
    private OutboundOrderMapper outboundOrderMapper;
    @Autowired
    private PickTaskMapper pickTaskMapper;
    @Autowired
    private PickTaskLineMapper pickTaskLineMapper;
    @Autowired
    private PackOrderMapper packOrderMapper;
    @Autowired
    private ShipmentMapper shipmentMapper;
    @Autowired
    private PlatformAckMapper platformAckMapper;

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
    public void testOutboundToShipmentFlow() {
        String fulfillmentNo = "FUL" + System.currentTimeMillis();
        List<OutboundOrderLine> lines = new ArrayList<>();
        OutboundOrderLine line = new OutboundOrderLine();
        line.setLineNo(1);
        line.setSkuId(10001L);
        line.setQty(2);
        lines.add(line);

        final String[] outboundNoHolder = new String[1];
        withRequestId(() -> {
            outboundNoHolder[0] = outboundService.createOutboundOrder(fulfillmentNo, 9001L, lines);
            Assertions.assertNotNull(outboundNoHolder[0]);
        });
        String outboundNo = outboundNoHolder[0];
        OutboundOrder outbound = outboundOrderMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OutboundOrder>().eq("outbound_no", outboundNo));
        Assertions.assertEquals("CREATED", outbound.getStatus());

        withRequestId(() -> Assertions.assertNotNull(outboundService.createPickTask(outboundNo)));
        outbound = outboundOrderMapper.selectById(outbound.getId());
        Assertions.assertEquals("ALLOCATED", outbound.getStatus());

        PickTask pickTask = pickTaskMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PickTask>().eq("outbound_id", outbound.getId()));
        List<PickTaskLine> pickLines = pickTaskLineMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PickTaskLine>().eq("pick_task_id", pickTask.getId()));
        for (PickTaskLine pl : pickLines) {
            final PickTaskLine pickLine = pl;
            withRequestId(() -> outboundService.confirmPickLine(pickLine.getId(), pickLine.getQty()));
        }
        final PickTask pickTaskFinal = pickTask;
        withRequestId(() -> outboundService.confirmPick(pickTaskFinal.getId()));

        pickTask = pickTaskMapper.selectById(pickTask.getId());
        Assertions.assertEquals("DONE", pickTask.getStatus());
        outbound = outboundOrderMapper.selectById(outbound.getId());
        Assertions.assertEquals("PICKED", outbound.getStatus());

        final String[] packNoHolder = new String[1];
        withRequestId(() -> packNoHolder[0] = outboundService.pack(outboundNo));
        String packNo = packNoHolder[0];
        Assertions.assertNotNull(packNo);
        PackOrder packOrder = packOrderMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PackOrder>().eq("pack_no", packNo));
        Assertions.assertEquals("PACKED", packOrder.getStatus());
        outbound = outboundOrderMapper.selectById(outbound.getId());
        Assertions.assertEquals("PACKED", outbound.getStatus());

        final String[] shipmentNoHolder = new String[1];
        final OutboundOrder outboundForShipment = outbound;
        withRequestId(() -> {
            Long carrierId = 5001L;
            String shipToAddress = "{\"country\": \"US\", \"city\": \"New York\"}";
            shipmentNoHolder[0] = shipmentService.createShipment(outboundForShipment.getId(), carrierId, shipToAddress);
        });
        String shipmentNo = shipmentNoHolder[0];
        Assertions.assertNotNull(shipmentNo);
        
        Shipment shipment = shipmentMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Shipment>().eq("shipment_no", shipmentNo));
        Assertions.assertEquals("LABELED", shipment.getStatus());
        Assertions.assertNotNull(shipment.getTrackingNo());
        Assertions.assertNotNull(shipment.getLabelUrl());

        withRequestId(() -> shipmentService.ship(shipmentNo));
        
        shipment = shipmentMapper.selectById(shipment.getId());
        Assertions.assertEquals("SHIPPED", shipment.getStatus());
        Assertions.assertNotNull(shipment.getShippedAt());

        // Verify Platform Ack
        PlatformAck ack = platformAckMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PlatformAck>()
                .eq("platform_order_id", "MOCK_ORDER_" + outbound.getId())
                .eq("action_type", "SHIP_CONFIRM"));
        Assertions.assertNotNull(ack);
        Assertions.assertEquals("SUCCESS", ack.getStatus());
        
        // Verify Outbound Status Updated by Ship
        outbound = outboundOrderMapper.selectById(outbound.getId());
        Assertions.assertEquals("SHIPPED", outbound.getStatus());
    }

    @Test
    public void testUnauthorizedAccess() {
        // 1. Setup Context A (Factory 2001)
        TenantContext.setTenantId(1001L);
        TenantContext.setFactoryId(2001L);

        // 2. Create Shipment in Factory 2001
        Shipment shipment = new Shipment();
        shipment.setShipmentNo("SH_AUTH_TEST");
        shipment.setOutboundId(999L);
        shipment.setCarrierId(5001L);
        shipment.setStatus("CREATED");
        shipmentMapper.insert(shipment);
        Long shipmentId = shipment.getId();

        // 3. Switch to Context B (Factory 9999) - Unauthorized
        TenantContext.setFactoryId(9999L);

        // 4. Try to Select
        Shipment result = shipmentMapper.selectById(shipmentId);
        Assertions.assertNull(result, "Should not find shipment from another factory");

        // 5. Try to Update
        shipment.setStatus("SHIPPED");
        int rows = shipmentMapper.updateById(shipment);
        Assertions.assertEquals(0, rows, "Should not be able to update shipment from another factory");
    }
}
