package com.pod.prd.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.util.List;

@TableName("prd_bom")
public class Bom extends BaseEntity {

    private Long skuId;
    private Integer versionNo;
    private String status;
    private String remark;

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";

    public void publish(List<BomItem> items) {
        if (STATUS_PUBLISHED.equals(this.status)) return;
        if (items == null || items.isEmpty()) {
            throw new BusinessException("BOM must have at least one item before publish");
        }
        for (BomItem item : items) {
            if (item.getQty() == null || item.getQty().doubleValue() <= 0) {
                throw new BusinessException("BOM item qty must be positive");
            }
        }
        this.status = STATUS_PUBLISHED;
    }

    public void unpublish() {
        if (STATUS_PUBLISHED.equals(this.status)) this.status = STATUS_DRAFT;
    }

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
