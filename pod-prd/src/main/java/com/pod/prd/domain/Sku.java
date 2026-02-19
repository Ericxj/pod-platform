package com.pod.prd.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.math.BigDecimal;

/**
 * SKU 聚合根。状态 DRAFT/ACTIVE/INACTIVE。
 * 激活条件（可配置）：条码>=1；BOM/Routing 可发布后再激活（默认条码必须>=1）。
 */
@TableName("prd_sku")
public class Sku extends BaseEntity {

    private Long spuId;
    private String skuCode;
    private String skuName;
    private BigDecimal price;
    private Integer weightG;
    private String attributesJson;
    private String status;

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    /**
     * 激活：校验条码>=1（若 requireBarcodeForActivate），BOM/Routing 可为发布后再激活（默认仅条码必填）。
     */
    public void activate(boolean hasAtLeastOneBarcode, boolean bomPublishedOrNotRequired, boolean routingPublishedOrNotRequired) {
        if (STATUS_ACTIVE.equals(this.status)) {
            return;
        }
        if (!STATUS_DRAFT.equals(this.status)) {
            throw new BusinessException("Only DRAFT SKU can be activated");
        }
        if (!hasAtLeastOneBarcode) {
            throw new BusinessException("SKU must have at least one barcode before activation");
        }
        if (!bomPublishedOrNotRequired) {
            throw new BusinessException("BOM must be published before SKU activation (or disable check)");
        }
        if (!routingPublishedOrNotRequired) {
            throw new BusinessException("Routing must be published before SKU activation (or disable check)");
        }
        this.status = STATUS_ACTIVE;
    }

    public void deactivate() {
        if (STATUS_ACTIVE.equals(this.status)) {
            this.status = STATUS_INACTIVE;
        }
    }

    public void update(String skuName, BigDecimal price, Integer weightG, String attributesJson) {
        if (skuName != null) this.skuName = skuName;
        if (price != null) this.price = price;
        if (weightG != null) this.weightG = weightG;
        if (attributesJson != null) this.attributesJson = attributesJson;
    }

    public Long getSpuId() { return spuId; }
    public void setSpuId(Long spuId) { this.spuId = spuId; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getWeightG() { return weightG; }
    public void setWeightG(Integer weightG) { this.weightG = weightG; }
    public String getAttributesJson() { return attributesJson; }
    public void setAttributesJson(String attributesJson) { this.attributesJson = attributesJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
