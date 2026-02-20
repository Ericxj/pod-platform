package com.pod.srm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.srm.domain.Supplier;
import com.pod.srm.service.SupplierApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/srm/suppliers")
public class SupplierController {

    @Autowired
    private SupplierApplicationService supplierApplicationService;

    @GetMapping
//    @RequirePerm("srm:supplier:page")
    public Result<IPage<Supplier>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(supplierApplicationService.page(new Page<>(current, size), keyword, status));
    }

    @GetMapping("/list")
//    @RequirePerm("srm:supplier:page")
    public Result<List<Supplier>> list(@RequestParam(required = false) String status) {
        return Result.success(supplierApplicationService.list(status));
    }

    @GetMapping("/{id}")
    @RequirePerm("srm:supplier:get")
    public Result<Supplier> get(@PathVariable Long id) {
        return Result.success(supplierApplicationService.get(id));
    }

    @PostMapping
    @RequirePerm("srm:supplier:create")
    public Result<Supplier> create(@RequestBody Supplier body) {
        return Result.success(supplierApplicationService.create(body));
    }

    @PutMapping("/{id}")
    @RequirePerm("srm:supplier:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody Supplier body) {
        supplierApplicationService.update(id, body);
        return Result.success();
    }

    @PostMapping("/{id}/enable")
    @RequirePerm("srm:supplier:enable")
    public Result<Void> enable(@PathVariable Long id) {
        supplierApplicationService.enable(id);
        return Result.success();
    }

    @PostMapping("/{id}/disable")
    @RequirePerm("srm:supplier:disable")
    public Result<Void> disable(@PathVariable Long id) {
        supplierApplicationService.disable(id);
        return Result.success();
    }
}
