package com.pod.tms.gateway;

/**
 * Amazon SP-API 网关。confirmShipment: POST /orders/v0/orders/{orderId}/shipmentConfirmation。
 */
public interface AmazonSpApiGateway {

    /**
     * 提交发货确认。
     * @param amazonOrderId 平台订单 ID
     * @param request 请求体（marketplaceId + packageDetail）
     * @return 成功 204；失败包含 httpStatusCode/errorCode/errorMessage
     */
    ConfirmShipmentResult confirmShipment(String amazonOrderId, ConfirmShipmentRequest request);
}
