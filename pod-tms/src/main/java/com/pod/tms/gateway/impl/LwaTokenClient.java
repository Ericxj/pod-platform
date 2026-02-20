package com.pod.tms.gateway.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pod.tms.config.AmazonSpApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LWA access token 获取与缓存。POST https://api.amazon.com/auth/o2/token，grant_type=refresh_token。
 * 缓存到内存，在 expires_in 过期前 60 秒刷新。
 */
@Component
@ConditionalOnProperty(name = "tms.amazon.gateway", havingValue = "real")
public class LwaTokenClient {

    private static final Logger log = LoggerFactory.getLogger(LwaTokenClient.class);
    private static final String TOKEN_URL = "https://api.amazon.com/auth/o2/token";
    private static final int REFRESH_BEFORE_SECONDS = 60;
    private static final ObjectMapper JSON = new ObjectMapper();

    private final AmazonSpApiProperties properties;
    private final ReentrantLock lock = new ReentrantLock();
    private String cachedAccessToken;
    private Instant expiresAt;

    public LwaTokenClient(AmazonSpApiProperties properties) {
        this.properties = properties;
    }

    /**
     * 返回当前有效的 access_token，必要时刷新。
     */
    public String getAccessToken() {
        lock.lock();
        try {
            if (cachedAccessToken != null && expiresAt != null && Instant.now().plusSeconds(REFRESH_BEFORE_SECONDS).isBefore(expiresAt)) {
                return cachedAccessToken;
            }
            refresh();
            return cachedAccessToken;
        } finally {
            lock.unlock();
        }
    }

    private void refresh() {
        String clientId = properties.getLwaClientId();
        String clientSecret = properties.getLwaClientSecret();
        String refreshToken = properties.getLwaRefreshToken();
        if (clientId == null || clientSecret == null || refreshToken == null) {
            throw new IllegalStateException("LWA credentials not configured: lwaClientId, lwaClientSecret, lwaRefreshToken required");
        }
        String body = "grant_type=refresh_token"
            + "&refresh_token=" + urlEncode(refreshToken)
            + "&client_id=" + urlEncode(clientId)
            + "&client_secret=" + urlEncode(clientSecret);
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder().build();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(java.time.Duration.ofSeconds(15))
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                .build();
            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                log.warn("LWA token response status={} body={}", response.statusCode(), response.body());
                throw new IllegalStateException("LWA token failed: status=" + response.statusCode());
            }
            JsonNode node = JSON.readTree(response.body());
            cachedAccessToken = node.has("access_token") ? node.get("access_token").asText() : null;
            int expiresIn = node.has("expires_in") ? node.get("expires_in").asInt(3600) : 3600;
            expiresAt = Instant.now().plusSeconds(expiresIn);
            if (cachedAccessToken == null) {
                throw new IllegalStateException("LWA response missing access_token");
            }
            log.debug("LWA token refreshed, expiresIn={}", expiresIn);
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new IllegalStateException("LWA token refresh failed", e);
        }
    }

    private static String urlEncode(String s) {
        if (s == null) return "";
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
