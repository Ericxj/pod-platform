package com.pod.ai.controller;

import com.pod.ai.domain.AiTask;
import com.pod.ai.service.AiApplicationService;
import com.pod.common.core.domain.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiApplicationService aiApplicationService;

    public AiController(AiApplicationService aiApplicationService) {
        this.aiApplicationService = aiApplicationService;
    }

    @PostMapping("/diagnose")
    public Result<Map<String, String>> diagnose(@RequestBody Map<String, Object> body) {
        String bizType = (String) body.get("bizType");
        String bizNo = (String) body.get("bizNo");
        String payload = body.toString();
        String taskNo = aiApplicationService.createDiagnoseTask(bizType, bizNo, payload);
        return Result.success(Map.of("taskNo", taskNo));
    }

    @GetMapping("/tasks/{taskNo}")
    public Result<AiTask> getTask(@PathVariable String taskNo) {
        AiTask task = aiApplicationService.getTask(taskNo);
        if (task == null) {
            return Result.error("Task not found");
        }
        return Result.success(task);
    }
}
