package com.pod.oms.dto;

import java.math.BigDecimal;

public class ChannelOrderItemDto {
    private int lineNo;
    private String externalSku;
    private String itemTitle;
    private int qty;
    private BigDecimal unitPrice;
    private String personalizationJson;

    public int getLineNo() { return lineNo; }
    public void setLineNo(int lineNo) { this.lineNo = lineNo; }
    public String getExternalSku() { return externalSku; }
    public void setExternalSku(String externalSku) { this.externalSku = externalSku; }
    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getPersonalizationJson() { return personalizationJson; }
    public void setPersonalizationJson(String personalizationJson) { this.personalizationJson = personalizationJson; }
}
