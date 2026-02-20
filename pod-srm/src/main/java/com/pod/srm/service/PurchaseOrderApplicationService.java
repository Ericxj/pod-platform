package com.pod.srm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.prd.domain.Sku;
import com.pod.prd.service.PrdApplicationService;
import com.pod.srm.domain.PurchaseOrder;
import com.pod.srm.domain.PurchaseOrderLine;
import com.pod.srm.domain.Supplier;
import com.pod.srm.mapper.PurchaseOrderLineMapper;
import com.pod.srm.mapper.PurchaseOrderMapper;
import com.pod.srm.mapper.SupplierMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PurchaseOrderApplicationService {

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;
    @Autowired
    private PurchaseOrderLineMapper purchaseOrderLineMapper;
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private PrdApplicationService prdApplicationService;

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

    public IPage<PurchaseOrder> page(Page<PurchaseOrder> page, Long supplierId, String status) {
        LambdaQueryWrapper<PurchaseOrder> q = new LambdaQueryWrapper<>();
        q.eq(PurchaseOrder::getTenantId, tenantId()).eq(PurchaseOrder::getFactoryId, factoryId()).eq(PurchaseOrder::getDeleted, 0);
        if (supplierId != null) q.eq(PurchaseOrder::getSupplierId, supplierId);
        if (status != null && !status.isBlank()) q.eq(PurchaseOrder::getStatus, status);
        q.orderByDesc(PurchaseOrder::getCreatedAt);
        return purchaseOrderMapper.selectPage(page, q);
    }

    public PurchaseOrder get(Long id) {
        PurchaseOrder po = purchaseOrderMapper.selectById(id);
        if (po == null || !Objects.equals(po.getTenantId(), tenantId()) || !Objects.equals(po.getFactoryId(), factoryId()) || (po.getDeleted() != null && po.getDeleted() != 0)) {
            throw new BusinessException(404, "Purchase order not found");
        }
        LambdaQueryWrapper<PurchaseOrderLine> lq = new LambdaQueryWrapper<>();
        lq.eq(PurchaseOrderLine::getPoId, id).eq(PurchaseOrderLine::getDeleted, 0).orderByAsc(PurchaseOrderLine::getLineNo);
        po.setLines(purchaseOrderLineMapper.selectList(lq));
        return po;
    }

    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder createDraft(Long supplierId, String currency, LocalDate expectedArriveDate) {
        Supplier sup = supplierMapper.selectById(supplierId);
        if (sup == null || !Objects.equals(sup.getTenantId(), tenantId()) || !Objects.equals(sup.getFactoryId(), factoryId()) || (sup.getDeleted() != null && sup.getDeleted() != 0)) {
            throw new BusinessException(404, "Supplier not found");
        }
        if (!Supplier.STATUS_ENABLED.equals(sup.getStatus())) {
            throw new BusinessException(400, "Supplier must be ENABLED");
        }
        String poNo = "PO" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        long ex = purchaseOrderMapper.selectCount(new LambdaQueryWrapper<PurchaseOrder>()
            .eq(PurchaseOrder::getTenantId, tenantId()).eq(PurchaseOrder::getFactoryId, factoryId()).eq(PurchaseOrder::getPoNo, poNo).eq(PurchaseOrder::getDeleted, 0));
        if (ex > 0) poNo = poNo + "-" + System.currentTimeMillis() % 10000;
        PurchaseOrder po = new PurchaseOrder();
        po.setTenantId(tenantId());
        po.setFactoryId(factoryId());
        po.setPoNo(poNo);
        po.setSupplierId(supplierId);
        po.setCurrency(currency != null && !currency.isBlank() ? currency : "CNY");
        po.setStatus(PurchaseOrder.STATUS_DRAFT);
        po.setTotalQty(BigDecimal.ZERO);
        po.setTotalAmount(BigDecimal.ZERO);
        po.setExpectedArriveDate(expectedArriveDate);
        purchaseOrderMapper.insert(po);
        return po;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addLine(Long poId, Long skuId, BigDecimal qtyOrdered, BigDecimal unitPrice) {
        PurchaseOrder po = get(poId);
        if (!PurchaseOrder.STATUS_DRAFT.equals(po.getStatus())) {
            throw new BusinessException(400, "Only DRAFT PO can add lines");
        }
        Sku sku = prdApplicationService.getSku(skuId);
        int nextLineNo = (po.getLines() == null || po.getLines().isEmpty()) ? 1 : po.getLines().stream().mapToInt(PurchaseOrderLine::getLineNo).max().orElse(0) + 1;
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setTenantId(tenantId());
        line.setFactoryId(factoryId());
        line.setPoId(poId);
        line.setLineNo(nextLineNo);
        line.setSkuId(skuId);
        line.setSkuCode(sku.getSkuCode());
        line.setSkuName(sku.getSkuName());
        line.setQtyOrdered(qtyOrdered != null ? qtyOrdered : BigDecimal.ZERO);
        line.setUnitPrice(unitPrice != null ? unitPrice : BigDecimal.ZERO);
        line.setQtyReceived(BigDecimal.ZERO);
        line.validate();
        purchaseOrderLineMapper.insert(line);
        po.getLines().add(line);
        po.recalcTotals();
        purchaseOrderMapper.updateById(po);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateLine(Long poId, Long lineId, BigDecimal qtyOrdered, BigDecimal unitPrice) {
        PurchaseOrder po = get(poId);
        if (!PurchaseOrder.STATUS_DRAFT.equals(po.getStatus())) {
            throw new BusinessException(400, "Only DRAFT PO can update lines");
        }
        PurchaseOrderLine line = purchaseOrderLineMapper.selectById(lineId);
        if (line == null || !Objects.equals(line.getPoId(), poId)) throw new BusinessException(404, "Line not found");
        if (qtyOrdered != null) line.setQtyOrdered(qtyOrdered);
        if (unitPrice != null) line.setUnitPrice(unitPrice);
        line.validate();
        purchaseOrderLineMapper.updateById(line);
        po.setLines(purchaseOrderLineMapper.selectList(new LambdaQueryWrapper<PurchaseOrderLine>().eq(PurchaseOrderLine::getPoId, poId).eq(PurchaseOrderLine::getDeleted, 0).orderByAsc(PurchaseOrderLine::getLineNo)));
        po.recalcTotals();
        purchaseOrderMapper.updateById(po);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        PurchaseOrder po = get(id);
        Supplier sup = supplierMapper.selectById(po.getSupplierId());
        boolean supplierValid = sup != null && Supplier.STATUS_ENABLED.equals(sup.getStatus()) && Objects.equals(sup.getTenantId(), tenantId());
        po.submit(supplierValid);
        po.recalcTotals();
        int n = purchaseOrderMapper.updateById(po);
        if (n == 0) throw new BusinessException(409, "PO version conflict, please refresh and retry");
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        PurchaseOrder po = get(id);
        po.approve();
        int n = purchaseOrderMapper.updateById(po);
        if (n == 0) throw new BusinessException(409, "PO version conflict, please refresh and retry");
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        PurchaseOrder po = get(id);
        po.cancel();
        int n = purchaseOrderMapper.updateById(po);
        if (n == 0) throw new BusinessException(409, "PO version conflict, please refresh and retry");
    }

    @Transactional(rollbackFor = Exception.class)
    public void close(Long id) {
        PurchaseOrder po = get(id);
        po.close();
        int n = purchaseOrderMapper.updateById(po);
        if (n == 0) throw new BusinessException(409, "PO version conflict, please refresh and retry");
    }
}
