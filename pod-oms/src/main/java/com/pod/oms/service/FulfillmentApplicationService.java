package com.pod.oms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
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
import java.util.Objects;

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

    private Long tenantId() { return TenantContext.getTenantId(); }
    private Long factoryId() { return TenantContext.getFactoryId(); }

    /** 幂等：按 uk_src_order(tenant, factory, channel, shop_id, external_order_id) 查，有则返回；否则创建。 */
    @Transactional(rollbackFor = Exception.class)
    public Long createFromUnifiedOrder(Long unifiedOrderId) {
        String requestId = RequestIdContext.get();
        if (requestId == null || requestId.isBlank()) requestId = "ful-create-" + unifiedOrderId;
        idempotentService.lock(requestId, "CreateFulfillment:" + unifiedOrderId);

        UnifiedOrder order = orderMapper.selectById(unifiedOrderId);
        if (order == null || !Objects.equals(order.getTenantId(), tenantId()) || !Objects.equals(order.getFactoryId(), factoryId()) || (order.getDeleted() != null && order.getDeleted() != 0)) {
            throw new BusinessException("Order not found: " + unifiedOrderId);
        }
        String channel = order.getChannel() != null ? order.getChannel() : order.getPlatformCode();
        String externalOrderId = order.getExternalOrderId() != null ? order.getExternalOrderId() : order.getPlatformOrderId();
        Long shopId = order.getShopId();
        if (channel != null && externalOrderId != null) {
            LambdaQueryWrapper<Fulfillment> uk = new LambdaQueryWrapper<>();
            uk.eq(Fulfillment::getTenantId, tenantId()).eq(Fulfillment::getFactoryId, factoryId()).eq(Fulfillment::getDeleted, 0)
              .eq(Fulfillment::getChannel, channel).eq(Fulfillment::getExternalOrderId, externalOrderId);
            if (shopId != null) uk.eq(Fulfillment::getShopId, shopId);
            Fulfillment existing = fulfillmentMapper.selectOne(uk);
            if (existing != null) return existing.getId();
        }

        List<UnifiedOrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<UnifiedOrderItem>().eq(UnifiedOrderItem::getUnifiedOrderId, unifiedOrderId).eq(UnifiedOrderItem::getDeleted, 0));
        order.setItems(orderItems);

        Fulfillment fulfillment = Fulfillment.createFromUnifiedOrder(order, orderItems);
        fulfillment.setTenantId(tenantId());
        fulfillment.setFactoryId(factoryId());
        fulfillmentMapper.insert(fulfillment);
        for (FulfillmentItem item : fulfillment.getItems()) {
            item.setFulfillmentId(fulfillment.getId());
            item.setTenantId(tenantId());
            item.setFactoryId(factoryId());
            item.setReservedQty(0);
            item.setReserveStatus(null);
            fulfillmentItemMapper.insert(item);
        }
        return fulfillment.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createFulfillment(Long orderId) {
        return createFromUnifiedOrder(orderId);
    }

    /** 预占库存：CREATED 或 HOLD_INVENTORY -> 调 INV reserve；成功 RESERVED，失败 HOLD_INVENTORY。 */
    @Transactional(rollbackFor = Exception.class)
    public void reserveInventory(Long fulfillmentId) {
        Fulfillment fulfillment = loadAggregate(fulfillmentId);
        fulfillment.getStatus(); // trigger load
        Long warehouseId = fulfillment.getWarehouseId() != null ? fulfillment.getWarehouseId() : 300001L;
        String fulfillmentNo = fulfillment.getFulfillmentNo();
        String oldStatus = fulfillment.getStatus();
        Integer version = fulfillment.getVersion();
        try {
            for (FulfillmentItem item : fulfillment.getItems()) {
                int qty = item.getQty() != null ? item.getQty() : 0;
                if (qty <= 0) continue;
                inventoryService.reserve("FULFILLMENT", fulfillmentNo, warehouseId, item.getSkuId(), qty);
                item.setReservedQty(qty);
                item.setReserveStatus("RESERVED");
                fulfillmentItemMapper.updateById(item);
            }
            fulfillment.markReserved();
            fulfillmentMapper.updateStatusWithLock(fulfillmentId, fulfillment.getStatus(), oldStatus, version, TenantContext.getUserId());
        } catch (BusinessException e) {
            try { inventoryService.releaseByBiz("FULFILLMENT", fulfillmentNo); } catch (Exception ignored) { }
            fulfillment.markHoldInventory();
            fulfillmentMapper.updateStatusWithLock(fulfillmentId, fulfillment.getStatus(), oldStatus, version, TenantContext.getUserId());
            for (FulfillmentItem item : fulfillment.getItems()) {
                item.setReservedQty(0);
                item.setReserveStatus("SHORTAGE");
                fulfillmentItemMapper.updateById(item);
            }
            throw e;
        }
    }

    /** 释放预占：调 INV releaseByBiz。 */
    @Transactional(rollbackFor = Exception.class)
    public void releaseInventory(Long fulfillmentId) {
        Fulfillment fulfillment = loadAggregate(fulfillmentId);
        if (!"RESERVED".equals(fulfillment.getStatus()) && !"RELEASED".equals(fulfillment.getStatus())) {
            throw new BusinessException("Fulfillment must be RESERVED to release inventory. Current: " + fulfillment.getStatus());
        }
        inventoryService.releaseByBiz("FULFILLMENT", fulfillment.getFulfillmentNo());
        if ("RESERVED".equals(fulfillment.getStatus())) {
            for (FulfillmentItem item : fulfillment.getItems()) {
                item.setReservedQty(0);
                item.setReserveStatus("RELEASED");
                fulfillmentItemMapper.updateById(item);
            }
            fulfillment.release();
            fulfillmentMapper.updateStatusWithLock(fulfillmentId, fulfillment.getStatus(), "RESERVED", fulfillment.getVersion(), TenantContext.getUserId());
        }
    }

    /** 重试预占（用于 HOLD_INVENTORY 补库存后）。 */
    @Transactional(rollbackFor = Exception.class)
    public void retryReserve(Long fulfillmentId) {
        reserveInventory(fulfillmentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void releaseFulfillment(Long id) {
        Fulfillment fulfillment = loadAggregate(id);
        if ("CREATED".equals(fulfillment.getStatus()) || "HOLD_INVENTORY".equals(fulfillment.getStatus())) {
            reserveInventory(id);
            return;
        }
        if ("RESERVED".equals(fulfillment.getStatus())) {
            releaseInventory(id);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelFulfillment(Long id) {
        Fulfillment fulfillment = loadAggregate(id);
        if ("RESERVED".equals(fulfillment.getStatus())) {
            try { inventoryService.releaseByBiz("FULFILLMENT", fulfillment.getFulfillmentNo()); } catch (Exception ignored) { }
            for (FulfillmentItem item : fulfillment.getItems()) {
                item.setReservedQty(0);
                item.setReserveStatus("RELEASED");
                fulfillmentItemMapper.updateById(item);
            }
        }
        String oldStatus = fulfillment.getStatus();
        Integer version = fulfillment.getVersion();
        fulfillment.cancel();
        int rows = fulfillmentMapper.updateStatusWithLock(id, fulfillment.getStatus(), oldStatus, version, TenantContext.getUserId());
        if (rows != 1) throw new BusinessException(Fulfillment.ERR_CONCURRENT);
    }

    public Fulfillment getFulfillment(Long id) {
        return loadAggregate(id);
    }

    public IPage<Fulfillment> page(Page<Fulfillment> page, String status, String fulfillmentNo) {
        LambdaQueryWrapper<Fulfillment> q = new LambdaQueryWrapper<>();
        q.eq(Fulfillment::getTenantId, tenantId()).eq(Fulfillment::getFactoryId, factoryId()).eq(Fulfillment::getDeleted, 0);
        if (status != null && !status.isBlank()) q.eq(Fulfillment::getStatus, status);
        if (fulfillmentNo != null && !fulfillmentNo.isBlank()) q.eq(Fulfillment::getFulfillmentNo, fulfillmentNo);
        q.orderByDesc(Fulfillment::getCreatedAt);
        return fulfillmentMapper.selectPage(page, q);
    }

    private Fulfillment loadAggregate(Long id) {
        Fulfillment fulfillment = fulfillmentMapper.selectById(id);
        if (fulfillment == null || !Objects.equals(fulfillment.getTenantId(), tenantId()) || !Objects.equals(fulfillment.getFactoryId(), factoryId()) || (fulfillment.getDeleted() != null && fulfillment.getDeleted() != 0)) {
            throw new BusinessException("Fulfillment not found: " + id);
        }
        List<FulfillmentItem> items = fulfillmentItemMapper.selectList(
                new LambdaQueryWrapper<FulfillmentItem>().eq(FulfillmentItem::getFulfillmentId, id).eq(FulfillmentItem::getDeleted, 0));
        fulfillment.setItems(items);
        return fulfillment;
    }
}
