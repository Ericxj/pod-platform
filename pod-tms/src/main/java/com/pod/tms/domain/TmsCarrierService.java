package com.pod.tms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("tms_carrier_service")
public class TmsCarrierService extends BaseEntity {

    @TableField("carrier_id")
    private Long carrierId;
    @TableField("service_code")
    private String serviceCode;
    @TableField("service_name")
    private String serviceName;
    private String status;

    public Long getCarrierId() { return carrierId; }
    public void setCarrierId(Long carrierId) { this.carrierId = carrierId; }
    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
