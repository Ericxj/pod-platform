package com.pod.mes.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@TableName("mes_work_order")
public class WorkOrder extends BaseEntity {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_SCHEDULED = "SCHEDULED";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_QC = "QC";
    public static final String STATUS_FINISHED = "FINISHED";
    public static final String STATUS_FAILED = "FAILED";

    // Error Codes
    public static final int ERR_PRECONDITION_FAILED = 412;
    public static final int ERR_CONCURRENT_MODIFICATION = 409;

    private String workOrderNo;
    private Long fulfillmentId;
    private Long routingId;
    private String status;
    private Integer priority;
    private java.time.LocalDateTime plannedStartAt;
    private java.time.LocalDateTime plannedEndAt;
    private String remark;

    public static WorkOrder create(Long fulfillmentId, String workOrderNo) {
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(workOrderNo);
        wo.setFulfillmentId(fulfillmentId);
        wo.setStatus(STATUS_CREATED);
        wo.setPriority(100);
        return wo;
    }

    public void release() {
        if (!STATUS_CREATED.equals(this.status)) {
            throw new BusinessException("Cannot release WO from status: " + this.status);
        }
        this.status = STATUS_SCHEDULED;
    }

    public void start() {
        if (!STATUS_SCHEDULED.equals(this.status) && !STATUS_RUNNING.equals(this.status)) {
            throw new BusinessException("Cannot start WO from status: " + this.status);
        }
        this.status = STATUS_RUNNING;
    }

    public void complete() {
        if (!STATUS_RUNNING.equals(this.status) && !STATUS_QC.equals(this.status)) {
             throw new BusinessException("Cannot complete WO from status: " + this.status);
        }
        this.status = STATUS_FINISHED;
    }

    /**
     * Start an operation, enforcing sequential dependency.
     */
    public void startOp(WorkOrderOp op, List<WorkOrderOp> allOps) {
        if (op == null) throw new BusinessException("Operation cannot be null");
        
        // 1. Check current status
        if (!WorkOrderOp.STATUS_PENDING.equals(op.getStatus())) {
             throw new BusinessException(ERR_PRECONDITION_FAILED, "Operation must be PENDING to start");
        }

        // 2. Check predecessor (Step No > 1)
        validatePredecessorDone(op, allOps);

        // 3. Update Operation State
        op.start();

        // 4. Update WorkOrder State if needed
        if (STATUS_SCHEDULED.equals(this.status) || STATUS_CREATED.equals(this.status)) {
            this.status = STATUS_RUNNING;
        }
    }

    /**
     * Finish an operation, enforcing sequential dependency.
     */
    public void finishOp(WorkOrderOp op, List<WorkOrderOp> allOps) {
        if (op == null) throw new BusinessException("Operation cannot be null");

        // 1. Check current status
        if (!WorkOrderOp.STATUS_RUNNING.equals(op.getStatus())) {
             throw new BusinessException(ERR_PRECONDITION_FAILED, "Operation must be RUNNING to finish");
        }

        // 2. Check predecessor (Step No > 1) - Double Check
        validatePredecessorDone(op, allOps);

        // 3. Update Operation State
        op.finish();

        // 4. Check if all ops are done
        boolean allDone = allOps.stream()
                .filter(o -> !o.getId().equals(op.getId())) // exclude current one as it's just finished in memory
                .allMatch(o -> WorkOrderOp.STATUS_DONE.equals(o.getStatus()));
        
        // Since op.finish() updates status in memory, the stream check above might see OLD status for current op if we didn't exclude it.
        // Actually, op object is reference passed, so op.status is already DONE.
        // So:
        allDone = allOps.stream().allMatch(o -> WorkOrderOp.STATUS_DONE.equals(o.getStatus()));

        if (allDone) {
            this.complete();
        }
    }

    private void validatePredecessorDone(WorkOrderOp currentOp, List<WorkOrderOp> allOps) {
        if (currentOp.getStepNo() > 1) {
            int prevStepNo = currentOp.getStepNo() - 1;
            Optional<WorkOrderOp> prevOp = allOps.stream()
                    .filter(o -> Objects.equals(o.getStepNo(), prevStepNo))
                    .findFirst();
            
            if (prevOp.isPresent()) {
                if (!WorkOrderOp.STATUS_DONE.equals(prevOp.get().getStatus())) {
                    throw new BusinessException(ERR_PRECONDITION_FAILED, 
                        "Previous operation (Step " + prevStepNo + ") is not DONE. Current status: " + prevOp.get().getStatus());
                }
            } else {
                // If previous step is missing, should we fail? Yes, gap in sequence.
                throw new BusinessException(ERR_PRECONDITION_FAILED, "Previous operation (Step " + prevStepNo + ") not found");
            }
        }
    }

    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Long getFulfillmentId() { return fulfillmentId; }
    public void setFulfillmentId(Long fulfillmentId) { this.fulfillmentId = fulfillmentId; }
    public Long getRoutingId() { return routingId; }
    public void setRoutingId(Long routingId) { this.routingId = routingId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public java.time.LocalDateTime getPlannedStartAt() { return plannedStartAt; }
    public void setPlannedStartAt(java.time.LocalDateTime plannedStartAt) { this.plannedStartAt = plannedStartAt; }
    public java.time.LocalDateTime getPlannedEndAt() { return plannedEndAt; }
    public void setPlannedEndAt(java.time.LocalDateTime plannedEndAt) { this.plannedEndAt = plannedEndAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
