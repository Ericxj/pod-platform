package com.pod.tms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.common.utils.TraceIdUtils;
import com.pod.infra.context.RequestIdContext;
import com.pod.infra.idempotent.service.IdempotentService;
import com.pod.tms.domain.Shipment;
import com.pod.tms.gateway.CarrierGateway;
import com.pod.tms.gateway.ChannelFulfillmentGateway;
import com.pod.tms.gateway.CreateLabelRequest;
import com.pod.tms.gateway.CreateLabelResult;
import com.pod.tms.mapper.ShipmentMapper;
import com.pod.wms.domain.OutboundOrder;
import com.pod.wms.domain.WmsShipment;
import com.pod.wms.mapper.WmsShipmentMapper;
import com.pod.wms.service.OutboundApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * P1.6 TMS 应用服务：基于 WMS 出库单 SHIPPED 创建 tms_shipment，生成面单，回传平台。
 */
@Service
public class TmsApplicationService {

    @Autowired
    private ShipmentMapper shipmentMapper;
    @Autowired
    private OutboundApplicationService outboundApplicationService;
    @Autowired
    private WmsShipmentMapper wmsShipmentMapper;
    @Autowired
    private CarrierGateway carrierGateway;
    @Autowired
    private ChannelFulfillmentGateway amazonShipmentConfirmGateway;
    @Autowired
    private IdempotentService idempotentService;

    private long tenantId() { return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L; }
    private long factoryId() { return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L; }

