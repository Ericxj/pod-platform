package com.pod.oms;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.pod.common.core.exception.BusinessException;
import com.pod.infra.context.RequestIdContext;
import com.pod.infra.idempotent.domain.IdempotentRecord;
import com.pod.infra.idempotent.mapper.IdempotentMapper;
import com.pod.infra.idempotent.service.IdempotentService;
import com.pod.infra.outbox.service.OutboxService;
import com.pod.inv.domain.InventoryBalance;
import com.pod.inv.mapper.InventoryBalanceMapper;
import com.pod.inv.mapper.InventoryLedgerMapper;
import com.pod.inv.mapper.InventoryReservationMapper;
import com.pod.inv.service.InventoryApplicationService;
import com.pod.oms.domain.Fulfillment;
import com.pod.oms.domain.FulfillmentItem;
import com.pod.oms.domain.UnifiedOrder;
import com.pod.oms.mapper.FulfillmentItemMapper;
import com.pod.oms.mapper.FulfillmentMapper;
import com.pod.oms.mapper.UnifiedOrderItemMapper;
import com.pod.oms.mapper.UnifiedOrderMapper;
import com.pod.oms.service.FulfillmentApplicationService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.slf4j.MDC;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {FulfillmentApplicationService.class, InventoryApplicationService.class, IdempotentService.class})
public class OmsInvIntegrationTest {

    @MockBean
    private FulfillmentMapper fulfillmentMapper;
    @MockBean
    private FulfillmentItemMapper fulfillmentItemMapper;
    @MockBean
    private UnifiedOrderMapper orderMapper;
    @MockBean
    private UnifiedOrderItemMapper orderItemMapper;
    @MockBean
    private InventoryBalanceMapper balanceMapper;
    @MockBean
    private InventoryReservationMapper reservationMapper;
    @MockBean
    private InventoryLedgerMapper ledgerMapper;
    @MockBean
    private IdempotentMapper idempotentMapper;
    @MockBean
    private OutboxService outboxService;

    @Autowired
    private FulfillmentApplicationService fulfillmentService;

    @BeforeAll
    public static void setup() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Fulfillment.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), FulfillmentItem.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), InventoryBalance.class);
    }

    /**
     * Write API without X-Request-Id (no requestId in MDC) must throw BusinessException with code 400.
     */
    @Test
    public void testCreateFulfillment_WithoutRequestId_Returns400() {
        MDC.remove(RequestIdContext.MDC_KEY);
        Long orderId = 100L;
        UnifiedOrder order = new UnifiedOrder();
        order.setId(orderId);
        order.setOrderStatus("VALIDATED");
        when(orderMapper.selectById(orderId)).thenReturn(order);
        when(fulfillmentMapper.selectCount(any())).thenReturn(0L);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> {
            fulfillmentService.createFulfillment(orderId);
        });
        Assertions.assertEquals(400, ex.getCode());
        Assertions.assertTrue(ex.getMessage().contains("X-Request-Id"));
    }

    @Test
    public void testIdempotency_CreateFulfillment() {
        String requestId = "req-duplicate-1";
        Long orderId = 100L;

        // Mock Idempotency: First time success, Second time fail
        when(idempotentMapper.insert(any(IdempotentRecord.class)))
            .thenReturn(1)
            .thenThrow(new DuplicateKeyException("Duplicate"));

        // Mock Order
        UnifiedOrder order = new UnifiedOrder();
        order.setId(orderId);
        order.setOrderStatus("VALIDATED");
        when(orderMapper.selectById(orderId)).thenReturn(order);

        // Mock Fulfillment check (first time not found)
        when(fulfillmentMapper.selectCount(any())).thenReturn(0L);

        MDC.put(RequestIdContext.MDC_KEY, requestId);
        try {
            try {
                fulfillmentService.createFulfillment(orderId);
            } catch (Exception e) {
                Assertions.fail("First call should succeed");
            }
            Assertions.assertThrows(BusinessException.class, () -> {
                fulfillmentService.createFulfillment(orderId);
            });
        } finally {
            MDC.remove(RequestIdContext.MDC_KEY);
        }
    }

    @Test
    public void testConcurrency_ReleaseFulfillment() throws InterruptedException {
        Long fulfillmentId = 200L;
        String reqId1 = "req-c-1";
        String reqId2 = "req-c-2";

        // Mock Fulfillment
        when(fulfillmentMapper.selectById(fulfillmentId)).thenAnswer(invocation -> {
            Fulfillment f = new Fulfillment();
            f.setId(fulfillmentId);
            f.setStatus("CREATED");
            f.setVersion(1);
            f.setFulfillmentNo("FF-001");
            return f;
        });
        
        // Mock Items
        FulfillmentItem item = new FulfillmentItem();
        item.setSkuId(10L);
        item.setQty(10);
        when(fulfillmentItemMapper.selectList(any())).thenReturn(Collections.singletonList(item));

        // Mock Inventory Balance
        InventoryBalance balance = new InventoryBalance();
        balance.setId(500L);
        balance.setSkuId(10L);
        balance.setOnHandQty(100);
        balance.setAllocatedQty(0);
        balance.setAvailableQty(100);
        balance.setVersion(1);
        when(balanceMapper.selectOne(any())).thenReturn(balance);
        when(balanceMapper.updateBalanceWithVersion(any(), any(), any(), any(), any(), any())).thenReturn(1);

        // DB 条件更新：WHERE id=? AND status=? AND version=?，只有一次更新成功
        AtomicInteger updateAttempt = new AtomicInteger(0);
        when(fulfillmentMapper.updateStatusWithLock(any(Long.class), any(), any(), any(Integer.class), any()))
                .thenAnswer(invocation -> updateAttempt.incrementAndGet() == 1 ? 1 : 0);

        // Mock Idempotency to always succeed for different requestIds
        when(idempotentMapper.insert(any(IdempotentRecord.class))).thenReturn(1);

        // Execute in threads
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Consumer<String> task = (reqId) -> {
            try {
                MDC.put(RequestIdContext.MDC_KEY, reqId);
                try {
                    fulfillmentService.releaseFulfillment(fulfillmentId);
                    successCount.incrementAndGet();
                } finally {
                    MDC.remove(RequestIdContext.MDC_KEY);
                }
            } catch (BusinessException e) {
                if (e.getMessage() != null && e.getMessage().contains(Fulfillment.ERR_CONCURRENT)) {
                    failCount.incrementAndGet();
                } else {
                    throw e;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        };

        executor.submit(() -> task.accept(reqId1));
        executor.submit(() -> task.accept(reqId2));

        latch.await(5, TimeUnit.SECONDS);

        // Verification
        // Only one should succeed
        Assertions.assertEquals(1, successCount.get()); 
        Assertions.assertEquals(1, failCount.get());
        
        // Note: Because I mocked `fulfillmentMapper.selectById` to return the SAME object instance, 
        // both threads see version=1. 
        // The service calls `fulfillmentMapper.update(..., wrapper.eq(version, 1))`.
        // The mock returns 1 for first, 0 for second.
        // So the second one should throw BusinessException("Concurrent modification...").
    }
}
