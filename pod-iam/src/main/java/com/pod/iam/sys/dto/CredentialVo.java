package com.pod.iam.sys.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 返回给前端：敏感字段已脱敏，不包含 encrypted_payload 明文 */
public class CredentialVo implements Serializable {
    private Long id;
    private String platformCode;
    private Long shopId;
    private String authType;
    private String credentialName;
    /** 脱敏：如 ***abcd 或 masked */
    private String payloadMasked;
    private LocalDateTime expiresAt;
    private LocalDateTime refreshExpiresAt;
    private LocalDateTime lastRefreshAt;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlatformCode() { return platformCode; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public String getCredentialName() { return credentialName; }
    public void setCredentialName(String credentialName) { this.credentialName = credentialName; }
    public String getPayloadMasked() { return payloadMasked; }
    public void setPayloadMasked(String payloadMasked) { this.payloadMasked = payloadMasked; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getRefreshExpiresAt() { return refreshExpiresAt; }
    public void setRefreshExpiresAt(LocalDateTime refreshExpiresAt) { this.refreshExpiresAt = refreshExpiresAt; }
    public LocalDateTime getLastRefreshAt() { return lastRefreshAt; }
    public void setLastRefreshAt(LocalDateTime lastRefreshAt) { this.lastRefreshAt = lastRefreshAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
