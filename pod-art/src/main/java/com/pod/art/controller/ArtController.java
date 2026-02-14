package com.pod.art.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.art.domain.ArtJob;
import com.pod.art.domain.ProductionFile;
import com.pod.art.domain.RenderTask;
import com.pod.art.mapper.ArtJobMapper;
import com.pod.art.mapper.RenderTaskMapper;
import com.pod.art.service.ArtJobService;
import com.pod.common.core.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/art")
public class ArtController {

    @Autowired
    private ArtJobService artJobService;

    @Autowired
    private ArtJobMapper artJobMapper;

    @Autowired
    private RenderTaskMapper renderTaskMapper;

    @PostMapping("/artJobs/fromFulfillment/{fulfillmentId}")
    public Result<Long> createFromFulfillment(@PathVariable("fulfillmentId") Long fulfillmentId,
                                         @RequestParam(value = "jobNo", defaultValue = "JOB-AUTO") String jobNo) {
        if ("JOB-AUTO".equals(jobNo)) {
             jobNo = "JOB-" + fulfillmentId + "-" + System.currentTimeMillis();
        }
        return Result.success(artJobService.createJobFromFulfillment(fulfillmentId, jobNo));
    }

    @GetMapping("/artJobs/page")
    public Result<Page<ArtJob>> pageJobs(Page<ArtJob> page) {
        return Result.success(artJobMapper.selectPage(page, new LambdaQueryWrapper<ArtJob>().orderByDesc(ArtJob::getId)));
    }
    
    @GetMapping("/artJobs/{id}")
    public Result<ArtJob> getJob(@PathVariable("id") Long id) {
        return Result.success(artJobService.getJob(id));
    }

    @GetMapping("/renderTasks/page")
    public Result<Page<RenderTask>> pageTasks(Page<RenderTask> page) {
        return Result.success(renderTaskMapper.selectPage(page, new LambdaQueryWrapper<RenderTask>().orderByDesc(RenderTask::getId)));
    }

    @PostMapping("/renderTasks/{id}/retry")
    public Result<Void> retryTask(@PathVariable("id") Long id) {
        RenderTask task = renderTaskMapper.selectById(id);
        if (task != null && "FAILED".equals(task.getStatus())) {
            task.retry();
            renderTaskMapper.updateById(task);
        }
        return Result.success();
    }

    @GetMapping("/productionFiles/{id}")
    public Result<ProductionFile> getProductionFile(@PathVariable("id") Long id) {
        return Result.success(artJobService.getProductionFile(id));
    }
}
