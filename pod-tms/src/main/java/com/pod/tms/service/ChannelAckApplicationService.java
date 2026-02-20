package com.pod.tms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.common.utils.TraceIdUtils;
import com.pod.infra.context.RequestIdContext;
import com.pod.infra.idempotent.service.IdempotentService;
import com.pod.oms.domain.Fulfillment;
import com.pod.oms.domain.FulfillmentItem;
import com.pod.oms.domain.OrderHold;
import com.pod.oms.domain.UnifiedOrder;
import com.pod.oms.domain.UnifiedOrderItem;
import com.pod.oms.mapper.FulfillmentItemMapper;
import com.pod.oms.mapper.FulfillmentMapper;
import com.pod.oms.mapper.OrderHoldMapper;
import com.pod.oms.mapper.UnifiedOrderItemMapper;
import com.pod.oms.mapper.UnifiedOrderMapper;
import com.pod.tms.domain.ChannelShipmentAck;
import com.pod.tms.gateway.AmazonSpApiGateway;
import com.pod.tms.gateway.ConfirmShipmentRequest;
import com.pod.tms.gateway.ConfirmShipmentResult;
import com.pod.tms.mapper.ChannelShipmentAckMapper;
import com.pod.wms.domain.OutboundOrder;
import com.pod.wms.domain.WmsShipment;
import com.pod.wms.mapper.WmsShipmentMapper;
import com.pod.wms.service.OutboundApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 渠道发货回传应用服务。基于 WMS 出库/发货创建 ack，调 Amazon confirmShipment，状态机与重试。
 */
