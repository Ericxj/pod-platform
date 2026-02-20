package com.pod.tms.gateway;

/**
 * 渠道履约网关：回传运单号/发货确认到平台（如 Amazon SP-API 2026）。
 */
public interface ChannelFulfillmentGateway {

    /**
     * 回传发货信息到渠道。
     * @param channelCode 渠道编码（AMAZON 等）
     * @param platformOrderId 平台订单/履约单 ID 或单号
     * @param carrierCode 承运商编码
     * @param trackingNo 运单号
     * @param shipDate 发货日期（可选）
     * @return 是否成功
     */
    boolean syncShipmentToChannel(String channelCode, String platformOrderId, String carrierCode, String trackingNo, java.time.LocalDateTime shipDate);
}
