package com.pod.tms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.common.utils.TraceIdUtils;
import com.pod.oms.domain.FulfillmentItem;
import com.pod.oms.domain.UnifiedOrderItem;
import com.pod.oms.mapper.FulfillmentItemMapper;
import com.pod.oms.mapper.UnifiedOrderItemMapper;
import com.pod.tms.gateway.AmazonOrderItemDTO;
import com.pod.tms.gateway.AmazonOrderItemsApiException;
import com.pod.tms.gateway.AmazonSpApiGateway;
import com.pod.tms.gateway.GetOrderItemsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Amazon Orders API 查询与 orderItemId 回填。缺 external_order_item_id 时调用 getOrderItems 并按 SKU/ASIN 匹配回填。
 */
@Service
public class AmazonOrderQueryService {

    private static final Logger log = LoggerFactory.getLogger(AmazonOrderQueryService.class);
    private static final String MATCH_BY_SELLER_SKU = "amazon_seller_sku";
    private static final String MATCH_BY_SKU_CODE = "sku_code";
    private static final String MATCH_BY_ASIN = "amazon_asin";

    @Autowired
    private AmazonSpApiGateway amazonSpApiGateway;
    @Autowired
    private FulfillmentItemMapper fulfillmentItemMapper;
    @Autowired
    private UnifiedOrderItemMapper unifiedOrderItemMapper;

    private long tenantId() { return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L; }
    private long factoryId() { return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L; }

    /**
     * 当存在缺失 external_order_item_id 的订单行时，拉取 getOrderItems 并回填；匹配失败抛 BusinessException。
     * 在 sendAck 事务内调用；API 非 2xx 时抛 AmazonOrderItemsApiException。
     *
     * @param amazonOrderId 平台订单 ID
     * @param unifiedOrderId 统一订单 ID
     * @param fulfillmentId 履约单 ID（用于取履约行对应的 order items）
     * @param ackId 用于日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void getOrderItemsAndBackfill(String amazonOrderId, Long unifiedOrderId, Long fulfillmentId, Long ackId) {
        List<UnifiedOrderItem> toFill = listItemsMissingExternalId(unifiedOrderId, fulfillmentId);
        if (toFill.isEmpty()) return;

        long startMs = System.currentTimeMillis();
        GetOrderItemsResult result = amazonSpApiGateway.getOrderItems(amazonOrderId);
        long durationMs = System.currentTimeMillis() - startMs;
        log.info("getOrderItems amazonOrderId={} ackId={} tenantId={} factoryId={} traceId={} durationMs={} success={}",
            amazonOrderId, ackId, tenantId(), factoryId(), TraceIdUtils.getTraceId(), durationMs, result.isSuccess());

        if (!result.isSuccess()) {
            throw new AmazonOrderItemsApiException(result);
        }

        List<AmazonOrderItemDTO> apiItems = result.getOrderItems() != null ? result.getOrderItems() : new ArrayList<>();
        List<AmazonOrderItemDTO> remaining = new ArrayList<>(apiItems);

        for (UnifiedOrderItem local : toFill) {
            int qty = local.getQuantity() != null ? local.getQuantity() : 0;
            AmazonOrderItemDTO matched = null;
            String matchRule = null;

            for (AmazonOrderItemDTO amz : remaining) {
                int amzQty = amz.getQuantityOrdered() != null ? amz.getQuantityOrdered() : 0;
                if (amzQty < qty) continue;
                if (matchBySellerSku(local, amz)) {
                    matched = amz; matchRule = MATCH_BY_SELLER_SKU; break;
                }
                if (matchBySkuCode(local, amz)) {
                    matched = amz; matchRule = MATCH_BY_SKU_CODE; break;
                }
                if (matchByAsin(local, amz)) {
                    matched = amz; matchRule = MATCH_BY_ASIN; break;
                }
            }
            if (matched == null) {
                log.warn("getOrderItemsAndBackfill no match amazonOrderId={} ackId={} itemId={} skuCode={} tenantId={} factoryId={} traceId={}",
                    amazonOrderId, ackId, local.getId(), local.getSkuCode(), tenantId(), factoryId(), TraceIdUtils.getTraceId());
                throw new BusinessException("Amazon orderItemId not matched");
            }

            remaining.remove(matched);
            String beforeExtId = local.getExternalOrderItemId();
            int rows = unifiedOrderItemMapper.update(null, new LambdaUpdateWrapper<UnifiedOrderItem>()
                .eq(UnifiedOrderItem::getId, local.getId())
                .eq(UnifiedOrderItem::getVersion, local.getVersion())
                .eq(UnifiedOrderItem::getDeleted, 0)
                .set(UnifiedOrderItem::getExternalOrderItemId, matched.getOrderItemId())
                .set(UnifiedOrderItem::getAmazonSellerSku, matched.getSellerSKU())
                .set(UnifiedOrderItem::getAmazonAsin, matched.getAsin())
                .set(UnifiedOrderItem::getAmazonQuantityOrdered, matched.getQuantityOrdered())
                .setSql("version = version + 1"));
            if (rows == 0) {
                throw new BusinessException("Concurrent update unified_order_item id=" + local.getId());
            }
            log.info("getOrderItemsAndBackfill backfill amazonOrderId={} ackId={} itemId={} matchRule={} beforeExtId={} afterExtId={} tenantId={} factoryId={} traceId={}",
                amazonOrderId, ackId, local.getId(), matchRule, beforeExtId, matched.getOrderItemId(), tenantId(), factoryId(), TraceIdUtils.getTraceId());
        }
    }

    private List<UnifiedOrderItem> listItemsMissingExternalId(Long unifiedOrderId, Long fulfillmentId) {
        if (fulfillmentId == null && unifiedOrderId == null) return new ArrayList<>();
        List<FulfillmentItem> ffItems = fulfillmentId != null
            ? fulfillmentItemMapper.selectList(new LambdaQueryWrapper<FulfillmentItem>()
                .eq(FulfillmentItem::getFulfillmentId, fulfillmentId).eq(FulfillmentItem::getDeleted, 0))
            : new ArrayList<>();
        List<UnifiedOrderItem> out = new ArrayList<>();
        for (FulfillmentItem fi : ffItems) {
            UnifiedOrderItem oi = unifiedOrderItemMapper.selectById(fi.getUnifiedOrderItemId());
            if (oi == null || !Objects.equals(oi.getTenantId(), tenantId())) continue;
            if (oi.getExternalOrderItemId() != null && !oi.getExternalOrderItemId().isBlank()) continue;
            out.add(oi);
        }
        return out;
    }

    private boolean matchBySellerSku(UnifiedOrderItem local, AmazonOrderItemDTO amz) {
        String sku = local.getAmazonSellerSku();
        if (sku == null || sku.isBlank()) return false;
        return sku.equals(amz.getSellerSKU());
    }

    private boolean matchBySkuCode(UnifiedOrderItem local, AmazonOrderItemDTO amz) {
        String code = local.getSkuCode();
        if (code == null || code.isBlank()) return false;
        return code.equals(amz.getSellerSKU());
    }

    private boolean matchByAsin(UnifiedOrderItem local, AmazonOrderItemDTO amz) {
        String asin = local.getAmazonAsin();
        if (asin == null || asin.isBlank()) return false;
        return asin.equals(amz.getAsin());
    }
}
