package com.pod.oms.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.time.LocalDateTime;

/**
 * OMS 异常队列表（如 SKU 映射失败）。同一 (tenant,factory,hold_type,channel,shop_id,external_order_id,external_sku) 唯一。
 */
@TableName("oms_order_hold")
public class OrderHold extends BaseEntity {

    public static final String HOLD_TYPE_SKU_MAPPING = "SKU_MAPPING";
    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_RESOLVED = "RESOLVED";

    private String holdType;
    private String status;
    private String reasonCode;
    private String reasonMsg;
    private String channel;
    private String shopId;
    private String externalOrderId;
    private String externalSku;
    private Long unifiedOrderId;
    private Long unifiedOrderItemId;
    private Long resolveSkuId;
    private String resolveSkuCode;
    private LocalDateTime resolvedAt;
    private Long resolvedBy;

    public void resolve(Long skuId, String skuCode, Long resolvedBy) {
        if (!STATUS_OPEN.equals(this.status)) {
            throw new BusinessException("Hold is not OPEN, cannot resolve");
        }
        if (skuId == null) {
            throw new BusinessException("skuId required to resolve");
        }
        this.resolveSkuId = skuId;
        this.resolveSkuCode = skuCode != null ? skuCode : this.resolveSkuCode;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = LocalDateTime.now();
        this.status = STATUS_RESOLVED;
    }

    public String getHoldType() { return holdType; }
    public void setHoldType(String holdType) { this.holdType = holdType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public String getReasonMsg() { return reasonMsg; }
    public void setReasonMsg(String reasonMsg) { this.reasonMsg = reasonMsg; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }
    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
    public String getExternalSku() { return externalSku; }
    public void setExternalSku(String externalSku) { this.externalSku = externalSku; }
    public Long getUnifiedOrderId() { return unifiedOrderId; }
    public void setUnifiedOrderId(Long unifiedOrderId) { this.unifiedOrderId = unifiedOrderId; }
    public Long getUnifiedOrderItemId() { return unifiedOrderItemId; }
    public void setUnifiedOrderItemId(Long unifiedOrderItemId) { this.unifiedOrderItemId = unifiedOrderItemId; }
    public Long getResolveSkuId() { return resolveSkuId; }
    public void setResolveSkuId(Long resolveSkuId) { this.resolveSkuId = resolveSkuId; }
    public String getResolveSkuCode() { return resolveSkuCode; }
    public void setResolveSkuCode(String resolveSkuCode) { this.resolveSkuCode = resolveSkuCode; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public Long getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(Long resolvedBy) { this.resolvedBy = resolvedBy; }
}
