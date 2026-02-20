package com.pod.iam.sys.dto;

import java.io.Serializable;

public class CredentialCreateDto implements Serializable {
    private String platformCode;
    private Long shopId;
    private String authType;
    private String credentialName;
    private String payloadPlainJson;
    private String status;

    public String getPlatformCode() { return platformCode; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public String getCredentialName() { return credentialName; }
    public void setCredentialName(String credentialName) { this.credentialName = credentialName; }
    public String getPayloadPlainJson() { return payloadPlainJson; }
    public void setPayloadPlainJson(String payloadPlainJson) { this.payloadPlainJson = payloadPlainJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
