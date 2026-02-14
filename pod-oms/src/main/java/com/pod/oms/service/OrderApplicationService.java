package com.pod.oms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.exception.BusinessException;
import com.pod.oms.domain.UnifiedOrder;
import com.pod.oms.domain.UnifiedOrderItem;
import com.pod.oms.mapper.UnifiedOrderItemMapper;
import com.pod.oms.mapper.UnifiedOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderApplicationService {

    @Autowired
    private UnifiedOrderMapper orderMapper;

    @Autowired
    private UnifiedOrderItemMapper orderItemMapper;

    public IPage<UnifiedOrder> pageOrders(Page<UnifiedOrder> page, String orderNo) {
        LambdaQueryWrapper<UnifiedOrder> wrapper = new LambdaQueryWrapper<>();
        if (orderNo != null) wrapper.eq(UnifiedOrder::getUnifiedOrderNo, orderNo);
        wrapper.orderByDesc(UnifiedOrder::getOrderCreatedAt);
        return orderMapper.selectPage(page, wrapper);
    }

    public UnifiedOrder getOrder(Long id) {
        UnifiedOrder order = orderMapper.selectById(id);
        if (order != null) {
            LambdaQueryWrapper<UnifiedOrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(UnifiedOrderItem::getUnifiedOrderId, id);
            order.setItems(orderItemMapper.selectList(itemWrapper));
        }
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public void validateOrder(Long id) {
        UnifiedOrder order = getOrder(id);
        if (order == null) {
            throw new BusinessException("Order not found: " + id);
        }

        // 1. Domain Logic
        order.validate();

        // 2. Optimistic Update
        // Note: For simple status update, we can rely on @Version if present, or manual check.
        // The requirement says: "UPDATE ... WHERE status=? AND version=?".
        // BaseMapper.updateById uses optimistic locking if @Version is present on the entity field.
        // BaseEntity has `version` field.
        // However, user specifically asked for "UPDATE ... WHERE status=?". 
        // This is to prevent state jumping (e.g. from CANCELLED back to VALIDATED).
        // My `validate` method checks status.
        // So `updateById` with `@Version` is safe enough for concurrency.
        // But to be strictly following "UPDATE ... WHERE status=?", we can use UpdateWrapper.
        
        LambdaQueryWrapper<UnifiedOrder> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(UnifiedOrder::getId, id)
                     .eq(UnifiedOrder::getOrderStatus, "NEW") // Previous Status
                     .eq(UnifiedOrder::getVersion, order.getVersion());
        
        order.setVersion(order.getVersion() + 1);
        
        int rows = orderMapper.update(order, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("Order validation failed due to concurrency or invalid status");
        }
    }
}
