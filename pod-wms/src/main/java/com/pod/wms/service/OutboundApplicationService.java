package com.pod.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.common.utils.TraceIdUtils;
import com.pod.infra.context.RequestIdContext;
import com.pod.infra.idempotent.service.IdempotentService;
import com.pod.inv.service.InventoryApplicationService;
import com.pod.oms.domain.Fulfillment;
import com.pod.oms.domain.FulfillmentItem;
import com.pod.oms.domain.FulfillmentStatus;
import com.pod.oms.mapper.FulfillmentMapper;
import com.pod.oms.mapper.FulfillmentItemMapper;
import org.slf4j.MDC;
import com.pod.wms.domain.*;
import com.pod.wms.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class OutboundApplicationService {

    private static final String SOURCE_TYPE_FULFILLMENT = "FULFILLMENT";

    @Autowired
    private OutboundOrderMapper outboundOrderMapper;
    @Autowired
    private OutboundOrderLineMapper outboundOrderLineMapper;
    @Autowired
    private PickWaveMapper pickWaveMapper;
    @Autowired
    private PickTaskMapper pickTaskMapper;
    @Autowired
    private PickTaskLineMapper pickTaskLineMapper;
    @Autowired
    private PackOrderMapper packOrderMapper;
    @Autowired
    private PackOrderLineMapper packOrderLineMapper;
    @Autowired
    private WmsShipmentMapper wmsShipmentMapper;
    @Autowired
    private FulfillmentMapper fulfillmentMapper;
    @Autowired
    private FulfillmentItemMapper fulfillmentItemMapper;
    @Autowired
    private InventoryApplicationService inventoryApplicationService;
    @Autowired
    private IdempotentService idempotentService;

    private long tenantId() { return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L; }
    private long factoryId() { return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L; }

    /** P1.5: 从 Fulfillment(READY_TO_SHIP) 创建出库单，幂等 uk_src */
    @Transactional(rollbackFor = Exception.class)
    public Long createFromFulfillment(Long fulfillmentId) {
        String requestId = RequestIdContext.get();
        if (requestId == null || requestId.isBlank()) requestId = "wms-ob-" + fulfillmentId + "-" + System.currentTimeMillis();
        return idempotentService.execute(requestId, "createOutboundFromFulfillment:" + fulfillmentId, () -> {
            Fulfillment f = fulfillmentMapper.selectById(fulfillmentId);
            if (f == null || !Objects.equals(f.getTenantId(), tenantId()) || !Objects.equals(f.getFactoryId(), factoryId()) || (f.getDeleted() != null && f.getDeleted() != 0))
                throw new BusinessException("Fulfillment not found: " + fulfillmentId);
            if (!FulfillmentStatus.READY_TO_SHIP.name().equals(f.getStatus()))
                throw new BusinessException("Fulfillment must be READY_TO_SHIP. Current: " + f.getStatus());
            String sourceNo = f.getFulfillmentNo();
            OutboundOrder existing = outboundOrderMapper.selectOne(new LambdaQueryWrapper<OutboundOrder>()
                .eq(OutboundOrder::getTenantId, tenantId()).eq(OutboundOrder::getFactoryId, factoryId())
                .eq(OutboundOrder::getSourceType, SOURCE_TYPE_FULFILLMENT).eq(OutboundOrder::getSourceNo, sourceNo).eq(OutboundOrder::getDeleted, 0));
            if (existing != null) return existing.getId();

            List<FulfillmentItem> items = fulfillmentItemMapper.selectList(
                new LambdaQueryWrapper<FulfillmentItem>().eq(FulfillmentItem::getFulfillmentId, fulfillmentId).eq(FulfillmentItem::getDeleted, 0));
            if (items == null || items.isEmpty()) throw new BusinessException("Fulfillment has no lines: " + fulfillmentId);

            String outboundNo = "OB-" + sourceNo;
            OutboundOrder ob = new OutboundOrder();
            ob.setOutboundNo(outboundNo);
            ob.setOutboundType(SOURCE_TYPE_FULFILLMENT);
            ob.setSourceType(SOURCE_TYPE_FULFILLMENT);
            ob.setSourceNo(sourceNo);
            ob.setFulfillmentId(fulfillmentId);
            ob.setWarehouseId(f.getWarehouseId() != null ? f.getWarehouseId() : 300001L);
            ob.setStatus(OutboundOrder.STATUS_CREATED);
            ob.setTraceId(TraceIdUtils.getTraceId());
            outboundOrderMapper.insert(ob);

            int lineNo = 1;
            for (FulfillmentItem item : items) {
                int qty = item.getReservedQty() != null && item.getReservedQty() > 0 ? item.getReservedQty() : (item.getQty() != null ? item.getQty() : 0);
                if (qty <= 0) continue;
                OutboundOrderLine line = new OutboundOrderLine();
                line.setOutboundId(ob.getId());
                line.setLineNo(lineNo++);
                line.setSkuId(item.getSkuId());
                line.setQty(qty);
                line.setQtyPicked(0);
                line.setPackedQty(0);
                line.setQtyShipped(0);
                outboundOrderLineMapper.insert(line);
            }
            return ob.getId();
        });
    }

    /** P1.5: 开始拣货 CREATED -> PICKING */
    @Transactional(rollbackFor = Exception.class)
    public void startPicking(Long outboundId) {
        OutboundOrder ob = getOutbound(outboundId);
        if (!OutboundOrder.STATUS_CREATED.equals(ob.getStatus()))
            throw new BusinessException("Outbound must be CREATED to start picking. Current: " + ob.getStatus());
        ob.startPicking();
        int rows = outboundOrderMapper.update(null, new LambdaUpdateWrapper<OutboundOrder>()
            .eq(OutboundOrder::getId, outboundId).eq(OutboundOrder::getStatus, OutboundOrder.STATUS_CREATED)
            .eq(OutboundOrder::getVersion, ob.getVersion()).set(OutboundOrder::getStatus, OutboundOrder.STATUS_PICKING).setSql("version = version + 1"));
        if (rows == 0) throw new BusinessException("Concurrent update or invalid status");
    }

    /** P1.5: 确认拣货，按行累加 picked_qty。lines: list of {lineId, pickedQty} */
    @Transactional(rollbackFor = Exception.class)
    public void confirmPicking(Long outboundId, List<PickingLineDto> lines) {
        OutboundOrder ob = getOutbound(outboundId);
        if (!OutboundOrder.STATUS_PICKING.equals(ob.getStatus()) && !OutboundOrder.STATUS_CREATED.equals(ob.getStatus()))
            throw new BusinessException("Outbound must be PICKING to confirm. Current: " + ob.getStatus());
        for (PickingLineDto dto : lines) {
            if (dto.getLineId() == null || dto.getPickedQty() == null) continue;
            OutboundOrderLine line = outboundOrderLineMapper.selectById(dto.getLineId());
            if (line == null || !Objects.equals(line.getOutboundId(), outboundId)) continue;
            int add = dto.getPickedQty();
            if (add <= 0) continue;
            int newPicked = (line.getQtyPicked() != null ? line.getQtyPicked() : 0) + add;
            if (line.getQty() != null && newPicked > line.getQty()) newPicked = line.getQty();
            outboundOrderLineMapper.update(null, new LambdaUpdateWrapper<OutboundOrderLine>()
                .eq(OutboundOrderLine::getId, dto.getLineId()).set(OutboundOrderLine::getQtyPicked, newPicked).setSql("version = version + 1"));
        }
        if (!OutboundOrder.STATUS_PICKING.equals(ob.getStatus())) {
            outboundOrderMapper.update(null, new LambdaUpdateWrapper<OutboundOrder>()
                .eq(OutboundOrder::getId, outboundId).set(OutboundOrder::getStatus, OutboundOrder.STATUS_PICKING).setSql("version = version + 1"));
        }
    }

    /** P1.5: 打包，生成 wms_pack_order + lines，出库单 -> PACKED */
    @Transactional(rollbackFor = Exception.class)
    public void pack(Long outboundId) {
        OutboundOrder ob = getOutbound(outboundId);
        if (!OutboundOrder.STATUS_PICKING.equals(ob.getStatus()) && !OutboundOrder.STATUS_PICKED.equals(ob.getStatus()))
            throw new BusinessException("Outbound must be PICKING or PICKED to pack. Current: " + ob.getStatus());
        List<OutboundOrderLine> lines = outboundOrderLineMapper.selectList(new LambdaQueryWrapper<OutboundOrderLine>().eq(OutboundOrderLine::getOutboundId, outboundId).eq(OutboundOrderLine::getDeleted, 0));
        PackOrder packOrder = new PackOrder();
        packOrder.setPackNo("PK-" + ob.getOutboundNo() + "-" + System.currentTimeMillis());
        packOrder.setOutboundId(ob.getId());
        packOrder.setStatus("PACKED");
        packOrder.setPackageCount(1);
        packOrder.setTraceId(TraceIdUtils.getTraceId());
        packOrderMapper.insert(packOrder);
        int lineNo = 1;
        for (OutboundOrderLine line : lines) {
            int qty = line.getQtyPicked() != null && line.getQtyPicked() > 0 ? line.getQtyPicked() : (line.getQty() != null ? line.getQty() : 0);
            if (qty <= 0) continue;
            PackOrderLine pl = new PackOrderLine();
            pl.setPackId(packOrder.getId());
            pl.setLineNo(lineNo++);
            pl.setSkuId(line.getSkuId());
            pl.setQty(qty);
            pl.setPackageNo(1);
            packOrderLineMapper.insert(pl);
            outboundOrderLineMapper.update(null, new LambdaUpdateWrapper<OutboundOrderLine>()
                .eq(OutboundOrderLine::getId, line.getId()).set(OutboundOrderLine::getPackedQty, qty).setSql("version = version + 1"));
        }
        ob.completePacking();
        outboundOrderMapper.update(null, new LambdaUpdateWrapper<OutboundOrder>()
            .eq(OutboundOrder::getId, outboundId).eq(OutboundOrder::getVersion, ob.getVersion())
            .set(OutboundOrder::getStatus, OutboundOrder.STATUS_PACKED).setSql("version = version + 1"));
    }

    /** P1.5: 发货 — INV 扣减(FULFILLMENT, sourceNo)、写 wms_shipment、出库单 SHIPPED、Fulfillment SHIPPED */
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long outboundId, String carrierCode, String trackingNo) {
        OutboundOrder ob = getOutbound(outboundId);
        if (!OutboundOrder.STATUS_PACKED.equals(ob.getStatus()))
            throw new BusinessException("Outbound must be PACKED to ship. Current: " + ob.getStatus());
        String sourceNo = ob.getSourceNo();
        if (sourceNo == null) sourceNo = ob.getOutboundNo();
        inventoryApplicationService.deductByBiz("FULFILLMENT", sourceNo);

        WmsShipment ship = new WmsShipment();
        ship.setOutboundId(ob.getId());
        ship.setOutboundNo(ob.getOutboundNo());
        ship.setCarrierCode(carrierCode);
        ship.setTrackingNo(trackingNo);
        ship.setShippedAt(LocalDateTime.now());
        wmsShipmentMapper.insert(ship);

        ob.ship();
        outboundOrderMapper.update(null, new LambdaUpdateWrapper<OutboundOrder>()
            .eq(OutboundOrder::getId, outboundId).eq(OutboundOrder::getVersion, ob.getVersion())
            .set(OutboundOrder::getStatus, OutboundOrder.STATUS_SHIPPED).setSql("version = version + 1"));

        if (ob.getFulfillmentId() != null) {
            Fulfillment f = fulfillmentMapper.selectById(ob.getFulfillmentId());
            if (f != null && FulfillmentStatus.READY_TO_SHIP.name().equals(f.getStatus())) {
                f.markShipped();
                fulfillmentMapper.updateStatusWithLock(f.getId(), f.getStatus(), FulfillmentStatus.READY_TO_SHIP.name(), f.getVersion(), TenantContext.getUserId());
            }
        }
    }

    /** P1.5: 取消 — 释放预占、出库单 CANCELED */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long outboundId) {
        OutboundOrder ob = getOutbound(outboundId);
        if (OutboundOrder.STATUS_SHIPPED.equals(ob.getStatus()))
            throw new BusinessException("Cannot cancel SHIPPED outbound");
        String sourceNo = ob.getSourceNo();
        if (sourceNo != null)
            inventoryApplicationService.releaseByBiz("FULFILLMENT", sourceNo);
        ob.cancel();
        outboundOrderMapper.update(null, new LambdaUpdateWrapper<OutboundOrder>()
            .eq(OutboundOrder::getId, outboundId).in(OutboundOrder::getStatus, OutboundOrder.STATUS_CREATED, OutboundOrder.STATUS_PICKING, OutboundOrder.STATUS_PICKED, OutboundOrder.STATUS_PACKED)
            .eq(OutboundOrder::getVersion, ob.getVersion()).set(OutboundOrder::getStatus, OutboundOrder.STATUS_CANCELLED).setSql("version = version + 1"));
    }

    public OutboundOrder getOutbound(Long id) {
        OutboundOrder ob = outboundOrderMapper.selectById(id);
        if (ob == null || !Objects.equals(ob.getTenantId(), tenantId()) || !Objects.equals(ob.getFactoryId(), factoryId()) || (ob.getDeleted() != null && ob.getDeleted() != 0))
            throw new BusinessException("Outbound not found: " + id);
        return ob;
    }

    public IPage<OutboundOrder> page(Page<OutboundOrder> page, String status) {
        LambdaQueryWrapper<OutboundOrder> q = new LambdaQueryWrapper<>();
        q.eq(OutboundOrder::getTenantId, tenantId()).eq(OutboundOrder::getFactoryId, factoryId()).eq(OutboundOrder::getDeleted, 0);
        if (status != null && !status.isBlank()) q.eq(OutboundOrder::getStatus, status);
        q.orderByDesc(OutboundOrder::getId);
        return outboundOrderMapper.selectPage(page, q);
    }

    public static class PickingLineDto {
        private Long lineId;
        private Integer pickedQty;
        public Long getLineId() { return lineId; }
        public void setLineId(Long lineId) { this.lineId = lineId; }
        public Integer getPickedQty() { return pickedQty; }
        public void setPickedQty(Integer pickedQty) { this.pickedQty = pickedQty; }
    }

    @Transactional(rollbackFor = Exception.class)
    public void pullOrdersFromOmsMock() {
        // Mock 1-3 orders
        int count = (int) (Math.random() * 3) + 1;
        for (int i = 0; i < count; i++) {
            // Generate ID. 50% chance to generate a "recent" ID to trigger deduplication logic if we stored history?
            // For now just random.
            String omsOrderNo = "OMS_" + System.currentTimeMillis() + "_" + i;
            
            OutboundOrderLine line = new OutboundOrderLine();
            line.setSkuId(1001L);
            // line.setItemCode("SKU_001");
            line.setQty(10);
            
            try {
                MDC.put(RequestIdContext.MDC_KEY, "JOB_PULL_" + omsOrderNo);
                try {
                    createOutboundOrder(omsOrderNo, 101L, List.of(line));
                } finally {
                    MDC.remove(RequestIdContext.MDC_KEY);
                }
            } catch (BusinessException e) {
                // Log duplicate and continue
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String createOutboundOrder(String fulfillmentNo, Long warehouseId, List<OutboundOrderLine> lines) {
        String requestId = RequestIdContext.getRequired();
        return idempotentService.execute(requestId, "createOutboundOrder:" + fulfillmentNo, () -> {
            OutboundOrder outboundOrder = new OutboundOrder();
            outboundOrder.setOutboundNo("OB" + System.currentTimeMillis()); // Simple ID generation
            outboundOrder.setOutboundType("FULFILLMENT");
            outboundOrder.setSourceNo(fulfillmentNo);
            outboundOrder.setWarehouseId(warehouseId);
            outboundOrder.setStatus(OutboundOrder.STATUS_CREATED);
            outboundOrder.setTraceId(TraceIdUtils.getTraceId());
            outboundOrderMapper.insert(outboundOrder);

            for (OutboundOrderLine line : lines) {
                line.setOutboundId(outboundOrder.getId());
                line.setQtyPicked(0);
                line.setQtyShipped(0);
                outboundOrderLineMapper.insert(line);
            }
            return outboundOrder.getOutboundNo();
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public String createPickTask(String outboundNo) {
        String requestId = RequestIdContext.getRequired();
        return idempotentService.execute(requestId, "createPickTask:" + outboundNo, () -> {
            // Find outbound order
            OutboundOrder outboundOrder = outboundOrderMapper.selectOne(new QueryWrapper<OutboundOrder>().eq("outbound_no", outboundNo));
            if (outboundOrder == null) {
                throw new BusinessException("Outbound order not found: " + outboundNo);
            }

            // Idempotency: Check if active pick task exists
            PickTask existingTask = pickTaskMapper.selectOne(new QueryWrapper<PickTask>()
                .eq("outbound_id", outboundOrder.getId())
                .ne("status", PickTask.STATUS_CANCELLED));
            if (existingTask != null) {
                return existingTask.getPickTaskNo();
            }

            // Domain Logic: Allocate Outbound
            String oldStatus = outboundOrder.getStatus();
            outboundOrder.allocate();
            
            // Optimistic Lock Update: WHERE id=? AND version=? AND status=?
            boolean success = outboundOrderMapper.update(
                outboundOrder,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<OutboundOrder>()
                    .eq(OutboundOrder::getId, outboundOrder.getId())
                    .eq(OutboundOrder::getVersion, outboundOrder.getVersion())
                    .eq(OutboundOrder::getStatus, oldStatus)
            ) > 0;
            
            if (!success) {
                throw new BusinessException("Concurrency conflict: Outbound status changed or version mismatch.");
            }

            // Create Wave (Simplified: 1 wave per outbound for now)
            PickWave wave = new PickWave();
            wave.setWaveNo("WV" + System.currentTimeMillis());
            wave.setWarehouseId(outboundOrder.getWarehouseId());
            wave.setStatus("CREATED");
            pickWaveMapper.insert(wave);

            // Create Pick Task
            PickTask pickTask = new PickTask();
            pickTask.setPickTaskNo("PT" + System.currentTimeMillis());
            pickTask.setWaveId(wave.getId());
            pickTask.setOutboundId(outboundOrder.getId());
            pickTask.setStatus(PickTask.STATUS_CREATED);
            pickTask.setTraceId(TraceIdUtils.getTraceId());
            pickTaskMapper.insert(pickTask);

            // Create Pick Task Lines from Outbound Lines
            List<OutboundOrderLine> outboundLines = outboundOrderLineMapper.selectList(new QueryWrapper<OutboundOrderLine>().eq("outbound_id", outboundOrder.getId()));
            for (OutboundOrderLine obLine : outboundLines) {
                PickTaskLine pickLine = new PickTaskLine();
                pickLine.setPickTaskId(pickTask.getId());
                pickLine.setOutboundLineId(obLine.getId());
                pickLine.setLineNo(obLine.getLineNo());
                pickLine.setSkuId(obLine.getSkuId());
                pickLine.setQty(obLine.getQty());
                pickLine.setQtyActual(0);
                pickLine.setStatus(PickTaskLine.STATUS_PENDING);
                pickTaskLineMapper.insert(pickLine);
            }

            return pickTask.getPickTaskNo();
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmPickLine(Long pickTaskLineId, Integer qtyActual) {
        String requestId = RequestIdContext.getRequired();
        idempotentService.execute(requestId, "confirmPickLine:" + pickTaskLineId, () -> {
            PickTaskLine line = pickTaskLineMapper.selectById(pickTaskLineId);
            if (line == null) throw new BusinessException("Pick task line not found");

            PickTask task = pickTaskMapper.selectById(line.getPickTaskId());
            if (task == null) throw new BusinessException("Pick task not found");

            // Start task if not started
            if (PickTask.STATUS_CREATED.equals(task.getStatus())) {
                 String oldTaskStatus = task.getStatus();
                 task.start();
                 pickTaskMapper.update(task, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<PickTask>()
                     .eq(PickTask::getId, task.getId())
                     .eq(PickTask::getVersion, task.getVersion())
                     .eq(PickTask::getStatus, oldTaskStatus));
                 
                 // Also start Outbound Picking
                 OutboundOrder outbound = outboundOrderMapper.selectById(task.getOutboundId());
                 if (OutboundOrder.STATUS_ALLOCATED.equals(outbound.getStatus())) {
                     String oldObStatus = outbound.getStatus();
                     outbound.startPicking();
                     outboundOrderMapper.update(outbound, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<OutboundOrder>()
                         .eq(OutboundOrder::getId, outbound.getId())
                         .eq(OutboundOrder::getVersion, outbound.getVersion())
                         .eq(OutboundOrder::getStatus, oldObStatus));
                 }
            }

            // Confirm Line
            String oldLineStatus = line.getStatus();
            line.confirm(qtyActual);
            
            boolean success = pickTaskLineMapper.update(
                line,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<PickTaskLine>()
                    .eq(PickTaskLine::getId, line.getId())
                    .eq(PickTaskLine::getVersion, line.getVersion())
                    .eq(PickTaskLine::getStatus, oldLineStatus)
            ) > 0;
            
            if (!success) {
                throw new BusinessException("Concurrency conflict: PickTaskLine modified.");
            }
        });
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void confirmPick(Long pickTaskId) {
        String requestId = RequestIdContext.getRequired();
        idempotentService.execute(requestId, "confirmPick:" + pickTaskId, () -> {
            PickTask pickTask = pickTaskMapper.selectById(pickTaskId);
            if (pickTask == null) throw new BusinessException("Pick task not found");
            
            // Check all lines done
            List<PickTaskLine> lines = pickTaskLineMapper.selectList(new QueryWrapper<PickTaskLine>().eq("pick_task_id", pickTask.getId()));
            boolean allDone = lines.stream().allMatch(l -> PickTaskLine.STATUS_DONE.equals(l.getStatus()));
            if (!allDone) {
                throw new BusinessException("Cannot complete pick task: Not all lines are DONE.");
            }

            // Complete Pick Task
            String oldStatus = pickTask.getStatus();
            pickTask.complete();
            
            // Optimistic Lock Update
            boolean success = pickTaskMapper.update(
                pickTask,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<PickTask>()
                    .eq(PickTask::getId, pickTask.getId())
                    .eq(PickTask::getVersion, pickTask.getVersion())
                    .eq(PickTask::getStatus, oldStatus)
            ) > 0;
            if (!success) {
                throw new BusinessException("Concurrency conflict: PickTask status changed or version mismatch.");
            }
            
            OutboundOrder outbound = outboundOrderMapper.selectById(pickTask.getOutboundId());
            
            // Update Outbound Lines QtyPicked
            List<OutboundOrderLine> obLines = outboundOrderLineMapper.selectList(new QueryWrapper<OutboundOrderLine>().eq("outbound_id", outbound.getId()));
            boolean outboundFullyPicked = true;
            for (OutboundOrderLine obLine : obLines) {
                // Find matching pick lines (sum actual qty)
                int pickedQty = lines.stream()
                    .filter(pl -> pl.getOutboundLineId().equals(obLine.getId()))
                    .mapToInt(PickTaskLine::getQtyActual)
                    .sum();
                obLine.setQtyPicked(pickedQty);
                outboundOrderLineMapper.updateById(obLine);
                
                if (obLine.getQtyPicked() < obLine.getQty()) {
                    outboundFullyPicked = false;
                }
            }

            // Complete Outbound Picking if fully picked
            if (outboundFullyPicked && OutboundOrder.STATUS_PICKING.equals(outbound.getStatus())) {
                 String oldOutboundStatus = outbound.getStatus();
                 outbound.completePicking(); // To PICKED
                 boolean outboundSuccess = outboundOrderMapper.update(
                    outbound,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<OutboundOrder>()
                        .eq(OutboundOrder::getId, outbound.getId())
                        .eq(OutboundOrder::getVersion, outbound.getVersion())
                        .eq(OutboundOrder::getStatus, oldOutboundStatus)
                 ) > 0;
                 if (!outboundSuccess) {
                     throw new BusinessException("Concurrency conflict: Outbound status changed.");
                 }
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public String pack(String outboundNo) {
        String requestId = RequestIdContext.getRequired();
        return idempotentService.execute(requestId, "pack:" + outboundNo, () -> {
            OutboundOrder outboundOrder = outboundOrderMapper.selectOne(new QueryWrapper<OutboundOrder>().eq("outbound_no", outboundNo));
            
            PackOrder packOrder = new PackOrder();
            packOrder.setPackNo("PK" + System.currentTimeMillis());
            packOrder.setOutboundId(outboundOrder.getId());
            packOrder.setStatus("PACKED");
            packOrder.setPackageCount(1);
            packOrder.setTraceId(TraceIdUtils.getTraceId());
            packOrderMapper.insert(packOrder);
            
            // Create Pack Lines
            List<OutboundOrderLine> lines = outboundOrderLineMapper.selectList(new QueryWrapper<OutboundOrderLine>().eq("outbound_id", outboundOrder.getId()));
            for (OutboundOrderLine line : lines) {
                PackOrderLine packLine = new PackOrderLine();
                packLine.setPackId(packOrder.getId());
                packLine.setLineNo(line.getLineNo());
                packLine.setSkuId(line.getSkuId());
                packLine.setQty(line.getQtyPicked());
                packLine.setPackageNo(1);
                packOrderLineMapper.insert(packLine);
            }
            
            // Domain Logic: Complete Packing
            if (OutboundOrder.STATUS_PICKED.equals(outboundOrder.getStatus())) {
                 String oldStatus = outboundOrder.getStatus();
                 outboundOrder.completePacking();
                 boolean success = outboundOrderMapper.update(
                    outboundOrder,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<OutboundOrder>()
                        .eq(OutboundOrder::getId, outboundOrder.getId())
                        .eq(OutboundOrder::getVersion, outboundOrder.getVersion())
                        .eq(OutboundOrder::getStatus, oldStatus)
                 ) > 0;
                 if (!success) {
                     throw new BusinessException("Concurrency conflict: Outbound status changed.");
                 }
            }
            return packOrder.getPackNo();
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmShipped(Long outboundId) {
        System.out.println("DEBUG: Entering confirmShipped for outboundId: " + outboundId);
        String requestId = RequestIdContext.getRequired();
        idempotentService.execute(requestId, "confirmShipped:" + outboundId, () -> {
            OutboundOrder outboundOrder = outboundOrderMapper.selectById(outboundId);
            if (outboundOrder == null) throw new BusinessException("Outbound order not found");
            
            System.out.println("DEBUG: Found outboundOrder: " + outboundOrder.getId() + " Status: " + outboundOrder.getStatus());

            // Domain Logic: Ship
            String oldStatus = outboundOrder.getStatus();
            outboundOrder.ship();
            
            System.out.println("DEBUG: Changed status to SHIPPED. Updating DB...");

            // Optimistic Lock Update
            // Note: MyBatis-Plus OptimisticLockerInnerInterceptor handles version check and increment automatically.
            boolean success = outboundOrderMapper.update(
                outboundOrder,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<OutboundOrder>()
                    .eq(OutboundOrder::getId, outboundOrder.getId())
                    .eq(OutboundOrder::getStatus, oldStatus)
            ) > 0;
            
            System.out.println("DEBUG: Update success: " + success);

            if (!success) {
                throw new BusinessException("Concurrency conflict: Outbound status changed or version mismatch.");
            }
            
            // TODO: Deduct Inventory (Mock)
            // Requirement: consumeReserved idempotency key must be bizType+bizNo
            // String inventoryDedupKey = "OUTBOUND_SHIP_" + outboundOrder.getOutboundNo();
            // inventoryService.deduct(outboundOrder, inventoryDedupKey);
        });
    }

    public PickTask getPickTask(Long id) {
        PickTask task = pickTaskMapper.selectById(id);
        if (task != null) {
            List<PickTaskLine> lines = pickTaskLineMapper.selectList(new QueryWrapper<PickTaskLine>().eq("pick_task_id", id));
            task.setLines(lines);
        }
        return task;
    }

    public OutboundOrder getOutboundOrder(Long id) {
        OutboundOrder order = getOutbound(id);
        List<OutboundOrderLine> lineList = outboundOrderLineMapper.selectList(new LambdaQueryWrapper<OutboundOrderLine>().eq(OutboundOrderLine::getOutboundId, id).eq(OutboundOrderLine::getDeleted, 0));
        order.setLines(lineList);
        return order;
    }
}
