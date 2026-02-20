package com.pod.oms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.infra.idempotent.service.IdempotentService;
import com.pod.oms.domain.OrderHold;
import com.pod.oms.domain.UnifiedOrder;
import com.pod.oms.domain.UnifiedOrderItem;
import com.pod.oms.dto.ChannelOrderDto;
import com.pod.oms.dto.ChannelOrderItemDto;
import com.pod.oms.mapper.UnifiedOrderItemMapper;
import com.pod.oms.mapper.UnifiedOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UnifiedOrderApplicationService {

    @Autowired
    private UnifiedOrderMapper orderMapper;
    @Autowired
    private UnifiedOrderItemMapper orderItemMapper;
    @Autowired
    private SkuMappingResolver skuMappingResolver;
    @Autowired
    private IdempotentService idempotentService;

    private Long tenantId() { return TenantContext.getTenantId(); }
    private Long factoryId() { return TenantContext.getFactoryId(); }

    public IPage<UnifiedOrder> page(Page<UnifiedOrder> page, String channel, String shopId, String externalOrderId, String orderStatus) {
        LambdaQueryWrapper<UnifiedOrder> q = new LambdaQueryWrapper<>();
        q.eq(UnifiedOrder::getTenantId, tenantId()).eq(UnifiedOrder::getFactoryId, factoryId()).eq(UnifiedOrder::getDeleted, 0);
        if (channel != null && !channel.isBlank()) q.eq(UnifiedOrder::getChannel, channel);
        if (shopId != null && !shopId.isBlank()) {
            try { q.eq(UnifiedOrder::getShopId, Long.parseLong(shopId)); } catch (NumberFormatException ignored) { }
        }
        if (externalOrderId != null && !externalOrderId.isBlank()) q.eq(UnifiedOrder::getExternalOrderId, externalOrderId);
        if (orderStatus != null && !orderStatus.isBlank()) q.eq(UnifiedOrder::getOrderStatus, orderStatus);
        q.orderByDesc(UnifiedOrder::getOrderCreatedAt);
        return orderMapper.selectPage(page, q);
    }

    public UnifiedOrder get(Long id) {
        UnifiedOrder o = orderMapper.selectById(id);
        if (o == null || !o.getTenantId().equals(tenantId()) || !o.getFactoryId().equals(factoryId()) || (o.getDeleted() != null && o.getDeleted() != 0))
            throw new BusinessException(404, "Order not found");
        LambdaQueryWrapper<UnifiedOrderItem> iq = new LambdaQueryWrapper<>();
        iq.eq(UnifiedOrderItem::getUnifiedOrderId, id).eq(UnifiedOrderItem::getDeleted, 0).orderByAsc(UnifiedOrderItem::getLineNo);
        o.setItems(orderItemMapper.selectList(iq));
        return o;
    }

    /**
     * 幂等：同一 requestId 或同一 (tenant,factory,channel,shop_id,external_order_id) 不重复创建订单。
     */
    @Transactional(rollbackFor = Exception.class)
    public UnifiedOrder upsertFromChannel(String requestId, String channel, String shopId, ChannelOrderDto dto) {
        if (dto == null || dto.getExternalOrderId() == null) throw new BusinessException(400, "externalOrderId required");
        final String shopIdStr = shopId != null ? shopId : "";
        Long shopIdLongVal = null;
        try { shopIdLongVal = shopIdStr.isEmpty() ? null : Long.parseLong(shopIdStr); } catch (NumberFormatException e) { }
        final Long shopIdLong = shopIdLongVal;

        final String channelFinal = channel;
        final ChannelOrderDto dtoFinal = dto;
        final String idemKey = "oms:upsert:" + channel + ":" + shopIdStr + ":" + dto.getExternalOrderId();
        final String requestIdFinal = (requestId == null || requestId.isBlank()) ? UUID.randomUUID().toString() : requestId;

        return idempotentService.execute(requestIdFinal, idemKey, () -> {
            UnifiedOrder existing = findByExternal(channelFinal, shopIdLong, shopIdStr, dtoFinal.getExternalOrderId());
            if (existing != null) return get(existing.getId());

            String extId = dtoFinal.getExternalOrderId();
            String unifiedOrderNo = "UO" + System.currentTimeMillis() + "-" + (extId.length() > 8 ? extId.substring(0, 8) : extId);
            UnifiedOrder order = new UnifiedOrder();
            order.setUnifiedOrderNo(unifiedOrderNo);
            order.setPlatformCode(channelFinal);
            order.setChannel(channelFinal);
            order.setExternalOrderId(extId);
            order.setPlatformOrderId(extId);
            order.setPlatformOrderNo(dtoFinal.getOrderNo());
            order.setShopId(shopIdLong != null ? shopIdLong : 0L);
            order.setServiceLevel(dtoFinal.getServiceLevel());
            order.setOrderCreatedAt(dtoFinal.getOrderCreatedAt());
            order.setBuyerName(dtoFinal.getBuyerName());
            order.setBuyerEmail(dtoFinal.getBuyerEmail());
            order.setBuyerNote(dtoFinal.getBuyerNote());
            order.setCurrency(dtoFinal.getCurrency());
            order.setTotalAmount(dtoFinal.getTotalAmount());
            order.setShippingAmount(dtoFinal.getShippingAmount());
            order.setTaxAmount(dtoFinal.getTaxAmount());
            order.setDiscountAmount(dtoFinal.getDiscountAmount());
            order.setOrderStatus("NEW");
            order.setPaymentStatus(null);
            order.setRiskFlag(0);
            order.setMarketplaceId(dtoFinal.getMarketplaceId());
            order.setTenantId(tenantId());
            order.setFactoryId(factoryId());
            orderMapper.insert(order);

            List<ChannelOrderItemDto> items = dtoFinal.getItems();
            if (items != null) {
                for (ChannelOrderItemDto it : items) {
                    UnifiedOrderItem line = new UnifiedOrderItem();
                    line.setUnifiedOrderId(order.getId());
                    line.setLineNo(it.getLineNo());
                    line.setPlatformSkuCode(it.getExternalSku());
                    line.setProductName(it.getItemTitle());
                    line.setQuantity(it.getQty());
                    line.setUnitPrice(it.getUnitPrice());
                    line.setItemStatus("NEW");
                    line.setPersonalizationJson(it.getPersonalizationJson());
                    line.setTenantId(tenantId());
                    line.setFactoryId(factoryId());
                    orderItemMapper.insert(line);

                    SkuMappingResolver.ResolveResult res = skuMappingResolver.resolveOrHold(channelFinal, shopIdStr, extId, it.getExternalSku(), order.getId(), line.getId());
                    if (res.isResolved()) {
                        line.setSkuId(res.getSkuId());
                        line.setSkuCode(res.getSkuCode());
                        orderItemMapper.updateById(line);
                    }
                }
            }
            return get(order.getId());
        });
    }

    private UnifiedOrder findByExternal(String channel, Long shopIdLong, String shopIdStr, String externalOrderId) {
        LambdaQueryWrapper<UnifiedOrder> q = new LambdaQueryWrapper<>();
        q.eq(UnifiedOrder::getTenantId, tenantId()).eq(UnifiedOrder::getFactoryId, factoryId()).eq(UnifiedOrder::getDeleted, 0)
          .eq(UnifiedOrder::getChannel, channel).eq(UnifiedOrder::getExternalOrderId, externalOrderId);
        if (shopIdLong != null) q.eq(UnifiedOrder::getShopId, shopIdLong);
        return orderMapper.selectOne(q);
    }
}
