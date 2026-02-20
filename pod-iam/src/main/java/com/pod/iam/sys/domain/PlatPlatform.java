package com.pod.iam.sys.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

@TableName("plat_platform")
public class PlatPlatform extends BaseEntity {

    public static final String STATUS_ENABLED = "ENABLED";
    public static final String STATUS_DISABLED = "DISABLED";

    @TableField("platform_code")
    private String platformCode;
    @TableField("platform_name")
    private String platformName;
    private String status;

    public void enable() {
        if (STATUS_DISABLED.equals(this.status)) this.status = STATUS_ENABLED;
    }

    public void disable() {
        if (STATUS_ENABLED.equals(this.status)) this.status = STATUS_DISABLED;
    }

    public void validateForCreate() {
        if (platformCode == null || platformCode.isBlank()) throw new BusinessException("platform_code required");
        if (platformName == null || platformName.isBlank()) throw new BusinessException("platform_name required");
        this.status = status != null && STATUS_DISABLED.equals(status) ? STATUS_DISABLED : STATUS_ENABLED;
    }

    public void validateForUpdate() {
        if (platformName != null && platformName.isBlank()) throw new BusinessException("platform_name cannot be blank");
    }

    public String getPlatformCode() { return platformCode; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }
    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
