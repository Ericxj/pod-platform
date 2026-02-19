package com.pod.oms.client.impl;

import com.pod.oms.client.AmazonOrdersClient;
import com.pod.oms.dto.ChannelOrderDto;
import com.pod.oms.dto.ChannelOrderItemDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ConditionalOnProperty(name = "oms.amazon.client", havingValue = "mock", matchIfMissing = true)
@Component
public class MockAmazonOrdersClient implements AmazonOrdersClient {

    @Override
    public List<ChannelOrderDto> fetchOrders(String shopId, LocalDateTime lastUpdatedAfter, LocalDateTime lastUpdatedBefore) {
        List<ChannelOrderDto> list = new ArrayList<>();
        ChannelOrderDto o = new ChannelOrderDto();
        o.setExternalOrderId("AMZ-MOCK-" + System.currentTimeMillis());
        o.setOrderNo("AMZ-" + System.currentTimeMillis());
        o.setOrderCreatedAt(LocalDateTime.now().minusHours(1));
        o.setBuyerName("Mock Buyer");
        o.setCurrency("USD");
        o.setTotalAmount(new BigDecimal("29.99"));
        o.setShippingAmount(BigDecimal.ZERO);
        o.setTaxAmount(BigDecimal.ZERO);
        o.setDiscountAmount(BigDecimal.ZERO);
        List<ChannelOrderItemDto> items = new ArrayList<>();
        ChannelOrderItemDto item = new ChannelOrderItemDto();
        item.setLineNo(1);
        item.setExternalSku("AMZ-SELLER-SKU-001");
        item.setItemTitle("Mock Product");
        item.setQty(1);
        item.setUnitPrice(new BigDecimal("29.99"));
        items.add(item);
        o.setItems(items);
        list.add(o);
        return list;
    }
}
