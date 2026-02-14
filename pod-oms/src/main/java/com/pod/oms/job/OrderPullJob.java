package com.pod.oms.job;

import com.pod.oms.dto.FulfillmentCreateCmd;
import com.pod.oms.service.FulfillmentApplicationService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OrderPullJob {

    private static final Logger log = LoggerFactory.getLogger(OrderPullJob.class);

    private final FulfillmentApplicationService fulfillmentApplicationService;

    public OrderPullJob(FulfillmentApplicationService fulfillmentApplicationService) {
        this.fulfillmentApplicationService = fulfillmentApplicationService;
    }

    /**
     * Mock pulling orders from external platforms (e.g. Amazon/Shopify)
     */
    @XxlJob("orderPullJob")
    public void pullOrders() {
        log.info("Starting Order Pull Job...");
        // Job logic temporarily disabled during refactoring
    }
}
