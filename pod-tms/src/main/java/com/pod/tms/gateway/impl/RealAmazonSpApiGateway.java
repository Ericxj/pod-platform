package com.pod.tms.gateway.impl;

import com.pod.tms.gateway.AmazonSpApiGateway;
import com.pod.tms.gateway.ConfirmShipmentRequest;
import com.pod.tms.gateway.ConfirmShipmentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 真实 Amazon SP-API confirmShipment。需配置 LWA/签名/endpoint 后接入 Orders API 2026。
 * 当前为占位：可配置 tms.amazon.gateway=real 切换至此实现，再接入真实 HTTP 调用。
 */
@Component
@ConditionalOnProperty(name = "tms.amazon.gateway", havingValue = "real")
public class RealAmazonSpApiGateway implements AmazonSpApiGateway {

    private static final Logger log = LoggerFactory.getLogger(RealAmazonSpApiGateway.class);

    @Override
    public ConfirmShipmentResult confirmShipment(String amazonOrderId, ConfirmShipmentRequest request) {
        log.info("Real confirmShipment orderId={} marketplaceId={}", amazonOrderId, request != null ? request.getMarketplaceId() : null);
        // TODO: LWA token + request signing + POST sellingpartnerapi-xx.amazon.com/orders/v0/orders/{orderId}/shipmentConfirmation
        return ConfirmShipmentResult.fail(501, "NotImplemented", "Real SP-API not wired yet", null);
    }
}
