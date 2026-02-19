package com.pod.oms.job;

import com.pod.common.core.context.TenantContext;
import com.pod.oms.client.AmazonOrdersClient;
import com.pod.oms.domain.UnifiedOrder;
import com.pod.oms.dto.ChannelOrderDto;
import com.pod.oms.service.FulfillmentApplicationService;
import com.pod.oms.service.UnifiedOrderApplicationService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * XXL-JOB：拉取 Amazon 订单 -> 落库 -> SKU 映射 -> 失败写入 oms_order_hold。
 * 入参：shopId（必填）, lastUpdatedAfter, lastUpdatedBefore（可选，格式 yyyy-MM-dd HH:mm:ss）
 */
@Component
public class OmsPullAmazonOrdersJobHandler {

    private static final Logger log = LoggerFactory.getLogger(OmsPullAmazonOrdersJobHandler.class);
    private static final String CHANNEL_AMAZON = "AMAZON";

    @Autowired
    private AmazonOrdersClient amazonOrdersClient;
    @Autowired
    private UnifiedOrderApplicationService unifiedOrderApplicationService;
    @Autowired
    private FulfillmentApplicationService fulfillmentApplicationService;

    @XxlJob("omsPullAmazonOrdersJobHandler")
    public void pullAmazonOrders() {
        String shopId = null;
        LocalDateTime lastUpdatedAfter = LocalDateTime.now().minusDays(1);
        LocalDateTime lastUpdatedBefore = LocalDateTime.now();
        String param = XxlJobHelper.getJobParam();
        if (param != null && !param.isBlank()) {
            for (String p : param.split(",")) {
                String[] kv = p.split("=", 2);
                if (kv.length != 2) continue;
                String k = kv[0].trim();
                String v = kv[1].trim();
                if ("shopId".equals(k)) shopId = v;
                else if ("lastUpdatedAfter".equals(k)) lastUpdatedAfter = LocalDateTime.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                else if ("lastUpdatedBefore".equals(k)) lastUpdatedBefore = LocalDateTime.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        }
        if (shopId == null || shopId.isBlank()) {
            XxlJobHelper.handleFail("job param shopId required, e.g. shopId=1 or shopId=1,lastUpdatedAfter=2026-02-01 00:00:00");
            return;
        }

        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        if (tenantId == null) TenantContext.setTenantId(1L);
        if (factoryId == null) TenantContext.setFactoryId(1L);

        try {
            List<ChannelOrderDto> orders = amazonOrdersClient.fetchOrders(shopId, lastUpdatedAfter, lastUpdatedBefore);
            int count = 0;
            int fulfillmentsCreated = 0;
            for (ChannelOrderDto dto : orders) {
                String requestId = "xxl-" + XxlJobHelper.getJobId() + "-" + dto.getExternalOrderId();
                UnifiedOrder order = unifiedOrderApplicationService.upsertFromChannel(requestId, CHANNEL_AMAZON, shopId, dto);
                count++;
                if (order != null && order.getItems() != null && !order.getItems().isEmpty()) {
                    boolean allMapped = order.getItems().stream().allMatch(i -> i.getSkuId() != null);
                    if (allMapped) {
                        try {
                            fulfillmentApplicationService.createFromUnifiedOrder(order.getId());
                            fulfillmentsCreated++;
                        } catch (Exception ex) {
                            log.warn("Create fulfillment for order {} failed: {}", order.getId(), ex.getMessage());
                        }
                    }
                }
            }
            XxlJobHelper.handleSuccess("Pulled " + count + " orders, " + fulfillmentsCreated + " fulfillments created");
        } catch (Exception e) {
            log.error("omsPullAmazonOrdersJobHandler failed", e);
            XxlJobHelper.handleFail(e.getMessage());
        }
    }
}
