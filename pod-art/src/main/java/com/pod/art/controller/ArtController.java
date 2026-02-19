package com.pod.art.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.art.domain.ArtJob;
import com.pod.art.domain.ProductionFile;
import com.pod.art.domain.RenderTask;
import com.pod.art.mapper.ArtJobMapper;
import com.pod.art.mapper.ProductionFileMapper;
import com.pod.art.mapper.RenderTaskMapper;
import com.pod.art.service.ArtJobApplicationService;
import com.pod.art.service.ArtJobService;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * P1.3 稿件/生产图 API：jobs 与 files，多租户过滤，权限标注。
 */
@RestController
@RequestMapping("/api/art")
public class ArtController {

    @Autowired
    private ArtJobApplicationService artJobApplicationService;
    @Autowired
    private ArtJobService artJobService;
    @Autowired
    private ArtJobMapper artJobMapper;
    @Autowired
    private ProductionFileMapper productionFileMapper;
    @Autowired
    private RenderTaskMapper renderTaskMapper;

    // ---------- P1.3 标准 API ----------

    @GetMapping("/jobs")
    @RequirePerm("art:job:page")
    public Result<IPage<ArtJob>> pageJobs(
            @RequestParam(name = "current", defaultValue = "1") Integer current,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "status", required = false) String status) {
        return Result.success(artJobApplicationService.page(new Page<>(current, size), status));
    }

    @GetMapping("/jobs/{id}")
    @RequirePerm("art:job:get")
    public Result<ArtJob> getJob(@PathVariable("id") Long id) {
        return Result.success(artJobApplicationService.get(id));
    }

    @PostMapping("/jobs/{id}/retry")
    @RequirePerm("art:job:retry")
    public Result<Void> retryJob(@PathVariable("id") Long id) {
        artJobApplicationService.retry(id);
        return Result.success();
    }

    @PostMapping("/jobs/from-fulfillment/{fulfillmentId}")
    @RequirePerm("art:job:create")
    public Result<List<Long>> createForFulfillment(@PathVariable("fulfillmentId") Long fulfillmentId) {
        return Result.success(artJobApplicationService.createForFulfillment(fulfillmentId));
    }

    @GetMapping("/files")
    @RequirePerm("art:file:page")
    public Result<IPage<ProductionFile>> pageFiles(
            @RequestParam(name = "current", defaultValue = "1") Integer current,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "jobId", required = false) Long jobId) {
        LambdaQueryWrapper<ProductionFile> q = new LambdaQueryWrapper<>();
        if (TenantContext.getTenantId() != null) q.eq(ProductionFile::getTenantId, TenantContext.getTenantId());
        if (TenantContext.getFactoryId() != null) q.eq(ProductionFile::getFactoryId, TenantContext.getFactoryId());
        q.eq(ProductionFile::getDeleted, 0);
        if (jobId != null) q.eq(ProductionFile::getArtJobId, jobId);
        q.orderByDesc(ProductionFile::getId);
        return Result.success(productionFileMapper.selectPage(new Page<>(current, size), q));
    }

    @GetMapping("/files/{id}")
    @RequirePerm("art:file:get")
    public Result<ProductionFile> getFile(@PathVariable("id") Long id) {
        ProductionFile f = productionFileMapper.selectById(id);
        if (f == null || !Objects.equals(f.getTenantId(), TenantContext.getTenantId()) || !Objects.equals(f.getFactoryId(), TenantContext.getFactoryId())
                || (f.getDeleted() != null && f.getDeleted() != 0)) {
            return Result.error("File not found: " + id);
        }
        return Result.success(f);
    }

    // ---------- 兼容旧路径（RenderTask / 单 job 创建） ----------

    @PostMapping("/artJobs/fromFulfillment/{fulfillmentId}")
    public Result<Long> createFromFulfillment(@PathVariable("fulfillmentId") Long fulfillmentId,
                                              @RequestParam(value = "jobNo", defaultValue = "JOB-AUTO") String jobNo) {
        if ("JOB-AUTO".equals(jobNo)) {
            jobNo = "JOB-" + fulfillmentId + "-" + System.currentTimeMillis();
        }
        return Result.success(artJobService.createJobFromFulfillment(fulfillmentId, jobNo));
    }

    @GetMapping("/artJobs/page")
    public Result<IPage<ArtJob>> pageJobsLegacy(Page<ArtJob> page) {
        LambdaQueryWrapper<ArtJob> q = new LambdaQueryWrapper<>();
        if (TenantContext.getTenantId() != null) q.eq(ArtJob::getTenantId, TenantContext.getTenantId());
        if (TenantContext.getFactoryId() != null) q.eq(ArtJob::getFactoryId, TenantContext.getFactoryId());
        q.eq(ArtJob::getDeleted, 0).orderByDesc(ArtJob::getId);
        return Result.success(artJobMapper.selectPage(page, q));
    }

    @GetMapping("/artJobs/{id}")
    public Result<ArtJob> getJobLegacy(@PathVariable("id") Long id) {
        return Result.success(artJobApplicationService.get(id));
    }

    @GetMapping("/renderTasks/page")
    public Result<IPage<RenderTask>> pageTasks(Page<RenderTask> page) {
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
