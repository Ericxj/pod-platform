package com.pod.wms.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

@TableName("wms_pick_task_line")
public class PickTaskLine extends BaseEntity {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_DONE = "DONE";

    private Long pickTaskId;
    private Long outboundLineId;
    private Integer lineNo;
    private Long skuId;
    private Integer qty;
    private Integer qtyActual;
    private String status;

    // --- Behaviors ---

    public void confirm(Integer qtyConfirmed) {
        if (qtyConfirmed == null || qtyConfirmed < 0) {
            throw new BusinessException("Invalid quantity confirmed: " + qtyConfirmed);
        }
        if (qtyConfirmed > this.qty) {
            throw new BusinessException("Cannot confirm more than requested quantity. Requested: " + this.qty + ", Confirmed: " + qtyConfirmed);
        }
        this.qtyActual = qtyConfirmed;
        if (this.qtyActual.equals(this.qty)) {
            this.status = STATUS_DONE;
        } else {
            this.status = STATUS_PENDING;
        }
    }

    // --- Getters and Setters ---

    public Long getPickTaskId() {
        return pickTaskId;
    }

    public void setPickTaskId(Long pickTaskId) {
        this.pickTaskId = pickTaskId;
    }

    public Long getOutboundLineId() {
        return outboundLineId;
    }

    public void setOutboundLineId(Long outboundLineId) {
        this.outboundLineId = outboundLineId;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Integer getQtyActual() {
        return qtyActual;
    }

    public void setQtyActual(Integer qtyActual) {
        this.qtyActual = qtyActual;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
