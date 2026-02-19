package com.pod.oms.service;

import com.pod.common.core.context.TenantContext;
import com.pod.oms.domain.OrderHold;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.oms.mapper.OrderHoldMapper;
import com.pod.prd.domain.SkuMapping;
import com.pod.prd.service.PrdApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 解析平台 SKU -> 内部 skuId/skuCode；失败则写入 oms_order_hold（OPEN），不抛异常。
 */
@Service
public class SkuMappingResolver {

    @Autowired
    private PrdApplicationService prdApplicationService;
    @Autowired
    private OrderHoldMapper orderHoldMapper;

    public static final class ResolveResult {
        private final Long skuId;
        private final String skuCode;
        private final OrderHold hold;

        public ResolveResult(Long skuId, String skuCode) {
            this.skuId = skuId;
            this.skuCode = skuCode;
            this.hold = null;
        }

        public ResolveResult(OrderHold hold) {
            this.skuId = null;
            this.skuCode = null;
            this.hold = hold;
        }

        public boolean isResolved() { return skuId != null; }
        public Long getSkuId() { return skuId; }
        public String getSkuCode() { return skuCode; }
        public OrderHold getHold() { return hold; }
    }

    /**
     * 解析一条订单行的 externalSku；若映射存在则返回 skuId/skuCode，否则创建 OPEN 的 hold 并返回。
     * 幂等：同一 (tenant,factory,hold_type,channel,shop_id,external_order_id,external_sku) 不重复插入 hold。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResolveResult resolveOrHold(String channel, String shopId, String externalOrderId, String externalSku,
                                       Long unifiedOrderId, Long unifiedOrderItemId) {
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        String shopIdStr = shopId != null ? String.valueOf(shopId) : "";

        SkuMapping mapping = prdApplicationService.findSkuMappingByExternal(channel, shopIdStr, externalSku);
        if (mapping != null && mapping.getSkuId() != null) {
            return new ResolveResult(mapping.getSkuId(), mapping.getSkuCode());
        }

        OrderHold existing = selectHold(tenantId, factoryId, OrderHold.HOLD_TYPE_SKU_MAPPING, channel, shopIdStr, externalOrderId, externalSku);
        if (existing != null) {
            return new ResolveResult(existing);
        }

        OrderHold hold = new OrderHold();
        hold.setHoldType(OrderHold.HOLD_TYPE_SKU_MAPPING);
        hold.setStatus(OrderHold.STATUS_OPEN);
        hold.setReasonCode("SKU_MAPPING_NOT_FOUND");
        hold.setReasonMsg("No mapping for channel=" + channel + ", shopId=" + shopIdStr + ", externalSku=" + externalSku);
        hold.setChannel(channel);
        hold.setShopId(shopIdStr);
        hold.setExternalOrderId(externalOrderId);
        hold.setExternalSku(externalSku);
        hold.setUnifiedOrderId(unifiedOrderId);
        hold.setUnifiedOrderItemId(unifiedOrderItemId);
        hold.setTenantId(tenantId);
        hold.setFactoryId(factoryId);
        orderHoldMapper.insert(hold);
        return new ResolveResult(hold);
    }

    private OrderHold selectHold(Long tenantId, Long factoryId, String holdType, String channel, String shopId, String externalOrderId, String externalSku) {
        return orderHoldMapper.selectOne(new LambdaQueryWrapper<OrderHold>()
            .eq(OrderHold::getTenantId, tenantId).eq(OrderHold::getFactoryId, factoryId).eq(OrderHold::getDeleted, 0)
            .eq(OrderHold::getHoldType, holdType).eq(OrderHold::getChannel, channel).eq(OrderHold::getShopId, shopId)
            .eq(OrderHold::getExternalOrderId, externalOrderId).eq(OrderHold::getExternalSku, externalSku));
    }
}
