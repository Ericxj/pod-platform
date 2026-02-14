package com.pod.tms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pod.common.core.exception.BusinessException;
import com.pod.common.utils.TraceIdUtils;
import com.pod.infra.context.RequestIdContext;
import com.pod.infra.idempotent.service.IdempotentService;
import com.pod.tms.domain.PlatformAck;
import com.pod.tms.domain.Shipment;
import com.pod.tms.mapper.PlatformAckMapper;
import com.pod.tms.mapper.ShipmentMapper;
import com.pod.wms.service.OutboundApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ShipmentApplicationService {

    @Autowired
    private ShipmentMapper shipmentMapper;
    @Autowired
    private PlatformAckMapper platformAckMapper;
    @Autowired
    private IdempotentService idempotentService;
    @Autowired
    private OutboundApplicationService outboundApplicationService;

    @Transactional(rollbackFor = Exception.class)
    public String createShipment(Long outboundId, Long carrierId, String shipToAddressJson) {
        String requestId = RequestIdContext.getRequired();
        return idempotentService.execute(requestId, "createShipment:" + outboundId, () -> {
            Shipment shipment = new Shipment();
            shipment.setShipmentNo("SH" + System.currentTimeMillis());
            shipment.setOutboundId(outboundId);
            shipment.setCarrierId(carrierId);
            shipment.setShipToAddressJson(shipToAddressJson);
            shipment.setStatus(Shipment.STATUS_CREATED);
            shipment.setTraceId(TraceIdUtils.getTraceId());
            shipment.setVersion(0);
            shipmentMapper.insert(shipment);
            
            // Mock Waybill immediately
            mockWaybill(shipment);
            
            return shipment.getShipmentNo();
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void retryPlatformAcks() {
        // 1. Scan shipments that are SHIPPED but no SUCCESS ack
        // Ideally this should be a custom SQL, but we use MP here.
        // For demo, we just find some SHIPPED shipments and try to ACK them if not already acked.
        // We can check tms_platform_ack table.
        
        // Find shipments shipped in last 24 hours
        java.util.List<Shipment> shipments = shipmentMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Shipment>()
                .eq(Shipment::getStatus, Shipment.STATUS_SHIPPED)
                .last("LIMIT 100"));

        for (Shipment shipment : shipments) {
            // Check if ack exists and is success
            Long count = platformAckMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlatformAck>()
                    .eq(PlatformAck::getPlatformOrderId, String.valueOf(shipment.getOutboundId())) // Assuming outboundId is the link or platform_order_id
                    .eq(PlatformAck::getStatus, "SUCCESS"));
            
            if (count == 0) {
                // Mock ACK
                PlatformAck ack = new PlatformAck();
                ack.setPlatformOrderId(String.valueOf(shipment.getOutboundId()));
                ack.setTenantId(shipment.getTenantId());
                ack.setFactoryId(shipment.getFactoryId());
                ack.setStatus("SUCCESS");
                ack.setResponseJson("{\"mock\": true}");
                ack.setActionType("SHIP_CONFIRM");
                ack.setPlatformCode("MOCK");
                ack.setShopId(1L);
                platformAckMapper.insert(ack);
            }
        }
    }

    private void mockWaybill(Shipment shipment) {
        String trackingNo = "TRK" + System.currentTimeMillis();
        String labelUrl = "https://mock-carrier.com/label/" + trackingNo + ".pdf";
        
        // Domain Logic
        String oldStatus = shipment.getStatus();
        shipment.label(trackingNo, labelUrl);
        shipment.setLabelFormat("PDF");
        
        // Optimistic Lock Update (Even for new records, good habit)
        // Note: MyBatis-Plus OptimisticLockerInnerInterceptor handles version check and increment automatically.
        // We only need to ensure status is consistent (CAS on status).
        boolean success = shipmentMapper.update(
            shipment,
            new LambdaUpdateWrapper<Shipment>()
                .eq(Shipment::getId, shipment.getId())
                .eq(Shipment::getStatus, oldStatus)
        ) > 0;
        
        if (!success) {
            throw new BusinessException("Concurrency conflict: Shipment status changed.");
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void ship(String shipmentNo) {
        System.out.println("DEBUG: Entering ship for shipmentNo: " + shipmentNo);
        String requestId = RequestIdContext.getRequired();
        idempotentService.execute(requestId, "ship:" + shipmentNo, () -> {
            Shipment shipment = shipmentMapper.selectOne(new QueryWrapper<Shipment>().eq("shipment_no", shipmentNo));
            if (shipment == null) throw new BusinessException("Shipment not found");
            
            // Domain Logic
            String oldStatus = shipment.getStatus();
            shipment.confirmShip(LocalDateTime.now());
            
            // Optimistic Lock Update
            // Note: MyBatis-Plus OptimisticLockerInnerInterceptor handles version check and increment automatically.
            boolean success = shipmentMapper.update(
                shipment,
                new LambdaUpdateWrapper<Shipment>()
                    .eq(Shipment::getId, shipment.getId())
                    .eq(Shipment::getStatus, oldStatus)
            ) > 0;
            
            if (!success) {
                throw new BusinessException("Concurrency conflict: Shipment status changed or version mismatch.");
            }
            
            // Call WMS to confirm ship and deduct inventory
            System.out.println("DEBUG: Calling outboundApplicationService.confirmShipped for outboundId: " + shipment.getOutboundId());
            outboundApplicationService.confirmShipped(shipment.getOutboundId());
            
            // Mock Platform Callback
            mockPlatformCallback(shipment);
        });
    }

    private void mockPlatformCallback(Shipment shipment) {
        PlatformAck ack = new PlatformAck();
        ack.setPlatformCode("MOCK_PLATFORM");
        ack.setShopId(12345L); // Mock Shop
        ack.setPlatformOrderId("MOCK_ORDER_" + shipment.getOutboundId());
        ack.setActionType("SHIP_CONFIRM");
        ack.setStatus("SUCCESS"); // Direct success for mock
        ack.setAttempts(1);
        ack.setPayloadJson("{\"trackingNo\": \"" + shipment.getTrackingNo() + "\"}");
        ack.setResponseJson("{\"success\": true}");
        ack.setTraceId(TraceIdUtils.getTraceId());
        platformAckMapper.insert(ack);
    }
}
