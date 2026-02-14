package com.pod.inv.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("inv_ledger")
public class InventoryLedger extends BaseEntity {

    private String txnNo;
    private String txnType; // RESERVE, RELEASE, IN, OUT
    private String bizType;
    private String bizNo;
    private Long warehouseId;
    private Long locationId;
    private Long skuId;
    private Integer deltaQty;
    private Integer beforeOnHand;
    private Integer afterOnHand;
    private Integer beforeAllocated;
    private Integer afterAllocated;
    private String remark;
    private String extraJson;

    // --- Getters & Setters ---

    public String getTxnNo() { return txnNo; }
    public void setTxnNo(String txnNo) { this.txnNo = txnNo; }
    public String getTxnType() { return txnType; }
    public void setTxnType(String txnType) { this.txnType = txnType; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getBizNo() { return bizNo; }
    public void setBizNo(String bizNo) { this.bizNo = bizNo; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Integer getDeltaQty() { return deltaQty; }
    public void setDeltaQty(Integer deltaQty) { this.deltaQty = deltaQty; }
    public Integer getBeforeOnHand() { return beforeOnHand; }
    public void setBeforeOnHand(Integer beforeOnHand) { this.beforeOnHand = beforeOnHand; }
    public Integer getAfterOnHand() { return afterOnHand; }
    public void setAfterOnHand(Integer afterOnHand) { this.afterOnHand = afterOnHand; }
    public Integer getBeforeAllocated() { return beforeAllocated; }
    public void setBeforeAllocated(Integer beforeAllocated) { this.beforeAllocated = beforeAllocated; }
    public Integer getAfterAllocated() { return afterAllocated; }
    public void setAfterAllocated(Integer afterAllocated) { this.afterAllocated = afterAllocated; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getExtraJson() { return extraJson; }
    public void setExtraJson(String extraJson) { this.extraJson = extraJson; }
}
