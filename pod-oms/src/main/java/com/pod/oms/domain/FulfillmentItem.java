package com.pod.oms.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("oms_fulfillment_item")
public class FulfillmentItem extends BaseEntity {
    private Long fulfillmentId;
    private Long unifiedOrderItemId;
    private Integer lineNo;
    private Long skuId;
    private Integer qty;
    private String status;
    private String personalizationJson;

    public Long getFulfillmentId() { return fulfillmentId; }
    public void setFulfillmentId(Long fulfillmentId) { this.fulfillmentId = fulfillmentId; }
    public Long getUnifiedOrderItemId() { return unifiedOrderItemId; }
    public void setUnifiedOrderItemId(Long unifiedOrderItemId) { this.unifiedOrderItemId = unifiedOrderItemId; }
    public Integer getLineNo() { return lineNo; }
    public void setLineNo(Integer lineNo) { this.lineNo = lineNo; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPersonalizationJson() { return personalizationJson; }
    public void setPersonalizationJson(String personalizationJson) { this.personalizationJson = personalizationJson; }
}
