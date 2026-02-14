package com.pod.art.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pod.art.domain.ArtJob;
import com.pod.art.domain.ProductionFile;
import com.pod.art.domain.RenderTask;
import com.pod.art.mapper.ArtJobMapper;
import com.pod.art.mapper.ProductionFileMapper;
import com.pod.art.mapper.RenderTaskMapper;
import com.pod.common.core.exception.BusinessException;
import com.pod.infra.context.RequestIdContext;
import com.pod.infra.outbox.service.OutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ArtJobService {

    @Autowired
    private ArtJobMapper artJobMapper;

    @Autowired
    private RenderTaskMapper renderTaskMapper;

    @Autowired
    private ProductionFileMapper productionFileMapper;

    @Autowired
    private com.pod.infra.idempotent.service.IdempotentService idempotentService;

    @Autowired
    private OutboxService outboxService;

    // --- Public API ---

    @Transactional(rollbackFor = Exception.class)
    public Long createJobFromFulfillment(Long fulfillmentId, String jobNo) {
        String requestId = RequestIdContext.getRequired();
        return idempotentService.execute(requestId, "createArtJob:" + fulfillmentId, () -> {
            // Idempotency check: One ArtJob per Fulfillment
            Long count = artJobMapper.selectCount(new LambdaQueryWrapper<ArtJob>()
                    .eq(ArtJob::getFulfillmentId, fulfillmentId));
            if (count > 0) {
                ArtJob existing = artJobMapper.selectOne(new LambdaQueryWrapper<ArtJob>()
                        .eq(ArtJob::getFulfillmentId, fulfillmentId));
                return existing.getId();
            }

            ArtJob job = ArtJob.createFromFulfillment(fulfillmentId, jobNo);
            artJobMapper.insert(job);

            // Create tasks (e.g., Preview, HighRes) - In real world, determined by Template
            RenderTask task1 = RenderTask.create(job.getId(), "Production-PDF");
            renderTaskMapper.insert(task1);

            return job.getId();
        });
    }

    public ArtJob getJob(Long id) {
        return artJobMapper.selectById(id);
    }
    
    public ProductionFile getProductionFile(Long id) {
        return productionFileMapper.selectById(id);
    }

    // --- Job/Worker Methods ---

    /**
     * Pick a pending task to run.
     * Logic: status=PENDING OR (status=RUNNING AND updated_at < now-5min)
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean pickAndExecuteTask() {
        // 1. Find a candidate task
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5);
        
        List<RenderTask> tasks = renderTaskMapper.selectList(new LambdaQueryWrapper<RenderTask>()
                .and(w -> w.eq(RenderTask::getStatus, RenderTask.STATUS_PENDING)
                      .or(o -> o.eq(RenderTask::getStatus, RenderTask.STATUS_RUNNING).lt(RenderTask::getUpdatedAt, timeoutThreshold)))
                .lt(RenderTask::getAttempts, RenderTask.MAX_ATTEMPTS) // Max 3 attempts
                .last("LIMIT 1"));

        if (tasks.isEmpty()) {
            return false;
        }

        RenderTask task = tasks.get(0);
        
        // 2. Optimistic Lock to RUNNING
        // If it was RUNNING (timeout), we reset it to RUNNING (touch update_time)
        String oldStatus = task.getStatus();
        Integer version = task.getVersion();
        
        task.start(); // Set to RUNNING
        
        int rows = renderTaskMapper.update(task, new LambdaUpdateWrapper<RenderTask>()
                .eq(RenderTask::getId, task.getId())
                .eq(RenderTask::getVersion, version)); // Optimistic Lock
                
        if (rows == 0) {
            return false; // Contention, retry next time
        }

        // 3. Execute Mock Render (Synchronous here for simplicity, or could be async)
        try {
            // Mocking execution time
            // Thread.sleep(100); 
            
            String mockUrl = "http://localhost:9000/files/" + task.getId() + ".pdf";
            
            // Success
            completeTaskSuccess(task.getId(), mockUrl);
            
        } catch (Exception e) {
            // Fail
            completeTaskFail(task.getId(), e.getMessage());
        }
        
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeTaskSuccess(Long taskId, String url) {
        RenderTask task = renderTaskMapper.selectById(taskId);
        if (task == null) return;
        
        task.success(url);
        renderTaskMapper.updateById(task);
        
        checkAndCompleteJob(task.getArtJobId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeTaskFail(Long taskId, String error) {
        RenderTask task = renderTaskMapper.selectById(taskId);
        if (task == null) return;
        
        task.fail(error != null ? error : "Unknown error");
        renderTaskMapper.updateById(task);
        
        // If max retries reached, fail the job? Or leave it for manual intervention?
        // Current requirement: "RenderTaskRetry" job will retry it if attempts < max.
    }

    @Transactional(rollbackFor = Exception.class)
    public void retryFailedTasks() {
        // Find tasks that are FAILED and retry count < Max
        List<RenderTask> failedTasks = renderTaskMapper.selectList(new LambdaQueryWrapper<RenderTask>()
                .eq(RenderTask::getStatus, RenderTask.STATUS_FAILED)
                .lt(RenderTask::getAttempts, RenderTask.MAX_ATTEMPTS)
                .last("LIMIT 100"));
        
        for (RenderTask task : failedTasks) {
            task.retry();
            renderTaskMapper.updateById(task);
        }
    }

    // --- Internal Helpers ---

    private void checkAndCompleteJob(Long jobId) {
        ArtJob job = artJobMapper.selectById(jobId);
        // Ensure we only complete if currently RENDERING or CREATED (if skipped directly)
        // But logic says: Job starts RENDERING when task starts? 
        // Actually Job.startRendering() should be called when we create tasks or first task starts.
        // For simplicity, let's assume Job is in RENDERING or CREATED.
        
        // If job is already success, skip
        if (ArtJob.STATUS_SUCCESS.equals(job.getStatus())) return;

        // Check all tasks
        List<RenderTask> tasks = renderTaskMapper.selectList(new LambdaQueryWrapper<RenderTask>()
                .eq(RenderTask::getArtJobId, jobId));

        boolean allSuccess = tasks.stream().allMatch(t -> RenderTask.STATUS_SUCCESS.equals(t.getStatus()));

        if (allSuccess && !tasks.isEmpty()) {
            // Create ProductionFile
            // Idempotency check for ProductionFile
            Long pfCount = productionFileMapper.selectCount(new LambdaQueryWrapper<ProductionFile>()
                    .eq(ProductionFile::getArtJobId, jobId));
            
            String fileUrl = tasks.get(0).getOutputUrl(); // Take first task output for demo
            
            if (pfCount == 0) {
                ProductionFile pf = ProductionFile.create(job.getId(), "PF-" + job.getArtJobNo(), "PDF", fileUrl);
                productionFileMapper.insert(pf);
            }
            
            // Update Job Status
            if (ArtJob.STATUS_CREATED.equals(job.getStatus())) {
                job.startRendering(); // Ensure state transition validity
            }
            job.complete();
            artJobMapper.updateById(job);

            // Publish Outbox Event
            outboxService.publish("ArtJobCompleted", "ART_JOB", job.getId(), job.getArtJobNo(), 
                    new ArtJobCompletedEvent(job.getId(), job.getFulfillmentId(), job.getArtJobNo(), fileUrl));
        }
    }

    // Inner DTO for Event
    public static class ArtJobCompletedEvent {
        public Long jobId;
        public Long fulfillmentId;
        public String artJobNo;
        public String productionFile;

        public ArtJobCompletedEvent(Long jobId, Long fulfillmentId, String artJobNo, String productionFile) {
            this.jobId = jobId;
            this.fulfillmentId = fulfillmentId;
            this.artJobNo = artJobNo;
            this.productionFile = productionFile;
        }
        
        public ArtJobCompletedEvent() {} // For Jackson
    }
}
