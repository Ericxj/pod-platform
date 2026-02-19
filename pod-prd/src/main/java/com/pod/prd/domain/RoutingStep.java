package com.pod.prd.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("prd_routing_step")
public class RoutingStep extends BaseEntity {

    private Long routingId;
    private Integer stepNo;
    private String opCode;
    private String opName;
    private String equipmentType;
    private Integer stdCycleSeconds;
    private Integer qcRequired;
    private String configJson;

    public Long getRoutingId() { return routingId; }
    public void setRoutingId(Long routingId) { this.routingId = routingId; }
    public Integer getStepNo() { return stepNo; }
    public void setStepNo(Integer stepNo) { this.stepNo = stepNo; }
    public String getOpCode() { return opCode; }
    public void setOpCode(String opCode) { this.opCode = opCode; }
    public String getOpName() { return opName; }
    public void setOpName(String opName) { this.opName = opName; }
    public String getEquipmentType() { return equipmentType; }
    public void setEquipmentType(String equipmentType) { this.equipmentType = equipmentType; }
    public Integer getStdCycleSeconds() { return stdCycleSeconds; }
    public void setStdCycleSeconds(Integer stdCycleSeconds) { this.stdCycleSeconds = stdCycleSeconds; }
    public Integer getQcRequired() { return qcRequired; }
    public void setQcRequired(Integer qcRequired) { this.qcRequired = qcRequired; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
}
