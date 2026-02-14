package com.pod.mes.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pod.common.core.exception.BusinessException;
import com.pod.infra.context.RequestIdContext;
import com.pod.mes.domain.WorkOrder;
import com.pod.mes.domain.WorkOrderOp;
import com.pod.mes.domain.routing.RoutingStepDef;
import com.pod.mes.domain.routing.RoutingTemplate;
import com.pod.mes.domain.routing.RoutingTemplateProvider;
import com.pod.mes.mapper.WorkOrderMapper;
import com.pod.mes.mapper.WorkOrderOpMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkOrderService {

    @Autowired
    private WorkOrderMapper workOrderMapper;
    
    @Autowired
    private WorkOrderOpMapper workOrderOpMapper;
    
    @Autowired
    private com.pod.infra.idempotent.service.IdempotentService idempotentService;

    @Autowired
    private RoutingTemplateProvider routingTemplateProvider;

    @Transactional(rollbackFor = Exception.class)
    public Long createWorkOrder(Long fulfillmentId, String jobNo) {
        String requestId = RequestIdContext.getRequired();
        return idempotentService.execute(requestId, "createWorkOrder:" + fulfillmentId, () -> {
            String woNo = "WO-" + jobNo;
            
            // Idempotency Check
            Long count = workOrderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>()
                    .eq(WorkOrder::getWorkOrderNo, woNo));
            if (count > 0) {
                WorkOrder existing = workOrderMapper.selectOne(new LambdaQueryWrapper<WorkOrder>()
                        .eq(WorkOrder::getWorkOrderNo, woNo));
                return existing.getId();
            }
    
            WorkOrder wo = WorkOrder.create(fulfillmentId, woNo);
            
            // Get Routing Template
            // TODO: Get real tenant/factory from Context
            String productType = (jobNo != null && jobNo.toUpperCase().contains("TSHIRT")) ? "TSHIRT" : "DEFAULT";
            RoutingTemplate template = routingTemplateProvider.getTemplateFor(1L, 1L, productType);
            
            if (template != null) {
                wo.setRemark("RoutingTemplate:" + template.getCode());
                workOrderMapper.insert(wo);
                
                // Create Operations from Template
                for (RoutingStepDef step : template.getSteps()) {
                    WorkOrderOp op = WorkOrderOp.create(wo.getId(), step.getStepNo(), step.getOpCode());
                    // Copy other properties if needed (e.g. isQc)
                    // If step.isQc(), maybe set a flag in op? WorkOrderOp doesn't have isQc flag, only opCode.
                    workOrderOpMapper.insert(op);
                }
            } else {
                // Fallback (Should not happen with InMemoryProvider)
                workOrderMapper.insert(wo);
                workOrderOpMapper.insert(WorkOrderOp.create(wo.getId(), 1, "PRINT"));
                workOrderOpMapper.insert(WorkOrderOp.create(wo.getId(), 2, "CUT"));
                workOrderOpMapper.insert(WorkOrderOp.create(wo.getId(), 3, "PACK"));
            }
    
            return wo.getId();
        });
    }
    
    public WorkOrder getWorkOrder(Long id) {
        return workOrderMapper.selectById(id);
    }
    
    public List<WorkOrderOp> getOperations(Long woId) {
        return workOrderOpMapper.selectList(new LambdaQueryWrapper<WorkOrderOp>()
                .eq(WorkOrderOp::getWorkOrderId, woId)
                .orderByAsc(WorkOrderOp::getStepNo));
    }

    @Transactional(rollbackFor = Exception.class)
    public void releaseWorkOrder(Long id) {
        String requestId = RequestIdContext.getRequired();
        idempotentService.execute(requestId, "releaseWorkOrder:" + id, () -> {
            WorkOrder wo = workOrderMapper.selectById(id);
            if (wo == null) throw new BusinessException("WorkOrder not found");
            
            String oldStatus = wo.getStatus();
            wo.release();
            
            int rows = workOrderMapper.update(wo, new LambdaUpdateWrapper<WorkOrder>()
                    .eq(WorkOrder::getId, id)
                    .eq(WorkOrder::getStatus, oldStatus)
                    .eq(WorkOrder::getVersion, wo.getVersion()));
                    
            if (rows == 0) throw new BusinessException("Concurrent update failed");
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void startWorkOrder(Long id) {
        String requestId = RequestIdContext.getRequired();
        idempotentService.execute(requestId, "startWorkOrder:" + id, () -> {
            WorkOrder wo = workOrderMapper.selectById(id);
            if (wo == null) throw new BusinessException("WorkOrder not found");
    
            String oldStatus = wo.getStatus();
            wo.start();
    
            int rows = workOrderMapper.update(wo, new LambdaUpdateWrapper<WorkOrder>()
                    .eq(WorkOrder::getId, id)
                    .eq(WorkOrder::getStatus, oldStatus)
                    .eq(WorkOrder::getVersion, wo.getVersion()));
    
            if (rows == 0) throw new BusinessException("Concurrent update failed");
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void startOperation(Long woId, Long opId) {
        String requestId = RequestIdContext.getRequired();
        idempotentService.execute(requestId, "startOperation:" + opId, () -> {
            WorkOrder wo = workOrderMapper.selectById(woId);
            if (wo == null) throw new BusinessException("WorkOrder not found");

            List<WorkOrderOp> allOps = getOperations(woId);
            WorkOrderOp currentOp = allOps.stream()
                    .filter(o -> o.getId().equals(opId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Operation not found"));

            String oldOpStatus = currentOp.getStatus();
            String oldWoStatus = wo.getStatus();

            // Domain Logic
            wo.startOp(currentOp, allOps);

            // Persist Op
            int rows = workOrderOpMapper.update(currentOp, new LambdaUpdateWrapper<WorkOrderOp>()
                    .eq(WorkOrderOp::getId, opId)
                    .eq(WorkOrderOp::getStatus, oldOpStatus)
                    .eq(WorkOrderOp::getVersion, currentOp.getVersion()));
            
            if (rows == 0) throw new BusinessException(WorkOrder.ERR_CONCURRENT_MODIFICATION, "Concurrent update failed for Operation");

            // Persist WO if changed
            if (!oldWoStatus.equals(wo.getStatus())) {
                rows = workOrderMapper.update(wo, new LambdaUpdateWrapper<WorkOrder>()
                        .eq(WorkOrder::getId, woId)
                        .eq(WorkOrder::getStatus, oldWoStatus)
                        .eq(WorkOrder::getVersion, wo.getVersion()));
                if (rows == 0) throw new BusinessException(WorkOrder.ERR_CONCURRENT_MODIFICATION, "Concurrent update failed for WorkOrder");
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void finishOperation(Long woId, Long opId) {
        String requestId = RequestIdContext.getRequired();
        idempotentService.execute(requestId, "finishOperation:" + opId, () -> {
            WorkOrder wo = workOrderMapper.selectById(woId);
            if (wo == null) throw new BusinessException("WorkOrder not found");
            
            List<WorkOrderOp> allOps = getOperations(woId);
            WorkOrderOp currentOp = allOps.stream()
                    .filter(o -> o.getId().equals(opId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Operation not found"));
            
            String oldOpStatus = currentOp.getStatus();
            String oldWoStatus = wo.getStatus();
            
            // Domain Logic
            wo.finishOp(currentOp, allOps);
            
            // Persist Op
            int rows = workOrderOpMapper.update(currentOp, new LambdaUpdateWrapper<WorkOrderOp>()
                    .eq(WorkOrderOp::getId, opId)
                    .eq(WorkOrderOp::getStatus, oldOpStatus)
                    .eq(WorkOrderOp::getVersion, currentOp.getVersion()));
            
            if (rows == 0) throw new BusinessException(WorkOrder.ERR_CONCURRENT_MODIFICATION, "Concurrent update failed for Operation");
            
            // Persist WO if changed
            if (!oldWoStatus.equals(wo.getStatus())) {
                rows = workOrderMapper.update(wo, new LambdaUpdateWrapper<WorkOrder>()
                        .eq(WorkOrder::getId, woId)
                        .eq(WorkOrder::getStatus, oldWoStatus)
                        .eq(WorkOrder::getVersion, wo.getVersion()));
                if (rows == 0) throw new BusinessException(WorkOrder.ERR_CONCURRENT_MODIFICATION, "Concurrent update failed for WorkOrder");
            }
        });
    }
    
    // Removed legacy checkAndCompleteWorkOrder as logic is now in Domain

}
