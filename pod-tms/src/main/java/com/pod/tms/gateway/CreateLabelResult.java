package com.pod.tms.gateway;

public class CreateLabelResult {
    private String trackingNo;
    private String labelUrl;
    private String labelFormat;

    public CreateLabelResult() {}
    public CreateLabelResult(String trackingNo, String labelUrl, String labelFormat) {
        this.trackingNo = trackingNo;
        this.labelUrl = labelUrl;
        this.labelFormat = labelFormat;
    }

    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public String getLabelUrl() { return labelUrl; }
    public void setLabelUrl(String labelUrl) { this.labelUrl = labelUrl; }
    public String getLabelFormat() { return labelFormat; }
    public void setLabelFormat(String labelFormat) { this.labelFormat = labelFormat; }
}
