package com.pod.oms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

import java.math.BigDecimal;

@TableName("oms_unified_order_item")
public class UnifiedOrderItem extends BaseEntity {

    private Long unifiedOrderId;
    private Integer lineNo;
    private Long skuId;
    
    @TableField("sku_code")
    private String skuCode;
    
    @TableField("platform_sku")
    private String platformSkuCode;
    
    @TableField("item_title")
    private String productName;
    
    @TableField("qty")
    private Integer quantity;
    
    private BigDecimal unitPrice;
    private String itemStatus;
    private String personalizationJson;
    private String extraJson;
    @TableField("external_order_item_id")
    private String externalOrderItemId;
    @TableField("amazon_seller_sku")
    private String amazonSellerSku;
    @TableField("amazon_asin")
    private String amazonAsin;
    @TableField("amazon_quantity_ordered")
    private Integer amazonQuantityOrdered;

    // --- Getters & Setters ---

    public Long getUnifiedOrderId() { return unifiedOrderId; }
    public void setUnifiedOrderId(Long unifiedOrderId) { this.unifiedOrderId = unifiedOrderId; }
    public Integer getLineNo() { return lineNo; }
    public void setLineNo(Integer lineNo) { this.lineNo = lineNo; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getPlatformSkuCode() { return platformSkuCode; }
    public void setPlatformSkuCode(String platformSkuCode) { this.platformSkuCode = platformSkuCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getItemStatus() { return itemStatus; }
    public void setItemStatus(String itemStatus) { this.itemStatus = itemStatus; }
    public String getPersonalizationJson() { return personalizationJson; }
    public void setPersonalizationJson(String personalizationJson) { this.personalizationJson = personalizationJson; }
    public String getExtraJson() { return extraJson; }
    public void setExtraJson(String extraJson) { this.extraJson = extraJson; }
    public String getExternalOrderItemId() { return externalOrderItemId; }
    public void setExternalOrderItemId(String externalOrderItemId) { this.externalOrderItemId = externalOrderItemId; }
    public String getAmazonSellerSku() { return amazonSellerSku; }
    public void setAmazonSellerSku(String amazonSellerSku) { this.amazonSellerSku = amazonSellerSku; }
    public String getAmazonAsin() { return amazonAsin; }
    public void setAmazonAsin(String amazonAsin) { this.amazonAsin = amazonAsin; }
    public Integer getAmazonQuantityOrdered() { return amazonQuantityOrdered; }
    public void setAmazonQuantityOrdered(Integer amazonQuantityOrdered) { this.amazonQuantityOrdered = amazonQuantityOrdered; }
}
