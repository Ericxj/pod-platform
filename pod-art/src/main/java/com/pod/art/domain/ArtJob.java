package com.pod.art.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;

@TableName("art_job")
public class ArtJob extends BaseEntity {

    /** P1.3 状态：待处理 / 生成中 / 就绪 / 失败 */
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_GENERATING = "GENERATING";
    public static final String STATUS_READY = "READY";
    public static final String STATUS_FAILED = "FAILED";
    /** 兼容旧数据 */
    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_RENDERING = "RENDERING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @com.baomidou.mybatisplus.annotation.TableField("art_job_no")
    private String artJobNo;
    private Long fulfillmentId;
    @com.baomidou.mybatisplus.annotation.TableField("fulfillment_line_id")
    private Long fulfillmentLineId;
    private Long templateId;
    private String status;
    private Integer priority;
    @com.baomidou.mybatisplus.annotation.TableField("retry_count")
    private Integer retryCount;
    @com.baomidou.mybatisplus.annotation.TableField("last_error_code")
    private String lastErrorCode;
    @com.baomidou.mybatisplus.annotation.TableField("last_error_msg")
    private String lastErrorMsg;

    /** P1.3：按履约行创建任务（uk_line 幂等维度） */
    public static ArtJob createForLine(Long fulfillmentId, Long fulfillmentLineId, String artJobNo) {
        ArtJob job = new ArtJob();
        job.setArtJobNo(artJobNo);
        job.setStatus(STATUS_PENDING);
        job.setFulfillmentId(fulfillmentId);
        job.setFulfillmentLineId(fulfillmentLineId);
        job.setTemplateId(null);
        job.setPriority(100);
        job.setRetryCount(0);
        return job;
    }

    /** 兼容：按履约单创建（单 job，无 line 维度） */
    public static ArtJob createFromFulfillment(Long fulfillmentId, String artJobNo) {
        ArtJob job = new ArtJob();
        job.setArtJobNo(artJobNo);
        job.setStatus(STATUS_PENDING);
        job.setFulfillmentId(fulfillmentId);
        job.setFulfillmentLineId(null);
        job.setTemplateId(0L);
        job.setPriority(100);
        job.setRetryCount(0);
        return job;
    }

    public void startGenerating() {
        if (!STATUS_PENDING.equals(this.status) && !STATUS_FAILED.equals(this.status)
                && !STATUS_CREATED.equals(this.status) && !STATUS_RENDERING.equals(this.status)) {
            throw new BusinessException("Cannot start generating from status: " + this.status);
        }
        this.status = STATUS_GENERATING;
    }

    public void startRendering() {
        if (!STATUS_CREATED.equals(this.status) && !STATUS_FAILED.equals(this.status)) {
            throw new BusinessException("Cannot start rendering from status: " + this.status);
        }
        this.status = STATUS_RENDERING;
    }

    public void markReady() {
        if (!STATUS_GENERATING.equals(this.status) && !STATUS_RENDERING.equals(this.status)) {
            throw new BusinessException("Cannot mark ready from status: " + this.status);
        }
        this.status = STATUS_READY;
    }

    public void complete() {
        if (!STATUS_RENDERING.equals(this.status) && !STATUS_GENERATING.equals(this.status)) {
            throw new BusinessException("Cannot complete job from status: " + this.status);
        }
        this.status = STATUS_READY;
    }

    public void fail(String errorCode, String errorMsg) {
        this.status = STATUS_FAILED;
        this.lastErrorCode = errorCode;
        this.lastErrorMsg = errorMsg;
    }

    public void fail() {
        this.status = STATUS_FAILED;
    }

    public void incrementRetry() {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
    }

    public void resetForRetry() {
        if (!STATUS_FAILED.equals(this.status)) {
            throw new BusinessException("Only FAILED job can retry. Current: " + this.status);
        }
        this.status = STATUS_PENDING;
        this.lastErrorCode = null;
        this.lastErrorMsg = null;
    }

    public String getArtJobNo() { return artJobNo; }
    public void setArtJobNo(String artJobNo) { this.artJobNo = artJobNo; }
    public Long getFulfillmentId() { return fulfillmentId; }
    public void setFulfillmentId(Long fulfillmentId) { this.fulfillmentId = fulfillmentId; }
    public Long getFulfillmentLineId() { return fulfillmentLineId; }
    public void setFulfillmentLineId(Long fulfillmentLineId) { this.fulfillmentLineId = fulfillmentLineId; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public String getLastErrorCode() { return lastErrorCode; }
    public void setLastErrorCode(String lastErrorCode) { this.lastErrorCode = lastErrorCode; }
    public String getLastErrorMsg() { return lastErrorMsg; }
    public void setLastErrorMsg(String lastErrorMsg) { this.lastErrorMsg = lastErrorMsg; }
}
