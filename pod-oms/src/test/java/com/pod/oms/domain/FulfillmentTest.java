package com.pod.oms.domain;

import com.pod.common.core.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 履约单聚合根行为与状态迁移校验。
 */
class FulfillmentTest {

    @Test
    void createFromUnifiedOrder_validOrder_createsWithItems() {
        UnifiedOrder order = new UnifiedOrder();
        order.setId(100L);
        order.setOrderStatus("VALIDATED");
        order.setUnifiedOrderNo("ORD-001");
        UnifiedOrderItem item = new UnifiedOrderItem();
        item.setId(1L);
        item.setSkuId(10L);
        item.setQuantity(2);
        List<UnifiedOrderItem> items = List.of(item);

        Fulfillment f = Fulfillment.createFromUnifiedOrder(order, items);
        assertEquals("FF-ORD-001", f.getFulfillmentNo());
        assertEquals(FulfillmentStatus.CREATED.name(), f.getStatus());
        assertEquals(1, f.getItems().size());
        assertEquals(10L, f.getItems().get(0).getSkuId());
        assertEquals(2, f.getItems().get(0).getQty().intValue());
    }

    @Test
    void createFromUnifiedOrder_orderNotValidated_throws() {
        UnifiedOrder order = new UnifiedOrder();
        order.setId(100L);
        order.setOrderStatus("NEW");
        order.setUnifiedOrderNo("ORD-001");
        UnifiedOrderItem item = new UnifiedOrderItem();
        item.setId(1L);
        item.setSkuId(10L);
        item.setQuantity(1);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                Fulfillment.createFromUnifiedOrder(order, List.of(item)));
        assertEquals("Order must be VALIDATED before fulfillment", ex.getMessage().split("\\. Current")[0]);
    }

    @Test
    void createFromUnifiedOrder_emptyItems_throws() {
        UnifiedOrder order = new UnifiedOrder();
        order.setId(100L);
        order.setOrderStatus("VALIDATED");
        order.setUnifiedOrderNo("ORD-001");

        assertThrows(BusinessException.class, () ->
                Fulfillment.createFromUnifiedOrder(order, Collections.emptyList()));
    }

    @Test
    void release_created_succeeds() {
        Fulfillment f = new Fulfillment();
        f.setStatus(FulfillmentStatus.CREATED.name());
        f.setItems(List.of(new FulfillmentItem()));
        f.release();
        assertEquals(FulfillmentStatus.RELEASED.name(), f.getStatus());
    }

    @Test
    void release_alreadyReleased_throws() {
        Fulfillment f = new Fulfillment();
        f.setStatus(FulfillmentStatus.RELEASED.name());
        f.setItems(List.of(new FulfillmentItem()));

        BusinessException ex = assertThrows(BusinessException.class, f::release);
        assertEquals("Fulfillment can only be released from CREATED", ex.getMessage().split("\\. Current")[0]);
    }

    @Test
    void release_noItems_throws() {
        Fulfillment f = new Fulfillment();
        f.setStatus(FulfillmentStatus.CREATED.name());
        f.setItems(Collections.emptyList());

        assertThrows(BusinessException.class, f::release);
    }

    @Test
    void cancel_created_succeeds() {
        Fulfillment f = new Fulfillment();
        f.setStatus(FulfillmentStatus.CREATED.name());
        f.cancel();
        assertEquals(FulfillmentStatus.CANCELLED.name(), f.getStatus());
    }

    @Test
    void cancel_released_succeeds() {
        Fulfillment f = new Fulfillment();
        f.setStatus(FulfillmentStatus.RELEASED.name());
        f.cancel();
        assertEquals(FulfillmentStatus.CANCELLED.name(), f.getStatus());
    }

    @Test
    void cancel_alreadyCancelled_idempotent() {
        Fulfillment f = new Fulfillment();
        f.setStatus(FulfillmentStatus.CANCELLED.name());
        f.cancel();
        assertEquals(FulfillmentStatus.CANCELLED.name(), f.getStatus());
    }

    @Test
    void confirm_created_succeeds() {
        Fulfillment f = new Fulfillment();
        f.setStatus(FulfillmentStatus.CREATED.name());
        f.setItems(List.of(new FulfillmentItem()));
        f.confirm();
        assertEquals(FulfillmentStatus.RELEASED.name(), f.getStatus());
    }
}
