package com.pod.srm.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@TableName("srm_purchase_order")
public class PurchaseOrder extends BaseEntity {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String STATUS_CANCELED = "CANCELED";

    @TableField("po_no")
    private String poNo;
    @TableField("supplier_id")
    private Long supplierId;
    private String currency;
    private String status;
    @TableField("total_qty")
    private BigDecimal totalQty;
    @TableField("total_amount")
    private BigDecimal totalAmount;
    @TableField("expected_arrive_date")
    private LocalDate expectedArriveDate;

    @TableField(exist = false)
    private List<PurchaseOrderLine> lines = new ArrayList<>();

    /**
     * 添加行（仅 DRAFT 可编辑行）。
     */
    public void addLine(PurchaseOrderLine line) {
        if (!STATUS_DRAFT.equals(this.status)) {
            throw new BusinessException("Only DRAFT PO can add lines, current: " + this.status);
        }
        line.validate();
        if (line.getQtyReceived() == null) line.setQtyReceived(BigDecimal.ZERO);
        lines.add(line);
    }

    /**
     * 提交：必须有行、qty>0、supplier 有效由应用层校验。
     */
    public void submit(boolean supplierValid) {
        if (!STATUS_DRAFT.equals(this.status)) {
            throw new BusinessException("Only DRAFT can submit, current: " + this.status);
        }
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException("PO must have at least one line");
        }
        for (PurchaseOrderLine line : lines) {
            if (line.getQtyOrdered() == null || line.getQtyOrdered().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("qty_ordered must be > 0 for all lines");
            }
        }
        if (!supplierValid) {
            throw new BusinessException("Supplier must exist and be ENABLED");
        }
        this.status = STATUS_SUBMITTED;
    }

    /**
     * 审批：仅 SUBMITTED 可审批。
     */
    public void approve() {
        if (!STATUS_SUBMITTED.equals(this.status)) {
            throw new BusinessException("Only SUBMITTED can approve, current: " + this.status);
        }
        this.status = STATUS_APPROVED;
    }

    /**
     * 取消：仅 DRAFT/SUBMITTED 可取消。
     */
    public void cancel() {
        if (!STATUS_DRAFT.equals(this.status) && !STATUS_SUBMITTED.equals(this.status)) {
            throw new BusinessException("Only DRAFT or SUBMITTED can cancel, current: " + this.status);
        }
        this.status = STATUS_CANCELED;
    }

    /**
     * 关闭：仅 APPROVED 可关闭。
     */
    public void close() {
        if (!STATUS_APPROVED.equals(this.status)) {
            throw new BusinessException("Only APPROVED can close, current: " + this.status);
        }
        this.status = STATUS_CLOSED;
    }

    public void recalcTotals() {
        if (lines == null || lines.isEmpty()) {
            this.totalQty = BigDecimal.ZERO;
            this.totalAmount = BigDecimal.ZERO;
            return;
        }
        BigDecimal qty = BigDecimal.ZERO;
        BigDecimal amt = BigDecimal.ZERO;
        for (PurchaseOrderLine line : lines) {
            qty = qty.add(line.getQtyOrdered() != null ? line.getQtyOrdered() : BigDecimal.ZERO);
            amt = amt.add(line.getLineAmount());
        }
        this.totalQty = qty;
        this.totalAmount = amt;
    }

    public String getPoNo() { return poNo; }
    public void setPoNo(String poNo) { this.poNo = poNo; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public LocalDate getExpectedArriveDate() { return expectedArriveDate; }
    public void setExpectedArriveDate(LocalDate expectedArriveDate) { this.expectedArriveDate = expectedArriveDate; }
    public List<PurchaseOrderLine> getLines() { return lines; }
    public void setLines(List<PurchaseOrderLine> lines) { this.lines = lines != null ? lines : new ArrayList<>(); }
}