    /** P1.6: 基于 WMS 出库单 SHIPPED 创建 tms_shipment，幂等 uk_source(tenant_id, factory_id, source_type, source_no) */
    @Transactional(rollbackFor = Exception.class)
    public Long createShipmentFromOutbound(Long outboundId) {
        String requestId = RequestIdContext.get();
        if (requestId == null || requestId.isBlank()) requestId = "tms-ship-" + outboundId + "-" + System.currentTimeMillis();
        return idempotentService.execute(requestId, "createTmsShipmentFromOutbound:" + outboundId, () -> {
            OutboundOrder ob = outboundApplicationService.getOutbound(outboundId);
            if (!OutboundOrder.STATUS_SHIPPED.equals(ob.getStatus())) {
                throw new BusinessException("Outbound must be SHIPPED to create TMS shipment. Current: " + ob.getStatus());
            }
            String sourceType = Shipment.SOURCE_TYPE_WMS_OUTBOUND;
            String sourceNo = ob.getOutboundNo();
            Shipment existing = shipmentMapper.selectOne(new LambdaQueryWrapper<Shipment>()
                .eq(Shipment::getTenantId, tenantId()).eq(Shipment::getFactoryId, factoryId())
                .eq(Shipment::getSourceType, sourceType).eq(Shipment::getSourceNo, sourceNo).eq(Shipment::getDeleted, 0));
            if (existing != null) return existing.getId();

            Shipment ship = new Shipment();
            ship.setShipmentNo("TMS-" + sourceNo + "-" + System.currentTimeMillis());
            ship.setSourceType(sourceType);
            ship.setSourceNo(sourceNo);
            ship.setOutboundId(ob.getId());
            ship.setFulfillmentId(ob.getFulfillmentId());
            ship.setStatus(Shipment.STATUS_CREATED);
            ship.setTraceId(TraceIdUtils.getTraceId());

            WmsShipment wmsShip = wmsShipmentMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WmsShipment>()
                .eq(WmsShipment::getOutboundId, outboundId).eq(WmsShipment::getDeleted, 0).last("LIMIT 1"));
            if (wmsShip != null) {
                ship.setCarrierCode(wmsShip.getCarrierCode());
                ship.setTrackingNo(wmsShip.getTrackingNo());
            }
            shipmentMapper.insert(ship);
            return ship.getId();
        });
    }

    /** P1.6: 调承运商创建面单，回写 trackingNo/labelUrl，状态 -> LABEL_CREATED；失败则 -> FAILED 供重试 */
    @Transactional(rollbackFor = Exception.class)
    public void createLabel(Long shipmentId) {
        Shipment ship = getShipment(shipmentId);
        if (!Shipment.STATUS_CREATED.equals(ship.getStatus()) && !Shipment.STATUS_FAILED.equals(ship.getStatus())) {
            throw new BusinessException("Shipment must be CREATED or FAILED to create label. Current: " + ship.getStatus());
        }
        String carrierCode = ship.getCarrierCode() != null ? ship.getCarrierCode() : "MOCK";
        String serviceCode = ship.getServiceCode();

        CreateLabelRequest req = new CreateLabelRequest();
        req.setReferenceNo(ship.getSourceNo());
        req.setShipToCountry("US");
        CreateLabelResult result;
        try {
            result = carrierGateway.createLabel(carrierCode, serviceCode, req);
        } catch (Exception e) {
            ship.markFailed(e.getMessage());
            shipmentMapper.update(null, new LambdaUpdateWrapper<Shipment>()
                .eq(Shipment::getId, shipmentId).eq(Shipment::getDeleted, 0)
                .set(Shipment::getStatus, ship.getStatus()).set(Shipment::getFailReason, ship.getFailReason()).setSql("version = version + 1"));
            throw new BusinessException("Create label failed: " + e.getMessage());
        }

        ship.markLabelCreated(result.getTrackingNo(), result.getLabelUrl());
        if (result.getLabelFormat() != null) ship.setLabelFormat(result.getLabelFormat());
        int rows = shipmentMapper.update(null, new LambdaUpdateWrapper<Shipment>()
            .eq(Shipment::getId, shipmentId).eq(Shipment::getDeleted, 0)
            .set(Shipment::getStatus, ship.getStatus()).set(Shipment::getTrackingNo, ship.getTrackingNo())
            .set(Shipment::getLabelUrl, ship.getLabelUrl()).set(Shipment::getLabelFormat, ship.getLabelFormat())
            .set(Shipment::getFailReason, null).setSql("version = version + 1"));
        if (rows == 0) throw new BusinessException("Concurrent update shipment");
    }

    /** P1.6: 交接（可选），状态 -> HANDED_OVER */
    @Transactional(rollbackFor = Exception.class)
    public void handover(Long shipmentId) {
        Shipment ship = getShipment(shipmentId);
        ship.markHandedOver();
        shipmentMapper.update(null, new LambdaUpdateWrapper<Shipment>()
            .eq(Shipment::getId, shipmentId).eq(Shipment::getDeleted, 0)
            .set(Shipment::getStatus, ship.getStatus()).setSql("version = version + 1"));
    }

    /** P1.6: 回传平台（Amazon 2026），状态 -> TRACKING_SYNCED */
    @Transactional(rollbackFor = Exception.class)
    public void syncToChannel(Long shipmentId) {
        Shipment ship = getShipment(shipmentId);
        if (!Shipment.STATUS_LABEL_CREATED.equals(ship.getStatus()) && !Shipment.STATUS_HANDED_OVER.equals(ship.getStatus())) {
            throw new BusinessException("Shipment must be LABEL_CREATED or HANDED_OVER to sync. Current: " + ship.getStatus());
        }
        String channelCode = "AMAZON";
        String platformOrderId = ship.getSourceNo();
        if (ship.getFulfillmentId() != null) platformOrderId = String.valueOf(ship.getFulfillmentId());
        boolean ok = amazonShipmentConfirmGateway.syncShipmentToChannel(channelCode, platformOrderId,
            ship.getCarrierCode() != null ? ship.getCarrierCode() : "MOCK", ship.getTrackingNo(), null);
        if (!ok) {
            ship.markFailed("Sync to channel failed");
            shipmentMapper.update(null, new LambdaUpdateWrapper<Shipment>()
                .eq(Shipment::getId, shipmentId).eq(Shipment::getDeleted, 0)
                .set(Shipment::getStatus, ship.getStatus()).set(Shipment::getFailReason, ship.getFailReason()).setSql("version = version + 1"));
            throw new BusinessException("Sync to channel failed");
        }
        ship.markTrackingSynced();
        shipmentMapper.update(null, new LambdaUpdateWrapper<Shipment>()
            .eq(Shipment::getId, shipmentId).eq(Shipment::getDeleted, 0)
            .set(Shipment::getStatus, ship.getStatus()).setSql("version = version + 1"));
    }

    public Shipment getShipment(Long id) {
        Shipment s = shipmentMapper.selectById(id);
        if (s == null || !Long.valueOf(tenantId()).equals(s.getTenantId()) || !Long.valueOf(factoryId()).equals(s.getFactoryId()) || (s.getDeleted() != null && s.getDeleted() != 0)) {
            throw new BusinessException("Shipment not found: " + id);
        }
        return s;
    }

    public IPage<Shipment> page(Page<Shipment> page, String status) {
        LambdaQueryWrapper<Shipment> q = new LambdaQueryWrapper<>();
        q.eq(Shipment::getTenantId, tenantId()).eq(Shipment::getFactoryId, factoryId()).eq(Shipment::getDeleted, 0);
        if (status != null && !status.isBlank()) q.eq(Shipment::getStatus, status);
        q.orderByDesc(Shipment::getId);
        return shipmentMapper.selectPage(page, q);
    }

    /** 扫描 CREATED/FAILED 用于 XXL-JOB 重试 createLabel */
    public List<Shipment> listForCreateLabelRetry(int limit) {
        return shipmentMapper.selectList(new LambdaQueryWrapper<Shipment>()
            .eq(Shipment::getTenantId, tenantId()).eq(Shipment::getFactoryId, factoryId()).eq(Shipment::getDeleted, 0)
            .in(Shipment::getStatus, Shipment.STATUS_CREATED, Shipment.STATUS_FAILED).orderByAsc(Shipment::getId).last("LIMIT " + Math.min(limit, 100)));
    }

    /** 扫描 LABEL_CREATED/HANDED_OVER 用于 XXL-JOB 重试 syncToChannel */
    public List<Shipment> listForSyncToChannelRetry(int limit) {
        return shipmentMapper.selectList(new LambdaQueryWrapper<Shipment>()
            .eq(Shipment::getTenantId, tenantId()).eq(Shipment::getFactoryId, factoryId()).eq(Shipment::getDeleted, 0)
            .in(Shipment::getStatus, Shipment.STATUS_LABEL_CREATED, Shipment.STATUS_HANDED_OVER).orderByAsc(Shipment::getId).last("LIMIT " + Math.min(limit, 100)));
    }
}
