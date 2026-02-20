package com.pod.tms.gateway;

/**
 * getOrderItems 调用非 2xx 时抛出，供 sendAck 转为 FAILED_MANUAL / FAILED_RETRYABLE 与 OMS Hold。
 */
public class AmazonOrderItemsApiException extends RuntimeException {

    private final GetOrderItemsResult result;

    public AmazonOrderItemsApiException(GetOrderItemsResult result) {
        super("getOrderItems failed: " + (result != null ? result.getHttpStatusCode() + " " + result.getErrorMessage() : ""));
        this.result = result;
    }

    public GetOrderItemsResult getResult() {
        return result;
    }
}
