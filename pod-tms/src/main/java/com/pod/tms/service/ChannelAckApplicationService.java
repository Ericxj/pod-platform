package com.pod.tms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
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
import com.pod.tms.gateway.AmazonOrderItemsApiException;
import com.pod.tms.gateway.AmazonSpApiGateway;
import com.pod.tms.gateway.ConfirmShipmentRequest;
import com.pod.tms.gateway.ConfirmShipmentResult;
import com.pod.tms.gateway.GetOrderItemsResult;
import com.pod.tms.mapper.ChannelShipmentAckMapper;
import com.pod.wms.domain.OutboundOrder;
import com.pod.wms.domain.PackOrder;
import com.pod.wms.domain.PackOrderLine;
import com.pod.wms.domain.WmsShipment;
import com.pod.wms.mapper.PackOrderLineMapper;
import com.pod.wms.mapper.PackOrderMapper;
import com.pod.wms.mapper.WmsShipmentMapper;
import com.pod.wms.service.OutboundApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private PackOrderMapper packOrderMapper;
    @Autowired
    private PackOrderLineMapper packOrderLineMapper;
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
    private AmazonOrderQueryService amazonOrderQueryService;
    @Autowired
    private IdempotentService idempotentService;

    private long tenantId() { return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L; }
    private long factoryId() { return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L; }

    /** 幂等：按 uk_ack 创建回传任务。P1.6++ C：按 pack 生成多条 ack（packageReferenceId 1,2,3...），单 pack 时退化为原一单一 ack。 */
    @Transactional(rollbackFor = Exception.class)
    public Long createAckFromOutbound(Long outboundId) {
        String requestId = RequestIdContext.get();
        if (requestId == null || requestId.isBlank()) requestId = "ack-outbound-" + outboundId + "-" + System.currentTimeMillis();
        return idempotentService.execute(requestId, "createChannelAck:" + outboundId, () -> {
            OutboundOrder outbound = outboundApplicationService.getOutbound(outboundId);
            if (!OutboundOrder.STATUS_SHIPPED.equals(outbound.getStatus())) {
                throw new BusinessException("Outbound must be SHIPPED. Current: " + outbound.getStatus());
            }
            List<WmsShipment> shipments = wmsShipmentMapper.selectList(new LambdaQueryWrapper<WmsShipment>()
                .eq(WmsShipment::getOutboundId, outboundId).eq(WmsShipment::getDeleted, 0));
            if (shipments == null || shipments.isEmpty()) {
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

            List<PackOrder> packs = packOrderMapper.selectList(new LambdaQueryWrapper<PackOrder>()
                .eq(PackOrder::getOutboundId, outboundId).eq(PackOrder::getDeleted, 0).orderByAsc(PackOrder::getId));
            if (packs == null) packs = new ArrayList<>();

            if (packs.isEmpty()) {
                return createSingleAck(outboundId, shipments.get(0), fulfillment, order, ffItems, amazonOrderId);
            }

            Long firstAckId = null;
            for (int i = 0; i < packs.size(); i++) {
                PackOrder pack = packs.get(i);
                String packageRef = String.valueOf(i + 1);
                ChannelShipmentAck existing = ackMapper.selectOne(new LambdaQueryWrapper<ChannelShipmentAck>()
                    .eq(ChannelShipmentAck::getTenantId, tenantId()).eq(ChannelShipmentAck::getFactoryId, factoryId())
                    .eq(ChannelShipmentAck::getChannel, CHANNEL_AMAZON).eq(ChannelShipmentAck::getAmazonOrderId, amazonOrderId)
                    .eq(ChannelShipmentAck::getPackageReferenceId, packageRef).eq(ChannelShipmentAck::getDeleted, 0));
                if (existing != null) {
                    if (firstAckId == null) firstAckId = existing.getId();
                    continue;
                }
                WmsShipment ship = resolveShipmentForPack(pack, shipments, packs.size());
                List<PackOrderLine> packLines = packOrderLineMapper.selectList(new LambdaQueryWrapper<PackOrderLine>()
                    .eq(PackOrderLine::getPackId, pack.getId()).eq(PackOrderLine::getDeleted, 0).orderByAsc(PackOrderLine::getLineNo));
                List<ConfirmShipmentRequest.OrderItem> packOrderItems = allocatePackLinesToOrderItems(packLines, ffItems);
                String orderItemsJson = serializeOrderItems(packOrderItems);

                LocalDateTime shippedAt = ship.getShippedAt();
                Instant shipDateUtc = shippedAt != null ? shippedAt.atOffset(ZoneOffset.UTC).toInstant() : Instant.now().minusSeconds(10);
                Instant nowUtc = Instant.now();
                if (shipDateUtc.isAfter(nowUtc.minusSeconds(2))) shipDateUtc = nowUtc.minusSeconds(2);
                if (order.getOrderPurchaseDateUtc() != null) {
                    Instant orderPurchase = order.getOrderPurchaseDateUtc().atOffset(ZoneOffset.UTC).toInstant();
                    if (shipDateUtc.isBefore(orderPurchase)) shipDateUtc = orderPurchase;
                }
                if (shipDateUtc.isAfter(nowUtc.minusSeconds(2))) shipDateUtc = nowUtc.minusSeconds(2);

                String marketplaceId = order.getMarketplaceId() != null ? order.getMarketplaceId() : "ATVPDKIKX0DER";
                String carrierCode = ship.getCarrierCode() != null ? ship.getCarrierCode() : "Other";
                String carrierName = "Other".equals(carrierCode) ? (ship.getCarrierCode() != null ? ship.getCarrierCode() : "Other") : null;

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
                ack.setTrackingNo(ship.getTrackingNo());
                ack.setShipDateUtc(LocalDateTime.ofInstant(shipDateUtc, ZoneOffset.UTC));
                ack.setStatus(ChannelShipmentAck.STATUS_CREATED);
                ack.setRetryCount(0);
                ack.setBusinessIdempotencyKey(ChannelShipmentAck.buildIdempotencyKey(CHANNEL_AMAZON, amazonOrderId, packageRef));
                ack.setWmsShipmentId(ship.getId());
                ack.setWmsPackId(pack.getId());
                ack.setOrderItemsJson(orderItemsJson);
                ack.setOutboundId(outboundId);
                ack.setFulfillmentId(fulfillment != null ? fulfillment.getId() : null);
                ack.setUnifiedOrderId(order.getId());
                ack.setTraceId(TraceIdUtils.getTraceId());
                ackMapper.insert(ack);
                if (firstAckId == null) firstAckId = ack.getId();
                log.info("Channel ack created ackId={} orderId={} packageRef={} tracking={}", ack.getId(), amazonOrderId, packageRef, ack.getTrackingNo());
            }
            return firstAckId != null ? firstAckId : createSingleAck(outboundId, shipments.get(0), fulfillment, order, ffItems, amazonOrderId);
        });
    }

    /** 无 pack 时：单 ack，packageReferenceId=1，与原逻辑一致 */
    private Long createSingleAck(Long outboundId, WmsShipment wmsShip, Fulfillment fulfillment, UnifiedOrder order,
                                List<FulfillmentItem> ffItems, String amazonOrderId) {
        String packageRef = PACKAGE_REF_DEFAULT;
        ChannelShipmentAck existing = ackMapper.selectOne(new LambdaQueryWrapper<ChannelShipmentAck>()
            .eq(ChannelShipmentAck::getTenantId, tenantId()).eq(ChannelShipmentAck::getFactoryId, factoryId())
            .eq(ChannelShipmentAck::getChannel, CHANNEL_AMAZON).eq(ChannelShipmentAck::getAmazonOrderId, amazonOrderId)
            .eq(ChannelShipmentAck::getPackageReferenceId, packageRef).eq(ChannelShipmentAck::getDeleted, 0));
        if (existing != null) return existing.getId();

        LocalDateTime shippedAt = wmsShip.getShippedAt();
        Instant shipDateUtc = shippedAt != null ? shippedAt.atOffset(ZoneOffset.UTC).toInstant() : Instant.now().minusSeconds(10);
        Instant nowUtc = Instant.now();
        if (shipDateUtc.isAfter(nowUtc.minusSeconds(2))) shipDateUtc = nowUtc.minusSeconds(2);
        if (order.getOrderPurchaseDateUtc() != null) {
            Instant orderPurchase = order.getOrderPurchaseDateUtc().atOffset(ZoneOffset.UTC).toInstant();
            if (shipDateUtc.isBefore(orderPurchase)) shipDateUtc = orderPurchase;
        }
        if (shipDateUtc.isAfter(nowUtc.minusSeconds(2))) shipDateUtc = nowUtc.minusSeconds(2);

        String marketplaceId = order.getMarketplaceId() != null ? order.getMarketplaceId() : "ATVPDKIKX0DER";
        String carrierCode = wmsShip.getCarrierCode() != null ? wmsShip.getCarrierCode() : "Other";
        String carrierName = "Other".equals(carrierCode) ? (wmsShip.getCarrierCode() != null ? wmsShip.getCarrierCode() : "Other") : null;

        List<ConfirmShipmentRequest.OrderItem> orderItems = new ArrayList<>();
        for (FulfillmentItem fi : ffItems) {
            UnifiedOrderItem oi = unifiedOrderItemMapper.selectById(fi.getUnifiedOrderItemId());
            if (oi == null) continue;
            String orderItemId = oi.getExternalOrderItemId();
            if (orderItemId == null || orderItemId.isBlank()) continue;
            int qty = fi.getReservedQty() != null ? fi.getReservedQty() : (fi.getQty() != null ? fi.getQty() : 0);
            if (qty <= 0) continue;
            ConfirmShipmentRequest.OrderItem item = new ConfirmShipmentRequest.OrderItem();
            item.setOrderItemId(orderItemId);
            item.setQuantity(qty);
            orderItems.add(item);
        }
        if (orderItems.isEmpty()) {
            log.info("createAckFromOutbound all items missing external_order_item_id orderId={} outboundId={} (P1.6+ will backfill on sendAck)", amazonOrderId, outboundId);
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
        ack.setShipDateUtc(LocalDateTime.ofInstant(shipDateUtc, ZoneOffset.UTC));
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
    }

    private WmsShipment resolveShipmentForPack(PackOrder pack, List<WmsShipment> shipments, int packCount) {
        if (shipments.size() == 1) return shipments.get(0);
        for (WmsShipment s : shipments) {
            if (pack.getId() != null && Objects.equals(s.getPackId(), pack.getId())) return s;
        }
        return shipments.get(0);
    }

    /** 按 pack_line(sku_id,qty) 分摊到 fulfillment items，返回 (orderItemId, quantity) 列表；同 orderItemId 合并。 */
    private List<ConfirmShipmentRequest.OrderItem> allocatePackLinesToOrderItems(List<PackOrderLine> packLines,
                                                                                   List<FulfillmentItem> ffItems) {
        if (packLines == null || packLines.isEmpty()) return new ArrayList<>();
        List<FulfillmentItem> sortedFi = ffItems.stream().sorted(Comparator.comparing(FulfillmentItem::getLineNo, Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList());
        Map<Long, Integer> remaining = new LinkedHashMap<>();
        Map<Long, String> fiIdToOrderItemId = new LinkedHashMap<>();
        for (FulfillmentItem fi : sortedFi) {
            int q = fi.getReservedQty() != null ? fi.getReservedQty() : (fi.getQty() != null ? fi.getQty() : 0);
            remaining.put(fi.getId(), q);
            UnifiedOrderItem oi = unifiedOrderItemMapper.selectById(fi.getUnifiedOrderItemId());
            if (oi != null && oi.getExternalOrderItemId() != null && !oi.getExternalOrderItemId().isBlank()) {
                fiIdToOrderItemId.put(fi.getId(), oi.getExternalOrderItemId());
            }
        }
        Map<String, Integer> orderItemQty = new LinkedHashMap<>();
        for (PackOrderLine pl : packLines.stream().sorted(Comparator.comparing(PackOrderLine::getLineNo, Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList())) {
            int need = pl.getQty() != null ? pl.getQty() : 0;
            if (need <= 0) continue;
            Long skuId = pl.getSkuId();
            for (FulfillmentItem fi : sortedFi) {
                if (need <= 0) break;
                if (!Objects.equals(fi.getSkuId(), skuId)) continue;
                int rem = remaining.getOrDefault(fi.getId(), 0);
                if (rem <= 0) continue;
                int assign = Math.min(need, rem);
                String oid = fiIdToOrderItemId.get(fi.getId());
                if (oid != null) {
                    orderItemQty.merge(oid, assign, (a, b) -> (a == null ? 0 : a) + (b == null ? 0 : b));
                }
                remaining.put(fi.getId(), rem - assign);
                need -= assign;
            }
        }
        List<ConfirmShipmentRequest.OrderItem> out = new ArrayList<>();
        for (Map.Entry<String, Integer> e : orderItemQty.entrySet()) {
            if (e.getValue() <= 0) continue;
            ConfirmShipmentRequest.OrderItem item = new ConfirmShipmentRequest.OrderItem();
            item.setOrderItemId(e.getKey());
            item.setQuantity(e.getValue());
            out.add(item);
        }
        return out;
    }

    private String serializeOrderItems(List<ConfirmShipmentRequest.OrderItem> items) {
        try {
            return JSON.writeValueAsString(items);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<ConfirmShipmentRequest.OrderItem> parseOrderItemsJson(String json) {
        try {
            return JSON.readValue(json, new TypeReference<List<ConfirmShipmentRequest.OrderItem>>() {});
        } catch (Exception e) {
            log.warn("parseOrderItemsJson failed json={}", json != null && json.length() > 200 ? json.substring(0, 200) : json, e);
            return new ArrayList<>();
        }
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

        Long fulfillmentId = ack.getFulfillmentId();
        if (fulfillmentId == null && ack.getUnifiedOrderId() != null) {
            Fulfillment f = fulfillmentMapper.selectOne(new LambdaQueryWrapper<Fulfillment>()
                .eq(Fulfillment::getUnifiedOrderId, ack.getUnifiedOrderId()).eq(Fulfillment::getDeleted, 0).last("LIMIT 1"));
            if (f != null) fulfillmentId = f.getId();
        }
        try {
            amazonOrderQueryService.getOrderItemsAndBackfill(ack.getAmazonOrderId(), ack.getUnifiedOrderId(), fulfillmentId, ack.getMarketplaceId(), ackId);
        } catch (AmazonOrderItemsApiException e) {
            GetOrderItemsResult res = e.getResult();
            if (res == null) res = GetOrderItemsResult.fail(500, "Unknown", e.getMessage(), null);
            ConfirmShipmentResult result = ConfirmShipmentResult.fail(res.getHttpStatusCode(), res.getErrorCode(), res.getErrorMessage(), res.getResponseBody());
            applyFailureAndHold(ackId, ack, result);
            return;
        } catch (BusinessException be) {
            if ("Amazon orderItemId not matched".equals(be.getMessage())) {
                ack.markFailedManual(500, "OrderItemNotMatched", be.getMessage());
                ackMapper.update(null, new LambdaUpdateWrapper<ChannelShipmentAck>()
                    .eq(ChannelShipmentAck::getId, ackId).eq(ChannelShipmentAck::getStatus, ChannelShipmentAck.STATUS_SENDING)
                    .set(ChannelShipmentAck::getStatus, ack.getStatus()).set(ChannelShipmentAck::getResponseCode, ack.getResponseCode())
                    .set(ChannelShipmentAck::getErrorCode, ack.getErrorCode()).set(ChannelShipmentAck::getErrorMessage, ack.getErrorMessage())
                    .set(ChannelShipmentAck::getNextRetryAt, null).set(ChannelShipmentAck::getRetryCount, ack.getRetryCount()).setSql("version = version + 1"));
                createOrderHoldForFailedManual(ack, "AMZ_ORDER_ITEM_NOT_MATCHED");
                return;
            }
            throw be;
        }

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
        if (result.getHttpStatusCode() == 404) {
            handle404AndRetryOrManual(ackId, ack, result);
            return;
        }
        if (result.getHttpStatusCode() == 400 && !Boolean.TRUE.equals(ack.getSelfHealAttempted())) {
            ConfirmShipmentSelfHealStrategy strategy = ConfirmShipmentSelfHealStrategy.classify(
                result.getHttpStatusCode(), result.getResponseBody(), result.getErrorMessage());
            if (strategy != ConfirmShipmentSelfHealStrategy.UNKNOWN && strategy.getActionCode() != null) {
                LocalDateTime now = LocalDateTime.now();
                applySelfHealFix(ackId, ack, strategy, now);
                ack.markSelfHeal(strategy.getActionCode(), now);
                ackMapper.update(null, new LambdaUpdateWrapper<ChannelShipmentAck>()
                    .eq(ChannelShipmentAck::getId, ackId)
                    .set(ChannelShipmentAck::getSelfHealAttempted, true)
                    .set(ChannelShipmentAck::getSelfHealAction, strategy.getActionCode())
                    .set(ChannelShipmentAck::getSelfHealAt, now)
                    .set(ChannelShipmentAck::getShipDateUtc, ack.getShipDateUtc())
                    .set(ChannelShipmentAck::getCarrierCode, ack.getCarrierCode())
                    .set(ChannelShipmentAck::getCarrierName, ack.getCarrierName())
                    .set(ChannelShipmentAck::getOrderItemsJson, ack.getOrderItemsJson())
                    .setSql("version = version + 0"));
                ConfirmShipmentRequest retryReq = buildConfirmRequest(ack);
                log.info("Self-heal retry ackId={} action={}", ackId, strategy.getActionCode());
                try {
                    ConfirmShipmentResult retryResult = amazonSpApiGateway.confirmShipment(ack.getAmazonOrderId(), retryReq);
                    if (retryResult.isSuccess()) {
                        ack.markSuccess(retryResult.getHttpStatusCode(), retryResult.getResponseBody());
                        ackMapper.update(null, new LambdaUpdateWrapper<ChannelShipmentAck>()
                            .eq(ChannelShipmentAck::getId, ackId).eq(ChannelShipmentAck::getStatus, ChannelShipmentAck.STATUS_SENDING)
                            .set(ChannelShipmentAck::getStatus, ack.getStatus()).set(ChannelShipmentAck::getResponseCode, ack.getResponseCode())
                            .set(ChannelShipmentAck::getResponseBody, ack.getResponseBody()).set(ChannelShipmentAck::getErrorCode, null).set(ChannelShipmentAck::getErrorMessage, null)
                            .set(ChannelShipmentAck::getNextRetryAt, null).setSql("version = version + 1"));
                        return;
                    }
                } catch (Exception e) {
                    log.warn("Self-heal retry exception ackId={}", ackId, e);
                }
            }
        }
        applyFailureAndHold(ackId, ack, result);
    }

    private void handle404AndRetryOrManual(Long ackId, ChannelShipmentAck ack, ConfirmShipmentResult result) {
        int count404 = (ack.getRetry404Count() == null ? 0 : ack.getRetry404Count()) + 1;
        ack.setRetry404Count(count404);
        if (count404 < ChannelShipmentAck.MAX_RETRY_404) {
            LocalDateTime nextAt = LocalDateTime.now().plusMinutes(30);
            ack.setStatus(ChannelShipmentAck.STATUS_FAILED_RETRYABLE);
            ack.setResponseCode(result.getHttpStatusCode());
            ack.setErrorCode(result.getErrorCode());
            ack.setErrorMessage(result.getErrorMessage());
            ack.setResponseBody(result.getResponseBody());
            ack.setNextRetryAt(nextAt);
            ackMapper.update(null, new LambdaUpdateWrapper<ChannelShipmentAck>()
                .eq(ChannelShipmentAck::getId, ackId).eq(ChannelShipmentAck::getStatus, ChannelShipmentAck.STATUS_SENDING)
                .set(ChannelShipmentAck::getStatus, ack.getStatus()).set(ChannelShipmentAck::getResponseCode, ack.getResponseCode())
                .set(ChannelShipmentAck::getResponseBody, result.getResponseBody()).set(ChannelShipmentAck::getErrorCode, ack.getErrorCode())
                .set(ChannelShipmentAck::getErrorMessage, ack.getErrorMessage()).set(ChannelShipmentAck::getNextRetryAt, ack.getNextRetryAt())
                .set(ChannelShipmentAck::getRetry404Count, count404).setSql("version = version + 1"));
            return;
        }
        ack.markFailedManual(result.getHttpStatusCode(), result.getErrorCode(), result.getErrorMessage());
        ackMapper.update(null, new LambdaUpdateWrapper<ChannelShipmentAck>()
            .eq(ChannelShipmentAck::getId, ackId).eq(ChannelShipmentAck::getStatus, ChannelShipmentAck.STATUS_SENDING)
            .set(ChannelShipmentAck::getStatus, ack.getStatus()).set(ChannelShipmentAck::getResponseCode, ack.getResponseCode())
            .set(ChannelShipmentAck::getResponseBody, result.getResponseBody()).set(ChannelShipmentAck::getErrorCode, ack.getErrorCode())
            .set(ChannelShipmentAck::getErrorMessage, ack.getErrorMessage()).set(ChannelShipmentAck::getNextRetryAt, null)
            .set(ChannelShipmentAck::getRetryCount, (ack.getRetryCount() == null ? 0 : ack.getRetryCount()) + 1)
            .set(ChannelShipmentAck::getRetry404Count, count404).setSql("version = version + 1"));
        createOrderHoldForFailedManual(ack, "AMZ_ORDER_NOT_FOUND_404");
    }

    private void applySelfHealFix(Long ackId, ChannelShipmentAck ack, ConfirmShipmentSelfHealStrategy strategy, LocalDateTime now) {
        Instant nowUtc = Instant.now();
        switch (strategy) {
            case SHIP_DATE_AFTER_NOW:
                ack.setShipDateUtc(LocalDateTime.ofInstant(nowUtc.minusSeconds(2), ZoneOffset.UTC));
                break;
            case SHIP_DATE_BEFORE_ORDER:
                UnifiedOrder order = ack.getUnifiedOrderId() != null ? unifiedOrderMapper.selectById(ack.getUnifiedOrderId()) : null;
                Instant shipDate = ack.getShipDateUtc() != null ? ack.getShipDateUtc().atOffset(ZoneOffset.UTC).toInstant() : nowUtc.minusSeconds(5);
                if (order != null && order.getOrderPurchaseDateUtc() != null) {
                    Instant purchaseUtc = order.getOrderPurchaseDateUtc().atOffset(ZoneOffset.UTC).toInstant();
                    if (shipDate.isBefore(purchaseUtc)) shipDate = purchaseUtc;
                }
                if (shipDate.isAfter(nowUtc.minusSeconds(2))) shipDate = nowUtc.minusSeconds(2);
                ack.setShipDateUtc(LocalDateTime.ofInstant(shipDate, ZoneOffset.UTC));
                break;
            case CARRIER_INVALID:
                String orig = ack.getCarrierCode() != null ? ack.getCarrierCode() : "Other";
                ack.setCarrierCode("Other");
                ack.setCarrierName(orig);
                break;
            case ORDER_ITEM_INVALID:
                Long fulfillmentId = ack.getFulfillmentId();
                if (fulfillmentId == null && ack.getUnifiedOrderId() != null) {
                    Fulfillment f = fulfillmentMapper.selectOne(new LambdaQueryWrapper<Fulfillment>()
                        .eq(Fulfillment::getUnifiedOrderId, ack.getUnifiedOrderId()).eq(Fulfillment::getDeleted, 0).last("LIMIT 1"));
                    if (f != null) fulfillmentId = f.getId();
                }
                amazonOrderQueryService.getOrderItemsAndBackfillForce(ack.getAmazonOrderId(), ack.getUnifiedOrderId(), fulfillmentId, ack.getMarketplaceId(), ackId);
                if (ack.getWmsPackId() != null) {
                    List<FulfillmentItem> ffItems = fulfillmentId != null ? fulfillmentItemMapper.selectList(new LambdaQueryWrapper<FulfillmentItem>()
                        .eq(FulfillmentItem::getFulfillmentId, fulfillmentId).eq(FulfillmentItem::getDeleted, 0)) : new ArrayList<>();
                    List<PackOrderLine> packLines = packOrderLineMapper.selectList(new LambdaQueryWrapper<PackOrderLine>()
                        .eq(PackOrderLine::getPackId, ack.getWmsPackId()).eq(PackOrderLine::getDeleted, 0).orderByAsc(PackOrderLine::getLineNo));
                    List<ConfirmShipmentRequest.OrderItem> packOrderItems = allocatePackLinesToOrderItems(packLines, ffItems);
                    ack.setOrderItemsJson(serializeOrderItems(packOrderItems));
                }
                break;
            default:
                break;
        }
    }

    private void applyFailureAndHold(Long ackId, ChannelShipmentAck ack, ConfirmShipmentResult result) {
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
        createOrderHoldForFailedManual(ack, "AMZ_CONFIRM_SHIPMENT_FAILED");
    }

    private void createOrderHoldForFailedManual(ChannelShipmentAck ack, String reasonCode) {
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
        hold.setReasonCode(reasonCode != null ? reasonCode : "AMZ_CONFIRM_SHIPMENT_FAILED");
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
        Instant nowUtc = Instant.now();
        Instant shipDate;
        if (ack.getShipDateUtc() != null) {
            shipDate = ack.getShipDateUtc().atOffset(ZoneOffset.UTC).toInstant();
        } else {
            shipDate = nowUtc.minusSeconds(5);
        }
        shipDate = shipDate.isAfter(nowUtc.minusSeconds(2)) ? nowUtc.minusSeconds(2) : shipDate;
        UnifiedOrder order = null;
        if (ack.getUnifiedOrderId() != null) {
            order = unifiedOrderMapper.selectById(ack.getUnifiedOrderId());
            if (order != null && order.getOrderPurchaseDateUtc() != null) {
                Instant purchaseUtc = order.getOrderPurchaseDateUtc().atOffset(ZoneOffset.UTC).toInstant();
                if (shipDate.isBefore(purchaseUtc)) shipDate = purchaseUtc;
            }
        }
        if (shipDate.isAfter(nowUtc.minusSeconds(2))) shipDate = nowUtc.minusSeconds(2);

        ConfirmShipmentRequest req = new ConfirmShipmentRequest();
        req.setMarketplaceId(ack.getMarketplaceId() != null ? ack.getMarketplaceId() : "ATVPDKIKX0DER");
        req.setCodCollectionMethod("");
        ConfirmShipmentRequest.PackageDetail pkg = new ConfirmShipmentRequest.PackageDetail();
        pkg.setPackageReferenceId(ack.getPackageReferenceId());
        String carrierCode = ack.getCarrierCode() != null ? ack.getCarrierCode() : "Other";
        pkg.setCarrierCode(carrierCode);
        if ("Other".equals(carrierCode) && ack.getCarrierName() != null && !ack.getCarrierName().isBlank()) {
            pkg.setCarrierName(ack.getCarrierName());
        }
        pkg.setShippingMethod(ack.getShippingMethod() != null ? ack.getShippingMethod() : "SHIPPING");
        pkg.setTrackingNumber(ack.getTrackingNo());
        pkg.setShipDate(shipDate);

        List<ConfirmShipmentRequest.OrderItem> items;
        if (ack.getOrderItemsJson() != null && !ack.getOrderItemsJson().isBlank()) {
            items = parseOrderItemsJson(ack.getOrderItemsJson());
        } else {
            items = new ArrayList<>();
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
                String orderItemId = oi.getExternalOrderItemId();
                if (orderItemId == null || orderItemId.isBlank()) continue;
                int qty = fi.getReservedQty() != null ? fi.getReservedQty() : (fi.getQty() != null ? fi.getQty() : 0);
                if (qty <= 0) continue;
                ConfirmShipmentRequest.OrderItem item = new ConfirmShipmentRequest.OrderItem();
                item.setOrderItemId(orderItemId);
                item.setQuantity(qty);
                items.add(item);
            }
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Missing Amazon orderItemId (external_order_item_id required for all items)");
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
