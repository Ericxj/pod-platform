package com.pod.prd.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

/**
 * 平台 SKU 映射。externalSku 在 (tenant,factory,channel,shopId) 下唯一。
 * bind 时校验内部 sku 存在且 ACTIVE。
 */
@TableName("prd_sku_mapping")
public class SkuMapping extends BaseEntity {

    private Long skuId;
    private String skuCode;
    private String channel;
    private String shopId;
    private String externalSku;
    private String externalName;
    private String remark;

    public void bind(Long skuId, String skuCode) {
        if (skuId == null) {
            throw new BusinessException("skuId required");
        }
        this.skuId = skuId;
        this.skuCode = skuCode != null ? skuCode : this.skuCode;
    }

    public void unbind() {
        this.skuId = null;
        this.skuCode = null;
    }

    public void update(String externalName, String remark) {
        if (externalName != null) this.externalName = externalName;
        if (remark != null) this.remark = remark;
    }

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }
    public String getExternalSku() { return externalSku; }
    public void setExternalSku(String externalSku) { this.externalSku = externalSku; }
    public String getExternalName() { return externalName; }
    public void setExternalName(String externalName) { this.externalName = externalName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
