package com.pod.tms.gateway.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LWA token 响应 JSON 解析（与 LwaTokenClient 使用相同逻辑）。
 */
class LwaTokenParseTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void parseLwaResponse_containsAccessTokenAndExpiresIn() throws Exception {
        String body = "{\"access_token\":\"Atza|xxx\",\"token_type\":\"bearer\",\"expires_in\":3600}";
        JsonNode node = JSON.readTree(body);
        assertTrue(node.has("access_token"));
        assertEquals("Atza|xxx", node.get("access_token").asText());
        assertTrue(node.has("expires_in"));
        assertEquals(3600, node.get("expires_in").asInt());
    }

    @Test
    void parseLwaResponse_defaultExpiresIn() throws Exception {
        String body = "{\"access_token\":\"t\"}";
        JsonNode node = JSON.readTree(body);
        int expiresIn = node.has("expires_in") ? node.get("expires_in").asInt(3600) : 3600;
        assertEquals(3600, expiresIn);
    }
}
