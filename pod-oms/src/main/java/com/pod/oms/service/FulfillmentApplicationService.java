package com.pod.oms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.exception.BusinessException;
import com.pod.infra.context.RequestIdContext;
import com.pod.infra.idempotent.service.IdempotentService;
import com.pod.inv.service.InventoryApplicationService;
import com.pod.oms.domain.Fulfillment;
import com.pod.oms.domain.FulfillmentItem;
import com.pod.oms.domain.FulfillmentStatus;
import com.pod.oms.domain.UnifiedOrder;
import com.pod.oms.domain.UnifiedOrderItem;
import com.pod.oms.dto.FulfillmentCreateCmd;
import com.pod.oms.mapper.FulfillmentItemMapper;
import com.pod.oms.mapper.FulfillmentMapper;
import com.pod.oms.mapper.UnifiedOrderItemMapper;
import com.pod.oms.mapper.UnifiedOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FulfillmentApplicationService {

    @Autowired
    private FulfillmentMapper fulfillmentMapper;

    @Autowired
    private FulfillmentItemMapper fulfillmentItemMapper;

    @Autowired
    private UnifiedOrderMapper orderMapper;

    @Autowired
    private UnifiedOrderItemMapper orderItemMapper;

    @Autowired
    private InventoryApplicationService inventoryService;

    @Autowired
    private IdempotentService idempotentService;

    @Transactional(rollbackFor = Exception.class)
    public Long createFulfillment(Long orderId) {
        String requestId = RequestIdContext.getRequired();
        idempotentService.lock(requestId, "CreateFulfillment:" + orderId);

        // 2. Check Order
        UnifiedOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("Order not found: " + orderId);
        }
        if (!"VALIDATED".equals(order.getOrderStatus())) {
            throw new BusinessException("Order must be VALIDATED before fulfillment");
        }

        // 3. Check Duplicate Fulfillment (One per Order per Factory)
        // Since we have factory_id isolation via Mybatis Plugin, we just check by unified_order_id
        Long count = fulfillmentMapper.selectCount(new LambdaQueryWrapper<Fulfillment>()
                .eq(Fulfillment::getUnifiedOrderId, orderId));
        if (count > 0) {
            // Already exists. Return existing ID or throw?
            // User requirement: "If exists return it or idempotent"
            // Let's find it
            Fulfillment existing = fulfillmentMapper.selectOne(new LambdaQueryWrapper<Fulfillment>()
                    .eq(Fulfillment::getUnifiedOrderId, orderId));
            return existing.getId();
        }

        // 4. Create Aggregate Root
        String fulfillmentNo = "FF-" + order.getUnifiedOrderNo();
        Fulfillment fulfillment = Fulfillment.create(fulfillmentNo, orderId);
        fulfillmentMapper.insert(fulfillment);

        // 5. Create Items
        List<UnifiedOrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<UnifiedOrderItem>()
                .eq(UnifiedOrderItem::getUnifiedOrderId, orderId));
        
        int lineNo = 1;
        for (UnifiedOrderItem oi : orderItems) {
            fulfillment.addItem(oi.getSkuId(), oi.getQuantity(), oi.getId(), lineNo++);
        }

        // 6. Save Items
        for (FulfillmentItem item : fulfillment.getItems()) {
            item.setFulfillmentId(fulfillment.getId());
            fulfillmentItemMapper.insert(item);
        }

        return fulfillment.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void releaseFulfillment(Long id) {
        String requestId = RequestIdContext.getRequired();
        idempotentService.lock(requestId, "ReleaseFulfillment:" + id);

        // 2. Load Aggregate
        Fulfillment fulfillment = fulfillmentMapper.selectById(id);
        if (fulfillment == null) {
            throw new BusinessException("Fulfillment not found: " + id);
        }
        
        List<FulfillmentItem> items = fulfillmentItemMapper.selectList(new LambdaQueryWrapper<FulfillmentItem>()
                .eq(FulfillmentItem::getFulfillmentId, id));
        fulfillment.setItems(items);

        // 3. Domain Logic: Release
        String oldStatus = fulfillment.getStatus();
        Integer oldVersion = fulfillment.getVersion();
        fulfillment.release(); // Status -> RELEASED

        // 4. Optimistic Lock Update (Status + Version)
        // UPDATE ... WHERE id=? AND status='CREATED' AND version=?
        LambdaUpdateWrapper<Fulfillment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Fulfillment::getId, id)
                     .eq(Fulfillment::getStatus, oldStatus)
                     .eq(Fulfillment::getVersion, oldVersion)
                     .set(Fulfillment::getStatus, fulfillment.getStatus())
                     .set(Fulfillment::getVersion, oldVersion + 1); // Manual version increment if not using entity update
        
        // Alternatively, use updateById if @Version is reliable, but wrapper is safer for status check
        int rows = fulfillmentMapper.update(null, updateWrapper);
        if (rows == 0) {
            // Check if already released?
            Fulfillment current = fulfillmentMapper.selectById(id);
            if ("RELEASED".equals(current.getStatus()) || "RESERVED".equals(current.getStatus())) {
                return; // Idempotent success
            }
            throw new BusinessException("Fulfillment release failed due to concurrency or invalid status");
        }

        // 5. Trigger Inventory Reservation (Sync Transaction)
        // Loop items and reserve
        // Assuming Warehouse ID is on Fulfillment (default 300001L for demo if null)
        Long warehouseId = fulfillment.getWarehouseId();
        if (warehouseId == null) warehouseId = 300001L; // Default Main Warehouse

        for (FulfillmentItem item : items) {
            inventoryService.reserve(
                    "FULFILLMENT", 
                    fulfillment.getFulfillmentNo(), 
                    warehouseId, 
                    item.getSkuId(), 
                    item.getQty()
            );
        }

        // 6. Status remains RELEASED (Inventory Reserved)
        // No further status update needed as RELEASED implies ready for warehouse processing.
    }
    
    public Fulfillment getFulfillment(Long id) {
        Fulfillment f = fulfillmentMapper.selectById(id);
        if (f != null) {
            f.setItems(fulfillmentItemMapper.selectList(new LambdaQueryWrapper<FulfillmentItem>()
                    .eq(FulfillmentItem::getFulfillmentId, id)));
        }
        return f;
    }
}
