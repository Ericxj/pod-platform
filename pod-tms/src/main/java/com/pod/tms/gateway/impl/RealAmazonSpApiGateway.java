package com.pod.tms.gateway.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pod.tms.config.AmazonSpApiProperties;
import com.pod.tms.gateway.AmazonOrderItemDTO;
import com.pod.tms.gateway.AmazonSpApiGateway;
import com.pod.tms.gateway.ConfirmShipmentRequest;
import com.pod.tms.gateway.ConfirmShipmentResult;
import com.pod.tms.gateway.GetOrderItemsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 真实 Amazon SP-API confirmShipment：LWA token + SigV4 + POST /orders/v0/orders/{orderId}/shipmentConfirmation。
 */
@Component
@ConditionalOnProperty(name = "tms.amazon.gateway", havingValue = "real")
public class RealAmazonSpApiGateway implements AmazonSpApiGateway {

    private static final Logger log = LoggerFactory.getLogger(RealAmazonSpApiGateway.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final AmazonSpApiProperties properties;
    private final LwaTokenClient lwaTokenClient;
    private final java.net.http.HttpClient httpClient;

    public RealAmazonSpApiGateway(AmazonSpApiProperties properties, LwaTokenClient lwaTokenClient) {
        this.properties = properties;
        this.lwaTokenClient = lwaTokenClient;
        this.httpClient = java.net.http.HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofMillis(properties.getConnectTimeoutMs()))
            .build();
    }

    @Override
    public ConfirmShipmentResult confirmShipment(String amazonOrderId, ConfirmShipmentRequest request) {
        long startMs = System.currentTimeMillis();
        if (request == null || request.getPackageDetail() == null) {
            log.warn("confirmShipment invalid request orderId={}", amazonOrderId);
            return ConfirmShipmentResult.fail(400, "BadRequest", "Missing packageDetail", null);
        }
        String endpoint = properties.getEndpoint();
        String baseUrl = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        String pathSegment = URLEncoder.encode(amazonOrderId, StandardCharsets.UTF_8).replace("+", "%20");
        String url = baseUrl + "/orders/v0/orders/" + pathSegment + "/shipmentConfirmation";
        String host = hostFromEndpoint(endpoint);
        String canonicalUri = "/orders/v0/orders/" + pathSegment + "/shipmentConfirmation";

        byte[] bodyBytes;
        try {
            bodyBytes = JSON.writeValueAsBytes(request);
        } catch (Exception e) {
            log.warn("confirmShipment serialize failed orderId={}", amazonOrderId, e);
            return ConfirmShipmentResult.fail(500, "SerializationError", e.getMessage(), null);
        }

        String accessToken;
        try {
            accessToken = lwaTokenClient.getAccessToken();
        } catch (Exception e) {
            log.warn("confirmShipment LWA token failed orderId={}", amazonOrderId, e);
            return ConfirmShipmentResult.fail(500, "LwaTokenError", e.getMessage(), null);
        }

        SigV4Signer signer = new SigV4Signer(
            properties.getRegion(),
            "execute-api",
            properties.getAwsAccessKeyId(),
            properties.getAwsSecretAccessKey()
        );
        Instant now = Instant.now();
        Map<String, List<String>> signedHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        signer.sign("POST", host, canonicalUri, null, bodyBytes, accessToken, now, signedHeaders);

        java.net.http.HttpRequest.Builder reqBuilder = java.net.http.HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(java.time.Duration.ofMillis(properties.getReadTimeoutMs()))
            .header("Content-Type", "application/json");
        for (Map.Entry<String, List<String>> e : signedHeaders.entrySet()) {
            for (String v : e.getValue()) reqBuilder.header(e.getKey(), v);
        }
        reqBuilder.POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(bodyBytes));

        int httpStatus;
        String responseBody;
        try {
            java.net.http.HttpResponse<String> response = httpClient.send(reqBuilder.build(),
                java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            httpStatus = response.statusCode();
            responseBody = response.body();
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startMs;
            log.warn("confirmShipment request failed orderId={} durationMs={} error={}", amazonOrderId, durationMs, e.getMessage());
            return ConfirmShipmentResult.fail(500, "NetworkError", e.getMessage(), null);
        }

        long durationMs = System.currentTimeMillis() - startMs;
        if (httpStatus == 204) {
            log.info("confirmShipment success orderId={} httpStatus=204 durationMs={}", amazonOrderId, durationMs);
            return ConfirmShipmentResult.ok(204, responseBody != null ? responseBody : "");
        }

        String errorCode = "HTTP" + httpStatus;
        String errorMessage = responseBody;
        if (responseBody != null && !responseBody.isBlank()) {
            try {
                JsonNode node = JSON.readTree(responseBody);
                if (node.has("errors") && node.get("errors").isArray() && node.get("errors").size() > 0) {
                    JsonNode first = node.get("errors").get(0);
                    if (first.has("code")) errorCode = first.get("code").asText();
                    if (first.has("message")) errorMessage = first.get("message").asText();
                } else if (node.has("code")) errorCode = node.get("code").asText();
                if (node.has("message") && errorMessage == responseBody) errorMessage = node.get("message").asText();
            } catch (Exception ignored) { }
        }
        if (errorMessage == null || errorMessage.isBlank()) errorMessage = "HTTP " + httpStatus;
        log.warn("confirmShipment failed orderId={} httpStatus={} errorCode={} durationMs={}", amazonOrderId, httpStatus, errorCode, durationMs);
        return ConfirmShipmentResult.fail(httpStatus, errorCode, errorMessage, responseBody);
    }

