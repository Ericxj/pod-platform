package com.pod.iam.sys.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

@TableName("plat_shop")
public class PlatShop extends BaseEntity {

    public static final String STATUS_ENABLED = "ENABLED";
    public static final String STATUS_DISABLED = "DISABLED";

    @TableField("platform_code")
    private String platformCode;
    @TableField("shop_code")
    private String shopCode;
    @TableField("shop_name")
    private String shopName;
    @TableField("site_code")
    private String siteCode;
    private String currency;
    private String status;

    public void enable() {
        if (STATUS_DISABLED.equals(this.status)) this.status = STATUS_ENABLED;
    }

    public void disable() {
        if (STATUS_ENABLED.equals(this.status)) this.status = STATUS_DISABLED;
    }

    public void validateForCreate() {
        if (platformCode == null || platformCode.isBlank()) throw new BusinessException("platform_code required");
        if (shopCode == null || shopCode.isBlank()) throw new BusinessException("shop_code required");
        if (shopName == null || shopName.isBlank()) throw new BusinessException("shop_name required");
        this.status = status != null && STATUS_DISABLED.equals(status) ? STATUS_DISABLED : STATUS_ENABLED;
    }

    public void validateForUpdate() {
        if (shopName != null && shopName.isBlank()) throw new BusinessException("shop_name cannot be blank");
    }

    public String getPlatformCode() { return platformCode; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }
    public String getShopCode() { return shopCode; }
    public void setShopCode(String shopCode) { this.shopCode = shopCode; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getSiteCode() { return siteCode; }
    public void setSiteCode(String siteCode) { this.siteCode = siteCode; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
