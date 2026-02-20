package com.pod.tms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.time.LocalDateTime;

/**
 * 渠道发货回传任务聚合根。状态：CREATED -> SENDING -> SUCCESS | FAILED_RETRYABLE | FAILED_MANUAL。
 */
@TableName("channel_shipment_ack")
public class ChannelShipmentAck extends BaseEntity {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_SENDING = "SENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED_RETRYABLE = "FAILED_RETRYABLE";
    public static final String STATUS_FAILED_MANUAL = "FAILED_MANUAL";

    public static final String CHANNEL_AMAZON = "AMAZON";
    public static final int MAX_RETRY_COUNT = 10;

    private String channel;
    @TableField("marketplace_id")
    private String marketplaceId;
    @TableField("shop_id")
    private Long shopId;
    @TableField("amazon_order_id")
    private String amazonOrderId;
    @TableField("external_order_id")
    private String externalOrderId;
    @TableField("package_reference_id")
    private String packageReferenceId;
    @TableField("carrier_code")
    private String carrierCode;
    @TableField("carrier_name")
    private String carrierName;
    @TableField("shipping_method")
    private String shippingMethod;
    @TableField("tracking_no")
    private String trackingNo;
    @TableField("ship_date_utc")
    private LocalDateTime shipDateUtc;
    @TableField("request_payload_json")
    private String requestPayloadJson;
    @TableField("response_code")
    private Integer responseCode;
    @TableField("response_body")
    private String responseBody;
    @TableField("error_code")
    private String errorCode;
    @TableField("error_message")
    private String errorMessage;
    private String status;
    @TableField("retry_count")
    private Integer retryCount;
    @TableField("next_retry_at")
    private LocalDateTime nextRetryAt;
    @TableField("last_attempt_at")
    private LocalDateTime lastAttemptAt;
    @TableField("business_idempotency_key")
    private String businessIdempotencyKey;
    @TableField("wms_shipment_id")
    private Long wmsShipmentId;
    @TableField("outbound_id")
    private Long outboundId;
    @TableField("fulfillment_id")
    private Long fulfillmentId;
    @TableField("unified_order_id")
    private Long unifiedOrderId;

    public void markSending() {
        if (!STATUS_CREATED.equals(this.status) && !STATUS_FAILED_RETRYABLE.equals(this.status)) {
            throw new BusinessException("Ack must be CREATED or FAILED_RETRYABLE to send. Current: " + this.status);
        }
        this.status = STATUS_SENDING;
    }

    public void markSuccess(Integer responseCode, String responseBody) {
        if (!STATUS_SENDING.equals(this.status)) {
            throw new BusinessException("Ack must be SENDING to mark success. Current: " + this.status);
        }
        this.status = STATUS_SUCCESS;
        this.responseCode = responseCode;
        this.responseBody = responseBody != null && responseBody.length() > 4096 ? responseBody.substring(0, 4096) : responseBody;
        this.errorCode = null;
        this.errorMessage = null;
        this.nextRetryAt = null;
    }

    public void markFailedRetryable(int responseCode, String errorCode, String errorMessage, LocalDateTime nextRetryAt) {
        if (!STATUS_SENDING.equals(this.status)) {
            throw new BusinessException("Ack must be SENDING to mark failed retryable. Current: " + this.status);
        }
        this.status = STATUS_FAILED_RETRYABLE;
        this.responseCode = responseCode;
        this.errorCode = errorCode != null && errorCode.length() > 64 ? errorCode.substring(0, 64) : errorCode;
        this.errorMessage = errorMessage != null && errorMessage.length() > 1024 ? errorMessage.substring(0, 1024) : errorMessage;
        this.nextRetryAt = nextRetryAt;
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
    }

    public void markFailedManual(int responseCode, String errorCode, String errorMessage) {
        if (!STATUS_SENDING.equals(this.status)) {
            throw new BusinessException("Ack must be SENDING to mark failed manual. Current: " + this.status);
        }
        this.status = STATUS_FAILED_MANUAL;
        this.responseCode = responseCode;
        this.errorCode = errorCode != null && errorCode.length() > 64 ? errorCode.substring(0, 64) : errorCode;
        this.errorMessage = errorMessage != null && errorMessage.length() > 1024 ? errorMessage.substring(0, 1024) : errorMessage;
        this.nextRetryAt = null;
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
    }

    public boolean canRetry() {
        if (retryCount != null && retryCount >= MAX_RETRY_COUNT) return false;
        return STATUS_CREATED.equals(status) || STATUS_FAILED_RETRYABLE.equals(status);
    }

    public static String buildIdempotencyKey(String channel, String amazonOrderId, String packageReferenceId) {
        return channel + "|" + amazonOrderId + "|" + packageReferenceId;
    }

    /** 指数退避：1m, 5m, 30m, 2h, 6h，上限 24h */
    public static LocalDateTime nextRetryAt(int retryCount, LocalDateTime now) {
        int minutes;
        if (retryCount <= 0) minutes = 1;
        else if (retryCount == 1) minutes = 5;
        else if (retryCount == 2) minutes = 30;
        else if (retryCount == 3) minutes = 120;
        else if (retryCount == 4) minutes = 360;
        else minutes = 1440;
        return now.plusMinutes(minutes);
    }

    // --- Getters & Setters ---
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getMarketplaceId() { return marketplaceId; }
    public void setMarketplaceId(String marketplaceId) { this.marketplaceId = marketplaceId; }
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public String getAmazonOrderId() { return amazonOrderId; }
    public void setAmazonOrderId(String amazonOrderId) { this.amazonOrderId = amazonOrderId; }
    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
    public String getPackageReferenceId() { return packageReferenceId; }
    public void setPackageReferenceId(String packageReferenceId) { this.packageReferenceId = packageReferenceId; }
    public String getCarrierCode() { return carrierCode; }
    public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }
    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
    public String getShippingMethod() { return shippingMethod; }
    public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public LocalDateTime getShipDateUtc() { return shipDateUtc; }
    public void setShipDateUtc(LocalDateTime shipDateUtc) { this.shipDateUtc = shipDateUtc; }
    public String getRequestPayloadJson() { return requestPayloadJson; }
    public void setRequestPayloadJson(String requestPayloadJson) { this.requestPayloadJson = requestPayloadJson; }
    public Integer getResponseCode() { return responseCode; }
    public void setResponseCode(Integer responseCode) { this.responseCode = responseCode; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    public LocalDateTime getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(LocalDateTime lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    public String getBusinessIdempotencyKey() { return businessIdempotencyKey; }
    public void setBusinessIdempotencyKey(String businessIdempotencyKey) { this.businessIdempotencyKey = businessIdempotencyKey; }
    public Long getWmsShipmentId() { return wmsShipmentId; }
    public void setWmsShipmentId(Long wmsShipmentId) { this.wmsShipmentId = wmsShipmentId; }
    public Long getOutboundId() { return outboundId; }
    public void setOutboundId(Long outboundId) { this.outboundId = outboundId; }
    public Long getFulfillmentId() { return fulfillmentId; }
    public void setFulfillmentId(Long fulfillmentId) { this.fulfillmentId = fulfillmentId; }
    public Long getUnifiedOrderId() { return unifiedOrderId; }
    public void setUnifiedOrderId(Long unifiedOrderId) { this.unifiedOrderId = unifiedOrderId; }
}
