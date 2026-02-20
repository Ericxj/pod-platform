package com.pod.oms.client.impl;

import com.pod.oms.client.TemuOrdersClient;
import com.pod.oms.dto.ChannelOrderDto;
import com.pod.oms.dto.ChannelOrderItemDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ConditionalOnProperty(name = "oms.temu.client", havingValue = "mock", matchIfMissing = true)
@Component
public class MockTemuOrdersClient implements TemuOrdersClient {

    @Override
    public List<ChannelOrderDto> fetchOrders(String shopId, LocalDateTime from, LocalDateTime to) {
        List<ChannelOrderDto> list = new ArrayList<>();
        ChannelOrderDto o = new ChannelOrderDto();
        o.setExternalOrderId("TEMU-MOCK-" + System.currentTimeMillis());
        o.setOrderNo("TM-" + System.currentTimeMillis());
        o.setOrderCreatedAt(LocalDateTime.now().minusHours(1));
        o.setBuyerName("Mock Temu Buyer");
        o.setCurrency("USD");
        o.setTotalAmount(new BigDecimal("19.99"));
        o.setShippingAmount(BigDecimal.ZERO);
        o.setTaxAmount(BigDecimal.ZERO);
        o.setDiscountAmount(BigDecimal.ZERO);
        List<ChannelOrderItemDto> items = new ArrayList<>();
        ChannelOrderItemDto item = new ChannelOrderItemDto();
        item.setLineNo(1);
        item.setExternalSku("TEMU-SKU-001");
        item.setItemTitle("Mock Temu Product");
        item.setQty(1);
        item.setUnitPrice(new BigDecimal("19.99"));
        items.add(item);
        o.setItems(items);
        list.add(o);
        return list;
    }
}
