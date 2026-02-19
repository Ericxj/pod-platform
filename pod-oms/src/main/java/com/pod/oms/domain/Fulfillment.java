package com.pod.oms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 履约单聚合根，富领域模型。
 * 状态：CREATED -> RELEASED | CANCELLED；RELEASED -> CANCELLED。
 */
@TableName("oms_fulfillment")
public class Fulfillment extends BaseEntity {

    private String fulfillmentNo;
    private Long unifiedOrderId;
    private String channel;
    private Long shopId;
    private String externalOrderId;
    private String fulfillmentType;
    private String status;
    private Integer priority;
    private Long warehouseId;
    private LocalDateTime expectedShipAt;
    private String remark;

    @TableField(exist = false)
    private List<FulfillmentItem> items = new ArrayList<>();

    public static final String ERR_CONCURRENT = "Fulfillment concurrent modification";

    // ------------------------- 工厂方法 -------------------------

    /**
     * 从统一订单创建履约单（聚合根行为）。
     * 前置：订单状态必须为 VALIDATED；订单行非空。
     */
    public static Fulfillment createFromUnifiedOrder(UnifiedOrder order, List<UnifiedOrderItem> orderItems) {
        if (order == null) {
            throw new BusinessException("Order cannot be null");
        }
        if (order.getOrderStatus() == null || order.getOrderStatus().isEmpty()) {
            throw new BusinessException("Order status required");
        }
        if (!"VALIDATED".equals(order.getOrderStatus()) && !"NEW".equals(order.getOrderStatus())) {
            throw new BusinessException("Order must be NEW or VALIDATED before fulfillment. Current: " + order.getOrderStatus());
        }
        if (orderItems == null || orderItems.isEmpty()) {
            throw new BusinessException("Order must have at least one item to create fulfillment");
        }

        String fulfillmentNo = "FF-" + order.getUnifiedOrderNo();
        Fulfillment f = new Fulfillment();
        f.setFulfillmentNo(fulfillmentNo);
        f.setUnifiedOrderId(order.getId());
        f.setChannel(order.getChannel());
        f.setShopId(order.getShopId());
        f.setExternalOrderId(order.getExternalOrderId());
        f.setFulfillmentType("POD");
        f.setStatus(FulfillmentStatus.CREATED.name());
        f.setPriority(100);
        f.setItems(new ArrayList<>());

        int lineNo = 1;
        for (UnifiedOrderItem oi : orderItems) {
            if (oi.getSkuId() == null) {
                throw new BusinessException("Order item must have skuId");
            }
            int qty = oi.getQuantity() != null ? oi.getQuantity().intValue() : 0;
            if (qty <= 0) {
                throw new BusinessException("Order item quantity must be positive");
            }
            f.addItem(oi.getSkuId(), qty, oi.getId(), lineNo++);
        }
        return f;
    }

    /** 添加行项目（仅用于创建时，不暴露为公开行为） */
    private void addItem(Long skuId, int qty, Long unifiedOrderItemId, int lineNo) {
        FulfillmentItem item = new FulfillmentItem();
        item.setSkuId(skuId);
        item.setQty(qty);
        item.setUnifiedOrderItemId(unifiedOrderItemId);
        item.setLineNo(lineNo);
        item.setStatus("CREATED");
        this.items.add(item);
    }

    // ------------------------- 行为方法（状态校验 + 不变量） -------------------------

    /**
     * 确认履约（CREATED -> RELEASED），与 release() 同义，供不同语义的 API 使用。
     */
    public void confirm() {
        FulfillmentStatus current = FulfillmentStatus.from(this.status);
        current.requireAllowConfirm();
        ensureItemsNonEmpty();
        this.status = FulfillmentStatus.RELEASED.name();
    }

    /**
     * 预占成功：CREATED -> RESERVED 或 HOLD_INVENTORY -> RESERVED。
     */
    public void markReserved() {
        FulfillmentStatus.from(this.status).requireAllowReserve();
        this.status = FulfillmentStatus.RESERVED.name();
    }

    /**
     * 预占失败（库存不足）：CREATED -> HOLD_INVENTORY。
     */
    public void markHoldInventory() {
        FulfillmentStatus current = FulfillmentStatus.from(this.status);
        if (!FulfillmentStatus.CREATED.equals(current) && !FulfillmentStatus.HOLD_INVENTORY.equals(current)) {
            throw new BusinessException("Cannot set HOLD_INVENTORY from " + this.status);
        }
        this.status = FulfillmentStatus.HOLD_INVENTORY.name();
    }

    /**
     * 释放到仓库（CREATED | RESERVED -> RELEASED）：状态前置校验。
     */
    public void release() {
        FulfillmentStatus current = FulfillmentStatus.from(this.status);
        current.requireAllowRelease();
        ensureItemsNonEmpty();
        this.status = FulfillmentStatus.RELEASED.name();
    }

    /**
     * P1.3：全部行生产图就绪后，RESERVED -> ART_READY。
     */
    public void markArtReady() {
        FulfillmentStatus.from(this.status).requireAllowArtReady();
        this.status = FulfillmentStatus.ART_READY.name();
    }

    /**
     * 取消履约（CREATED | RESERVED | ART_READY | HOLD_INVENTORY | RELEASED -> CANCELLED）。
     */
    public void cancel() {
        FulfillmentStatus current = FulfillmentStatus.from(this.status);
        current.requireAllowCancel();
        if (FulfillmentStatus.CANCELLED.equals(current)) return;
        this.status = FulfillmentStatus.CANCELLED.name();
    }

    private void ensureItemsNonEmpty() {
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Fulfillment must have at least one item");
        }
    }

    // ------------------------- Getters / Setters -------------------------

    public String getFulfillmentNo() { return fulfillmentNo; }
    public void setFulfillmentNo(String fulfillmentNo) { this.fulfillmentNo = fulfillmentNo; }
    public Long getUnifiedOrderId() { return unifiedOrderId; }
    public void setUnifiedOrderId(Long unifiedOrderId) { this.unifiedOrderId = unifiedOrderId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
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
    public List<FulfillmentItem> getItems() { return items == null ? Collections.emptyList() : items; }
    public void setItems(List<FulfillmentItem> items) { this.items = items != null ? items : new ArrayList<>(); }
}
