package com.pod.oms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@TableName("oms_fulfillment")
public class Fulfillment extends BaseEntity {

    private String fulfillmentNo; // 对应 fulfillment_no
    private Long unifiedOrderId; // 对应 unified_order_id
    private String fulfillmentType; // 对应 fulfillment_type
    private String status; // 对应 status
    private Integer priority; // 对应 priority
    private Long warehouseId; // 对应 warehouse_id
    private LocalDateTime expectedShipAt; // 对应 expected_ship_at
    private String remark; // 对应 remark

    @TableField(exist = false)
    private List<FulfillmentItem> items = new ArrayList<>();

    // Factory method for creation
    public static Fulfillment create(String fulfillmentNo, Long unifiedOrderId) {
        Fulfillment f = new Fulfillment();
        f.setFulfillmentNo(fulfillmentNo);
        f.setUnifiedOrderId(unifiedOrderId);
        f.setFulfillmentType("POD"); // Default type
        f.setStatus(FulfillmentStatus.CREATED.name());
        f.setPriority(100); // Default priority
        return f;
    }

    public void addItem(Long skuId, int qty, Long unifiedOrderItemId, int lineNo) {
        FulfillmentItem item = new FulfillmentItem();
        item.setSkuId(skuId);
        item.setQty(qty);
        item.setUnifiedOrderItemId(unifiedOrderItemId);
        item.setLineNo(lineNo);
        item.setStatus("CREATED");
        this.items.add(item);
    }

    // Behaviors
    public void release() {
        if (!FulfillmentStatus.CREATED.name().equals(this.status)) {
            throw new BusinessException("Only CREATED fulfillment can be released. Current: " + this.status);
        }
        this.status = FulfillmentStatus.RELEASED.name();
    }

    public void cancel() {
         if (FulfillmentStatus.CANCELLED.name().equals(this.status)) {
             return;
         }
         this.status = FulfillmentStatus.CANCELLED.name();
    }

    // Getters Setters
    public String getFulfillmentNo() { return fulfillmentNo; }
    public void setFulfillmentNo(String fulfillmentNo) { this.fulfillmentNo = fulfillmentNo; }
    public Long getUnifiedOrderId() { return unifiedOrderId; }
    public void setUnifiedOrderId(Long unifiedOrderId) { this.unifiedOrderId = unifiedOrderId; }
    public String getFulfillmentType() { return fulfillmentType; }
    public void setFulfillmentType(String fulfillmentType) { this.fulfillmentType = fulfillmentType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public LocalDateTime getExpectedShipAt() { return expectedShipAt; }
    public void setExpectedShipAt(LocalDateTime expectedShipAt) { this.expectedShipAt = expectedShipAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<FulfillmentItem> getItems() { return items; }
    public void setItems(List<FulfillmentItem> items) { this.items = items; }
}