@Service
public class ChannelAckApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ChannelAckApplicationService.class);
    private static final String CHANNEL_AMAZON = "AMAZON";
    private static final String PACKAGE_REF_DEFAULT = "1";
    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private ChannelShipmentAckMapper ackMapper;
    @Autowired
    private OutboundApplicationService outboundApplicationService;
    @Autowired
    private WmsShipmentMapper wmsShipmentMapper;
    @Autowired
    private FulfillmentMapper fulfillmentMapper;
    @Autowired
    private FulfillmentItemMapper fulfillmentItemMapper;
    @Autowired
    private UnifiedOrderMapper unifiedOrderMapper;
    @Autowired
    private UnifiedOrderItemMapper unifiedOrderItemMapper;
    @Autowired
    private OrderHoldMapper orderHoldMapper;
    @Autowired
    private AmazonSpApiGateway amazonSpApiGateway;
    @Autowired
    private IdempotentService idempotentService;

    private long tenantId() { return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L; }
    private long factoryId() { return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L; }

    /** 幂等：按 uk_ack 创建回传任务；从 outbound -> wms_shipment -> fulfillment -> order 组装 payload。 */
    @Transactional(rollbackFor = Exception.class)
    public Long createAckFromOutbound(Long outboundId) {
        String requestId = RequestIdContext.get();
        if (requestId == null || requestId.isBlank()) requestId = "ack-outbound-" + outboundId + "-" + System.currentTimeMillis();
        return idempotentService.execute(requestId, "createChannelAck:" + outboundId, () -> {
            OutboundOrder outbound = outboundApplicationService.getOutbound(outboundId);
            if (!OutboundOrder.STATUS_SHIPPED.equals(outbound.getStatus())) {
                throw new BusinessException("Outbound must be SHIPPED. Current: " + outbound.getStatus());
            }
            WmsShipment wmsShip = wmsShipmentMapper.selectOne(new LambdaQueryWrapper<WmsShipment>()
                .eq(WmsShipment::getOutboundId, outboundId).eq(WmsShipment::getDeleted, 0).last("LIMIT 1"));
            if (wmsShip == null) {
                throw new BusinessException("WMS shipment not found for outbound: " + outboundId);
            }
            Fulfillment fulfillment = null;
            UnifiedOrder order = null;
            List<FulfillmentItem> ffItems = new ArrayList<>();
            if (outbound.getFulfillmentId() != null) {
                fulfillment = fulfillmentMapper.selectById(outbound.getFulfillmentId());
                if (fulfillment != null && fulfillment.getDeleted() != null && fulfillment.getDeleted() == 0) {
                    order = unifiedOrderMapper.selectById(fulfillment.getUnifiedOrderId());
                    if (order != null && !Objects.equals(order.getTenantId(), tenantId())) order = null;
                    if (order != null) {
                        ffItems = fulfillmentItemMapper.selectList(new LambdaQueryWrapper<FulfillmentItem>()
                            .eq(FulfillmentItem::getFulfillmentId, fulfillment.getId()).eq(FulfillmentItem::getDeleted, 0));
                    }
                }
            }
            if (order == null) {
                throw new BusinessException("Unified order not found for outbound fulfillment: " + outbound.getFulfillmentId());
            }
            String amazonOrderId = order.getExternalOrderId() != null ? order.getExternalOrderId() : order.getPlatformOrderId();
            if (amazonOrderId == null || amazonOrderId.isBlank()) {
                throw new BusinessException("Order has no externalOrderId for Amazon ack");
            }
            String packageRef = PACKAGE_REF_DEFAULT;
            ChannelShipmentAck existing = ackMapper.selectOne(new LambdaQueryWrapper<ChannelShipmentAck>()
                .eq(ChannelShipmentAck::getTenantId, tenantId()).eq(ChannelShipmentAck::getFactoryId, factoryId())
                .eq(ChannelShipmentAck::getChannel, CHANNEL_AMAZON).eq(ChannelShipmentAck::getAmazonOrderId, amazonOrderId)
                .eq(ChannelShipmentAck::getPackageReferenceId, packageRef).eq(ChannelShipmentAck::getDeleted, 0));
            if (existing != null) return existing.getId();

            LocalDateTime shippedAt = wmsShip.getShippedAt();
            Instant shipDateUtc = shippedAt != null ? shippedAt.atZone(ZoneId.systemDefault()).toInstant() : Instant.now().minusSeconds(10);
            Instant nowUtc = Instant.now();
            if (shipDateUtc.isAfter(nowUtc.minusSeconds(2))) shipDateUtc = nowUtc.minusSeconds(2);
            if (order.getOrderPurchaseDateUtc() != null) {
                Instant orderPurchase = order.getOrderPurchaseDateUtc().atZone(ZoneId.systemDefault()).toInstant();
                if (shipDateUtc.isBefore(orderPurchase)) shipDateUtc = orderPurchase;
            }

            String marketplaceId = order.getMarketplaceId() != null ? order.getMarketplaceId() : "ATVPDKIKX0DER";
            String carrierCode = wmsShip.getCarrierCode() != null ? wmsShip.getCarrierCode() : "Other";
            String carrierName = carrierCode;
            if ("Other".equals(carrierCode)) carrierName = wmsShip.getCarrierCode() != null ? wmsShip.getCarrierCode() : "Other";

            List<ConfirmShipmentRequest.OrderItem> orderItems = new ArrayList<>();
            for (FulfillmentItem fi : ffItems) {
                UnifiedOrderItem oi = unifiedOrderItemMapper.selectById(fi.getUnifiedOrderItemId());
                if (oi == null) continue;
                String orderItemId = oi.getExternalOrderItemId();
                if (orderItemId == null || orderItemId.isBlank()) orderItemId = String.valueOf(oi.getId());
                int qty = fi.getReservedQty() != null ? fi.getReservedQty() : (fi.getQty() != null ? fi.getQty() : 0);
                if (qty <= 0) continue;
                ConfirmShipmentRequest.OrderItem item = new ConfirmShipmentRequest.OrderItem();
                item.setOrderItemId(orderItemId);
                item.setQuantity(qty);
                orderItems.add(item);
            }
            if (orderItems.isEmpty()) {
                throw new BusinessException("No order items for confirmShipment");
            }

            ChannelShipmentAck ack = new ChannelShipmentAck();
            ack.setChannel(CHANNEL_AMAZON);
            ack.setMarketplaceId(marketplaceId);
            ack.setShopId(order.getShopId());
            ack.setAmazonOrderId(amazonOrderId);
            ack.setExternalOrderId(amazonOrderId);
            ack.setPackageReferenceId(packageRef);
            ack.setCarrierCode(carrierCode);
            ack.setCarrierName(carrierName);
            ack.setShippingMethod("SHIPPING");
            ack.setTrackingNo(wmsShip.getTrackingNo());
            ack.setShipDateUtc(LocalDateTime.ofInstant(shipDateUtc, ZoneId.systemDefault()));
            ack.setStatus(ChannelShipmentAck.STATUS_CREATED);
            ack.setRetryCount(0);
            ack.setBusinessIdempotencyKey(ChannelShipmentAck.buildIdempotencyKey(CHANNEL_AMAZON, amazonOrderId, packageRef));
            ack.setWmsShipmentId(wmsShip.getId());
            ack.setOutboundId(outboundId);
            ack.setFulfillmentId(fulfillment != null ? fulfillment.getId() : null);
            ack.setUnifiedOrderId(order.getId());
            ack.setTraceId(TraceIdUtils.getTraceId());
            ackMapper.insert(ack);
            log.info("Channel ack created ackId={} orderId={} tracking={}", ack.getId(), amazonOrderId, ack.getTrackingNo());
            return ack.getId();
        });
    }

    /** 发送回传：乐观锁置 SENDING 后调网关，根据结果更新 SUCCESS / FAILED_RETRYABLE / FAILED_MANUAL；FAILED_MANUAL 时写 OMS hold。 */
    @Transactional(rollbackFor = Exception.class)
    public void sendAck(Long ackId) {
        ChannelShipmentAck ack = getAck(ackId);
        if (!ack.canRetry()) {
            throw new BusinessException("Ack cannot be sent: status=" + ack.getStatus() + " retryCount=" + ack.getRetryCount());
        }
        int rows = ackMapper.update(null, new LambdaUpdateWrapper<ChannelShipmentAck>()
            .eq(ChannelShipmentAck::getId, ackId).eq(ChannelShipmentAck::getDeleted, 0)
            .in(ChannelShipmentAck::getStatus, ChannelShipmentAck.STATUS_CREATED, ChannelShipmentAck.STATUS_FAILED_RETRYABLE)
            .eq(ChannelShipmentAck::getVersion, ack.getVersion())
            .set(ChannelShipmentAck::getStatus, ChannelShipmentAck.STATUS_SENDING)
            .set(ChannelShipmentAck::getLastAttemptAt, LocalDateTime.now())
            .setSql("version = version + 1"));
        if (rows == 0) {
            throw new BusinessException("Concurrent send or invalid status");
        }
        ack.setStatus(ChannelShipmentAck.STATUS_SENDING);
        ack.setVersion(ack.getVersion() == null ? 0 : ack.getVersion() + 1);
        ack.setLastAttemptAt(LocalDateTime.now());

        ConfirmShipmentRequest req = buildConfirmRequest(ack);
        String payloadJson = maskSensitive(req);
        ackMapper.update(null, new LambdaUpdateWrapper<ChannelShipmentAck>()
            .eq(ChannelShipmentAck::getId, ackId).set(ChannelShipmentAck::getRequestPayloadJson, payloadJson).setSql("version = version + 0"));

        log.info("Sending confirmShipment ackId={} orderId={} traceId={}", ackId, ack.getAmazonOrderId(), TraceIdUtils.getTraceId());
        ConfirmShipmentResult result;
        try {
            result = amazonSpApiGateway.confirmShipment(ack.getAmazonOrderId(), req);
        } catch (Exception e) {
            log.warn("confirmShipment exception ackId={}", ackId, e);
            result = ConfirmShipmentResult.fail(500, "InternalError", e.getMessage(), null);
        }

        if (result.isSuccess()) {
            ack.markSuccess(result.getHttpStatusCode(), result.getResponseBody());
            ackMapper.update(null, new LambdaUpdateWrapper<ChannelShipmentAck>()
                .eq(ChannelShipmentAck::getId, ackId).eq(ChannelShipmentAck::getStatus, ChannelShipmentAck.STATUS_SENDING)
                .set(ChannelShipmentAck::getStatus, ack.getStatus()).set(ChannelShipmentAck::getResponseCode, ack.getResponseCode())
                .set(ChannelShipmentAck::getResponseBody, ack.getResponseBody()).set(ChannelShipmentAck::getErrorCode, null).set(ChannelShipmentAck::getErrorMessage, null)
                .set(ChannelShipmentAck::getNextRetryAt, null).setSql("version = version + 1"));
            return;
        }
        int retryCount = (ack.getRetryCount() == null ? 0 : ack.getRetryCount()) + 1;
        if (result.isRetryable() && retryCount < ChannelShipmentAck.MAX_RETRY_COUNT) {
            LocalDateTime nextAt = ChannelShipmentAck.nextRetryAt(retryCount, LocalDateTime.now());
            ack.markFailedRetryable(result.getHttpStatusCode(), result.getErrorCode(), result.getErrorMessage(), nextAt);
            ackMapper.update(null, new LambdaUpdateWrapper<ChannelShipmentAck>()
                .eq(ChannelShipmentAck::getId, ackId).eq(ChannelShipmentAck::getStatus, ChannelShipmentAck.STATUS_SENDING)
                .set(ChannelShipmentAck::getStatus, ack.getStatus()).set(ChannelShipmentAck::getResponseCode, ack.getResponseCode())
                .set(ChannelShipmentAck::getResponseBody, result.getResponseBody()).set(ChannelShipmentAck::getErrorCode, ack.getErrorCode())
                .set(ChannelShipmentAck::getErrorMessage, ack.getErrorMessage()).set(ChannelShipmentAck::getNextRetryAt, ack.getNextRetryAt())
                .set(ChannelShipmentAck::getRetryCount, ack.getRetryCount()).setSql("version = version + 1"));
            return;
        }
        ack.markFailedManual(result.getHttpStatusCode(), result.getErrorCode(), result.getErrorMessage());
        ackMapper.update(null, new LambdaUpdateWrapper<ChannelShipmentAck>()
            .eq(ChannelShipmentAck::getId, ackId).eq(ChannelShipmentAck::getStatus, ChannelShipmentAck.STATUS_SENDING)
            .set(ChannelShipmentAck::getStatus, ack.getStatus()).set(ChannelShipmentAck::getResponseCode, ack.getResponseCode())
            .set(ChannelShipmentAck::getResponseBody, result.getResponseBody()).set(ChannelShipmentAck::getErrorCode, ack.getErrorCode())
            .set(ChannelShipmentAck::getErrorMessage, ack.getErrorMessage()).set(ChannelShipmentAck::getNextRetryAt, null)
            .set(ChannelShipmentAck::getRetryCount, ack.getRetryCount()).setSql("version = version + 1"));
        createOrderHoldForFailedManual(ack);
    }

    private void createOrderHoldForFailedManual(ChannelShipmentAck ack) {
        if (ack.getUnifiedOrderId() == null) return;
        String shopIdStr = ack.getShopId() != null ? String.valueOf(ack.getShopId()) : "";
        OrderHold existing = orderHoldMapper.selectOne(new LambdaQueryWrapper<OrderHold>()
            .eq(OrderHold::getTenantId, tenantId()).eq(OrderHold::getFactoryId, factoryId()).eq(OrderHold::getDeleted, 0)
            .eq(OrderHold::getHoldType, OrderHold.HOLD_TYPE_CHANNEL_ACK).eq(OrderHold::getChannel, CHANNEL_AMAZON)
            .eq(OrderHold::getExternalOrderId, ack.getAmazonOrderId()).eq(OrderHold::getStatus, OrderHold.STATUS_OPEN));
        if (existing != null) return;
        OrderHold hold = new OrderHold();
        hold.setHoldType(OrderHold.HOLD_TYPE_CHANNEL_ACK);
        hold.setStatus(OrderHold.STATUS_OPEN);
        hold.setReasonCode("AMZ_CONFIRM_SHIPMENT_FAILED");
        hold.setReasonMsg(ack.getErrorMessage() != null ? ack.getErrorMessage().substring(0, Math.min(512, ack.getErrorMessage().length())) : "Confirm shipment failed");
        hold.setChannel(CHANNEL_AMAZON);
        hold.setShopId(shopIdStr);
        hold.setExternalOrderId(ack.getAmazonOrderId());
        hold.setExternalSku("SHIP_CONFIRM");
        hold.setUnifiedOrderId(ack.getUnifiedOrderId());
        hold.setTraceId(TraceIdUtils.getTraceId());
        orderHoldMapper.insert(hold);
        log.info("Order hold created for ack failure orderId={} holdId={}", ack.getAmazonOrderId(), hold.getId());
    }

    private ConfirmShipmentRequest buildConfirmRequest(ChannelShipmentAck ack) {
        ConfirmShipmentRequest req = new ConfirmShipmentRequest();
        req.setMarketplaceId(ack.getMarketplaceId() != null ? ack.getMarketplaceId() : "ATVPDKIKX0DER");
        req.setCodCollectionMethod("");
        ConfirmShipmentRequest.PackageDetail pkg = new ConfirmShipmentRequest.PackageDetail();
        pkg.setPackageReferenceId(ack.getPackageReferenceId());
        pkg.setCarrierCode(ack.getCarrierCode() != null ? ack.getCarrierCode() : "Other");
        pkg.setCarrierName(ack.getCarrierName() != null ? ack.getCarrierName() : "Other");
        pkg.setShippingMethod(ack.getShippingMethod() != null ? ack.getShippingMethod() : "SHIPPING");
        pkg.setTrackingNumber(ack.getTrackingNo());
        if (ack.getShipDateUtc() != null) {
            pkg.setShipDate(ack.getShipDateUtc().atZone(ZoneId.systemDefault()).toInstant());
        } else {
            pkg.setShipDate(Instant.now().minusSeconds(5));
        }
        List<ConfirmShipmentRequest.OrderItem> items = new ArrayList<>();
        Long fulfillmentId = ack.getFulfillmentId();
        if (fulfillmentId == null && ack.getUnifiedOrderId() != null) {
            Fulfillment f = fulfillmentMapper.selectOne(new LambdaQueryWrapper<Fulfillment>()
                .eq(Fulfillment::getUnifiedOrderId, ack.getUnifiedOrderId()).eq(Fulfillment::getDeleted, 0).last("LIMIT 1"));
            if (f != null) fulfillmentId = f.getId();
        }
        List<FulfillmentItem> ffItems = fulfillmentId != null ? fulfillmentItemMapper.selectList(new LambdaQueryWrapper<FulfillmentItem>()
            .eq(FulfillmentItem::getFulfillmentId, fulfillmentId).eq(FulfillmentItem::getDeleted, 0)) : new ArrayList<>();
        for (FulfillmentItem fi : ffItems) {
            UnifiedOrderItem oi = unifiedOrderItemMapper.selectById(fi.getUnifiedOrderItemId());
            if (oi == null) continue;
            String orderItemId = oi.getExternalOrderItemId() != null ? oi.getExternalOrderItemId() : String.valueOf(oi.getId());
            int qty = fi.getReservedQty() != null ? fi.getReservedQty() : (fi.getQty() != null ? fi.getQty() : 0);
            if (qty <= 0) continue;
            ConfirmShipmentRequest.OrderItem item = new ConfirmShipmentRequest.OrderItem();
            item.setOrderItemId(orderItemId);
            item.setQuantity(qty);
            items.add(item);
        }
        if (items.isEmpty()) {
            UnifiedOrderItem oi = unifiedOrderItemMapper.selectOne(new LambdaQueryWrapper<UnifiedOrderItem>()
                .eq(UnifiedOrderItem::getUnifiedOrderId, ack.getUnifiedOrderId()).eq(UnifiedOrderItem::getDeleted, 0).last("LIMIT 1"));
            if (oi != null) {
                ConfirmShipmentRequest.OrderItem item = new ConfirmShipmentRequest.OrderItem();
                item.setOrderItemId(oi.getExternalOrderItemId() != null ? oi.getExternalOrderItemId() : String.valueOf(oi.getId()));
                item.setQuantity(oi.getQuantity() != null ? oi.getQuantity() : 1);
                items.add(item);
            }
        }
        pkg.setOrderItems(items);
        req.setPackageDetail(pkg);
        return req;
    }

    private String maskSensitive(ConfirmShipmentRequest req) {
        try {
            return JSON.writeValueAsString(req);
        } catch (Exception e) {
            return "{}";
        }
    }

    public void manualRetry(Long ackId) {
        sendAck(ackId);
    }

    public ChannelShipmentAck getAck(Long id) {
        ChannelShipmentAck a = ackMapper.selectById(id);
        if (a == null || !Objects.equals(a.getTenantId(), tenantId()) || !Objects.equals(a.getFactoryId(), factoryId()) || (a.getDeleted() != null && a.getDeleted() != 0)) {
            throw new BusinessException("Channel ack not found: " + id);
        }
        return a;
    }

    public IPage<ChannelShipmentAck> page(Page<ChannelShipmentAck> page, String channel, String status, String orderId, String trackingNo) {
        LambdaQueryWrapper<ChannelShipmentAck> q = new LambdaQueryWrapper<>();
        q.eq(ChannelShipmentAck::getTenantId, tenantId()).eq(ChannelShipmentAck::getFactoryId, factoryId()).eq(ChannelShipmentAck::getDeleted, 0);
        if (channel != null && !channel.isBlank()) q.eq(ChannelShipmentAck::getChannel, channel);
        if (status != null && !status.isBlank()) q.eq(ChannelShipmentAck::getStatus, status);
        if (orderId != null && !orderId.isBlank()) q.eq(ChannelShipmentAck::getAmazonOrderId, orderId);
        if (trackingNo != null && !trackingNo.isBlank()) q.eq(ChannelShipmentAck::getTrackingNo, trackingNo);
        q.orderByDesc(ChannelShipmentAck::getId);
        return ackMapper.selectPage(page, q);
    }

    /** 扫描 CREATED 或 FAILED_RETRYABLE 且 next_retry_at <= now，用于 XXL-JOB */
    public List<ChannelShipmentAck> listForRetry(int limit) {
        LocalDateTime now = LocalDateTime.now();
        return ackMapper.selectList(new LambdaQueryWrapper<ChannelShipmentAck>()
            .eq(ChannelShipmentAck::getTenantId, tenantId()).eq(ChannelShipmentAck::getFactoryId, factoryId()).eq(ChannelShipmentAck::getDeleted, 0)
            .in(ChannelShipmentAck::getStatus, ChannelShipmentAck.STATUS_CREATED, ChannelShipmentAck.STATUS_FAILED_RETRYABLE)
            .and(w -> w.isNull(ChannelShipmentAck::getNextRetryAt).or().le(ChannelShipmentAck::getNextRetryAt, now))
            .and(w -> w.isNull(ChannelShipmentAck::getRetryCount).or().lt(ChannelShipmentAck::getRetryCount, ChannelShipmentAck.MAX_RETRY_COUNT))
            .orderByAsc(ChannelShipmentAck::getId).last("LIMIT " + Math.min(limit, 200)));
    }
}
