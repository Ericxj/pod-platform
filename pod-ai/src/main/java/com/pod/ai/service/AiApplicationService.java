package com.pod.ai.service;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.ai.domain.AiTask;
import com.pod.ai.mapper.AiTaskMapper;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.context.TenantIgnoreContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.common.utils.TraceIdUtils;
import com.pod.infra.context.RequestIdContext;
import com.pod.infra.idempotent.service.IdempotentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AiApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AiApplicationService.class);

    private final AiTaskMapper aiTaskMapper;
    private final IdempotentService idempotentService;

    public AiApplicationService(AiTaskMapper aiTaskMapper, IdempotentService idempotentService) {
        this.aiTaskMapper = aiTaskMapper;
        this.idempotentService = idempotentService;
    }

    /**
     * Create AI Task with Idempotency
     */
    public String createDiagnoseTask(String bizType, String bizNo, String payload) {
        String requestId = RequestIdContext.getRequired();
        try {
            return idempotentService.execute(requestId, "createAiTask:" + bizType + ":" + bizNo, () -> {
                // Check if exists
                AiTask existing = aiTaskMapper.selectOne(new LambdaQueryWrapper<AiTask>()
                        .eq(AiTask::getBizType, bizType)
                        .eq(AiTask::getBizNo, bizNo)
                        .last("LIMIT 1"));

                if (existing != null) {
                    return existing.getTaskNo();
                }

                AiTask task = new AiTask();
                task.setTaskNo("AI_" + bizNo); // Simple task no generation
                task.setTaskType("DIAGNOSE");
                task.setBizType(bizType);
                task.setBizNo(bizNo);
                task.setStatus(AiTask.STATUS_CREATED);
                task.setPayloadJson(payload);
                task.setAttempts(0);
                task.setMaxAttempts(3);
                
                // Explicitly set Tenant info to ensure it's populated
                task.setTenantId(TenantContext.getTenantId());
                task.setFactoryId(TenantContext.getFactoryId());
                task.setTraceId(TraceIdUtils.getTraceId());
                
                aiTaskMapper.insert(task);
                return task.getTaskNo();
            });
        } catch (BusinessException e) {
            // If duplicate request, try to return existing task
            if (e.getMessage().startsWith("Duplicate request")) {
                 AiTask existing = aiTaskMapper.selectOne(new LambdaQueryWrapper<AiTask>()
                        .eq(AiTask::getBizType, bizType)
                        .eq(AiTask::getBizNo, bizNo)
                        .last("LIMIT 1"));
                 if (existing != null) {
                     return existing.getTaskNo();
                 }
            }
            throw e;
        }
    }

    public AiTask getTask(String taskNo) {
        return aiTaskMapper.selectOne(new LambdaQueryWrapper<AiTask>().eq(AiTask::getTaskNo, taskNo));
    }

    /**
     * Scan and process tasks (Called by XXL-JOB)
     * Ignores Tenant Filter to scan globally, then sets context for processing
     */
    public void scanAndProcessTasks(String taskType, int shardIndex, int shardTotal) {
        System.err.println("DEBUG: scanAndProcessTasks start. type=" + taskType + ", shard=" + shardIndex + "/" + shardTotal);
        
        List<AiTask> tasks;
        TenantIgnoreContext.setIgnore(true);
        try {
            // Fetch pending tasks
            Page<AiTask> page = new Page<>(1, 100);
            aiTaskMapper.selectPage(page, new LambdaQueryWrapper<AiTask>()
                    .eq(taskType != null, AiTask::getTaskType, taskType)
                    .in(AiTask::getStatus, AiTask.STATUS_CREATED, AiTask.STATUS_FAILED)
                    .apply("attempts < max_attempts")
                    .apply("MOD(id, {0}) = {1}", shardTotal, shardIndex));
            tasks = page.getRecords();
        } finally {
            TenantIgnoreContext.clear();
        }

        System.err.println("DEBUG: Found tasks: " + tasks.size());
        log.info("Scanning tasks type={}, shard={}/{}. Found: {}", taskType, shardIndex, shardTotal, tasks.size());
        for (AiTask task : tasks) {
            System.err.println("DEBUG: Processing task: " + task.getId());
            processSingleTaskWithContext(task);
        }
    }

    private void processSingleTaskWithContext(AiTask task) {
        // Set Tenant Context from task
        Long oldTenant = TenantContext.getTenantId();
        Long oldFactory = TenantContext.getFactoryId();
        try {
            if (task.getTenantId() != null) TenantContext.setTenantId(task.getTenantId());
            if (task.getFactoryId() != null) TenantContext.setFactoryId(task.getFactoryId());
            
            processSingleTask(task);
        } finally {
            // Restore or clear
            if (oldTenant != null) TenantContext.setTenantId(oldTenant); else TenantContext.clear();
            if (oldFactory != null) TenantContext.setFactoryId(oldFactory);
        }
    }

    @Transactional
    public void processSingleTask(AiTask task) {
        // 1. Optimistic Lock: Preempt task
        // Update status CREATED/FAILED -> RUNNING
        int rows = aiTaskMapper.update(null, new LambdaUpdateWrapper<AiTask>()
                .set(AiTask::getStatus, AiTask.STATUS_RUNNING)
                .set(AiTask::getVersion, task.getVersion() + 1)
                .eq(AiTask::getId, task.getId())
                .eq(AiTask::getVersion, task.getVersion())
                .in(AiTask::getStatus, AiTask.STATUS_CREATED, AiTask.STATUS_FAILED));

        if (rows == 0) {
            log.info("Task {} preempted by another worker", task.getTaskNo());
            return;
        }

        log.info("Task {} preempted success, processing...", task.getTaskNo());

        // Refresh task to get new version
        task = aiTaskMapper.selectById(task.getId());

        try {
            // 2. Execute Mock Logic
            Thread.sleep(500); // Simulate processing
            String output = "{\"diagnosis\": \"Optimization suggested\", \"score\": 95}";

            // 3. Success
            task.setStatus(AiTask.STATUS_SUCCESS);
            task.setResultJson(output);
            aiTaskMapper.updateById(task); // Version handled by MP
            
        } catch (Exception e) {
            log.error("Task processing failed", e);
            // 4. Failure
            task.setStatus(AiTask.STATUS_FAILED);
            task.setErrorMsg(e.getMessage());
            task.setAttempts(task.getAttempts() + 1);
            aiTaskMapper.updateById(task);
        }
    }
}
