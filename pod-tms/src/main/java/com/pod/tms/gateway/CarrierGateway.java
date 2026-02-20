package com.pod.tms.gateway;

/**
 * 承运商面单/运单网关。实现类：MockCarrierGateway、4PX、云途、Shippo、EasyPost 等。
 */
public interface CarrierGateway {

    /**
     * 创建面单，返回运单号与面单 URL。
     * @param carrierCode 承运商编码
     * @param serviceCode 服务编码（可选）
     * @param request 请求体（收件地址、包裹信息等）
     * @return 运单号与面单 URL
     */
    CreateLabelResult createLabel(String carrierCode, String serviceCode, CreateLabelRequest request);
}
