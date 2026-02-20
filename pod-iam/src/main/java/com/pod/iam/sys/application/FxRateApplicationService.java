package com.pod.iam.sys.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.sys.domain.FxRate;
import com.pod.iam.sys.mapper.FxRateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class FxRateApplicationService {

    private final FxRateMapper fxRateMapper;

    public FxRateApplicationService(FxRateMapper fxRateMapper) {
        this.fxRateMapper = fxRateMapper;
    }

    private long tenantId() { return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L; }
    private long factoryId() { return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L; }

    public IPage<FxRate> page(Page<FxRate> page, String baseCurrency, String quoteCurrency, String status) {
        LambdaQueryWrapper<FxRate> q = new LambdaQueryWrapper<>();
        q.eq(FxRate::getTenantId, tenantId()).eq(FxRate::getFactoryId, factoryId()).eq(FxRate::getDeleted, 0);
        if (baseCurrency != null && !baseCurrency.isBlank()) q.eq(FxRate::getBaseCurrency, baseCurrency);
        if (quoteCurrency != null && !quoteCurrency.isBlank()) q.eq(FxRate::getQuoteCurrency, quoteCurrency);
        if (status != null && !status.isBlank()) q.eq(FxRate::getStatus, status);
        q.orderByDesc(FxRate::getEffectiveDate).orderByDesc(FxRate::getId);
        return fxRateMapper.selectPage(page, q);
    }

    public List<FxRate> list(String baseCurrency, String quoteCurrency, String status) {
        LambdaQueryWrapper<FxRate> q = new LambdaQueryWrapper<>();
        q.eq(FxRate::getTenantId, tenantId()).eq(FxRate::getFactoryId, factoryId()).eq(FxRate::getDeleted, 0);
        if (baseCurrency != null && !baseCurrency.isBlank()) q.eq(FxRate::getBaseCurrency, baseCurrency);
        if (quoteCurrency != null && !quoteCurrency.isBlank()) q.eq(FxRate::getQuoteCurrency, quoteCurrency);
        if (status != null && !status.isBlank()) q.eq(FxRate::getStatus, status);
        q.orderByDesc(FxRate::getEffectiveDate);
        return fxRateMapper.selectList(q);
    }

    public FxRate get(Long id) {
        FxRate r = fxRateMapper.selectById(id);
        if (r == null || !Objects.equals(r.getTenantId(), tenantId()) || (r.getDeleted() != null && r.getDeleted() != 0)) {
            throw new BusinessException("FxRate not found: " + id);
        }
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(FxRate entity) {
        entity.setTenantId(tenantId());
        entity.setFactoryId(factoryId());
        entity.validateForCreate();
        long c = fxRateMapper.selectCount(new LambdaQueryWrapper<FxRate>()
            .eq(FxRate::getTenantId, tenantId()).eq(FxRate::getBaseCurrency, entity.getBaseCurrency())
            .eq(FxRate::getQuoteCurrency, entity.getQuoteCurrency()).eq(FxRate::getEffectiveDate, entity.getEffectiveDate()).eq(FxRate::getDeleted, 0));
        if (c > 0) throw new BusinessException("Same base+quote+effective_date already exists");
        fxRateMapper.insert(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, FxRate entity) {
        FxRate r = get(id);
        if (entity.getRate() != null) r.setRate(entity.getRate());
        if (entity.getSource() != null) r.setSource(entity.getSource());
        if (entity.getStatus() != null) r.setStatus(entity.getStatus());
        r.validateForUpdate();
        fxRateMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        FxRate r = get(id);
        r.enable();
        fxRateMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        FxRate r = get(id);
        r.disable();
        fxRateMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        FxRate r = get(id);
        r.setDeleted(1);
        fxRateMapper.updateById(r);
    }

    /** 查询最近有效汇率：effective_date <= date，取一条；若无则报错 */
    public BigDecimal quote(String base, String quote, LocalDate date) {
        if (base == null || base.isBlank() || quote == null || quote.isBlank()) {
            throw new BusinessException("base and quote required");
        }
        if (date == null) date = LocalDate.now();
        LambdaQueryWrapper<FxRate> q = new LambdaQueryWrapper<>();
        q.eq(FxRate::getTenantId, tenantId()).eq(FxRate::getFactoryId, factoryId()).eq(FxRate::getDeleted, 0)
            .eq(FxRate::getBaseCurrency, base).eq(FxRate::getQuoteCurrency, quote).eq(FxRate::getStatus, FxRate.STATUS_ENABLED)
            .le(FxRate::getEffectiveDate, date).orderByDesc(FxRate::getEffectiveDate).last("LIMIT 1");
        FxRate r = fxRateMapper.selectOne(q);
        if (r == null) throw new BusinessException("No fx rate for " + base + "/" + quote + " on or before " + date);
        return r.getRate();
    }
}
