package com.pod.srm.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.math.BigDecimal;

@TableName("srm_purchase_order_line")
public class PurchaseOrderLine extends BaseEntity {

    @TableField("po_id")
    private Long poId;
    @TableField("line_no")
    private Integer lineNo;
    @TableField("sku_id")
    private Long skuId;
    @TableField("sku_code")
    private String skuCode;
    @TableField("sku_name")
    private String skuName;
    @TableField("qty_ordered")
    private BigDecimal qtyOrdered;
    @TableField("unit_price")
    private BigDecimal unitPrice;
    @TableField("qty_received")
    private BigDecimal qtyReceived;

    public void validate() {
        if (skuId == null) throw new BusinessException("sku_id required");
        if (qtyOrdered == null || qtyOrdered.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("qty_ordered must be > 0");
        if (unitPrice == null) throw new BusinessException("unit_price required");
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) throw new BusinessException("unit_price must be >= 0");
    }

    public BigDecimal getLineAmount() {
        if (qtyOrdered == null || unitPrice == null) return BigDecimal.ZERO;
        return qtyOrdered.multiply(unitPrice);
    }

    public Long getPoId() { return poId; }
    public void setPoId(Long poId) { this.poId = poId; }
    public Integer getLineNo() { return lineNo; }
    public void setLineNo(Integer lineNo) { this.lineNo = lineNo; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }
    public BigDecimal getQtyOrdered() { return qtyOrdered; }
    public void setQtyOrdered(BigDecimal qtyOrdered) { this.qtyOrdered = qtyOrdered; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getQtyReceived() { return qtyReceived; }
    public void setQtyReceived(BigDecimal qtyReceived) { this.qtyReceived = qtyReceived; }
}
