package com.pod.mes.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.infra.context.RequestIdContext;
import com.pod.infra.idempotent.service.IdempotentService;
import com.pod.inv.service.InventoryApplicationService;
import com.pod.mes.domain.MesReport;
import com.pod.mes.domain.WorkOrder;
import com.pod.mes.domain.WorkOrderItem;
import com.pod.mes.domain.WorkOrderOp;
import com.pod.mes.domain.routing.RoutingStepDef;
import com.pod.mes.domain.routing.RoutingTemplate;
import com.pod.mes.domain.routing.RoutingTemplateProvider;
import com.pod.mes.mapper.MesReportMapper;
import com.pod.mes.mapper.WorkOrderItemMapper;
import com.pod.mes.mapper.WorkOrderMapper;
import com.pod.mes.mapper.WorkOrderOpMapper;
import com.pod.oms.domain.Fulfillment;
import com.pod.oms.domain.FulfillmentItem;
import com.pod.oms.domain.FulfillmentStatus;
import com.pod.oms.mapper.FulfillmentMapper;
import com.pod.oms.mapper.FulfillmentItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * P1.4 MES 工单应用服务：从 Fulfillment(ART_READY) 创建工单、释放、开始、报工、完工入库推进 Fulfillment。
 */
@Service
public class WorkOrderApplicationService {

    private static final String SOURCE_TYPE_FULFILLMENT = "FULFILLMENT";
    private static final String BIZ_TYPE_MES_PRODUCE_IN = "MES_PRODUCE_IN";

    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private WorkOrderItemMapper workOrderItemMapper;
    @Autowired
    private WorkOrderOpMapper workOrderOpMapper;
    @Autowired
    private MesReportMapper mesReportMapper;
    @Autowired
    private FulfillmentMapper fulfillmentMapper;
    @Autowired
    private FulfillmentItemMapper fulfillmentItemMapper;
    @Autowired
    private InventoryApplicationService inventoryApplicationService;
    @Autowired
    private RoutingTemplateProvider routingTemplateProvider;
    @Autowired
    private IdempotentService idempotentService;

    private long tenantId() {
        return TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L;
    }

