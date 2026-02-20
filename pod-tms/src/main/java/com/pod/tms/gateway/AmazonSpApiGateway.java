package com.pod.tms.gateway;

import java.util.List;

/**
 * Amazon SP-API 网关。confirmShipment: POST /orders/v0/orders/{orderId}/shipmentConfirmation；
 * getOrderItems: GET /orders/v0/orders/{orderId}/orderItems。
 */
public interface AmazonSpApiGateway {

    /**
     * 提交发货确认。
     * @param amazonOrderId 平台订单 ID
     * @param request 请求体（marketplaceId + packageDetail）
     * @return 成功 204；失败包含 httpStatusCode/errorCode/errorMessage
     */
    ConfirmShipmentResult confirmShipment(String amazonOrderId, ConfirmShipmentRequest request);

    /**
     * 拉取订单行（Orders API getOrderItems）。用于缺 external_order_item_id 时回填。
     * @param amazonOrderId 平台订单 ID
     * @return 成功 200 返回 OrderItems；401/403/404→FAILED_MANUAL，429/503→可重试
     */
    GetOrderItemsResult getOrderItems(String amazonOrderId);
}
