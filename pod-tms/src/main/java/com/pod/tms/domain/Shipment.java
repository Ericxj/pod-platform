package com.pod.tms.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("tms_shipment")
public class Shipment extends BaseEntity {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_LABELED = "LABELED";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_EXCEPTION = "EXCEPTION";

    private String shipmentNo;
    private Long fulfillmentId;
    private Long outboundId;
    private Long carrierId;
    private Long methodId;
    private String trackingNo;
    private String labelUrl;
    private String labelFormat;
    private String status;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private BigDecimal costAmount;
    private String currency;
    private String shipToAddressJson;

    // --- Behaviors ---

    public void label(String trackingNo, String labelUrl) {
        if (!STATUS_CREATED.equals(this.status)) {
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

    public String getShipmentNo() {
        return shipmentNo;
    }

    public void setShipmentNo(String shipmentNo) {
        this.shipmentNo = shipmentNo;
    }

    public Long getFulfillmentId() {
        return fulfillmentId;
    }

    public void setFulfillmentId(Long fulfillmentId) {
        this.fulfillmentId = fulfillmentId;
    }

    public Long getOutboundId() {
        return outboundId;
    }

    public void setOutboundId(Long outboundId) {
        this.outboundId = outboundId;
    }

    public Long getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(Long carrierId) {
        this.carrierId = carrierId;
    }

    public Long getMethodId() {
        return methodId;
    }

    public void setMethodId(Long methodId) {
        this.methodId = methodId;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public String getLabelUrl() {
        return labelUrl;
    }

    public void setLabelUrl(String labelUrl) {
        this.labelUrl = labelUrl;
    }

    public String getLabelFormat() {
        return labelFormat;
    }

    public void setLabelFormat(String labelFormat) {
        this.labelFormat = labelFormat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public BigDecimal getCostAmount() {
        return costAmount;
    }

    public void setCostAmount(BigDecimal costAmount) {
        this.costAmount = costAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getShipToAddressJson() {
        return shipToAddressJson;
    }

    public void setShipToAddressJson(String shipToAddressJson) {
        this.shipToAddressJson = shipToAddressJson;
    }
}
