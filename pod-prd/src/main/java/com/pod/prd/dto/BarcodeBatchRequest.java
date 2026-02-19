package com.pod.prd.dto;

import java.util.List;

public class BarcodeBatchRequest {
    private Long skuId;
    private List<String> barcodes;
    private String barcodeType;
    private Integer isPrimary;
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public List<String> getBarcodes() { return barcodes; }
    public void setBarcodes(List<String> barcodes) { this.barcodes = barcodes; }
    public String getBarcodeType() { return barcodeType; }
    public void setBarcodeType(String barcodeType) { this.barcodeType = barcodeType; }
    public Integer getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Integer isPrimary) { this.isPrimary = isPrimary; }
}
