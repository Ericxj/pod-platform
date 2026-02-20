package com.pod.wms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.wms.domain.OutboundOrder;
import com.pod.wms.domain.OutboundOrderLine;
import com.pod.wms.domain.PickTask;
import com.pod.wms.service.OutboundApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * P1.5 WMS 出库 API：出库单列表/详情、开始拣货、确认拣货、打包、发货、取消。
 */
@RestController
@RequestMapping("/api/wms")
public class WmsController {

    @Autowired
    private OutboundApplicationService outboundService;

    // ---------- P1.5 出库单 ----------

    @GetMapping("/outbounds")
    @RequirePerm("wms:outbound:page")
    public Result<IPage<OutboundOrder>> pageOutbounds(
            @RequestParam(name = "current", defaultValue = "1") Integer current,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "status", required = false) String status) {
        return Result.success(outboundService.page(new Page<>(current, size), status));
    }

    @GetMapping("/outbounds/{id}")
    @RequirePerm("wms:outbound:get")
    public Result<OutboundOrder> getOutbound(@PathVariable Long id) {
        return Result.success(outboundService.getOutboundOrder(id));
    }

    @PostMapping("/outbounds/from-fulfillment/{fulfillmentId}")
    @RequirePerm("wms:outbound:create")
    public Result<Long> createFromFulfillment(@PathVariable Long fulfillmentId) {
        return Result.success(outboundService.createFromFulfillment(fulfillmentId));
    }

    @PostMapping("/outbounds/{id}/picking/start")
    @RequirePerm("wms:outbound:picking:start")
    public Result<Void> startPicking(@PathVariable Long id) {
        outboundService.startPicking(id);
        return Result.success();
    }

    @PostMapping("/outbounds/{id}/picking/confirm")
    @RequirePerm("wms:outbound:picking:confirm")
    public Result<Void> confirmPicking(@PathVariable Long id, @RequestBody List<OutboundApplicationService.PickingLineDto> body) {
        outboundService.confirmPicking(id, body != null ? body : List.of());
        return Result.success();
    }

    @PostMapping("/outbounds/{id}/pack")
    @RequirePerm("wms:outbound:pack")
    public Result<Void> pack(@PathVariable Long id) {
        outboundService.pack(id);
        return Result.success();
    }

    @PostMapping("/outbounds/{id}/ship")
    @RequirePerm("wms:outbound:ship")
    public Result<Void> ship(@PathVariable Long id, @RequestBody ShipBody body) {
        outboundService.ship(id, body != null ? body.getCarrierCode() : null, body != null ? body.getTrackingNo() : null);
        return Result.success();
    }

    @PostMapping("/outbounds/{id}/cancel")
    @RequirePerm("wms:outbound:cancel")
    public Result<Void> cancelOutbound(@PathVariable Long id) {
        outboundService.cancel(id);
        return Result.success();
    }

    public static class ShipBody {
        private String carrierCode;
        private String trackingNo;
        public String getCarrierCode() { return carrierCode; }
        public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }
        public String getTrackingNo() { return trackingNo; }
        public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    }

    // ---------- 兼容旧 ----------

    @PostMapping("/outbound")
    public Result<String> createOutbound(@RequestBody CreateOutboundRequest request) {
        return Result.success(outboundService.createOutboundOrder(
            request.getFulfillmentNo(),
            request.getWarehouseId(),
            request.getLines()
        ));
    }

    @GetMapping("/outbound/{id}")
    public Result<OutboundOrder> getOutboundLegacy(@PathVariable Long id) {
        return Result.success(outboundService.getOutboundOrder(id));
    }

    @PostMapping("/pick-tasks")
    public Result<String> createPickTask(@RequestBody java.util.Map<String, String> request) {
        String outboundNo = request.get("outboundNo");
        return Result.success(outboundService.createPickTask(outboundNo));
    }

    @GetMapping("/pick-tasks/{id}")
    public Result<PickTask> getPickTask(@PathVariable Long id) {
        return Result.success(outboundService.getPickTask(id));
    }

    @PostMapping("/pick-tasks/lines/{lineId}/confirm")
    public Result<Void> confirmPickLine(@PathVariable Long lineId, @RequestBody java.util.Map<String, Integer> request) {
        Integer qtyActual = request.get("qtyActual");
        outboundService.confirmPickLine(lineId, qtyActual);
        return Result.success();
    }

    @PostMapping("/pick-tasks/{id}/complete")
    public Result<Void> completePickTask(@PathVariable Long id) {
        outboundService.confirmPick(id);
        return Result.success();
    }

    @PostMapping("/pack")
    public Result<String> packLegacy(@RequestBody java.util.Map<String, String> request) {
        String outboundNo = request.get("outboundNo");
        return Result.success(outboundService.pack(outboundNo));
    }

    @PostMapping("/ship")
    public Result<Void> shipLegacy(@RequestBody java.util.Map<String, Long> request) {
        Long outboundId = request.get("outboundId");
        outboundService.confirmShipped(outboundId);
        return Result.success();
    }

    public static class CreateOutboundRequest {
        private String fulfillmentNo;
        private Long warehouseId;
        private List<OutboundOrderLine> lines;
        public String getFulfillmentNo() { return fulfillmentNo; }
        public void setFulfillmentNo(String fulfillmentNo) { this.fulfillmentNo = fulfillmentNo; }
        public Long getWarehouseId() { return warehouseId; }
        public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
        public List<OutboundOrderLine> getLines() { return lines; }
        public void setLines(List<OutboundOrderLine> lines) { this.lines = lines; }
    }
}
