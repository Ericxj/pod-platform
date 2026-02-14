package com.pod.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.domain.Result;
import com.pod.mes.domain.WorkOrder;
import com.pod.mes.mapper.WorkOrderMapper;
import com.pod.mes.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mes")
public class MesController {

    @Autowired
    private WorkOrderService workOrderService;
    
    @Autowired
    private WorkOrderMapper workOrderMapper;

    @GetMapping("/workOrders/page")
    public Result<Page<WorkOrder>> page(Page<WorkOrder> page) {
        return Result.success(workOrderMapper.selectPage(page, new LambdaQueryWrapper<WorkOrder>().orderByDesc(WorkOrder::getId)));
    }

    @GetMapping("/workOrders/{id}")
    public Result<WorkOrder> get(@PathVariable("id") Long id) {
        return Result.success(workOrderService.getWorkOrder(id));
    }
    
    @PostMapping("/workOrders/{id}/release")
    public Result<Void> release(@PathVariable("id") Long id) {
        workOrderService.releaseWorkOrder(id);
        return Result.success();
    }

    @PostMapping("/workOrders/{id}/start")
    public Result<Void> start(@PathVariable("id") Long id) {
        workOrderService.startWorkOrder(id);
        return Result.success();
    }

    @PostMapping("/workOrders/{id}/finishOp")
    public Result<Void> finishOp(@PathVariable("id") Long id, @RequestParam("opId") Long opId) {
        workOrderService.finishOperation(id, opId);
        return Result.success();
    }
}
