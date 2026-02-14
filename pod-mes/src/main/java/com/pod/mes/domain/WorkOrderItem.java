package com.pod.mes.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("mes_work_order_item")
public class WorkOrderItem extends BaseEntity {

    private Long workOrderId;
    private Integer lineNo;
    private Long skuId;
    private Integer qty;
    private String surfaceCode;
    private Long productionFileId;
    private String status;

    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public Integer getLineNo() { return lineNo; }
    public void setLineNo(Integer lineNo) { this.lineNo = lineNo; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
    public String getSurfaceCode() { return surfaceCode; }
    public void setSurfaceCode(String surfaceCode) { this.surfaceCode = surfaceCode; }
    public Long getProductionFileId() { return productionFileId; }
    public void setProductionFileId(Long productionFileId) { this.productionFileId = productionFileId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
