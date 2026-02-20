package com.pod.tms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Amazon SP-API 配置。prefix = tms.amazon.spapi
 * endpoint 可选；未配置时按 sellingRegion 推导。
 */
@ConfigurationProperties(prefix = "tms.amazon.spapi")
public class AmazonSpApiProperties {

    /** 销售区域：NA / EU / FE，对应官方 endpoint 与 region */
    private String sellingRegion = "NA";
    /** 可选；不配置时由 sellingRegion 推导 */
    private String endpoint;
    /** AWS region，由 sellingRegion 推导：NA=us-east-1, EU=eu-west-1, FE=us-west-2 */
    private String region;
    /** LWA: client_id */
    private String lwaClientId;
    /** LWA: client_secret */
    private String lwaClientSecret;
    /** LWA: refresh_token（生产授权） */
    private String lwaRefreshToken;
    /** AWS IAM: access_key_id（用于 SigV4） */
    private String awsAccessKeyId;
    /** AWS IAM: secret_access_key */
    private String awsSecretAccessKey;
    private int connectTimeoutMs = 10_000;
    private int readTimeoutMs = 30_000;

    public String getSellingRegion() {
        return sellingRegion;
    }

    public void setSellingRegion(String sellingRegion) {
        this.sellingRegion = sellingRegion;
    }

    public String getEndpoint() {
        if (endpoint != null && !endpoint.isBlank()) {
            return endpoint;
        }
        if ("EU".equalsIgnoreCase(sellingRegion)) return "https://sellingpartnerapi-eu.amazon.com";
        if ("FE".equalsIgnoreCase(sellingRegion)) return "https://sellingpartnerapi-fe.amazon.com";
        return "https://sellingpartnerapi-na.amazon.com";
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /** 由 sellingRegion 得到 AWS region：NA=us-east-1, EU=eu-west-1, FE=us-west-2 */
    public String getRegion() {
        if (region != null && !region.isBlank()) {
            return region;
        }
        if ("EU".equalsIgnoreCase(sellingRegion)) return "eu-west-1";
        if ("FE".equalsIgnoreCase(sellingRegion)) return "us-west-2";
        return "us-east-1";
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLwaClientId() { return lwaClientId; }
    public void setLwaClientId(String lwaClientId) { this.lwaClientId = lwaClientId; }
    public String getLwaClientSecret() { return lwaClientSecret; }
    public void setLwaClientSecret(String lwaClientSecret) { this.lwaClientSecret = lwaClientSecret; }
    public String getLwaRefreshToken() { return lwaRefreshToken; }
    public void setLwaRefreshToken(String lwaRefreshToken) { this.lwaRefreshToken = lwaRefreshToken; }
    public String getAwsAccessKeyId() { return awsAccessKeyId; }
    public void setAwsAccessKeyId(String awsAccessKeyId) { this.awsAccessKeyId = awsAccessKeyId; }
    public String getAwsSecretAccessKey() { return awsSecretAccessKey; }
    public void setAwsSecretAccessKey(String awsSecretAccessKey) { this.awsSecretAccessKey = awsSecretAccessKey; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
}
