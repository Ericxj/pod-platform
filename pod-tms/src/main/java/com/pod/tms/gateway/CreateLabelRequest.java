package com.pod.tms.gateway;

import java.util.List;

public class CreateLabelRequest {
    private String referenceNo;
    private String shipToName;
    private String shipToPhone;
    private String shipToAddressLine1;
    private String shipToAddressLine2;
    private String shipToCity;
    private String shipToState;
    private String shipToPostCode;
    private String shipToCountry;
    private List<PackageItem> packages;

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public String getShipToName() { return shipToName; }
    public void setShipToName(String shipToName) { this.shipToName = shipToName; }
    public String getShipToPhone() { return shipToPhone; }
    public void setShipToPhone(String shipToPhone) { this.shipToPhone = shipToPhone; }
    public String getShipToAddressLine1() { return shipToAddressLine1; }
    public void setShipToAddressLine1(String shipToAddressLine1) { this.shipToAddressLine1 = shipToAddressLine1; }
    public String getShipToAddressLine2() { return shipToAddressLine2; }
    public void setShipToAddressLine2(String shipToAddressLine2) { this.shipToAddressLine2 = shipToAddressLine2; }
    public String getShipToCity() { return shipToCity; }
    public void setShipToCity(String shipToCity) { this.shipToCity = shipToCity; }
    public String getShipToState() { return shipToState; }
    public void setShipToState(String shipToState) { this.shipToState = shipToState; }
    public String getShipToPostCode() { return shipToPostCode; }
    public void setShipToPostCode(String shipToPostCode) { this.shipToPostCode = shipToPostCode; }
    public String getShipToCountry() { return shipToCountry; }
    public void setShipToCountry(String shipToCountry) { this.shipToCountry = shipToCountry; }
    public List<PackageItem> getPackages() { return packages; }
    public void setPackages(List<PackageItem> packages) { this.packages = packages; }

    public static class PackageItem {
        private Integer weightG;
        private Integer lengthMm;
        private Integer widthMm;
        private Integer heightMm;
        public Integer getWeightG() { return weightG; }
        public void setWeightG(Integer weightG) { this.weightG = weightG; }
        public Integer getLengthMm() { return lengthMm; }
        public void setLengthMm(Integer lengthMm) { this.lengthMm = lengthMm; }
        public Integer getWidthMm() { return widthMm; }
        public void setWidthMm(Integer widthMm) { this.widthMm = widthMm; }
        public Integer getHeightMm() { return heightMm; }
        public void setHeightMm(Integer heightMm) { this.heightMm = heightMm; }
    }
}
