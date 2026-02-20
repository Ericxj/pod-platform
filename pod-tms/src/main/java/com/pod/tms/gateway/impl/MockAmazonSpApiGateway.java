package com.pod.tms.gateway.impl;

import com.pod.tms.gateway.AmazonSpApiGateway;
import com.pod.tms.gateway.ConfirmShipmentRequest;
import com.pod.tms.gateway.ConfirmShipmentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mock Amazon SP-API confirmShipment。可通过配置模拟 204/429/503/400 用于联调与验收。
 * tms.amazon.confirm.mock.response=204|429|503|400
 */
@Component
@ConditionalOnProperty(name = "tms.amazon.gateway", havingValue = "mock", matchIfMissing = true)
public class MockAmazonSpApiGateway implements AmazonSpApiGateway {

    private static final Logger log = LoggerFactory.getLogger(MockAmazonSpApiGateway.class);

    @Value("${tms.amazon.confirm.mock.response:204}")
    private String mockResponse;

    @Override
    public ConfirmShipmentResult confirmShipment(String amazonOrderId, ConfirmShipmentRequest request) {
        log.info("[Mock] confirmShipment orderId={} marketplaceId={} tracking={} mockResponse={}",
            amazonOrderId, request != null ? request.getMarketplaceId() : null,
            request != null && request.getPackageDetail() != null ? request.getPackageDetail().getTrackingNumber() : null, mockResponse);
        if ("429".equals(mockResponse)) {
            return ConfirmShipmentResult.fail(429, "TooManyRequests", "Rate limit exceeded", "{\"errors\":[{\"code\":\"TooManyRequests\"}]}");
        }
        if ("503".equals(mockResponse)) {
            return ConfirmShipmentResult.fail(503, "ServiceUnavailable", "Temporary unavailable", "{\"message\":\"Service Unavailable\"}");
        }
        if ("400".equals(mockResponse)) {
            return ConfirmShipmentResult.fail(400, "InvalidInput", "Invalid shipment confirmation payload", "{\"errors\":[{\"code\":\"InvalidInput\"}]}");
        }
        return ConfirmShipmentResult.ok(204, null);
    }
}
