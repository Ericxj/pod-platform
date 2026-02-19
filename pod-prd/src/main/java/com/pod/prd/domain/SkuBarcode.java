package com.pod.prd.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("prd_sku_barcode")
public class SkuBarcode extends BaseEntity {

    private Long skuId;
    private String barcode;
    private String barcodeType;
    private Integer isPrimary;

    public static final String TYPE_UPC = "UPC";
    public static final String TYPE_EAN = "EAN";
    public static final String TYPE_CODE128 = "CODE128";
    public static final String TYPE_OTHER = "OTHER";

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getBarcodeType() { return barcodeType; }
    public void setBarcodeType(String barcodeType) { this.barcodeType = barcodeType; }
    public Integer getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Integer isPrimary) { this.isPrimary = isPrimary; }
}
