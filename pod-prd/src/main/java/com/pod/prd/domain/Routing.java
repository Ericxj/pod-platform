package com.pod.prd.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.util.List;

@TableName("prd_routing")
public class Routing extends BaseEntity {

    private Long skuId;
    private Integer versionNo;
    private String status;

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";

    public void publish(List<RoutingStep> steps) {
        if (STATUS_PUBLISHED.equals(this.status)) return;
        if (steps == null || steps.isEmpty()) {
            throw new BusinessException("Routing must have at least one step before publish");
        }
        for (int i = 0; i < steps.size(); i++) {
            RoutingStep step = steps.get(i);
            if (step.getStepNo() == null || step.getStepNo() != i + 1) {
                throw new BusinessException("Routing step_no must be continuous (1,2,3,...)");
            }
            if (step.getOpCode() == null || step.getOpCode().trim().isEmpty()) {
                throw new BusinessException("Routing step op_code must be non-empty");
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
}
