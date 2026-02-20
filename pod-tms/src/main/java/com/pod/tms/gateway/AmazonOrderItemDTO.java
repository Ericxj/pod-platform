package com.pod.tms.gateway;

/**
 * Amazon Orders API getOrderItems 单行。payload.OrderItems[] 元素。
 */
public class AmazonOrderItemDTO {
    private String orderItemId;
    private String sellerSKU;
    private String asin;
    private Integer quantityOrdered;

    public String getOrderItemId() { return orderItemId; }
    public void setOrderItemId(String orderItemId) { this.orderItemId = orderItemId; }
    public String getSellerSKU() { return sellerSKU; }
    public void setSellerSKU(String sellerSKU) { this.sellerSKU = sellerSKU; }
    public String getAsin() { return asin; }
    public void setAsin(String asin) { this.asin = asin; }
    public Integer getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; }
}
