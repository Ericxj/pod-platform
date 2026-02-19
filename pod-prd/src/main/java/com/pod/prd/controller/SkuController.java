package com.pod.prd.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.prd.domain.Sku;
import com.pod.prd.dto.SkuCreateRequest;
import com.pod.prd.dto.SkuUpdateRequest;
import com.pod.prd.service.PrdApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prd/sku")
public class SkuController {
    @Autowired
    private PrdApplicationService prdService;

    @GetMapping("/page")
    @RequirePerm("prd:sku:page")
    public Result<IPage<Sku>> page(
            @RequestParam(name = "current", defaultValue = "1") Integer current,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "spuId", required = false) Long spuId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {
        return Result.success(prdService.pageSku(new Page<>(current, size), spuId, keyword, status));
    }

    @GetMapping("/{id}")
    @RequirePerm("prd:sku:get")
    public Result<Sku> get(@PathVariable("id") Long id) {
        return Result.success(prdService.getSku(id));
    }

    @PostMapping
    @RequirePerm("prd:sku:create")
    public Result<Sku> create(@RequestBody SkuCreateRequest req) {
        return Result.success(prdService.createSku(req.getSpuId(), req.getSkuCode(), req.getSkuName(), req.getPrice(), req.getWeightG(), req.getAttributesJson()));
    }

    @PutMapping("/{id}")
    @RequirePerm("prd:sku:update")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody SkuUpdateRequest req) {
        prdService.updateSku(id, req.getSkuName(), req.getPrice(), req.getWeightG(), req.getAttributesJson());
        return Result.success();
    }

    @PostMapping("/{id}/activate")
    @RequirePerm("prd:sku:activate")
    public Result<Void> activate(@PathVariable("id") Long id) {
        prdService.activateSku(id);
        return Result.success();
    }

    @PostMapping("/{id}/deactivate")
    @RequirePerm("prd:sku:deactivate")
    public Result<Void> deactivate(@PathVariable("id") Long id) {
        prdService.deactivateSku(id);
        return Result.success();
    }
}
