package com.pod.tms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pod.common.core.context.TenantContext;
import com.pod.common.utils.TraceIdUtils;
import com.pod.oms.domain.UnifiedOrderItem;
import com.pod.oms.mapper.UnifiedOrderItemMapper;
import com.pod.oms.service.AmazonOrderItemBackfillFacade;
import com.pod.oms.service.BackfillResult;
import com.pod.oms.service.BackfillResultVo;
import com.pod.tms.gateway.AmazonOrderItemDTO;
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
 * P1.6++ 拉单阶段 orderItemId 预回填。使用缓存与限流后的 getOrderItems，按订单行匹配回填。
 */
@Service
public class AmazonOrderItemBackfillFacadeImpl implements AmazonOrderItemBackfillFacade {

    private static final Logger log = LoggerFactory.getLogger(AmazonOrderItemBackfillFacadeImpl.class);
    private static final String MATCH_BY_SELLER_SKU = "amazon_seller_sku";
    private static final String MATCH_BY_SKU_CODE = "sku_code";
    private static final String MATCH_BY_ASIN = "amazon_asin";

    @Autowired
    private OrderItemsCacheService orderItemsCacheService;
    @Autowired
    private UnifiedOrderItemMapper unifiedOrderItemMapper;

    private long tenantId() { return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L; }
    private long factoryId() { return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L; }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BackfillResultVo backfillOrderItemsForPull(String amazonOrderId, Long unifiedOrderId, String marketplaceId, String shopIdStr) {
        List<UnifiedOrderItem> toFill = listOrderItemsMissingExternalId(unifiedOrderId);
        if (toFill.isEmpty()) {
            return BackfillResultVo.of(BackfillResult.SUCCESS);
        }

        long startMs = System.currentTimeMillis();
        GetOrderItemsResult result = orderItemsCacheService.getOrderItemsWithCache(amazonOrderId, marketplaceId != null ? marketplaceId : "");
        long durationMs = System.currentTimeMillis() - startMs;
        log.info("backfillOrderItemsForPull getOrderItems amazonOrderId={} unifiedOrderId={} tenantId={} factoryId={} traceId={} durationMs={} cacheHit={} httpStatus={}",
            amazonOrderId, unifiedOrderId, tenantId(), factoryId(), TraceIdUtils.getTraceId(), durationMs, result.isCacheHit(), result.getHttpStatusCode());

        if (!result.isSuccess()) {
            if (result.getHttpStatusCode() == 401 || result.getHttpStatusCode() == 403) {
                return BackfillResultVo.of(BackfillResult.AUTH_FAILED);
            }
            if (result.getHttpStatusCode() == 429 || result.getHttpStatusCode() == 503) {
                return BackfillResultVo.of(BackfillResult.RETRYABLE);
            }
            return BackfillResultVo.of(BackfillResult.NOT_MATCHED);
        }

        List<AmazonOrderItemDTO> apiItems = result.getOrderItems() != null ? result.getOrderItems() : new ArrayList<>();
        List<AmazonOrderItemDTO> remaining = new ArrayList<>(apiItems);
        int filled = 0;
        List<String> unmatchedSkus = new ArrayList<>();

        for (UnifiedOrderItem local : toFill) {
            int qty = local.getQuantity() != null ? local.getQuantity() : 0;
            AmazonOrderItemDTO matched = null;
            String matchRule = null;

            for (AmazonOrderItemDTO amz : remaining) {
                int amzQty = amz.getQuantityOrdered() != null ? amz.getQuantityOrdered() : 0;
                if (amzQty < qty) continue;
                if (matchBySellerSku(local, amz)) { matched = amz; matchRule = MATCH_BY_SELLER_SKU; break; }
                if (matchBySkuCode(local, amz)) { matched = amz; matchRule = MATCH_BY_SKU_CODE; break; }
                if (matchByPlatformSku(local, amz)) { matched = amz; matchRule = "platform_sku"; break; }
                if (matchByAsin(local, amz)) { matched = amz; matchRule = MATCH_BY_ASIN; break; }
            }
            if (matched == null) {
                String sku = local.getSkuCode() != null ? local.getSkuCode() : local.getPlatformSkuCode();
                if (sku != null) unmatchedSkus.add(sku);
                log.warn("backfillOrderItemsForPull no match amazonOrderId={} itemId={} skuCode={} tenantId={} factoryId={} traceId={}",
                    amazonOrderId, local.getId(), sku, tenantId(), factoryId(), TraceIdUtils.getTraceId());
                continue;
            }
            remaining.remove(matched);
            int rows = unifiedOrderItemMapper.update(null, new LambdaUpdateWrapper<UnifiedOrderItem>()
                .eq(UnifiedOrderItem::getId, local.getId())
                .eq(UnifiedOrderItem::getVersion, local.getVersion())
                .eq(UnifiedOrderItem::getDeleted, 0)
                .set(UnifiedOrderItem::getExternalOrderItemId, matched.getOrderItemId())
                .set(UnifiedOrderItem::getAmazonSellerSku, matched.getSellerSKU())
                .set(UnifiedOrderItem::getAmazonAsin, matched.getAsin())
                .set(UnifiedOrderItem::getAmazonQuantityOrdered, matched.getQuantityOrdered())
                .setSql("version = version + 1"));
            if (rows == 1) {
                filled++;
                log.info("backfillOrderItemsForPull backfill amazonOrderId={} unifiedOrderId={} itemId={} matchRule={} tenantId={} factoryId={} traceId={}",
                    amazonOrderId, unifiedOrderId, local.getId(), matchRule, tenantId(), factoryId(), TraceIdUtils.getTraceId());
            }
        }

        if (!unmatchedSkus.isEmpty()) {
            String msg = "Unmatched sku/asin: " + String.join(", ", unmatchedSkus);
            if (msg.length() > 512) msg = msg.substring(0, 512);
            log.warn("backfillOrderItemsForPull NOT_MATCHED amazonOrderId={} unifiedOrderId={} filled={} unmatchedCount={} tenantId={} factoryId={} traceId={}",
                amazonOrderId, unifiedOrderId, filled, unmatchedSkus.size(), tenantId(), factoryId(), TraceIdUtils.getTraceId());
            return BackfillResultVo.notMatched(msg);
        }
        log.info("backfillOrderItemsForPull SUCCESS amazonOrderId={} unifiedOrderId={} filled={} tenantId={} factoryId={} traceId={}",
            amazonOrderId, unifiedOrderId, filled, tenantId(), factoryId(), TraceIdUtils.getTraceId());
        return BackfillResultVo.of(BackfillResult.SUCCESS);
    }

