package com.pod.tms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("tms_shipment")
public class Shipment extends BaseEntity {

    /** P1.6 状态：CREATED -> LABEL_CREATED -> HANDED_OVER -> TRACKING_SYNCED | FAILED */
    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_LABEL_CREATED = "LABEL_CREATED";
    public static final String STATUS_HANDED_OVER = "HANDED_OVER";
    public static final String STATUS_TRACKING_SYNCED = "TRACKING_SYNCED";
    public static final String STATUS_FAILED = "FAILED";
    /** 兼容旧：LABELED/SHIPPED/DELIVERED/EXCEPTION */
    public static final String STATUS_LABELED = "LABELED";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_EXCEPTION = "EXCEPTION";

    public static final String SOURCE_TYPE_WMS_OUTBOUND = "WMS_OUTBOUND";

    private String shipmentNo;
    private Long fulfillmentId;
    private Long outboundId;
    private Long carrierId;
    private Long methodId;

    @TableField("source_type")
    private String sourceType;
    @TableField("source_no")
    private String sourceNo;
    @TableField("carrier_code")
    private String carrierCode;
    @TableField("service_code")
    private String serviceCode;

    private String trackingNo;
    private String labelUrl;
    private String labelFormat;
    private String status;
    @TableField("fail_reason")
    private String failReason;

    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private BigDecimal costAmount;
    private String currency;
    private String shipToAddressJson;

    // --- P1.6 状态机：CREATED -> LABEL_CREATED -> HANDED_OVER -> TRACKING_SYNCED | FAILED ---

    public void markLabelCreated(String trackingNo, String labelUrl) {
        if (!STATUS_CREATED.equals(this.status) && !STATUS_FAILED.equals(this.status)) {
            throw new BusinessException("Shipment must be CREATED or FAILED to create label. Current: " + this.status);
        }
        this.trackingNo = trackingNo;
        this.labelUrl = labelUrl;
        this.status = STATUS_LABEL_CREATED;
        this.failReason = null;
    }

    public void markHandedOver() {
        if (!STATUS_LABEL_CREATED.equals(this.status) && !STATUS_HANDED_OVER.equals(this.status)) {
            throw new BusinessException("Shipment must be LABEL_CREATED to handover. Current: " + this.status);
        }
        this.status = STATUS_HANDED_OVER;
    }

    public void markTrackingSynced() {
        if (!STATUS_LABEL_CREATED.equals(this.status) && !STATUS_HANDED_OVER.equals(this.status)) {
            throw new BusinessException("Shipment must be LABEL_CREATED or HANDED_OVER to sync. Current: " + this.status);
        }
        this.status = STATUS_TRACKING_SYNCED;
    }

    public void markFailed(String reason) {
        this.status = STATUS_FAILED;
        this.failReason = reason != null && reason.length() > 512 ? reason.substring(0, 512) : reason;
    }

    public void label(String trackingNo, String labelUrl) {
        if (!STATUS_CREATED.equals(this.status) && !STATUS_LABELED.equals(this.status)) {
            throw new BusinessException("Shipment must be CREATED to label. Current: " + this.status);
        }
        this.trackingNo = trackingNo;
        this.labelUrl = labelUrl;
        this.status = STATUS_LABELED;
    }

    public void confirmShip(LocalDateTime shippedAt) {
        if (!STATUS_LABELED.equals(this.status)) {
            throw new BusinessException("Shipment must be LABELED to confirm ship. Current: " + this.status);
        }
        this.shippedAt = shippedAt;
        this.status = STATUS_SHIPPED;
    }

    public void confirmDelivery(LocalDateTime deliveredAt) {
        if (!STATUS_SHIPPED.equals(this.status)) {
            throw new BusinessException("Shipment must be SHIPPED to confirm delivery. Current: " + this.status);
        }
        this.deliveredAt = deliveredAt;
        this.status = STATUS_DELIVERED;
    }

    // --- Getters and Setters ---

    public String getShipmentNo() { return shipmentNo; }
    public void setShipmentNo(String shipmentNo) { this.shipmentNo = shipmentNo; }
    public Long getFulfillmentId() { return fulfillmentId; }
    public void setFulfillmentId(Long fulfillmentId) { this.fulfillmentId = fulfillmentId; }
    public Long getOutboundId() { return outboundId; }
    public void setOutboundId(Long outboundId) { this.outboundId = outboundId; }
    public Long getCarrierId() { return carrierId; }
    public void setCarrierId(Long carrierId) { this.carrierId = carrierId; }
    public Long getMethodId() { return methodId; }
    public void setMethodId(Long methodId) { this.methodId = methodId; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceNo() { return sourceNo; }
    public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
    public String getCarrierCode() { return carrierCode; }
    public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }
    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public String getLabelUrl() { return labelUrl; }
    public void setLabelUrl(String labelUrl) { this.labelUrl = labelUrl; }
    public String getLabelFormat() { return labelFormat; }
    public void setLabelFormat(String labelFormat) { this.labelFormat = labelFormat; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public BigDecimal getCostAmount() { return costAmount; }
    public void setCostAmount(BigDecimal costAmount) { this.costAmount = costAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getShipToAddressJson() { return shipToAddressJson; }
    public void setShipToAddressJson(String shipToAddressJson) { this.shipToAddressJson = shipToAddressJson; }
}
