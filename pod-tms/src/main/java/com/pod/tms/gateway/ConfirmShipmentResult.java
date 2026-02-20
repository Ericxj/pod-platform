package com.pod.tms.gateway;

/**
 * Amazon confirmShipment 调用结果。204 成功；429/500/503 可重试；400/404 等需人工。
 */
public class ConfirmShipmentResult {
    private boolean success;
    private int httpStatusCode;
    private String responseBody;
    private String errorCode;
    private String errorMessage;

    public static ConfirmShipmentResult ok(int statusCode, String body) {
        ConfirmShipmentResult r = new ConfirmShipmentResult();
        r.setSuccess(true);
        r.setHttpStatusCode(statusCode);
        r.setResponseBody(body);
        return r;
    }

    public static ConfirmShipmentResult fail(int statusCode, String errorCode, String errorMessage, String body) {
        ConfirmShipmentResult r = new ConfirmShipmentResult();
        r.setSuccess(false);
        r.setHttpStatusCode(statusCode);
        r.setErrorCode(errorCode);
        r.setErrorMessage(errorMessage);
        r.setResponseBody(body);
        return r;
    }

    public boolean isRetryable() {
        return httpStatusCode == 429 || httpStatusCode == 500 || httpStatusCode == 503;
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
}
