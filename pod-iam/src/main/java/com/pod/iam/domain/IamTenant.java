package com.pod.iam.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import java.time.LocalDateTime;

@TableName("iam_tenant")
public class IamTenant extends BaseEntity {
    private String tenantCode;
    private String tenantName;
    private String status;
    private String planType;
    private LocalDateTime planExpireAt;

    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public LocalDateTime getPlanExpireAt() { return planExpireAt; }
    public void setPlanExpireAt(LocalDateTime planExpireAt) { this.planExpireAt = planExpireAt; }
}
