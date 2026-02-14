package com.pod.oms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.common.core.exception.BusinessException;
import com.pod.infra.context.RequestIdContext;
import com.pod.infra.idempotent.service.IdempotentService;
import com.pod.infra.outbox.service.OutboxService;
import com.pod.inv.service.InventoryApplicationService;
import com.pod.oms.domain.Fulfillment;
import com.pod.oms.domain.FulfillmentItem;
import com.pod.oms.domain.UnifiedOrder;
import com.pod.oms.domain.UnifiedOrderItem;
import com.pod.oms.mapper.FulfillmentItemMapper;
import com.pod.oms.mapper.FulfillmentMapper;
import com.pod.oms.mapper.UnifiedOrderItemMapper;
import com.pod.oms.mapper.UnifiedOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 履约单应用服务：仅编排 load -> domain method -> persist -> (side effects / outbox)。
 * 状态变更通过 DB 条件更新（id + status + version）做并发防护。
 */
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
    @Autowired
    private OutboxService outboxService;

    @Transactional(rollbackFor = Exception.class)
    public Long createFulfillment(Long orderId) {
        String requestId = RequestIdContext.getRequired();
        idempotentService.lock(requestId, "CreateFulfillment:" + orderId);

        UnifiedOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("Order not found: " + orderId);
        }
        List<UnifiedOrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<UnifiedOrderItem>().eq(UnifiedOrderItem::getUnifiedOrderId, orderId));
        if (orderItems != null) {
            order.setItems(orderItems);
        }

        Long count = fulfillmentMapper.selectCount(
                new LambdaQueryWrapper<Fulfillment>().eq(Fulfillment::getUnifiedOrderId, orderId));
        if (count != null && count > 0) {
            Fulfillment existing = fulfillmentMapper.selectOne(
                    new LambdaQueryWrapper<Fulfillment>().eq(Fulfillment::getUnifiedOrderId, orderId));
            return existing.getId();
        }

        Fulfillment fulfillment = Fulfillment.createFromUnifiedOrder(order, orderItems);
        fulfillmentMapper.insert(fulfillment);
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

        Fulfillment fulfillment = loadAggregate(id);
        String oldStatus = fulfillment.getStatus();
        Integer version = fulfillment.getVersion();
        fulfillment.release();

        int rows = fulfillmentMapper.updateStatusWithLock(
                id, fulfillment.getStatus(), oldStatus, version, fulfillment.getUpdatedBy());
        if (rows != 1) {
            Fulfillment current = fulfillmentMapper.selectById(id);
            if (current != null && "RELEASED".equals(current.getStatus())) {
                return;
            }
            throw new BusinessException(Fulfillment.ERR_CONCURRENT);
        }

        Long warehouseId = fulfillment.getWarehouseId() != null ? fulfillment.getWarehouseId() : 300001L;
        for (FulfillmentItem item : fulfillment.getItems()) {
            inventoryService.reserve(
                    "FULFILLMENT",
                    fulfillment.getFulfillmentNo(),
                    warehouseId,
                    item.getSkuId(),
                    item.getQty());
        }
        outboxService.publish("FulfillmentReleased", "FULFILLMENT", id, fulfillment.getFulfillmentNo(), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelFulfillment(Long id) {
        Fulfillment fulfillment = loadAggregate(id);
        String oldStatus = fulfillment.getStatus();
        Integer version = fulfillment.getVersion();
        fulfillment.cancel();

        int rows = fulfillmentMapper.updateStatusWithLock(
                id, fulfillment.getStatus(), oldStatus, version, fulfillment.getUpdatedBy());
        if (rows != 1) {
            throw new BusinessException(Fulfillment.ERR_CONCURRENT);
        }
    }

    public Fulfillment getFulfillment(Long id) {
        return loadAggregate(id);
    }

    private Fulfillment loadAggregate(Long id) {
        Fulfillment fulfillment = fulfillmentMapper.selectById(id);
        if (fulfillment == null) {
            throw new BusinessException("Fulfillment not found: " + id);
        }
        List<FulfillmentItem> items = fulfillmentItemMapper.selectList(
                new LambdaQueryWrapper<FulfillmentItem>().eq(FulfillmentItem::getFulfillmentId, id));
        fulfillment.setItems(items);
        return fulfillment;
    }
}
