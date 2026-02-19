package com.pod.oms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.oms.domain.Fulfillment;
import com.pod.oms.service.FulfillmentApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ful/fulfillments")
public class FulFulfillmentController {

    @Autowired
    private FulfillmentApplicationService fulfillmentApplicationService;

    @GetMapping
    @RequirePerm("ful:fulfillment:page")
    public Result<IPage<Fulfillment>> page(
            @RequestParam(name = "current", defaultValue = "1") Integer current,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "fulfillmentNo", required = false) String fulfillmentNo) {
        return Result.success(fulfillmentApplicationService.page(new Page<>(current, size), status, fulfillmentNo));
    }

    @GetMapping("/{id}")
    @RequirePerm("ful:fulfillment:get")
    public Result<Fulfillment> get(@PathVariable("id") Long id) {
        return Result.success(fulfillmentApplicationService.getFulfillment(id));
    }

    @PostMapping("/{id}/reserve/retry")
    @RequirePerm("ful:fulfillment:reserve-retry")
    public Result<Void> retryReserve(@PathVariable("id") Long id) {
        fulfillmentApplicationService.retryReserve(id);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    @RequirePerm("ful:fulfillment:cancel")
    public Result<Void> cancel(@PathVariable("id") Long id) {
        fulfillmentApplicationService.cancelFulfillment(id);
        return Result.success();
    }
}
