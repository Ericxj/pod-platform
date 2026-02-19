package com.pod.oms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.oms.domain.UnifiedOrder;
import com.pod.oms.service.UnifiedOrderApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oms/unified-orders")
public class UnifiedOrderController {

    @Autowired
    private UnifiedOrderApplicationService unifiedOrderApplicationService;

    @GetMapping
    @RequirePerm("oms:unified-order:page")
    public Result<IPage<UnifiedOrder>> page(
            @RequestParam(name = "current", defaultValue = "1") Integer current,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "channel", required = false) String channel,
            @RequestParam(name = "shopId", required = false) String shopId,
            @RequestParam(name = "externalOrderId", required = false) String externalOrderId,
            @RequestParam(name = "orderStatus", required = false) String orderStatus) {
        return Result.success(unifiedOrderApplicationService.page(new Page<>(current, size), channel, shopId, externalOrderId, orderStatus));
    }

    @GetMapping("/{id}")
    @RequirePerm("oms:unified-order:get")
    public Result<UnifiedOrder> get(@PathVariable("id") Long id) {
        return Result.success(unifiedOrderApplicationService.get(id));
    }
}
