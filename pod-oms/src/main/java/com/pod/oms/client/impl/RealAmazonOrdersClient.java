package com.pod.oms.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pod.common.core.context.TenantContext;
import com.pod.common.utils.TraceIdUtils;
import com.pod.iam.sys.application.CredentialApplicationService;
import com.pod.iam.sys.application.ShopApplicationService;
import com.pod.iam.sys.domain.PlatApiCredential;
import com.pod.iam.sys.domain.PlatShop;
import com.pod.oms.client.AmzAuthFailedException;
import com.pod.oms.client.AmazonOrdersClient;
import com.pod.oms.client.amazon.SigV4Signer;
import com.pod.oms.dto.ChannelOrderDto;
import com.pod.oms.dto.ChannelOrderItemDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.TreeMap;

/**
 * Amazon 拉单：从 plat_shop + plat_api_credential 驱动，支持多店铺/多站点/多租户。
 * 解密 encrypted_payload，自动刷新 access_token 并写回 credential。
 * 401/403 抛出 AmzAuthFailedException 供 job 写入 hold。
 */
@Component
@ConditionalOnProperty(name = "oms.amazon.client", havingValue = "real")
public class RealAmazonOrdersClient implements AmazonOrdersClient {

    private static final Logger log = LoggerFactory.getLogger(RealAmazonOrdersClient.class);
    private static final String LWA_TOKEN_URL = "https://api.amazon.com/auth/o2/token";
    private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);
    private static final int REFRESH_BEFORE_SECONDS = 60;
    private static final int MAX_RETRIES = 3;
    private static final ObjectMapper JSON = new ObjectMapper();

    private static final Map<String, String> SITE_TO_MARKETPLACE = new HashMap<>();
    static {
        SITE_TO_MARKETPLACE.put("US", "A2EUQ1WTGCTBG2");
        SITE_TO_MARKETPLACE.put("CA", "A2EUQ1WTGCTBG2");
        SITE_TO_MARKETPLACE.put("UK", "A1F83G8C2ARO7P");
        SITE_TO_MARKETPLACE.put("DE", "A1PA6795UKMFR9");
        SITE_TO_MARKETPLACE.put("FR", "A13V1IB3VIYZZH");
        SITE_TO_MARKETPLACE.put("IT", "AP1F83G8C2ARO7P");
        SITE_TO_MARKETPLACE.put("ES", "A1RKKUPIHCS9HS");
        SITE_TO_MARKETPLACE.put("JP", "A1VC38T3YXB528");
    }

    private final CredentialApplicationService credentialApplicationService;
    private final ShopApplicationService shopApplicationService;
    private final java.net.http.HttpClient httpClient;

    private final ConcurrentHashMap<Long, CachedToken> tokenCache = new ConcurrentHashMap<>();

    public RealAmazonOrdersClient(CredentialApplicationService credentialApplicationService,
                                  ShopApplicationService shopApplicationService) {
        this.credentialApplicationService = credentialApplicationService;
        this.shopApplicationService = shopApplicationService;
        this.httpClient = java.net.http.HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(10)).build();
    }

    @Override
    public List<ChannelOrderDto> fetchOrders(String shopId, LocalDateTime lastUpdatedAfter, LocalDateTime lastUpdatedBefore) {
        Long shopIdLong = parseShopId(shopId);
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        String traceId = TraceIdUtils.getTraceId();
        log.info("fetchOrders shopId={} tenantId={} factoryId={} traceId={}", shopIdLong, tenantId, factoryId, traceId);

        PlatShop shop = shopApplicationService.get(shopIdLong);
        if (!"AMAZON".equalsIgnoreCase(shop.getPlatformCode())) {
            throw new IllegalArgumentException("Shop is not AMAZON: " + shop.getPlatformCode());
        }
        PlatApiCredential cred = credentialApplicationService.getCredentialEntityForChannelPull("AMAZON", shopIdLong, "OAUTH");
        if (cred == null || !PlatApiCredential.STATUS_ENABLED.equals(cred.getStatus())) {
            throw new IllegalStateException("No enabled AMAZON OAUTH credential for shopId=" + shopIdLong);
        }
        String payloadJson = credentialApplicationService.getDecryptedPayloadForChannelPull("AMAZON", shopIdLong, "OAUTH");
        if (payloadJson == null || payloadJson.isBlank()) {
            throw new AmzAuthFailedException(500, "Decrypt credential failed");
        }

        String accessToken = ensureAccessToken(shopIdLong, cred.getId(), payloadJson, tenantId, factoryId, traceId);
        String endpoint = resolveEndpoint(payloadJson);
        String region = resolveRegion(payloadJson);
        String host = hostFromEndpoint(endpoint);
        List<String> marketplaceIds = resolveMarketplaceIds(shop.getSiteCode());

        List<ChannelOrderDto> result = new ArrayList<>();
        String nextToken = null;
        int page = 0;
        do {
            GetOrdersPage pageResult = getOrdersPage(endpoint, host, region, accessToken, payloadJson, marketplaceIds, lastUpdatedAfter, lastUpdatedBefore, nextToken, tenantId, factoryId, shopIdLong, traceId);
            if (pageResult.authFailed) {
                throw new AmzAuthFailedException(pageResult.httpStatus, pageResult.errorMessage);
            }
            for (String orderId : pageResult.orderIds) {
                ChannelOrderDto dto = fetchOrderDetail(endpoint, host, region, accessToken, payloadJson, orderId, marketplaceIds.isEmpty() ? "A2EUQ1WTGCTBG2" : marketplaceIds.get(0), tenantId, factoryId, traceId);
                if (dto != null) result.add(dto);
            }
            nextToken = pageResult.nextToken;
            page++;
        } while (nextToken != null && !nextToken.isBlank() && page < 50);

        log.info("fetchOrders shopId={} count={} tenantId={} factoryId={} traceId={}", shopIdLong, result.size(), tenantId, factoryId, traceId);
        return result;
    }

    private Long parseShopId(String shopId) {
        if (shopId == null || shopId.isBlank()) throw new IllegalArgumentException("shopId required");
        try {
            return Long.parseLong(shopId.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("shopId must be numeric: " + shopId);
        }
    }

    private String ensureAccessToken(Long shopId, Long credentialId, String payloadJson, Long tenantId, Long factoryId, String traceId) {
        CachedToken cached = tokenCache.get(shopId);
        if (cached != null && Instant.now().plusSeconds(REFRESH_BEFORE_SECONDS).isBefore(cached.expiresAt)) {
            return cached.accessToken;
        }
        JsonNode root;
        try {
            root = JSON.readTree(payloadJson);
        } catch (Exception e) {
            throw new AmzAuthFailedException(500, "Invalid credential JSON", e);
        }
        String accessToken = root.has("access_token") ? root.get("access_token").asText(null) : null;
        Instant expiresAt = null;
        if (root.has("expires_at")) {
            try {
                expiresAt = Instant.parse(root.get("expires_at").asText());
            } catch (Exception ignored) { }
        }
        if (accessToken != null && !accessToken.isBlank() && expiresAt != null && Instant.now().plusSeconds(REFRESH_BEFORE_SECONDS).isBefore(expiresAt)) {
            tokenCache.put(shopId, new CachedToken(accessToken, expiresAt));
            return accessToken;
        }
        String refreshToken = root.has("refresh_token") ? root.get("refresh_token").asText(null) : null;
        String clientId = root.has("client_id") ? root.get("client_id").asText(null) : null;
        String clientSecret = root.has("client_secret") ? root.get("client_secret").asText(null) : null;
        if (refreshToken == null || clientId == null || clientSecret == null) {
            throw new AmzAuthFailedException(500, "Missing refresh_token/client_id/client_secret in credential");
        }
        String body = "grant_type=refresh_token&refresh_token=" + urlEncode(refreshToken)
            + "&client_id=" + urlEncode(clientId) + "&client_secret=" + urlEncode(clientSecret);
        try {
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(LWA_TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(java.time.Duration.ofSeconds(15))
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                .build();
            java.net.http.HttpResponse<String> resp = httpClient.send(req, java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() == 401 || resp.statusCode() == 403) {
                log.warn("LWA refresh 401/403 shopId={} tenantId={} factoryId={} traceId={}", shopId, tenantId, factoryId, traceId);
                throw new AmzAuthFailedException(resp.statusCode(), "LWA token refresh denied");
            }
            if (resp.statusCode() != 200) {
                throw new AmzAuthFailedException(resp.statusCode(), "LWA token failed: " + resp.body());
            }
            JsonNode node = JSON.readTree(resp.body());
            accessToken = node.has("access_token") ? node.get("access_token").asText() : null;
            int expiresIn = node.has("expires_in") ? node.get("expires_in").asInt(3600) : 3600;
            expiresAt = Instant.now().plusSeconds(expiresIn);
            if (accessToken == null) throw new AmzAuthFailedException(500, "LWA response missing access_token");
            Map<String, Object> updated = new HashMap<>();
            try {
                @SuppressWarnings("unchecked")
            Map<String, Object> m = JSON.readValue(payloadJson, Map.class);
            updated = m;
            } catch (Exception ignored) { }
            updated.put("access_token", accessToken);
            updated.put("expires_at", expiresAt.toString());
            String newPayload = JSON.writeValueAsString(updated);
            credentialApplicationService.updateCredentialPayloadAfterRefresh(credentialId, newPayload,
                LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC), LocalDateTime.now());
            tokenCache.put(shopId, new CachedToken(accessToken, expiresAt));
            log.info("LWA token refreshed shopId={} tenantId={} factoryId={} traceId={}", shopId, tenantId, factoryId, traceId);
            return accessToken;
        } catch (AmzAuthFailedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("LWA refresh failed shopId={} traceId={}", shopId, traceId, e);
            throw new AmzAuthFailedException(500, "LWA refresh failed: " + e.getMessage(), e);
        }
    }

    private static String urlEncode(String s) {
        if (s == null) return "";
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private String resolveEndpoint(String payloadJson) {
        try {
            JsonNode n = JSON.readTree(payloadJson);
            if (n.has("endpoint") && !n.get("endpoint").asText().isBlank()) return n.get("endpoint").asText();
            if (n.has("selling_region")) {
                String r = n.get("selling_region").asText("");
                if ("EU".equalsIgnoreCase(r)) return "https://sellingpartnerapi-eu.amazon.com";
                if ("FE".equalsIgnoreCase(r)) return "https://sellingpartnerapi-fe.amazon.com";
            }
        } catch (Exception ignored) { }
        return "https://sellingpartnerapi-na.amazon.com";
    }

    private String resolveRegion(String payloadJson) {
        try {
            JsonNode n = JSON.readTree(payloadJson);
            if (n.has("region") && !n.get("region").asText().isBlank()) return n.get("region").asText();
            if (n.has("selling_region")) {
                String r = n.get("selling_region").asText("");
                if ("EU".equalsIgnoreCase(r)) return "eu-west-1";
                if ("FE".equalsIgnoreCase(r)) return "us-west-2";
            }
        } catch (Exception ignored) { }
        return "us-east-1";
    }

    private List<String> resolveMarketplaceIds(String siteCode) {
        List<String> out = new ArrayList<>();
        if (siteCode != null && !siteCode.isBlank()) {
            String m = SITE_TO_MARKETPLACE.get(siteCode.toUpperCase());
            if (m != null) out.add(m);
        }
        if (out.isEmpty()) out.add("A2EUQ1WTGCTBG2");
        return out;
    }

    private static String hostFromEndpoint(String endpoint) {
        try {
            String h = URI.create(endpoint).getHost();
            return h != null ? h : "sellingpartnerapi-na.amazon.com";
        } catch (Exception e) {
            return "sellingpartnerapi-na.amazon.com";
        }
    }

    private GetOrdersPage getOrdersPage(String endpoint, String host, String region, String accessToken, String payloadJson,
                                        List<String> marketplaceIds, LocalDateTime lastUpdatedAfter, LocalDateTime lastUpdatedBefore,
                                        String nextToken, Long tenantId, Long factoryId, Long shopId, String traceId) {
        String path = "/orders/v0/orders";
        Map<String, String> params = new TreeMap<>();
        params.put("lastUpdatedAfter", lastUpdatedAfter.atZone(ZoneOffset.UTC).format(ISO8601));
        params.put("lastUpdatedBefore", lastUpdatedBefore.atZone(ZoneOffset.UTC).format(ISO8601));
        params.put("marketplaceIds", String.join(",", marketplaceIds));
        if (nextToken != null && !nextToken.isBlank()) params.put("nextToken", nextToken);
        StringBuilder qb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (qb.length() > 0) qb.append("&");
            qb.append(urlEncode(e.getKey())).append("=").append(urlEncode(e.getValue()));
        }
        String canonicalQuery = qb.toString();
        String url = endpoint.replaceAll("/$", "") + path + "?" + qb.toString();

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                JsonNode cred = JSON.readTree(payloadJson);
                String awsKey = cred.has("aws_access_key") ? cred.get("aws_access_key").asText() : null;
                String awsSecret = cred.has("aws_secret_key") ? cred.get("aws_secret_key").asText() : null;
                if (awsKey == null || awsSecret == null) return new GetOrdersPage(true, 500, "Missing aws_access_key/aws_secret_key", null, null);
                SigV4Signer signer = new SigV4Signer(region, "execute-api", awsKey, awsSecret);
                Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                signer.sign("GET", host, path, canonicalQuery, new byte[0], accessToken, Instant.now(), headers);
                java.net.http.HttpRequest.Builder rb = java.net.http.HttpRequest.newBuilder().uri(URI.create(url)).timeout(java.time.Duration.ofSeconds(30)).GET();
                for (Map.Entry<String, List<String>> e : headers.entrySet()) { for (String v : e.getValue()) rb.header(e.getKey(), v); }
                java.net.http.HttpResponse<String> resp = httpClient.send(rb.build(), java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                int status = resp.statusCode();
                if (status == 401 || status == 403) {
                    log.warn("getOrders 401/403 shopId={} tenantId={} factoryId={} traceId={}", shopId, tenantId, factoryId, traceId);
                    return new GetOrdersPage(true, status, resp.body(), null, null);
                }
                if (status == 429 || status == 503) {
                    if (attempt < MAX_RETRIES) {
                        long backoff = (long) Math.pow(2, attempt) * 1000;
                        Thread.sleep(backoff);
                        continue;
                    }
                    throw new RuntimeException("getOrders rate limited after " + MAX_RETRIES + " retries");
                }
                if (status != 200) return new GetOrdersPage(false, status, resp.body(), null, null);
                JsonNode root = JSON.readTree(resp.body());
                JsonNode payload = root.has("payload") ? root.get("payload") : null;
                List<String> orderIds = new ArrayList<>();
                String next = null;
                if (payload != null && payload.has("Orders") && payload.get("Orders").isArray()) {
                    for (JsonNode o : payload.get("Orders")) {
                        if (o.has("OrderId")) orderIds.add(o.get("OrderId").asText());
                    }
                }
                if (payload != null && payload.has("NextToken")) next = payload.get("NextToken").asText(null);
                return new GetOrdersPage(false, 200, null, orderIds, next);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (Exception e) {
                if (attempt == MAX_RETRIES) throw new RuntimeException("getOrders failed: " + e.getMessage(), e);
                try { Thread.sleep((long) Math.pow(2, attempt) * 1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw new RuntimeException(ie); }
            }
        }
        return new GetOrdersPage(false, 500, "Unknown", null, null);
    }

    private ChannelOrderDto fetchOrderDetail(String endpoint, String host, String region, String accessToken, String payloadJson,
                                             String orderId, String marketplaceId, Long tenantId, Long factoryId, String traceId) {
        try {
            JsonNode cred = JSON.readTree(payloadJson);
            String awsKey = cred.has("aws_access_key") ? cred.get("aws_access_key").asText() : null;
            String awsSecret = cred.has("aws_secret_key") ? cred.get("aws_secret_key").asText() : null;
            if (awsKey == null || awsSecret == null) return null;
            String pathSeg = URLEncoder.encode(orderId, StandardCharsets.UTF_8).replace("+", "%20");
            String path = "/orders/v0/orders/" + pathSeg + "/orderItems";
            String url = endpoint.replaceAll("/$", "") + path;
            SigV4Signer signer = new SigV4Signer(region, "execute-api", awsKey, awsSecret);
            Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            signer.sign("GET", host, path, null, new byte[0], accessToken, Instant.now(), headers);
            java.net.http.HttpRequest.Builder rb = java.net.http.HttpRequest.newBuilder().uri(URI.create(url)).timeout(java.time.Duration.ofSeconds(15)).GET();
            for (Map.Entry<String, List<String>> e : headers.entrySet()) { for (String v : e.getValue()) rb.header(e.getKey(), v); }
            java.net.http.HttpResponse<String> resp = httpClient.send(rb.build(), java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() == 401 || resp.statusCode() == 403) throw new AmzAuthFailedException(resp.statusCode(), resp.body());
            if (resp.statusCode() != 200) return null;
            return parseOrderItemsToDto(orderId, marketplaceId, resp.body());
        } catch (AmzAuthFailedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("fetchOrderDetail orderId={} traceId={} error={}", orderId, traceId, e.getMessage());
            return null;
        }
    }

    private ChannelOrderDto parseOrderItemsToDto(String orderId, String marketplaceId, String responseBody) {
        ChannelOrderDto dto = new ChannelOrderDto();
        dto.setExternalOrderId(orderId);
        dto.setOrderNo(orderId);
        dto.setMarketplaceId(marketplaceId);
        dto.setOrderCreatedAt(LocalDateTime.now());
        List<ChannelOrderItemDto> items = new ArrayList<>();
        try {
            JsonNode root = JSON.readTree(responseBody);
            JsonNode payload = root.has("payload") ? root.get("payload") : null;
            if (payload != null && payload.has("OrderItems") && payload.get("OrderItems").isArray()) {
                int lineNo = 1;
                for (JsonNode it : payload.get("OrderItems")) {
                    ChannelOrderItemDto item = new ChannelOrderItemDto();
                    item.setLineNo(lineNo++);
                    item.setExternalSku(it.has("SellerSKU") ? it.get("SellerSKU").asText() : "");
                    item.setItemTitle(it.has("Title") ? it.get("Title").asText() : "");
                    item.setQty(it.has("QuantityOrdered") ? it.get("QuantityOrdered").asInt(1) : 1);
                    item.setUnitPrice(it.has("ItemPrice") && it.get("ItemPrice").has("Amount") ? new BigDecimal(it.get("ItemPrice").get("Amount").asText()) : BigDecimal.ZERO);
                    items.add(item);
                }
            }
        } catch (Exception e) {
            log.warn("parseOrderItems orderId={} error={}", orderId, e.getMessage());
        }
        dto.setItems(items);
        return dto;
    }

    private static class CachedToken {
        final String accessToken;
        final Instant expiresAt;
        CachedToken(String accessToken, Instant expiresAt) {
            this.accessToken = accessToken;
            this.expiresAt = expiresAt;
        }
    }

    private static class GetOrdersPage {
        final boolean authFailed;
        final int httpStatus;
        final String errorMessage;
        final List<String> orderIds;
        final String nextToken;
        GetOrdersPage(boolean authFailed, int httpStatus, String errorMessage, List<String> orderIds, String nextToken) {
            this.authFailed = authFailed;
            this.httpStatus = httpStatus;
            this.errorMessage = errorMessage;
            this.orderIds = orderIds != null ? orderIds : new ArrayList<>();
            this.nextToken = nextToken;
        }
    }
}
