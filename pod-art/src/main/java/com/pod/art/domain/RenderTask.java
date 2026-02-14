package com.pod.art.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

@TableName("art_render_task")
public class RenderTask extends BaseEntity {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    
    public static final int MAX_ATTEMPTS = 3;

    @TableField("art_job_id")
    private Long artJobId;
    
    @TableField("task_no")
    private String taskNo;
    
    private String status;
    private Integer attempts;
    private String lastError;
    private String outputUrl;

    public static RenderTask create(Long artJobId, String taskNo) {
        RenderTask task = new RenderTask();
        task.setArtJobId(artJobId);
        task.setTaskNo(taskNo);
        task.setStatus(STATUS_PENDING);
        task.setAttempts(0);
        return task;
    }
    
    public void start() {
        if (STATUS_SUCCESS.equals(this.status)) {
             throw new BusinessException("Task already succeeded");
        }
        this.status = STATUS_RUNNING;
    }

    public void success(String outputUrl) {
        this.status = STATUS_SUCCESS;
        this.outputUrl = outputUrl;
    }

    public void fail(String error) {
        this.status = STATUS_FAILED;
        this.lastError = error;
        this.attempts++;
    }

    public void retry() {
        if (!STATUS_FAILED.equals(this.status)) {
            throw new BusinessException("Only FAILED tasks can be retried");
        }
        this.status = STATUS_PENDING;
    }

    public Long getArtJobId() { return artJobId; }
    public void setArtJobId(Long artJobId) { this.artJobId = artJobId; }
    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public String getOutputUrl() { return outputUrl; }
    public void setOutputUrl(String outputUrl) { this.outputUrl = outputUrl; }
}
