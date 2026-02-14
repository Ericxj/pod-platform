package com.pod.iam.application;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.common.utils.TraceIdUtils;
import com.pod.iam.domain.IamTenant;
import com.pod.iam.dto.TenantPageQuery;
import com.pod.iam.mapper.IamTenantMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IamTenantApplicationService {

    private final IamTenantMapper tenantMapper;

    public IamTenantApplicationService(IamTenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    public IPage<IamTenant> page(TenantPageQuery query) {
        Page<IamTenant> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<IamTenant> w = new LambdaQueryWrapper<>();
        w.and(StrUtil.isNotBlank(query.getKeyword()), q -> q
                .like(IamTenant::getTenantCode, query.getKeyword())
                .or().like(IamTenant::getTenantName, query.getKeyword()));
        w.eq(StrUtil.isNotBlank(query.getStatus()), IamTenant::getStatus, query.getStatus());
        w.orderByDesc(IamTenant::getCreatedAt);
        return tenantMapper.selectPage(page, w);
    }

    public IamTenant get(Long id) {
        return tenantMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(IamTenant entity) {
        if (StrUtil.isBlank(entity.getTenantCode())) {
            throw new BusinessException("tenantCode is required");
        }
        long c = tenantMapper.selectCount(new LambdaQueryWrapper<IamTenant>().eq(IamTenant::getTenantCode, entity.getTenantCode()));
        if (c > 0) throw new BusinessException("tenantCode already exists");
        if (StrUtil.isBlank(entity.getTenantName())) entity.setTenantName(entity.getTenantCode());
        if (StrUtil.isBlank(entity.getStatus())) entity.setStatus("ENABLED");
        tenantMapper.insert(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, IamTenant entity) {
        IamTenant t = tenantMapper.selectById(id);
        if (t == null) throw new BusinessException("Tenant not found");
        if (StrUtil.isNotBlank(entity.getTenantName())) t.setTenantName(entity.getTenantName());
        if (StrUtil.isNotBlank(entity.getStatus())) t.setStatus(entity.getStatus());
        if (entity.getPlanType() != null) t.setPlanType(entity.getPlanType());
        if (entity.getPlanExpireAt() != null) t.setPlanExpireAt(entity.getPlanExpireAt());
        tenantMapper.updateById(t);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        IamTenant t = tenantMapper.selectById(id);
        if (t == null) throw new BusinessException("Tenant not found");
        t.setDeleted(1);
        t.setTraceId(TraceIdUtils.getTraceId());
        tenantMapper.updateById(t);
    }
}
