package com.pod.tms.gateway;

import java.time.Instant;
import java.util.List;

/**
 * Amazon SP-API confirmShipment 请求体（Orders API 2026）。
 */
public class ConfirmShipmentRequest {
    private String marketplaceId;
    private String codCollectionMethod;
    private PackageDetail packageDetail;

    public String getMarketplaceId() { return marketplaceId; }
    public void setMarketplaceId(String marketplaceId) { this.marketplaceId = marketplaceId; }
    public String getCodCollectionMethod() { return codCollectionMethod; }
    public void setCodCollectionMethod(String codCollectionMethod) { this.codCollectionMethod = codCollectionMethod; }
    public PackageDetail getPackageDetail() { return packageDetail; }
    public void setPackageDetail(PackageDetail packageDetail) { this.packageDetail = packageDetail; }

    public static class PackageDetail {
        private String packageReferenceId;
        private String carrierCode;
        private String carrierName;
        private String shippingMethod;
        private String trackingNumber;
        private Instant shipDate;
        private List<OrderItem> orderItems;

        public String getPackageReferenceId() { return packageReferenceId; }
        public void setPackageReferenceId(String packageReferenceId) { this.packageReferenceId = packageReferenceId; }
        public String getCarrierCode() { return carrierCode; }
        public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }
        public String getCarrierName() { return carrierName; }
        public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
        public String getShippingMethod() { return shippingMethod; }
        public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
        public Instant getShipDate() { return shipDate; }
        public void setShipDate(Instant shipDate) { this.shipDate = shipDate; }
        public List<OrderItem> getOrderItems() { return orderItems; }
        public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    }

    public static class OrderItem {
        private String orderItemId;
        private Integer quantity;

        public String getOrderItemId() { return orderItemId; }
        public void setOrderItemId(String orderItemId) { this.orderItemId = orderItemId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
