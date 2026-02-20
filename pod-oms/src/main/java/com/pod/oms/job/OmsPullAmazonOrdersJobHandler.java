package com.pod.oms.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.common.core.context.TenantContext;
import com.pod.oms.client.AmazonOrdersClient;
import com.pod.oms.client.AmzAuthFailedException;
import com.pod.oms.domain.OrderHold;
import com.pod.oms.domain.UnifiedOrder;
import com.pod.oms.dto.ChannelOrderDto;
import com.pod.oms.mapper.OrderHoldMapper;
import com.pod.oms.service.AmazonOrderItemBackfillFacade;
import com.pod.oms.service.BackfillResult;
import com.pod.oms.service.BackfillResultVo;
import com.pod.oms.service.FulfillmentApplicationService;
import com.pod.oms.service.UnifiedOrderApplicationService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * XXL-JOB：拉取 Amazon 订单 -> 落库 -> SKU 映射 -> P1.6++ 预回填 orderItemId -> 失败写入 oms_order_hold。
 * 入参：shopId（必填）, lastUpdatedAfter, lastUpdatedBefore（可选，格式 yyyy-MM-dd HH:mm:ss）
 */
@Component
public class OmsPullAmazonOrdersJobHandler {

    private static final Logger log = LoggerFactory.getLogger(OmsPullAmazonOrdersJobHandler.class);
    private static final String CHANNEL_AMAZON = "AMAZON";

    @Autowired
    private AmazonOrdersClient amazonOrdersClient;
    @Autowired
    private UnifiedOrderApplicationService unifiedOrderApplicationService;
    @Autowired
    private FulfillmentApplicationService fulfillmentApplicationService;
    @Autowired(required = false)
    private AmazonOrderItemBackfillFacade amazonOrderItemBackfillFacade;
    @Autowired
    private OrderHoldMapper orderHoldMapper;

