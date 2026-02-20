package com.pod.srm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.srm.domain.Supplier;
import com.pod.srm.mapper.SupplierMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class SupplierApplicationService {

    @Autowired
    private SupplierMapper supplierMapper;

    private Long tenantId() {
        Long t = TenantContext.getTenantId();
        if (t == null) throw new BusinessException(400, "Tenant context required");
        return t;
    }

    private Long factoryId() {
        Long f = TenantContext.getFactoryId();
        if (f == null) throw new BusinessException(400, "Factory context required");
        return f;
    }

    public IPage<Supplier> page(Page<Supplier> page, String keyword, String status) {
        LambdaQueryWrapper<Supplier> q = new LambdaQueryWrapper<>();
        q.eq(Supplier::getTenantId, tenantId()).eq(Supplier::getFactoryId, factoryId()).eq(Supplier::getDeleted, 0);
        if (keyword != null && !keyword.isBlank()) {
            q.and(w -> w.like(Supplier::getSupplierCode, keyword).or().like(Supplier::getSupplierName, keyword));
        }
        if (status != null && !status.isBlank()) q.eq(Supplier::getStatus, status);
        q.orderByAsc(Supplier::getSupplierCode);
        return supplierMapper.selectPage(page, q);
    }

    public List<Supplier> list(String status) {
        LambdaQueryWrapper<Supplier> q = new LambdaQueryWrapper<>();
        q.eq(Supplier::getTenantId, tenantId()).eq(Supplier::getFactoryId, factoryId()).eq(Supplier::getDeleted, 0);
        if (status != null && !status.isBlank()) q.eq(Supplier::getStatus, status);
        q.orderByAsc(Supplier::getSupplierCode);
        return supplierMapper.selectList(q);
    }

    public Supplier get(Long id) {
        Supplier e = supplierMapper.selectById(id);
        if (e == null || !Objects.equals(e.getTenantId(), tenantId()) || !Objects.equals(e.getFactoryId(), factoryId()) || (e.getDeleted() != null && e.getDeleted() != 0)) {
            throw new BusinessException(404, "Supplier not found");
        }
        return e;
    }

    @Transactional(rollbackFor = Exception.class)
    public Supplier create(Supplier entity) {
        entity.setTenantId(tenantId());
        entity.setFactoryId(factoryId());
        entity.validateForCreate();
        long c = supplierMapper.selectCount(new LambdaQueryWrapper<Supplier>()
            .eq(Supplier::getTenantId, tenantId()).eq(Supplier::getFactoryId, factoryId())
            .eq(Supplier::getSupplierCode, entity.getSupplierCode()).eq(Supplier::getDeleted, 0));
        if (c > 0) throw new BusinessException("supplier_code already exists");
        supplierMapper.insert(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, Supplier entity) {
        Supplier s = get(id);
        if (entity.getSupplierName() != null) s.setSupplierName(entity.getSupplierName());
        if (entity.getContactName() != null) s.setContactName(entity.getContactName());
        if (entity.getPhone() != null) s.setPhone(entity.getPhone());
        if (entity.getEmail() != null) s.setEmail(entity.getEmail());
        if (entity.getAddress() != null) s.setAddress(entity.getAddress());
        if (entity.getStatus() != null) s.setStatus(entity.getStatus());
        if (entity.getRemark() != null) s.setRemark(entity.getRemark());
        s.validateForUpdate();
        supplierMapper.updateById(s);
    }

    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        Supplier s = get(id);
        s.enable();
        supplierMapper.updateById(s);
    }

    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        Supplier s = get(id);
        s.disable();
        supplierMapper.updateById(s);
    }
}
