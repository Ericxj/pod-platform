package com.pod.oms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.oms.domain.OrderHold;
import com.pod.oms.domain.UnifiedOrderItem;
import com.pod.oms.mapper.OrderHoldMapper;
import com.pod.oms.mapper.UnifiedOrderItemMapper;
import com.pod.prd.service.PrdApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HoldApplicationService {

    @Autowired
    private OrderHoldMapper orderHoldMapper;
    @Autowired
    private UnifiedOrderItemMapper orderItemMapper;
    @Autowired
    private PrdApplicationService prdApplicationService;

    private Long tenantId() { return TenantContext.getTenantId(); }
    private Long factoryId() { return TenantContext.getFactoryId(); }

    public IPage<OrderHold> page(Page<OrderHold> page, String holdType, String status, String channel, String shopId) {
        LambdaQueryWrapper<OrderHold> q = new LambdaQueryWrapper<>();
        q.eq(OrderHold::getTenantId, tenantId()).eq(OrderHold::getFactoryId, factoryId()).eq(OrderHold::getDeleted, 0);
        if (holdType != null && !holdType.isBlank()) q.eq(OrderHold::getHoldType, holdType);
        if (status != null && !status.isBlank()) q.eq(OrderHold::getStatus, status);
        if (channel != null && !channel.isBlank()) q.eq(OrderHold::getChannel, channel);
        if (shopId != null && !shopId.isBlank()) q.eq(OrderHold::getShopId, shopId);
        q.orderByDesc(OrderHold::getCreatedAt);
        return orderHoldMapper.selectPage(page, q);
    }

    public OrderHold get(Long id) {
        OrderHold h = orderHoldMapper.selectById(id);
        if (h == null || !h.getTenantId().equals(tenantId()) || !h.getFactoryId().equals(factoryId()) || (h.getDeleted() != null && h.getDeleted() != 0))
            throw new BusinessException(404, "Hold not found");
        return h;
    }

    @Transactional(rollbackFor = Exception.class)
    public void resolve(Long holdId, Long skuId) {
        OrderHold hold = get(holdId);
        String skuCode = null;
        if (skuId != null) {
            var sku = prdApplicationService.getSku(skuId);
            if (sku == null) throw new BusinessException(404, "SKU not found: " + skuId);
            skuCode = sku.getSkuCode();
        }
        hold.resolve(skuId, skuCode, TenantContext.getUserId());
        orderHoldMapper.updateById(hold);

        if (hold.getUnifiedOrderItemId() != null && skuId != null) {
            UnifiedOrderItem item = orderItemMapper.selectById(hold.getUnifiedOrderItemId());
            if (item != null && item.getTenantId().equals(tenantId()) && item.getFactoryId().equals(factoryId())) {
                item.setSkuId(skuId);
                item.setSkuCode(skuCode);
                orderItemMapper.updateById(item);
            }
        }
    }
}
