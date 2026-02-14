package com.pod.oms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.domain.Result;
import com.pod.oms.domain.UnifiedOrder;
import com.pod.oms.service.OrderApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oms/orders")
public class OrderController {

    @Autowired
    private OrderApplicationService orderService;

    @GetMapping("/page")
    public Result<IPage<UnifiedOrder>> page(
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(orderService.pageOrders(new Page<>(page, size), orderNo));
    }

    @GetMapping("/{id}")
    public Result<UnifiedOrder> get(@PathVariable("id") Long id) {
        return Result.success(orderService.getOrder(id));
    }

    @PostMapping("/{id}/validate")
    public Result<Void> validate(@PathVariable("id") Long id) {
        orderService.validateOrder(id);
        return Result.success();
    }
}
