package com.pod.wms.controller;

import com.pod.common.core.domain.Result;
import com.pod.wms.domain.OutboundOrder;
import com.pod.wms.domain.OutboundOrderLine;
import com.pod.wms.domain.PickTask;
import com.pod.wms.service.OutboundApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wms")
public class WmsController {

    @Autowired
    private OutboundApplicationService outboundService;

    // --- Outbound ---

    @PostMapping("/outbound")
    public Result<String> createOutbound(@RequestBody CreateOutboundRequest request) {
        return Result.success(outboundService.createOutboundOrder(
            request.getFulfillmentNo(),
            request.getWarehouseId(),
            request.getLines()
        ));
    }

    @GetMapping("/outbound/{id}")
    public Result<OutboundOrder> getOutbound(@PathVariable Long id) {
        return Result.success(outboundService.getOutboundOrder(id));
    }

    // --- Pick Task ---

    @PostMapping("/pick-tasks")
    public Result<String> createPickTask(@RequestBody Map<String, String> request) {
        String outboundNo = request.get("outboundNo");
        return Result.success(outboundService.createPickTask(outboundNo));
    }

    @GetMapping("/pick-tasks/{id}")
    public Result<PickTask> getPickTask(@PathVariable Long id) {
        return Result.success(outboundService.getPickTask(id));
    }

    @PostMapping("/pick-tasks/lines/{lineId}/confirm")
    public Result<Void> confirmPickLine(@PathVariable Long lineId, @RequestBody Map<String, Integer> request) {
        Integer qtyActual = request.get("qtyActual");
        outboundService.confirmPickLine(lineId, qtyActual);
        return Result.success();
    }

    @PostMapping("/pick-tasks/{id}/complete")
    public Result<Void> completePickTask(@PathVariable Long id) {
        outboundService.confirmPick(id);
        return Result.success();
    }

    // --- Pack & Ship ---

    @PostMapping("/pack")
    public Result<String> pack(@RequestBody Map<String, String> request) {
        String outboundNo = request.get("outboundNo");
        return Result.success(outboundService.pack(outboundNo));
    }

    @PostMapping("/ship")
    public Result<Void> ship(@RequestBody Map<String, Long> request) {
        Long outboundId = request.get("outboundId");
        outboundService.confirmShipped(outboundId);
        return Result.success();
    }

    // --- DTOs ---

    public static class CreateOutboundRequest {
        private String fulfillmentNo;
        private Long warehouseId;
        private List<OutboundOrderLine> lines;

        public String getFulfillmentNo() {
            return fulfillmentNo;
        }

        public void setFulfillmentNo(String fulfillmentNo) {
            this.fulfillmentNo = fulfillmentNo;
        }

        public Long getWarehouseId() {
            return warehouseId;
        }

        public void setWarehouseId(Long warehouseId) {
            this.warehouseId = warehouseId;
        }

        public List<OutboundOrderLine> getLines() {
            return lines;
        }

        public void setLines(List<OutboundOrderLine> lines) {
            this.lines = lines;
        }
    }
}
