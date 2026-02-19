package com.pod.art.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pod.art.domain.ArtJob;
import com.pod.art.domain.ProductionFile;
import com.pod.art.gateway.ProductionRenderGateway;
import com.pod.art.mapper.ArtJobMapper;
import com.pod.art.mapper.ProductionFileMapper;
import com.pod.common.core.context.TenantContext;
import com.pod.oms.domain.Fulfillment;
import com.pod.oms.domain.FulfillmentStatus;
import com.pod.oms.mapper.FulfillmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * P1.3 生产图生成：扫描 PENDING/FAILED 任务，调用渲染网关，写 production_file，推进 fulfillment 状态。
 */
@Service
public class ArtProductionGenerateService {

    private static final int BATCH_SIZE = 20;

    @Autowired
    private ArtJobMapper artJobMapper;
    @Autowired
    private ProductionFileMapper productionFileMapper;
    @Autowired
    private ProductionRenderGateway productionRenderGateway;
    @Autowired
    private FulfillmentMapper fulfillmentMapper;

    /**
     * 扫描 PENDING/FAILED，置 GENERATING（乐观锁），渲染，成功写 production_file 并 READY，失败则 FAILED + retry_count+1。
     * 当某 fulfillment 下全部 job 为 READY 时，将 fulfillment 状态置为 ART_READY。
     */
    @Transactional(rollbackFor = Exception.class)
    public int processNextBatch() {
        List<ArtJob> candidates = artJobMapper.selectList(
                new LambdaQueryWrapper<ArtJob>()
                        .in(ArtJob::getStatus, ArtJob.STATUS_PENDING, ArtJob.STATUS_FAILED)
                        .eq(ArtJob::getDeleted, 0)
                        .orderByAsc(ArtJob::getId)
                        .last("LIMIT " + BATCH_SIZE));
        int processed = 0;
        for (ArtJob job : candidates) {
            try {
                if (tryProcessOne(job)) {
                    processed++;
                }
            } catch (Exception e) {
                failJob(job, "RENDER_ERROR", e.getMessage());
            }
        }
        return processed;
    }

    private boolean tryProcessOne(ArtJob job) {
        int rows = artJobMapper.update(null, new LambdaUpdateWrapper<ArtJob>()
                .eq(ArtJob::getId, job.getId())
                .eq(ArtJob::getVersion, job.getVersion())
                .in(ArtJob::getStatus, ArtJob.STATUS_PENDING, ArtJob.STATUS_FAILED)
                .set(ArtJob::getStatus, ArtJob.STATUS_GENERATING)
                .setSql("version = version + 1"));
        if (rows == 0) {
            return false;
        }
        job.setVersion((job.getVersion() == null ? 0 : job.getVersion()) + 1);
        job.setStatus(ArtJob.STATUS_GENERATING);

        Long tenantId = job.getTenantId();
        Long factoryId = job.getFactoryId();
        if (tenantId != null) TenantContext.setTenantId(tenantId);
        if (factoryId != null) TenantContext.setFactoryId(factoryId);

        try {
            ProductionRenderGateway.RenderResult result = productionRenderGateway.render(tenantId, factoryId, job.getId());
            if (result == null) {
                failJob(job, "RENDER_EMPTY", "Gateway returned null");
                return true;
            }
            String fileNo = "PF-" + job.getArtJobNo();
            ProductionFile pf = result.toProductionFile(job.getId(), fileNo);
            if (pf.getFileHash() != null && !pf.getFileHash().isEmpty()) {
                Long exists = productionFileMapper.selectCount(new LambdaQueryWrapper<ProductionFile>()
                        .eq(ProductionFile::getTenantId, tenantId).eq(ProductionFile::getFactoryId, factoryId)
                        .eq(ProductionFile::getFileHash, pf.getFileHash()).eq(ProductionFile::getDeleted, 0));
                if (exists > 0) {
                    job.markReady();
                    artJobMapper.updateById(job);
                    tryAdvanceFulfillmentToArtReady(job.getFulfillmentId());
                    return true;
                }
            }
            productionFileMapper.insert(pf);
            job.markReady();
            artJobMapper.updateById(job);
            tryAdvanceFulfillmentToArtReady(job.getFulfillmentId());
            return true;
        } catch (Exception e) {
            failJob(job, "RENDER_ERROR", e.getMessage());
            return true;
        }
    }

    private void failJob(ArtJob job, String code, String msg) {
        job.incrementRetry();
        job.fail(code, msg);
        artJobMapper.updateById(job);
    }

    private void tryAdvanceFulfillmentToArtReady(Long fulfillmentId) {
        if (fulfillmentId == null) return;
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        List<ArtJob> all = artJobMapper.selectList(new LambdaQueryWrapper<ArtJob>()
                .eq(ArtJob::getFulfillmentId, fulfillmentId).eq(ArtJob::getDeleted, 0));
        boolean allReady = all.stream().allMatch(j -> ArtJob.STATUS_READY.equals(j.getStatus()));
        if (!allReady) return;
        Fulfillment f = fulfillmentMapper.selectById(fulfillmentId);
        if (f == null || !Objects.equals(f.getTenantId(), tenantId) || !Objects.equals(f.getFactoryId(), factoryId)
                || (f.getDeleted() != null && f.getDeleted() != 0)) return;
        if (!FulfillmentStatus.RESERVED.name().equals(f.getStatus())) return;
        f.markArtReady();
        fulfillmentMapper.updateStatusWithLock(fulfillmentId, f.getStatus(), FulfillmentStatus.RESERVED.name(), f.getVersion(), TenantContext.getUserId());
    }
}
