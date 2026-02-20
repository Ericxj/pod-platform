package com.pod.iam.sys.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.sys.domain.PlatPlatform;
import com.pod.iam.sys.mapper.PlatPlatformMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class PlatformApplicationService {

    private final PlatPlatformMapper platformMapper;

    public PlatformApplicationService(PlatPlatformMapper platformMapper) {
        this.platformMapper = platformMapper;
    }

    private long tenantId() { return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L; }
    private long factoryId() { return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L; }

    public IPage<PlatPlatform> page(Page<PlatPlatform> page, String status) {
        LambdaQueryWrapper<PlatPlatform> q = new LambdaQueryWrapper<>();
        q.eq(PlatPlatform::getTenantId, tenantId()).eq(PlatPlatform::getFactoryId, factoryId()).eq(PlatPlatform::getDeleted, 0);
        if (status != null && !status.isBlank()) q.eq(PlatPlatform::getStatus, status);
        q.orderByAsc(PlatPlatform::getPlatformCode);
        return platformMapper.selectPage(page, q);
    }

    public List<PlatPlatform> list(String status) {
        LambdaQueryWrapper<PlatPlatform> q = new LambdaQueryWrapper<>();
        q.eq(PlatPlatform::getTenantId, tenantId()).eq(PlatPlatform::getFactoryId, factoryId()).eq(PlatPlatform::getDeleted, 0);
        if (status != null && !status.isBlank()) q.eq(PlatPlatform::getStatus, status);
        q.orderByAsc(PlatPlatform::getPlatformCode);
        return platformMapper.selectList(q);
    }

    public PlatPlatform get(Long id) {
        PlatPlatform p = platformMapper.selectById(id);
        if (p == null || !Objects.equals(p.getTenantId(), tenantId()) || (p.getDeleted() != null && p.getDeleted() != 0)) {
            throw new BusinessException("Platform not found: " + id);
        }
        return p;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(PlatPlatform entity) {
        entity.setTenantId(tenantId());
        entity.setFactoryId(factoryId());
        entity.validateForCreate();
        long c = platformMapper.selectCount(new LambdaQueryWrapper<PlatPlatform>()
            .eq(PlatPlatform::getPlatformCode, entity.getPlatformCode()).eq(PlatPlatform::getDeleted, 0));
        if (c > 0) throw new BusinessException("platform_code already exists: " + entity.getPlatformCode());
        if (entity.getStatus() == null || entity.getStatus().isBlank()) entity.setStatus(PlatPlatform.STATUS_ENABLED);
        platformMapper.insert(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PlatPlatform entity) {
        PlatPlatform p = get(id);
        if (entity.getPlatformName() != null) p.setPlatformName(entity.getPlatformName());
        if (entity.getStatus() != null) p.setStatus(entity.getStatus());
        p.validateForUpdate();
        platformMapper.updateById(p);
    }

    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        PlatPlatform p = get(id);
        p.enable();
        platformMapper.updateById(p);
    }

    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        PlatPlatform p = get(id);
        p.disable();
        platformMapper.updateById(p);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PlatPlatform p = get(id);
        p.setDeleted(1);
        platformMapper.updateById(p);
    }
}
