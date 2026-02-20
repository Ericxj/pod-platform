package com.pod.iam.sys.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.sys.domain.PlatPlatform;
import com.pod.iam.sys.domain.PlatShop;
import com.pod.iam.sys.domain.PlatSite;
import com.pod.iam.sys.mapper.PlatPlatformMapper;
import com.pod.iam.sys.mapper.PlatShopMapper;
import com.pod.iam.sys.mapper.PlatSiteMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ShopApplicationService {

    private final PlatShopMapper shopMapper;
    private final PlatPlatformMapper platformMapper;
    private final PlatSiteMapper siteMapper;

    public ShopApplicationService(PlatShopMapper shopMapper, PlatPlatformMapper platformMapper, PlatSiteMapper siteMapper) {
        this.shopMapper = shopMapper;
        this.platformMapper = platformMapper;
        this.siteMapper = siteMapper;
    }

    private long tenantId() { return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L; }
    private long factoryId() { return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L; }

    public IPage<PlatShop> page(Page<PlatShop> page, String platformCode, String siteCode, String status) {
        LambdaQueryWrapper<PlatShop> q = new LambdaQueryWrapper<>();
        q.eq(PlatShop::getTenantId, tenantId()).eq(PlatShop::getFactoryId, factoryId()).eq(PlatShop::getDeleted, 0);
        if (platformCode != null && !platformCode.isBlank()) q.eq(PlatShop::getPlatformCode, platformCode);
        if (siteCode != null && !siteCode.isBlank()) q.eq(PlatShop::getSiteCode, siteCode);
        if (status != null && !status.isBlank()) q.eq(PlatShop::getStatus, status);
        q.orderByAsc(PlatShop::getPlatformCode).orderByAsc(PlatShop::getShopCode);
        return shopMapper.selectPage(page, q);
    }

    public List<PlatShop> list(String platformCode, String siteCode, String status) {
        LambdaQueryWrapper<PlatShop> q = new LambdaQueryWrapper<>();
        q.eq(PlatShop::getTenantId, tenantId()).eq(PlatShop::getFactoryId, factoryId()).eq(PlatShop::getDeleted, 0);
        if (platformCode != null && !platformCode.isBlank()) q.eq(PlatShop::getPlatformCode, platformCode);
        if (siteCode != null && !siteCode.isBlank()) q.eq(PlatShop::getSiteCode, siteCode);
        if (status != null && !status.isBlank()) q.eq(PlatShop::getStatus, status);
        q.orderByAsc(PlatShop::getPlatformCode).orderByAsc(PlatShop::getShopCode);
        return shopMapper.selectList(q);
    }

    public PlatShop get(Long id) {
        PlatShop s = shopMapper.selectById(id);
        if (s == null || !Objects.equals(s.getTenantId(), tenantId()) || (s.getDeleted() != null && s.getDeleted() != 0)) {
            throw new BusinessException("Shop not found: " + id);
        }
        return s;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(PlatShop entity) {
        entity.setTenantId(tenantId());
        entity.setFactoryId(factoryId());
        entity.validateForCreate();
        PlatPlatform plat = platformMapper.selectOne(new LambdaQueryWrapper<PlatPlatform>()
            .eq(PlatPlatform::getTenantId, tenantId()).eq(PlatPlatform::getFactoryId, factoryId())
            .eq(PlatPlatform::getPlatformCode, entity.getPlatformCode()).eq(PlatPlatform::getDeleted, 0));
        if (plat == null) throw new BusinessException("platform_code not found or not ENABLED: " + entity.getPlatformCode());
        if (!PlatPlatform.STATUS_ENABLED.equals(plat.getStatus())) throw new BusinessException("platform must be ENABLED");
        if (entity.getSiteCode() != null && !entity.getSiteCode().isBlank()) {
            PlatSite site = siteMapper.selectOne(new LambdaQueryWrapper<PlatSite>()
                .eq(PlatSite::getTenantId, tenantId()).eq(PlatSite::getPlatformCode, entity.getPlatformCode())
                .eq(PlatSite::getSiteCode, entity.getSiteCode()).eq(PlatSite::getDeleted, 0));
            if (site == null) throw new BusinessException("site_code not found: " + entity.getSiteCode());
            if (!PlatSite.STATUS_ENABLED.equals(site.getStatus())) throw new BusinessException("site must be ENABLED");
        }
        long c = shopMapper.selectCount(new LambdaQueryWrapper<PlatShop>()
            .eq(PlatShop::getTenantId, tenantId()).eq(PlatShop::getPlatformCode, entity.getPlatformCode())
            .eq(PlatShop::getShopCode, entity.getShopCode()).eq(PlatShop::getDeleted, 0));
        if (c > 0) throw new BusinessException("shop_code already exists in this platform");
        shopMapper.insert(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PlatShop entity) {
        PlatShop s = get(id);
        if (entity.getShopName() != null) s.setShopName(entity.getShopName());
        if (entity.getSiteCode() != null) s.setSiteCode(entity.getSiteCode());
        if (entity.getCurrency() != null) s.setCurrency(entity.getCurrency());
        if (entity.getStatus() != null) s.setStatus(entity.getStatus());
        if (s.getSiteCode() != null && !s.getSiteCode().isBlank()) {
            PlatSite site = siteMapper.selectOne(new LambdaQueryWrapper<PlatSite>()
                .eq(PlatSite::getTenantId, tenantId()).eq(PlatSite::getPlatformCode, s.getPlatformCode())
                .eq(PlatSite::getSiteCode, s.getSiteCode()).eq(PlatSite::getDeleted, 0));
            if (site == null) throw new BusinessException("site_code not found: " + s.getSiteCode());
            if (!PlatSite.STATUS_ENABLED.equals(site.getStatus())) throw new BusinessException("site must be ENABLED");
        }
        s.validateForUpdate();
        shopMapper.updateById(s);
    }

    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        PlatShop s = get(id);
        s.enable();
        shopMapper.updateById(s);
    }

    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        PlatShop s = get(id);
        s.disable();
        shopMapper.updateById(s);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PlatShop s = get(id);
        s.setDeleted(1);
        shopMapper.updateById(s);
    }
}
