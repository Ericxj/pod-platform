package com.pod.tms.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Amazon SP-API 2026 发货确认网关。当前为 Mock：直接返回成功，后续接入真实 SP-API confirmShipment。
 */
@Component
public class AmazonShipmentConfirmGateway implements ChannelFulfillmentGateway {

    private static final Logger log = LoggerFactory.getLogger(AmazonShipmentConfirmGateway.class);
    public static final String CHANNEL_AMAZON = "AMAZON";

    @Override
    public boolean syncShipmentToChannel(String channelCode, String platformOrderId, String carrierCode, String trackingNo, LocalDateTime shipDate) {
        if (!CHANNEL_AMAZON.equalsIgnoreCase(channelCode)) {
            log.warn("Unsupported channel for Amazon gateway: {}", channelCode);
            return false;
        }
        // TODO: 调用 Amazon SP-API 2026 confirmShipment / putShipmentConfirm
        log.info("Mock Amazon shipment confirm: orderId={}, carrier={}, trackingNo={}", platformOrderId, carrierCode, trackingNo);
        return true;
    }
}
