package com.pod.tms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * Amazon getOrderItems 缓存。uk(tenant_id, factory_id, amazon_order_id, marketplace_id)。
 */
@TableName("amazon_order_items_cache")
public class AmazonOrderItemsCache extends BaseEntity {

    public static final String STATUS_VALID = "VALID";
    public static final String STATUS_EXPIRED = "EXPIRED";

    @TableField("amazon_order_id")
    private String amazonOrderId;
    @TableField("marketplace_id")
    private String marketplaceId;
    @TableField("payload_json")
    private String payloadJson;
    @TableField("fetched_at")
    private LocalDateTime fetchedAt;
    @TableField("expire_at")
    private LocalDateTime expireAt;
    private String status;

    public String getAmazonOrderId() { return amazonOrderId; }
    public void setAmazonOrderId(String amazonOrderId) { this.amazonOrderId = amazonOrderId; }
    public String getMarketplaceId() { return marketplaceId; }
    public void setMarketplaceId(String marketplaceId) { this.marketplaceId = marketplaceId; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
