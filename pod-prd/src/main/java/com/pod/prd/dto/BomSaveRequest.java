package com.pod.prd.dto;

import com.pod.prd.domain.BomItem;
import java.util.List;

public class BomSaveRequest {
    private Long skuId;
    private Integer versionNo;
    private String remark;
    private List<BomItem> items;
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<BomItem> getItems() { return items; }
    public void setItems(List<BomItem> items) { this.items = items; }
}
