package com.pod.wms.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

@TableName("wms_pick_task")
public class PickTask extends BaseEntity {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_PICKING = "PICKING";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private String pickTaskNo;
    private Long waveId;
    private Long outboundId;
    private String status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private java.util.List<PickTaskLine> lines;

    public void start() {
        if (!STATUS_CREATED.equals(this.status)) {
            throw new BusinessException("Pick task must be CREATED to start. Current: " + this.status);
        }
        this.status = STATUS_PICKING;
    }

    public void complete() {
        if (!STATUS_PICKING.equals(this.status)) {
            throw new BusinessException("Pick task must be PICKING to complete. Current: " + this.status);
        }
        this.status = STATUS_DONE;
    }

    public void cancel() {
        if (STATUS_DONE.equals(this.status)) {
             throw new BusinessException("Cannot cancel DONE pick task");
        }
        this.status = STATUS_CANCELLED;
    }

    public String getPickTaskNo() {
        return pickTaskNo;
    }

    public void setPickTaskNo(String pickTaskNo) {
        this.pickTaskNo = pickTaskNo;
    }

    public Long getWaveId() {
        return waveId;
    }

    public void setWaveId(Long waveId) {
        this.waveId = waveId;
    }

    public Long getOutboundId() {
        return outboundId;
    }

    public void setOutboundId(Long outboundId) {
        this.outboundId = outboundId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.util.List<PickTaskLine> getLines() {
        return lines;
    }

    public void setLines(java.util.List<PickTaskLine> lines) {
        this.lines = lines;
    }
}
