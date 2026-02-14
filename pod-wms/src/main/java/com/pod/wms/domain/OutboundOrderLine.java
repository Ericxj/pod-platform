package com.pod.wms.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("wms_outbound_order_line")
public class OutboundOrderLine extends BaseEntity {
    private Long outboundId;
    private Integer lineNo;
    private Long skuId;
    private Integer qty;
    private Integer qtyPicked;
    private Integer qtyShipped;

    public Long getOutboundId() {
        return outboundId;
    }

    public void setOutboundId(Long outboundId) {
        this.outboundId = outboundId;
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

    public Integer getQtyPicked() {
        return qtyPicked;
    }

    public void setQtyPicked(Integer qtyPicked) {
        this.qtyPicked = qtyPicked;
    }

    public Integer getQtyShipped() {
        return qtyShipped;
    }

    public void setQtyShipped(Integer qtyShipped) {
        this.qtyShipped = qtyShipped;
    }
}
