package com.pod.iam.sys.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.time.LocalDateTime;

@TableName("plat_api_credential")
public class PlatApiCredential extends BaseEntity {

    public static final String STATUS_ENABLED = "ENABLED";
    public static final String STATUS_DISABLED = "DISABLED";

    @TableField("platform_code")
    private String platformCode;
    @TableField("shop_id")
    private Long shopId;
    @TableField("auth_type")
    private String authType;
    @TableField("credential_name")
    private String credentialName;
    @TableField("encrypted_payload")
    private String encryptedPayload;
    @TableField("expires_at")
    private LocalDateTime expiresAt;
    @TableField("refresh_expires_at")
    private LocalDateTime refreshExpiresAt;
    @TableField("last_refresh_at")
    private LocalDateTime lastRefreshAt;
    private String status;

    public void enable() {
        if (STATUS_DISABLED.equals(this.status)) this.status = STATUS_ENABLED;
    }

    public void disable() {
        if (STATUS_ENABLED.equals(this.status)) this.status = STATUS_DISABLED;
    }

    public void validateForCreate() {
        if (platformCode == null || platformCode.isBlank()) throw new BusinessException("platform_code required");
        if (shopId == null) throw new BusinessException("shop_id required");
        if (authType == null || authType.isBlank()) throw new BusinessException("auth_type required");
        this.status = status != null && STATUS_DISABLED.equals(status) ? STATUS_DISABLED : STATUS_ENABLED;
    }

    public void validateForUpdate() {
        // no required fields for partial update
    }

    public String getPlatformCode() { return platformCode; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public String getCredentialName() { return credentialName; }
    public void setCredentialName(String credentialName) { this.credentialName = credentialName; }
    public String getEncryptedPayload() { return encryptedPayload; }
    public void setEncryptedPayload(String encryptedPayload) { this.encryptedPayload = encryptedPayload; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getRefreshExpiresAt() { return refreshExpiresAt; }
    public void setRefreshExpiresAt(LocalDateTime refreshExpiresAt) { this.refreshExpiresAt = refreshExpiresAt; }
    public LocalDateTime getLastRefreshAt() { return lastRefreshAt; }
    public void setLastRefreshAt(LocalDateTime lastRefreshAt) { this.lastRefreshAt = lastRefreshAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
