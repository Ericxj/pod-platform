package com.pod.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pod.common.core.context.TenantContext;
import com.pod.oms.domain.UnifiedOrder;
import com.pod.oms.mapper.UnifiedOrderMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@Transactional
public class DataPermissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnifiedOrderMapper orderMapper;

    private String testOrderNo;

    @BeforeEach
    void setUp() {
        // Setup Context for Data Creation
        TenantContext.setTenantId(100001L);
        TenantContext.setFactoryId(200001L);

        testOrderNo = "TEST-PERM-" + UUID.randomUUID().toString().substring(0, 8);

        // Create a test order for Factory 200001
        UnifiedOrder order = new UnifiedOrder();
        order.setUnifiedOrderNo(testOrderNo);
        order.setPlatformCode("TEST");
        order.setShopId(1L);
        order.setPlatformOrderId("P1-" + testOrderNo);
        order.setPlatformOrderNo("P1-" + testOrderNo);
        order.setOrderStatus("NEW");
        order.setPaymentStatus("PAID");
        order.setTotalAmount(new java.math.BigDecimal("100.00"));
        order.setBuyerName("Test Buyer");
        order.setTenantId(100001L);
        order.setFactoryId(200001L); // Owned by Factory 200001
        orderMapper.insert(order);
        
        System.out.println("Inserted Order: " + testOrderNo);
        
        TenantContext.clear();
    }
    
    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testOrderIsolation_WithCorrectFactory() throws Exception {
        // Use Factory 200001
        mockMvc.perform(get("/api/oms/orders/page")
                .header("X-Tenant-Id", "100001")
                .header("X-Factory-Id", "200001")
                .param("page", "1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.unifiedOrderNo == '" + testOrderNo + "')]").exists());
    }

    @Test
    void testOrderIsolation_WithWrongFactory() throws Exception {
        // Use Factory 200002 (Different Factory)
        mockMvc.perform(get("/api/oms/orders/page")
                .header("X-Tenant-Id", "100001")
                .header("X-Factory-Id", "200002") // Intruder
                .param("page", "1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Should NOT find the order
                .andExpect(jsonPath("$.data.records[?(@.unifiedOrderNo == '" + testOrderNo + "')]").doesNotExist());
    }
}
