package com.pod.prd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.infra.idempotent.service.IdempotentService;
import com.pod.infra.context.RequestIdContext;
import com.pod.prd.domain.*;
import com.pod.prd.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 商品域应用服务：事务边界、租户/工厂过滤、幂等（X-Request-Id + IdempotentService）。
 */
@Service
public class PrdApplicationService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SkuBarcodeMapper skuBarcodeMapper;
    @Autowired
    private BomMapper bomMapper;
    @Autowired
    private BomItemMapper bomItemMapper;
    @Autowired
    private RoutingMapper routingMapper;
    @Autowired
    private RoutingStepMapper routingStepMapper;
    @Autowired
    private SkuMappingMapper skuMappingMapper;
    @Autowired
    private IdempotentService idempotentService;

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

    private LambdaQueryWrapper<Spu> spuWrapper() {
        LambdaQueryWrapper<Spu> q = new LambdaQueryWrapper<>();
        q.eq(Spu::getTenantId, tenantId()).eq(Spu::getFactoryId, factoryId()).eq(Spu::getDeleted, 0);
        return q;
    }

    private LambdaQueryWrapper<Sku> skuWrapper() {
        LambdaQueryWrapper<Sku> q = new LambdaQueryWrapper<>();
        q.eq(Sku::getTenantId, tenantId()).eq(Sku::getFactoryId, factoryId()).eq(Sku::getDeleted, 0);
        return q;
    }

    // ---- SPU ----
    public IPage<Spu> pageSpu(Page<Spu> page, String keyword, String status) {
        LambdaQueryWrapper<Spu> q = spuWrapper();
        if (keyword != null && !keyword.isBlank()) {
            q.and(w -> w.like(Spu::getSpuCode, keyword).or().like(Spu::getSpuName, keyword));
        }
        if (status != null && !status.isBlank()) q.eq(Spu::getStatus, status);
        q.orderByDesc(Spu::getUpdatedAt);
        return spuMapper.selectPage(page, q);
    }

    public Spu getSpu(Long id) {
        Spu e = spuMapper.selectById(id);
        if (e == null || !Objects.equals(e.getTenantId(), tenantId()) || !Objects.equals(e.getFactoryId(), factoryId()) || (e.getDeleted() != null && e.getDeleted() != 0))
            throw new BusinessException(404, "SPU not found");
        return e;
    }

    @Transactional(rollbackFor = Exception.class)
    public Spu createSpu(String spuCode, String spuName, String categoryCode, String brand) {
        String requestId = RequestIdContext.getRequired();
        idempotentService.execute(requestId, "prd:spu:create:" + spuCode, () -> {});
        LambdaQueryWrapper<Spu> q = spuWrapper().eq(Spu::getSpuCode, spuCode);
        if (spuMapper.selectCount(q) > 0) throw new BusinessException(409, "SPU code already exists");
        Spu spu = new Spu();
        spu.setSpuCode(spuCode);
        spu.setSpuName(spuName != null ? spuName : spuCode);
        spu.setCategoryCode(categoryCode);
        spu.setBrand(brand);
        spu.setStatus(Spu.STATUS_DRAFT);
        spu.setTenantId(tenantId());
        spu.setFactoryId(factoryId());
        spuMapper.insert(spu);
        return spu;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSpu(Long id, String spuName, String categoryCode, String brand) {
        Spu spu = getSpu(id);
        spu.update(spuName, categoryCode, brand);
        spuMapper.updateById(spu);
    }

    // ---- SKU ----
    public IPage<Sku> pageSku(Page<Sku> page, Long spuId, String keyword, String status) {
        LambdaQueryWrapper<Sku> q = skuWrapper();
        if (spuId != null) q.eq(Sku::getSpuId, spuId);
        if (keyword != null && !keyword.isBlank()) {
            q.and(w -> w.like(Sku::getSkuCode, keyword).or().like(Sku::getSkuName, keyword));
        }
        if (status != null && !status.isBlank()) q.eq(Sku::getStatus, status);
        q.orderByDesc(Sku::getUpdatedAt);
        return skuMapper.selectPage(page, q);
    }

    public Sku getSku(Long id) {
        Sku e = skuMapper.selectById(id);
        if (e == null || !Objects.equals(e.getTenantId(), tenantId()) || !Objects.equals(e.getFactoryId(), factoryId()) || (e.getDeleted() != null && e.getDeleted() != 0))
            throw new BusinessException(404, "SKU not found");
        return e;
    }

    public Sku getSkuByCode(String skuCode) {
        LambdaQueryWrapper<Sku> q = skuWrapper().eq(Sku::getSkuCode, skuCode);
        return skuMapper.selectOne(q);
    }

    @Transactional(rollbackFor = Exception.class)
    public Sku createSku(Long spuId, String skuCode, String skuName, BigDecimal price, Integer weightG, String attributesJson) {
        String requestId = RequestIdContext.getRequired();
        idempotentService.execute(requestId, "prd:sku:create:" + skuCode, () -> {});
        Spu spu = getSpu(spuId);
        LambdaQueryWrapper<Sku> q = skuWrapper().eq(Sku::getSkuCode, skuCode);
        if (skuMapper.selectCount(q) > 0) throw new BusinessException(409, "SKU code already exists");
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        sku.setSkuCode(skuCode);
        sku.setSkuName(skuName != null ? skuName : skuCode);
        sku.setPrice(price);
        sku.setWeightG(weightG);
        sku.setAttributesJson(attributesJson);
        sku.setStatus(Sku.STATUS_DRAFT);
        sku.setTenantId(tenantId());
        sku.setFactoryId(factoryId());
        skuMapper.insert(sku);
        return sku;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSku(Long id, String skuName, BigDecimal price, Integer weightG, String attributesJson) {
        Sku sku = getSku(id);
        sku.update(skuName, price, weightG, attributesJson);
        skuMapper.updateById(sku);
    }

    @Transactional(rollbackFor = Exception.class)
    public void activateSku(Long id) {
        Sku sku = getSku(id);
        LambdaQueryWrapper<SkuBarcode> bq = new LambdaQueryWrapper<>();
        bq.eq(SkuBarcode::getTenantId, tenantId()).eq(SkuBarcode::getFactoryId, factoryId()).eq(SkuBarcode::getDeleted, 0).eq(SkuBarcode::getSkuId, id);
        long barcodeCount = skuBarcodeMapper.selectCount(bq);
        LambdaQueryWrapper<Bom> bomQ = new LambdaQueryWrapper<>();
        bomQ.eq(Bom::getTenantId, tenantId()).eq(Bom::getFactoryId, factoryId()).eq(Bom::getDeleted, 0).eq(Bom::getSkuId, id).eq(Bom::getStatus, Bom.STATUS_PUBLISHED);
        boolean bomPublished = bomMapper.selectCount(bomQ) > 0;
        LambdaQueryWrapper<Routing> rQ = new LambdaQueryWrapper<>();
        rQ.eq(Routing::getTenantId, tenantId()).eq(Routing::getFactoryId, factoryId()).eq(Routing::getDeleted, 0).eq(Routing::getSkuId, id).eq(Routing::getStatus, Routing.STATUS_PUBLISHED);
        boolean routingPublished = routingMapper.selectCount(rQ) > 0;
        sku.activate(barcodeCount >= 1, true, true);
        skuMapper.updateById(sku);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deactivateSku(Long id) {
        Sku sku = getSku(id);
        sku.deactivate();
        skuMapper.updateById(sku);
    }

    // ---- Barcode ----
    public List<SkuBarcode> listBarcode(Long skuId) {
        LambdaQueryWrapper<SkuBarcode> q = new LambdaQueryWrapper<>();
        q.eq(SkuBarcode::getTenantId, tenantId()).eq(SkuBarcode::getFactoryId, factoryId()).eq(SkuBarcode::getDeleted, 0).eq(SkuBarcode::getSkuId, skuId);
        return skuBarcodeMapper.selectList(q);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchAddBarcode(Long skuId, List<String> barcodes, String barcodeType, Integer isPrimary) {
        getSku(skuId);
        for (String b : barcodes) {
            if (b == null || b.isBlank()) continue;
            LambdaQueryWrapper<SkuBarcode> q = new LambdaQueryWrapper<>();
            q.eq(SkuBarcode::getTenantId, tenantId()).eq(SkuBarcode::getFactoryId, factoryId()).eq(SkuBarcode::getDeleted, 0).eq(SkuBarcode::getBarcode, b.trim());
            if (skuBarcodeMapper.selectCount(q) > 0) throw new BusinessException(409, "Barcode already exists: " + b);
            SkuBarcode bar = new SkuBarcode();
            bar.setSkuId(skuId);
            bar.setBarcode(b.trim());
            bar.setBarcodeType(barcodeType != null ? barcodeType : SkuBarcode.TYPE_OTHER);
            bar.setIsPrimary(isPrimary != null && isPrimary != 0 ? 1 : 0);
            bar.setTenantId(tenantId());
            bar.setFactoryId(factoryId());
            skuBarcodeMapper.insert(bar);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBarcode(Long id) {
        SkuBarcode bar = skuBarcodeMapper.selectById(id);
        if (bar == null || !Objects.equals(bar.getTenantId(), tenantId()) || !Objects.equals(bar.getFactoryId(), factoryId()))
            throw new BusinessException(404, "Barcode not found");
        skuBarcodeMapper.deleteById(id);
    }

    // ---- BOM ----
    public Bom getBom(Long id) {
        Bom e = bomMapper.selectById(id);
        if (e == null || !Objects.equals(e.getTenantId(), tenantId()) || !Objects.equals(e.getFactoryId(), factoryId()) || (e.getDeleted() != null && e.getDeleted() != 0))
            throw new BusinessException(404, "BOM not found");
        return e;
    }

    public List<BomItem> listBomItems(Long bomId) {
        LambdaQueryWrapper<BomItem> q = new LambdaQueryWrapper<>();
        q.eq(BomItem::getTenantId, tenantId()).eq(BomItem::getFactoryId, factoryId()).eq(BomItem::getDeleted, 0).eq(BomItem::getBomId, bomId).orderByAsc(BomItem::getSortNo);
        return bomItemMapper.selectList(q);
    }

    /** 按 SKU 查询 BOM（取最新版本，无则返回 null） */
    public Bom getBomBySkuId(Long skuId) {
        LambdaQueryWrapper<Bom> q = new LambdaQueryWrapper<>();
        q.eq(Bom::getTenantId, tenantId()).eq(Bom::getFactoryId, factoryId()).eq(Bom::getDeleted, 0).eq(Bom::getSkuId, skuId).orderByDesc(Bom::getVersionNo).last("LIMIT 1");
        return bomMapper.selectOne(q);
    }

    @Transactional(rollbackFor = Exception.class)
    public Bom saveBom(Long skuId, Integer versionNo, String remark, List<BomItem> items) {
        getSku(skuId);
        LambdaQueryWrapper<Bom> q = new LambdaQueryWrapper<>();
        q.eq(Bom::getTenantId, tenantId()).eq(Bom::getFactoryId, factoryId()).eq(Bom::getDeleted, 0).eq(Bom::getSkuId, skuId);
        if (versionNo != null) q.eq(Bom::getVersionNo, versionNo);
        Bom bom = bomMapper.selectOne(q);
        if (bom == null) {
            bom = new Bom();
            bom.setSkuId(skuId);
            bom.setVersionNo(versionNo != null ? versionNo : 1);
            bom.setStatus(Bom.STATUS_DRAFT);
            bom.setRemark(remark);
            bom.setTenantId(tenantId());
            bom.setFactoryId(factoryId());
            bomMapper.insert(bom);
        } else {
            bom.setRemark(remark);
            bomMapper.updateById(bom);
        }
        LambdaQueryWrapper<BomItem> delQ = new LambdaQueryWrapper<>();
        delQ.eq(BomItem::getBomId, bom.getId());
        bomItemMapper.delete(delQ);
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                BomItem it = items.get(i);
                it.setBomId(bom.getId());
                it.setSortNo(it.getSortNo() != null ? it.getSortNo() : i + 1);
                it.setTenantId(tenantId());
                it.setFactoryId(factoryId());
                bomItemMapper.insert(it);
            }
        }
        return bom;
    }

    @Transactional(rollbackFor = Exception.class)
    public void publishBom(Long id) {
        Bom bom = getBom(id);
        List<BomItem> items = listBomItems(id);
        bom.publish(items);
        bomMapper.updateById(bom);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unpublishBom(Long id) {
        Bom bom = getBom(id);
        bom.unpublish();
        bomMapper.updateById(bom);
    }

    // ---- Routing ----
    public Routing getRouting(Long id) {
        Routing e = routingMapper.selectById(id);
        if (e == null || !Objects.equals(e.getTenantId(), tenantId()) || !Objects.equals(e.getFactoryId(), factoryId()) || (e.getDeleted() != null && e.getDeleted() != 0))
            throw new BusinessException(404, "Routing not found");
        return e;
    }

    public List<RoutingStep> listRoutingSteps(Long routingId) {
        LambdaQueryWrapper<RoutingStep> q = new LambdaQueryWrapper<>();
        q.eq(RoutingStep::getTenantId, tenantId()).eq(RoutingStep::getFactoryId, factoryId()).eq(RoutingStep::getDeleted, 0).eq(RoutingStep::getRoutingId, routingId).orderByAsc(RoutingStep::getStepNo);
        return routingStepMapper.selectList(q);
    }

    /** 按 SKU 查询工艺路线（取最新版本，无则返回 null） */
    public Routing getRoutingBySkuId(Long skuId) {
        LambdaQueryWrapper<Routing> q = new LambdaQueryWrapper<>();
        q.eq(Routing::getTenantId, tenantId()).eq(Routing::getFactoryId, factoryId()).eq(Routing::getDeleted, 0).eq(Routing::getSkuId, skuId).orderByDesc(Routing::getVersionNo).last("LIMIT 1");
        return routingMapper.selectOne(q);
    }

    @Transactional(rollbackFor = Exception.class)
    public Routing saveRouting(Long skuId, Integer versionNo, List<RoutingStep> steps) {
        getSku(skuId);
        LambdaQueryWrapper<Routing> q = new LambdaQueryWrapper<>();
        q.eq(Routing::getTenantId, tenantId()).eq(Routing::getFactoryId, factoryId()).eq(Routing::getDeleted, 0).eq(Routing::getSkuId, skuId);
        if (versionNo != null) q.eq(Routing::getVersionNo, versionNo);
        Routing routing = routingMapper.selectOne(q);
        if (routing == null) {
            routing = new Routing();
            routing.setSkuId(skuId);
            routing.setVersionNo(versionNo != null ? versionNo : 1);
            routing.setStatus(Routing.STATUS_DRAFT);
            routing.setTenantId(tenantId());
            routing.setFactoryId(factoryId());
            routingMapper.insert(routing);
        }
        LambdaQueryWrapper<RoutingStep> delQ = new LambdaQueryWrapper<>();
        delQ.eq(RoutingStep::getRoutingId, routing.getId());
        routingStepMapper.delete(delQ);
        if (steps != null) {
            for (RoutingStep s : steps) {
                s.setRoutingId(routing.getId());
                s.setTenantId(tenantId());
                s.setFactoryId(factoryId());
                routingStepMapper.insert(s);
            }
        }
        return routing;
    }

    @Transactional(rollbackFor = Exception.class)
    public void publishRouting(Long id) {
        Routing routing = getRouting(id);
        List<RoutingStep> steps = listRoutingSteps(id);
        routing.publish(steps);
        routingMapper.updateById(routing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unpublishRouting(Long id) {
        Routing routing = getRouting(id);
        routing.unpublish();
        routingMapper.updateById(routing);
    }

    // ---- SkuMapping ----
    /** OMS 拉单解析用：按渠道/店铺/外部SKU 查映射，无则返回 null */
    public SkuMapping findSkuMappingByExternal(String channel, String shopId, String externalSku) {
        if (channel == null || externalSku == null) return null;
        LambdaQueryWrapper<SkuMapping> q = new LambdaQueryWrapper<>();
        q.eq(SkuMapping::getTenantId, tenantId()).eq(SkuMapping::getFactoryId, factoryId()).eq(SkuMapping::getDeleted, 0)
          .eq(SkuMapping::getChannel, channel).eq(SkuMapping::getShopId, shopId != null ? shopId : "").eq(SkuMapping::getExternalSku, externalSku);
        return skuMappingMapper.selectOne(q);
    }

    public IPage<SkuMapping> pageSkuMapping(Page<SkuMapping> page, String channel, String shopId, String externalSku, String skuCode) {
        LambdaQueryWrapper<SkuMapping> q = new LambdaQueryWrapper<>();
        q.eq(SkuMapping::getTenantId, tenantId()).eq(SkuMapping::getFactoryId, factoryId()).eq(SkuMapping::getDeleted, 0);
        if (channel != null && !channel.isBlank()) q.eq(SkuMapping::getChannel, channel);
        if (shopId != null && !shopId.isBlank()) q.eq(SkuMapping::getShopId, shopId);
        if (externalSku != null && !externalSku.isBlank()) q.like(SkuMapping::getExternalSku, externalSku);
        if (skuCode != null && !skuCode.isBlank()) q.like(SkuMapping::getSkuCode, skuCode);
        q.orderByDesc(SkuMapping::getUpdatedAt);
        return skuMappingMapper.selectPage(page, q);
    }

    @Transactional(rollbackFor = Exception.class)
    public SkuMapping createSkuMapping(String channel, String shopId, String externalSku, String externalName, String skuCode, String remark) {
        String requestId = RequestIdContext.getRequired();
        idempotentService.execute(requestId, "prd:mapping:create:" + channel + ":" + (shopId != null ? shopId : "") + ":" + externalSku, () -> {});
        Sku sku = getSkuByCode(skuCode);
        if (sku == null) throw new BusinessException(404, "SKU not found: " + skuCode);
        if (!Sku.STATUS_ACTIVE.equals(sku.getStatus())) throw new BusinessException(400, "SKU must be ACTIVE to bind mapping");
        LambdaQueryWrapper<SkuMapping> uq = new LambdaQueryWrapper<>();
        uq.eq(SkuMapping::getTenantId, tenantId()).eq(SkuMapping::getFactoryId, factoryId()).eq(SkuMapping::getDeleted, 0)
          .eq(SkuMapping::getChannel, channel).eq(SkuMapping::getShopId, shopId != null ? shopId : "").eq(SkuMapping::getExternalSku, externalSku);
        if (skuMappingMapper.selectCount(uq) > 0) throw new BusinessException(409, "Mapping already exists for channel/shop/externalSku");
        SkuMapping m = new SkuMapping();
        m.setChannel(channel);
        m.setShopId(shopId != null ? shopId : "");
        m.setExternalSku(externalSku);
        m.setExternalName(externalName);
        m.setSkuId(sku.getId());
        m.setSkuCode(sku.getSkuCode());
        m.setRemark(remark);
        m.setTenantId(tenantId());
        m.setFactoryId(factoryId());
        skuMappingMapper.insert(m);
        return m;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSkuMapping(Long id, String externalName, String remark) {
        SkuMapping m = skuMappingMapper.selectById(id);
        if (m == null || !Objects.equals(m.getTenantId(), tenantId()) || !Objects.equals(m.getFactoryId(), factoryId()))
            throw new BusinessException(404, "Mapping not found");
        m.update(externalName, remark);
        skuMappingMapper.updateById(m);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSkuMapping(Long id) {
        SkuMapping m = skuMappingMapper.selectById(id);
        if (m == null || !Objects.equals(m.getTenantId(), tenantId()) || !Objects.equals(m.getFactoryId(), factoryId()))
            throw new BusinessException(404, "Mapping not found");
        skuMappingMapper.deleteById(id);
    }
}
