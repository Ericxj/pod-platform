package com.pod.art.job;

import com.pod.art.service.ArtJobService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class RenderRetryJob {

    private final ArtJobService artJobService;

    public RenderRetryJob(ArtJobService artJobService) {
        this.artJobService = artJobService;
    }

    /**
     * Retry failed rendering tasks
     */
    @XxlJob("renderRetryJob")
    public void retryFailedTasks() {
        artJobService.retryFailedTasks();
    }

    /**
     * Worker to pick and execute tasks.
     * Logic: Scan PENDING or Timeout RUNNING tasks and execute them.
     */
    @XxlJob("artRenderWorker")
    public void renderTaskWorker() {
        // Loop to pick tasks until none left or time limit reached
        // For simple demo, pick one batch or loop a few times
        for (int i = 0; i < 10; i++) {
             boolean picked = artJobService.pickAndExecuteTask();
             if (!picked) {
                 break;
             }
        }
    }
}

