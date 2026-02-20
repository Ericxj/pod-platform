package com.pod.iam.sys.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.sys.domain.PlatSite;
import com.pod.iam.sys.domain.PlatShop;
import com.pod.iam.sys.mapper.PlatSiteMapper;
import com.pod.iam.sys.mapper.PlatShopMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class SiteApplicationService {

    private final PlatSiteMapper siteMapper;
    private final PlatShopMapper shopMapper;

    public SiteApplicationService(PlatSiteMapper siteMapper, PlatShopMapper shopMapper) {
        this.siteMapper = siteMapper;
        this.shopMapper = shopMapper;
    }

    private long tenantId() { return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L; }
    private long factoryId() { return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L; }

    public IPage<PlatSite> page(Page<PlatSite> page, String platformCode, String status) {
        LambdaQueryWrapper<PlatSite> q = new LambdaQueryWrapper<>();
        q.eq(PlatSite::getTenantId, tenantId()).eq(PlatSite::getFactoryId, factoryId()).eq(PlatSite::getDeleted, 0);
        if (platformCode != null && !platformCode.isBlank()) q.eq(PlatSite::getPlatformCode, platformCode);
        if (status != null && !status.isBlank()) q.eq(PlatSite::getStatus, status);
        q.orderByAsc(PlatSite::getPlatformCode).orderByAsc(PlatSite::getSiteCode);
        return siteMapper.selectPage(page, q);
    }

    public List<PlatSite> list(String platformCode, String status) {
        LambdaQueryWrapper<PlatSite> q = new LambdaQueryWrapper<>();
        q.eq(PlatSite::getTenantId, tenantId()).eq(PlatSite::getFactoryId, factoryId()).eq(PlatSite::getDeleted, 0);
        if (platformCode != null && !platformCode.isBlank()) q.eq(PlatSite::getPlatformCode, platformCode);
        if (status != null && !status.isBlank()) q.eq(PlatSite::getStatus, status);
        q.orderByAsc(PlatSite::getPlatformCode).orderByAsc(PlatSite::getSiteCode);
        return siteMapper.selectList(q);
    }

    public PlatSite get(Long id) {
        PlatSite s = siteMapper.selectById(id);
        if (s == null || !Objects.equals(s.getTenantId(), tenantId()) || (s.getDeleted() != null && s.getDeleted() != 0)) {
            throw new BusinessException("Site not found: " + id);
        }
        return s;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(PlatSite entity) {
        entity.setTenantId(tenantId());
        entity.setFactoryId(factoryId());
        entity.validateForCreate();
        long c = siteMapper.selectCount(new LambdaQueryWrapper<PlatSite>()
            .eq(PlatSite::getTenantId, tenantId()).eq(PlatSite::getPlatformCode, entity.getPlatformCode())
            .eq(PlatSite::getSiteCode, entity.getSiteCode()).eq(PlatSite::getDeleted, 0));
        if (c > 0) throw new BusinessException("platform_code+site_code already exists");
        siteMapper.insert(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PlatSite entity) {
        PlatSite s = get(id);
        if (entity.getSiteName() != null) s.setSiteName(entity.getSiteName());
        if (entity.getCountryCode() != null) s.setCountryCode(entity.getCountryCode());
        if (entity.getCurrency() != null) s.setCurrency(entity.getCurrency());
        if (entity.getTimezone() != null) s.setTimezone(entity.getTimezone());
        if (entity.getStatus() != null) s.setStatus(entity.getStatus());
        s.validateForUpdate();
        siteMapper.updateById(s);
    }

    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        PlatSite s = get(id);
        s.enable();
        siteMapper.updateById(s);
    }

    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        PlatSite s = get(id);
        long activeShops = shopMapper.selectCount(new LambdaQueryWrapper<PlatShop>()
            .eq(PlatShop::getTenantId, tenantId()).eq(PlatShop::getPlatformCode, s.getPlatformCode())
            .eq(PlatShop::getSiteCode, s.getSiteCode()).eq(PlatShop::getStatus, PlatShop.STATUS_ENABLED).eq(PlatShop::getDeleted, 0));
        if (activeShops > 0) throw new BusinessException("Cannot disable site: " + activeShops + " active shop(s) reference this site");
        s.disable();
        siteMapper.updateById(s);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PlatSite s = get(id);
        s.setDeleted(1);
        siteMapper.updateById(s);
    }
}
