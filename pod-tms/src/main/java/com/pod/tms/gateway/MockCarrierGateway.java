package com.pod.tms.gateway;

import org.springframework.stereotype.Component;

/**
 * Mock 承运商：直接返回模拟运单号与面单 URL，用于联调与重试测试。
 */
@Component
public class MockCarrierGateway implements CarrierGateway {

    @Override
    public CreateLabelResult createLabel(String carrierCode, String serviceCode, CreateLabelRequest request) {
        String ref = request != null && request.getReferenceNo() != null ? request.getReferenceNo() : "REF";
        String trackingNo = "MOCK-" + carrierCode + "-" + ref + "-" + System.currentTimeMillis();
        String labelUrl = "https://mock-carrier.com/label/" + trackingNo + ".pdf";
        return new CreateLabelResult(trackingNo, labelUrl, "PDF");
    }
}
