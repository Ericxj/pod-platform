package com.pod.art.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;

@TableName("art_job")
public class ArtJob extends BaseEntity {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_RENDERING = "RENDERING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @com.baomidou.mybatisplus.annotation.TableField("art_job_no")
    private String artJobNo;
    
    private Long fulfillmentId;
    private Long templateId;
    
    private String status;
    private Integer priority;

    public static ArtJob createFromFulfillment(Long fulfillmentId, String artJobNo) {
        ArtJob job = new ArtJob();
        job.setArtJobNo(artJobNo);
        job.setStatus(STATUS_CREATED);
        job.setFulfillmentId(fulfillmentId);
        job.setTemplateId(0L); // Default
        job.setPriority(100);
        return job;
    }

    public void startRendering() {
        if (!STATUS_CREATED.equals(this.status) && !STATUS_FAILED.equals(this.status)) {
             throw new BusinessException("Cannot start rendering from status: " + this.status);
        }
        this.status = STATUS_RENDERING;
    }

    public void complete() {
        if (!STATUS_RENDERING.equals(this.status)) {
             throw new BusinessException("Cannot complete job from status: " + this.status);
        }
        this.status = STATUS_SUCCESS;
    }

    public void fail() {
        this.status = STATUS_FAILED;
    }

    public String getArtJobNo() { return artJobNo; }
    public void setArtJobNo(String artJobNo) { this.artJobNo = artJobNo; }
    public Long getFulfillmentId() { return fulfillmentId; }
    public void setFulfillmentId(Long fulfillmentId) { this.fulfillmentId = fulfillmentId; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
}
