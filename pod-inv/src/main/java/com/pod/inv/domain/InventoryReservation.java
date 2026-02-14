package com.pod.inv.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

import java.time.LocalDateTime;

@TableName("inv_reservation")
public class InventoryReservation extends BaseEntity {

    private String bizType;
    private String bizNo;
    private Long warehouseId;
    private Long skuId;
    private Integer qty;
    private String status; // RESERVED, RELEASED, CONSUMED
    private LocalDateTime expireAt;
    private String remark;

    public static InventoryReservation create(String bizType, String bizNo, Long warehouseId, Long skuId, int qty) {
        InventoryReservation res = new InventoryReservation();
        res.setBizType(bizType);
        res.setBizNo(bizNo);
        res.setWarehouseId(warehouseId);
        res.setSkuId(skuId);
        res.setQty(qty);
        res.setStatus("RESERVED");
        return res;
    }

    public void release() {
        this.status = "RELEASED";
    }

    public void consume() {
        this.status = "CONSUMED";
    }

    // --- Getters & Setters ---

    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getBizNo() { return bizNo; }
    public void setBizNo(String bizNo) { this.bizNo = bizNo; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
