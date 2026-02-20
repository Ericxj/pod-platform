package com.pod.tms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pod.common.core.context.TenantContext;
import com.pod.common.utils.TraceIdUtils;
import com.pod.tms.config.AmazonSpApiProperties;
import com.pod.tms.domain.AmazonOrderItemsCache;
import com.pod.tms.gateway.AmazonOrderItemDTO;
import com.pod.tms.gateway.AmazonSpApiGateway;
import com.pod.tms.gateway.GetOrderItemsResult;
import com.pod.tms.mapper.AmazonOrderItemsCacheMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * getOrderItems 缓存 + 按 marketplace 限流。命中 VALID 且未过期直接返回；过期但在容忍期内可返回旧缓存；否则调网关并落库。
 */
@Service
public class OrderItemsCacheService {

    private static final Logger log = LoggerFactory.getLogger(OrderItemsCacheService.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private AmazonSpApiGateway amazonSpApiGateway;
    @Autowired
    private AmazonOrderItemsCacheMapper cacheMapper;
    @Autowired
    private AmazonSpApiProperties properties;

    private final Map<String, Semaphore> semaphoreByMarketplace = new ConcurrentHashMap<>();

    private long tenantId() { return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L; }
    private long factoryId() { return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L; }

    /**
     * 带缓存与限流的 getOrderItems。先查缓存；未命中或过期则限流后调网关，成功则写入/更新缓存。
     * 429/503 不更新缓存。
     */
    public GetOrderItemsResult getOrderItemsWithCache(String amazonOrderId, String marketplaceId) {
        return getOrderItemsWithCache(amazonOrderId, marketplaceId, false);
    }

    /**
     * 当 ignoreCache=true 时跳过缓存直接调 API（P1.6++ D 自愈 orderItemId invalid 时强制刷新）。
     */
    public GetOrderItemsResult getOrderItemsWithCache(String amazonOrderId, String marketplaceId, boolean ignoreCache) {
        long startMs = System.currentTimeMillis();
        String mk = marketplaceId != null ? marketplaceId : "";
        String traceId = TraceIdUtils.getTraceId();
        AmazonOrderItemsCache cached = null;
        LocalDateTime now = LocalDateTime.now();

        if (!ignoreCache) {
            cached = cacheMapper.selectOne(new LambdaQueryWrapper<AmazonOrderItemsCache>()
                .eq(AmazonOrderItemsCache::getTenantId, tenantId()).eq(AmazonOrderItemsCache::getFactoryId, factoryId())
                .eq(AmazonOrderItemsCache::getAmazonOrderId, amazonOrderId).eq(AmazonOrderItemsCache::getMarketplaceId, mk)
                .eq(AmazonOrderItemsCache::getDeleted, 0).last("LIMIT 1"));
            if (cached != null && cached.getPayloadJson() != null && !cached.getPayloadJson().isBlank()) {
                LocalDateTime expireAt = cached.getExpireAt();
                int staleHours = properties.getOrderItemsStaleToleranceHours();
                boolean valid = AmazonOrderItemsCache.STATUS_VALID.equals(cached.getStatus()) && (expireAt == null || now.isBefore(expireAt));
                boolean staleOk = expireAt != null && now.isAfter(expireAt) && cached.getFetchedAt() != null && cached.getFetchedAt().isAfter(now.minusHours(staleHours));
                if (valid || staleOk) {
                    List<AmazonOrderItemDTO> items = parsePayloadToItems(cached.getPayloadJson());
                    long durationMs = System.currentTimeMillis() - startMs;
                    log.info("getOrderItemsWithCache cacheHit=true amazonOrderId={} marketplaceId={} tenantId={} factoryId={} traceId={} durationMs={} itemCount={}",
                        amazonOrderId, mk, tenantId(), factoryId(), traceId, durationMs, items != null ? items.size() : 0);
                    return GetOrderItemsResult.ok(items != null ? items : new ArrayList<>(), true);
                }
            }
        }

        Semaphore sem = semaphoreByMarketplace.computeIfAbsent(mk, k ->
            new Semaphore(Math.max(1, properties.getOrderItemsMaxConcurrentPerMarketplace())));
        boolean acquired = false;
        try {
            acquired = sem.tryAcquire(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return GetOrderItemsResult.fail(500, "Interrupted", "getOrderItems rate limit wait interrupted", null);
        }
        try {
            if (!acquired) {
                if (cached != null && cached.getPayloadJson() != null && cached.getFetchedAt() != null && cached.getFetchedAt().isAfter(now.minusHours(properties.getOrderItemsStaleToleranceHours()))) {
                    List<AmazonOrderItemDTO> items = parsePayloadToItems(cached.getPayloadJson());
                    log.info("getOrderItemsWithCache rateLimit fallback to stale cache amazonOrderId={} marketplaceId={} traceId={}", amazonOrderId, mk, traceId);
                    return GetOrderItemsResult.ok(items != null ? items : new ArrayList<>(), true);
                }
                return GetOrderItemsResult.fail(429, "TooManyRequests", "getOrderItems rate limited", null);
            }
            GetOrderItemsResult result = amazonSpApiGateway.getOrderItems(amazonOrderId);
            long durationMs = System.currentTimeMillis() - startMs;
            log.info("getOrderItemsWithCache cacheHit=false amazonOrderId={} marketplaceId={} tenantId={} factoryId={} traceId={} durationMs={} httpStatus={}",
                amazonOrderId, mk, tenantId(), factoryId(), traceId, durationMs, result.getHttpStatusCode());

            if (result.isSuccess() && result.getOrderItems() != null) {
                saveOrUpdateCache(amazonOrderId, mk, result.getOrderItems());
            }
            return result;
        } finally {
            if (acquired) sem.release();
        }
    }

    private List<AmazonOrderItemDTO> parsePayloadToItems(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) return new ArrayList<>();
        try {
            JsonNode root = JSON.readTree(payloadJson);
            JsonNode payload = root.has("payload") ? root.get("payload") : null;
            if (payload == null || !payload.has("OrderItems") || !payload.get("OrderItems").isArray()) return new ArrayList<>();
            List<AmazonOrderItemDTO> list = new ArrayList<>();
            for (JsonNode item : payload.get("OrderItems")) {
                AmazonOrderItemDTO dto = new AmazonOrderItemDTO();
                if (item.has("OrderItemId")) dto.setOrderItemId(item.get("OrderItemId").asText());
                if (item.has("SellerSKU")) dto.setSellerSKU(item.get("SellerSKU").asText());
                if (item.has("ASIN")) dto.setAsin(item.get("ASIN").asText());
                if (item.has("QuantityOrdered")) dto.setQuantityOrdered(item.get("QuantityOrdered").asInt());
                list.add(dto);
            }
            return list;
        } catch (Exception e) {
            log.warn("parsePayloadToItems failed", e);
            return new ArrayList<>();
        }
    }

    private void saveOrUpdateCache(String amazonOrderId, String marketplaceId, List<AmazonOrderItemDTO> items) {
        try {
            String payloadJson = buildPayloadJson(items);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expireAt = now.plusMinutes(Math.max(1, properties.getOrderItemsCacheTtlMinutes()));

            AmazonOrderItemsCache existing = cacheMapper.selectOne(new LambdaQueryWrapper<AmazonOrderItemsCache>()
                .eq(AmazonOrderItemsCache::getTenantId, tenantId()).eq(AmazonOrderItemsCache::getFactoryId, factoryId())
                .eq(AmazonOrderItemsCache::getAmazonOrderId, amazonOrderId).eq(AmazonOrderItemsCache::getMarketplaceId, marketplaceId)
                .eq(AmazonOrderItemsCache::getDeleted, 0));
            if (existing != null) {
                cacheMapper.update(null, new LambdaUpdateWrapper<AmazonOrderItemsCache>()
                    .eq(AmazonOrderItemsCache::getId, existing.getId())
                    .set(AmazonOrderItemsCache::getPayloadJson, payloadJson)
                    .set(AmazonOrderItemsCache::getFetchedAt, now)
                    .set(AmazonOrderItemsCache::getExpireAt, expireAt)
                    .set(AmazonOrderItemsCache::getStatus, AmazonOrderItemsCache.STATUS_VALID));
            } else {
                AmazonOrderItemsCache c = new AmazonOrderItemsCache();
                c.setTenantId(tenantId());
                c.setFactoryId(factoryId());
                c.setAmazonOrderId(amazonOrderId);
                c.setMarketplaceId(marketplaceId);
                c.setPayloadJson(payloadJson);
                c.setFetchedAt(now);
                c.setExpireAt(expireAt);
                c.setStatus(AmazonOrderItemsCache.STATUS_VALID);
                c.setTraceId(TraceIdUtils.getTraceId());
                cacheMapper.insert(c);
            }
        } catch (Exception e) {
            log.warn("saveOrUpdateCache failed amazonOrderId={}", amazonOrderId, e);
        }
    }

    private String buildPayloadJson(List<AmazonOrderItemDTO> items) {
        try {
            com.fasterxml.jackson.databind.node.ArrayNode arr = JSON.getNodeFactory().arrayNode();
            for (AmazonOrderItemDTO dto : items) {
                com.fasterxml.jackson.databind.node.ObjectNode o = JSON.getNodeFactory().objectNode();
                if (dto.getOrderItemId() != null) o.put("OrderItemId", dto.getOrderItemId());
                if (dto.getSellerSKU() != null) o.put("SellerSKU", dto.getSellerSKU());
                if (dto.getAsin() != null) o.put("ASIN", dto.getAsin());
                if (dto.getQuantityOrdered() != null) o.put("QuantityOrdered", dto.getQuantityOrdered());
                arr.add(o);
            }
            return JSON.getNodeFactory().objectNode().set("payload", JSON.getNodeFactory().objectNode().set("OrderItems", arr)).toString();
        } catch (Exception e) {
            return "{}";
        }
    }
}
