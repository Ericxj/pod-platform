package com.pod.mes.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

@TableName("mes_work_order_op")
public class WorkOrderOp extends BaseEntity {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAILED = "FAILED";

    private Long workOrderId;
    private Integer stepNo;
    private String opCode;
    private String status;
    private Long workstationId;
    private Long equipmentId;
    private java.time.LocalDateTime startAt;
    private java.time.LocalDateTime endAt;
    private String resultJson;

    public static WorkOrderOp create(Long workOrderId, Integer stepNo, String opCode) {
        WorkOrderOp op = new WorkOrderOp();
        op.setWorkOrderId(workOrderId);
        op.setStepNo(stepNo);
        op.setOpCode(opCode);
        op.setStatus(STATUS_PENDING);
        return op;
    }

    public void start() {
        if (!STATUS_PENDING.equals(this.status)) {
            throw new BusinessException("Cannot start Operation from status: " + this.status);
        }
        this.status = STATUS_RUNNING;
        this.startAt = java.time.LocalDateTime.now();
    }

    public void finish() {
        if (!STATUS_RUNNING.equals(this.status)) {
            throw new BusinessException("Cannot finish Operation from status: " + this.status);
        }
        this.status = STATUS_DONE;
        this.endAt = java.time.LocalDateTime.now();
    }

    public void fail() {
        this.status = STATUS_FAILED;
        this.endAt = java.time.LocalDateTime.now();
    }

    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public Integer getStepNo() { return stepNo; }
    public void setStepNo(Integer stepNo) { this.stepNo = stepNo; }
    public String getOpCode() { return opCode; }
    public void setOpCode(String opCode) { this.opCode = opCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getWorkstationId() { return workstationId; }
    public void setWorkstationId(Long workstationId) { this.workstationId = workstationId; }
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public java.time.LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(java.time.LocalDateTime startAt) { this.startAt = startAt; }
    public java.time.LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(java.time.LocalDateTime endAt) { this.endAt = endAt; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
}
