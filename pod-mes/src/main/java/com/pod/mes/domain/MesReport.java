package com.pod.mes.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("mes_report")
public class MesReport extends BaseEntity {

    @TableField("work_order_id")
    private Long workOrderId;
    @TableField("work_order_line_id")
    private Long workOrderLineId;
    @TableField("op_code")
    private String opCode;
    @TableField("work_order_op_id")
    private Long workOrderOpId;
    @TableField("good_qty")
    private Integer goodQty;
    @TableField("scrap_qty")
    private Integer scrapQty;
    @TableField("workstation_id")
    private Long workstationId;
    private String remark;

    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public Long getWorkOrderLineId() { return workOrderLineId; }
    public void setWorkOrderLineId(Long workOrderLineId) { this.workOrderLineId = workOrderLineId; }
    public String getOpCode() { return opCode; }
    public void setOpCode(String opCode) { this.opCode = opCode; }
    public Long getWorkOrderOpId() { return workOrderOpId; }
    public void setWorkOrderOpId(Long workOrderOpId) { this.workOrderOpId = workOrderOpId; }
    public Integer getGoodQty() { return goodQty; }
    public void setGoodQty(Integer goodQty) { this.goodQty = goodQty; }
    public Integer getScrapQty() { return scrapQty; }
    public void setScrapQty(Integer scrapQty) { this.scrapQty = scrapQty; }
    public Long getWorkstationId() { return workstationId; }
    public void setWorkstationId(Long workstationId) { this.workstationId = workstationId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
