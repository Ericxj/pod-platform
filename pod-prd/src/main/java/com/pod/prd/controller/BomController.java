package com.pod.prd.controller;

import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.prd.domain.Bom;
import com.pod.prd.domain.BomItem;
import com.pod.prd.dto.BomSaveRequest;
import com.pod.prd.service.PrdApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prd/bom")
public class BomController {
    @Autowired
    private PrdApplicationService prdService;

    @GetMapping("/by-sku")
    @RequirePerm("prd:bom:get")
    public Result<Bom> getBySkuId(@RequestParam("skuId") Long skuId) {
        Bom bom = prdService.getBomBySkuId(skuId);
        return Result.success(bom);
    }

    @GetMapping("/{id}")
    @RequirePerm("prd:bom:get")
    public Result<Bom> get(@PathVariable("id") Long id) {
        return Result.success(prdService.getBom(id));
    }

    @GetMapping("/{id}/items")
    @RequirePerm("prd:bom:get")
    public Result<List<BomItem>> items(@PathVariable("id") Long id) {
        return Result.success(prdService.listBomItems(id));
    }

    @PostMapping("/save")
    @RequirePerm("prd:bom:save")
    public Result<Bom> save(@RequestBody BomSaveRequest req) {
        return Result.success(prdService.saveBom(req.getSkuId(), req.getVersionNo(), req.getRemark(), req.getItems()));
    }

    @PostMapping("/{id}/publish")
    @RequirePerm("prd:bom:publish")
    public Result<Void> publish(@PathVariable("id") Long id) {
        prdService.publishBom(id);
        return Result.success();
    }

    @PostMapping("/{id}/unpublish")
    @RequirePerm("prd:bom:unpublish")
    public Result<Void> unpublish(@PathVariable("id") Long id) {
        prdService.unpublishBom(id);
        return Result.success();
    }
}
