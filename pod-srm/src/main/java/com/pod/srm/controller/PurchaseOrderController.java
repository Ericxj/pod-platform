package com.pod.srm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.srm.domain.PurchaseOrder;
import com.pod.srm.service.PurchaseOrderApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/srm/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderApplicationService purchaseOrderApplicationService;

    @GetMapping
//    @RequirePerm("srm:po:page")
    public Result<IPage<PurchaseOrder>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status) {
        return Result.success(purchaseOrderApplicationService.page(new Page<>(current, size), supplierId, status));
    }

    @GetMapping("/{id}")
    @RequirePerm("srm:po:get")
    public Result<PurchaseOrder> get(@PathVariable Long id) {
        return Result.success(purchaseOrderApplicationService.get(id));
    }

    @PostMapping
    @RequirePerm("srm:po:create")
    public Result<PurchaseOrder> create(@RequestBody Map<String, Object> body) {
        Long supplierId = body.get("supplierId") != null ? Long.valueOf(body.get("supplierId").toString()) : null;
        String currency = body.get("currency") != null ? body.get("currency").toString() : "CNY";
        LocalDate expectedArriveDate = null;
        if (body.get("expectedArriveDate") != null && !body.get("expectedArriveDate").toString().isBlank()) {
            expectedArriveDate = LocalDate.parse(body.get("expectedArriveDate").toString());
        }
        return Result.success(purchaseOrderApplicationService.createDraft(supplierId, currency, expectedArriveDate));
    }

    @PostMapping("/{id}/lines")
    @RequirePerm("srm:po:addLine")
    public Result<Void> addLine(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long skuId = body.get("skuId") != null ? Long.valueOf(body.get("skuId").toString()) : null;
        BigDecimal qtyOrdered = body.get("qtyOrdered") != null ? new BigDecimal(body.get("qtyOrdered").toString()) : BigDecimal.ZERO;
        BigDecimal unitPrice = body.get("unitPrice") != null ? new BigDecimal(body.get("unitPrice").toString()) : BigDecimal.ZERO;
        purchaseOrderApplicationService.addLine(id, skuId, qtyOrdered, unitPrice);
        return Result.success();
    }

    @PutMapping("/{id}/lines/{lineId}")
    @RequirePerm("srm:po:updateLine")
    public Result<Void> updateLine(@PathVariable Long id, @PathVariable Long lineId, @RequestBody Map<String, Object> body) {
        BigDecimal qtyOrdered = body.get("qtyOrdered") != null ? new BigDecimal(body.get("qtyOrdered").toString()) : null;
        BigDecimal unitPrice = body.get("unitPrice") != null ? new BigDecimal(body.get("unitPrice").toString()) : null;
        purchaseOrderApplicationService.updateLine(id, lineId, qtyOrdered, unitPrice);
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    @RequirePerm("srm:po:submit")
    public Result<Void> submit(@PathVariable Long id) {
        purchaseOrderApplicationService.submit(id);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    @RequirePerm("srm:po:approve")
    public Result<Void> approve(@PathVariable Long id) {
        purchaseOrderApplicationService.approve(id);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    @RequirePerm("srm:po:cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        purchaseOrderApplicationService.cancel(id);
        return Result.success();
    }

    @PostMapping("/{id}/close")
    @RequirePerm("srm:po:close")
    public Result<Void> close(@PathVariable Long id) {
        purchaseOrderApplicationService.close(id);
        return Result.success();
    }
}
