package com.pod.iam.application;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.common.utils.TraceIdUtils;
import com.pod.iam.domain.IamFactory;
import com.pod.iam.dto.FactoryAllItemDto;
import com.pod.iam.dto.FactoryPageQuery;
import com.pod.iam.mapper.IamFactoryMapper;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IamFactoryApplicationService {

    private final IamFactoryMapper factoryMapper;

    public IamFactoryApplicationService(IamFactoryMapper factoryMapper) {
        this.factoryMapper = factoryMapper;
    }

    public IPage<IamFactory> page(FactoryPageQuery query) {
        Page<IamFactory> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<IamFactory> w = new LambdaQueryWrapper<>();
        w.eq(query.getTenantId() != null, IamFactory::getTenantId, query.getTenantId());
        w.and(StrUtil.isNotBlank(query.getKeyword()), q -> q
                .like(IamFactory::getFactoryCode, query.getKeyword())
                .or().like(IamFactory::getFactoryName, query.getKeyword()));
        w.eq(StrUtil.isNotBlank(query.getStatus()), IamFactory::getStatus, query.getStatus());
        w.orderByDesc(IamFactory::getCreatedAt);
        return factoryMapper.selectPage(page, w);
    }

    public IamFactory get(Long id) {
        return factoryMapper.selectById(id);
    }

    /** 当前租户下全部 ENABLED 工厂，用于下拉等；返回 DTO 不暴露通用字段。 */
    public List<FactoryAllItemDto> listAllEnabled(Long tenantId) {
        if (tenantId == null) return List.of();
        LambdaQueryWrapper<IamFactory> w = new LambdaQueryWrapper<>();
        w.eq(IamFactory::getTenantId, tenantId);
        w.eq(IamFactory::getStatus, "ENABLED");
        w.orderByAsc(IamFactory::getId);
        List<IamFactory> list = factoryMapper.selectList(w);
        return list.stream().map(f -> {
            FactoryAllItemDto dto = new FactoryAllItemDto();
            dto.setId(f.getId());
            dto.setFactoryCode(f.getFactoryCode());
            dto.setFactoryName(f.getFactoryName());
            dto.setStatus(f.getStatus());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(IamFactory entity) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new BusinessException("Tenant context required");
        if (StrUtil.isBlank(entity.getFactoryCode())) throw new BusinessException("factoryCode is required");
        long c = factoryMapper.selectCount(new LambdaQueryWrapper<IamFactory>()
                .eq(IamFactory::getTenantId, tenantId)
                .eq(IamFactory::getFactoryCode, entity.getFactoryCode()));
        if (c > 0) throw new BusinessException("factoryCode already exists in tenant");
        if (StrUtil.isBlank(entity.getFactoryName())) entity.setFactoryName(entity.getFactoryCode());
        if (StrUtil.isBlank(entity.getStatus())) entity.setStatus("ENABLED");
        entity.setTenantId(tenantId);
        factoryMapper.insert(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, IamFactory entity) {
        IamFactory f = factoryMapper.selectById(id);
        if (f == null) throw new BusinessException("Factory not found");
        if (StrUtil.isNotBlank(entity.getFactoryName())) f.setFactoryName(entity.getFactoryName());
        if (StrUtil.isNotBlank(entity.getStatus())) f.setStatus(entity.getStatus());
        if (entity.getCountryCode() != null) f.setCountryCode(entity.getCountryCode());
        if (entity.getProvince() != null) f.setProvince(entity.getProvince());
        if (entity.getCity() != null) f.setCity(entity.getCity());
        if (entity.getAddress() != null) f.setAddress(entity.getAddress());
        if (entity.getContactName() != null) f.setContactName(entity.getContactName());
        if (entity.getContactPhone() != null) f.setContactPhone(entity.getContactPhone());
        factoryMapper.updateById(f);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        IamFactory f = factoryMapper.selectById(id);
        if (f == null) throw new BusinessException("Factory not found");
        f.setDeleted(1);
        f.setTraceId(TraceIdUtils.getTraceId());
        factoryMapper.updateById(f);
    }
}
