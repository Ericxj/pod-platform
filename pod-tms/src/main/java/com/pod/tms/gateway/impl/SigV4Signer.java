package com.pod.tms.gateway.impl;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * AWS Signature Version 4（JDK 实现，不依赖 AWS SDK）。
 * service 固定为 execute-api，用于 SP-API 请求签名。
 */
public final class SigV4Signer {

    private static final String ALGORITHM = "AWS4-HMAC-SHA256";
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.US).withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.US).withZone(ZoneOffset.UTC);

    private final String region;
    private final String service;
    private final String accessKeyId;
    private final String secretAccessKey;

    public SigV4Signer(String region, String service, String accessKeyId, String secretAccessKey) {
        this.region = region;
        this.service = service;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
    }

    /**
     * 为请求添加 SigV4 所需头：host, x-amz-date, x-amz-access-token, Authorization。
     * method/uri/query 用于构建 canonical request；body 用于 payload hash。
     */
    public void sign(String method, String host, String canonicalUri, String canonicalQuery,
                     byte[] body, String lwaAccessToken, Instant now,
                     Map<String, List<String>> headersOut) {
        String amzDate = TIME_FMT.format(now);
        String dateStamp = DATE_FMT.format(now);
        String payloadHash = sha256Hex(body != null ? body : new byte[0]);

        TreeMap<String, String> canonicalHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        canonicalHeaders.put("host", host);
        canonicalHeaders.put("x-amz-date", amzDate);
        canonicalHeaders.put("x-amz-access-token", lwaAccessToken);

        StringBuilder signedHeaders = new StringBuilder();
        StringBuilder canonicalHeadersStr = new StringBuilder();
        for (Map.Entry<String, String> e : canonicalHeaders.entrySet()) {
            String k = e.getKey().toLowerCase(Locale.US);
            signedHeaders.append(k).append(";");
            canonicalHeadersStr.append(k).append(":").append(e.getValue().trim()).append("\n");
        }
        if (signedHeaders.length() > 0) signedHeaders.setLength(signedHeaders.length() - 1);

        String canonicalRequest = method + "\n"
            + canonicalUri + "\n"
            + (canonicalQuery != null ? canonicalQuery : "") + "\n"
            + canonicalHeadersStr + "\n"
            + signedHeaders + "\n"
            + payloadHash;

        String credentialScope = dateStamp + "/" + region + "/" + service + "/aws4_request";
        String stringToSign = ALGORITHM + "\n"
            + amzDate + "\n"
            + credentialScope + "\n"
            + sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8));

        byte[] signingKey = getSigningKey(dateStamp);
        String signature = hmacHex(signingKey, stringToSign.getBytes(StandardCharsets.UTF_8));

        String authorization = ALGORITHM + " Credential=" + accessKeyId + "/" + credentialScope
            + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;

        headersOut.put("host", list(host));
        headersOut.put("x-amz-date", list(amzDate));
        headersOut.put("x-amz-access-token", list(lwaAccessToken));
        headersOut.put("Authorization", list(authorization));
    }

    private byte[] getSigningKey(String dateStamp) {
        byte[] kSecret = ("AWS4" + secretAccessKey).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmac(kSecret, dateStamp.getBytes(StandardCharsets.UTF_8));
        byte[] kRegion = hmac(kDate, region.getBytes(StandardCharsets.UTF_8));
        byte[] kService = hmac(kRegion, service.getBytes(StandardCharsets.UTF_8));
        return hmac(kService, "aws4_request".getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] hmac(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(key, HMAC_SHA256));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC failed", e);
        }
    }

    private static String hmacHex(byte[] key, byte[] data) {
        return bytesToHex(hmac(key, data));
    }

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return bytesToHex(md.digest(data));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 failed", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static List<String> list(String s) {
        List<String> l = new ArrayList<>();
        l.add(s);
        return l;
    }
}
