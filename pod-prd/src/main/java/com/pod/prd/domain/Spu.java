package com.pod.prd.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("prd_spu")
public class Spu extends BaseEntity {
    private String spuCode;
    private String spuName;
    private String categoryCode;
    private String brand;
    private String status;
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public void update(String spuName, String categoryCode, String brand) {
        if (STATUS_ACTIVE.equals(this.status)) {
            if (spuName != null) this.spuName = spuName;
            if (categoryCode != null) this.categoryCode = categoryCode;
            if (brand != null) this.brand = brand;
        }
    }
    public String getSpuCode() { return spuCode; }
    public void setSpuCode(String spuCode) { this.spuCode = spuCode; }
    public String getSpuName() { return spuName; }
    public void setSpuName(String spuName) { this.spuName = spuName; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
