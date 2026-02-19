package com.pod.oms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.oms.domain.OrderHold;
import com.pod.oms.service.HoldApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oms/holds")
public class HoldController {

    @Autowired
    private HoldApplicationService holdApplicationService;

    @GetMapping
    @RequirePerm("oms:hold:page")
    public Result<IPage<OrderHold>> page(
            @RequestParam(name = "current", defaultValue = "1") Integer current,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "type", required = false) String holdType,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "channel", required = false) String channel,
            @RequestParam(name = "shopId", required = false) String shopId) {
        return Result.success(holdApplicationService.page(new Page<>(current, size), holdType, status, channel, shopId));
    }

    @GetMapping("/{id}")
    @RequirePerm("oms:hold:get")
    public Result<OrderHold> get(@PathVariable("id") Long id) {
        return Result.success(holdApplicationService.get(id));
    }

    @PostMapping("/{id}/resolve")
    @RequirePerm("oms:hold:resolve")
    public Result<Void> resolve(@PathVariable("id") Long id, @RequestBody HoldResolveRequest body) {
        holdApplicationService.resolve(id, body.getSkuId());
        return Result.success();
    }

    public static class HoldResolveRequest {
        private Long skuId;
        public Long getSkuId() { return skuId; }
        public void setSkuId(Long skuId) { this.skuId = skuId; }
    }
}
