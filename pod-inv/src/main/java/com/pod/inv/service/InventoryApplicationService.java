package com.pod.inv.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.inv.domain.InventoryBalance;
import com.pod.inv.domain.InventoryLedger;
import com.pod.inv.domain.InventoryReservation;
import com.pod.inv.mapper.InventoryBalanceMapper;
import com.pod.inv.mapper.InventoryLedgerMapper;
import com.pod.inv.mapper.InventoryReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryApplicationService {

    @Autowired
    private InventoryBalanceMapper balanceMapper;
    @Autowired
    private InventoryReservationMapper reservationMapper;
    @Autowired
    private InventoryLedgerMapper ledgerMapper;

    /**
     * Reserve inventory for a business operation (e.g. Fulfillment)
     * Idempotent based on bizType + bizNo + skuId
     */
    @Transactional(rollbackFor = Exception.class)
    public void reserve(String bizType, String bizNo, Long warehouseId, Long skuId, int qty) {
        // 1. Idempotency Check (Domain Level)
        LambdaQueryWrapper<InventoryReservation> query = new LambdaQueryWrapper<>();
        query.eq(InventoryReservation::getBizType, bizType)
             .eq(InventoryReservation::getBizNo, bizNo)
             .eq(InventoryReservation::getSkuId, skuId);
        
        if (reservationMapper.exists(query)) {
            // Already reserved, assume success (idempotent)
            return;
        }

        // 2. Load Balance
        LambdaQueryWrapper<InventoryBalance> balanceQuery = new LambdaQueryWrapper<>();
        balanceQuery.eq(InventoryBalance::getWarehouseId, warehouseId)
                    .eq(InventoryBalance::getSkuId, skuId);
        InventoryBalance balance = balanceMapper.selectOne(balanceQuery);

        if (balance == null) {
            throw new BusinessException("Inventory balance not found for SKU: " + skuId);
        }

        // 3. Domain Logic (In-Memory)
        Integer oldAllocated = balance.getAllocatedQty();
        Integer oldAvailable = balance.getAvailableQty();
        balance.reserve(qty); // updates allocated & available

        // 4. Optimistic Lock Update
        int rows = balanceMapper.updateBalanceWithVersion(
                balance.getId(), 
                balance.getAllocatedQty(), 
                balance.getAvailableQty(), 
                balance.getVersion(),
                TenantContext.getTenantId(),
                TenantContext.getFactoryId()
        );

        if (rows == 0) {
            throw new BusinessException("Inventory concurrency conflict. Please retry.");
        }

        // 5. Create Reservation Record
        InventoryReservation res = InventoryReservation.create(bizType, bizNo, warehouseId, skuId, qty);
        reservationMapper.insert(res);

        // 6. Create Ledger Record
        InventoryLedger ledger = new InventoryLedger();
        ledger.setWarehouseId(warehouseId);
        ledger.setLocationId(balance.getLocationId());
        ledger.setSkuId(skuId);
        ledger.setTxnNo("TXN" + System.currentTimeMillis() + (int)(Math.random() * 1000)); // Simple generation for demo
        ledger.setTxnType("RESERVE");
        ledger.setBizType(bizType);
        ledger.setBizNo(bizNo);
        ledger.setDeltaQty(qty); // Reserve is positive delta for allocated, but maybe negative for available? DDL says "正入负出" (positive in, negative out). For reserve, it's not in/out of on_hand, but change of state. Let's keep it positive as it refers to the "change amount".
        ledger.setBeforeOnHand(balance.getOnHandQty());
        ledger.setAfterOnHand(balance.getOnHandQty()); // OnHand doesn't change for reservation
        ledger.setBeforeAllocated(oldAllocated);
        ledger.setAfterAllocated(balance.getAllocatedQty());
        ledger.setRemark("Reserve for " + bizNo);
        ledgerMapper.insert(ledger);
    }

    public IPage<InventoryBalance> pageBalances(Page<InventoryBalance> page, Long warehouseId, Long skuId) {
        LambdaQueryWrapper<InventoryBalance> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) wrapper.eq(InventoryBalance::getWarehouseId, warehouseId);
        if (skuId != null) wrapper.eq(InventoryBalance::getSkuId, skuId);
        return balanceMapper.selectPage(page, wrapper);
    }

    public IPage<InventoryReservation> pageReservations(Page<InventoryReservation> page, String bizNo, Long skuId) {
        LambdaQueryWrapper<InventoryReservation> wrapper = new LambdaQueryWrapper<>();
        if (bizNo != null) wrapper.eq(InventoryReservation::getBizNo, bizNo);
        if (skuId != null) wrapper.eq(InventoryReservation::getSkuId, skuId);
        return reservationMapper.selectPage(page, wrapper);
    }
    
    public IPage<InventoryLedger> pageLedgers(Page<InventoryLedger> page, Long skuId) {
         LambdaQueryWrapper<InventoryLedger> wrapper = new LambdaQueryWrapper<>();
         if (skuId != null) wrapper.eq(InventoryLedger::getSkuId, skuId);
         wrapper.orderByDesc(InventoryLedger::getCreatedAt);
         return ledgerMapper.selectPage(page, wrapper);
    }

    /**
     * 释放指定业务单号下的全部预占（bizType+bizNo）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void releaseByBiz(String bizType, String bizNo) {
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        LambdaQueryWrapper<InventoryReservation> q = new LambdaQueryWrapper<>();
        q.eq(InventoryReservation::getBizType, bizType).eq(InventoryReservation::getBizNo, bizNo)
         .eq(InventoryReservation::getTenantId, tenantId).eq(InventoryReservation::getFactoryId, factoryId)
         .eq(InventoryReservation::getDeleted, 0).eq(InventoryReservation::getStatus, "RESERVED");
        List<InventoryReservation> list = reservationMapper.selectList(q);
        for (InventoryReservation res : list) {
            LambdaQueryWrapper<InventoryBalance> bq = new LambdaQueryWrapper<>();
            bq.eq(InventoryBalance::getWarehouseId, res.getWarehouseId()).eq(InventoryBalance::getSkuId, res.getSkuId())
              .eq(InventoryBalance::getTenantId, tenantId).eq(InventoryBalance::getFactoryId, factoryId).eq(InventoryBalance::getDeleted, 0);
            InventoryBalance balance = balanceMapper.selectOne(bq);
            if (balance == null) continue;
            int qty = res.getQty() != null ? res.getQty() : 0;
            if (qty <= 0) continue;
            balance.release(qty);
            int rows = balanceMapper.updateBalanceWithVersion(balance.getId(), balance.getAllocatedQty(), balance.getAvailableQty(), balance.getVersion(), tenantId, factoryId);
            if (rows == 0) throw new BusinessException("Inventory concurrency conflict on release. Please retry.");
            res.release();
            reservationMapper.updateById(res);
        }
    }

    /**
     * P1.4 完工入库：按业务单号+SKU 增加在库数量，幂等（同一 bizType+bizNo+skuId 仅入一次）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void produceIn(String bizType, String bizNo, Long warehouseId, Long skuId, int qty) {
        if (qty <= 0) return;
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        LambdaQueryWrapper<InventoryLedger> idem = new LambdaQueryWrapper<>();
        idem.eq(InventoryLedger::getBizType, bizType).eq(InventoryLedger::getBizNo, bizNo)
            .eq(InventoryLedger::getSkuId, skuId).eq(InventoryLedger::getTxnType, "PRODUCE_IN")
            .eq(InventoryLedger::getTenantId, tenantId).eq(InventoryLedger::getFactoryId, factoryId)
            .eq(InventoryLedger::getDeleted, 0);
        if (ledgerMapper.exists(idem)) return;

        LambdaQueryWrapper<InventoryBalance> bq = new LambdaQueryWrapper<>();
        bq.eq(InventoryBalance::getWarehouseId, warehouseId).eq(InventoryBalance::getSkuId, skuId)
          .eq(InventoryBalance::getTenantId, tenantId).eq(InventoryBalance::getFactoryId, factoryId)
          .eq(InventoryBalance::getDeleted, 0);
        InventoryBalance balance = balanceMapper.selectOne(bq);
        if (balance == null) throw new BusinessException("Inventory balance not found for warehouse/sku: " + warehouseId + "/" + skuId);

        int beforeOnHand = balance.getOnHandQty() != null ? balance.getOnHandQty() : 0;
        balance.produceIn(qty);
        int rows = balanceMapper.updateOnHandWithVersion(
                balance.getId(), balance.getOnHandQty(), balance.getAvailableQty(), balance.getVersion(),
                tenantId, factoryId);
        if (rows == 0) throw new BusinessException("Inventory concurrency conflict on produce-in. Please retry.");

        InventoryLedger ledger = new InventoryLedger();
        ledger.setWarehouseId(warehouseId);
        ledger.setLocationId(balance.getLocationId());
        ledger.setSkuId(skuId);
        ledger.setTxnNo("PIN-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000));
        ledger.setTxnType("PRODUCE_IN");
        ledger.setBizType(bizType);
        ledger.setBizNo(bizNo);
        ledger.setDeltaQty(qty);
        ledger.setBeforeOnHand(beforeOnHand);
        ledger.setAfterOnHand(balance.getOnHandQty());
        ledger.setBeforeAllocated(balance.getAllocatedQty());
        ledger.setAfterAllocated(balance.getAllocatedQty());
        ledger.setRemark("MES produce-in " + bizNo);
        ledgerMapper.insert(ledger);
    }

    /**
     * P1.5 出库扣减：按业务单号消耗预占并扣减在库，幂等（同一 bizType+bizNo 仅扣一次）。
     * 释放/结转预占：将 RESERVED 置为 CONSUMED，扣减 balance.on_hand 与 allocated。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deductByBiz(String bizType, String bizNo) {
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        LambdaQueryWrapper<InventoryLedger> idem = new LambdaQueryWrapper<>();
        idem.eq(InventoryLedger::getBizType, bizType).eq(InventoryLedger::getBizNo, bizNo)
            .eq(InventoryLedger::getTxnType, "WMS_SHIP")
            .eq(InventoryLedger::getTenantId, tenantId).eq(InventoryLedger::getFactoryId, factoryId)
            .eq(InventoryLedger::getDeleted, 0);
        if (ledgerMapper.exists(idem)) return;

        LambdaQueryWrapper<InventoryReservation> q = new LambdaQueryWrapper<>();
        q.eq(InventoryReservation::getBizType, bizType).eq(InventoryReservation::getBizNo, bizNo)
         .eq(InventoryReservation::getTenantId, tenantId).eq(InventoryReservation::getFactoryId, factoryId)
         .eq(InventoryReservation::getDeleted, 0).eq(InventoryReservation::getStatus, "RESERVED");
        List<InventoryReservation> list = reservationMapper.selectList(q);
        for (InventoryReservation res : list) {
            int qty = res.getQty() != null ? res.getQty() : 0;
            if (qty <= 0) continue;
            LambdaQueryWrapper<InventoryBalance> bq = new LambdaQueryWrapper<>();
            bq.eq(InventoryBalance::getWarehouseId, res.getWarehouseId()).eq(InventoryBalance::getSkuId, res.getSkuId())
              .eq(InventoryBalance::getTenantId, tenantId).eq(InventoryBalance::getFactoryId, factoryId).eq(InventoryBalance::getDeleted, 0);
            InventoryBalance balance = balanceMapper.selectOne(bq);
            if (balance == null) throw new BusinessException("Inventory balance not found for warehouse/sku: " + res.getWarehouseId() + "/" + res.getSkuId());
            int beforeOnHand = balance.getOnHandQty() != null ? balance.getOnHandQty() : 0;
            int beforeAllocated = balance.getAllocatedQty() != null ? balance.getAllocatedQty() : 0;
            balance.deduct(qty);
            int rows = balanceMapper.update(null, new LambdaUpdateWrapper<InventoryBalance>()
                .eq(InventoryBalance::getId, balance.getId()).eq(InventoryBalance::getVersion, balance.getVersion())
                .set(InventoryBalance::getOnHandQty, balance.getOnHandQty())
                .set(InventoryBalance::getAllocatedQty, balance.getAllocatedQty())
                .set(InventoryBalance::getAvailableQty, balance.getAvailableQty())
                .setSql("version = version + 1"));
            if (rows == 0) throw new BusinessException("Inventory concurrency conflict on deduct. Please retry.");
            res.consume();
            reservationMapper.updateById(res);
            InventoryLedger ledger = new InventoryLedger();
            ledger.setWarehouseId(res.getWarehouseId());
            ledger.setSkuId(res.getSkuId());
            ledger.setTxnNo("WMS-SHIP-" + System.currentTimeMillis() + "-" + res.getSkuId());
            ledger.setTxnType("WMS_SHIP");
            ledger.setBizType(bizType);
            ledger.setBizNo(bizNo);
            ledger.setDeltaQty(-qty);
            ledger.setBeforeOnHand(beforeOnHand);
            ledger.setAfterOnHand(balance.getOnHandQty());
            ledger.setBeforeAllocated(beforeAllocated);
            ledger.setAfterAllocated(balance.getAllocatedQty());
            ledger.setRemark("WMS ship " + bizNo);
            ledgerMapper.insert(ledger);
        }
    }
}
