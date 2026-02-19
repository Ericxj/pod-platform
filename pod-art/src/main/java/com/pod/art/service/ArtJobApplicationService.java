package com.pod.art.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.art.domain.ArtJob;
import com.pod.art.mapper.ArtJobMapper;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.infra.context.RequestIdContext;
import com.pod.infra.idempotent.service.IdempotentService;
import com.pod.oms.domain.Fulfillment;
import com.pod.oms.domain.FulfillmentItem;
import com.pod.oms.domain.FulfillmentStatus;
import com.pod.oms.mapper.FulfillmentItemMapper;
import com.pod.oms.mapper.FulfillmentMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * P1.3 稿件任务应用服务：按履约单/履约行创建任务（幂等）、重试。
 */
@Service
public class ArtJobApplicationService {

    @Autowired
    private ArtJobMapper artJobMapper;
    @Autowired
    private FulfillmentMapper fulfillmentMapper;
    @Autowired
    private FulfillmentItemMapper fulfillmentItemMapper;
    @Autowired
    private IdempotentService idempotentService;

    private long tenantId() {
        return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L;
    }

    private long factoryId() {
        return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L;
    }

    /**
     * 为履约单创建生产图任务（按行幂等：uk_line = tenant_id, factory_id, fulfillment_id, fulfillment_line_id）。
     * 建议在 fulfillment 进入 RESERVED 后触发或手动触发。
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Long> createForFulfillment(Long fulfillmentId) {
        String requestId = RequestIdContext.get();
        if (requestId == null || requestId.isBlank()) {
            requestId = "art-create-" + fulfillmentId + "-" + System.currentTimeMillis();
        }
        return idempotentService.execute(requestId, "createArtJobForFulfillment:" + fulfillmentId, () -> {
            Fulfillment f = fulfillmentMapper.selectById(fulfillmentId);
            if (f == null || !Objects.equals(f.getTenantId(), tenantId()) || !Objects.equals(f.getFactoryId(), factoryId())
                    || (f.getDeleted() != null && f.getDeleted() != 0)) {
                throw new BusinessException("Fulfillment not found: " + fulfillmentId);
            }
            if (!FulfillmentStatus.RESERVED.name().equals(f.getStatus()) && !FulfillmentStatus.CREATED.name().equals(f.getStatus())) {
                throw new BusinessException("Fulfillment must be RESERVED or CREATED to create art jobs. Current: " + f.getStatus());
            }
            List<FulfillmentItem> items = fulfillmentItemMapper.selectList(
                    new LambdaQueryWrapper<FulfillmentItem>()
                            .eq(FulfillmentItem::getFulfillmentId, fulfillmentId)
                            .eq(FulfillmentItem::getDeleted, 0));
            if (items == null || items.isEmpty()) {
                throw new BusinessException("Fulfillment has no lines: " + fulfillmentId);
            }
            List<Long> created = new ArrayList<>();
            for (FulfillmentItem item : items) {
                Long lineId = item.getId();
                ArtJob existing = artJobMapper.selectOne(new LambdaQueryWrapper<ArtJob>()
                        .eq(ArtJob::getTenantId, tenantId()).eq(ArtJob::getFactoryId, factoryId())
                        .eq(ArtJob::getFulfillmentId, fulfillmentId).eq(ArtJob::getFulfillmentLineId, lineId)
                        .eq(ArtJob::getDeleted, 0));
                if (existing != null) {
                    created.add(existing.getId());
                    continue;
                }
                String artJobNo = "AJ-" + fulfillmentId + "-L" + lineId + "-" + System.currentTimeMillis();
                ArtJob job = ArtJob.createForLine(fulfillmentId, lineId, artJobNo);
                artJobMapper.insert(job);
                created.add(job.getId());
            }
            return created;
        });
    }

    /**
     * 分页查询（多租户/多工厂过滤）。
     */
    public IPage<ArtJob> page(Page<ArtJob> page, String status) {
        LambdaQueryWrapper<ArtJob> q = new LambdaQueryWrapper<>();
        q.eq(ArtJob::getTenantId, tenantId()).eq(ArtJob::getFactoryId, factoryId()).eq(ArtJob::getDeleted, 0);
        if (status != null && !status.isBlank()) q.eq(ArtJob::getStatus, status);
        q.orderByDesc(ArtJob::getId);
        return artJobMapper.selectPage(page, q);
    }

    /**
     * 按 ID 查询（租户/工厂校验）。
     */
    public ArtJob get(Long id) {
        ArtJob job = artJobMapper.selectById(id);
        if (job == null || !Objects.equals(job.getTenantId(), tenantId()) || !Objects.equals(job.getFactoryId(), factoryId())
                || (job.getDeleted() != null && job.getDeleted() != 0)) {
            throw new BusinessException("Art job not found: " + id);
        }
        return job;
    }

    /**
     * 重试失败任务：将 FAILED 置为 PENDING，清空错误信息。
     */
    @Transactional(rollbackFor = Exception.class)
    public void retry(Long jobId) {
        ArtJob job = get(jobId);
        job.resetForRetry();
        artJobMapper.updateById(job);
    }
}
