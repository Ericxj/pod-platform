package com.pod.tms.gateway.impl;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SigV4 签名：Authorization 形状与 canonical 相关头存在性。
 */
class SigV4SignerTest {

    @Test
    void sign_producesAuthorizationWithAlgorithmAndCredentialScope() {
        SigV4Signer signer = new SigV4Signer("us-east-1", "execute-api", "AKID", "secret");
        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Instant now = Instant.parse("2026-02-14T12:00:00Z");
        byte[] body = "{\"marketplaceId\":\"ATVPDKIKX0DER\"}".getBytes(StandardCharsets.UTF_8);
        signer.sign("POST", "sellingpartnerapi-na.amazon.com", "/orders/v0/orders/123/shipmentConfirmation", null, body, "atza|token", now, headers);

        assertNotNull(headers.get("Authorization"));
        String auth = headers.get("Authorization").get(0);
        assertTrue(auth.startsWith("AWS4-HMAC-SHA256 "));
        assertTrue(auth.contains("Credential=AKID/"));
        assertTrue(auth.contains("/us-east-1/execute-api/aws4_request"));
        assertTrue(auth.contains("SignedHeaders="));
        assertTrue(auth.contains("Signature="));
        assertTrue(headers.containsKey("host"));
        assertTrue(headers.containsKey("x-amz-date"));
        assertTrue(headers.containsKey("x-amz-access-token"));
        assertEquals("sellingpartnerapi-na.amazon.com", headers.get("host").get(0));
        assertEquals("20260214T120000Z", headers.get("x-amz-date").get(0));
    }

    @Test
    void sign_sameInputs_producesDeterministicSignature() {
        SigV4Signer signer = new SigV4Signer("eu-west-1", "execute-api", "K", "S");
        Map<String, List<String>> h1 = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, List<String>> h2 = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Instant t = Instant.parse("2026-02-14T00:00:00Z");
        byte[] body = new byte[0];
        signer.sign("POST", "sellingpartnerapi-eu.amazon.com", "/orders/v0/orders/oid/shipmentConfirmation", null, body, "token", t, h1);
        signer.sign("POST", "sellingpartnerapi-eu.amazon.com", "/orders/v0/orders/oid/shipmentConfirmation", null, body, "token", t, h2);
        assertEquals(h1.get("Authorization"), h2.get("Authorization"));
    }
}
