package com.pod.inv.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.math.BigDecimal;

@TableName("inv_balance")
public class InventoryBalance extends BaseEntity {

    private Long warehouseId;
    private Long locationId;
    private Long skuId;
    private String batchNo;
    private String lotJson;
    private Integer onHandQty;
    private Integer allocatedQty;
    private Integer availableQty;

    // --- Behavior ---

    public void reserve(int qty) {
        if (qty <= 0) {
            throw new BusinessException("Reservation quantity must be positive");
        }
        if (this.availableQty < qty) {
            throw new BusinessException("Insufficient inventory for SKU: " + this.skuId);
        }
        this.allocatedQty += qty;
        this.availableQty = this.onHandQty - this.allocatedQty;
    }

    public void release(int qty) {
        if (qty <= 0) {
            throw new BusinessException("Release quantity must be positive");
        }
        if (this.allocatedQty < qty) {
            throw new BusinessException("Cannot release more than allocated quantity");
        }
        this.allocatedQty -= qty;
        this.availableQty = this.onHandQty - this.allocatedQty;
    }
    
    // --- Getters & Setters ---

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getLotJson() { return lotJson; }
    public void setLotJson(String lotJson) { this.lotJson = lotJson; }
    public Integer getOnHandQty() { return onHandQty; }
    public void setOnHandQty(Integer onHandQty) { this.onHandQty = onHandQty; }
    public Integer getAllocatedQty() { return allocatedQty; }
    public void setAllocatedQty(Integer allocatedQty) { this.allocatedQty = allocatedQty; }
    public Integer getAvailableQty() { return availableQty; }
    public void setAvailableQty(Integer availableQty) { this.availableQty = availableQty; }
}
