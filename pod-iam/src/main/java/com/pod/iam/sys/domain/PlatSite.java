package com.pod.iam.sys.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

@TableName("plat_site")
public class PlatSite extends BaseEntity {

    public static final String STATUS_ENABLED = "ENABLED";
    public static final String STATUS_DISABLED = "DISABLED";

    @TableField("platform_code")
    private String platformCode;
    @TableField("site_code")
    private String siteCode;
    @TableField("site_name")
    private String siteName;
    @TableField("country_code")
    private String countryCode;
    private String currency;
    private String timezone;
    private String status;

    public void enable() {
        if (STATUS_DISABLED.equals(this.status)) this.status = STATUS_ENABLED;
    }

    public void disable() {
        if (STATUS_ENABLED.equals(this.status)) this.status = STATUS_DISABLED;
    }

    public void validateForCreate() {
        if (platformCode == null || platformCode.isBlank()) throw new BusinessException("platform_code required");
        if (siteCode == null || siteCode.isBlank()) throw new BusinessException("site_code required");
        if (siteName == null || siteName.isBlank()) throw new BusinessException("site_name required");
        this.status = status != null && STATUS_DISABLED.equals(status) ? STATUS_DISABLED : STATUS_ENABLED;
    }

    public void validateForUpdate() {
        if (siteName != null && siteName.isBlank()) throw new BusinessException("site_name cannot be blank");
    }

    public String getPlatformCode() { return platformCode; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }
    public String getSiteCode() { return siteCode; }
    public void setSiteCode(String siteCode) { this.siteCode = siteCode; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
