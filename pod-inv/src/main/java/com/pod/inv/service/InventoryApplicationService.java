package com.pod.inv.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
}
