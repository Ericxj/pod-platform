package com.pod.tms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.tms.domain.ChannelShipmentAck;
import com.pod.tms.domain.Shipment;
import com.pod.tms.domain.TmsCarrier;
import com.pod.tms.mapper.TmsCarrierMapper;
import com.pod.common.core.context.TenantContext;
import com.pod.tms.service.ChannelAckApplicationService;
import com.pod.tms.service.TmsApplicationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * P1.6 TMS API：发货单、回传任务(acks)、承运商列表。
 */
@RestController
@RequestMapping("/api/tms")
public class TmsController {

    @Autowired
    private TmsApplicationService tmsApplicationService;
    @Autowired
    private ChannelAckApplicationService channelAckApplicationService;
    @Autowired
    private TmsCarrierMapper tmsCarrierMapper;

    @GetMapping("/shipments")
    @RequirePerm("tms:shipment:page")
    public Result<IPage<Shipment>> pageShipments(
            @RequestParam(name = "current", defaultValue = "1") Integer current,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "status", required = false) String status) {
        return Result.success(tmsApplicationService.page(new Page<>(current, size), status));
    }

    @GetMapping("/shipments/{id}")
    @RequirePerm("tms:shipment:get")
    public Result<Shipment> getShipment(@PathVariable Long id) {
        return Result.success(tmsApplicationService.getShipment(id));
    }

    @PostMapping("/shipments/from-outbound/{outboundId}")
    @RequirePerm("tms:shipment:create")
    public Result<Long> createFromOutbound(@PathVariable Long outboundId) {
        return Result.success(tmsApplicationService.createShipmentFromOutbound(outboundId));
    }

    @PostMapping("/shipments/{id}/label/create")
    @RequirePerm("tms:shipment:label")
    public Result<Void> createLabel(@PathVariable Long id) {
        tmsApplicationService.createLabel(id);
        return Result.success();
    }

    @PostMapping("/shipments/{id}/sync/channel")
    @RequirePerm("tms:shipment:sync")
    public Result<Void> syncToChannel(@PathVariable Long id) {
        tmsApplicationService.syncToChannel(id);
        return Result.success();
    }

    @GetMapping("/carriers")
    @RequirePerm("tms:carrier:list")
    public Result<List<TmsCarrier>> listCarriers() {
        Long tenantId = TenantContext.getTenantId() != null ? TenantContext.getTenantId() : 0L;
        Long factoryId = TenantContext.getFactoryId() != null ? TenantContext.getFactoryId() : 0L;
        List<TmsCarrier> list = tmsCarrierMapper.selectList(new LambdaQueryWrapper<TmsCarrier>()
                .eq(TmsCarrier::getTenantId, tenantId).eq(TmsCarrier::getFactoryId, factoryId).eq(TmsCarrier::getDeleted, 0));
        return Result.success(list);
    }

    // ---------- 回传任务 (Channel Shipment Ack) ----------

    @GetMapping("/acks")
    @RequirePerm("tms:ack:page")
    public Result<IPage<ChannelShipmentAck>> pageAcks(
            @RequestParam(name = "current", defaultValue = "1") Integer current,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "channel", required = false) String channel,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "orderId", required = false) String orderId,
            @RequestParam(name = "trackingNo", required = false) String trackingNo) {
        return Result.success(channelAckApplicationService.page(new Page<>(current, size), channel, status, orderId, trackingNo));
    }

    @GetMapping("/acks/{id}")
    @RequirePerm("tms:ack:get")
    public Result<ChannelShipmentAck> getAck(@PathVariable Long id) {
        return Result.success(channelAckApplicationService.getAck(id));
    }

    @PostMapping("/acks/{id}/retry")
    @RequirePerm("tms:ack:retry")
    public Result<Void> retryAck(@PathVariable Long id) {
        channelAckApplicationService.manualRetry(id);
        return Result.success();
    }

    @PostMapping("/acks/createFromOutbound")
    @RequirePerm("tms:ack:create")
    public Result<Long> createAckFromOutbound(@RequestBody CreateAckFromOutboundBody body) {
        if (body == null || body.getOutboundId() == null) {
            throw new com.pod.common.core.exception.BusinessException(400, "outboundId required");
        }
        return Result.success(channelAckApplicationService.createAckFromOutbound(body.getOutboundId()));
    }

    public static class CreateAckFromOutboundBody {
        private Long outboundId;
        public Long getOutboundId() { return outboundId; }
        public void setOutboundId(Long outboundId) { this.outboundId = outboundId; }
    }
}
