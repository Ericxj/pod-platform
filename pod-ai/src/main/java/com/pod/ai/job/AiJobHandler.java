package com.pod.ai.job;

import com.pod.ai.service.AiApplicationService;
import com.pod.common.utils.TraceIdUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AiJobHandler {
    private static final Logger log = LoggerFactory.getLogger(AiJobHandler.class);

    private final AiApplicationService aiApplicationService;

    public AiJobHandler(AiApplicationService aiApplicationService) {
        this.aiApplicationService = aiApplicationService;
    }

    @XxlJob("aiDiagnoseWorker")
    public void aiDiagnoseWorker() {
        String traceId = TraceIdUtils.generateTraceId();
        TraceIdUtils.setTraceId(traceId);
        try {
            int shardIndex = XxlJobHelper.getShardIndex();
            int shardTotal = XxlJobHelper.getShardTotal();
            
            log.info("Job Start: aiDiagnoseWorker, Shard {}/{}", shardIndex, shardTotal);
            aiApplicationService.scanAndProcessTasks("DIAGNOSE", shardIndex, shardTotal);
            log.info("Job End: aiDiagnoseWorker");
            
            XxlJobHelper.handleSuccess("Finished");
        } catch (Exception e) {
            log.error("Job Failed", e);
            XxlJobHelper.handleFail(e.getMessage());
        } finally {
            TraceIdUtils.remove();
        }
    }

    @XxlJob("aiRenderWorker")
    public void renderTaskWorker() {
        String traceId = TraceIdUtils.generateTraceId();
        TraceIdUtils.setTraceId(traceId);
        try {
            int shardIndex = XxlJobHelper.getShardIndex();
            int shardTotal = XxlJobHelper.getShardTotal();
            
            log.info("Job Start: aiRenderWorker, Shard {}/{}", shardIndex, shardTotal);
            aiApplicationService.scanAndProcessTasks("ART_RENDER", shardIndex, shardTotal);
            log.info("Job End: aiRenderWorker");
            
            XxlJobHelper.handleSuccess("Finished");
        } catch (Exception e) {
            log.error("Job Failed", e);
            XxlJobHelper.handleFail(e.getMessage());
        } finally {
            TraceIdUtils.remove();
        }
    }
}
