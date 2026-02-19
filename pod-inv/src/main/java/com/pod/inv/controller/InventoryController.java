package com.pod.inv.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.domain.Result;
import com.pod.inv.domain.InventoryBalance;
import com.pod.inv.domain.InventoryLedger;
import com.pod.inv.domain.InventoryReservation;
import com.pod.inv.service.InventoryApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inv")
public class InventoryController {

    @Autowired
    private InventoryApplicationService inventoryService;

    @GetMapping("/balances")
    public Result<IPage<InventoryBalance>> pageBalances(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long skuId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(inventoryService.pageBalances(new Page<>(page, size), warehouseId, skuId));
    }

    @GetMapping("/reservations")
    public Result<IPage<InventoryReservation>> pageReservations(
            @RequestParam(required = false) String bizNo,
            @RequestParam(required = false) Long skuId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(inventoryService.pageReservations(new Page<>(page, size), bizNo, skuId));
    }

    @GetMapping("/ledgers")
    public Result<IPage<InventoryLedger>> pageLedgers(
            @RequestParam(required = false) Long skuId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(inventoryService.pageLedgers(new Page<>(page, size), skuId));
    }
}
