package com.pod.inv.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

@TableName("inv_inventory")
public class Inventory extends BaseEntity {

    private String skuCode;
    private Integer quantity; // Total physical quantity
    private Integer reservedQuantity; // Reserved quantity
    private String traceId;

    // Getters and Setters
    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    // Domain Behaviors

    /**
     * Reserve inventory.
     * @param amount Quantity to reserve.
     */
    public void reserve(int amount) {
        if (amount <= 0) {
            throw new BusinessException("Reserve amount must be positive");
        }
        int available = this.quantity - this.reservedQuantity;
        if (available < amount) {
            throw new BusinessException("Insufficient inventory for SKU: " + this.skuCode);
        }
        this.reservedQuantity += amount;
    }

    /**
     * Release reserved inventory (e.g. cancellation).
     * @param amount Quantity to release back to available.
     */
    public void release(int amount) {
        if (amount <= 0) {
            throw new BusinessException("Release amount must be positive");
        }
        if (this.reservedQuantity < amount) {
            throw new BusinessException("Cannot release more than reserved for SKU: " + this.skuCode);
        }
        this.reservedQuantity -= amount;
    }

    /**
     * Deduct inventory (e.g. shipment).
     * Reduces both quantity and reserved_quantity.
     * @param amount Quantity to deduct.
     */
    public void deduct(int amount) {
        if (amount <= 0) {
            throw new BusinessException("Deduct amount must be positive");
        }
        if (this.reservedQuantity < amount) {
             throw new BusinessException("Cannot deduct more than reserved for SKU: " + this.skuCode);
        }
        this.reservedQuantity -= amount;
        this.quantity -= amount;
    }
}
