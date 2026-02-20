package com.pod.wms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

import java.time.LocalDateTime;

@TableName("wms_shipment")
public class WmsShipment extends BaseEntity {

    @TableField("outbound_id")
    private Long outboundId;
    @TableField("outbound_no")
    private String outboundNo;
    @TableField("carrier_code")
    private String carrierCode;
    @TableField("tracking_no")
    private String trackingNo;
    @TableField("shipped_at")
    private LocalDateTime shippedAt;
    @TableField("pack_id")
    private Long packId;

    public Long getOutboundId() { return outboundId; }
    public void setOutboundId(Long outboundId) { this.outboundId = outboundId; }
    public String getOutboundNo() { return outboundNo; }
    public void setOutboundNo(String outboundNo) { this.outboundNo = outboundNo; }
    public String getCarrierCode() { return carrierCode; }
    public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }
    public Long getPackId() { return packId; }
    public void setPackId(Long packId) { this.packId = packId; }
}
