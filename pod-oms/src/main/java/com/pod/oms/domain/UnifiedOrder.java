package com.pod.oms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@TableName("oms_unified_order")
public class UnifiedOrder extends BaseEntity {

    private String unifiedOrderNo;
    private String platformCode;
    private Long shopId;
    private String platformOrderId;
    private String platformOrderNo;
    private String serviceLevel;
    private LocalDateTime orderCreatedAt;
    private String buyerName;
    private String buyerEmail;
    private String buyerNote;
    private String currency;
    private BigDecimal totalAmount;
    private BigDecimal shippingAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private String orderStatus;
    private String paymentStatus;
    private Integer riskFlag;
    private Long sourceRawOrderId;
    private String extraJson;

    @TableField(exist = false)
    private List<UnifiedOrderItem> items = new ArrayList<>();

    // --- Behavior Methods ---

    public void validate() {
        if (!"NEW".equals(this.orderStatus)) {
            throw new BusinessException("Only NEW orders can be validated. Current status: " + this.orderStatus);
        }
        
        if (items == null || items.isEmpty()) {
            this.orderStatus = "INVALID";
            throw new BusinessException("Order must have at least one item");
        }
        
        if (totalAmount != null && totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            this.orderStatus = "INVALID";
             throw new BusinessException("Total amount cannot be negative");
        }

        this.orderStatus = "VALIDATED";
    }

    public void cancel() {
        if ("COMPLETED".equals(this.orderStatus)) {
            throw new BusinessException("Cannot cancel completed order");
        }
        this.orderStatus = "CANCELLED";
    }

    // --- Getters & Setters ---
    public String getUnifiedOrderNo() { return unifiedOrderNo; }
    public void setUnifiedOrderNo(String unifiedOrderNo) { this.unifiedOrderNo = unifiedOrderNo; }
    public String getPlatformCode() { return platformCode; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public String getPlatformOrderId() { return platformOrderId; }
    public void setPlatformOrderId(String platformOrderId) { this.platformOrderId = platformOrderId; }
    public String getPlatformOrderNo() { return platformOrderNo; }
    public void setPlatformOrderNo(String platformOrderNo) { this.platformOrderNo = platformOrderNo; }
    public String getServiceLevel() { return serviceLevel; }
    public void setServiceLevel(String serviceLevel) { this.serviceLevel = serviceLevel; }
    public LocalDateTime getOrderCreatedAt() { return orderCreatedAt; }
    public void setOrderCreatedAt(LocalDateTime orderCreatedAt) { this.orderCreatedAt = orderCreatedAt; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }
    public String getBuyerNote() { return buyerNote; }
    public void setBuyerNote(String buyerNote) { this.buyerNote = buyerNote; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getShippingAmount() { return shippingAmount; }
    public void setShippingAmount(BigDecimal shippingAmount) { this.shippingAmount = shippingAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Integer getRiskFlag() { return riskFlag; }
    public void setRiskFlag(Integer riskFlag) { this.riskFlag = riskFlag; }
    public Long getSourceRawOrderId() { return sourceRawOrderId; }
    public void setSourceRawOrderId(Long sourceRawOrderId) { this.sourceRawOrderId = sourceRawOrderId; }
    public String getExtraJson() { return extraJson; }
    public void setExtraJson(String extraJson) { this.extraJson = extraJson; }
    public List<UnifiedOrderItem> getItems() { return items; }
    public void setItems(List<UnifiedOrderItem> items) { this.items = items; }
}