    @Override
    public GetOrderItemsResult getOrderItems(String amazonOrderId) {
        long startMs = System.currentTimeMillis();
        String endpoint = properties.getEndpoint();
        String baseUrl = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        String pathSegment = URLEncoder.encode(amazonOrderId, StandardCharsets.UTF_8).replace("+", "%20");
        String url = baseUrl + "/orders/v0/orders/" + pathSegment + "/orderItems";
        String host = hostFromEndpoint(endpoint);
        String canonicalUri = "/orders/v0/orders/" + pathSegment + "/orderItems";

        String accessToken;
        try {
            accessToken = lwaTokenClient.getAccessToken();
        } catch (Exception e) {
            log.warn("getOrderItems LWA token failed orderId={}", amazonOrderId, e);
            return GetOrderItemsResult.fail(500, "LwaTokenError", e.getMessage(), null);
        }

        byte[] bodyBytes = new byte[0];
        SigV4Signer signer = new SigV4Signer(
            properties.getRegion(),
            "execute-api",
            properties.getAwsAccessKeyId(),
            properties.getAwsSecretAccessKey()
        );
        Instant now = Instant.now();
        Map<String, List<String>> signedHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        signer.sign("GET", host, canonicalUri, null, bodyBytes, accessToken, now, signedHeaders);

        java.net.http.HttpRequest.Builder reqBuilder = java.net.http.HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(java.time.Duration.ofMillis(properties.getReadTimeoutMs()));
        for (Map.Entry<String, List<String>> e : signedHeaders.entrySet()) {
            for (String v : e.getValue()) reqBuilder.header(e.getKey(), v);
        }
        reqBuilder.GET();

        int httpStatus;
        String responseBody;
        try {
            java.net.http.HttpResponse<String> response = httpClient.send(reqBuilder.build(),
                java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            httpStatus = response.statusCode();
            responseBody = response.body();
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startMs;
            log.warn("getOrderItems request failed orderId={} durationMs={} error={}", amazonOrderId, durationMs, e.getMessage());
            return GetOrderItemsResult.fail(500, "NetworkError", e.getMessage(), null);
        }

        long durationMs = System.currentTimeMillis() - startMs;
        if (httpStatus == 200) {
            List<AmazonOrderItemDTO> items = parseOrderItemsPayload(responseBody);
            log.info("getOrderItems success orderId={} itemCount={} durationMs={}", amazonOrderId, items != null ? items.size() : 0, durationMs);
            return GetOrderItemsResult.ok(items != null ? items : new ArrayList<>());
        }

        String errorCode = "HTTP" + httpStatus;
        String errorMessage = responseBody;
        if (responseBody != null && !responseBody.isBlank()) {
            try {
                JsonNode node = JSON.readTree(responseBody);
                if (node.has("errors") && node.get("errors").isArray() && node.get("errors").size() > 0) {
                    JsonNode first = node.get("errors").get(0);
                    if (first.has("code")) errorCode = first.get("code").asText();
                    if (first.has("message")) errorMessage = first.get("message").asText();
                } else if (node.has("code")) errorCode = node.get("code").asText();
                if (node.has("message") && errorMessage == responseBody) errorMessage = node.get("message").asText();
            } catch (Exception ignored) { }
        }
        if (errorMessage == null || errorMessage.isBlank()) errorMessage = "HTTP " + httpStatus;
        log.warn("getOrderItems failed orderId={} httpStatus={} errorCode={} durationMs={}", amazonOrderId, httpStatus, errorCode, durationMs);
        return GetOrderItemsResult.fail(httpStatus, errorCode, errorMessage, responseBody);
    }

    private List<AmazonOrderItemDTO> parseOrderItemsPayload(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) return new ArrayList<>();
        try {
            JsonNode root = JSON.readTree(responseBody);
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
            log.warn("getOrderItems parse payload failed", e);
            return new ArrayList<>();
        }
    }

    private static String hostFromEndpoint(String endpoint) {
        if (endpoint == null) return "sellingpartnerapi-na.amazon.com";
        try {
            URI u = URI.create(endpoint);
            String h = u.getHost();
            return h != null ? h : "sellingpartnerapi-na.amazon.com";
        } catch (Exception e) {
            return "sellingpartnerapi-na.amazon.com";
        }
    }
}
