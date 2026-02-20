package com.pod.tms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("tms_channel_carrier_mapping")
public class TmsChannelCarrierMapping extends BaseEntity {

    @TableField("channel_code")
    private String channelCode;
    @TableField("carrier_code")
    private String carrierCode;
    @TableField("service_code")
    private String serviceCode;
    @TableField("carrier_id")
    private Long carrierId;
    private String status;

    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    public String getCarrierCode() { return carrierCode; }
    public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }
    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }
    public Long getCarrierId() { return carrierId; }
    public void setCarrierId(Long carrierId) { this.carrierId = carrierId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
