package com.pod.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.mes.domain.MesReport;
import com.pod.mes.domain.WorkOrder;
import com.pod.mes.domain.WorkOrderItem;
import com.pod.mes.domain.WorkOrderOp;
import com.pod.mes.service.WorkOrderApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * P1.4 MES 工单 API：工单列表/详情、释放、开始、报工、取消。
 */
@RestController
@RequestMapping("/api/mes")
public class MesController {

    @Autowired
    private WorkOrderApplicationService workOrderApplicationService;

    @GetMapping("/work-orders")
    @RequirePerm("mes:work-order:page")
    public Result<IPage<WorkOrder>> page(
            @RequestParam(name = "current", defaultValue = "1") Integer current,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "status", required = false) String status) {
        return Result.success(workOrderApplicationService.page(new Page<>(current, size), status));
    }

    @GetMapping("/work-orders/{id}")
    @RequirePerm("mes:work-order:get")
    public Result<WorkOrder> get(@PathVariable("id") Long id) {
        return Result.success(workOrderApplicationService.getWorkOrder(id));
    }

    @GetMapping("/work-orders/{id}/lines")
    @RequirePerm("mes:work-order:get")
    public Result<List<WorkOrderItem>> listLines(@PathVariable("id") Long id) {
        return Result.success(workOrderApplicationService.listLines(id));
    }

    @GetMapping("/work-orders/{id}/ops")
    @RequirePerm("mes:work-order:get")
    public Result<List<WorkOrderOp>> listOps(@PathVariable("id") Long id) {
        return Result.success(workOrderApplicationService.listOps(id));
    }

    @GetMapping("/work-orders/{id}/reports")
    @RequirePerm("mes:work-order:get")
    public Result<List<MesReport>> listReports(@PathVariable("id") Long id) {
        return Result.success(workOrderApplicationService.listReports(id));
    }

    @PostMapping("/work-orders/{id}/release")
    @RequirePerm("mes:work-order:release")
    public Result<Void> release(@PathVariable("id") Long id) {
        workOrderApplicationService.release(id);
        return Result.success();
    }

    @PostMapping("/work-orders/{id}/start")
    @RequirePerm("mes:work-order:start")
    public Result<Void> start(@PathVariable("id") Long id) {
        workOrderApplicationService.start(id);
        return Result.success();
    }

    @PostMapping("/work-orders/{id}/report")
    @RequirePerm("mes:work-order:report")
    public Result<Void> report(@PathVariable("id") Long id, @RequestBody ReportBody body) {
        workOrderApplicationService.report(id,
                body.getLineId(),
                body.getGoodQty(),
                body.getScrapQty(),
                body.getOpCode(),
                body.getWorkstationId());
        return Result.success();
    }

    @PostMapping("/work-orders/{id}/cancel")
    @RequirePerm("mes:work-order:cancel")
    public Result<Void> cancel(@PathVariable("id") Long id) {
        workOrderApplicationService.cancel(id);
        return Result.success();
    }

    @PostMapping("/work-orders/from-fulfillment/{fulfillmentId}")
    @RequirePerm("mes:work-order:create")
    public Result<Long> createFromFulfillment(@PathVariable("fulfillmentId") Long fulfillmentId) {
        return Result.success(workOrderApplicationService.createFromFulfillment(fulfillmentId));
    }

    public static class ReportBody {
        private Long lineId;
        private Integer goodQty;
        private Integer scrapQty;
        private String opCode;
        private Long workstationId;

        public Long getLineId() { return lineId; }
        public void setLineId(Long lineId) { this.lineId = lineId; }
        public Integer getGoodQty() { return goodQty; }
        public void setGoodQty(Integer goodQty) { this.goodQty = goodQty; }
        public Integer getScrapQty() { return scrapQty; }
        public void setScrapQty(Integer scrapQty) { this.scrapQty = scrapQty; }
        public String getOpCode() { return opCode; }
        public void setOpCode(String opCode) { this.opCode = opCode; }
        public Long getWorkstationId() { return workstationId; }
        public void setWorkstationId(Long workstationId) { this.workstationId = workstationId; }
    }
}
