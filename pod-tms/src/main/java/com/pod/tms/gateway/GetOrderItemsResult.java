package com.pod.tms.gateway;

import java.util.Collections;
import java.util.List;

/**
 * Amazon getOrderItems 调用结果。200 成功返回列表；非 2xx 与 ConfirmShipmentResult 一致语义（401/403/404→FAILED_MANUAL，429/503→可重试）。
 */
public class GetOrderItemsResult {
    private boolean success;
    private int httpStatusCode;
    private String responseBody;
    private String errorCode;
    private String errorMessage;
    private List<AmazonOrderItemDTO> orderItems;
    /** 是否来自缓存（P1.6++） */
    private boolean cacheHit;

    public static GetOrderItemsResult ok(List<AmazonOrderItemDTO> orderItems) {
        return ok(orderItems, false);
    }

    public static GetOrderItemsResult ok(List<AmazonOrderItemDTO> orderItems, boolean cacheHit) {
        GetOrderItemsResult r = new GetOrderItemsResult();
        r.setSuccess(true);
        r.setHttpStatusCode(200);
        r.setOrderItems(orderItems != null ? orderItems : Collections.emptyList());
        r.setCacheHit(cacheHit);
        return r;
    }

    public static GetOrderItemsResult fail(int statusCode, String errorCode, String errorMessage, String body) {
        GetOrderItemsResult r = new GetOrderItemsResult();
        r.setSuccess(false);
        r.setHttpStatusCode(statusCode);
        r.setErrorCode(errorCode);
        r.setErrorMessage(errorMessage);
        r.setResponseBody(body);
        r.setOrderItems(Collections.emptyList());
        return r;
    }

    /** 429/503 可重试；400/401/403/404 需人工。 */
    public boolean isRetryable() {
        return httpStatusCode == 429 || httpStatusCode == 503;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public int getHttpStatusCode() { return httpStatusCode; }
    public void setHttpStatusCode(int httpStatusCode) { this.httpStatusCode = httpStatusCode; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public List<AmazonOrderItemDTO> getOrderItems() { return orderItems; }
    public void setOrderItems(List<AmazonOrderItemDTO> orderItems) { this.orderItems = orderItems; }
    public boolean isCacheHit() { return cacheHit; }
    public void setCacheHit(boolean cacheHit) { this.cacheHit = cacheHit; }
}