    @XxlJob("omsPullAmazonOrdersJobHandler")
    public void pullAmazonOrders() {
        String shopId = null;
        LocalDateTime lastUpdatedAfter = LocalDateTime.now().minusDays(1);
        LocalDateTime lastUpdatedBefore = LocalDateTime.now();
        String param = XxlJobHelper.getJobParam();
        if (param != null && !param.isBlank()) {
            for (String p : param.split(",")) {
                String[] kv = p.split("=", 2);
                if (kv.length != 2) continue;
                String k = kv[0].trim();
                String v = kv[1].trim();
                if ("shopId".equals(k)) shopId = v;
                else if ("lastUpdatedAfter".equals(k)) lastUpdatedAfter = LocalDateTime.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                else if ("lastUpdatedBefore".equals(k)) lastUpdatedBefore = LocalDateTime.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        }
        if (shopId == null || shopId.isBlank()) {
            XxlJobHelper.handleFail("job param shopId required, e.g. shopId=1 or shopId=1,lastUpdatedAfter=2026-02-01 00:00:00");
            return;
        }

        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        if (tenantId == null) TenantContext.setTenantId(1L);
        if (factoryId == null) TenantContext.setFactoryId(1L);

        try {
            List<ChannelOrderDto> orders;
            try {
                orders = amazonOrdersClient.fetchOrders(shopId, lastUpdatedAfter, lastUpdatedBefore);
            } catch (AmzAuthFailedException e) {
                log.warn("Amazon auth failed shopId={} tenantId={} factoryId={} traceId={} status={}", shopId, TenantContext.getTenantId(), TenantContext.getFactoryId(), com.pod.common.utils.TraceIdUtils.getTraceId(), e.getHttpStatus());
                createAmzAuthHoldForShop(shopId);
                XxlJobHelper.handleFail("AMZ_AUTH_FAILED: " + e.getMessage());
                return;
            }
            int count = 0;
            int fulfillmentsCreated = 0;
            Set<String> backfillCalledForOrder = new HashSet<>();
            for (ChannelOrderDto dto : orders) {
                String requestId = "xxl-" + XxlJobHelper.getJobId() + "-" + dto.getExternalOrderId();
                UnifiedOrder order = unifiedOrderApplicationService.upsertFromChannel(requestId, CHANNEL_AMAZON, shopId, dto);
                count++;
                if (order != null && order.getItems() != null && !order.getItems().isEmpty()) {
                    boolean hasMissingExtItemId = order.getItems().stream().anyMatch(i -> i.getExternalOrderItemId() == null || i.getExternalOrderItemId().isBlank());
                    if (hasMissingExtItemId && amazonOrderItemBackfillFacade != null && backfillCalledForOrder.add(dto.getExternalOrderId())) {
                        BackfillResultVo backfill = amazonOrderItemBackfillFacade.backfillOrderItemsForPull(
                            dto.getExternalOrderId(), order.getId(), order.getMarketplaceId(), shopId);
                        if (backfill.getResult() == BackfillResult.AUTH_FAILED) {
                            createChannelDataHold(order, "AMZ_AUTH_FAILED", "getOrderItems auth failed (401/403)");
                        } else if (backfill.getResult() == BackfillResult.NOT_MATCHED) {
                            String msg = backfill.getReasonMsg() != null ? backfill.getReasonMsg() : "Amazon orderItemId not matched";
                            createChannelDataHold(order, "AMZ_ORDER_ITEM_NOT_MATCHED", msg);
                        }
                    }
                    boolean allMapped = order.getItems().stream().allMatch(i -> i.getSkuId() != null);
                    if (allMapped) {
                        try {
                            fulfillmentApplicationService.createFromUnifiedOrder(order.getId());
                            fulfillmentsCreated++;
                        } catch (Exception ex) {
                            log.warn("Create fulfillment for order {} failed: {}", order.getId(), ex.getMessage());
                        }
                    }
                }
            }
            XxlJobHelper.handleSuccess("Pulled " + count + " orders, " + fulfillmentsCreated + " fulfillments created");
        } catch (Exception e) {
            log.error("omsPullAmazonOrdersJobHandler failed", e);
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /** 401/403 时无订单可关联，按 shop 写入 hold（externalOrderId 用 shopId 占位）。 */
    private void createAmzAuthHoldForShop(String shopId) {
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        if (tenantId == null || factoryId == null) return;
        OrderHold existing = orderHoldMapper.selectOne(new LambdaQueryWrapper<OrderHold>()
            .eq(OrderHold::getTenantId, tenantId).eq(OrderHold::getFactoryId, factoryId).eq(OrderHold::getDeleted, 0)
            .eq(OrderHold::getHoldType, OrderHold.HOLD_TYPE_CHANNEL_DATA).eq(OrderHold::getChannel, CHANNEL_AMAZON)
            .eq(OrderHold::getShopId, shopId).eq(OrderHold::getExternalSku, "AMZ_AUTH_FAILED").eq(OrderHold::getStatus, OrderHold.STATUS_OPEN));
        if (existing != null) return;
        OrderHold hold = new OrderHold();
        hold.setHoldType(OrderHold.HOLD_TYPE_CHANNEL_DATA);
        hold.setStatus(OrderHold.STATUS_OPEN);
        hold.setReasonCode("AMZ_AUTH_FAILED");
        hold.setReasonMsg("Amazon credential 401/403 or LWA refresh denied");
        hold.setChannel(CHANNEL_AMAZON);
        hold.setShopId(shopId);
        hold.setExternalOrderId("shop-" + shopId);
        hold.setExternalSku("AMZ_AUTH_FAILED");
        hold.setTenantId(tenantId);
        hold.setFactoryId(factoryId);
        orderHoldMapper.insert(hold);
        log.info("Order hold created CHANNEL_DATA AMZ_AUTH_FAILED shopId={}", shopId);
    }

    private void createChannelDataHold(UnifiedOrder order, String reasonCode, String reasonMsg) {
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        if (tenantId == null || factoryId == null) return;
        String shopIdStr = order.getShopId() != null ? String.valueOf(order.getShopId()) : "";
        String extOrderId = order.getExternalOrderId() != null ? order.getExternalOrderId() : order.getPlatformOrderId();
        if (extOrderId == null) return;
        OrderHold existing = orderHoldMapper.selectOne(new LambdaQueryWrapper<OrderHold>()
            .eq(OrderHold::getTenantId, tenantId).eq(OrderHold::getFactoryId, factoryId).eq(OrderHold::getDeleted, 0)
            .eq(OrderHold::getHoldType, OrderHold.HOLD_TYPE_CHANNEL_DATA).eq(OrderHold::getChannel, CHANNEL_AMAZON)
            .eq(OrderHold::getExternalOrderId, extOrderId).eq(OrderHold::getExternalSku, reasonCode).eq(OrderHold::getStatus, OrderHold.STATUS_OPEN));
        if (existing != null) return;
        OrderHold hold = new OrderHold();
        hold.setHoldType(OrderHold.HOLD_TYPE_CHANNEL_DATA);
        hold.setStatus(OrderHold.STATUS_OPEN);
        hold.setReasonCode(reasonCode);
        hold.setReasonMsg(reasonMsg != null ? reasonMsg.substring(0, Math.min(512, reasonMsg.length())) : reasonCode);
        hold.setChannel(CHANNEL_AMAZON);
        hold.setShopId(shopIdStr);
        hold.setExternalOrderId(extOrderId);
        hold.setExternalSku(reasonCode);
        hold.setUnifiedOrderId(order.getId());
        orderHoldMapper.insert(hold);
        log.info("Order hold created CHANNEL_DATA orderId={} reasonCode={}", extOrderId, reasonCode);
    }
}
