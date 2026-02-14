package com.pod.oms.dto;

import java.util.List;

public class FulfillmentCreateCmd {
    private String orderNo;
    private List<FulfillmentItemDto> items;
    private String requestId; // Passed from controller

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public List<FulfillmentItemDto> getItems() { return items; }
    public void setItems(List<FulfillmentItemDto> items) { this.items = items; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public static class FulfillmentItemDto {
        private String skuCode;
        private Integer quantity;
        public String getSkuCode() { return skuCode; }
        public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
