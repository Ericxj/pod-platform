package com.pod.wms.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;
import java.util.Arrays;
import java.util.List;

@TableName("wms_outbound_order")
public class OutboundOrder extends BaseEntity {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_ALLOCATED = "ALLOCATED";
    public static final String STATUS_PICKING = "PICKING";
    public static final String STATUS_PICKED = "PICKED";
    public static final String STATUS_PACKED = "PACKED";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private String outboundNo;
    private String outboundType;
    @com.baomidou.mybatisplus.annotation.TableField("source_type")
    private String sourceType;
    private String sourceNo;
    private Long fulfillmentId;
    private Long warehouseId;
    private String shipToAddressJson;
    private String packStrategy;
    private String status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private List<OutboundOrderLine> lines;

    // --- Behaviors ---

    public void allocate() {
        if (!STATUS_CREATED.equals(this.status)) {
            throw new BusinessException("Outbound order must be CREATED to allocate. Current: " + this.status);
        }
        this.status = STATUS_ALLOCATED;
    }

    public void startPicking() {
        if (!STATUS_CREATED.equals(this.status) && !STATUS_PICKING.equals(this.status) && !STATUS_ALLOCATED.equals(this.status)) {
            throw new BusinessException("Outbound order must be CREATED to start picking. Current: " + this.status);
        }
        this.status = STATUS_PICKING;
    }

    public void completePicking() {
        if (!STATUS_PICKING.equals(this.status)) {
            throw new BusinessException("Outbound order must be PICKING to complete picking. Current: " + this.status);
        }
        this.status = STATUS_PICKED;
    }

    public void completePacking() {
        if (!STATUS_PICKED.equals(this.status) && !STATUS_PICKING.equals(this.status)) {
            throw new BusinessException("Outbound order must be PICKED or PICKING to complete packing. Current: " + this.status);
        }
        this.status = STATUS_PACKED;
    }

    public void ship() {
        if (!STATUS_PACKED.equals(this.status)) {
            throw new BusinessException("Outbound order must be PACKED to ship. Current: " + this.status);
        }
        this.status = STATUS_SHIPPED;
    }

    public void cancel() {
        if (STATUS_SHIPPED.equals(this.status)) {
            throw new BusinessException("Cannot cancel SHIPPED order");
        }
        this.status = STATUS_CANCELLED;
    }

    // --- Getters and Setters ---

    public String getOutboundNo() {
        return outboundNo;
    }

    public void setOutboundNo(String outboundNo) {
        this.outboundNo = outboundNo;
    }

    public String getOutboundType() {
        return outboundType;
    }

    public void setOutboundType(String outboundType) {
        this.outboundType = outboundType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceNo() {
        return sourceNo;
    }

    public void setSourceNo(String sourceNo) {
        this.sourceNo = sourceNo;
    }

    public Long getFulfillmentId() {
        return fulfillmentId;
    }

    public void setFulfillmentId(Long fulfillmentId) {
        this.fulfillmentId = fulfillmentId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public List<OutboundOrderLine> getLines() {
        return lines;
    }

    public void setLines(List<OutboundOrderLine> lines) {
        this.lines = lines;
    }

    public String getShipToAddressJson() {
        return shipToAddressJson;
    }

    public void setShipToAddressJson(String shipToAddressJson) {
        this.shipToAddressJson = shipToAddressJson;
    }

    public String getPackStrategy() {
        return packStrategy;
    }

    public void setPackStrategy(String packStrategy) {
        this.packStrategy = packStrategy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