    private List<UnifiedOrderItem> listOrderItemsMissingExternalId(Long unifiedOrderId) {
        if (unifiedOrderId == null) return new ArrayList<>();
        List<UnifiedOrderItem> all = unifiedOrderItemMapper.selectList(new LambdaQueryWrapper<UnifiedOrderItem>()
            .eq(UnifiedOrderItem::getUnifiedOrderId, unifiedOrderId).eq(UnifiedOrderItem::getDeleted, 0));
        List<UnifiedOrderItem> out = new ArrayList<>();
        for (UnifiedOrderItem oi : all) {
            if (!Objects.equals(oi.getTenantId(), tenantId())) continue;
            if (oi.getExternalOrderItemId() != null && !oi.getExternalOrderItemId().isBlank()) continue;
            out.add(oi);
        }
        return out;
    }

    private boolean matchBySellerSku(UnifiedOrderItem local, AmazonOrderItemDTO amz) {
        String sku = local.getAmazonSellerSku();
        return sku != null && !sku.isBlank() && sku.equals(amz.getSellerSKU());
    }

    private boolean matchBySkuCode(UnifiedOrderItem local, AmazonOrderItemDTO amz) {
        String code = local.getSkuCode();
        return code != null && !code.isBlank() && code.equals(amz.getSellerSKU());
    }

    private boolean matchByPlatformSku(UnifiedOrderItem local, AmazonOrderItemDTO amz) {
        String code = local.getPlatformSkuCode();
        return code != null && !code.isBlank() && code.equals(amz.getSellerSKU());
    }

    private boolean matchByAsin(UnifiedOrderItem local, AmazonOrderItemDTO amz) {
        String asin = local.getAmazonAsin();
        return asin != null && !asin.isBlank() && asin.equals(amz.getAsin());
    }
}