    private long factoryId() {
        return TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createFromFulfillment(Long fulfillmentId) {
        String requestId = RequestIdContext.get();
        if (requestId == null || requestId.isBlank()) requestId = "mes-wo-" + fulfillmentId + "-" + System.currentTimeMillis();
        return idempotentService.execute(requestId, "createWorkOrderFromFulfillment:" + fulfillmentId, () -> {
            Fulfillment f = fulfillmentMapper.selectById(fulfillmentId);
            if (f == null || !Objects.equals(f.getTenantId(), tenantId()) || !Objects.equals(f.getFactoryId(), factoryId())
                    || (f.getDeleted() != null && f.getDeleted() != 0)) {
                throw new BusinessException("Fulfillment not found: " + fulfillmentId);
            }
            if (!FulfillmentStatus.ART_READY.name().equals(f.getStatus())) {
                throw new BusinessException("Fulfillment must be ART_READY to create work order. Current: " + f.getStatus());
            }
            List<FulfillmentItem> items = fulfillmentItemMapper.selectList(
                    new LambdaQueryWrapper<FulfillmentItem>().eq(FulfillmentItem::getFulfillmentId, fulfillmentId).eq(FulfillmentItem::getDeleted, 0));
            if (items == null || items.isEmpty()) {
                throw new BusinessException("Fulfillment has no lines: " + fulfillmentId);
            }
            String sourceNo = f.getFulfillmentNo();
            WorkOrder existing = workOrderMapper.selectOne(new LambdaQueryWrapper<WorkOrder>()
                    .eq(WorkOrder::getTenantId, tenantId()).eq(WorkOrder::getFactoryId, factoryId())
                    .eq(WorkOrder::getSourceType, SOURCE_TYPE_FULFILLMENT).eq(WorkOrder::getSourceNo, sourceNo)
                    .eq(WorkOrder::getDeleted, 0));
            if (existing != null) return existing.getId();

            String workOrderNo = "WO-" + sourceNo;
            WorkOrder wo = new WorkOrder();
            wo.setWorkOrderNo(workOrderNo);
            wo.setFulfillmentId(fulfillmentId);
            wo.setSourceType(SOURCE_TYPE_FULFILLMENT);
            wo.setSourceNo(sourceNo);
            wo.setStatus(WorkOrder.STATUS_CREATED);
            wo.setPriority(100);
            workOrderMapper.insert(wo);

            int lineNo = 1;
            for (FulfillmentItem item : items) {
                WorkOrderItem line = new WorkOrderItem();
                line.setWorkOrderId(wo.getId());
                line.setLineNo(lineNo++);
                line.setSkuId(item.getSkuId());
                int qty = item.getReservedQty() != null && item.getReservedQty() > 0 ? item.getReservedQty() : (item.getQty() != null ? item.getQty() : 0);
                if (qty <= 0) continue;
                line.setQty(qty);
                line.setProducedQty(0);
                line.setScrapQty(0);
                line.setStatus("CREATED");
                workOrderItemMapper.insert(line);
            }

            Long firstSku = items.isEmpty() ? null : items.get(0).getSkuId();
            String productType = firstSku != null ? "DEFAULT" : "DEFAULT";
            RoutingTemplate template = routingTemplateProvider.getTemplateFor(tenantId(), factoryId(), productType);
            if (template != null) {
                for (RoutingStepDef step : template.getSteps()) {
                    WorkOrderOp op = WorkOrderOp.create(wo.getId(), step.getStepNo(), step.getOpCode());
                    workOrderOpMapper.insert(op);
                }
            } else {
                workOrderOpMapper.insert(WorkOrderOp.create(wo.getId(), 1, "PRINT"));
                workOrderOpMapper.insert(WorkOrderOp.create(wo.getId(), 2, "QC"));
                workOrderOpMapper.insert(WorkOrderOp.create(wo.getId(), 3, "PACK"));
            }
            return wo.getId();
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void release(Long workOrderId) {
        WorkOrder wo = getWorkOrder(workOrderId);
        wo.release();
        int rows = workOrderMapper.update(null, new LambdaUpdateWrapper<WorkOrder>()
                .eq(WorkOrder::getId, workOrderId).eq(WorkOrder::getStatus, WorkOrder.STATUS_CREATED)
                .eq(WorkOrder::getVersion, wo.getVersion()).set(WorkOrder::getStatus, WorkOrder.STATUS_RELEASED)
                .setSql("version = version + 1"));
        if (rows == 0) throw new BusinessException("Work order not in CREATED or concurrent update failed");
    }

    @Transactional(rollbackFor = Exception.class)
    public void start(Long workOrderId) {
        WorkOrder wo = getWorkOrder(workOrderId);
        String from = wo.getStatus();
        if (!WorkOrder.STATUS_RELEASED.equals(from) && !WorkOrder.STATUS_SCHEDULED.equals(from)) {
            throw new BusinessException("Work order must be RELEASED to start. Current: " + from);
        }
        wo.start();
        int rows = workOrderMapper.update(null, new LambdaUpdateWrapper<WorkOrder>()
                .eq(WorkOrder::getId, workOrderId).in(WorkOrder::getStatus, WorkOrder.STATUS_RELEASED, WorkOrder.STATUS_SCHEDULED)
                .eq(WorkOrder::getVersion, wo.getVersion()).set(WorkOrder::getStatus, WorkOrder.STATUS_IN_PROGRESS)
                .setSql("version = version + 1"));
        if (rows == 0) throw new BusinessException("Concurrent update or invalid status");
    }

    @Transactional(rollbackFor = Exception.class)
    public void report(Long workOrderId, Long lineId, Integer goodQty, Integer scrapQty, String opCode, Long workstationId) {
        WorkOrder wo = getWorkOrder(workOrderId);
        if (wo.isDone() || WorkOrder.STATUS_CANCELED.equals(wo.getStatus())) {
            throw new BusinessException("Work order is already done or canceled");
        }
        int g = goodQty != null ? goodQty : 0;
        int s = scrapQty != null ? scrapQty : 0;
        if (g <= 0 && s <= 0) return;

        WorkOrderItem line = workOrderItemMapper.selectById(lineId);
        if (line == null || !Objects.equals(line.getWorkOrderId(), workOrderId)
                || !Objects.equals(line.getTenantId(), tenantId()) || !Objects.equals(line.getFactoryId(), factoryId())) {
            throw new BusinessException("Work order line not found: " + lineId);
        }

        MesReport report = new MesReport();
        report.setWorkOrderId(workOrderId);
        report.setWorkOrderLineId(lineId);
        report.setOpCode(opCode != null ? opCode : "REPORT");
        report.setGoodQty(g);
        report.setScrapQty(s);
        report.setWorkstationId(workstationId);
        mesReportMapper.insert(report);

        int newProduced = (line.getProducedQty() != null ? line.getProducedQty() : 0) + g;
        int newScrap = (line.getScrapQty() != null ? line.getScrapQty() : 0) + s;
        workOrderItemMapper.update(null, new LambdaUpdateWrapper<WorkOrderItem>()
                .eq(WorkOrderItem::getId, lineId)
                .set(WorkOrderItem::getProducedQty, newProduced)
                .set(WorkOrderItem::getScrapQty, newScrap)
                .setSql("version = version + 1"));

        if (!WorkOrder.STATUS_IN_PROGRESS.equals(wo.getStatus()) && !WorkOrder.STATUS_RUNNING.equals(wo.getStatus())) {
            workOrderMapper.update(null, new LambdaUpdateWrapper<WorkOrder>()
                    .eq(WorkOrder::getId, workOrderId)
                    .set(WorkOrder::getStatus, WorkOrder.STATUS_IN_PROGRESS)
                    .setSql("version = version + 1"));
        }

        finishIfCompleted(workOrderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void finishIfCompleted(Long workOrderId) {
        WorkOrder wo = workOrderMapper.selectById(workOrderId);
        if (wo == null || !Objects.equals(wo.getTenantId(), tenantId()) || !Objects.equals(wo.getFactoryId(), factoryId())) return;
        if (wo.isDone()) return;

        List<WorkOrderItem> lines = workOrderItemMapper.selectList(
                new LambdaQueryWrapper<WorkOrderItem>().eq(WorkOrderItem::getWorkOrderId, workOrderId).eq(WorkOrderItem::getDeleted, 0));
        boolean allReached = true;
        for (WorkOrderItem line : lines) {
            int qty = line.getQty() != null ? line.getQty() : 0;
            int produced = line.getProducedQty() != null ? line.getProducedQty() : 0;
            if (qty > 0 && produced < qty) {
                allReached = false;
                break;
            }
        }
        if (!allReached) return;

        wo.complete();
        workOrderMapper.update(null, new LambdaUpdateWrapper<WorkOrder>()
                .eq(WorkOrder::getId, workOrderId)
                .eq(WorkOrder::getVersion, wo.getVersion())
                .set(WorkOrder::getStatus, WorkOrder.STATUS_DONE)
                .setSql("version = version + 1"));

        Fulfillment f = fulfillmentMapper.selectById(wo.getFulfillmentId());
        Long warehouseId = f != null && f.getWarehouseId() != null ? f.getWarehouseId() : 300001L;
        String bizNo = wo.getWorkOrderNo();
        for (WorkOrderItem line : lines) {
            int produced = line.getProducedQty() != null ? line.getProducedQty() : 0;
            if (produced > 0 && line.getSkuId() != null) {
                inventoryApplicationService.produceIn(BIZ_TYPE_MES_PRODUCE_IN, bizNo, warehouseId, line.getSkuId(), produced);
            }
        }

        if (f != null && FulfillmentStatus.ART_READY.name().equals(f.getStatus())) {
            f.markReadyToShip();
            fulfillmentMapper.updateStatusWithLock(f.getId(), f.getStatus(), FulfillmentStatus.ART_READY.name(), f.getVersion(), TenantContext.getUserId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long workOrderId) {
        WorkOrder wo = getWorkOrder(workOrderId);
        wo.cancel();
        int rows = workOrderMapper.update(null, new LambdaUpdateWrapper<WorkOrder>()
                .eq(WorkOrder::getId, workOrderId)
                .in(WorkOrder::getStatus, WorkOrder.STATUS_CREATED, WorkOrder.STATUS_RELEASED, WorkOrder.STATUS_IN_PROGRESS, WorkOrder.STATUS_SCHEDULED, WorkOrder.STATUS_RUNNING)
                .eq(WorkOrder::getVersion, wo.getVersion())
                .set(WorkOrder::getStatus, WorkOrder.STATUS_CANCELED)
                .setSql("version = version + 1"));
        if (rows == 0) throw new BusinessException("Work order cannot be canceled or concurrent update failed");
    }

    public WorkOrder getWorkOrder(Long id) {
        WorkOrder wo = workOrderMapper.selectById(id);
        if (wo == null || !Objects.equals(wo.getTenantId(), tenantId()) || !Objects.equals(wo.getFactoryId(), factoryId())
                || (wo.getDeleted() != null && wo.getDeleted() != 0)) {
            throw new BusinessException("Work order not found: " + id);
        }
        return wo;
    }

    public IPage<WorkOrder> page(Page<WorkOrder> page, String status) {
        LambdaQueryWrapper<WorkOrder> q = new LambdaQueryWrapper<>();
        q.eq(WorkOrder::getTenantId, tenantId()).eq(WorkOrder::getFactoryId, factoryId()).eq(WorkOrder::getDeleted, 0);
        if (status != null && !status.isBlank()) q.eq(WorkOrder::getStatus, status);
        q.orderByDesc(WorkOrder::getId);
        return workOrderMapper.selectPage(page, q);
    }

    public List<WorkOrderItem> listLines(Long workOrderId) {
        return workOrderItemMapper.selectList(
                new LambdaQueryWrapper<WorkOrderItem>().eq(WorkOrderItem::getWorkOrderId, workOrderId).eq(WorkOrderItem::getDeleted, 0).orderByAsc(WorkOrderItem::getLineNo));
    }

    public List<WorkOrderOp> listOps(Long workOrderId) {
        return workOrderOpMapper.selectList(
                new LambdaQueryWrapper<WorkOrderOp>().eq(WorkOrderOp::getWorkOrderId, workOrderId).eq(WorkOrderOp::getDeleted, 0).orderByAsc(WorkOrderOp::getStepNo));
    }

    public List<MesReport> listReports(Long workOrderId) {
        return mesReportMapper.selectList(
                new LambdaQueryWrapper<MesReport>().eq(MesReport::getWorkOrderId, workOrderId).eq(MesReport::getDeleted, 0).orderByDesc(MesReport::getCreatedAt));
    }
}
