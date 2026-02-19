package com.pod.art.job;

import com.pod.art.service.ArtProductionGenerateService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * P1.3 XXL-JOB：扫描 PENDING/FAILED 生产图任务，调用渲染网关，写 production_file，推进 fulfillment 至 ART_READY。
 */
@Component
public class ArtGenerateProductionFilesJobHandler {

    @Autowired
    private ArtProductionGenerateService artProductionGenerateService;

    @XxlJob("artGenerateProductionFilesJobHandler")
    public void generateProductionFiles() {
        int processed = artProductionGenerateService.processNextBatch();
        // 可打日志或 metrics
    }
}
