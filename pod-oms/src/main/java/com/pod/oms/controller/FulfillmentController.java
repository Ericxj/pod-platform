package com.pod.oms.controller;

import com.pod.common.core.domain.Result;
import com.pod.oms.domain.Fulfillment;
import com.pod.oms.service.FulfillmentApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oms")
public class FulfillmentController {

    @Autowired
    private FulfillmentApplicationService fulfillmentService;

    @PostMapping("/orders/{id}/createFulfillment")
    public Result<Long> createFulfillment(@PathVariable("id") Long id) {
        return Result.success(fulfillmentService.createFulfillment(id));
    }

    @PostMapping("/fulfillments/{id}/release")
    public Result<Void> release(@PathVariable("id") Long id) {
        fulfillmentService.releaseFulfillment(id);
        return Result.success();
    }
    
    @GetMapping("/fulfillments/{id}")
    public Result<Fulfillment> get(@PathVariable("id") Long id) {
        return Result.success(fulfillmentService.getFulfillment(id));
    }
}
